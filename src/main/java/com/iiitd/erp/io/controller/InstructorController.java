package com.iiitd.erp.io.controller;

import com.iiitd.erp.Domain.AssessmentComponentDomain;
import com.iiitd.erp.Domain.FinalGradeDomain;
import com.iiitd.erp.Domain.InstructorScoreOverviewDomain;
import com.iiitd.erp.Domain.SectionStatsView;
import com.iiitd.erp.core.service.AssessmentService;
import com.iiitd.erp.core.service.ScoreService;
import com.iiitd.erp.core.service.SectionService;
import com.iiitd.erp.core.service.TimetableService;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.io.controller.support.CurrentUser;
import com.iiitd.erp.io.dto.request.AssessmentComponentRequest;
import com.iiitd.erp.io.dto.request.UpdateStudentScoresRequest;
import com.iiitd.erp.io.dto.response.*;
import com.iiitd.erp.persistent.entity.erp_db.Instructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.iiitd.erp.mappers.DomainToResponseMapper.DOMAIN_TO_RESPONSE_MAPPER_INSTANCE;
import static com.iiitd.erp.mappers.RequestToDomainMapper.REQUEST_TO_DOMAIN_MAPPER_INSTANCE;

/**
 * Instructor self-service. {instructorCode} comes from GET /api/instructor/me; instructors can
 * only use their own code, and section-level operations additionally check they teach the section.
 */
@RestController
@RequestMapping("/api/instructor")
@Tag(name = "Instructor")
public class InstructorController {

    private final AssessmentService assessments;
    private final SectionService sectionService;
    private final TimetableService timetableService;
    private final ScoreService scoreService;
    private final CurrentUser currentUser;

    public InstructorController(AssessmentService assessments,
                                SectionService sectionService,
                                TimetableService timetableService,
                                ScoreService scoreService,
                                CurrentUser currentUser) {
        this.assessments = assessments;
        this.sectionService = sectionService;
        this.timetableService = timetableService;
        this.scoreService = scoreService;
        this.currentUser = currentUser;
    }

    @GetMapping("/me")
    @Operation(summary = "Logged-in instructor's profile (includes the instructorCode used in other URLs)")
    public InstructorProfileResponse me() {
        Instructor i = currentUser.instructor()
                .orElseThrow(() -> ErpException.of(ErrorCode.INSTRUCTOR_FORBIDDEN, Map.of()));
        return new InstructorProfileResponse(i.getInstructorId(), currentUser.username(), i.getInstructorCode(),
                i.getName(), i.getDepartmentId(), i.getEmail(), i.getPhone());
    }

    @GetMapping("/{instructorCode}/sections")
    @Operation(summary = "My sections")
    public List<InstructorSectionResponse> listSections(@PathVariable String instructorCode,
                                                        @RequestParam(required = false) String semester,
                                                        @RequestParam(required = false) Integer year) {
        currentUser.requireInstructor(instructorCode);
        return sectionService.sectionsForInstructor(instructorCode, semester, year).stream()
                .map(DOMAIN_TO_RESPONSE_MAPPER_INSTANCE::sectionDomainToInstructorSectionResponse)
                .toList();
    }

    @GetMapping("/{instructorCode}/timetable")
    @Operation(summary = "My weekly timetable")
    public List<InstructorTimetableSlotResponse> timetable(@PathVariable String instructorCode,
                                                           @RequestParam(required = false) String semester,
                                                           @RequestParam(required = false) Integer year) {
        currentUser.requireInstructor(instructorCode);
        return timetableService.timetable(instructorCode, semester, year).stream()
                .map(DOMAIN_TO_RESPONSE_MAPPER_INSTANCE::instructorTimetableSlotDomainToResponse)
                .toList();
    }

    // ----- Assessment components -----

    @GetMapping("/{instructorCode}/sections/{sectionId}/assessments")
    @Operation(summary = "Assessment components (e.g. Quiz 20%, Midsem 30%, Endsem 50%)")
    public List<AssessmentComponentResponse> listAssessments(@PathVariable String instructorCode,
                                                             @PathVariable String sectionId) {
        currentUser.requireInstructor(instructorCode);
        return assessments.list(instructorCode, sectionId).stream()
                .map(DOMAIN_TO_RESPONSE_MAPPER_INSTANCE::assessmentComponentDomainToResponse)
                .toList();
    }

    @PostMapping("/{instructorCode}/sections/{sectionId}/assessments")
    @ResponseStatus(HttpStatus.CREATED)
    public AssessmentComponentResponse createAssessment(@PathVariable String instructorCode,
                                                        @PathVariable String sectionId,
                                                        @RequestBody @Valid AssessmentComponentRequest request) {
        currentUser.requireInstructor(instructorCode);
        AssessmentComponentDomain domain = REQUEST_TO_DOMAIN_MAPPER_INSTANCE
                .assessmentComponentRequestToDomain(request)
                .withSectionId(sectionId);
        return DOMAIN_TO_RESPONSE_MAPPER_INSTANCE.assessmentComponentDomainToResponse(
                assessments.create(instructorCode, domain));
    }

    @PutMapping("/{instructorCode}/sections/{sectionId}/assessments/{componentId}")
    public AssessmentComponentResponse updateAssessment(@PathVariable String instructorCode,
                                                        @PathVariable String sectionId,
                                                        @PathVariable UUID componentId,
                                                        @RequestBody @Valid AssessmentComponentRequest request) {
        currentUser.requireInstructor(instructorCode);
        AssessmentComponentDomain domain = REQUEST_TO_DOMAIN_MAPPER_INSTANCE
                .assessmentComponentRequestToDomain(request)
                .withSectionId(sectionId);
        return DOMAIN_TO_RESPONSE_MAPPER_INSTANCE.assessmentComponentDomainToResponse(
                assessments.update(instructorCode, componentId, domain));
    }

    @DeleteMapping("/{instructorCode}/sections/{sectionId}/assessments/{componentId}")
    public AssessmentComponentResponse deleteAssessment(@PathVariable String instructorCode,
                                                        @PathVariable String sectionId,
                                                        @PathVariable UUID componentId) {
        currentUser.requireInstructor(instructorCode);
        return DOMAIN_TO_RESPONSE_MAPPER_INSTANCE.assessmentComponentDomainToResponse(
                assessments.delete(instructorCode, componentId));
    }

    // ----- Scores -----

    @GetMapping("/{instructorCode}/sections/{sectionId}/students")
    @Operation(summary = "Gradebook: enrolled students with their component scores")
    public InstructorScoreOverviewResponse listStudents(@PathVariable String instructorCode,
                                                        @PathVariable String sectionId) {
        currentUser.requireInstructor(instructorCode);
        return toScoreResponse(scoreService.overview(instructorCode, sectionId));
    }

    @PutMapping("/{instructorCode}/sections/{sectionId}/scores")
    @Operation(summary = "Save scores")
    public InstructorScoreOverviewResponse updateScores(@PathVariable String instructorCode,
                                                        @PathVariable String sectionId,
                                                        @RequestBody @Valid UpdateStudentScoresRequest request) {
        currentUser.requireInstructor(instructorCode);
        return toScoreResponse(scoreService.updateScores(instructorCode, sectionId, request, currentUser.username()));
    }

    @GetMapping("/{instructorCode}/sections/{sectionId}/scores/export")
    @Operation(summary = "Download the gradebook as CSV (can be edited and re-imported)")
    public ResponseEntity<byte[]> exportScores(@PathVariable String instructorCode,
                                               @PathVariable String sectionId) {
        currentUser.requireInstructor(instructorCode);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename("scores-" + sectionId + ".csv").build());
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        return new ResponseEntity<>(scoreService.exportCsv(instructorCode, sectionId), headers, HttpStatus.OK);
    }

    @PostMapping(path = "/{instructorCode}/sections/{sectionId}/scores/import",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a gradebook CSV", description = "Component columns may be names (as exported) or component ids.")
    public InstructorScoreOverviewResponse importScores(@PathVariable String instructorCode,
                                                        @PathVariable String sectionId,
                                                        @RequestParam("file") MultipartFile file) {
        currentUser.requireInstructor(instructorCode);
        return toScoreResponse(scoreService.importCsv(instructorCode, sectionId, file, currentUser.username()));
    }

    // ----- Final grades & stats -----

    @PostMapping("/{instructorCode}/sections/{sectionId}/grades/compute")
    @Operation(summary = "Compute final grades",
            description = "total = sum(score / outOf x weight) x 100; A>=90, A->=80, B>=70, B->=60, C>=50, C->=40, D>=30, else F. "
                    + "Component weights must add up to 100%.")
    public List<FinalGradeResponse> computeGrades(@PathVariable String instructorCode,
                                                  @PathVariable String sectionId) {
        currentUser.requireInstructor(instructorCode);
        return toFinalGradeResponse(scoreService.computeFinalGrades(instructorCode, sectionId));
    }

    @GetMapping("/{instructorCode}/sections/{sectionId}/grades")
    @Operation(summary = "Final grades as last computed")
    public List<FinalGradeResponse> finalGrades(@PathVariable String instructorCode,
                                                @PathVariable String sectionId) {
        currentUser.requireInstructor(instructorCode);
        return toFinalGradeResponse(scoreService.finalGrades(instructorCode, sectionId));
    }

    @GetMapping("/{instructorCode}/sections/{sectionId}/stats")
    @Operation(summary = "Class statistics", description = "Per-component average/min/max/median over entered scores, "
            + "plus final-grade average and distribution.")
    public SectionStatsView stats(@PathVariable String instructorCode, @PathVariable String sectionId) {
        currentUser.requireInstructor(instructorCode);
        return scoreService.sectionStats(instructorCode, sectionId);
    }

    private List<FinalGradeResponse> toFinalGradeResponse(List<FinalGradeDomain> grades) {
        return grades.stream()
                .map(g -> new FinalGradeResponse(g.getEnrollmentId(), g.getStudentId(), g.getRollNo(),
                        g.getStudentName(), g.getTotalScore(), g.getFinalGrade()))
                .toList();
    }

    private InstructorScoreOverviewResponse toScoreResponse(InstructorScoreOverviewDomain overview) {
        Map<UUID, AssessmentComponentDomain> componentsById = overview.getComponents().stream()
                .collect(Collectors.toMap(AssessmentComponentDomain::getComponentId, c -> c));
        List<InstructorScoreOverviewResponse.StudentScoreRow> students = overview.getStudents().stream()
                .map(student -> InstructorScoreOverviewResponse.StudentScoreRow.builder()
                        .enrollmentId(student.getEnrollmentId())
                        .studentId(student.getStudentId())
                        .studentName(student.getStudentName())
                        .rollNo(student.getRollNo())
                        .scores(student.getComponentScores().entrySet().stream()
                                .map(entry -> {
                                    AssessmentComponentDomain c = componentsById.get(entry.getKey());
                                    return InstructorScoreOverviewResponse.StudentComponentScore.builder()
                                            .componentId(entry.getKey())
                                            .componentName(c != null ? c.getName() : "")
                                            .score(entry.getValue())
                                            .scoreOutOf(c != null ? c.getScoreOutOf() : 0)
                                            .build();
                                })
                                .toList())
                        .build())
                .toList();
        return InstructorScoreOverviewResponse.builder()
                .components(overview.getComponents().stream()
                        .map(DOMAIN_TO_RESPONSE_MAPPER_INSTANCE::assessmentComponentDomainToResponse)
                        .toList())
                .students(students)
                .build();
    }
}
