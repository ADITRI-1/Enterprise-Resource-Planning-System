package com.iiitd.erp.persistent.persistence;

import com.iiitd.erp.Domain.ClassScheduleDomain;
import com.iiitd.erp.core.ClassSchedulePersistPort;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.persistent.entity.erp_db.ClassSchedule;
import com.iiitd.erp.persistent.persistence.repository.ClassScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.iiitd.erp.mappers.DomainToEntityMapper.DOMAIN_TO_ENTITY_MAPPER_INSTANCE;
import static com.iiitd.erp.mappers.EntityToDomainMapper.ENTITY_TO_DOMAIN_MAPPER_INSTANCE;

@Repository
@Transactional
public class ClassSchedulePersistPortImpl implements ClassSchedulePersistPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassSchedulePersistPortImpl.class);

    private final ClassScheduleRepository repository;

    public ClassSchedulePersistPortImpl(ClassScheduleRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ClassScheduleDomain> findBySectionId(String sectionId) {
        LOGGER.debug("Fetching schedules for section {}", sectionId);
        try {
            return repository.findBySectionIdOrderByDayOfWeekAscStartTimeAsc(sectionId).stream()
                    .map(ENTITY_TO_DOMAIN_MAPPER_INSTANCE::classScheduleToClassScheduleDomain)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            LOGGER.error("Failed to fetch schedules for section {}", sectionId, ex);
            throw ErpException.wrap(ErrorCode.SECTION_FETCH_FAILED, ex, Map.of("sectionId", sectionId));
        }
    }

    @Override
    public List<ClassScheduleDomain> replaceSectionSchedule(String sectionId, List<ClassScheduleDomain> slots) {
        LOGGER.debug("Replacing schedule for section {} with {} slots", sectionId, slots.size());
        try {
            repository.deleteBySectionId(sectionId);
            List<ClassSchedule> entities = slots.stream()
                    .map(slot -> slot.getScheduleId() == null
                            ? slot.withScheduleId(UUID.randomUUID())
                            : slot)
                    .map(DOMAIN_TO_ENTITY_MAPPER_INSTANCE::classScheduleDomainToClassSchedule)
                    .toList();
            List<ClassScheduleDomain> saved = repository.saveAll(entities).stream()
                    .sorted(Comparator.comparing(ClassSchedule::getDayOfWeek)
                            .thenComparing(ClassSchedule::getStartTime))
                    .map(ENTITY_TO_DOMAIN_MAPPER_INSTANCE::classScheduleToClassScheduleDomain)
                    .collect(Collectors.toList());
            LOGGER.info("Persisted {} schedule slots for section {}", saved.size(), sectionId);
            return saved;
        } catch (Exception ex) {
            LOGGER.error("Failed to persist schedule for section {}", sectionId, ex);
            throw ErpException.wrap(ErrorCode.SECTION_UPDATE_FAILED, ex, Map.of("sectionId", sectionId));
        }
    }

    @Override
    public void deleteBySectionId(String sectionId) {
        LOGGER.debug("Clearing schedule for section {}", sectionId);
        try {
            repository.deleteBySectionId(sectionId);
        } catch (Exception ex) {
            LOGGER.error("Failed to delete schedules for section {}", sectionId, ex);
            throw ErpException.wrap(ErrorCode.SECTION_DELETE_FAILED, ex, Map.of("sectionId", sectionId));
        }
    }

    @Override
    public List<ClassScheduleDomain> findBySectionIds(List<String> sectionIds) {
        try {
            return repository.findBySectionIdInOrderByDayOfWeekAscStartTimeAsc(sectionIds).stream()
                    .map(ENTITY_TO_DOMAIN_MAPPER_INSTANCE::classScheduleToClassScheduleDomain)
                    .toList();
        } catch (Exception ex) {
            LOGGER.error("Failed to fetch schedules for sections {}", sectionIds, ex);
            throw ErpException.wrap(ErrorCode.SECTION_FETCH_FAILED, ex, Map.of("sectionIds", sectionIds));
        }
    }
}
