package com.iiitd.erp.io.controller;

import com.iiitd.erp.Domain.AcademicRecordView;
import com.iiitd.erp.Domain.RegistrationView;
import com.iiitd.erp.Domain.StudentUserDomain;
import com.iiitd.erp.core.service.AcademicRecordService;
import com.iiitd.erp.core.service.SectionService;
import com.iiitd.erp.core.service.StudentService;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.io.controller.support.CurrentUser;
import com.iiitd.erp.io.dto.request.SectionIdRequest;
import com.iiitd.erp.io.dto.response.GradeRow;
import com.iiitd.erp.io.dto.response.RegistrationResult;
import com.iiitd.erp.io.dto.response.StudentProfileResponse;
import com.iiitd.erp.io.dto.response.TimetableSlot;
import com.iiitd.erp.mappers.DomainToResponseMapper;
import com.iiitd.erp.persistent.entity.auth_db.UserAuth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Student self-service. {uid} is the student's user id from GET /api/student/me; a student can
 * only act on their own uid. Writes are blocked during maintenance by MaintenanceReadOnlyInterceptor.
 */
@RestController
@RequestMapping("/api/student")
@Tag(name = "Student")
public class StudentController {

    private final SectionService sections;
    private final StudentService students;
    private final CurrentUser currentUser;
    private final AcademicRecordService academicRecords;
    private final DomainToResponseMapper mapper = DomainToResponseMapper.DOMAIN_TO_RESPONSE_MAPPER_INSTANCE;

    public StudentController(SectionService sections, StudentService students, CurrentUser currentUser,
                             AcademicRecordService academicRecords) {
        this.academicRecords = academicRecords;
        this.sections = sections;
        this.students = students;
        this.currentUser = currentUser;
    }

    @GetMapping("/me")
    @Operation(summary = "Logged-in student's profile (includes the userId used in other student URLs)")
    public StudentProfileResponse me() {
        UserAuth user = currentUser.student()
                .orElseThrow(() -> ErpException.of(ErrorCode.STUDENT_FORBIDDEN, Map.of()));
        StudentUserDomain student = students.findStudent(user.getUserId());
        return new StudentProfileResponse(user.getUserId(), user.getUsername(), student.getName(),
                student.getRollNo(), student.getProgramId(), student.getYearOfRegistration());
    }

    @PostMapping("/{uid}/register")
    @Operation(summary = "Register for a section",
            description = "409 if the section is full or you're already registered; 400 after the deadline.")
    public RegistrationResult register(@PathVariable String uid, @RequestBody @Valid SectionIdRequest request) {
        currentUser.requireStudent(uid);
        return mapper.registrationOutcomeToResponse(sections.registerStudent(uid, request.sectionId()));
    }

    @PostMapping("/{uid}/drop")
    @Operation(summary = "Drop a section (before its deadline)")
    public Map<String, Object> drop(@PathVariable String uid, @RequestBody @Valid SectionIdRequest request) {
        currentUser.requireStudent(uid);
        return Map.of("dropped", sections.dropStudent(uid, request.sectionId()));
    }

    @GetMapping("/{uid}/registrations")
    @Operation(summary = "My registrations", description = "Includes dropped sections; canDrop tells the UI whether to show a Drop button.")
    public List<RegistrationView> registrations(@PathVariable String uid,
                                                @RequestParam(required = false) String semester,
                                                @RequestParam(required = false) Integer year) {
        currentUser.requireStudent(uid);
        return sections.registrationsForStudent(uid, semester, year);
    }

    @GetMapping("/{uid}/timetable")
    @Operation(summary = "Weekly timetable for a term")
    public List<TimetableSlot> timetable(@PathVariable String uid,
                                         @RequestParam String semester,
                                         @RequestParam int year) {
        currentUser.requireStudent(uid);
        return sections.timetableForStudent(uid, semester, year).stream()
                .map(mapper::studentTimeTableSlotToResponse)
                .toList();
    }

    @GetMapping("/{uid}/grades")
    @Operation(summary = "Component scores and final grades", description = "Rows with component = FINAL hold the computed total.")
    public List<GradeRow> grades(@PathVariable String uid) {
        currentUser.requireStudent(uid);
        return sections.gradesForStudent(uid).stream()
                .map(mapper::studentGradeDomainToResponse)
                .toList();
    }

    @GetMapping("/{uid}/academic-record")
    @Operation(summary = "Results by term with SGPA and CGPA (10-point scale)")
    public AcademicRecordView academicRecord(@PathVariable String uid) {
        currentUser.requireStudent(uid);
        return academicRecords.record(uid);
    }

    @GetMapping("/{uid}/transcript.pdf")
    @Operation(summary = "Download transcript as PDF")
    public ResponseEntity<byte[]> transcriptPdf(@PathVariable String uid) {
        currentUser.requireStudent(uid);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transcript-" + uid + ".pdf")
                .body(academicRecords.transcriptPdf(uid));
    }

    @GetMapping({"/{uid}/transcript.csv", "/{uid}/transcript/download"})
    @Operation(summary = "Download transcript as CSV")
    public ResponseEntity<byte[]> transcript(@PathVariable String uid) {
        currentUser.requireStudent(uid);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transcript-" + uid + ".csv")
                .body(sections.exportTranscript(uid));
    }
}
