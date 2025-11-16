package com.iiitd.erp.core.service.impl;

import com.iiitd.erp.Domain.AssessmentComponentDomain;
import com.iiitd.erp.Domain.SectionDomain;
import com.iiitd.erp.core.AssessmentComponentPersistPort;
import com.iiitd.erp.core.SectionPersistPort;
import com.iiitd.erp.core.service.AssessmentService;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class AssessmentServiceImpl implements AssessmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssessmentServiceImpl.class);

    private final SectionPersistPort sections;
    private final AssessmentComponentPersistPort assessments;

    public AssessmentServiceImpl(SectionPersistPort sections,
                                 AssessmentComponentPersistPort assessments) {
        this.sections = sections;
        this.assessments = assessments;
    }

    @Override
    public List<AssessmentComponentDomain> list(String instructorCode, String sectionId) {
        SectionDomain section = ensureOwnership(instructorCode, sectionId);
        return assessments.listBySection(section.getSectionId());
    }

    @Override
    public AssessmentComponentDomain create(String instructorCode, AssessmentComponentDomain request) {
        SectionDomain section = ensureOwnership(instructorCode, request.getSectionId());
        ensureWeightBudget(section.getSectionId(), request.getWeight(), null);
        LOGGER.info("Instructor {} adding assessment {} to section {}", instructorCode, request.getName(), section.getSectionId());
        return assessments.save(request.withSectionId(section.getSectionId()));
    }

    @Override
    public AssessmentComponentDomain update(String instructorCode, UUID componentId, AssessmentComponentDomain request) {
        AssessmentComponentDomain existing = fetch(componentId);
        SectionDomain section = ensureOwnership(instructorCode, existing.getSectionId());
        ensureWeightBudget(section.getSectionId(), request.getWeight(), componentId);
        LOGGER.info("Instructor {} updating assessment {} on section {}", instructorCode, componentId, section.getSectionId());
        return assessments.update(request.withComponentId(componentId).withSectionId(section.getSectionId()));
    }

    @Override
    public AssessmentComponentDomain delete(String instructorCode, UUID componentId) {
        AssessmentComponentDomain existing = fetch(componentId);
        ensureOwnership(instructorCode, existing.getSectionId());
        assessments.delete(componentId);
        return existing;
    }

    @Override
    public double weightedScore(int rawScore, AssessmentComponentDomain component) {
        double normalized = (double) rawScore / component.getScoreOutOf();
        return normalized * component.getWeight().doubleValue();
    }

    private SectionDomain ensureOwnership(String instructorCode, String sectionId) {
        SectionDomain section = sections.findBySectionId(sectionId);
        if (section == null) {
            throw ErpException.of(ErrorCode.SECTION_NOT_FOUND, Map.of("sectionId", sectionId));
        }
        if (!sectionId.equals(section.getSectionId())
                || !instructorCode.equalsIgnoreCase(section.getInstructorCode())) {
            LOGGER.warn("Instructor {} attempted to access section {}", instructorCode, sectionId);
            throw ErpException.of(ErrorCode.SECTION_NOT_OWNED_BY_INSTRUCTOR,
                    Map.of("instructorCode", instructorCode, "sectionId", sectionId));
        }
        return section;
    }

    private void ensureWeightBudget(String sectionId, BigDecimal incomingWeight, UUID exclude) {
        double existing = assessments.weightSum(sectionId, exclude);
        double total = existing + incomingWeight.doubleValue();
        if (total > 1.0 + 1e-6) {
            LOGGER.warn("Weights exceed budget for section {}: current {}, incoming {}", sectionId, existing, incomingWeight);
            throw ErpException.of(ErrorCode.ASSESSMENT_WEIGHT_EXCEEDED,
                    Map.of("sectionId", sectionId, "currentWeight", existing, "incomingWeight", incomingWeight));
        }
    }

    private AssessmentComponentDomain fetch(UUID componentId) {
        AssessmentComponentDomain domain = assessments.findById(componentId);
        if (domain == null) {
            throw ErpException.of(ErrorCode.ASSESSMENT_NOT_FOUND, Map.of("componentId", componentId));
        }
        return domain;
    }
}
