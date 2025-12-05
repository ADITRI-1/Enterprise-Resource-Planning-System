package com.iiitd.erp.io.dto.response;

import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

@Builder
@Jacksonized
@With
public record GradeRow(String courseCode,
                       String courseTitle,
                       String component,
                       BigDecimal score,
                       String finalGrade) {}
