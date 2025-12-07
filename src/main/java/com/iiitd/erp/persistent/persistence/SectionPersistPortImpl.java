package com.iiitd.erp.persistent.persistence;

import com.iiitd.erp.Domain.SectionDomain;
import com.iiitd.erp.core.SectionPersistPort;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.persistent.entity.erp_db.Section;
import com.iiitd.erp.persistent.persistence.repository.SectionRepository;
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
public class SectionPersistPortImpl implements SectionPersistPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(SectionPersistPortImpl.class);
    private final SectionRepository sectionRepository;

    public SectionPersistPortImpl(SectionRepository sectionRepository) {
        this.sectionRepository = sectionRepository;
    }

    @Override
    public SectionDomain findBySectionId(String sectionId) {
        LOGGER.debug("Fetching section {}", sectionId);
        try {
            return sectionRepository.findById(sectionId)
                    .map(ENTITY_TO_DOMAIN_MAPPER_INSTANCE::sectionToSectionDomain)
                    .orElse(null);
        } catch (Exception ex) {
            LOGGER.error("Failed to fetch section {}", sectionId, ex);
            throw ErpException.wrap(ErrorCode.SECTION_FETCH_FAILED, ex, Map.of("sectionId", sectionId));
        }
    }

    @Override
    public SectionDomain save(SectionDomain sectionDomain) {
        LOGGER.debug("Persisting section {}", sectionDomain.getSectionId());
        try {
            Section saved = sectionRepository.save(DOMAIN_TO_ENTITY_MAPPER_INSTANCE.sectionDomainToSection(sectionDomain));
            return ENTITY_TO_DOMAIN_MAPPER_INSTANCE.sectionToSectionDomain(saved);
        } catch (Exception ex) {
            LOGGER.error("Failed to persist section {}", sectionDomain.getSectionId(), ex);
            throw ErpException.wrap(ErrorCode.SECTION_CREATE_FAILED, ex, Map.of("sectionId", sectionDomain.getSectionId()));
        }
    }

    @Override
    public SectionDomain update(SectionDomain sectionDomain) {
        String sectionId = sectionDomain.getSectionId();
        LOGGER.debug("Updating section {}", sectionId);
        sectionRepository.findById(sectionId)
                .orElseThrow(() -> ErpException.of(ErrorCode.SECTION_NOT_FOUND, Map.of("sectionId", sectionId)));
        try {
            Section saved = sectionRepository.save(DOMAIN_TO_ENTITY_MAPPER_INSTANCE.sectionDomainToSection(sectionDomain));
            return ENTITY_TO_DOMAIN_MAPPER_INSTANCE.sectionToSectionDomain(saved);
        } catch (Exception ex) {
            LOGGER.error("Failed to update section {}", sectionId, ex);
            throw ErpException.wrap(ErrorCode.SECTION_UPDATE_FAILED, ex, Map.of("sectionId", sectionId));
        }
    }

    @Override
    public List<SectionDomain> findAll(String semester, Integer year) {
        LOGGER.debug("Listing all sections (semester={}, year={})", semester, year);
        try {
            return sectionRepository.findAllFiltered(semester, year).stream()
                    .map(ENTITY_TO_DOMAIN_MAPPER_INSTANCE::sectionToSectionDomain)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            LOGGER.error("Failed to list sections (semester={}, year={})", semester, year, ex);
            throw ErpException.wrap(ErrorCode.SECTION_LIST_FAILED, ex, Map.of("semester", semester, "year", year));
        }
    }


    @Override
    public boolean delete(String sectionId) {
        LOGGER.debug("Soft-deleting section {}", sectionId);
        Section existing = sectionRepository.findById(sectionId)
                .orElseThrow(() -> ErpException.of(ErrorCode.SECTION_NOT_FOUND, Map.of("sectionId", sectionId)));
        try {
            existing.setIsActive(false);
            sectionRepository.save(existing);
            LOGGER.info("Marked section {} as inactive", sectionId);
            return true;
        } catch (Exception ex) {
            LOGGER.error("Failed to mark section {} inactive", sectionId, ex);
            throw ErpException.wrap(ErrorCode.SECTION_DELETE_FAILED, ex, Map.of("sectionId", sectionId));
        }
    }

    @Override
    public List<SectionDomain> findByCourse(String courseId, String semester, Integer year) {
        LOGGER.debug("Listing sections for course {} (semester={}, year={})", courseId, semester, year);
        try {
            List<Section> rows = (semester != null && year != null)
                    ? sectionRepository.findByCourseIdAndSemesterIgnoreCaseAndYear(courseId, semester, year)
                    : sectionRepository.findByCourseId(courseId);
            return rows.stream()
                    .map(ENTITY_TO_DOMAIN_MAPPER_INSTANCE::sectionToSectionDomain)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            LOGGER.error("Failed to list sections for course {}", courseId, ex);
            throw ErpException.wrap(ErrorCode.SECTION_LIST_FAILED, ex, Map.of("courseId", courseId));
        }
    }

    @Override
    public List<SectionDomain> findByInstructor(String instructorCode, String semester, Integer year) {
        LOGGER.debug("Listing sections for instructor {} (semester={}, year={})", instructorCode, semester, year);
        try {
            List<Section> rows = (semester != null && year != null)
                    ? sectionRepository.findByInstructorCodeIgnoreCaseAndSemesterIgnoreCaseAndYear(instructorCode, semester, year)
                    : sectionRepository.findByInstructorCodeIgnoreCase(instructorCode);
            return rows.stream()
                    .map(ENTITY_TO_DOMAIN_MAPPER_INSTANCE::sectionToSectionDomain)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            LOGGER.error("Failed to list sections for instructor {}", instructorCode, ex);
            throw ErpException.wrap(ErrorCode.SECTION_LIST_FAILED, ex, Map.of("instructorCode", instructorCode));
        }
    }
}
