package com.iiitd.erp.io.dto.response;

import java.util.UUID;

public record StudentProfileResponse(UUID userId,
                                     String username,
                                     String name,
                                     String rollNo,
                                     String programId,
                                     String year) {}
