package com.iiitd.erp.ui.client;

import com.google.gson.Gson;
import com.iiitd.erp.io.dto.response.CourseResponse;
import com.iiitd.erp.io.dto.response.Grade;
import com.iiitd.erp.io.dto.response.StudentResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
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
}
