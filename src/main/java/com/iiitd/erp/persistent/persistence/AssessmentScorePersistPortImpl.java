package com.iiitd.erp.persistent.persistence;

import com.iiitd.erp.Domain.AssessmentScoreDomain;
import com.iiitd.erp.core.AssessmentScorePersistPort;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.persistent.entity.erp_db.AssessmentScore;
import com.iiitd.erp.persistent.persistence.repository.AssessmentScoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.iiitd.erp.mappers.DomainToEntityMapper.DOMAIN_TO_ENTITY_MAPPER_INSTANCE;
import static com.iiitd.erp.mappers.EntityToDomainMapper.ENTITY_TO_DOMAIN_MAPPER_INSTANCE;
@Repository
@Transactional
public class AssessmentScorePersistPortImpl implements AssessmentScorePersistPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssessmentScorePersistPortImpl.class);

    private final AssessmentScoreRepository repository;

    public AssessmentScorePersistPortImpl(AssessmentScoreRepository repository) {
        this.repository = repository;
    }

    @Override
    public AssessmentScoreDomain upsert(AssessmentScoreDomain domain) {
        LOGGER.debug("Upserting score for enrollment {} component {}", domain.getEnrollmentId(), domain.getComponentId());
        try {
            AssessmentScore entity = DOMAIN_TO_ENTITY_MAPPER_INSTANCE.assessmentScoreDomainToAssessmentScore(domain);
            AssessmentScore saved = repository.save(entity);
            LOGGER.info("Saved score {} for enrollment {} component {}", saved.getScoreId(), saved.getEnrollmentId(), saved.getComponentId());
            return ENTITY_TO_DOMAIN_MAPPER_INSTANCE.assessmentScoreToDomain(saved);
        } catch (Exception ex) {
            LOGGER.error("Failed to upsert score for enrollment {} component {}", domain.getEnrollmentId(), domain.getComponentId(), ex);
            throw ErpException.wrap(ErrorCode.ASSESSMENT_SCORE_INVALID, ex,
                    Map.of("enrollmentId", domain.getEnrollmentId(), "componentId", domain.getComponentId()));
        }
    }

    @Override
    public List<AssessmentScoreDomain> findBySection(String sectionId) {
        LOGGER.debug("Fetching scores for section {}", sectionId);
        try {
            return repository.findBySectionId(sectionId).stream()
                    .map(ENTITY_TO_DOMAIN_MAPPER_INSTANCE::assessmentScoreToDomain)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            LOGGER.error("Failed to fetch scores for section {}", sectionId, ex);
            throw ErpException.wrap(ErrorCode.ASSESSMENT_SCORE_INVALID, ex, Map.of("sectionId", sectionId));
        }
    }
}
