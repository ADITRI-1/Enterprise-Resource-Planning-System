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
    INSTRUCTOR_FETCH_FAILED("INST-006", "Unable to fetch instructor %s");

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
