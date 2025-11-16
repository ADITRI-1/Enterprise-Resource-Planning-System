package com.iiitd.erp.Domain;

import lombok.Builder;
import lombok.Value;

import java.util.Map;
import java.util.UUID;

@Value
@Builder
public class InstructorStudentScoreDomain {
    UUID enrollmentId;
    UUID studentId;
    String studentName;
    String rollNo;
    Map<UUID, Double> componentScores;
}
