package com.iiitd.erp.core.service.impl;

import com.iiitd.erp.Domain.CreateUserDomain;
import com.iiitd.erp.Domain.StudentUserDomain;
import com.iiitd.erp.Domain.UserAuthDomain;
import com.iiitd.erp.core.ProgramPersistPort;
import com.iiitd.erp.core.StudentPersistPort;
import com.iiitd.erp.core.UserAuthPersistPort;
import com.iiitd.erp.core.service.StudentService;
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
import java.util.UUID;

@Service
@Transactional
public class StudentServiceImpl implements StudentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StudentServiceImpl.class);

    private final StudentPersistPort studentPersistPort;
    private final ProgramPersistPort programPersistPort;
    private final UserAuthPersistPort userAuthPersistPort;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public StudentServiceImpl(StudentPersistPort studentPersistPort,
                              ProgramPersistPort programPersistPort,
                              UserAuthPersistPort userAuthPersistPort) {
        this.studentPersistPort = studentPersistPort;
        this.programPersistPort = programPersistPort;
        this.userAuthPersistPort = userAuthPersistPort;
    }

    @Override
    public CreateUserDomain createStudentUser(StudentUserDomain studentDomain) {
        String rollNo = studentDomain.getRollNo();
        LOGGER.info("Attempting to create student user for roll {}", rollNo);

        if (studentPersistPort.findByRollNo(rollNo) != null) {
            LOGGER.warn("Duplicate student creation attempt for roll {}", rollNo);
            throw ErpException.of(ErrorCode.DUPLICATE_STUDENT, Map.of("rollNo", rollNo));
        }

        ensureProgramExists(studentDomain.getProgramId());

        try {
            UUID userId = UUID.randomUUID();
            String username = buildStudentUsername(studentDomain);
            String rawPassword = buildStudentPassword(studentDomain);
            String passwordHash = passwordEncoder.encode(rawPassword);

            userAuthPersistPort.save(UserAuthDomain.builder()
                    .username(username)
                    .role("student")
                    .passwordHash(passwordHash)
                    .status("active")
                    .active(true)
                    .build());

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
            throw ErpException.wrap(ErrorCode.USER_CREATION_FAILED, ex, Map.of("rollNo", rollNo));
        }
    }

    @Override
    public StudentUserDomain updateStudentUser(StudentUserDomain studentDomain) {
        String rollNo = studentDomain.getRollNo();
        LOGGER.info("Updating student user for roll {}", rollNo);

        if (studentPersistPort.findByRollNo(rollNo) == null) {
            throw ErpException.of(ErrorCode.STUDENT_NOT_FOUND, Map.of("rollNo", rollNo));
        }

        ensureProgramExists(studentDomain.getProgramId());

        try {
            StudentUserDomain updated = studentPersistPort.updateStudent(studentDomain);
            LOGGER.info("Updated student user for roll {}", rollNo);
            return updated;
        } catch (ErpException ex) {
            LOGGER.error("Business error while updating student {}: {}", rollNo, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while updating student {}", rollNo, ex);
            throw ErpException.wrap(ErrorCode.STUDENT_UPDATE_FAILED, ex, Map.of("rollNo", rollNo));
        }
    }

    @Override
    public boolean deleteStudentUser(String rollNo) {
        LOGGER.info("Deleting student user for roll {}", rollNo);

        if (studentPersistPort.findByRollNo(rollNo) == null) {
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
            throw ErpException.wrap(ErrorCode.STUDENT_DELETE_FAILED, ex, Map.of("rollNo", rollNo));
        }
    }

    @Override
    public List<StudentUserDomain> listStudents() {
        LOGGER.info("Listing all students");
        try {
            return studentPersistPort.findAll();
        } catch (ErpException ex) {
            LOGGER.error("Business error while listing students: {}", ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while listing students", ex);
            throw ErpException.wrap(ErrorCode.STUDENT_FETCH_FAILED, ex, Map.of());
        }
    }

    private void ensureProgramExists(String programId) {
        if (programId == null || programId.isBlank()) {
            throw ErpException.of(ErrorCode.PROGRAM_NOT_FOUND, Map.of("programId", programId));
        }
        if (programPersistPort.findByProgramId(programId) == null) {
            throw ErpException.of(ErrorCode.PROGRAM_NOT_FOUND, Map.of("programId", programId));
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
}
