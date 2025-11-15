package com.iiitd.erp.io.controller;

import com.iiitd.erp.Domain.*;
import com.iiitd.erp.core.service.*;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.io.dto.request.*;
import com.iiitd.erp.io.dto.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.iiitd.erp.io.controller.support.ApiResponseSupport.error;
import static com.iiitd.erp.io.controller.support.ApiResponseSupport.from;
import static com.iiitd.erp.mappers.DomainToResponseMapper.DOMAIN_TO_RESPONSE_MAPPER_INSTANCE;
import static com.iiitd.erp.mappers.RequestToDomainMapper.REQUEST_TO_DOMAIN_MAPPER_INSTANCE;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);

    private final StudentService studentService;
    private final InstructorService instructorService;
    private final ProgramService programService;
    private final DepartmentService departmentService;
    private final CourseService courseService;
    private final RoomService roomService;

    public AdminController(StudentService studentService,
                           InstructorService instructorService,
                           ProgramService programService,
                           DepartmentService departmentService,
                           CourseService courseService,
                           RoomService roomService) {
        this.studentService = studentService;
        this.instructorService = instructorService;
        this.programService = programService;
        this.departmentService = departmentService;
        this.courseService = courseService;
        this.roomService = roomService;
    }

    // ----- Student listing + CRUD -----
    @GetMapping("/students")
    public ResponseEntity<?> listStudents() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            LOGGER.warn("Unauthorized attempt to list students by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.ADMIN_FORBIDDEN);
        }
        try {
            List<StudentResponse> response = studentService.listStudents().stream()
                    .map(DOMAIN_TO_RESPONSE_MAPPER_INSTANCE::studentDomainToResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (ErpException ex) {
            LOGGER.error("Business error while listing students: {}", ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while listing students", ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.STUDENT_FETCH_FAILED);
        }
    }

    @PostMapping("/create/student")
    public ResponseEntity<?> createStudentUser(@RequestBody StudentCreateRequest studentCreateRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            LOGGER.warn("Unauthorized attempt to create student by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.ADMIN_FORBIDDEN);
        }

        Integer rollNo = studentCreateRequest.rollNo();
        try {
            CreateUserDomain created = studentService.createStudentUser(
                    REQUEST_TO_DOMAIN_MAPPER_INSTANCE.studentUserRequestToDomain(studentCreateRequest)
            );
            CreateUserResponse response = DOMAIN_TO_RESPONSE_MAPPER_INSTANCE.domainToCreateUserResponse(created);
            LOGGER.info("Admin {} created student user for roll {}", auth.getName(), rollNo);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ErpException ex) {
            LOGGER.error("Business error while creating student {}: {}", rollNo, ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while creating student {}", rollNo, ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.USER_CREATION_FAILED);
        }
    }

    @PutMapping("/student/{rollNo}")
    public ResponseEntity<?> updateStudentUser(@PathVariable Integer rollNo,
                                               @RequestBody StudentUpdateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            LOGGER.warn("Unauthorized attempt to update student by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.ADMIN_FORBIDDEN);
        }

        StudentUserDomain domain = REQUEST_TO_DOMAIN_MAPPER_INSTANCE.studentUpdateRequestToDomain(request)
                .withRollNo(rollNo);

        try {
            StudentUserDomain updated = studentService.updateStudentUser(domain);
            StudentResponse response = DOMAIN_TO_RESPONSE_MAPPER_INSTANCE.studentDomainToResponse(updated);
            return ResponseEntity.ok(response);
        } catch (ErpException ex) {
            LOGGER.error("Business error while updating student {}: {}", rollNo, ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while updating student {}", rollNo, ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.STUDENT_UPDATE_FAILED);
        }
    }

    @DeleteMapping("/student/{rollNo}")
    public ResponseEntity<?> deleteStudentUser(@PathVariable Integer rollNo) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            LOGGER.warn("Unauthorized attempt to delete student by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.ADMIN_FORBIDDEN);
        }

        try {
            boolean deleted = studentService.deleteStudentUser(rollNo);
            return ResponseEntity.ok(Map.of("deleted", deleted, "rollNo", rollNo));
        } catch (ErpException ex) {
            LOGGER.error("Business error while deleting student {}: {}", rollNo, ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while deleting student {}", rollNo, ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.STUDENT_DELETE_FAILED);
        }
    }

    // ----- Instructor listing + CRUD -----
    @GetMapping("/instructors")
    public ResponseEntity<?> listInstructors() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            LOGGER.warn("Unauthorized attempt to list instructors by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.ADMIN_FORBIDDEN);
        }

        try {
            List<InstructorResponse> response = instructorService.listInstructors().stream()
                    .map(DOMAIN_TO_RESPONSE_MAPPER_INSTANCE::instructorDomainToResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (ErpException ex) {
            LOGGER.error("Business error while listing instructors: {}", ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while listing instructors", ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INSTRUCTOR_FETCH_FAILED);
        }
    }

    @PostMapping("/create/instructor")
    public ResponseEntity<?> createInstructor(@RequestBody InstructorCreateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            LOGGER.warn("Unauthorized attempt to create instructor by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.ADMIN_FORBIDDEN);
        }

        InstructorDomain domain = REQUEST_TO_DOMAIN_MAPPER_INSTANCE.instructorCreateRequestToDomain(request);

        try {
            CreateUserDomain created = instructorService.createInstructor(domain);
            CreateUserResponse response = DOMAIN_TO_RESPONSE_MAPPER_INSTANCE.domainToCreateUserResponse(created);
            LOGGER.info("Admin {} created instructor {}", auth.getName(), request.instructorCode());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ErpException ex) {
            LOGGER.error("Business error while creating instructor {}: {}", request.instructorCode(), ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while creating instructor {}", request.instructorCode(), ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INSTRUCTOR_CREATE_FAILED);
        }
    }

    @PutMapping("/instructor/{instructorCode}")
    public ResponseEntity<?> updateInstructor(@PathVariable String instructorCode,
                                              @RequestBody InstructorUpdateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            LOGGER.warn("Unauthorized attempt to update instructor by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.ADMIN_FORBIDDEN);
        }

        InstructorDomain domain = REQUEST_TO_DOMAIN_MAPPER_INSTANCE.instructorUpdateRequestToDomain(request)
                .withInstructorCode(instructorCode);

        try {
            InstructorResponse response = DOMAIN_TO_RESPONSE_MAPPER_INSTANCE
                    .instructorDomainToResponse(instructorService.updateInstructor(domain));
            return ResponseEntity.ok(response);
        } catch (ErpException ex) {
            LOGGER.error("Business error while updating instructor {}: {}", instructorCode, ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while updating instructor {}", instructorCode, ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INSTRUCTOR_UPDATE_FAILED);
        }
    }

    @DeleteMapping("/instructor/{instructorCode}")
    public ResponseEntity<?> deleteInstructor(@PathVariable String instructorCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            LOGGER.warn("Unauthorized attempt to delete instructor by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.ADMIN_FORBIDDEN);
        }

        try {
            boolean deleted = instructorService.deleteInstructor(instructorCode);
            return ResponseEntity.ok(Map.of("deleted", deleted, "instructorCode", instructorCode));
        } catch (ErpException ex) {
            LOGGER.error("Business error while deleting instructor {}: {}", instructorCode, ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while deleting instructor {}", instructorCode, ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INSTRUCTOR_DELETE_FAILED);
        }
    }

    // ----- Program listing + CRUD -----
    @GetMapping("/programs")
    public ResponseEntity<?> listPrograms() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            LOGGER.warn("Unauthorized attempt to list programs by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.ADMIN_FORBIDDEN);
        }

        try {
            List<ProgramResponse> response = programService.listPrograms().stream()
                    .map(DOMAIN_TO_RESPONSE_MAPPER_INSTANCE::programDomainToResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (ErpException ex) {
            LOGGER.error("Business error while listing programs: {}", ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while listing programs", ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.PROGRAM_FETCH_FAILED);
        }
    }

    @PostMapping("/programs")
    public ResponseEntity<?> createProgram(@RequestBody ProgramCreateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            LOGGER.warn("Unauthorized attempt to create program by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.ADMIN_FORBIDDEN);
        }

        ProgramDomain domain = REQUEST_TO_DOMAIN_MAPPER_INSTANCE.programCreateRequestToDomain(request);

        try {
            ProgramResponse response = DOMAIN_TO_RESPONSE_MAPPER_INSTANCE
                    .programDomainToResponse(programService.createProgram(domain));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ErpException ex) {
            LOGGER.error("Business error while creating program {}: {}", request.programId(), ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while creating program {}", request.programId(), ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.PROGRAM_CREATE_FAILED);
        }
    }

    @PutMapping("/programs/{programId}")
    public ResponseEntity<?> updateProgram(@PathVariable String programId,
                                           @RequestBody ProgramUpdateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            LOGGER.warn("Unauthorized attempt to update program by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.ADMIN_FORBIDDEN);
        }

        ProgramDomain domain = REQUEST_TO_DOMAIN_MAPPER_INSTANCE.programUpdateRequestToDomain(request)
                .withProgramId(programId);

        try {
            ProgramResponse response = DOMAIN_TO_RESPONSE_MAPPER_INSTANCE
                    .programDomainToResponse(programService.updateProgram(domain));
            return ResponseEntity.ok(response);
        } catch (ErpException ex) {
            LOGGER.error("Business error while updating program {}: {}", programId, ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while updating program {}", programId, ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.PROGRAM_UPDATE_FAILED);
        }
    }

    @DeleteMapping("/programs/{programId}")
    public ResponseEntity<?> deleteProgram(@PathVariable String programId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            LOGGER.warn("Unauthorized attempt to delete program by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.ADMIN_FORBIDDEN);
        }

        try {
            boolean deleted = programService.deleteProgram(programId);
            return ResponseEntity.ok(Map.of("deleted", deleted, "programId", programId));
        } catch (ErpException ex) {
            LOGGER.error("Business error while deleting program {}: {}", programId, ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while deleting program {}", programId, ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.PROGRAM_DELETE_FAILED);
        }
    }

    // ----- Department listing + CRUD -----
    @GetMapping("/departments")
    public ResponseEntity<?> listDepartments() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            LOGGER.warn("Unauthorized attempt to list departments by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.ADMIN_FORBIDDEN);
        }

        try {
            List<DepartmentResponse> response = departmentService.listDepartments().stream()
                    .map(DOMAIN_TO_RESPONSE_MAPPER_INSTANCE::departmentDomainToResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (ErpException ex) {
            LOGGER.error("Business error while listing departments: {}", ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while listing departments", ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.DEPARTMENT_FETCH_FAILED);
        }
    }

    @PostMapping("/departments")
    public ResponseEntity<?> createDepartment(@RequestBody DepartmentCreateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            LOGGER.warn("Unauthorized attempt to create department by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.ADMIN_FORBIDDEN);
        }

        DepartmentDomain domain = REQUEST_TO_DOMAIN_MAPPER_INSTANCE.departmentCreateRequestToDomain(request);

        try {
            DepartmentResponse response = DOMAIN_TO_RESPONSE_MAPPER_INSTANCE
                    .departmentDomainToResponse(departmentService.createDepartment(domain));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ErpException ex) {
            LOGGER.error("Business error while creating department {}: {}", request.deptId(), ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while creating department {}", request.deptId(), ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.DEPARTMENT_CREATE_FAILED);
        }
    }

    @PutMapping("/departments/{deptId}")
    public ResponseEntity<?> updateDepartment(@PathVariable String deptId,
                                              @RequestBody DepartmentUpdateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            LOGGER.warn("Unauthorized attempt to update department by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.ADMIN_FORBIDDEN);
        }

        DepartmentDomain domain = REQUEST_TO_DOMAIN_MAPPER_INSTANCE.departmentUpdateRequestToDomain(request)
                .withDeptId(deptId);

        try {
            DepartmentResponse response = DOMAIN_TO_RESPONSE_MAPPER_INSTANCE
                    .departmentDomainToResponse(departmentService.updateDepartment(domain));
            return ResponseEntity.ok(response);
        } catch (ErpException ex) {
            LOGGER.error("Business error while updating department {}: {}", deptId, ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while updating department {}", deptId, ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.DEPARTMENT_UPDATE_FAILED);
        }
    }

    @DeleteMapping("/departments/{deptId}")
    public ResponseEntity<?> deleteDepartment(@PathVariable String deptId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            LOGGER.warn("Unauthorized attempt to delete department by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.ADMIN_FORBIDDEN);
        }

        try {
            boolean deleted = departmentService.deleteDepartment(deptId);
            return ResponseEntity.ok(Map.of("deleted", deleted, "deptId", deptId));
        } catch (ErpException ex) {
            LOGGER.error("Business error while deleting department {}: {}", deptId, ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while deleting department {}", deptId, ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.DEPARTMENT_DELETE_FAILED);
        }
    }

    // ----- Course listing + CRUD -----
    @GetMapping("/courses")
    public ResponseEntity<?> listCourses() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            LOGGER.warn("Unauthorized attempt to list courses by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.ADMIN_FORBIDDEN);
        }

        try {
            List<CourseResponse> response = courseService.listCourses().stream()
                    .map(DOMAIN_TO_RESPONSE_MAPPER_INSTANCE::courseDomainToResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (ErpException ex) {
            LOGGER.error("Business error while listing courses: {}", ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while listing courses", ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.COURSE_FETCH_FAILED);
        }
    }

    @PostMapping("/courses")
    public ResponseEntity<?> createCourse(@RequestBody CourseCreateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            LOGGER.warn("Unauthorized attempt to create course by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.ADMIN_FORBIDDEN);
        }

        CourseDomain domain = REQUEST_TO_DOMAIN_MAPPER_INSTANCE.courseCreateRequestToDomain(request);

        try {
            CourseResponse response = DOMAIN_TO_RESPONSE_MAPPER_INSTANCE
                    .courseDomainToResponse(courseService.createCourse(domain));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ErpException ex) {
            LOGGER.error("Business error while creating course {}: {}", request.courseId(), ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while creating course {}", request.courseId(), ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.COURSE_CREATE_FAILED);
        }
    }

    @PutMapping("/courses/{courseId}")
    public ResponseEntity<?> updateCourse(@PathVariable String courseId,
                                          @RequestBody CourseUpdateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            LOGGER.warn("Unauthorized attempt to update course by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.ADMIN_FORBIDDEN);
        }

        CourseDomain domain = REQUEST_TO_DOMAIN_MAPPER_INSTANCE.courseUpdateRequestToDomain(request)
                .withCourseId(courseId);

        try {
            CourseResponse response = DOMAIN_TO_RESPONSE_MAPPER_INSTANCE
                    .courseDomainToResponse(courseService.updateCourse(domain));
            return ResponseEntity.ok(response);
        } catch (ErpException ex) {
            LOGGER.error("Business error while updating course {}: {}", courseId, ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while updating course {}", courseId, ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.COURSE_UPDATE_FAILED);
        }
    }

    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<?> deleteCourse(@PathVariable String courseId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            LOGGER.warn("Unauthorized attempt to delete course by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.ADMIN_FORBIDDEN);
        }

        try {
            boolean deleted = courseService.deleteCourse(courseId);
            return ResponseEntity.ok(Map.of("deleted", deleted, "courseId", courseId));
        } catch (ErpException ex) {
            LOGGER.error("Business error while deleting course {}: {}", courseId, ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while deleting course {}", courseId, ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.COURSE_DELETE_FAILED);
        }
    }


    // ----- Room listing + CRUD -----
    @GetMapping("/rooms")
    public ResponseEntity<?> listRooms() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            LOGGER.warn("Unauthorized attempt to list rooms by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.ADMIN_FORBIDDEN);
        }
        try {
            List<RoomResponse> response = roomService.listRooms().stream()
                    .map(DOMAIN_TO_RESPONSE_MAPPER_INSTANCE::roomDomainToResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (ErpException ex) {
            LOGGER.error("Business error while listing rooms: {}", ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while listing rooms", ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.ROOM_FETCH_FAILED);
        }
    }

    @PostMapping("/rooms")
    public ResponseEntity<?> createRoom(@RequestBody RoomCreateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            LOGGER.warn("Unauthorized attempt to create room by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.ADMIN_FORBIDDEN);
        }

        RoomDomain domain = REQUEST_TO_DOMAIN_MAPPER_INSTANCE.roomCreateRequestToDomain(request);
        try {
            RoomResponse response = DOMAIN_TO_RESPONSE_MAPPER_INSTANCE
                    .roomDomainToResponse(roomService.createRoom(domain));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ErpException ex) {
            LOGGER.error("Business error while creating room {}: {}", request.roomNumber(), ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while creating room {}", request.roomNumber(), ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.ROOM_CREATE_FAILED);
        }
    }

    @PutMapping("/rooms/{roomId}")
    public ResponseEntity<?> updateRoom(@PathVariable String roomId,
                                        @RequestBody RoomUpdateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            LOGGER.warn("Unauthorized attempt to update room by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.ADMIN_FORBIDDEN);
        }

        RoomDomain domain = REQUEST_TO_DOMAIN_MAPPER_INSTANCE.roomUpdateRequestToDomain(request)
                .withRoomId(roomId);

        try {
            RoomResponse response = DOMAIN_TO_RESPONSE_MAPPER_INSTANCE
                    .roomDomainToResponse(roomService.updateRoom(domain));
            return ResponseEntity.ok(response);
        } catch (ErpException ex) {
            LOGGER.error("Business error while updating room {}: {}", roomId, ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while updating room {}", roomId, ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.ROOM_UPDATE_FAILED);
        }
    }

    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<?> deleteRoom(@PathVariable String roomId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            LOGGER.warn("Unauthorized attempt to delete room by {}", auth != null ? auth.getName() : "anonymous");
            return error(HttpStatus.FORBIDDEN, ErrorCode.ADMIN_FORBIDDEN);
        }

        try {
            boolean deleted = roomService.deleteRoom(roomId);
            return ResponseEntity.ok(Map.of("deleted", deleted, "roomId", roomId));
        } catch (ErpException ex) {
            LOGGER.error("Business error while deleting room {}: {}", roomId, ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while deleting room {}", roomId, ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.ROOM_DELETE_FAILED);
        }
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));
    }
}
