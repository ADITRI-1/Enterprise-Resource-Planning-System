package com.iiitd.erp.persistent.entity.erp_db;


import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "department", schema = "erp_db")
public class Department {
    @Id
    @Column(name = "dept_id")
    private String deptId;

    @Column(name = "dept_desc", nullable = false, length = 255)
    private String deptDesc;
}