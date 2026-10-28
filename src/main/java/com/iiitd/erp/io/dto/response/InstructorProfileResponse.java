package com.iiitd.erp.io.dto.response;

import java.util.UUID;

public record InstructorProfileResponse(UUID userId,
                                        String username,
                                        String instructorCode,
                                        String name,
                                        String departmentId,
                                        String email,
                                        String phone) {}
