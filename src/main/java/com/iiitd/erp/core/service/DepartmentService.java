package com.iiitd.erp.core.service;

import com.iiitd.erp.Domain.DepartmentDomain;

import java.util.List;

public interface DepartmentService {
    DepartmentDomain createDepartment(DepartmentDomain departmentDomain);
    DepartmentDomain updateDepartment(DepartmentDomain departmentDomain);
    boolean deleteDepartment(String deptId);
    List<DepartmentDomain> listDepartments();
}
