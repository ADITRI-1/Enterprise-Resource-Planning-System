package com.iiitd.erp.io.dto.response;

import lombok.Builder;

@Builder
public record ProgramResponse(
        String programId,
        String programDesc,
        String programType,
        Integer duration
) {}
