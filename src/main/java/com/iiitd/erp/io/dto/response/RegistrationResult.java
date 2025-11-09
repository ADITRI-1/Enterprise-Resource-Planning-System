package com.iiitd.erp.io.dto.response;

import com.iiitd.erp.io.dto.Status;
import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@With
public record RegistrationResult(Status status, String message) {
    public static RegistrationResult ok(String msg){ return new RegistrationResult(Status.OK, msg); }
    public static RegistrationResult blocked(String msg){ return new RegistrationResult(Status.BLOCKED, msg); }
    public static RegistrationResult error(String msg){ return new RegistrationResult(Status.ERROR, msg); }
}
