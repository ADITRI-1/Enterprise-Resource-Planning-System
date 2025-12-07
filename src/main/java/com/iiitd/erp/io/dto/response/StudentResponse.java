package com.iiitd.erp.io.dto.response;

import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@With
public record StudentResponse(
        String name,
        Integer rollNo,
        String dateOfBirth,
        String programId,
        String yearOfRegistration
) {}