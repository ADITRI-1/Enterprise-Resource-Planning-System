package com.iiitd.erp.core.service.impl;

import com.iiitd.erp.Domain.DepartmentDomain;
import com.iiitd.erp.core.DepartmentPersistPort;
import com.iiitd.erp.core.service.DepartmentService;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DepartmentServiceImpl.class);

    private final DepartmentPersistPort departmentPersistPort;

    public DepartmentServiceImpl(DepartmentPersistPort departmentPersistPort) {
        this.departmentPersistPort = departmentPersistPort;
    }

    @Override
    public DepartmentDomain createDepartment(DepartmentDomain departmentDomain) {
        String deptId = departmentDomain.getDeptId();
        LOGGER.info("Creating department {}", deptId);

        if (departmentPersistPort.findByDeptId(deptId) != null) {
            throw ErpException.of(ErrorCode.DEPARTMENT_DUPLICATE, Map.of("deptId", deptId));
        }

        try {
            DepartmentDomain saved = departmentPersistPort.save(departmentDomain);
            LOGGER.info("Created department {}", deptId);
            return saved;
        } catch (ErpException ex) {
            LOGGER.error("Business error while creating department {}: {}", deptId, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while creating department {}", deptId, ex);
            throw ErpException.wrap(ErrorCode.DEPARTMENT_CREATE_FAILED, ex, Map.of("deptId", deptId));
        }
    }

    @Override
    public DepartmentDomain updateDepartment(DepartmentDomain departmentDomain) {
        String deptId = departmentDomain.getDeptId();
        LOGGER.info("Updating department {}", deptId);

        if (departmentPersistPort.findByDeptId(deptId) == null) {
            throw ErpException.of(ErrorCode.DEPARTMENT_NOT_FOUND, Map.of("deptId", deptId));
        }

        try {
            DepartmentDomain updated = departmentPersistPort.update(departmentDomain);
            LOGGER.info("Updated department {}", deptId);
            return updated;
        } catch (ErpException ex) {
            LOGGER.error("Business error while updating department {}: {}", deptId, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while updating department {}", deptId, ex);
            throw ErpException.wrap(ErrorCode.DEPARTMENT_UPDATE_FAILED, ex, Map.of("deptId", deptId));
        }
    }

    @Override
    public boolean deleteDepartment(String deptId) {
        LOGGER.info("Deleting department {}", deptId);

        if (departmentPersistPort.findByDeptId(deptId) == null) {
            throw ErpException.of(ErrorCode.DEPARTMENT_NOT_FOUND, Map.of("deptId", deptId));
        }

        try {
            boolean deleted = departmentPersistPort.deleteByDeptId(deptId);
            LOGGER.info("Deleted department {}: {}", deptId, deleted);
            return deleted;
        } catch (ErpException ex) {
            LOGGER.error("Business error while deleting department {}: {}", deptId, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while deleting department {}", deptId, ex);
            throw ErpException.wrap(ErrorCode.DEPARTMENT_DELETE_FAILED, ex, Map.of("deptId", deptId));
        }
    }

    @Override
    public List<DepartmentDomain> listDepartments() {
        LOGGER.info("Listing all departments");
        try {
            return departmentPersistPort.findAll();
        } catch (ErpException ex) {
            LOGGER.error("Business error while listing departments: {}", ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while listing departments", ex);
            throw ErpException.wrap(ErrorCode.DEPARTMENT_FETCH_FAILED, ex, Map.of());
        }
    }
}
