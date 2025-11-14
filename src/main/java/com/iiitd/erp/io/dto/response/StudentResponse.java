package com.iiitd.erp.io.dto.response;

import lombok.Builder;

@Builder
public record StudentResponse(
        String name,
        Integer rollNo,
        String dateOfBirth,
        String programId,
        String yearOfRegistration
) {}