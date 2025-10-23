package com.iiitd.erp.Domain;

import lombok.Builder;
import lombok.Data;
import lombok.With;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;


@Data
@Builder
@With
public class AuthResultDomain {
    private UUID userId;
    private String username;
    private String role;         // e.g., STUDENT/INSTRUCTOR/ADMIN
//    private List<String> authorities; // ROLE_*
    private String sessionId;        // JSESSIONID (for debugging/UI)
    private OffsetDateTime lastLogin; // previous login time (if you store it)
    private Boolean firstLogin;
}