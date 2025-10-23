package com.iiitd.erp.persistent.entity.erp_db;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "course_details")
public class Course {

    @Id
    @Column(name="id")
    String courseId;

    @Column(name="title")
    String title;

    @Column(name="credits")
    Integer credits;

    @Column(name="change_log")
    @JdbcTypeCode(SqlTypes.JSON)
    List<Changelog> changeLog;

}
