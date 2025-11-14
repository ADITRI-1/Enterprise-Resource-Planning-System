package com.iiitd.erp.io.dto.response;

import com.iiitd.erp.exception.ErrorCode;

public record ApiErrorResponse(String errorCode, String message) {

    public static ApiErrorResponse of(ErrorCode code) {
        return new ApiErrorResponse(code.getCode(), code.getDefaultMessage());
    }

    public static ApiErrorResponse of(ErrorCode code, String overrideMessage) {
        String message = (overrideMessage == null || overrideMessage.isBlank())
                ? code.getDefaultMessage()
                : overrideMessage;
        return new ApiErrorResponse(code.getCode(), message);
    }
}
