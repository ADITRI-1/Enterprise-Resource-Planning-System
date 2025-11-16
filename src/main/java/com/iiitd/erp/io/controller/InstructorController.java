package com.iiitd.erp.io.controller;

import com.iiitd.erp.Domain.AssessmentComponentDomain;
import com.iiitd.erp.core.service.AssessmentService;
import com.iiitd.erp.core.service.SectionService;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.io.dto.request.AssessmentComponentRequest;
import com.iiitd.erp.io.dto.response.AssessmentComponentResponse;
import com.iiitd.erp.io.dto.response.InstructorSectionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

import static com.iiitd.erp.io.controller.support.ApiResponseSupport.error;
import static com.iiitd.erp.io.controller.support.ApiResponseSupport.from;
import static com.iiitd.erp.mappers.DomainToResponseMapper.DOMAIN_TO_RESPONSE_MAPPER_INSTANCE;
import static com.iiitd.erp.mappers.RequestToDomainMapper.REQUEST_TO_DOMAIN_MAPPER_INSTANCE;

@RestController
@RequestMapping("/api/instructor")
public class InstructorController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstructorController.class);

    private final AssessmentService assessments;
    private final SectionService sectionService;

    public InstructorController(AssessmentService assessments, SectionService sectionService) {
        this.assessments = assessments;
        this.sectionService = sectionService;
    }

    @GetMapping("/{instructorCode}/sections/{sectionId}/assessments")
    public ResponseEntity<?> listAssessments(@PathVariable String instructorCode,
                                             @PathVariable String sectionId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isInstructor(auth, instructorCode)) {
            LOGGER.warn("Unauthorized assessment list attempt by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.INSTRUCTOR_FORBIDDEN);
        }
        try {
            List<AssessmentComponentResponse> response = assessments.list(instructorCode, sectionId).stream()
                    .map(DOMAIN_TO_RESPONSE_MAPPER_INSTANCE::assessmentComponentDomainToResponse)
                    .toList();
            return ResponseEntity.ok(response);
        } catch (ErpException ex) {
            LOGGER.error("Assessment list failed for {} section {}", instructorCode, sectionId, ex);
            return from(ex);
        }
    }

    @PostMapping("/{instructorCode}/sections/{sectionId}/assessments")
    public ResponseEntity<?> createAssessment(@PathVariable String instructorCode,
                                              @PathVariable String sectionId,
                                              @RequestBody @Valid AssessmentComponentRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isInstructor(auth, instructorCode)) {
            LOGGER.warn("Unauthorized assessment create attempt by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.INSTRUCTOR_FORBIDDEN);
        }
        try {
            AssessmentComponentDomain domain = REQUEST_TO_DOMAIN_MAPPER_INSTANCE
                    .assessmentComponentRequestToDomain(request)
                    .withSectionId(sectionId);
            AssessmentComponentResponse response = DOMAIN_TO_RESPONSE_MAPPER_INSTANCE
                    .assessmentComponentDomainToResponse(assessments.create(instructorCode, domain));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ErpException ex) {
            LOGGER.error("Assessment creation failed for instructor {} section {}", instructorCode, sectionId, ex);
            return from(ex);
        }
    }

    @PutMapping("/{instructorCode}/sections/{sectionId}/assessments/{componentId}")
    public ResponseEntity<?> updateAssessment(@PathVariable String instructorCode,
                                              @PathVariable String sectionId,
                                              @PathVariable UUID componentId,
                                              @RequestBody @Valid AssessmentComponentRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isInstructor(auth, instructorCode)) {
            LOGGER.warn("Unauthorized assessment update attempt by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.INSTRUCTOR_FORBIDDEN);
        }
        try {
            AssessmentComponentDomain domain = REQUEST_TO_DOMAIN_MAPPER_INSTANCE
                    .assessmentComponentRequestToDomain(request)
                    .withSectionId(sectionId);
            AssessmentComponentResponse response = DOMAIN_TO_RESPONSE_MAPPER_INSTANCE
                    .assessmentComponentDomainToResponse(assessments.update(instructorCode, componentId, domain));
            return ResponseEntity.ok(response);
        } catch (ErpException ex) {
            LOGGER.error("Assessment update failed for instructor {} section {} component {}",
                    instructorCode, sectionId, componentId, ex);
            return from(ex);
        }
    }

    @DeleteMapping("/{instructorCode}/sections/{sectionId}/assessments/{componentId}")
    public ResponseEntity<?> deleteAssessment(@PathVariable String instructorCode,
                                              @PathVariable String sectionId,
                                              @PathVariable UUID componentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isInstructor(auth, instructorCode)) {
            LOGGER.warn("Unauthorized assessment delete attempt by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.INSTRUCTOR_FORBIDDEN);
        }
        try {
            AssessmentComponentResponse response = DOMAIN_TO_RESPONSE_MAPPER_INSTANCE
                    .assessmentComponentDomainToResponse(assessments.delete(instructorCode, componentId));
            return ResponseEntity.ok(response);
        } catch (ErpException ex) {
            LOGGER.error("Assessment delete failed for instructor {} section {} component {}",
                    instructorCode, sectionId, componentId, ex);
            return from(ex);
        }
    }

    @GetMapping("/{instructorCode}/sections")
    public ResponseEntity<?> listSections(@PathVariable String instructorCode,
                                          @RequestParam(required = false) String semester,
                                          @RequestParam(required = false) Integer year) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isInstructor(auth, instructorCode)) {
            LOGGER.warn("Unauthorized section list attempt by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.INSTRUCTOR_FORBIDDEN);
        }
        try {
            List<InstructorSectionResponse> response = sectionService
                    .sectionsForInstructor(instructorCode, semester, year).stream()
                    .map(DOMAIN_TO_RESPONSE_MAPPER_INSTANCE::sectionDomainToInstructorSectionResponse)
                    .toList();
            return ResponseEntity.ok(response);
        } catch (ErpException ex) {
            LOGGER.error("Section list failed for instructor {}", instructorCode, ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error listing sections for instructor {}", instructorCode, ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.SECTION_LIST_FAILED);
        }
    }

    private boolean isInstructor(Authentication auth, String expectedCode) {
        return auth == null
                || !auth.isAuthenticated()
                || !expectedCode.equalsIgnoreCase(auth.getName())
                || auth.getAuthorities().stream()
                .noneMatch(granted -> "INSTRUCTOR".equalsIgnoreCase(granted.getAuthority()));
    }
}
