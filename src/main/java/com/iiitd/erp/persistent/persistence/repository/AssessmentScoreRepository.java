package com.iiitd.erp.persistent.persistence.repository;

import com.iiitd.erp.persistent.entity.erp_db.AssessmentScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssessmentScoreRepository extends JpaRepository<AssessmentScore, UUID> {
    Optional<AssessmentScore> findByEnrollmentIdAndComponentId(UUID enrollmentId, UUID componentId);

    @Query("""
           select sc from AssessmentScore sc
           join Enrollment e on sc.enrollmentId = e.enrollmentId
           where e.sectionId = :sectionId
           """)
    List<AssessmentScore> findBySectionId(@Param("sectionId") String sectionId);
}
