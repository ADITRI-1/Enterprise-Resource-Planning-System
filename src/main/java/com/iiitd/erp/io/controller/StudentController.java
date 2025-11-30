package com.iiitd.erp.io.controller;

import com.iiitd.erp.Domain.RegistrationOutcomeDomain;
import com.iiitd.erp.Domain.StudentGradeDomain;
import com.iiitd.erp.Domain.StudentTimetableSlotDomain;
import com.iiitd.erp.core.service.AccessService;
import com.iiitd.erp.core.service.CatalogService;
import com.iiitd.erp.core.service.MaintenanceService;
import com.iiitd.erp.core.service.SectionService;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.io.dto.request.DropRequest;
import com.iiitd.erp.io.dto.request.RegisterRequest;
import com.iiitd.erp.io.dto.response.GradeRow;
import com.iiitd.erp.io.dto.response.TimetableSlot;
import com.iiitd.erp.mappers.DomainToResponseMapper;
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

    private final SectionService sections;
    private final AccessService access;
    private final MaintenanceService maintenance;
    private final CatalogService catalog;
    private final DomainToResponseMapper mapper = DomainToResponseMapper.DOMAIN_TO_RESPONSE_MAPPER_INSTANCE;

    public StudentController(SectionService sections,
                             AccessService access,
                             MaintenanceService maintenance,
                             CatalogService catalog){
        this.sections = sections;
        this.access = access;
        this.maintenance = maintenance;
        this.catalog = catalog;
    }

    // Catalog endpoints unchanged (use response DTOs)

    @PostMapping("/{uid}/register")
    public ResponseEntity<?> register(@PathVariable String uid, @RequestBody RegisterRequest request){
        try {
            access.ensureStudent(uid);
            if (maintenance.isReadOnly()) {
                LOGGER.warn("Registration blocked for {} due to maintenance mode", uid);
                return error(HttpStatus.SERVICE_UNAVAILABLE, ErrorCode.STUDENT_REGISTRATION_BLOCKED);
            }
            RegistrationOutcomeDomain outcome = sections.registerStudent(uid, request.sectionId());
            return ResponseEntity.ok(mapper.registrationOutcomeToResponse(outcome));
        } catch (ErpException ex) {
            return from(ex);
        } catch (Exception ex) {
            return error(HttpStatus.BAD_REQUEST,
                    ErrorCode.STUDENT_REGISTRATION_FAILED,
                    ErrorCode.STUDENT_REGISTRATION_FAILED.format(uid, request.sectionId()));
        }
    }

    @PostMapping("/{uid}/drop")
    public ResponseEntity<?> drop(@PathVariable String uid, @RequestBody DropRequest request){
        try {
            access.ensureStudent(uid);
            if (maintenance.isReadOnly()) {
                return error(HttpStatus.SERVICE_UNAVAILABLE, ErrorCode.STUDENT_REGISTRATION_BLOCKED);
            }
            boolean dropped = sections.dropStudent(uid, request.sectionId());
            return ResponseEntity.ok(Map.of("dropped", dropped));
        } catch (ErpException ex) {
            return from(ex);
        } catch (Exception ex) {
            return error(HttpStatus.BAD_REQUEST,
                    ErrorCode.STUDENT_DROP_FAILED,
                    ErrorCode.STUDENT_DROP_FAILED.format(uid, request.sectionId()));
        }
    }

    @GetMapping("/{uid}/timetable")
    public ResponseEntity<?> timetable(@PathVariable String uid,
                                       @RequestParam String semester,
                                       @RequestParam int year){
        try {
            access.ensureStudent(uid);
            List<StudentTimetableSlotDomain> slots = sections.timetableForStudent(uid, semester, year);
            List<TimetableSlot> response = slots.stream()
                    .map(mapper::studentTimetableSlotToResponse)
                    .toList();
            return ResponseEntity.ok(response);
        } catch (ErpException ex) {
            return from(ex);
        } catch (Exception ex) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.STUDENT_TIMETABLE_FAILED,
                    ErrorCode.STUDENT_TIMETABLE_FAILED.format(uid));
        }
    }

    @GetMapping("/{uid}/grades")
    public ResponseEntity<?> grades(@PathVariable String uid){
        try {
            access.ensureStudent(uid);
            List<StudentGradeDomain> grades = sections.gradesForStudent(uid);
            List<GradeRow> response = grades.stream()
                    .map(mapper::studentGradeDomainToResponse)
                    .toList();
            return ResponseEntity.ok(response);
        } catch (ErpException ex) {
            return from(ex);
        } catch (Exception ex) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.STUDENT_GRADES_FAILED,
                    ErrorCode.STUDENT_GRADES_FAILED.format(uid));
        }
    }

    @GetMapping(value = "/{uid}/transcript.csv", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> transcriptPath(@PathVariable String uid){
        try {
            access.ensureStudent(uid);
            Path path = sections.exportTranscript(uid);
            return ResponseEntity.ok(path.toString());
        } catch (ErpException ex) {
            return from(ex);
        } catch (Exception ex) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.STUDENT_TRANSCRIPT_FAILED,
                    ErrorCode.STUDENT_TRANSCRIPT_FAILED.format(uid));
        }
    }

    @GetMapping("/{uid}/transcript/download")
    public ResponseEntity<?> transcriptDownload(@PathVariable String uid){
        try {
            access.ensureStudent(uid);
            Path path = sections.exportTranscript(uid);
            FileSystemResource resource = new FileSystemResource(path);
            if (!resource.exists()) {
                return error(HttpStatus.NOT_FOUND,
                        ErrorCode.STUDENT_TRANSCRIPT_FAILED,
                        "Transcript not available yet");
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transcript-" + uid + ".csv")
                    .body(resource);
        } catch (ErpException ex) {
            return from(ex);
        } catch (Exception ex) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.STUDENT_TRANSCRIPT_FAILED,
                    ErrorCode.STUDENT_TRANSCRIPT_FAILED.format(uid));
        }
    }
}