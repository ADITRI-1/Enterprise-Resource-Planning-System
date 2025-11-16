package com.iiitd.erp.io.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
public record AssessmentComponentResponse(
        UUID componentId,
        String sectionId,
        String name,
        BigDecimal weight,
        int maxScore,
        int scoreOutOf,
        LocalDate dueDate) {}
