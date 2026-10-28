package com.iiitd.erp.io.dto.response;


import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@With
public record LoginResponse (
    String message,
    String username,
    String role,
    String sessionId,
    boolean mustChangePassword
){}