
package com.iiitd.erp.persistent.entity.erp_db;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "sections", schema = "erp_db")
public class Section {
    @Id
    @Column(name = "section_id", columnDefinition = "uuid")
    private UUID sectionId;

    @Column(name = "course_code", nullable = false, length = 64)
    private String courseCode;            // FK to erp_db.course_details.id (string)

    @Column(name = "instructor_id", columnDefinition = "uuid")
    private UUID instructorId;            // nullable

    @Column(nullable = false, length = 20)
    private String semester;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "enrollment_count", nullable = false)
    private Integer enrollmentCount;

    @Column(name = "registration_deadline", nullable = false)
    private LocalDate registrationDeadline;

}