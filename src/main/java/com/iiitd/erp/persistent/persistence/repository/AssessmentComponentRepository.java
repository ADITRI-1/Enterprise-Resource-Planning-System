package com.iiitd.erp.persistent.persistence.repository;

import com.iiitd.erp.persistent.entity.erp_db.AssessmentComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssessmentComponentRepository extends JpaRepository<AssessmentComponent, UUID> {
    List<AssessmentComponent> findBySectionIdOrderByDueDateAscNameAsc(String sectionId);
    Optional<AssessmentComponent> findBySectionIdAndNameIgnoreCase(String sectionId, String name);

    @Query("select coalesce(sum(c.weight),0) from AssessmentComponent c " +
            "where c.sectionId = :sectionId and (:excludeId is null or c.componentId <> :excludeId)")
    double weightSum(String sectionId, UUID excludeId);
}
