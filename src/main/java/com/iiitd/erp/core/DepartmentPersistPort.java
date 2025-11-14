package com.iiitd.erp.core;

import com.iiitd.erp.Domain.DepartmentDomain;

import java.util.List;

public interface DepartmentPersistPort {
    DepartmentDomain findByDeptId(String deptId);
    DepartmentDomain save(DepartmentDomain departmentDomain);
    DepartmentDomain update(DepartmentDomain departmentDomain);
    boolean deleteByDeptId(String deptId);
    List<DepartmentDomain> findAll();
}
