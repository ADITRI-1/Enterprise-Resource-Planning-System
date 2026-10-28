package com.iiitd.erp.io.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AccountResponse(UUID userId,
                              String username,
                              String role,
                              String status,
                              OffsetDateTime lastLogin,
                              boolean locked,
                              OffsetDateTime lockedUntil,
                              boolean mustChangePassword) {}
