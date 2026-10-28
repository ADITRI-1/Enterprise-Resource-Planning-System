package com.iiitd.erp.io.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iiitd.erp.exception.ErrorCode;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorResponse(String errorCode, String message, Map<String, Object> details) {

    public static ApiErrorResponse of(ErrorCode code) {
        return new ApiErrorResponse(code.getCode(), code.getDefaultMessage(), Map.of());
    }

    public static ApiErrorResponse of(ErrorCode code, String overrideMessage) {
        return of(code, overrideMessage, Map.of());
    }

    public static ApiErrorResponse of(ErrorCode code, String overrideMessage, Map<String, Object> details) {
        String message = (overrideMessage == null || overrideMessage.isBlank())
                ? code.getDefaultMessage()
                : overrideMessage;
        return new ApiErrorResponse(code.getCode(), message, details == null ? Map.of() : details);
    }
}
