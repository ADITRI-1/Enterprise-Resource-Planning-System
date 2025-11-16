package com.iiitd.erp.core.service;

import com.iiitd.erp.Domain.AssessmentComponentDomain;

import java.util.List;
import java.util.UUID;

public interface AssessmentService {
    List<AssessmentComponentDomain> list(String instructorCode, String sectionId);
    AssessmentComponentDomain create(String instructorCode, AssessmentComponentDomain request);
    AssessmentComponentDomain update(String instructorCode, UUID componentId, AssessmentComponentDomain request);
    AssessmentComponentDomain delete(String instructorCode, UUID componentId);
    double weightedScore(int rawScore, AssessmentComponentDomain component);
}
