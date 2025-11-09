package com.iiitd.erp.persistent.persistence.repository;

import com.iiitd.erp.io.dto.response.CourseRow;
import com.iiitd.erp.persistent.entity.erp_db.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
public interface CourseRepository extends JpaRepository<Course, Long> {
  @Query("select new com.iiitd.erp.io.dto.response.CourseRow(c.id, c.code, c.title, c.credits) " +
         "from CourseEntity c " +
         "where (:q is null or lower(c.title) like lower(concat('%',:q,'%')) or lower(c.code) like lower(concat('%',:q,'%')))")
  List<CourseRow> search(@Param("q") String q);
}

