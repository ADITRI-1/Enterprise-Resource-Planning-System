package com.iiitd.erp.persistent.entity.erp_db;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "student", schema = "erp_db")
public class Student {
    @Id
    @Column(name = "student_id", columnDefinition = "uuid")
    private UUID studentId;  // FK to auth_db.user_auth.user_id

    @Column(name = "student_name", nullable = false, length = 50)
    private String studentName;

    @Column(name = "roll_no", nullable = false, unique = true, length = 50)
    private String rollNo;

    @Column(name = "program_id", nullable = false, length = 120)
    private String program_id;

    @Column(name = "year", nullable = false)
    private Integer year;
}
