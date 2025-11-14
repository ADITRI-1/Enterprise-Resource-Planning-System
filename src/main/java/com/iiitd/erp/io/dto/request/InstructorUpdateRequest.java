package com.iiitd.erp.io.dto.request;

import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@With
public record InstructorUpdateRequest(
        String name,
        String dateOfBirth,
        String deptId,
        String email,
        String phone
) {}
