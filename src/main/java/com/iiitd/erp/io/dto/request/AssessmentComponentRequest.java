package com.iiitd.erp.io.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AssessmentComponentRequest(
        @NotBlank String name,
        @DecimalMin(value = "0.0", inclusive = false) @DecimalMax(value = "1.0") Double weight,
        @Positive int maxScore,
        LocalDate dueDate) {}
