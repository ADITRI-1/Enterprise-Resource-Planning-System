package com.iiitd.erp.core.service.impl;

import com.iiitd.erp.Domain.CourseDomain;
import com.iiitd.erp.Domain.CreateUserDomain;
import com.iiitd.erp.Domain.DepartmentDomain;
import com.iiitd.erp.Domain.InstructorDomain;
import com.iiitd.erp.Domain.ProgramDomain;
import com.iiitd.erp.Domain.StudentUserDomain;
import com.iiitd.erp.Domain.UserAuthDomain;
import com.iiitd.erp.core.CoursePersistPort;
import com.iiitd.erp.core.DepartmentPersistPort;
import com.iiitd.erp.core.InstructorPersistPort;
import com.iiitd.erp.core.ProgramPersistPort;
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

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AdminServiceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminServiceImpl.class);

    private final StudentPersistPort studentPersistPort;
    private final InstructorPersistPort instructorPersistPort;
    private final ProgramPersistPort programPersistPort;
    private final DepartmentPersistPort departmentPersistPort;
    private final CoursePersistPort coursePersistPort;
    private final UserAuthPersistPort userAuthPersistPort;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public AdminServiceImpl(StudentPersistPort studentPersistPort,
                            InstructorPersistPort instructorPersistPort,
                            ProgramPersistPort programPersistPort,
                            DepartmentPersistPort departmentPersistPort,
                            CoursePersistPort coursePersistPort,
                            UserAuthPersistPort userAuthPersistPort) {
        this.studentPersistPort = studentPersistPort;
        this.instructorPersistPort = instructorPersistPort;
        this.programPersistPort = programPersistPort;
        this.departmentPersistPort = departmentPersistPort;
        this.coursePersistPort = coursePersistPort;
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

        ensureProgramExists(studentDomain.getProgramId());

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

        ensureDepartmentExists(instructorDomain.getDeptId());

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

    public ProgramDomain createProgram(ProgramDomain programDomain) {
        String programId = programDomain.getProgramId();
        LOGGER.info("Creating program {}", programId);

        if (programPersistPort.findByProgramId(programId) != null) {
            throw ErpException.of(ErrorCode.PROGRAM_DUPLICATE, Map.of("programId", programId));
        }

        try {
            ProgramDomain saved = programPersistPort.save(programDomain);
            LOGGER.info("Created program {}", programId);
            return saved;
        } catch (ErpException ex) {
            LOGGER.error("Business error while creating program {}: {}", programId, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while creating program {}", programId, ex);
            throw ErpException.wrap(
                    ErrorCode.PROGRAM_CREATE_FAILED, ex, Map.of("programId", programId));
        }
    }

    public ProgramDomain updateProgram(ProgramDomain programDomain) {
        String programId = programDomain.getProgramId();
        LOGGER.info("Updating program {}", programId);

        if (programPersistPort.findByProgramId(programId) == null) {
            throw ErpException.of(ErrorCode.PROGRAM_NOT_FOUND, Map.of("programId", programId));
        }

        try {
            ProgramDomain updated = programPersistPort.update(programDomain);
            LOGGER.info("Updated program {}", programId);
            return updated;
        } catch (ErpException ex) {
            LOGGER.error("Business error while updating program {}: {}", programId, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while updating program {}", programId, ex);
            throw ErpException.wrap(
                    ErrorCode.PROGRAM_UPDATE_FAILED, ex, Map.of("programId", programId));
        }
    }

    public boolean deleteProgram(String programId) {
        LOGGER.info("Deleting program {}", programId);

        if (programPersistPort.findByProgramId(programId) == null) {
            throw ErpException.of(ErrorCode.PROGRAM_NOT_FOUND, Map.of("programId", programId));
        }

        try {
            boolean deleted = programPersistPort.deleteByProgramId(programId);
            LOGGER.info("Deleted program {}: {}", programId, deleted);
            return deleted;
        } catch (ErpException ex) {
            LOGGER.error("Business error while deleting program {}: {}", programId, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while deleting program {}", programId, ex);
            throw ErpException.wrap(
                    ErrorCode.PROGRAM_DELETE_FAILED, ex, Map.of("programId", programId));
        }
    }

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
            throw ErpException.wrap(
                    ErrorCode.DEPARTMENT_CREATE_FAILED, ex, Map.of("deptId", deptId));
        }
    }

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
            throw ErpException.wrap(
                    ErrorCode.DEPARTMENT_UPDATE_FAILED, ex, Map.of("deptId", deptId));
        }
    }

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
            throw ErpException.wrap(
                    ErrorCode.DEPARTMENT_DELETE_FAILED, ex, Map.of("deptId", deptId));
        }
    }

    public CourseDomain createCourse(CourseDomain courseDomain) {
        String courseId = courseDomain.getCourseId();
        LOGGER.info("Creating course {}", courseId);

        if (coursePersistPort.findByCourseId(courseId) != null) {
            throw ErpException.of(ErrorCode.COURSE_DUPLICATE, Map.of("courseId", courseId));
        }

        ensureDepartmentExists(courseDomain.getDeptId());

        try {
            CourseDomain saved = coursePersistPort.save(courseDomain);
            LOGGER.info("Created course {}", courseId);
            return saved;
        } catch (ErpException ex) {
            LOGGER.error("Business error while creating course {}: {}", courseId, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while creating course {}", courseId, ex);
            throw ErpException.wrap(
                    ErrorCode.COURSE_CREATE_FAILED, ex, Map.of("courseId", courseId));
        }
    }

    public CourseDomain updateCourse(CourseDomain courseDomain) {
        String courseId = courseDomain.getCourseId();
        LOGGER.info("Updating course {}", courseId);

        if (coursePersistPort.findByCourseId(courseId) == null) {
            throw ErpException.of(ErrorCode.COURSE_NOT_FOUND, Map.of("courseId", courseId));
        }

        ensureDepartmentExists(courseDomain.getDeptId());

        try {
            CourseDomain updated = coursePersistPort.update(courseDomain);
            LOGGER.info("Updated course {}", courseId);
            return updated;
        } catch (ErpException ex) {
            LOGGER.error("Business error while updating course {}: {}", courseId, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while updating course {}", courseId, ex);
            throw ErpException.wrap(
                    ErrorCode.COURSE_UPDATE_FAILED, ex, Map.of("courseId", courseId));
        }
    }

    public boolean deleteCourse(String courseId) {
        LOGGER.info("Deleting course {}", courseId);

        if (coursePersistPort.findByCourseId(courseId) == null) {
            throw ErpException.of(ErrorCode.COURSE_NOT_FOUND, Map.of("courseId", courseId));
        }

        try {
            boolean deleted = coursePersistPort.deleteByCourseId(courseId);
            LOGGER.info("Deleted course {}: {}", courseId, deleted);
            return deleted;
        } catch (ErpException ex) {
            LOGGER.error("Business error while deleting course {}: {}", courseId, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while deleting course {}", courseId, ex);
            throw ErpException.wrap(
                    ErrorCode.COURSE_DELETE_FAILED, ex, Map.of("courseId", courseId));
        }
    }

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

    public List<ProgramDomain> listPrograms() {
        LOGGER.info("Listing all programs");
        try {
            return programPersistPort.findAll();
        } catch (ErpException ex) {
            LOGGER.error("Business error while listing programs: {}", ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while listing programs", ex);
            throw ErpException.wrap(ErrorCode.PROGRAM_FETCH_FAILED, ex, Map.of());
        }
    }

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

    public List<CourseDomain> listCourses() {
        LOGGER.info("Listing all courses");
        try {
            return coursePersistPort.findAll();
        } catch (ErpException ex) {
            LOGGER.error("Business error while listing courses: {}", ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while listing courses", ex);
            throw ErpException.wrap(ErrorCode.COURSE_FETCH_FAILED, ex, Map.of());
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

    private void ensureDepartmentExists(String deptId) {
        if (deptId == null || deptId.isBlank()) {
            throw ErpException.of(ErrorCode.DEPARTMENT_NOT_FOUND, Map.of("deptId", deptId));
        }
        if (departmentPersistPort.findByDeptId(deptId) == null) {
            throw ErpException.of(ErrorCode.DEPARTMENT_NOT_FOUND, Map.of("deptId", deptId));
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
