package com.iiitd.erp.io.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record SectionCreateRequest(
        @NotBlank String instructorCode,
        @NotBlank String semester,
        @Min(2000) @Max(2100) int year,
        @Positive int capacity,
        @NotNull LocalDate registrationDeadline,
        Boolean active
) {}
