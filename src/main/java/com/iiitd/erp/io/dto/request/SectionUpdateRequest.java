package com.iiitd.erp.io.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record SectionUpdateRequest(
        @NotBlank String instructorCode,
        @NotBlank String semester,
        @Min(2000) @Max(2100) int year,
        @Positive int capacity,
        @NotNull LocalDate registrationDeadline,
        @NotNull Boolean active
) {
}
