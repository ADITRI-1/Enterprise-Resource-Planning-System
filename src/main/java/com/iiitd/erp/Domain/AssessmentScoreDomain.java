package com.iiitd.erp.Domain;

import lombok.Builder;
import lombok.Data;
import lombok.With;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@With
public class AssessmentScoreDomain {
    private UUID scoreId;
    private UUID enrollmentId;
    private UUID componentId;
    private BigDecimal score;
    private OffsetDateTime updatedAt;
    private String updatedBy;
}
