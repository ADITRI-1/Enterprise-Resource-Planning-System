package com.iiitd.erp.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class ErpException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Map<String, Object> context;

    private ErpException(ErrorCode errorCode, String message, Throwable cause, Map<String, Object> context) {
        super(message == null ? errorCode.getDefaultMessage() : message, cause);
        this.errorCode = errorCode;
        this.context = context == null ? Collections.emptyMap() : Map.copyOf(context);
    }

    public static ErpException of(ErrorCode code, Map<String, Object> ctx) {
        return new ErpException(code, null, null, ctx);
    }

    public static ErpException wrap(ErrorCode code, Throwable cause, Map<String, Object> ctx) {
        return new ErpException(code, null, cause, ctx);
    }
}

