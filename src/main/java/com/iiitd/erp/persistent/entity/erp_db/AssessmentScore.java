package com.iiitd.erp.persistent.entity.erp_db;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "assessment_scores", schema = "erp_db",
        uniqueConstraints = @UniqueConstraint(columnNames = {"enrollment_id","component_id"}))
public class AssessmentScore {
    @Id
    @Column(name = "score_id", columnDefinition = "uuid")
    private UUID scoreId;

    @Column(name = "enrollment_id", columnDefinition = "uuid", nullable = false)
    private UUID enrollmentId;

    @Column(name = "component_id", columnDefinition = "uuid", nullable = false)
    private UUID componentId;

    @Column(nullable = false, precision = 7, scale = 3)
    private BigDecimal score;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by", nullable = false, length = 64)
    private String updatedBy;
}
