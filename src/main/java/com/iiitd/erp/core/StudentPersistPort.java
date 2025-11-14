package com.iiitd.erp.core;

import com.iiitd.erp.Domain.StudentUserDomain;

public interface StudentPersistPort {
    StudentUserDomain findByRollNo(Integer rollNo);
    boolean saveStudent(StudentUserDomain studentDomain);
    StudentUserDomain updateStudent(StudentUserDomain studentDomain);
    boolean deleteStudent(Integer rollNo);
}
