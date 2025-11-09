package com.iiitd.erp.persistent.persistence.repository;

import com.iiitd.erp.io.dto.response.GradeRow;
import com.iiitd.erp.persistent.entity.erp_db.Grade;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
public interface GradeRepository extends Repository<Grade, Long> {
  @Query("select new com.iiitd.erp.io.dto.response.GradeRow(c.code, c.title, g.component, g.score, g.finalGrade) " +
         "from com.iiitd.erp.domain.Grade g join com.iiitd.erp.domain.Enrollment e on g.enrollmentId=e.id " +
         "join com.iiitd.erp.domain.Section s on e.sectionId=s.id join com.iiitd.erp.domain.Course c on s.course.id=c.id " +
         "where e.studentId=:sid")
  List<GradeRow> findByStudent(@Param("sid") long studentId);
}
