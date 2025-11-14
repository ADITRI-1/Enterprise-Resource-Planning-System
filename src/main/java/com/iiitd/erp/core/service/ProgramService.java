package com.iiitd.erp.core.service;

import com.iiitd.erp.Domain.ProgramDomain;

import java.util.List;

public interface ProgramService {
    ProgramDomain createProgram(ProgramDomain programDomain);
    ProgramDomain updateProgram(ProgramDomain programDomain);
    boolean deleteProgram(String programId);
    List<ProgramDomain> listPrograms();
}
