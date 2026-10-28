package com.iiitd.erp.Domain;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@Builder
public class FinalGradeDomain {
    UUID enrollmentId;
    UUID studentId;
    String rollNo;
    String studentName;
    BigDecimal totalScore;
    String finalGrade;
}
