package com.iiitd.erp.persistent.persistence;

import com.iiitd.erp.Domain.InstructorDomain;
import com.iiitd.erp.core.InstructorPersistPort;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.persistent.entity.erp_db.Instructor;
import com.iiitd.erp.persistent.persistence.repository.InstructorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

import static com.iiitd.erp.mappers.DomainToEntityMapper.DOMAIN_TO_ENTITY_MAPPER_INSTANCE;
import static com.iiitd.erp.mappers.EntityToDomainMapper.ENTITY_TO_DOMAIN_MAPPER_INSTANCE;

@Repository
@Transactional
public class InstructorPersistPortImpl implements InstructorPersistPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstructorPersistPortImpl.class);

    private final InstructorRepository instructorRepository;

    public InstructorPersistPortImpl(InstructorRepository instructorRepository) {
        this.instructorRepository = instructorRepository;
    }

    @Override
    public InstructorDomain findByInstructorCode(String instructorCode) {
        LOGGER.debug("Fetching instructor {}", instructorCode);
        try {
            return instructorRepository.findByInstructorCode(instructorCode)
                    .map(ENTITY_TO_DOMAIN_MAPPER_INSTANCE::instructorToInstructorDomain)
                    .orElse(null);
        } catch (Exception ex) {
            LOGGER.error("Failed to fetch instructor {}", instructorCode, ex);
            throw ErpException.wrap(
                    ErrorCode.INSTRUCTOR_FETCH_FAILED, ex, Map.of("instructorCode", instructorCode));
        }
    }

    @Override
    public InstructorDomain save(InstructorDomain instructorDomain) {
        LOGGER.debug("Persisting instructor {}", instructorDomain.getInstructorCode());
        try {
            Instructor entity = DOMAIN_TO_ENTITY_MAPPER_INSTANCE.instructorDomainToInstructor(instructorDomain);
            if (entity.getInstructorId() == null) {
                entity.setInstructorId(UUID.randomUUID());
            }
            Instructor saved = instructorRepository.save(entity);
            LOGGER.info("Persisted instructor {}", instructorDomain.getInstructorCode());
            return ENTITY_TO_DOMAIN_MAPPER_INSTANCE.instructorToInstructorDomain(saved);
        } catch (Exception ex) {
            LOGGER.error("Failed to persist instructor {}", instructorDomain.getInstructorCode(), ex);
            throw ErpException.wrap(
                    ErrorCode.INSTRUCTOR_CREATE_FAILED, ex, Map.of("instructorCode", instructorDomain.getInstructorCode()));
        }
    }

    @Override
    public InstructorDomain update(InstructorDomain instructorDomain) {
        String code = instructorDomain.getInstructorCode();
        Instructor existing = instructorRepository.findByInstructorCode(code)
                .orElseThrow(() -> ErpException.of(ErrorCode.INSTRUCTOR_NOT_FOUND, Map.of("instructorCode", code)));
        try {
            Instructor entity = DOMAIN_TO_ENTITY_MAPPER_INSTANCE.instructorDomainToInstructor(instructorDomain);
            entity.setInstructorId(existing.getInstructorId());
            Instructor saved = instructorRepository.save(entity);
            LOGGER.info("Updated instructor {}", code);
            return ENTITY_TO_DOMAIN_MAPPER_INSTANCE.instructorToInstructorDomain(saved);
        } catch (Exception ex) {
            LOGGER.error("Failed to update instructor {}", code, ex);
            throw ErpException.wrap(
                    ErrorCode.INSTRUCTOR_UPDATE_FAILED, ex, Map.of("instructorCode", code));
        }
    }

    @Override
    public boolean deleteByInstructorCode(String instructorCode) {
        Instructor existing = instructorRepository.findByInstructorCode(instructorCode)
                .orElseThrow(() -> ErpException.of(ErrorCode.INSTRUCTOR_NOT_FOUND, Map.of("instructorCode", instructorCode)));
        try {
            instructorRepository.delete(existing);
            LOGGER.info("Deleted instructor {}", instructorCode);
            return true;
        } catch (Exception ex) {
            LOGGER.error("Failed to delete instructor {}", instructorCode, ex);
            throw ErpException.wrap(
                    ErrorCode.INSTRUCTOR_DELETE_FAILED, ex, Map.of("instructorCode", instructorCode));
        }
    }
}
