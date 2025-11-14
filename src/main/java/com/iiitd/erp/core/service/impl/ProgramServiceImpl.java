package com.iiitd.erp.core.service.impl;

import com.iiitd.erp.Domain.ProgramDomain;
import com.iiitd.erp.core.ProgramPersistPort;
import com.iiitd.erp.core.service.ProgramService;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ProgramServiceImpl implements ProgramService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProgramServiceImpl.class);

    private final ProgramPersistPort programPersistPort;

    public ProgramServiceImpl(ProgramPersistPort programPersistPort) {
        this.programPersistPort = programPersistPort;
    }

    @Override
    public ProgramDomain createProgram(ProgramDomain programDomain) {
        String programId = programDomain.getProgramId();
        LOGGER.info("Creating program {}", programId);

        if (programPersistPort.findByProgramId(programId) != null) {
            throw ErpException.of(ErrorCode.PROGRAM_DUPLICATE, Map.of("programId", programId));
        }

        try {
            ProgramDomain saved = programPersistPort.save(programDomain);
            LOGGER.info("Created program {}", programId);
            return saved;
        } catch (ErpException ex) {
            LOGGER.error("Business error while creating program {}: {}", programId, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while creating program {}", programId, ex);
            throw ErpException.wrap(ErrorCode.PROGRAM_CREATE_FAILED, ex, Map.of("programId", programId));
        }
    }

    @Override
    public ProgramDomain updateProgram(ProgramDomain programDomain) {
        String programId = programDomain.getProgramId();
        LOGGER.info("Updating program {}", programId);

        if (programPersistPort.findByProgramId(programId) == null) {
            throw ErpException.of(ErrorCode.PROGRAM_NOT_FOUND, Map.of("programId", programId));
        }

        try {
            ProgramDomain updated = programPersistPort.update(programDomain);
            LOGGER.info("Updated program {}", programId);
            return updated;
        } catch (ErpException ex) {
            LOGGER.error("Business error while updating program {}: {}", programId, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while updating program {}", programId, ex);
            throw ErpException.wrap(ErrorCode.PROGRAM_UPDATE_FAILED, ex, Map.of("programId", programId));
        }
    }

    @Override
    public boolean deleteProgram(String programId) {
        LOGGER.info("Deleting program {}", programId);

        if (programPersistPort.findByProgramId(programId) == null) {
            throw ErpException.of(ErrorCode.PROGRAM_NOT_FOUND, Map.of("programId", programId));
        }

        try {
            boolean deleted = programPersistPort.deleteByProgramId(programId);
            LOGGER.info("Deleted program {}: {}", programId, deleted);
            return deleted;
        } catch (ErpException ex) {
            LOGGER.error("Business error while deleting program {}: {}", programId, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while deleting program {}", programId, ex);
            throw ErpException.wrap(ErrorCode.PROGRAM_DELETE_FAILED, ex, Map.of("programId", programId));
        }
    }

    @Override
    public List<ProgramDomain> listPrograms() {
        LOGGER.info("Listing all programs");
        try {
            return programPersistPort.findAll();
        } catch (ErpException ex) {
            LOGGER.error("Business error while listing programs: {}", ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while listing programs", ex);
            throw ErpException.wrap(ErrorCode.PROGRAM_FETCH_FAILED, ex, Map.of());
        }
    }
}
