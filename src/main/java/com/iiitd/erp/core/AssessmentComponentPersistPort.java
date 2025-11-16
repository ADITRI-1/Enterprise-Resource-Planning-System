package com.iiitd.erp.core;

import com.iiitd.erp.Domain.AssessmentComponentDomain;

import java.util.List;
import java.util.UUID;

public interface AssessmentComponentPersistPort {
    AssessmentComponentDomain save(AssessmentComponentDomain domain);
    AssessmentComponentDomain update(AssessmentComponentDomain domain);
    AssessmentComponentDomain findById(UUID componentId);
    void delete(UUID componentId);
    List<AssessmentComponentDomain> listBySection(String sectionId);
    double weightSum(String sectionId, UUID excludeComponentId);
}
