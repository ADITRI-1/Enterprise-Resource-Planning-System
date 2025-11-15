package com.iiitd.erp.persistent.persistence.repository;

import com.iiitd.erp.io.dto.response.SectionRow;
import com.iiitd.erp.persistent.entity.erp_db.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
public interface SectionRepository extends JpaRepository<Section, String> {
  @Query("select new com.iiitd.erp.io.dto.response.SectionRow(s.id, s.course.id, cast(s.instructorId as string), s.capacity, " +
         "(select count(e) from Enrollment e where e.sectionId = s.id and e.status='ENROLLED'), " +
         "s.dayOfWeek, s.startTime, s.endTime, s.room, s.semester, s.year) " +
         "from Section s where s.course.id = :courseId and s.semester = :sem and s.year = :yr")
  List<SectionRow> listByCourse(@Param("courseId") long courseId, @Param("sem") String sem, @Param("yr") int year);
    List<Section> findByCourseId(String courseId);
    List<Section> findByCourseIdAndSemesterIgnoreCaseAndYear(String courseId, String semester, Integer year);
}
