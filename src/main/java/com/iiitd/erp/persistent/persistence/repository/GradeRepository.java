package com.iiitd.erp.persistent.persistence.repository;

import com.iiitd.erp.Domain.StudentGradeDomain;
import com.iiitd.erp.io.dto.response.GradeRow;
import com.iiitd.erp.persistent.entity.erp_db.Grade;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface GradeRepository extends Repository<Grade, UUID> {

    @Query("""
           select new com.iiitd.erp.io.dto.response.GradeRow(
               c.courseId,
               c.title,
               g.component,
               coalesce(g.score, 0),
               g.finalGrade
           )
           from Grade g
           join Enrollment e on g.enrollmentId = e.enrollmentId
           join Section s on e.sectionId = s.sectionId
           join Course c on s.courseId = c.courseId
           where e.studentId = :studentId
             and lower(e.status) in ('enrolled','completed')
           """)
    List<StudentGradeDomain> findByStudent(@Param("studentId") UUID studentId);
}
