package com.iiitd.erp.persistent.persistence.repository;

import com.iiitd.erp.io.dto.response.SectionRow;
import com.iiitd.erp.persistent.entity.erp_db.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, String> {

    @Query("""
           select new com.iiitd.erp.io.dto.response.SectionRow(
               s.sectionId,
               s.courseId,
               s.instructorCode,
               null,
               coalesce(s.capacity, 0),
               cast(coalesce(sum(case when lower(e.status) = 'enrolled' then 1 else 0 end), 0) as integer),
               cast(coalesce(s.capacity, 0) - coalesce(sum(case when lower(e.status) = 'enrolled' then 1 else 0 end), 0) as integer),
               s.semester,
               s.year
           )
           from Section s
           left join Enrollment e on e.sectionId = s.sectionId
           where s.courseId = :courseId
             and upper(s.semester) = upper(:sem)
             and s.year = :yr
             and (s.isActive is null or s.isActive = true)
           group by s.sectionId, s.courseId, s.instructorCode, s.capacity, s.semester, s.year
           order by s.sectionId
           """)
    List<SectionRow> listByCourse(@Param("courseId") String courseId,
                                  @Param("sem") String sem,
                                  @Param("yr") int year);

    List<Section> findByCourseId(String courseId);

    List<Section> findByCourseIdAndSemesterIgnoreCaseAndYear(String courseId, String semester, Integer year);

    List<Section> findByInstructorCodeIgnoreCase(String instructorCode);

    List<Section> findByInstructorCodeIgnoreCaseAndSemesterIgnoreCaseAndYear(String instructorCode, String semester, Integer year);
}
