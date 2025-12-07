package com.iiitd.erp.persistent.entity.erp_db;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Table(name = "program", schema = "erp_db")
public class Program {
    @Id
    @Column(name = "program_id")
    private String programId;

    @Column(name = "program_desc", nullable = false, length = 255)
    private String programDesc;

    @Column(name = "program_type", nullable = false, length = 100)
    private String programType;

    @Column(name = "duration")
    private Integer duration;

}