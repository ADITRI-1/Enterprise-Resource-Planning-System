package com.iiitd.erp.io.dto.response;

import lombok.Builder;

@Builder
public record InstructorResponse(
        String name,
        String instructorCode,
        String dateOfBirth,
        String department,
        String email,
        String phone
) {}
