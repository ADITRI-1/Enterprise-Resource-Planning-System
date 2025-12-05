package com.iiitd.erp.core.service;

import com.iiitd.erp.Domain.CreateUserDomain;
import com.iiitd.erp.Domain.StudentUserDomain;

import java.util.List;

public interface StudentService {
    CreateUserDomain createStudentUser(StudentUserDomain student);
    StudentUserDomain updateStudentUser(StudentUserDomain student);
    boolean deleteStudentUser(String rollNo);
    List<StudentUserDomain> listStudents();
}
