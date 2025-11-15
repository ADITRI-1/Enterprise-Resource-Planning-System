package com.iiitd.erp.exception;

public enum ErrorCode {
    DUPLICATE_STUDENT("USR-001", "Student with roll %s already exists"),
    USER_CREATION_FAILED("USR-002", "Failed to create student account"),
    STUDENT_FETCH_FAILED("PER-001", "Unable to fetch student %s"),
    STUDENT_PERSIST_FAILED("PER-002", "Unable to persist student %s"),
    USERAUTH_FETCH_FAILED("PER-003", "Unable to fetch auth record %s"),
    USERAUTH_PERSIST_FAILED("PER-004", "Unable to persist auth record %s"),
    USERAUTH_LAST_LOGIN_UPDATE_FAILED("PER-005", "Unable to update last login for %s"),
    ADMIN_FORBIDDEN("API-001", "User does not have admin privileges"),
    AUTH_NOT_AUTHENTICATED("API-002", "User not authenticated"),
    AUTHENTICATION_FAILED("API-003", "Unable to authenticate user %s"),
    CATALOG_LIST_FAILED("API-010", "Unable to fetch catalog data"),
    CATALOG_SECTIONS_FAILED("API-011", "Unable to fetch sections for course %s"),
    STUDENT_REGISTRATION_BLOCKED("API-020", "Registration blocked because maintenance mode is ON"),
    STUDENT_REGISTRATION_FAILED("API-021", "Unable to register student %s for section %s"),
    STUDENT_DROP_FAILED("API-022", "Unable to drop student %s from section %s"),
    STUDENT_TIMETABLE_FAILED("API-023", "Unable to fetch timetable for student %s"),
    STUDENT_GRADES_FAILED("API-024", "Unable to fetch grades for student %s"),
    STUDENT_TRANSCRIPT_FAILED("API-025", "Unable to generate transcript for student %s"),
    REQUEST_VALIDATION_FAILED("API-030", "Invalid request payload"),
    UNEXPECTED_SERVER_ERROR("API-999", "Unexpected server error"),
    STUDENT_NOT_FOUND("PER-006", "Student with roll %s not found"),
    STUDENT_UPDATE_FAILED("PER-007", "Unable to update student %s"),
    STUDENT_DELETE_FAILED("PER-008", "Unable to delete student %s"),
    INSTRUCTOR_DUPLICATE("INST-001", "Instructor with code %s already exists"),
    INSTRUCTOR_CREATE_FAILED("INST-002", "Failed to create instructor %s"),
    INSTRUCTOR_NOT_FOUND("INST-003", "Instructor with code %s not found"),
    INSTRUCTOR_UPDATE_FAILED("INST-004", "Unable to update instructor %s"),
    INSTRUCTOR_DELETE_FAILED("INST-005", "Unable to delete instructor %s"),
    INSTRUCTOR_FETCH_FAILED("INST-006", "Unable to fetch instructor %s"),
    PROGRAM_DUPLICATE("PRG-001", "Program with id %s already exists"),
    PROGRAM_NOT_FOUND("PRG-002", "Program with id %s not found"),
    PROGRAM_CREATE_FAILED("PRG-003", "Failed to create program %s"),
    PROGRAM_UPDATE_FAILED("PRG-004", "Unable to update program %s"),
    PROGRAM_DELETE_FAILED("PRG-005", "Unable to delete program %s"),
    PROGRAM_FETCH_FAILED("PRG-006", "Unable to fetch program %s"),
    DEPARTMENT_DUPLICATE("DEP-001", "Department with id %s already exists"),
    DEPARTMENT_NOT_FOUND("DEP-002", "Department with id %s not found"),
    DEPARTMENT_CREATE_FAILED("DEP-003", "Failed to create department %s"),
    DEPARTMENT_UPDATE_FAILED("DEP-004", "Unable to update department %s"),
    DEPARTMENT_DELETE_FAILED("DEP-005", "Unable to delete department %s"),
    DEPARTMENT_FETCH_FAILED("DEP-006", "Unable to fetch department %s"),
    COURSE_DUPLICATE("CRS-001", "Course with id %s already exists"),
    COURSE_NOT_FOUND("CRS-002", "Course with id %s not found"),
    COURSE_CREATE_FAILED("CRS-003", "Failed to create course %s"),
    COURSE_UPDATE_FAILED("CRS-004", "Unable to update course %s"),
    COURSE_DELETE_FAILED("CRS-005", "Unable to delete course %s"),
    COURSE_FETCH_FAILED("CRS-006", "Unable to fetch course %s"),
    ROOM_DUPLICATE("RM-001", "Room with number %s already exists"),
    ROOM_NOT_FOUND("RM-002", "Room with id/number %s not found"),
    ROOM_CREATE_FAILED("RM-003", "Failed to create room %s"),
    ROOM_UPDATE_FAILED("RM-004", "Unable to update room %s"),
    ROOM_DELETE_FAILED("RM-005", "Unable to delete room %s"),
    ROOM_FETCH_FAILED("RM-006", "Unable to fetch room data");

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

    public String format(Object... args) {
        return String.format(defaultMessage, args);
    }
}
