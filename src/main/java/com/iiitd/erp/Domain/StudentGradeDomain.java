package com.iiitd.erp.Domain;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class StudentGradeDomain {
    String courseCode;
    String courseTitle;
    String component;
    BigDecimal score;
    String finalGrade;
}
