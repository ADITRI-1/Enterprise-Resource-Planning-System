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
@Table(name = "instructor", schema = "erp_db")
public class Instructor {
    @Id
    @Column(name = "instructor_id", columnDefinition = "uuid")
    private UUID instructorId;

    @Column(name = "instructor_code", nullable = false, unique = true, length = 30)
    private String instructorCode;

    @Column(name = "instructor_name", nullable = false, length = 80)
    private String name;

    @Column(name = "date_of_birth", nullable = false, length = 10)
    private String dateOfBirth;

    @Column(name = "dept_id", nullable = false, length = 80)
    private String departmentId;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;
}
