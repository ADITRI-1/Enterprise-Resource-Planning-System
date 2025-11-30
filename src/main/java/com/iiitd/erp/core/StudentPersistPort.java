package com.iiitd.erp.core;

import com.iiitd.erp.Domain.StudentUserDomain;

import java.util.List;
import java.util.UUID;

public interface StudentPersistPort {
    StudentUserDomain findByRollNo(Integer rollNo);
    StudentUserDomain findByStudentId(UUID studentId);
    boolean saveStudent(StudentUserDomain studentDomain);
    StudentUserDomain updateStudent(StudentUserDomain studentDomain);
    boolean deleteStudent(Integer rollNo);
    List<StudentUserDomain> findAll();
}
