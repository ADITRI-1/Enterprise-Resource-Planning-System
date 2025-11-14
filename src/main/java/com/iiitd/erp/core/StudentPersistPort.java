package com.iiitd.erp.core;

import com.iiitd.erp.Domain.StudentUserDomain;

import java.util.List;

public interface StudentPersistPort {
    StudentUserDomain findByRollNo(Integer rollNo);
    boolean saveStudent(StudentUserDomain studentDomain);
    StudentUserDomain updateStudent(StudentUserDomain studentDomain);
    boolean deleteStudent(Integer rollNo);
    List<StudentUserDomain> findAll();
}
