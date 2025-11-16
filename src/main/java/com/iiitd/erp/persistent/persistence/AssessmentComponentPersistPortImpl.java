package com.iiitd.erp.persistent.persistence;

import com.iiitd.erp.Domain.AssessmentComponentDomain;
import com.iiitd.erp.core.AssessmentComponentPersistPort;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.persistent.entity.erp_db.AssessmentComponent;
import com.iiitd.erp.persistent.persistence.repository.AssessmentComponentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.iiitd.erp.mappers.DomainToEntityMapper.DOMAIN_TO_ENTITY_MAPPER_INSTANCE;
import static com.iiitd.erp.mappers.EntityToDomainMapper.ENTITY_TO_DOMAIN_MAPPER_INSTANCE;

@Repository
@Transactional
public class AssessmentComponentPersistPortImpl implements AssessmentComponentPersistPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssessmentComponentPersistPortImpl.class);

    private final AssessmentComponentRepository repository;

    public AssessmentComponentPersistPortImpl(AssessmentComponentRepository repository) {
        this.repository = repository;
    }

    @Override
    public AssessmentComponentDomain save(AssessmentComponentDomain domain) {
        LOGGER.debug("Persisting assessment {} for section {}", domain.getName(), domain.getSectionId());
        try {
            if (domain.getComponentId() == null) {
                domain = domain.withComponentId(UUID.randomUUID());
            }
            AssessmentComponent saved = repository.save(
                    DOMAIN_TO_ENTITY_MAPPER_INSTANCE.assessmentComponentDomainToAssessmentComponent(domain));
            LOGGER.info("Persisted assessment {} for section {}", domain.getName(), domain.getSectionId());
            return ENTITY_TO_DOMAIN_MAPPER_INSTANCE.assessmentComponentToDomain(saved);
        } catch (Exception ex) {
            LOGGER.error("Failed to persist assessment {} for section {}", domain.getName(), domain.getSectionId(), ex);
            throw ErpException.wrap(ErrorCode.ASSESSMENT_CREATE_FAILED, ex,
                    Map.of("sectionId", domain.getSectionId(), "name", domain.getName()));
        }
    }

    @Override
    public AssessmentComponentDomain update(AssessmentComponentDomain domain) {
        UUID componentId = domain.getComponentId();
        if (componentId == null) {
            throw ErpException.of(ErrorCode.ASSESSMENT_NOT_FOUND, Map.of("componentId", null));
        }
        repository.findById(componentId)
                .orElseThrow(() -> ErpException.of(ErrorCode.ASSESSMENT_NOT_FOUND, Map.of("componentId", componentId)));
        try {
            AssessmentComponent saved = repository.save(
                    DOMAIN_TO_ENTITY_MAPPER_INSTANCE.assessmentComponentDomainToAssessmentComponent(domain));
            LOGGER.info("Updated assessment {}", componentId);
            return ENTITY_TO_DOMAIN_MAPPER_INSTANCE.assessmentComponentToDomain(saved);
        } catch (Exception ex) {
            LOGGER.error("Failed to update assessment {}", componentId, ex);
            throw ErpException.wrap(ErrorCode.ASSESSMENT_UPDATE_FAILED, ex, Map.of("componentId", componentId));
        }
    }

    @Override
    public AssessmentComponentDomain findById(UUID componentId) {
        return repository.findById(componentId)
                .map(ENTITY_TO_DOMAIN_MAPPER_INSTANCE::assessmentComponentToDomain)
                .orElse(null);
    }

    @Override
    public void delete(UUID componentId) {
        AssessmentComponent existing = repository.findById(componentId)
                .orElseThrow(() -> ErpException.of(ErrorCode.ASSESSMENT_NOT_FOUND, Map.of("componentId", componentId)));
        try {
            repository.delete(existing);
            LOGGER.info("Deleted assessment {}", componentId);
        } catch (Exception ex) {
            LOGGER.error("Failed to delete assessment {}", componentId, ex);
            throw ErpException.wrap(ErrorCode.ASSESSMENT_DELETE_FAILED, ex, Map.of("componentId", componentId));
        }
    }

    @Override
    public List<AssessmentComponentDomain> listBySection(String sectionId) {
        LOGGER.debug("Listing assessments for section {}", sectionId);
        try {
            return repository.findBySectionIdOrderByDueDateAscNameAsc(sectionId).stream()
                    .map(ENTITY_TO_DOMAIN_MAPPER_INSTANCE::assessmentComponentToDomain)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            LOGGER.error("Failed to list assessments for section {}", sectionId, ex);
            throw ErpException.wrap(ErrorCode.ASSESSMENT_CREATE_FAILED, ex, Map.of("sectionId", sectionId));
        }
    }

    @Override
    public double weightSum(String sectionId, UUID excludeComponentId) {
        return repository.weightSum(sectionId, excludeComponentId);
    }
}
