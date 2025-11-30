package com.iiitd.erp.Domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StudentGradeDomain {
    String courseCode;
    String courseTitle;
    String component;
    double score;
    String finalGrade;
}
