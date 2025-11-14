package com.iiitd.erp.core;

import com.iiitd.erp.Domain.ProgramDomain;

import java.util.List;

public interface ProgramPersistPort {
    ProgramDomain findByProgramId(String programId);
    ProgramDomain save(ProgramDomain programDomain);
    ProgramDomain update(ProgramDomain programDomain);
    boolean deleteByProgramId(String programId);
    List<ProgramDomain> findAll();
}
