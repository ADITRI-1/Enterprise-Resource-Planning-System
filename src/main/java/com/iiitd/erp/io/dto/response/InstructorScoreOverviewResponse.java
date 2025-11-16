package com.iiitd.erp.io.dto.response;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record InstructorScoreOverviewResponse(
        List<AssessmentComponentResponse> components,
        List<StudentScoreRow> students) {

    @Builder
    public record StudentScoreRow(
            UUID enrollmentId,
            UUID studentId,
            String studentName,
            String rollNo,
            List<StudentComponentScore> scores) {}

    @Builder
    public record StudentComponentScore(
            UUID componentId,
            String componentName,
            double score,
            int scoreOutOf) {}
}
