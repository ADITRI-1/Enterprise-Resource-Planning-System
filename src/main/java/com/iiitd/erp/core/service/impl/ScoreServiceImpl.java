package com.iiitd.erp.core.service.impl;

import com.iiitd.erp.Domain.*;
import com.iiitd.erp.core.AssessmentScorePersistPort;
import com.iiitd.erp.core.SectionPersistPort;
import com.iiitd.erp.core.service.AssessmentService;
import com.iiitd.erp.core.service.NotificationService;
import com.iiitd.erp.core.service.ScoreService;
import com.iiitd.erp.core.service.support.GradeScale;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.io.dto.request.UpdateStudentScoresRequest;
import com.iiitd.erp.persistent.entity.erp_db.Grade;
import com.iiitd.erp.persistent.persistence.repository.EnrollmentRepository;
import com.iiitd.erp.persistent.persistence.repository.GradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class ScoreServiceImpl implements ScoreService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScoreServiceImpl.class);
    public static final String FINAL_COMPONENT = "FINAL";
    private static final BigDecimal WEIGHT_TOLERANCE = new BigDecimal("0.001");

    private final SectionPersistPort sections;
    private final AssessmentService assessmentService;
    private final AssessmentScorePersistPort scores;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository grades;
    private final NotificationService notifications;

    public ScoreServiceImpl(SectionPersistPort sections,
                            AssessmentService assessmentService,
                            AssessmentScorePersistPort scores,
                            EnrollmentRepository enrollmentRepository,
                            GradeRepository grades,
                            NotificationService notifications) {
        this.notifications = notifications;
        this.sections = sections;
        this.assessmentService = assessmentService;
        this.scores = scores;
        this.enrollmentRepository = enrollmentRepository;
        this.grades = grades;
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
    public List<FinalGradeDomain> computeFinalGrades(String instructorCode, String sectionId) {
        InstructorScoreOverviewDomain overview = overview(instructorCode, sectionId);
        List<AssessmentComponentDomain> components = overview.getComponents();
        BigDecimal weightSum = components.stream()
                .map(AssessmentComponentDomain::getWeight)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (components.isEmpty() || weightSum.subtract(BigDecimal.ONE).abs().compareTo(WEIGHT_TOLERANCE) > 0) {
            throw ErpException.of(ErrorCode.ASSESSMENT_WEIGHTS_INCOMPLETE,
                    Map.of("sectionId", sectionId, "weightSum", weightSum));
        }

        List<FinalGradeDomain> results = new ArrayList<>();
        for (InstructorStudentScoreDomain student : overview.getStudents()) {
            // total = sum(score / outOf * weight) * 100; a missing score counts as 0
            BigDecimal total = BigDecimal.ZERO;
            for (AssessmentComponentDomain component : components) {
                double score = student.getComponentScores().getOrDefault(component.getComponentId(), 0.0d);
                total = total.add(BigDecimal.valueOf(score)
                        .divide(BigDecimal.valueOf(component.getScoreOutOf()), 6, RoundingMode.HALF_UP)
                        .multiply(component.getWeight()));
            }
            total = total.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
            String letter = GradeScale.letterFor(total);

            Grade grade = grades.findByEnrollmentIdAndComponent(student.getEnrollmentId(), FINAL_COMPONENT)
                    .orElseGet(() -> Grade.builder()
                            .gradeId(UUID.randomUUID())
                            .enrollmentId(student.getEnrollmentId())
                            .component(FINAL_COMPONENT)
                            .build());
            grade.setScore(total);
            grade.setWeightedScore(total);
            grade.setFinalGrade(letter);
            grades.save(grade);

            notifications.notify(student.getStudentId(), NotificationService.GRADES,
                    "Final grade published for " + overview.getSection().getCourseId(),
                    "Your final grade is " + letter + " (" + total.toPlainString() + " / 100).",
                    "/student/grades");

            results.add(FinalGradeDomain.builder()
                    .enrollmentId(student.getEnrollmentId())
                    .studentId(student.getStudentId())
                    .rollNo(student.getRollNo())
                    .studentName(student.getStudentName())
                    .totalScore(total)
                    .finalGrade(letter)
                    .build());
        }
        LOGGER.info("Computed final grades for {} students in section {}", results.size(), sectionId);
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinalGradeDomain> finalGrades(String instructorCode, String sectionId) {
        ensureSectionOwnership(instructorCode, sectionId);
        List<EnrollmentRepository.SectionEnrollmentProjection> students = enrollmentRepository.findActiveStudents(sectionId);
        Map<UUID, Grade> finals = grades.findByEnrollmentIdInAndComponent(
                        students.stream().map(EnrollmentRepository.SectionEnrollmentProjection::getEnrollmentId).toList(),
                        FINAL_COMPONENT)
                .stream()
                .collect(Collectors.toMap(Grade::getEnrollmentId, Function.identity()));
        return students.stream()
                .map(p -> {
                    Grade g = finals.get(p.getEnrollmentId());
                    return FinalGradeDomain.builder()
                            .enrollmentId(p.getEnrollmentId())
                            .studentId(p.getStudentId())
                            .rollNo(p.getRollNo())
                            .studentName(p.getStudentName())
                            .totalScore(g == null ? null : g.getScore())
                            .finalGrade(g == null ? null : g.getFinalGrade())
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SectionStatsView sectionStats(String instructorCode, String sectionId) {
        SectionDomain section = ensureSectionOwnership(instructorCode, sectionId);
        List<AssessmentComponentDomain> components = assessmentService.list(instructorCode, sectionId);
        List<EnrollmentRepository.SectionEnrollmentProjection> students = enrollmentRepository.findActiveStudents(sectionId);
        Set<UUID> activeEnrollments = students.stream()
                .map(EnrollmentRepository.SectionEnrollmentProjection::getEnrollmentId)
                .collect(Collectors.toSet());

        // Only real, entered scores count: a student without a score is "not graded", not 0.
        Map<UUID, List<Double>> scoresByComponent = scores.findBySection(sectionId).stream()
                .filter(sc -> activeEnrollments.contains(sc.getEnrollmentId()))
                .collect(Collectors.groupingBy(AssessmentScoreDomain::getComponentId,
                        Collectors.mapping(sc -> sc.getScore().doubleValue(), Collectors.toList())));

        List<SectionStatsView.ComponentStats> componentStats = components.stream()
                .map(c -> {
                    List<Double> values = scoresByComponent.getOrDefault(c.getComponentId(), List.of());
                    Double average = average(values);
                    Double averagePercent = average == null ? null : round(average * 100 / c.getScoreOutOf());
                    return new SectionStatsView.ComponentStats(c.getComponentId(), c.getName(), c.getWeight(),
                            c.getScoreOutOf(), values.size(), average, averagePercent,
                            values.stream().min(Double::compare).orElse(null),
                            values.stream().max(Double::compare).orElse(null),
                            median(values));
                })
                .toList();

        List<Grade> finals = grades.findByEnrollmentIdInAndComponent(activeEnrollments, FINAL_COMPONENT);
        List<Double> totals = finals.stream().map(g -> g.getScore().doubleValue()).toList();
        Map<String, Long> distribution = new LinkedHashMap<>();
        GradeScale.letters().forEach(letter -> distribution.put(letter, 0L));
        finals.forEach(g -> distribution.merge(g.getFinalGrade(), 1L, Long::sum));

        return new SectionStatsView(section.getSectionId(), section.getCourseId(), students.size(), componentStats,
                new SectionStatsView.FinalStats(finals.size(), average(totals),
                        totals.stream().min(Double::compare).orElse(null),
                        totals.stream().max(Double::compare).orElse(null),
                        distribution));
    }

    private static Double average(List<Double> values) {
        return values.isEmpty() ? null : round(values.stream().mapToDouble(Double::doubleValue).average().orElse(0));
    }

    private static Double median(List<Double> values) {
        if (values.isEmpty()) {
            return null;
        }
        List<Double> sorted = values.stream().sorted().toList();
        int mid = sorted.size() / 2;
        return round(sorted.size() % 2 == 1 ? sorted.get(mid) : (sorted.get(mid - 1) + sorted.get(mid)) / 2);
    }

    private static double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
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
            UpdateStudentScoresRequest request = parseCsv(instructorCode, sectionId, lines, headers);
            return updateScores(instructorCode, sectionId, request, updatedBy);
        } catch (ErpException ex) {
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("CSV import failed for instructor {} section {}", instructorCode, sectionId, ex);
            throw ErpException.wrap(ErrorCode.ASSESSMENT_SCORE_IMPORT_FAILED, ex,
                    Map.of("sectionId", sectionId));
        }
    }

    private UpdateStudentScoresRequest parseCsv(String instructorCode, String sectionId,
                                                List<String> lines, String[] headers) {
        if (headers.length < 5) {
            throw ErpException.of(ErrorCode.ASSESSMENT_SCORE_IMPORT_FAILED,
                    Map.of("reason", "missing component headers"));
        }
        // Columns 4.. are components; the export writes names, but ids are accepted too.
        Map<String, UUID> componentsByKey = new HashMap<>();
        for (AssessmentComponentDomain component : assessmentService.list(instructorCode, sectionId)) {
            componentsByKey.put(component.getName().trim().toLowerCase(Locale.ROOT), component.getComponentId());
            componentsByKey.put(component.getComponentId().toString(), component.getComponentId());
        }
        List<UUID> columnComponents = new ArrayList<>();
        for (int c = 4; c < headers.length; c++) {
            String key = unquote(headers[c]).toLowerCase(Locale.ROOT);
            UUID componentId = componentsByKey.get(key);
            if (componentId == null) {
                throw ErpException.of(ErrorCode.ASSESSMENT_SCORE_IMPORT_FAILED,
                        Map.of("reason", "unknown component column", "column", unquote(headers[c])));
            }
            columnComponents.add(componentId);
        }

        List<UpdateStudentScoresRequest.StudentScoreEntry> entries = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).isBlank()) continue;
            String[] cols = lines.get(i).split(",", -1);
            if (cols.length != headers.length) {
                throw ErpException.of(ErrorCode.ASSESSMENT_SCORE_IMPORT_FAILED,
                        Map.of("line", i + 1, "reason", "column mismatch"));
            }
            UUID enrollmentId = UUID.fromString(unquote(cols[0]));
            List<UpdateStudentScoresRequest.ComponentScore> componentScores = new ArrayList<>();
            for (int c = 4; c < headers.length; c++) {
                String raw = unquote(cols[c]);
                if (raw.isEmpty()) continue;
                componentScores.add(new UpdateStudentScoresRequest.ComponentScore(
                        columnComponents.get(c - 4), Double.parseDouble(raw)));
            }
            entries.add(new UpdateStudentScoresRequest.StudentScoreEntry(enrollmentId, componentScores));
        }
        return new UpdateStudentScoresRequest(entries);
    }

    private static String unquote(String value) {
        String v = value.trim();
        if (v.length() >= 2 && v.startsWith("\"") && v.endsWith("\"")) {
            v = v.substring(1, v.length() - 1).replace("\"\"", "\"");
        }
        return v.trim();
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
