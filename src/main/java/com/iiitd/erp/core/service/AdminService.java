package com.iiitd.erp.core.service;

import com.iiitd.erp.Domain.CreateUserDomain;
import com.iiitd.erp.Domain.InstructorDomain;
import com.iiitd.erp.Domain.StudentUserDomain;

public interface AdminService {
    CreateUserDomain createStudentUser(StudentUserDomain studentUserDomain);
    CreateUserDomain createInstructor(InstructorDomain instructorDomain);
    StudentUserDomain updateStudentUser(StudentUserDomain studentUserDomain);
    InstructorDomain updateInstructor(InstructorDomain instructorDomain);
    boolean deleteStudentUser(Integer rollNo);
    boolean deleteInstructor(String instructorCode);
}
