package com.iiitd.erp.io.controller;

import com.iiitd.erp.Domain.*;
import com.iiitd.erp.core.service.*;
import com.iiitd.erp.core.service.dto.SectionScheduleResult;
import com.iiitd.erp.io.controller.support.Paging;
import com.iiitd.erp.persistent.entity.erp_db.AuditEntry;
import com.iiitd.erp.persistent.persistence.repository.AuditRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.iiitd.erp.io.dto.request.*;
import com.iiitd.erp.io.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static com.iiitd.erp.mappers.DomainToResponseMapper.DOMAIN_TO_RESPONSE_MAPPER_INSTANCE;
import static com.iiitd.erp.mappers.RequestToDomainMapper.REQUEST_TO_DOMAIN_MAPPER_INSTANCE;

/**
 * Admin back-office. Access is restricted to the ADMIN role in SecurityConfig, and errors are
 * turned into responses by GlobalApiExceptionHandler, so handlers only contain the happy path.
 * List endpoints accept q / sort / page / size (see {@link ListParams}).
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin")
public class AdminController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);

    private final StudentService studentService;
    private final InstructorService instructorService;
    private final ProgramService programService;
    private final DepartmentService departmentService;
    private final CourseService courseService;
    private final RoomService roomService;
    private final MaintenanceService maintenanceService;
    private final SectionService sectionService;
    private final AvailabilityService availabilityService;
    private final SectionScheduleService sectionScheduleService;
    private final TimetableService timetableService;
    private final AccountService accountService;
    private final DashboardService dashboardService;
    private final Paging paging;
    private final PrerequisiteService prerequisiteService;
    private final AnalyticsService analyticsService;
    private final AuditRepository auditRepository;

    public AdminController(StudentService studentService,
                           InstructorService instructorService,
                           ProgramService programService,
                           DepartmentService departmentService,
                           CourseService courseService,
                           RoomService roomService,
                           MaintenanceService maintenanceService,
                           SectionService sectionService,
                           AvailabilityService availabilityService,
                           SectionScheduleService sectionScheduleService,
                           TimetableService timetableService,
                           AccountService accountService,
                           DashboardService dashboardService,
                           Paging paging,
                           PrerequisiteService prerequisiteService,
                           AnalyticsService analyticsService,
                           AuditRepository auditRepository) {
        this.prerequisiteService = prerequisiteService;
        this.analyticsService = analyticsService;
        this.auditRepository = auditRepository;
        this.studentService = studentService;
        this.instructorService = instructorService;
        this.programService = programService;
        this.departmentService = departmentService;
        this.courseService = courseService;
        this.roomService = roomService;
        this.maintenanceService = maintenanceService;
        this.sectionService = sectionService;
        this.availabilityService = availabilityService;
        this.sectionScheduleService = sectionScheduleService;
        this.timetableService = timetableService;
        this.accountService = accountService;
        this.dashboardService = dashboardService;
        this.paging = paging;
    }

    // ----- Dashboard -----

    @GetMapping("/dashboard")
    @Operation(summary = "Counts for the admin home page")
    public DashboardView dashboard() {
        return dashboardService.adminDashboard();
    }

    @GetMapping("/analytics")
    @Operation(summary = "Section fill, enrollments by department, grade distribution, daily registrations")
    public AnalyticsView analytics(@RequestParam(required = false) String semester,
                                   @RequestParam(required = false) Integer year) {
        return analyticsService.adminAnalytics(semester, year);
    }

    @GetMapping("/audit")
    @Operation(summary = "Audit log of every change and login attempt, newest first",
            description = "q matches actor or path. Paged in the database.")
    public PageResponse<AuditEntryResponse> audit(@RequestParam(required = false) String q,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "25") int size) {
        Page<AuditEntry> rows = auditRepository.search(q == null ? "" : q.trim(),
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), ListParams.MAX_SIZE)));
        return new PageResponse<>(rows.map(a -> new AuditEntryResponse(a.getAuditId(), a.getAt(), a.getActor(), a.getRole(),
                        a.getMethod(), a.getPath(), a.getStatus(), a.getIp())).getContent(),
                rows.getNumber(), rows.getSize(), rows.getTotalElements(), rows.getTotalPages());
    }

    // ----- Login accounts -----

    @GetMapping("/users")
    @Operation(summary = "All login accounts with lock / password-change state")
    public PageResponse<AccountResponse> listAccounts(@ParameterObject ListParams params) {
        OffsetDateTime now = OffsetDateTime.now();
        return paging.page(accountService.listAccounts().stream()
                .map(u -> new AccountResponse(u.getUserId(), u.getUsername(), u.getRole(), u.getStatus(),
                        u.getLastLogin(), u.getLockedUntil() != null && u.getLockedUntil().isAfter(now),
                        u.getLockedUntil(), u.isMustChangePassword()))
                .toList(), params);
    }

    @PostMapping("/users/{username}/unlock")
    @Operation(summary = "Unlock an account locked by failed logins")
    public Map<String, Object> unlock(@PathVariable String username) {
        accountService.unlock(username);
        return Map.of("username", username, "unlocked", true);
    }

    @PostMapping("/users/{username}/reset-password")
    @Operation(summary = "Issue a new temporary password", description = "Shown once; the user must change it at next login.")
    public PasswordResetResponse resetPassword(@PathVariable String username) {
        return new PasswordResetResponse(username, accountService.resetPassword(username));
    }

    // ----- Students -----

    @GetMapping("/students")
    public PageResponse<StudentResponse> listStudents(@ParameterObject ListParams params) {
        return paging.page(studentService.listStudents().stream()
                .map(DOMAIN_TO_RESPONSE_MAPPER_INSTANCE::studentDomainToResponse)
                .toList(), params);
    }

    @PostMapping("/create/student")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a student login + profile", description = "The response contains a one-time temporaryPassword.")
    public CreateUserResponse createStudentUser(@RequestBody StudentCreateRequest request) {
        CreateUserDomain created = studentService.createStudentUser(
                REQUEST_TO_DOMAIN_MAPPER_INSTANCE.studentUserRequestToDomain(request));
        LOGGER.info("Created student user {} for roll {}", created.getUsername(), request.rollNo());
        return DOMAIN_TO_RESPONSE_MAPPER_INSTANCE.domainToCreateUserResponse(created);
    }

    @PutMapping("/student/{rollNo}")
    public StudentResponse updateStudentUser(@PathVariable String rollNo, @RequestBody StudentUpdateRequest request) {
        StudentUserDomain domain = REQUEST_TO_DOMAIN_MAPPER_INSTANCE.studentUpdateRequestToDomain(request).withRollNo(rollNo);
        return DOMAIN_TO_RESPONSE_MAPPER_INSTANCE.studentDomainToResponse(studentService.updateStudentUser(domain));
    }

    @DeleteMapping("/student/{rollNo}")
    public Map<String, Object> deleteStudentUser(@PathVariable String rollNo) {
        return Map.of("deleted", studentService.deleteStudentUser(rollNo), "rollNo", rollNo);
    }

    // ----- Instructors -----

    @GetMapping("/instructors")
    public PageResponse<InstructorResponse> listInstructors(@ParameterObject ListParams params) {
        return paging.page(instructorService.listInstructors().stream()
                .map(DOMAIN_TO_RESPONSE_MAPPER_INSTANCE::instructorDomainToResponse)
                .toList(), params);
    }

    @PostMapping("/create/instructor")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an instructor login + profile", description = "The response contains a one-time temporaryPassword.")
    public CreateUserResponse createInstructor(@RequestBody InstructorCreateRequest request) {
        CreateUserDomain created = instructorService.createInstructor(
                REQUEST_TO_DOMAIN_MAPPER_INSTANCE.instructorCreateRequestToDomain(request));
        LOGGER.info("Created instructor {} with username {}", request.instructorCode(), created.getUsername());
        return DOMAIN_TO_RESPONSE_MAPPER_INSTANCE.domainToCreateUserResponse(created);
    }

    @PutMapping("/instructor/{instructorCode}")
    public InstructorResponse updateInstructor(@PathVariable String instructorCode,
                                               @RequestBody InstructorUpdateRequest request) {
        InstructorDomain domain = REQUEST_TO_DOMAIN_MAPPER_INSTANCE.instructorUpdateRequestToDomain(request)
                .withInstructorCode(instructorCode);
        return DOMAIN_TO_RESPONSE_MAPPER_INSTANCE.instructorDomainToResponse(instructorService.updateInstructor(domain));
    }

    @DeleteMapping("/instructor/{instructorCode}")
    public Map<String, Object> deleteInstructor(@PathVariable String instructorCode) {
        return Map.of("deleted", instructorService.deleteInstructor(instructorCode), "instructorCode", instructorCode);
    }

    // ----- Programs -----

    @GetMapping("/programs")
    public PageResponse<ProgramResponse> listPrograms(@ParameterObject ListParams params) {
        return paging.page(programService.listPrograms().stream()
                .map(DOMAIN_TO_RESPONSE_MAPPER_INSTANCE::programDomainToResponse)
                .toList(), params);
    }

    @PostMapping("/programs")
    @ResponseStatus(HttpStatus.CREATED)
    public ProgramResponse createProgram(@RequestBody ProgramCreateRequest request) {
        ProgramDomain domain = REQUEST_TO_DOMAIN_MAPPER_INSTANCE.programCreateRequestToDomain(request);
        return DOMAIN_TO_RESPONSE_MAPPER_INSTANCE.programDomainToResponse(programService.createProgram(domain));
    }

    @PutMapping("/programs/{programId}")
    public ProgramResponse updateProgram(@PathVariable String programId, @RequestBody ProgramUpdateRequest request) {
        ProgramDomain domain = REQUEST_TO_DOMAIN_MAPPER_INSTANCE.programUpdateRequestToDomain(request).withProgramId(programId);
        return DOMAIN_TO_RESPONSE_MAPPER_INSTANCE.programDomainToResponse(programService.updateProgram(domain));
    }

    @DeleteMapping("/programs/{programId}")
    public Map<String, Object> deleteProgram(@PathVariable String programId) {
        return Map.of("deleted", programService.deleteProgram(programId), "programId", programId);
    }

    // ----- Departments -----

    @GetMapping("/departments")
    public PageResponse<DepartmentResponse> listDepartments(@ParameterObject ListParams params) {
        return paging.page(departmentService.listDepartments().stream()
                .map(DOMAIN_TO_RESPONSE_MAPPER_INSTANCE::departmentDomainToResponse)
                .toList(), params);
    }

    @PostMapping("/departments")
    @ResponseStatus(HttpStatus.CREATED)
    public DepartmentResponse createDepartment(@RequestBody DepartmentCreateRequest request) {
        DepartmentDomain domain = REQUEST_TO_DOMAIN_MAPPER_INSTANCE.departmentCreateRequestToDomain(request);
        return DOMAIN_TO_RESPONSE_MAPPER_INSTANCE.departmentDomainToResponse(departmentService.createDepartment(domain));
    }

    @PutMapping("/departments/{deptId}")
    public DepartmentResponse updateDepartment(@PathVariable String deptId, @RequestBody DepartmentUpdateRequest request) {
        DepartmentDomain domain = REQUEST_TO_DOMAIN_MAPPER_INSTANCE.departmentUpdateRequestToDomain(request).withDeptId(deptId);
        return DOMAIN_TO_RESPONSE_MAPPER_INSTANCE.departmentDomainToResponse(departmentService.updateDepartment(domain));
    }

    @DeleteMapping("/departments/{deptId}")
    public Map<String, Object> deleteDepartment(@PathVariable String deptId) {
        return Map.of("deleted", departmentService.deleteDepartment(deptId), "deptId", deptId);
    }

    // ----- Courses -----

    @GetMapping("/courses")
    public PageResponse<CourseResponse> listCourses(@ParameterObject ListParams params) {
        return paging.page(courseService.listCourses().stream()
                .map(DOMAIN_TO_RESPONSE_MAPPER_INSTANCE::courseDomainToResponse)
                .toList(), params);
    }

    @PostMapping("/courses")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse createCourse(@RequestBody CourseCreateRequest request) {
        CourseDomain domain = REQUEST_TO_DOMAIN_MAPPER_INSTANCE.courseCreateRequestToDomain(request);
        return DOMAIN_TO_RESPONSE_MAPPER_INSTANCE.courseDomainToResponse(courseService.createCourse(domain));
    }

    @PutMapping("/courses/{courseId}")
    public CourseResponse updateCourse(@PathVariable String courseId, @RequestBody CourseUpdateRequest request) {
        CourseDomain domain = REQUEST_TO_DOMAIN_MAPPER_INSTANCE.courseUpdateRequestToDomain(request).withCourseId(courseId);
        return DOMAIN_TO_RESPONSE_MAPPER_INSTANCE.courseDomainToResponse(courseService.updateCourse(domain));
    }

    @DeleteMapping("/courses/{courseId}")
    public Map<String, Object> deleteCourse(@PathVariable String courseId) {
        return Map.of("deleted", courseService.deleteCourse(courseId), "courseId", courseId);
    }

    @GetMapping("/prerequisites")
    @Operation(summary = "Every course's prerequisites (courseId -> required course ids)")
    public Map<String, List<String>> allPrerequisites() {
        return prerequisiteService.all();
    }

    @GetMapping("/courses/{courseId}/prerequisites")
    public List<String> prerequisites(@PathVariable String courseId) {
        return prerequisiteService.prerequisitesOf(courseId);
    }

    @PutMapping("/courses/{courseId}/prerequisites")
    @Operation(summary = "Replace a course's prerequisites", description = "Rejects unknown courses and circular requirements.")
    public List<String> replacePrerequisites(@PathVariable String courseId, @RequestBody @Valid PrerequisitesRequest request) {
        return prerequisiteService.replace(courseId, request.prerequisites());
    }

    // ----- Rooms -----

    @GetMapping("/rooms")
    public PageResponse<RoomResponse> listRooms(@ParameterObject ListParams params) {
        return paging.page(roomService.listRooms().stream()
                .map(DOMAIN_TO_RESPONSE_MAPPER_INSTANCE::roomDomainToResponse)
                .toList(), params);
    }

    @PostMapping("/rooms")
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse createRoom(@RequestBody RoomCreateRequest request) {
        RoomDomain domain = REQUEST_TO_DOMAIN_MAPPER_INSTANCE.roomCreateRequestToDomain(request);
        return DOMAIN_TO_RESPONSE_MAPPER_INSTANCE.roomDomainToResponse(roomService.createRoom(domain));
    }

    @PutMapping("/rooms/{roomId}")
    public RoomResponse updateRoom(@PathVariable String roomId, @RequestBody RoomUpdateRequest request) {
        RoomDomain domain = REQUEST_TO_DOMAIN_MAPPER_INSTANCE.roomUpdateRequestToDomain(request).withRoomId(roomId);
        return DOMAIN_TO_RESPONSE_MAPPER_INSTANCE.roomDomainToResponse(roomService.updateRoom(domain));
    }

    @DeleteMapping("/rooms/{roomId}")
    public Map<String, Object> deleteRoom(@PathVariable String roomId) {
        return Map.of("deleted", roomService.deleteRoom(roomId), "roomId", roomId);
    }

    @GetMapping("/rooms/available")
    @Operation(summary = "Rooms free for a slot with at least minCapacity seats")
    public List<RoomAvailabilityResponse> availableRooms(@RequestParam DayOfWeek day,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime start,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime end,
                                 @RequestParam int minCapacity) {
        return availabilityService.availableRooms(day, start, end, minCapacity);
    }

    // ----- Maintenance -----

    @GetMapping("/settings/maintenance")
    public MaintenanceStatusResponse maintenanceStatus() {
        MaintenanceState state = maintenanceService.status();
        return new MaintenanceStatusResponse(state.maintenanceOn(), state.bannerMessage());
    }

    @PutMapping("/settings/maintenance")
    @Operation(summary = "Turn maintenance (read-only) mode on or off")
    public MaintenanceStatusResponse toggleMaintenance(@RequestBody @Valid MaintenanceToggleRequest request) {
        MaintenanceState state = maintenanceService.updateMaintenanceMode(request.maintenanceOn());
        LOGGER.info("Maintenance mode set to {}", state.maintenanceOn());
        return new MaintenanceStatusResponse(state.maintenanceOn(), state.bannerMessage());
    }

    // ----- Sections -----

    @GetMapping("/sections")
    public PageResponse<SectionResponse> listAllSections(@RequestParam(required = false) String semester,
                                                         @RequestParam(required = false) Integer year,
                                                         @ParameterObject ListParams params) {
        return paging.page(sectionService.listAllSections(semester, year).stream()
                .map(DOMAIN_TO_RESPONSE_MAPPER_INSTANCE::sectionDomainToResponse)
                .toList(), params);
    }

    @GetMapping("/courses/{courseId}/sections")
    public List<SectionResponse> listSections(@PathVariable String courseId,
                                              @RequestParam(required = false) String semester,
                                              @RequestParam(required = false) Integer year) {
        return sectionService.listSections(courseId, semester, year).stream()
                .map(DOMAIN_TO_RESPONSE_MAPPER_INSTANCE::sectionDomainToResponse)
                .toList();
    }

    @PostMapping("/courses/{courseId}/sections")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a section", description = "The id is generated as COURSE-INSTRUCTOR-YY-TERM, e.g. CSE101-INS001-26-1.")
    public SectionResponse createSection(@PathVariable String courseId, @RequestBody @Valid SectionCreateRequest request) {
        SectionDomain created = sectionService.createSection(courseId,
                REQUEST_TO_DOMAIN_MAPPER_INSTANCE.sectionCreateRequestToDomain(request));
        return DOMAIN_TO_RESPONSE_MAPPER_INSTANCE.sectionDomainToResponse(created);
    }

    @PutMapping("/sections/{sectionId}")
    public SectionResponse updateSection(@PathVariable String sectionId, @RequestBody @Valid SectionUpdateRequest request) {
        SectionDomain updated = sectionService.updateSection(sectionId,
                REQUEST_TO_DOMAIN_MAPPER_INSTANCE.sectionUpdateRequestToDomain(request));
        return DOMAIN_TO_RESPONSE_MAPPER_INSTANCE.sectionDomainToResponse(updated);
    }

    @DeleteMapping("/sections/{sectionId}")
    @Operation(summary = "Deactivate a section", description = "Soft delete: enrollments and grades are kept.")
    public Map<String, Object> deleteSection(@PathVariable String sectionId) {
        return Map.of("sectionId", sectionId, "deleted", sectionService.deleteSection(sectionId));
    }

    // ----- Scheduling -----

    @GetMapping("/instructors/{code}/availability")
    public InstructorAvailabilityResponse instructorAvailability(@PathVariable String code,
                                         @RequestParam String semester,
                                         @RequestParam int year) {
        return availabilityService.instructorAvailability(code, semester, year);
    }

    @GetMapping("/sections/{sectionId}/schedule")
    public SectionScheduleResponse viewSchedule(@PathVariable String sectionId) {
        return toScheduleResponse(sectionScheduleService.view(sectionId));
    }

    @PutMapping("/sections/{sectionId}/schedule")
    @Operation(summary = "Replace a section's weekly slots",
            description = "409 if the room or instructor is already busy; 400 if outside working hours or the room is too small.")
    public SectionScheduleResponse upsertSchedule(@PathVariable String sectionId,
                                                  @RequestBody @Valid SectionScheduleRequest request) {
        List<ClassScheduleDomain> slots = request.slots().stream()
                .map(REQUEST_TO_DOMAIN_MAPPER_INSTANCE::scheduleSlotRequestToDomain)
                .map(domain -> domain.withSectionId(sectionId))
                .toList();
        return toScheduleResponse(sectionScheduleService.replace(sectionId, slots));
    }

    @DeleteMapping("/sections/{sectionId}/schedule")
    public Map<String, Object> deleteSchedule(@PathVariable String sectionId) {
        sectionScheduleService.clear(sectionId);
        return Map.of("sectionId", sectionId, "deleted", true);
    }

    @GetMapping("/timetable")
    @Operation(summary = "Search the master timetable; all filters optional")
    public List<TimetableSearchSlotResponse> searchTimetable(@RequestParam(required = false) String instructorCode,
                                                             @RequestParam(required = false) String courseId,
                                                             @RequestParam(required = false) String sectionId,
                                                             @RequestParam(required = false) String roomId,
                                                             @RequestParam(required = false) String semester,
                                                             @RequestParam(required = false) Integer year,
                                                             @RequestParam(required = false) DayOfWeek day) {
        return timetableService.search(instructorCode, courseId, sectionId, roomId, semester, year, day).stream()
                .map(DOMAIN_TO_RESPONSE_MAPPER_INSTANCE::timetableSearchSlotDomainToResponse)
                .toList();
    }

    private SectionScheduleResponse toScheduleResponse(SectionScheduleResult result) {
        return SectionScheduleResponse.builder()
                .sectionId(result.section().getSectionId())
                .courseId(result.section().getCourseId())
                .slots(result.slots().stream()
                        .map(DOMAIN_TO_RESPONSE_MAPPER_INSTANCE::classScheduleDomainToScheduleSlotResponse)
                        .toList())
                .build();
    }
}
