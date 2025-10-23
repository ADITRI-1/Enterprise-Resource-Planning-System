package com.iiitd.erp.persistent.entity.erp_db;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "assessment_components", schema = "erp_db",
        uniqueConstraints = @UniqueConstraint(columnNames = {"section_id","name"}))
public class AssessmentComponent {
    @Id
    @Column(name = "component_id", columnDefinition = "uuid")
    private UUID componentId;

    @Column(name = "section_id", columnDefinition = "uuid", nullable = false)
    private UUID sectionId;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(precision = 6, scale = 4, nullable = false)
    private BigDecimal weight;  // 0..1

    @Column(name = "max_score", nullable = false)
    private Integer maxScore;

    @Column(name = "due_date")
    private LocalDate dueDate;

    // getters/setters
}
