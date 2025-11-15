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
                .body(ApiErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    public static HttpStatus resolveStatus(ErrorCode code) {
        return switch (code) {
            case DUPLICATE_STUDENT,SECTION_DUPLICATE -> HttpStatus.CONFLICT;
            case ADMIN_FORBIDDEN -> HttpStatus.FORBIDDEN;
            case AUTH_NOT_AUTHENTICATED, AUTHENTICATION_FAILED -> HttpStatus.UNAUTHORIZED;
            case STUDENT_REGISTRATION_BLOCKED,
                 MAINTENANCE_STATUS_FETCH_FAILED,
                 MAINTENANCE_UPDATE_FAILED,
                 MAINTENANCE_WRITE_BLOCKED -> HttpStatus.SERVICE_UNAVAILABLE;
            case STUDENT_REGISTRATION_FAILED, STUDENT_DROP_FAILED, SECTION_ID_IMMUTABLE,
                 SECTION_INVALID_SEMESTER -> HttpStatus.BAD_REQUEST;
            case SECTION_NOT_FOUND -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}