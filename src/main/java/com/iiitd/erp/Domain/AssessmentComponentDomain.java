package com.iiitd.erp.Domain;

import lombok.Builder;
import lombok.Data;
import lombok.With;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@With
public class AssessmentComponentDomain {
    private UUID componentId;
    private String sectionId;
    private String name;
    private BigDecimal weight;
    private Integer maxScore;
    private Integer scoreOutOf;
    private LocalDate dueDate;
}
