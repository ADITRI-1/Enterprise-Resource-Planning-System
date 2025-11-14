package com.iiitd.erp.core.service;

import com.iiitd.erp.Domain.CreateUserDomain;
import com.iiitd.erp.Domain.InstructorDomain;

import java.util.List;

public interface InstructorService {
    CreateUserDomain createInstructor(InstructorDomain instructor);
    InstructorDomain updateInstructor(InstructorDomain instructor);
    boolean deleteInstructor(String instructorCode);
    List<InstructorDomain> listInstructors();
}
