package com.iiitd.erp.io.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record UpdateStudentScoresRequest(
        @NotEmpty List<@Valid StudentScoreEntry> entries) {

    public record StudentScoreEntry(
            UUID enrollmentId,
            @NotEmpty List<@Valid ComponentScore> components) {}

    public record ComponentScore(
            UUID componentId,
            double score) {}
}
