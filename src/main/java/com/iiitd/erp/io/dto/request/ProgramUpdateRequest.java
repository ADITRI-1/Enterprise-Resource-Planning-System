package com.iiitd.erp.io.dto.request;

import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@With
public record ProgramUpdateRequest(
        String programDesc,
        String programType,
        Integer duration
) {}

