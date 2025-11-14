package com.iiitd.erp.persistent.persistence;

import com.iiitd.erp.Domain.ProgramDomain;
import com.iiitd.erp.core.ProgramPersistPort;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.persistent.entity.erp_db.Program;
import com.iiitd.erp.persistent.persistence.repository.ProgramRepository;
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
public class ProgramPersistPortImpl implements ProgramPersistPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProgramPersistPortImpl.class);

    private final ProgramRepository programRepository;

    public ProgramPersistPortImpl(ProgramRepository programRepository) {
        this.programRepository = programRepository;
    }

    @Override
    public ProgramDomain findByProgramId(String programId) {
        LOGGER.debug("Fetching program {}", programId);
        try {
            return programRepository.findByProgramId(programId)
                    .map(ENTITY_TO_DOMAIN_MAPPER_INSTANCE::programToProgramDomain)
                    .orElse(null);
        } catch (Exception ex) {
            LOGGER.error("Failed to fetch program {}", programId, ex);
            throw ErpException.wrap(ErrorCode.PROGRAM_FETCH_FAILED, ex, Map.of("programId", programId));
        }
    }

    @Override
    public ProgramDomain save(ProgramDomain programDomain) {
        LOGGER.debug("Persisting program {}", programDomain.getProgramId());
        try {
            Program entity = DOMAIN_TO_ENTITY_MAPPER_INSTANCE.programDomainToProgram(programDomain);
            Program saved = programRepository.save(entity);
            LOGGER.info("Persisted program {}", programDomain.getProgramId());
            return ENTITY_TO_DOMAIN_MAPPER_INSTANCE.programToProgramDomain(saved);
        } catch (Exception ex) {
            LOGGER.error("Failed to persist program {}", programDomain.getProgramId(), ex);
            throw ErpException.wrap(ErrorCode.PROGRAM_CREATE_FAILED, ex, Map.of("programId", programDomain.getProgramId()));
        }
    }

    @Override
    public ProgramDomain update(ProgramDomain programDomain) {
        String programId = programDomain.getProgramId();
        programRepository.findByProgramId(programId)
                .orElseThrow(() -> ErpException.of(ErrorCode.PROGRAM_NOT_FOUND, Map.of("programId", programId)));
        try {
            Program entity = DOMAIN_TO_ENTITY_MAPPER_INSTANCE.programDomainToProgram(programDomain);
            Program saved = programRepository.save(entity);
            LOGGER.info("Updated program {}", programId);
            return ENTITY_TO_DOMAIN_MAPPER_INSTANCE.programToProgramDomain(saved);
        } catch (Exception ex) {
            LOGGER.error("Failed to update program {}", programId, ex);
            throw ErpException.wrap(ErrorCode.PROGRAM_UPDATE_FAILED, ex, Map.of("programId", programId));
        }
    }

    @Override
    public boolean deleteByProgramId(String programId) {
        Program existing = programRepository.findByProgramId(programId)
                .orElseThrow(() -> ErpException.of(ErrorCode.PROGRAM_NOT_FOUND, Map.of("programId", programId)));
        try {
            programRepository.delete(existing);
            LOGGER.info("Deleted program {}", programId);
            return true;
        } catch (Exception ex) {
            LOGGER.error("Failed to delete program {}", programId, ex);
            throw ErpException.wrap(ErrorCode.PROGRAM_DELETE_FAILED, ex, Map.of("programId", programId));
        }
    }

    @Override
    public List<ProgramDomain> findAll() {
        LOGGER.debug("Fetching all programs");
        try {
            return programRepository.findAll().stream()
                    .map(ENTITY_TO_DOMAIN_MAPPER_INSTANCE::programToProgramDomain)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            LOGGER.error("Failed to fetch programs", ex);
            throw ErpException.wrap(ErrorCode.PROGRAM_FETCH_FAILED, ex, Map.of());
        }
    }
}
