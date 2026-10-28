package com.iiitd.erp.io.controller;

import com.iiitd.erp.Domain.RegistrationOutcomeDomain;
import com.iiitd.erp.Domain.StudentGradeDomain;
import com.iiitd.erp.Domain.StudentTimeTableSlotDomain;
import com.iiitd.erp.Domain.StudentUserDomain;
import com.iiitd.erp.core.service.MaintenanceService;
import com.iiitd.erp.core.service.SectionService;
import com.iiitd.erp.core.service.StudentService;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.io.controller.support.CurrentUser;
import com.iiitd.erp.io.dto.request.DropRequest;
import com.iiitd.erp.io.dto.request.RegisterRequest;
import com.iiitd.erp.io.dto.response.GradeRow;
import com.iiitd.erp.io.dto.response.StudentProfileResponse;
import com.iiitd.erp.io.dto.response.TimetableSlot;
import com.iiitd.erp.mappers.DomainToResponseMapper;
import com.iiitd.erp.persistent.entity.auth_db.UserAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.iiitd.erp.io.controller.support.ApiResponseSupport.error;
import static com.iiitd.erp.io.controller.support.ApiResponseSupport.from;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StudentController.class);

    private final SectionService sections;
    private final MaintenanceService maintenance;
    private final StudentService students;
    private final CurrentUser currentUser;
    private final DomainToResponseMapper mapper = DomainToResponseMapper.DOMAIN_TO_RESPONSE_MAPPER_INSTANCE;

    public StudentController(SectionService sections,
                             MaintenanceService maintenance,
                             StudentService students,
                             CurrentUser currentUser) {
        this.sections = sections;
        this.maintenance = maintenance;
        this.students = students;
        this.currentUser = currentUser;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Optional<UserAuth> account = currentUser.student();
        if (account.isEmpty()) {
            return error(HttpStatus.FORBIDDEN, ErrorCode.STUDENT_FORBIDDEN);
        }
        try {
            UserAuth user = account.get();
            StudentUserDomain student = students.findStudent(user.getUserId());
            return ResponseEntity.ok(new StudentProfileResponse(
                    user.getUserId(), user.getUsername(), student.getName(), student.getRollNo(),
                    student.getProgramId(), student.getYearOfRegistration()));
        } catch (ErpException ex) {
            return from(ex);
        }
    }

    @PostMapping("/{uid}/register")
    public ResponseEntity<?> register(@PathVariable String uid, @RequestBody RegisterRequest request) {
        if (!currentUser.isStudent(uid)) {
            LOGGER.warn("Unauthorized registration attempt by {}", currentUser.username());
            return error(HttpStatus.FORBIDDEN, ErrorCode.STUDENT_FORBIDDEN);
        }
        try {
            if (maintenance.isReadOnly()) {
                LOGGER.warn("Registration blocked for {} due to maintenance mode", uid);
                return error(HttpStatus.SERVICE_UNAVAILABLE, ErrorCode.STUDENT_REGISTRATION_BLOCKED);
            }
            RegistrationOutcomeDomain outcome = sections.registerStudent(uid, request.sectionId());
            return ResponseEntity.ok(mapper.registrationOutcomeToResponse(outcome));
        } catch (ErpException ex) {
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected registration failure for {} section {}", uid, request.sectionId(), ex);
            return error(HttpStatus.BAD_REQUEST, ErrorCode.STUDENT_REGISTRATION_FAILED);
        }
    }

    @PostMapping("/{uid}/drop")
    public ResponseEntity<?> drop(@PathVariable String uid, @RequestBody DropRequest request) {
        if (!currentUser.isStudent(uid)) {
            LOGGER.warn("Unauthorized drop attempt by {}", currentUser.username());
            return error(HttpStatus.FORBIDDEN, ErrorCode.STUDENT_FORBIDDEN);
        }
        try {
            if (maintenance.isReadOnly()) {
                return error(HttpStatus.SERVICE_UNAVAILABLE, ErrorCode.MAINTENANCE_WRITE_BLOCKED);
            }
            boolean dropped = sections.dropStudent(uid, request.sectionId());
            return ResponseEntity.ok(Map.of("dropped", dropped));
        } catch (ErpException ex) {
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected drop failure for {} section {}", uid, request.sectionId(), ex);
            return error(HttpStatus.BAD_REQUEST, ErrorCode.STUDENT_DROP_FAILED);
        }
    }

    @GetMapping("/{uid}/timetable")
    public ResponseEntity<?> timetable(@PathVariable String uid,
                                       @RequestParam String semester,
                                       @RequestParam int year) {
        if (!currentUser.isStudent(uid)) {
            LOGGER.warn("Unauthorized timetable request by {}", currentUser.username());
            return error(HttpStatus.FORBIDDEN, ErrorCode.STUDENT_FORBIDDEN);
        }
        try {
            List<StudentTimeTableSlotDomain> slots = sections.timetableForStudent(uid, semester, year);
            List<TimetableSlot> response = slots.stream()
                    .map(mapper::studentTimeTableSlotToResponse)
                    .toList();
            return ResponseEntity.ok(response);
        } catch (ErpException ex) {
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected timetable failure for {}", uid, ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.STUDENT_TIMETABLE_FAILED);
        }
    }

    @GetMapping("/{uid}/grades")
    public ResponseEntity<?> grades(@PathVariable String uid) {
        if (!currentUser.isStudent(uid)) {
            LOGGER.warn("Unauthorized grades request by {}", currentUser.username());
            return error(HttpStatus.FORBIDDEN, ErrorCode.STUDENT_FORBIDDEN);
        }
        try {
            List<StudentGradeDomain> grades = sections.gradesForStudent(uid);
            List<GradeRow> response = grades.stream()
                    .map(mapper::studentGradeDomainToResponse)
                    .toList();
            return ResponseEntity.ok(response);
        } catch (ErpException ex) {
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected grades failure for {}", uid, ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.STUDENT_GRADES_FAILED);
        }
    }

    @GetMapping({"/{uid}/transcript.csv", "/{uid}/transcript/download"})
    public ResponseEntity<?> transcriptDownload(@PathVariable String uid) {
        if (!currentUser.isStudent(uid)) {
            LOGGER.warn("Unauthorized transcript download by {}", currentUser.username());
            return error(HttpStatus.FORBIDDEN, ErrorCode.STUDENT_FORBIDDEN);
        }
        try {
            byte[] csv = sections.exportTranscript(uid);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transcript-" + uid + ".csv")
                    .body(csv);
        } catch (ErpException ex) {
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected transcript download failure for {}", uid, ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.STUDENT_TRANSCRIPT_FAILED);
        }
    }
}
