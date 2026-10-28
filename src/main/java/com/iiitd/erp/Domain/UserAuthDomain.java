package com.iiitd.erp.Domain;

import lombok.Builder;
import lombok.Data;
import lombok.With;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@With
public class UserAuthDomain {
    private UUID userId;
    private String username;
    private String role;
    private String passwordHash;
    private String status;
    private Boolean active;
    private String sessionId;
    private OffsetDateTime lastLogin;
    private Boolean firstLogin;
    private boolean mustChangePassword;
    private int failedAttempts;
    private OffsetDateTime lockedUntil;
}
