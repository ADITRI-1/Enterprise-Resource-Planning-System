package com.iiitd.erp.core.service.impl;

import com.iiitd.erp.Domain.*;
import com.iiitd.erp.core.AssessmentScorePersistPort;
import com.iiitd.erp.core.SectionPersistPort;
import com.iiitd.erp.core.service.AssessmentService;
import com.iiitd.erp.core.service.ScoreService;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.io.dto.request.UpdateStudentScoresRequest;
import com.iiitd.erp.persistent.persistence.repository.EnrollmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class ScoreServiceImpl implements ScoreService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScoreServiceImpl.class);

    private final SectionPersistPort sections;
    private final AssessmentService assessmentService;
    private final AssessmentScorePersistPort scores;
    private final EnrollmentRepository enrollmentRepository;

    public ScoreServiceImpl(SectionPersistPort sections,
                                      AssessmentService assessmentService,
                                      AssessmentScorePersistPort scores,
                                      EnrollmentRepository enrollmentRepository) {
        this.sections = sections;
        this.assessmentService = assessmentService;
        this.scores = scores;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    public InstructorScoreOverviewDomain overview(String instructorCode, String sectionId) {
        SectionDomain section = ensureSectionOwnership(instructorCode, sectionId);
        List<AssessmentComponentDomain> components = assessmentService.list(instructorCode, sectionId);
        Map<UUID, AssessmentComponentDomain> componentMap = components.stream()
                .collect(Collectors.toMap(AssessmentComponentDomain::getComponentId, Function.identity()));

        Map<UUID, InstructorStudentScoreDomain> students = buildStudentMap(sectionId, componentMap.keySet());
        mergeScores(sectionId, students);

        LOGGER.info("Overview generated for instructor {} section {} (students={}, components={})",
                instructorCode, sectionId, students.size(), components.size());
        return InstructorScoreOverviewDomain.builder().section(section).components(components).students(students.values().stream().toList()).build();
    }

    @Override
    public InstructorScoreOverviewDomain updateScores(String instructorCode, String sectionId,
                                                UpdateStudentScoresRequest request, String updatedBy) {
        SectionDomain section = ensureSectionOwnership(instructorCode, sectionId);
        Map<UUID, AssessmentComponentDomain> componentMap = assessmentService.list(instructorCode, sectionId).stream()
                .collect(Collectors.toMap(AssessmentComponentDomain::getComponentId, Function.identity()));
        Map<UUID, UUID> enrollmentToStudent = ensureEnrollments(sectionId);

        for (UpdateStudentScoresRequest.StudentScoreEntry entry : request.entries()) {
            UUID enrollmentId = entry.enrollmentId();
            if (!enrollmentToStudent.containsKey(enrollmentId)) {
                throw ErpException.of(ErrorCode.STUDENT_NOT_IN_SECTION,
                        Map.of("sectionId", sectionId, "enrollmentId", enrollmentId));
            }
            for (UpdateStudentScoresRequest.ComponentScore score : entry.components()) {
                AssessmentComponentDomain component = componentMap.get(score.componentId());
                if (component == null) {
                    throw ErpException.of(ErrorCode.ASSESSMENT_NOT_FOUND,
                            Map.of("componentId", score.componentId()));
                }
                validateScore(score.score(), component);
                scores.upsert(AssessmentScoreDomain.builder()
                        .enrollmentId(enrollmentId)
                        .componentId(component.getComponentId())
                        .score(BigDecimal.valueOf(score.score()))
                        .updatedBy(updatedBy)
                        .updatedAt(OffsetDateTime.now())
                        .build());
            }
        }
        LOGGER.info("Scores updated for instructor {} section {}", instructorCode, sectionId);
        return overview(instructorCode, sectionId);
    }

    @Override
    public byte[] exportCsv(String instructorCode, String sectionId) {
        InstructorScoreOverviewDomain overview = overview(instructorCode, sectionId);
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("EnrollmentId,StudentId,StudentName,RollNo");
            overview.getComponents().forEach(c -> sb.append(',').append(escape(c.getName())));
            sb.append('\n');
            for (InstructorStudentScoreDomain student : overview.getStudents()) {
                sb.append(student.getEnrollmentId()).append(',')
                        .append(student.getStudentId()).append(',')
                        .append(escape(student.getStudentName())).append(',')
                        .append(escape(student.getRollNo()));
                for (AssessmentComponentDomain component : overview.getComponents()) {
                    double val = student.getComponentScores()
                            .getOrDefault(component.getComponentId(), 0.0d);
                    sb.append(',').append(val);
                }
                sb.append('\n');
            }
            return sb.toString().getBytes(StandardCharsets.UTF_8);
        } catch (Exception ex) {
            LOGGER.error("CSV export failed for instructor {} section {}", instructorCode, sectionId, ex);
            throw ErpException.wrap(ErrorCode.ASSESSMENT_SCORE_EXPORT_FAILED, ex,
                    Map.of("sectionId", sectionId));
        }
    }

    @Override
    public InstructorScoreOverviewDomain importCsv(String instructorCode, String sectionId,
                                                   MultipartFile file, String updatedBy) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<String> lines = reader.lines().toList();
            if (lines.isEmpty()) {
                throw ErpException.of(ErrorCode.ASSESSMENT_SCORE_IMPORT_FAILED,
                        Map.of("reason", "empty file"));
            }
            String[] headers = lines.get(0).split(",");
            UpdateStudentScoresRequest request = parseCsv(sectionId, lines, headers);
            return updateScores(instructorCode, sectionId, request, updatedBy);
        } catch (ErpException ex) {
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("CSV import failed for instructor {} section {}", instructorCode, sectionId, ex);
            throw ErpException.wrap(ErrorCode.ASSESSMENT_SCORE_IMPORT_FAILED, ex,
                    Map.of("sectionId", sectionId));
        }
    }

    private UpdateStudentScoresRequest parseCsv(String sectionId, List<String> lines, String[] headers) {
        if (headers.length < 5) {
            throw ErpException.of(ErrorCode.ASSESSMENT_SCORE_IMPORT_FAILED,
                    Map.of("reason", "missing component headers"));
        }
        List<UpdateStudentScoresRequest.StudentScoreEntry> entries = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String[] cols = lines.get(i).split(",");
            if (cols.length != headers.length) {
                throw ErpException.of(ErrorCode.ASSESSMENT_SCORE_IMPORT_FAILED,
                        Map.of("line", i + 1, "reason", "column mismatch"));
            }
            UUID enrollmentId = UUID.fromString(cols[0].trim());
            List<UpdateStudentScoresRequest.ComponentScore> componentScores = new ArrayList<>();
            for (int c = 4; c < headers.length; c++) {
                UUID componentId = UUID.fromString(headers[c].trim());
                double score = Double.parseDouble(cols[c].trim());
                componentScores.add(new UpdateStudentScoresRequest.ComponentScore(componentId, score));
            }
            entries.add(new UpdateStudentScoresRequest.StudentScoreEntry(enrollmentId, componentScores));
        }
        return new UpdateStudentScoresRequest(entries);
    }

    private void validateScore(double score, AssessmentComponentDomain component) {
        if (score < 0 || score > component.getScoreOutOf()) {
            throw ErpException.of(ErrorCode.ASSESSMENT_SCORE_INVALID,
                    Map.of("componentId", component.getComponentId(),
                            "score", score,
                            "scoreOutOf", component.getScoreOutOf()));
        }
    }

    private SectionDomain ensureSectionOwnership(String instructorCode, String sectionId) {
        SectionDomain section = sections.findBySectionId(sectionId);
        if (section == null) {
            throw ErpException.of(ErrorCode.SECTION_NOT_FOUND, Map.of("sectionId", sectionId));
        }
        if (!instructorCode.equalsIgnoreCase(section.getInstructorCode())) {
            throw ErpException.of(ErrorCode.SECTION_NOT_OWNED_BY_INSTRUCTOR,
                    Map.of("sectionId", sectionId, "instructorCode", instructorCode));
        }
        return section;
    }

    private Map<UUID, InstructorStudentScoreDomain> buildStudentMap(String sectionId, Set<UUID> componentIds) {
        List<EnrollmentRepository.SectionEnrollmentProjection> projections = enrollmentRepository.findActiveStudents(sectionId);
        return projections.stream()
                .collect(Collectors.toMap(
                        EnrollmentRepository.SectionEnrollmentProjection::getEnrollmentId,
                        proj -> InstructorStudentScoreDomain.builder()
                                .enrollmentId(proj.getEnrollmentId())
                                .studentId(proj.getStudentId())
                                .studentName(proj.getStudentName())
                                .rollNo(proj.getRollNo())
                                .componentScores(componentIds.stream().collect(Collectors.toMap(Function.identity(), id -> 0.0d,
                                        (a, b) -> a, LinkedHashMap::new)))
                                .build(),
                        (a, b) -> a,
                        LinkedHashMap::new));
    }

    private Map<UUID, UUID> ensureEnrollments(String sectionId) {
        List<EnrollmentRepository.SectionEnrollmentProjection> projections = enrollmentRepository.findActiveStudents(sectionId);
        if (projections.isEmpty()) {
            throw ErpException.of(ErrorCode.ENROLLMENT_NOT_FOUND, Map.of("sectionId", sectionId));
        }
        return projections.stream()
                .collect(Collectors.toMap(EnrollmentRepository.SectionEnrollmentProjection::getEnrollmentId,
                        EnrollmentRepository.SectionEnrollmentProjection::getStudentId));
    }

    private void mergeScores(String sectionId, Map<UUID, InstructorStudentScoreDomain> students) {
        List<AssessmentScoreDomain> existing = scores.findBySection(sectionId);
        existing.forEach(scoreDomain -> {
            InstructorStudentScoreDomain row = students.get(scoreDomain.getEnrollmentId());
            if (row != null) {
                row.getComponentScores().put(scoreDomain.getComponentId(), scoreDomain.getScore().doubleValue());
            }
        });
    }

    private String escape(String value) {
        if (value == null) return "";
        return '"' + value.replace("\"", "\"\"") + '"';
    }
}
