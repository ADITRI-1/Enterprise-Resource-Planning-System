package com.iiitd.erp.persistent.persistence;

import com.iiitd.erp.Domain.DepartmentDomain;
import com.iiitd.erp.core.DepartmentPersistPort;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.persistent.entity.erp_db.Department;
import com.iiitd.erp.persistent.persistence.repository.DepartmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.iiitd.erp.mappers.DomainToEntityMapper.DOMAIN_TO_ENTITY_MAPPER_INSTANCE;
import static com.iiitd.erp.mappers.EntityToDomainMapper.ENTITY_TO_DOMAIN_MAPPER_INSTANCE;

@Repository
@Transactional
public class DepartmentPersistPortImpl implements DepartmentPersistPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(DepartmentPersistPortImpl.class);

    private final DepartmentRepository departmentRepository;

    public DepartmentPersistPortImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public DepartmentDomain findByDeptId(String deptId) {
        LOGGER.debug("Fetching department {}", deptId);
        try {
            return departmentRepository.findByDeptId(deptId)
                    .map(ENTITY_TO_DOMAIN_MAPPER_INSTANCE::departmentToDepartmentDomain)
                    .orElse(null);
        } catch (Exception ex) {
            LOGGER.error("Failed to fetch department {}", deptId, ex);
            throw ErpException.wrap(ErrorCode.DEPARTMENT_FETCH_FAILED, ex, Map.of("deptId", deptId));
        }
    }

    @Override
    public DepartmentDomain save(DepartmentDomain departmentDomain) {
        LOGGER.debug("Persisting department {}", departmentDomain.getDeptId());
        try {
            Department entity = DOMAIN_TO_ENTITY_MAPPER_INSTANCE.departmentDomainToDepartment(departmentDomain);
            Department saved = departmentRepository.save(entity);
            LOGGER.info("Persisted department {}", departmentDomain.getDeptId());
            return ENTITY_TO_DOMAIN_MAPPER_INSTANCE.departmentToDepartmentDomain(saved);
        } catch (Exception ex) {
            LOGGER.error("Failed to persist department {}", departmentDomain.getDeptId(), ex);
            throw ErpException.wrap(ErrorCode.DEPARTMENT_CREATE_FAILED, ex, Map.of("deptId", departmentDomain.getDeptId()));
        }
    }

    @Override
    public DepartmentDomain update(DepartmentDomain departmentDomain) {
        String deptId = departmentDomain.getDeptId();
        departmentRepository.findByDeptId(deptId)
                .orElseThrow(() -> ErpException.of(ErrorCode.DEPARTMENT_NOT_FOUND, Map.of("deptId", deptId)));
        try {
            Department entity = DOMAIN_TO_ENTITY_MAPPER_INSTANCE.departmentDomainToDepartment(departmentDomain);
            Department saved = departmentRepository.save(entity);
            LOGGER.info("Updated department {}", deptId);
            return ENTITY_TO_DOMAIN_MAPPER_INSTANCE.departmentToDepartmentDomain(saved);
        } catch (Exception ex) {
            LOGGER.error("Failed to update department {}", deptId, ex);
            throw ErpException.wrap(ErrorCode.DEPARTMENT_UPDATE_FAILED, ex, Map.of("deptId", deptId));
        }
    }

    @Override
    public boolean deleteByDeptId(String deptId) {
        Department existing = departmentRepository.findByDeptId(deptId)
                .orElseThrow(() -> ErpException.of(ErrorCode.DEPARTMENT_NOT_FOUND, Map.of("deptId", deptId)));
        try {
            departmentRepository.delete(existing);
            LOGGER.info("Deleted department {}", deptId);
            return true;
        } catch (Exception ex) {
            LOGGER.error("Failed to delete department {}", deptId, ex);
            throw ErpException.wrap(ErrorCode.DEPARTMENT_DELETE_FAILED, ex, Map.of("deptId", deptId));
        }
    }

    @Override
    public List<DepartmentDomain> findAll() {
        LOGGER.debug("Fetching all departments");
        try {
            return departmentRepository.findAll().stream()
                    .map(ENTITY_TO_DOMAIN_MAPPER_INSTANCE::departmentToDepartmentDomain)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            LOGGER.error("Failed to fetch departments", ex);
            throw ErpException.wrap(ErrorCode.DEPARTMENT_FETCH_FAILED, ex, Map.of());
        }
    }
}
