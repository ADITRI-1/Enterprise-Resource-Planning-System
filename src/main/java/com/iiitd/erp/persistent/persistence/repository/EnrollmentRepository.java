package com.iiitd.erp.persistent.persistence.repository;

import com.iiitd.erp.io.dto.response.TimetableSlot;
import com.iiitd.erp.persistent.entity.erp_db.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
  boolean existsByStudentIdAndSectionIdAndStatus(Long studentId, Long sectionId, String status);
  @Modifying
  @Query("update Enrollment e set e.status='DROPPED' where e.studentId=:sid and e.sectionId=:secId and e.status='ENROLLED'")
  int softDrop(@Param("sid") long studentId, @Param("secId") long sectionId);
  @Query("select new com.iiitd.erp.io.dto.response.TimetableSlot(s.dayOfWeek, s.startTime, s.endTime, c.code, c.title, s.room) " +
         "from Enrollment e join Section s on e.sectionId = s.id join Course c on s.course.id=c.id " +
         "where e.studentId=:sid and e.status='ENROLLED' and s.semester=:sem and s.year=:yr")
  List<TimetableSlot> slots(@Param("sid") long studentId, @Param("sem") String sem, @Param("yr") int year);
  @Query("select count(e) from Enrollment e where e.sectionId=:secId and e.status='ENROLLED'")
  int countEnrolled(@Param("secId") long sectionId);
}
