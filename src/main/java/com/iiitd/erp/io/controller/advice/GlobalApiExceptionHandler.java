package com.iiitd.erp.io.controller.advice;

import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.io.controller.support.ApiResponseSupport;
import com.iiitd.erp.io.dto.response.ApiErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

/** Single place where exceptions become API error responses; controllers don't catch anything. */
@RestControllerAdvice
public class GlobalApiExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalApiExceptionHandler.class);

    @ExceptionHandler(ErpException.class)
    public ResponseEntity<ApiErrorResponse> handleErpException(ErpException ex) {
        HttpStatus status = ApiResponseSupport.resolveStatus(ex.getErrorCode());
        if (status.is5xxServerError()) {
            LOGGER.error("ErpException [{}] context={}", ex.getErrorCode().getCode(), ex.getContext(), ex);
        } else {
            LOGGER.info("Request rejected [{}] {} context={}", ex.getErrorCode().getCode(), ex.getMessage(), ex.getContext());
        }
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.of(ex.getErrorCode(), ex.getMessage(), ex.getContext()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElse(ErrorCode.REQUEST_VALIDATION_FAILED.getDefaultMessage());
        return badRequest(message, Map.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        return badRequest("Malformed request body", Map.of());
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MissingServletRequestPartException.class})
    public ResponseEntity<ApiErrorResponse> handleMissingParameter(Exception ex) {
        return badRequest(ex.getMessage(), Map.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return badRequest("Invalid value for '" + ex.getName() + "'", Map.of("value", String.valueOf(ex.getValue())));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraint(DataIntegrityViolationException ex) {
        LOGGER.warn("Constraint violation: {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(ErrorCode.REQUEST_VALIDATION_FAILED,
                        "This change conflicts with existing data (it may be referenced elsewhere)"));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethod(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiErrorResponse.of(ErrorCode.REQUEST_VALIDATION_FAILED, ex.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(ErrorCode.REQUEST_VALIDATION_FAILED, "No such endpoint"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex) {
        LOGGER.error("Unhandled exception in controller layer", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(ErrorCode.UNEXPECTED_SERVER_ERROR));
    }

    private static ResponseEntity<ApiErrorResponse> badRequest(String message, Map<String, Object> details) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(ErrorCode.REQUEST_VALIDATION_FAILED, message, details));
    }
}
