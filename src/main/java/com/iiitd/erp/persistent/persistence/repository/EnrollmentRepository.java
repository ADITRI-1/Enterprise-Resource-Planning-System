package com.iiitd.erp.persistent.persistence.repository;

import com.iiitd.erp.persistent.entity.erp_db.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    interface SectionEnrollmentProjection {
        UUID getEnrollmentId();
        UUID getStudentId();
        String getStudentName();
        String getRollNo();
    }

    @Query("""
           select e.enrollmentId as enrollmentId,
                  s.studentId   as studentId,
                  s.studentName as studentName,
                  s.rollNo      as rollNo
           from Enrollment e
           join Student s on e.studentId = s.studentId
           where e.sectionId = :sectionId and e.status = 'ENROLLED'
           order by s.rollNo
           """)
    List<SectionEnrollmentProjection> findActiveStudents(@Param("sectionId") String sectionId);

}

