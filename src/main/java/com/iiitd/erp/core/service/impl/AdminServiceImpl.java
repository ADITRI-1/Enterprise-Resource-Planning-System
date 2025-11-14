package com.iiitd.erp.core.service.impl;

import com.iiitd.erp.Domain.CreateUserDomain;
import com.iiitd.erp.Domain.InstructorDomain;
import com.iiitd.erp.Domain.StudentUserDomain;
import com.iiitd.erp.Domain.UserAuthDomain;
import com.iiitd.erp.core.InstructorPersistPort;
import com.iiitd.erp.core.StudentPersistPort;
import com.iiitd.erp.core.UserAuthPersistPort;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.io.dto.AccountStatus;
import com.iiitd.erp.io.dto.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
public class AdminServiceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminServiceImpl.class);

    private final StudentPersistPort studentPersistPort;
    private final InstructorPersistPort instructorPersistPort;
    private final UserAuthPersistPort userAuthPersistPort;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public AdminServiceImpl(StudentPersistPort studentPersistPort,
                            InstructorPersistPort instructorPersistPort,
                            UserAuthPersistPort userAuthPersistPort) {
        this.studentPersistPort = studentPersistPort;
        this.instructorPersistPort = instructorPersistPort;
        this.userAuthPersistPort = userAuthPersistPort;
    }

    public CreateUserDomain createStudentUser(StudentUserDomain studentDomain) {
        Integer rollNo = studentDomain.getRollNo();
        LOGGER.info("Attempting to create student user for roll {}", rollNo);

        StudentUserDomain existingStudent = studentPersistPort.findByRollNo(rollNo);
        if (existingStudent != null) {
            LOGGER.warn("Duplicate student creation attempt for roll {}", rollNo);
            throw ErpException.of(ErrorCode.DUPLICATE_STUDENT, Map.of("rollNo", rollNo));
        }

        try {
            String username = buildStudentUsername(studentDomain);
            String rawPassword = buildStudentPassword(studentDomain);
            String passwordHash = passwordEncoder.encode(rawPassword);

            userAuthPersistPort.save(
                    UserAuthDomain.builder()
                            .username(username)
                            .role("STUDENT")
                            .passwordHash(passwordHash)
                            .status("ACTIVE")
                            .active(true)
                            .build()
            );

            studentPersistPort.saveStudent(studentDomain);

            LOGGER.info("Successfully created student user {} for roll {}", username, rollNo);

            return CreateUserDomain.builder()
                    .username(username)
                    .role(Role.STUDENT)
                    .status(AccountStatus.ACTIVE)
                    .build();
        } catch (ErpException ex) {
            LOGGER.error("Known failure while creating student user for roll {}: {}", rollNo, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while creating student user for roll {}", rollNo, ex);
            throw ErpException.wrap(
                    ErrorCode.USER_CREATION_FAILED, ex, Map.of("rollNo", rollNo));
        }
    }

    public CreateUserDomain createInstructor(InstructorDomain instructorDomain) {
        String instructorCode = instructorDomain.getInstructorCode();
        LOGGER.info("Attempting to create instructor {}", instructorCode);

        InstructorDomain existing = instructorPersistPort.findByInstructorCode(instructorCode);
        if (existing != null) {
            LOGGER.warn("Duplicate instructor creation attempt for code {}", instructorCode);
            throw ErpException.of(ErrorCode.INSTRUCTOR_DUPLICATE, Map.of("instructorCode", instructorCode));
        }

        try {
            String username = buildInstructorUsername(instructorDomain);
            String rawPassword = buildInstructorPassword(instructorDomain);
            String passwordHash = passwordEncoder.encode(rawPassword);

            userAuthPersistPort.save(
                    UserAuthDomain.builder()
                            .username(username)
                            .role("INSTRUCTOR")
                            .passwordHash(passwordHash)
                            .status("ACTIVE")
                            .active(true)
                            .build()
            );

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
            throw ErpException.wrap(
                    ErrorCode.INSTRUCTOR_CREATE_FAILED, ex, Map.of("instructorCode", instructorCode));
        }
    }

    public StudentUserDomain updateStudentUser(StudentUserDomain studentDomain) {
        Integer rollNo = studentDomain.getRollNo();
        LOGGER.info("Updating student user for roll {}", rollNo);

        StudentUserDomain existing = studentPersistPort.findByRollNo(rollNo);
        if (existing == null) {
            throw ErpException.of(ErrorCode.STUDENT_NOT_FOUND, Map.of("rollNo", rollNo));
        }

        try {
            StudentUserDomain updated = studentPersistPort.updateStudent(studentDomain);
            LOGGER.info("Updated student user for roll {}", rollNo);
            return updated;
        } catch (ErpException ex) {
            LOGGER.error("Business error while updating student {}: {}", rollNo, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while updating student {}", rollNo, ex);
            throw ErpException.wrap(
                    ErrorCode.STUDENT_UPDATE_FAILED, ex, Map.of("rollNo", rollNo));
        }
    }

    public InstructorDomain updateInstructor(InstructorDomain instructorDomain) {
        String code = instructorDomain.getInstructorCode();
        LOGGER.info("Updating instructor {}", code);

        InstructorDomain existing = instructorPersistPort.findByInstructorCode(code);
        if (existing == null) {
            throw ErpException.of(ErrorCode.INSTRUCTOR_NOT_FOUND, Map.of("instructorCode", code));
        }

        try {
            InstructorDomain updated = instructorPersistPort.update(instructorDomain);
            LOGGER.info("Updated instructor {}", code);
            return updated;
        } catch (ErpException ex) {
            LOGGER.error("Business error while updating instructor {}: {}", code, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while updating instructor {}", code, ex);
            throw ErpException.wrap(
                    ErrorCode.INSTRUCTOR_UPDATE_FAILED, ex, Map.of("instructorCode", code));
        }
    }

    public boolean deleteStudentUser(Integer rollNo) {
        LOGGER.info("Deleting student user for roll {}", rollNo);

        StudentUserDomain existing = studentPersistPort.findByRollNo(rollNo);
        if (existing == null) {
            throw ErpException.of(ErrorCode.STUDENT_NOT_FOUND, Map.of("rollNo", rollNo));
        }

        try {
            boolean deleted = studentPersistPort.deleteStudent(rollNo);
            LOGGER.info("Deleted student user for roll {}: {}", rollNo, deleted);
            return deleted;
        } catch (ErpException ex) {
            LOGGER.error("Business error while deleting student {}: {}", rollNo, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while deleting student {}", rollNo, ex);
            throw ErpException.wrap(
                    ErrorCode.STUDENT_DELETE_FAILED, ex, Map.of("rollNo", rollNo));
        }
    }

    public boolean deleteInstructor(String instructorCode) {
        LOGGER.info("Deleting instructor {}", instructorCode);

        InstructorDomain existing = instructorPersistPort.findByInstructorCode(instructorCode);
        if (existing == null) {
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
            throw ErpException.wrap(
                    ErrorCode.INSTRUCTOR_DELETE_FAILED, ex, Map.of("instructorCode", instructorCode));
        }
    }

    private String buildStudentUsername(StudentUserDomain studentDomain) {
        String firstName = studentDomain.getName().split(" ")[0].toLowerCase();
        String programType = studentDomain.getProgramId().toLowerCase().contains("b.tech") ? "bt" : "mt";
        return firstName + "_" + programType + studentDomain.getRollNo();
    }

    private String buildStudentPassword(StudentUserDomain studentDomain) {
        String dob = studentDomain.getDateOfBirth();
        String monthDay = dob.substring(3, 5) + dob.substring(0, 2);
        String rollNo = studentDomain.getRollNo().toString();
        String lastThreeRollNo = rollNo.substring(rollNo.length() - 3);
        return monthDay + lastThreeRollNo;
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
