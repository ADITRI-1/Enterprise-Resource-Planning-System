package com.iiitd.erp.io.dto.request;

import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@With
public record ProgramCreateRequest(
        String programId,
        String programDesc,
        String programType,
        Integer duration
) {}

