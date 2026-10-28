package com.iiitd.erp.persistent.persistence.repository;

import com.iiitd.erp.persistent.entity.erp_db.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GradeRepository extends JpaRepository<Grade, UUID> {
    Optional<Grade> findByEnrollmentIdAndComponent(UUID enrollmentId, String component);
    List<Grade> findByEnrollmentIdInAndComponent(Collection<UUID> enrollmentIds, String component);
}
