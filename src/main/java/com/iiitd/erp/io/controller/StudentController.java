package com.iiitd.erp.io.controller;

import com.iiitd.erp.core.service.AccessService;
import com.iiitd.erp.core.service.MaintenanceService;
import com.iiitd.erp.core.service.StudentRoleService;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.io.dto.request.DropRequest;
import com.iiitd.erp.io.dto.request.RegisterRequest;
import com.iiitd.erp.io.dto.response.GradeRow;
import com.iiitd.erp.io.dto.response.RegistrationResult;
import com.iiitd.erp.io.dto.response.TimetableSlot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.iiitd.erp.io.controller.support.ApiResponseSupport.error;
import static com.iiitd.erp.io.controller.support.ApiResponseSupport.from;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StudentController.class);

    private final StudentRoleService students;
    private final AccessService access;
    private final MaintenanceService maintenance;

    public StudentController(StudentRoleService students, AccessService access, MaintenanceService maintenance){
        this.students = students;
        this.access = access;
        this.maintenance = maintenance;
    }

    @PostMapping("/{uid}/register")
    public ResponseEntity<?> register(@PathVariable long uid, @RequestBody RegisterRequest request){
        try {
            access.ensureStudent(uid);
            if (maintenance.isReadOnly()) {
                LOGGER.warn("Registration blocked for {} due to maintenance mode", uid);
                return error(HttpStatus.SERVICE_UNAVAILABLE, ErrorCode.STUDENT_REGISTRATION_BLOCKED);
            }
            RegistrationResult result = students.register(uid, request.sectionId());
            LOGGER.info("Student {} registered for section {}", uid, request.sectionId());
            return ResponseEntity.ok(result);
        } catch (ErpException ex) {
            LOGGER.error("Registration failure for {} section {}: {}", uid, request.sectionId(), ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected registration failure for {} section {}", uid, request.sectionId(), ex);
            return error(HttpStatus.BAD_REQUEST,
                    ErrorCode.STUDENT_REGISTRATION_FAILED,
                    ErrorCode.STUDENT_REGISTRATION_FAILED.format(uid, request.sectionId()));
        }
    }

    @PostMapping("/{uid}/drop")
    public ResponseEntity<?> drop(@PathVariable long uid, @RequestBody DropRequest request){
        try {
            access.ensureStudent(uid);
            if (maintenance.isReadOnly()) {
                LOGGER.warn("Drop blocked for {} due to maintenance mode", uid);
                return error(HttpStatus.SERVICE_UNAVAILABLE, ErrorCode.STUDENT_REGISTRATION_BLOCKED);
            }
            boolean dropped = students.drop(uid, request.sectionId());
            LOGGER.info("Student {} drop result for section {}: {}", uid, request.sectionId(), dropped);
            return ResponseEntity.ok(Map.of("dropped", dropped));
        } catch (ErpException ex) {
            LOGGER.error("Drop failure for {} section {}: {}", uid, request.sectionId(), ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected drop failure for {} section {}", uid, request.sectionId(), ex);
            return error(HttpStatus.BAD_REQUEST,
                    ErrorCode.STUDENT_DROP_FAILED,
                    ErrorCode.STUDENT_DROP_FAILED.format(uid, request.sectionId()));
        }
    }

    @GetMapping("/{uid}/timetable")
    public ResponseEntity<?> timetable(@PathVariable long uid,
                                       @RequestParam String semester,
                                       @RequestParam int year){
        try {
            access.ensureStudent(uid);
            List<TimetableSlot> slots = students.myTimetable(uid, semester, year);
            return ResponseEntity.ok(slots);
        } catch (ErpException ex) {
            LOGGER.error("Timetable failure for {}: {}", uid, ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected timetable failure for {}", uid, ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.STUDENT_TIMETABLE_FAILED,
                    ErrorCode.STUDENT_TIMETABLE_FAILED.format(uid));
        }
    }

    @GetMapping("/{uid}/grades")
    public ResponseEntity<?> grades(@PathVariable long uid){
        try {
            access.ensureStudent(uid);
            List<GradeRow> grades = students.myGrades(uid);
            return ResponseEntity.ok(grades);
        } catch (ErpException ex) {
            LOGGER.error("Grades fetch failure for {}: {}", uid, ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected grades failure for {}", uid, ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.STUDENT_GRADES_FAILED,
                    ErrorCode.STUDENT_GRADES_FAILED.format(uid));
        }
    }

    @GetMapping(value = "/{uid}/transcript.csv", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> transcriptPath(@PathVariable long uid){
        try {
            access.ensureStudent(uid);
            Path path = students.exportTranscriptCsv(uid);
            LOGGER.info("Transcript generated at {} for {}", path, uid);
            return ResponseEntity.ok(path.toString());
        } catch (ErpException ex) {
            LOGGER.error("Transcript export failure for {}: {}", uid, ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected transcript export failure for {}", uid, ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.STUDENT_TRANSCRIPT_FAILED,
                    ErrorCode.STUDENT_TRANSCRIPT_FAILED.format(uid));
        }
    }

    @GetMapping("/{uid}/transcript/download")
    public ResponseEntity<?> transcriptDownload(@PathVariable long uid){
        try {
            access.ensureStudent(uid);
            Path path = students.exportTranscriptCsv(uid);
            FileSystemResource resource = new FileSystemResource(path);
            if (!resource.exists()) {
                LOGGER.warn("Transcript file {} not found for {}", path, uid);
                return error(HttpStatus.NOT_FOUND,
                        ErrorCode.STUDENT_TRANSCRIPT_FAILED,
                        "Transcript not available yet");
            }
            LOGGER.info("Transcript download prepared for {}", uid);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transcript-" + uid + ".csv")
                    .body(resource);
        } catch (ErpException ex) {
            LOGGER.error("Transcript download failure for {}: {}", uid, ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected transcript download failure for {}", uid, ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.STUDENT_TRANSCRIPT_FAILED,
                    ErrorCode.STUDENT_TRANSCRIPT_FAILED.format(uid));
        }
    }
}