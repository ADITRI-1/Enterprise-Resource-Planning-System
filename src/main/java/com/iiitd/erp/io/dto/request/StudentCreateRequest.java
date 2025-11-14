package com.iiitd.erp.io.dto.request;

import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@With
public record StudentCreateRequest(
    String name,
    Integer rollNo,
    String dateOfBirth,
    String programId,
    String yearOfRegistration
) {}