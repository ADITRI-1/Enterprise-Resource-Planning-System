package com.iiitd.erp.exception;

/**
 * Messages are shown to end users as-is, so they contain no format placeholders;
 * request-specific values travel separately in {@link ErpException#getContext()}.
 */
public enum ErrorCode {
    DUPLICATE_STUDENT("USR-001", "A student with this roll number already exists"),
    USER_CREATION_FAILED("USR-002", "Failed to create student account"),
    STUDENT_FETCH_FAILED("PER-001", "Unable to fetch student"),
    STUDENT_PERSIST_FAILED("PER-002", "Unable to save student"),
    USERAUTH_FETCH_FAILED("PER-003", "Unable to fetch user account"),
    USERAUTH_PERSIST_FAILED("PER-004", "Unable to save user account"),
    USERAUTH_LAST_LOGIN_UPDATE_FAILED("PER-005", "Unable to update last login"),
    ADMIN_FORBIDDEN("API-001", "You need admin privileges for this action"),
    AUTH_NOT_AUTHENTICATED("API-002", "Please log in to continue"),
    AUTHENTICATION_FAILED("API-003", "Incorrect username or password"),
    INSTRUCTOR_FORBIDDEN("API-004", "You can only access your own sections"),
    STUDENT_FORBIDDEN("API-005", "You can only access your own records"),
    CATALOG_LIST_FAILED("API-010", "Unable to fetch catalog data"),
    CATALOG_SECTIONS_FAILED("API-011", "Unable to fetch sections for this course"),
    STUDENT_REGISTRATION_BLOCKED("API-020", "Registration is blocked because maintenance mode is ON"),
    STUDENT_REGISTRATION_FAILED("API-021", "Unable to register for this section"),
    STUDENT_DROP_FAILED("API-022", "Unable to drop this section"),
    STUDENT_TIMETABLE_FAILED("API-023", "Unable to fetch timetable"),
    STUDENT_GRADES_FAILED("API-024", "Unable to fetch grades"),
    STUDENT_TRANSCRIPT_FAILED("API-025", "Unable to generate transcript"),
    SECTION_FULL("API-026", "Section full"),
    ALREADY_ENROLLED("API-027", "You are already registered in this section"),
    REGISTRATION_DEADLINE_PASSED("API-028", "The registration deadline for this section has passed"),
    DROP_DEADLINE_PASSED("API-029", "The drop deadline for this section has passed"),
    SECTION_INACTIVE("API-031", "This section is not open for registration"),
    REQUEST_VALIDATION_FAILED("API-030", "Invalid request payload"),
    UNEXPECTED_SERVER_ERROR("API-999", "Unexpected server error"),
    STUDENT_NOT_FOUND("PER-006", "Student not found"),
    STUDENT_UPDATE_FAILED("PER-007", "Unable to update student"),
    STUDENT_DELETE_FAILED("PER-008", "Unable to delete student"),
    INSTRUCTOR_DUPLICATE("INST-001", "An instructor with this code already exists"),
    INSTRUCTOR_CREATE_FAILED("INST-002", "Failed to create instructor"),
    INSTRUCTOR_NOT_FOUND("INST-003", "Instructor not found"),
    INSTRUCTOR_UPDATE_FAILED("INST-004", "Unable to update instructor"),
    INSTRUCTOR_DELETE_FAILED("INST-005", "Unable to delete instructor"),
    INSTRUCTOR_FETCH_FAILED("INST-006", "Unable to fetch instructor"),
    PROGRAM_DUPLICATE("PRG-001", "A program with this id already exists"),
    PROGRAM_NOT_FOUND("PRG-002", "Program not found"),
    PROGRAM_CREATE_FAILED("PRG-003", "Failed to create program"),
    PROGRAM_UPDATE_FAILED("PRG-004", "Unable to update program"),
    PROGRAM_DELETE_FAILED("PRG-005", "Unable to delete program"),
    PROGRAM_FETCH_FAILED("PRG-006", "Unable to fetch program"),
    DEPARTMENT_DUPLICATE("DEP-001", "A department with this id already exists"),
    DEPARTMENT_NOT_FOUND("DEP-002", "Department not found"),
    DEPARTMENT_CREATE_FAILED("DEP-003", "Failed to create department"),
    DEPARTMENT_UPDATE_FAILED("DEP-004", "Unable to update department"),
    DEPARTMENT_DELETE_FAILED("DEP-005", "Unable to delete department"),
    DEPARTMENT_FETCH_FAILED("DEP-006", "Unable to fetch department"),
    COURSE_DUPLICATE("CRS-001", "A course with this id already exists"),
    COURSE_NOT_FOUND("CRS-002", "Course not found"),
    COURSE_CREATE_FAILED("CRS-003", "Failed to create course"),
    COURSE_UPDATE_FAILED("CRS-004", "Unable to update course"),
    COURSE_DELETE_FAILED("CRS-005", "Unable to delete course"),
    COURSE_FETCH_FAILED("CRS-006", "Unable to fetch course"),
    ROOM_DUPLICATE("RM-001", "A room with this number already exists"),
    ROOM_NOT_FOUND("RM-002", "Room not found"),
    ROOM_CREATE_FAILED("RM-003", "Failed to create room"),
    ROOM_UPDATE_FAILED("RM-004", "Unable to update room"),
    ROOM_DELETE_FAILED("RM-005", "Unable to delete room"),
    ROOM_FETCH_FAILED("RM-006", "Unable to fetch room data"),
    MAINTENANCE_STATUS_FETCH_FAILED("API-090", "Unable to read maintenance status"),
    MAINTENANCE_UPDATE_FAILED("API-091", "Unable to update maintenance flag"),
    MAINTENANCE_WRITE_BLOCKED("API-092", "Maintenance mode is ON: you can view data but changes are blocked"),
    SECTION_DUPLICATE("SEC-001", "This section already exists"),
    SECTION_NOT_FOUND("SEC-002", "Section not found"),
    SECTION_CREATE_FAILED("SEC-003", "Failed to create section"),
    SECTION_UPDATE_FAILED("SEC-004", "Unable to update section"),
    SECTION_DELETE_FAILED("SEC-005", "Unable to delete section"),
    SECTION_LIST_FAILED("SEC-006", "Unable to list sections"),
    SECTION_FETCH_FAILED("SEC-007", "Unable to fetch section"),
    SECTION_INVALID_SEMESTER("SEC-009", "Semester must be Monsoon or Winter"),
    SECTION_ID_IMMUTABLE("SEC-008", "Course, instructor, year and semester of a section cannot be changed"),
    SCHEDULE_OUTSIDE_WORKING_HOURS("SCH-001", "Requested slot falls outside working hours"),
    SCHEDULE_ROOM_CONFLICT("SCH-002", "Room is already booked for that slot"),
    SCHEDULE_INSTRUCTOR_CONFLICT("SCH-003", "Instructor has another class in that slot"),
    SCHEDULE_ROOM_CAPACITY_MISMATCH("SCH-004", "Room capacity is smaller than the section capacity"),
    SCHEDULE_SLOT_INVALID("SCH-005", "Invalid schedule slot definition"),
    ASSESSMENT_CREATE_FAILED("ASM-001", "Failed to create assessment"),
    ASSESSMENT_UPDATE_FAILED("ASM-002", "Failed to update assessment"),
    ASSESSMENT_DELETE_FAILED("ASM-003", "Failed to delete assessment"),
    ASSESSMENT_NOT_FOUND("ASM-004", "Assessment component not found"),
    ASSESSMENT_WEIGHT_EXCEEDED("ASM-005", "Assessment weights exceed 100% for this section"),
    SECTION_NOT_OWNED_BY_INSTRUCTOR("ASM-006", "Not your section"),
    ASSESSMENT_WEIGHTS_INCOMPLETE("ASM-007", "Assessment weights must add up to 100% before computing grades"),
    ASSESSMENT_SCORE_INVALID("ASM-010", "Score must be between 0 and the component's maximum"),
    ASSESSMENT_SCORE_IMPORT_FAILED("ASM-011", "Unable to import assessment scores"),
    ASSESSMENT_SCORE_EXPORT_FAILED("ASM-012", "Unable to export assessment scores"),
    ENROLLMENT_NOT_FOUND("ASM-013", "No enrolled students found"),
    STUDENT_NOT_IN_SECTION("ASM-014", "Student is not enrolled in this section");


    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    /** Kept for existing callers; messages have no placeholders so arguments are ignored. */
    public String format(Object... args) {
        return defaultMessage;
    }
}
