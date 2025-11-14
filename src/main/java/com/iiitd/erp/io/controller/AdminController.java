package com.iiitd.erp.io.controller;

import com.iiitd.erp.Domain.CreateUserDomain;
import com.iiitd.erp.Domain.InstructorDomain;
import com.iiitd.erp.Domain.StudentUserDomain;
import com.iiitd.erp.core.service.AdminService;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.io.dto.request.InstructorCreateRequest;
import com.iiitd.erp.io.dto.request.InstructorUpdateRequest;
import com.iiitd.erp.io.dto.request.StudentCreateRequest;
import com.iiitd.erp.io.dto.request.StudentUpdateRequest;
import com.iiitd.erp.io.dto.response.CreateUserResponse;
import com.iiitd.erp.io.dto.response.InstructorResponse;
import com.iiitd.erp.io.dto.response.StudentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.iiitd.erp.io.controller.support.ApiResponseSupport.error;
import static com.iiitd.erp.io.controller.support.ApiResponseSupport.from;
import static com.iiitd.erp.mappers.DomainToResponseMapper.DOMAIN_TO_RESPONSE_MAPPER_INSTANCE;
import static com.iiitd.erp.mappers.RequestToDomainMapper.REQUEST_TO_DOMAIN_MAPPER_INSTANCE;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);

    private final AdminService adminService;

    public AdminController(AdminService adminService){
        this.adminService = adminService;
    }

    @PostMapping("/create/student")
    public ResponseEntity<?> createStudentUser(@RequestBody StudentCreateRequest studentCreateRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            LOGGER.warn("Unauthorized attempt to create student by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.ADMIN_FORBIDDEN);
        }

        Integer rollNo = studentCreateRequest.rollNo();
        try {
            CreateUserDomain created = adminService.createStudentUser(
                    REQUEST_TO_DOMAIN_MAPPER_INSTANCE.studentUserRequestToDomain(studentCreateRequest)
            );
            CreateUserResponse response = DOMAIN_TO_RESPONSE_MAPPER_INSTANCE.domainToCreateUserResponse(created);
            LOGGER.info("Admin {} created student user for roll {}", auth.getName(), rollNo);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ErpException ex) {
            LOGGER.error("Business error while creating student {}: {}", rollNo, ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while creating student {}", rollNo, ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.USER_CREATION_FAILED);
        }
    }

    @PutMapping("/student/{rollNo}")
    public ResponseEntity<?> updateStudentUser(@PathVariable Integer rollNo,
                                               @RequestBody StudentUpdateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            LOGGER.warn("Unauthorized attempt to update student by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.ADMIN_FORBIDDEN);
        }

        StudentUserDomain domain = REQUEST_TO_DOMAIN_MAPPER_INSTANCE.studentUpdateRequestToDomain(request)
                .withRollNo(rollNo);

        try {
            StudentUserDomain updated = adminService.updateStudentUser(domain);
            StudentResponse response = DOMAIN_TO_RESPONSE_MAPPER_INSTANCE.studentDomainToResponse(updated);
            return ResponseEntity.ok(response);
        } catch (ErpException ex) {
            LOGGER.error("Business error while updating student {}: {}", rollNo, ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while updating student {}", rollNo, ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.STUDENT_UPDATE_FAILED);
        }
    }

    @DeleteMapping("/student/{rollNo}")
    public ResponseEntity<?> deleteStudentUser(@PathVariable Integer rollNo) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            LOGGER.warn("Unauthorized attempt to delete student by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.ADMIN_FORBIDDEN);
        }

        try {
            boolean deleted = adminService.deleteStudentUser(rollNo);
            return ResponseEntity.ok(Map.of("deleted", deleted, "rollNo", rollNo));
        } catch (ErpException ex) {
            LOGGER.error("Business error while deleting student {}: {}", rollNo, ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while deleting student {}", rollNo, ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.STUDENT_DELETE_FAILED);
        }
    }

    @PostMapping("/create/instructor")
    public ResponseEntity<?> createInstructor(@RequestBody InstructorCreateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            LOGGER.warn("Unauthorized attempt to create instructor by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.ADMIN_FORBIDDEN);
        }

        InstructorDomain domain = REQUEST_TO_DOMAIN_MAPPER_INSTANCE.instructorCreateRequestToDomain(request);

        try {
            CreateUserDomain created = adminService.createInstructor(domain);
            CreateUserResponse response = DOMAIN_TO_RESPONSE_MAPPER_INSTANCE.domainToCreateUserResponse(created);
            LOGGER.info("Admin {} created instructor {}", auth.getName(), request.instructorCode());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ErpException ex) {
            LOGGER.error("Business error while creating instructor {}: {}", request.instructorCode(), ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while creating instructor {}", request.instructorCode(), ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INSTRUCTOR_CREATE_FAILED);
        }
    }

    @PutMapping("/instructor/{instructorCode}")
    public ResponseEntity<?> updateInstructor(@PathVariable String instructorCode,
                                              @RequestBody InstructorUpdateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            LOGGER.warn("Unauthorized attempt to update instructor by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.ADMIN_FORBIDDEN);
        }

        InstructorDomain domain = REQUEST_TO_DOMAIN_MAPPER_INSTANCE.instructorUpdateRequestToDomain(request)
                .withInstructorCode(instructorCode);

        try {
            InstructorDomain updated = adminService.updateInstructor(domain);
            InstructorResponse response = DOMAIN_TO_RESPONSE_MAPPER_INSTANCE.instructorDomainToResponse(updated);
            return ResponseEntity.ok(response);
        } catch (ErpException ex) {
            LOGGER.error("Business error while updating instructor {}: {}", instructorCode, ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while updating instructor {}", instructorCode, ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INSTRUCTOR_UPDATE_FAILED);
        }
    }

    @DeleteMapping("/instructor/{instructorCode}")
    public ResponseEntity<?> deleteInstructor(@PathVariable String instructorCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            LOGGER.warn("Unauthorized attempt to delete instructor by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.ADMIN_FORBIDDEN);
        }

        try {
            boolean deleted = adminService.deleteInstructor(instructorCode);
            return ResponseEntity.ok(Map.of("deleted", deleted, "instructorCode", instructorCode));
        } catch (ErpException ex) {
            LOGGER.error("Business error while deleting instructor {}: {}", instructorCode, ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while deleting instructor {}", instructorCode, ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INSTRUCTOR_DELETE_FAILED);
        }
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));
    }
}
