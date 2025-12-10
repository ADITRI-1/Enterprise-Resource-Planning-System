package com.iiitd.erp.ui.client;

import com.google.gson.Gson;
import com.iiitd.erp.io.dto.response.CourseResponse;
import com.iiitd.erp.io.dto.response.CourseRow;
import com.iiitd.erp.io.dto.response.Grade;
import com.iiitd.erp.io.dto.response.SectionRow;
import com.iiitd.erp.io.dto.response.StudentResponse;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class StudentClient {
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private final String baseUrl;
    private String cookieHeader;

    public StudentClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public StudentClient(String baseUrl, String cookieHeader) {
        this.baseUrl = baseUrl;
        this.cookieHeader = cookieHeader;
    }

    public void setCookieHeader(String cookieHeader) {
        this.cookieHeader = cookieHeader;
    }

    public StudentResponse getMyInfo() throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/student/me")
                .addHeader("Cookie", cookieHeader)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String responseBody = response.body().string();
            return gson.fromJson(responseBody, StudentResponse.class);
        }
    }

    public List<CourseResponse> getMyCourses() throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/student/courses")
                .addHeader("Cookie", cookieHeader)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String responseBody = response.body().string();
            CourseResponse[] courses = gson.fromJson(responseBody, CourseResponse[].class);
            return Arrays.asList(courses);
        }
    }

    public List<CourseResponse> getAvailableCourses() throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/courses")
                .addHeader("Cookie", cookieHeader)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String responseBody = response.body().string();
            CourseResponse[] courses = gson.fromJson(responseBody, CourseResponse[].class);
            return Arrays.asList(courses);
        }
    }

    public List<Grade> getMyGrades() throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/student/grades")
                .addHeader("Cookie", cookieHeader)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String responseBody = response.body().string();
            Grade[] grades = gson.fromJson(responseBody, Grade[].class);
            return Arrays.asList(grades);
        }
    }

    public List<com.iiitd.erp.io.dto.response.TimetableSlot> getMyTimetable(String semester, int year) throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/student/timetable?semester=" + semester + "&year=" + year)
                .addHeader("Cookie", cookieHeader)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String responseBody = response.body().string();
            com.iiitd.erp.io.dto.response.TimetableSlot[] slots = gson.fromJson(responseBody, com.iiitd.erp.io.dto.response.TimetableSlot[].class);
            return Arrays.asList(slots);
        }
    }

    public List<SectionRow> getRegisteredSections(String uid) throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/student/" + uid + "/registrations")
                .addHeader("Cookie", cookieHeader)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            SectionRow[] rows = gson.fromJson(response.body().string(), SectionRow[].class);
            return Arrays.asList(rows);
        }
    }
    public List<SectionRow> courseCatalog() throws IOException {
        // derive current term
        LocalDate now = LocalDate.now();
        String semester = now.getMonthValue() >= 7 ? "Monsoon" : "Winter";
        int year = now.getYear();

        // list courses
        RequestBody body = RequestBody.create(
                gson.toJson(new CatalogListRequest(semester, year, "")),
                MediaType.parse("application/json"));
        Request courseReq = new Request.Builder()
                .url(baseUrl + "/api/catalog/list")
                .addHeader("Cookie", cookieHeader)
                .post(body)
                .build();
        CourseRow[] courses;
        try (Response response = client.newCall(courseReq).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            courses = gson.fromJson(response.body().string(), CourseRow[].class);
        }

        // fetch sections per course
        List<SectionRow> allSections = new java.util.ArrayList<>();
        if (courses != null) {
            for (CourseRow course : courses) {
                Request secReq = new Request.Builder()
                        .url(baseUrl + "/api/catalog/" + course.courseId() + "/sections?semester=" + semester + "&year=" + year)
                        .addHeader("Cookie", cookieHeader)
                        .build();
                try (Response response = client.newCall(secReq).execute()) {
                    if (!response.isSuccessful()) continue;
                    SectionRow[] sections = gson.fromJson(response.body().string(), SectionRow[].class);
                    if (sections != null) {
                        allSections.addAll(Arrays.asList(sections));
                    }
                }
            }
        }
        return allSections;
    }

    public void registerCourse(String sectionId) throws IOException {
        StudentResponse me = getMyInfo();
        String uid = me != null && me.rollNo() != null ? me.rollNo().toString() : "me";
        RequestBody body = RequestBody.create(
                gson.toJson(java.util.Map.of("sectionId", sectionId)),
                MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(baseUrl + "/api/student/" + uid + "/register")
                .addHeader("Cookie", cookieHeader)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Registration failed: " + response.code());
            }
        }
    }

    public void dropCourse(String sectionId) throws IOException {
        StudentResponse me = getMyInfo();
        String uid = me != null && me.rollNo() != null ? me.rollNo().toString() : "me";
        RequestBody body = RequestBody.create(
                gson.toJson(java.util.Map.of("sectionId", sectionId)),
                MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(baseUrl + "/api/student/" + uid + "/drop")
                .addHeader("Cookie", cookieHeader)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Drop failed: " + response.code());
            }
        }
    }

    private record CatalogListRequest(String semester, int year, String q) {}
}
