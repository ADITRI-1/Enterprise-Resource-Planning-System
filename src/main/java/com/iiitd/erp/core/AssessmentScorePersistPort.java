package com.iiitd.erp.core;

import com.iiitd.erp.Domain.AssessmentScoreDomain;

import java.util.List;
import java.util.UUID;

public interface AssessmentScorePersistPort {
    AssessmentScoreDomain upsert(AssessmentScoreDomain domain);
    List<AssessmentScoreDomain> findBySection(String sectionId);
}
