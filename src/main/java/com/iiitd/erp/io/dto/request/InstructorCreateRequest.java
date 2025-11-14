package com.iiitd.erp.io.dto.request;

import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@With
public record InstructorCreateRequest(
        String name,
        String instructorCode,
        String dateOfBirth,
        String deptId,
        String email,
        String phone
) {}
