package com.iiitd.erp.io.controller.support;

import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.io.dto.response.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public final class ApiResponseSupport {
    private ApiResponseSupport() {}

    public static ResponseEntity<ApiErrorResponse> error(HttpStatus status, ErrorCode code) {
        return ResponseEntity.status(status).body(ApiErrorResponse.of(code));
    }

    public static ResponseEntity<ApiErrorResponse> error(HttpStatus status, ErrorCode code, String overrideMessage) {
        return ResponseEntity.status(status).body(ApiErrorResponse.of(code, overrideMessage));
    }

    public static ResponseEntity<ApiErrorResponse> from(ErpException ex) {
        return ResponseEntity.status(resolveStatus(ex.getErrorCode()))
                .body(ApiErrorResponse.of(ex.getErrorCode(), ex.getMessage(), ex.getContext()));
    }

    public static HttpStatus resolveStatus(ErrorCode code) {
        return switch (code) {
            case DUPLICATE_STUDENT, SECTION_DUPLICATE, SCHEDULE_ROOM_CONFLICT, SCHEDULE_INSTRUCTOR_CONFLICT,
                 SECTION_FULL, ALREADY_ENROLLED -> HttpStatus.CONFLICT;
            case ADMIN_FORBIDDEN, INSTRUCTOR_FORBIDDEN, STUDENT_FORBIDDEN,
                 SECTION_NOT_OWNED_BY_INSTRUCTOR -> HttpStatus.FORBIDDEN;
            case AUTH_NOT_AUTHENTICATED, AUTHENTICATION_FAILED -> HttpStatus.UNAUTHORIZED;
            case STUDENT_REGISTRATION_BLOCKED,
                 MAINTENANCE_STATUS_FETCH_FAILED,
                 MAINTENANCE_UPDATE_FAILED,
                 MAINTENANCE_WRITE_BLOCKED -> HttpStatus.SERVICE_UNAVAILABLE;
            case STUDENT_REGISTRATION_FAILED, STUDENT_DROP_FAILED, SECTION_ID_IMMUTABLE,
                 SECTION_INVALID_SEMESTER, SCHEDULE_OUTSIDE_WORKING_HOURS, SCHEDULE_ROOM_CAPACITY_MISMATCH,
                 SCHEDULE_SLOT_INVALID, ASSESSMENT_SCORE_INVALID, STUDENT_NOT_IN_SECTION,
                 REGISTRATION_DEADLINE_PASSED, DROP_DEADLINE_PASSED, SECTION_INACTIVE,
                 ASSESSMENT_WEIGHT_EXCEEDED, ASSESSMENT_WEIGHTS_INCOMPLETE, ASSESSMENT_SCORE_IMPORT_FAILED,
                 REQUEST_VALIDATION_FAILED -> HttpStatus.BAD_REQUEST;
            case PROGRAM_DUPLICATE, DEPARTMENT_DUPLICATE, COURSE_DUPLICATE, ROOM_DUPLICATE,
                 INSTRUCTOR_DUPLICATE -> HttpStatus.CONFLICT;
            default -> code.name().endsWith("_NOT_FOUND") ? HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
