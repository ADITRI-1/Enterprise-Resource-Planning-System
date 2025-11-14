package com.iiitd.erp.core;

import com.iiitd.erp.Domain.InstructorDomain;

import java.util.List;

public interface InstructorPersistPort {
    InstructorDomain findByInstructorCode(String instructorCode);
    InstructorDomain save(InstructorDomain instructorDomain);
    InstructorDomain update(InstructorDomain instructorDomain);
    boolean deleteByInstructorCode(String instructorCode);
    List<InstructorDomain> findAll();
}
