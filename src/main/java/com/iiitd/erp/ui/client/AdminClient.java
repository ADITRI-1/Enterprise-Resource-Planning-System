package com.iiitd.erp.ui.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.iiitd.erp.io.dto.request.*;
import com.iiitd.erp.io.dto.response.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public class AdminClient {

    private final String baseUrl;
    private final String cookieHeader;
    private final ObjectMapper mapper;

    public AdminClient(String baseUrl, String cookieHeader) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.cookieHeader = cookieHeader;
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // Students
    public List<StudentResponse> listStudents() throws IOException {
        return send("GET", "/api/admin/students", null, new TypeReference<>() {});
    }
    public CreateUserResponse createStudent(StudentCreateRequest req) throws IOException {
        return send("POST", "/api/admin/create/student", req, new TypeReference<>() {});
    }
    public StudentResponse updateStudent(String rollNo, StudentUpdateRequest req) throws IOException {
        return send("PUT", "/api/admin/student/" + rollNo, req, new TypeReference<>() {});
    }
    public void deleteStudent(String rollNo) throws IOException {
        sendVoid("DELETE", "/api/admin/student/" + rollNo, null);
    }

    // Instructors
    public List<InstructorResponse> listInstructors() throws IOException {
        return send("GET", "/api/admin/instructors", null, new TypeReference<>() {});
    }
    public CreateUserResponse createInstructor(InstructorCreateRequest req) throws IOException {
        return send("POST", "/api/admin/create/instructor", req, new TypeReference<>() {});
    }
    public InstructorResponse updateInstructor(String code, InstructorUpdateRequest req) throws IOException {
        return send("PUT", "/api/admin/instructor/" + code, req, new TypeReference<>() {});
    }
    public void deleteInstructor(String code) throws IOException {
        sendVoid("DELETE", "/api/admin/instructor/" + code, null);
    }

    // Programs
    public List<ProgramResponse> listPrograms() throws IOException {
        return send("GET", "/api/admin/programs", null, new TypeReference<>() {});
    }
    public ProgramResponse createProgram(ProgramCreateRequest req) throws IOException {
        return send("POST", "/api/admin/programs", req, new TypeReference<>() {});
    }
    public ProgramResponse updateProgram(String id, ProgramUpdateRequest req) throws IOException {
        return send("PUT", "/api/admin/programs/" + id, req, new TypeReference<>() {});
    }
    public void deleteProgram(String id) throws IOException {
        sendVoid("DELETE", "/api/admin/programs/" + id, null);
    }

    // Departments
    public List<DepartmentResponse> listDepartments() throws IOException {
        return send("GET", "/api/admin/departments", null, new TypeReference<>() {});
    }
    public DepartmentResponse createDepartment(DepartmentCreateRequest req) throws IOException {
        return send("POST", "/api/admin/departments", req, new TypeReference<>() {});
    }
    public DepartmentResponse updateDepartment(String id, DepartmentUpdateRequest req) throws IOException {
        return send("PUT", "/api/admin/departments/" + id, req, new TypeReference<>() {});
    }
    public void deleteDepartment(String id) throws IOException {
        sendVoid("DELETE", "/api/admin/departments/" + id, null);
    }

    // Courses
    public List<CourseResponse> listCourses() throws IOException {
        return send("GET", "/api/admin/courses", null, new TypeReference<>() {});
    }
    public CourseResponse createCourse(CourseCreateRequest req) throws IOException {
        return send("POST", "/api/admin/courses", req, new TypeReference<>() {});
    }
    public CourseResponse updateCourse(String id, CourseUpdateRequest req) throws IOException {
        return send("PUT", "/api/admin/courses/" + id, req, new TypeReference<>() {});
    }
    public void deleteCourse(String id) throws IOException {
        sendVoid("DELETE", "/api/admin/courses/" + id, null);
    }

    // Rooms
    public List<RoomResponse> listRooms() throws IOException {
        return send("GET", "/api/admin/rooms", null, new TypeReference<>() {});
    }
    public RoomResponse createRoom(RoomCreateRequest req) throws IOException {
        return send("POST", "/api/admin/rooms", req, new TypeReference<>() {});
    }
    public RoomResponse updateRoom(String id, RoomUpdateRequest req) throws IOException {
        return send("PUT", "/api/admin/rooms/" + id, req, new TypeReference<>() {});
    }
    public void deleteRoom(String id) throws IOException {
        sendVoid("DELETE", "/api/admin/rooms/" + id, null);
    }

    // Sections
    public List<SectionResponse> listSections() throws IOException {
        return send("GET", "/api/admin/sections", null, new TypeReference<>() {});
    }
    public List<SectionResponse> listSectionsForCourse(String courseId, String semester, Integer year) throws IOException {
        String q = "";
        if (semester != null) q += (q.isEmpty() ? "?" : "&") + "semester=" + semester;
        if (year != null) q += (q.isEmpty() ? "?" : "&") + "year=" + year;
        return send("GET", "/api/admin/courses/" + courseId + "/sections" + q, null, new TypeReference<>() {});
    }
    public SectionResponse createSection(String courseId, SectionCreateRequest req) throws IOException {
        return send("POST", "/api/admin/courses/" + courseId + "/sections", req, new TypeReference<>() {});
    }
    public SectionResponse updateSection(String sectionId, SectionUpdateRequest req) throws IOException {
        return send("PUT", "/api/admin/sections/" + sectionId, req, new TypeReference<>() {});
    }
    public void deleteSection(String sectionId) throws IOException {
        sendVoid("DELETE", "/api/admin/sections/" + sectionId, null);
    }

    // Section schedule
    public SectionScheduleResponse viewSchedule(String sectionId) throws IOException {
        return send("GET", "/api/admin/sections/" + sectionId + "/schedule", null, new TypeReference<>() {});
    }
    public SectionScheduleResponse upsertSchedule(String sectionId, SectionScheduleRequest req) throws IOException {
        return send("PUT", "/api/admin/sections/" + sectionId + "/schedule", req, new TypeReference<>() {});
    }
    public void clearSchedule(String sectionId) throws IOException {
        sendVoid("DELETE", "/api/admin/sections/" + sectionId + "/schedule", null);
    }

    // Availability
    public List<RoomAvailabilityResponse> availableRooms(DayOfWeek day, LocalTime start, LocalTime end, int minCapacity) throws IOException {
        String path = String.format("/api/admin/rooms/available?day=%s&start=%s&end=%s&minCapacity=%d",
                day, start, end, minCapacity);
        return send("GET", path, null, new TypeReference<>() {});
    }
    public InstructorAvailabilityResponse instructorAvailability(String code, String semester, int year) throws IOException {
        String path = String.format("/api/admin/instructors/%s/availability?semester=%s&year=%d", code, semester, year);
        return send("GET", path, null, new TypeReference<>() {});
    }

    // Maintenance
    public MaintenanceStatusResponse maintenanceStatus() throws IOException {
        return send("GET", "/api/admin/settings/maintenance", null, new TypeReference<>() {});
    }
    public MaintenanceStatusResponse toggleMaintenance(boolean on) throws IOException {
        return send("PUT", "/api/admin/settings/maintenance",
                new MaintenanceToggleRequest(on), new TypeReference<>() {});
    }

    // HTTP helpers
    private <T> T send(String method, String path, Object body, TypeReference<T> typeRef) throws IOException {
        HttpURLConnection conn = open(method, path, body != null);
        if (body != null) writeBody(conn, body);
        int status = conn.getResponseCode();
        String payload = readPayload(conn, status);
        if (status >= 200 && status < 300) {
            if (typeRef.getType().getTypeName().equals("java.lang.Void")) return null;
            return mapper.readValue(payload, typeRef);
        }
        throw buildError(status, payload);
    }

    private void sendVoid(String method, String path, Object body) throws IOException {
        send(method, path, body, new TypeReference<Void>() {});
    }

    private HttpURLConnection open(String method, String path, boolean doOutput) throws IOException {
        URL url = new URL(baseUrl + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");
        if (cookieHeader != null && !cookieHeader.isBlank()) {
            conn.setRequestProperty("Cookie", cookieHeader);
        }
        conn.setDoOutput(doOutput);
        return conn;
    }

    private void writeBody(HttpURLConnection conn, Object body) throws IOException {
        String json = mapper.writeValueAsString(body);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }
    }

    private String readPayload(HttpURLConnection conn, int status) throws IOException {
        InputStream is = status >= 200 && status < 300 ? conn.getInputStream() : conn.getErrorStream();
        if (is == null) return "";
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    private IOException buildError(int status, String payload) {
        try {
            ApiErrorResponse api = mapper.readValue(payload, ApiErrorResponse.class);
            String msg = (api.message() != null && !api.message().isBlank())
                    ? api.message()
                    : api.errorCode();
            return new IOException("HTTP " + status + ": " + msg);
        } catch (Exception ignore) {
            return new IOException("HTTP " + status + ": " + payload);
        }
    }
}
