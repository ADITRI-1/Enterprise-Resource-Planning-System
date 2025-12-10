package com.iiitd.erp.ui.client;

import com.google.gson.Gson;
import com.iiitd.erp.io.dto.response.AssessmentComponentResponse;
import com.iiitd.erp.io.dto.response.CourseResponse;
import com.iiitd.erp.io.dto.response.InstructorResponse;
import com.iiitd.erp.io.dto.response.SectionResponse;
import com.iiitd.erp.io.dto.response.TimetableSlot;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class InstructorClient {
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private final String baseUrl;
    private String cookieHeader;
    private InstructorResponse instructorInfo;

    public InstructorClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public InstructorClient(String baseUrl, String cookieHeader) {
        this.baseUrl = baseUrl;
        this.cookieHeader = cookieHeader;
    }

    public void setCookieHeader(String cookieHeader) {
        this.cookieHeader = cookieHeader;
    }

    public InstructorResponse getMyInfo() throws IOException {
        if (instructorInfo != null) {
            return instructorInfo;
        }

        Request request = new Request.Builder()
                .url(baseUrl + "/api/instructor/me")
                .addHeader("Cookie", cookieHeader)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String responseBody = response.body().string();
            instructorInfo = gson.fromJson(responseBody, InstructorResponse.class);
            return instructorInfo;
        }
    }

    public List<CourseResponse> getMyCourses() throws IOException {
        InstructorResponse me = getMyInfo();
        Request request = new Request.Builder()
                .url(baseUrl + "/api/instructor/" + me.instructorCode() + "/courses")
                .addHeader("Cookie", cookieHeader)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String responseBody = response.body().string();
            CourseResponse[] courses = gson.fromJson(responseBody, CourseResponse[].class);
            return Arrays.asList(courses);
        }
    }

    public List<SectionResponse> getMySections() throws IOException {
        InstructorResponse me = getMyInfo();
        Request request = new Request.Builder()
                .url(baseUrl + "/api/instructor/" + me.instructorCode() + "/sections")
                .addHeader("Cookie", cookieHeader)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String responseBody = response.body().string();
            SectionResponse[] sections = gson.fromJson(responseBody, SectionResponse[].class);
            return Arrays.asList(sections);
        }
    }

    public List<AssessmentComponentResponse> getAssessments(String sectionId) throws IOException {
        InstructorResponse me = getMyInfo();
        Request request = new Request.Builder()
                .url(baseUrl + "/api/instructor/" + me.instructorCode() + "/sections/" + sectionId + "/assessments")
                .addHeader("Cookie", cookieHeader)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String responseBody = response.body().string();
            AssessmentComponentResponse[] assessments = gson.fromJson(responseBody, AssessmentComponentResponse[].class);
            return Arrays.asList(assessments);
        }
    }

    public List<TimetableSlot> getMyTimetable(String semester, int year) throws IOException {
        InstructorResponse me = getMyInfo();
        Request request = new Request.Builder()
                .url(baseUrl + "/api/instructor/" + me.instructorCode() + "/timetable?semester=" + semester + "&year=" + year)
                .addHeader("Cookie", cookieHeader)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String responseBody = response.body().string();
            TimetableSlot[] slots = gson.fromJson(responseBody, TimetableSlot[].class);
            return Arrays.asList(slots);
        }
    }
}
