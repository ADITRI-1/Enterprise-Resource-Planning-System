package com.iiitd.erp.persistent.persistence.repository;

import com.iiitd.erp.persistent.entity.erp_db.Section;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<Section, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Section s where s.sectionId = :sectionId")
    Optional<Section> findByIdForUpdate(@Param("sectionId") String sectionId);

    List<Section> findByCourseId(String courseId);

    long countByIsActiveTrue();

    @Query("select count(s) from Section s where s.isActive = true and s.enrollmentCount >= s.capacity")
    long countFullActive();

    List<Section> findByCourseIdAndSemesterIgnoreCaseAndYear(String courseId, String semester, Integer year);

    List<Section> findByInstructorCodeIgnoreCase(String instructorCode);

    List<Section> findByInstructorCodeIgnoreCaseAndSemesterIgnoreCaseAndYear(String instructorCode, String semester, Integer year);

    @Query("""
       select s from Section s
       where (:semester is null or s.semester = :semester)
         and (:year is null or s.year = :year)
       order by s.sectionId
       """)
    List<Section> findAllFiltered(@Param("semester") String semester, @Param("year") Integer year);

}
