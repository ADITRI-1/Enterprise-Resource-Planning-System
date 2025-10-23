package com.iiitd.erp.persistent.entity.erp_db;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "grades", schema = "erp_db")
public class Grade {
    @Id
    @Column(name = "grade_id", columnDefinition = "uuid")
    private UUID gradeId;

    @Column(name = "enrollment_id", columnDefinition = "uuid", nullable = false)
    private UUID enrollmentId;

    @Column(nullable = false, length = 50)
    private String component;

    @Column(precision = 7, scale = 3, nullable = false)
    private BigDecimal score;

    @Column(name = "final_grade", length = 5)
    private String finalGrade;

    @Column(name = "weighted_score", precision = 9, scale = 4)
    private BigDecimal weightedScore;

    // getters/setters
}
