package com.iiitd.erp.io.dto.response;

import java.time.OffsetDateTime;

public record AuditEntryResponse(long id, OffsetDateTime at, String actor, String role, String method, String path,
                                 int status, String ip) {}
