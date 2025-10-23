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
    private UUID instructorId;  // FK to auth_db.user_auth.user_id

    @Column(nullable = false, length = 80)
    private String department;

    @Column(nullable = false, length = 100)
    private String email;

}
