package com.iiitd.erp.persistent.entity.erp_db;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity

@Table(name = "enrollments", schema = "erp_db",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id","section_id"}))
public class Enrollment {
    @Id
    @Column(name = "enrollment_id", columnDefinition = "uuid")
    private UUID enrollmentId;

    @Column(name = "student_id", columnDefinition = "uuid", nullable = false)
    private UUID studentId;

    @Column(name = "section_id", columnDefinition = "uuid", nullable = false)
    private UUID sectionId;

    @Column(name = "enrollment_date", nullable = false)
    private OffsetDateTime enrollmentDate;

    @Column(name = "status", nullable = false, length = 16)
    private String status; // enrolled/dropped/completed

}