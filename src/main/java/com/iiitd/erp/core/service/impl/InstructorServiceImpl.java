package com.iiitd.erp.core.service.impl;

import com.iiitd.erp.Domain.CreateUserDomain;
import com.iiitd.erp.Domain.InstructorDomain;
import com.iiitd.erp.Domain.UserAuthDomain;
import com.iiitd.erp.core.DepartmentPersistPort;
import com.iiitd.erp.core.InstructorPersistPort;
import com.iiitd.erp.core.UserAuthPersistPort;
import com.iiitd.erp.core.service.InstructorService;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.io.dto.AccountStatus;
import com.iiitd.erp.io.dto.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class InstructorServiceImpl implements InstructorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstructorServiceImpl.class);

    private final InstructorPersistPort instructorPersistPort;
    private final DepartmentPersistPort departmentPersistPort;
    private final UserAuthPersistPort userAuthPersistPort;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public InstructorServiceImpl(InstructorPersistPort instructorPersistPort,
                                 DepartmentPersistPort departmentPersistPort,
                                 UserAuthPersistPort userAuthPersistPort) {
        this.instructorPersistPort = instructorPersistPort;
        this.departmentPersistPort = departmentPersistPort;
        this.userAuthPersistPort = userAuthPersistPort;
    }

    @Override
    public CreateUserDomain createInstructor(InstructorDomain instructorDomain) {
        String instructorCode = instructorDomain.getInstructorCode();
        LOGGER.info("Attempting to create instructor {}", instructorCode);

        if (instructorPersistPort.findByInstructorCode(instructorCode) != null) {
            LOGGER.warn("Duplicate instructor creation attempt for code {}", instructorCode);
            throw ErpException.of(ErrorCode.INSTRUCTOR_DUPLICATE, Map.of("instructorCode", instructorCode));
        }

        ensureDepartmentExists(instructorDomain.getDeptId());

        try {
            String username = buildInstructorUsername(instructorDomain);
            String rawPassword = buildInstructorPassword(instructorDomain);
            String passwordHash = passwordEncoder.encode(rawPassword);

            userAuthPersistPort.save(UserAuthDomain.builder()
                    .username(username)
                    .role("INSTRUCTOR")
                    .passwordHash(passwordHash)
                    .status("ACTIVE")
                    .active(true)
                    .build());

            instructorPersistPort.save(instructorDomain);

            LOGGER.info("Successfully created instructor {} with username {}", instructorCode, username);

            return CreateUserDomain.builder()
                    .username(username)
                    .role(Role.INSTRUCTOR)
                    .status(AccountStatus.ACTIVE)
                    .build();
        } catch (ErpException ex) {
            LOGGER.error("Known failure while creating instructor {}: {}", instructorCode, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while creating instructor {}", instructorCode, ex);
            throw ErpException.wrap(ErrorCode.INSTRUCTOR_CREATE_FAILED, ex, Map.of("instructorCode", instructorCode));
        }
    }

    @Override
    public InstructorDomain updateInstructor(InstructorDomain instructorDomain) {
        String code = instructorDomain.getInstructorCode();
        LOGGER.info("Updating instructor {}", code);

        if (instructorPersistPort.findByInstructorCode(code) == null) {
            throw ErpException.of(ErrorCode.INSTRUCTOR_NOT_FOUND, Map.of("instructorCode", code));
        }

        ensureDepartmentExists(instructorDomain.getDeptId());

        try {
            InstructorDomain updated = instructorPersistPort.update(instructorDomain);
            LOGGER.info("Updated instructor {}", code);
            return updated;
        } catch (ErpException ex) {
            LOGGER.error("Business error while updating instructor {}: {}", code, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while updating instructor {}", code, ex);
            throw ErpException.wrap(ErrorCode.INSTRUCTOR_UPDATE_FAILED, ex, Map.of("instructorCode", code));
        }
    }

    @Override
    public boolean deleteInstructor(String instructorCode) {
        LOGGER.info("Deleting instructor {}", instructorCode);

        if (instructorPersistPort.findByInstructorCode(instructorCode) == null) {
            throw ErpException.of(ErrorCode.INSTRUCTOR_NOT_FOUND, Map.of("instructorCode", instructorCode));
        }

        try {
            boolean deleted = instructorPersistPort.deleteByInstructorCode(instructorCode);
            LOGGER.info("Deleted instructor {}: {}", instructorCode, deleted);
            return deleted;
        } catch (ErpException ex) {
            LOGGER.error("Business error while deleting instructor {}: {}", instructorCode, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while deleting instructor {}", instructorCode, ex);
            throw ErpException.wrap(ErrorCode.INSTRUCTOR_DELETE_FAILED, ex, Map.of("instructorCode", instructorCode));
        }
    }

    @Override
    public List<InstructorDomain> listInstructors() {
        LOGGER.info("Listing all instructors");
        try {
            return instructorPersistPort.findAll();
        } catch (ErpException ex) {
            LOGGER.error("Business error while listing instructors: {}", ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while listing instructors", ex);
            throw ErpException.wrap(ErrorCode.INSTRUCTOR_FETCH_FAILED, ex, Map.of());
        }
    }

    private void ensureDepartmentExists(String deptId) {
        if (deptId == null || deptId.isBlank()) {
            throw ErpException.of(ErrorCode.DEPARTMENT_NOT_FOUND, Map.of("deptId", deptId));
        }
        if (departmentPersistPort.findByDeptId(deptId) == null) {
            throw ErpException.of(ErrorCode.DEPARTMENT_NOT_FOUND, Map.of("deptId", deptId));
        }
    }

    private String buildInstructorUsername(InstructorDomain instructorDomain) {
        String firstName = instructorDomain.getName().split(" ")[0].toLowerCase();
        return firstName + "_" + instructorDomain.getInstructorCode();
    }

    private String buildInstructorPassword(InstructorDomain instructorDomain) {
        String dob = instructorDomain.getDateOfBirth();
        String monthDay = dob.substring(3, 5) + dob.substring(0, 2);
        String code = instructorDomain.getInstructorCode();
        String lastThree = code.substring(Math.max(0, code.length() - 3));
        return monthDay + lastThree;
    }
}
