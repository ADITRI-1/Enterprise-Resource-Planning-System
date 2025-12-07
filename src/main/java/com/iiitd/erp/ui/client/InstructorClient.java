package com.iiitd.erp.ui.client;

import com.google.gson.Gson;
import com.iiitd.erp.io.dto.response.CourseResponse;
import com.iiitd.erp.io.dto.response.SectionResponse;
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

    public List<CourseResponse> getMyCourses() throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/instructor/courses")
                .addHeader("Cookie", cookieHeader)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String responseBody = response.body().string();
            CourseResponse[] courses = gson.fromJson(responseBody, CourseResponse[].class);
            return Arrays.asList(courses);
        }
    }

    public List<SectionResponse> getMySections(String courseId) throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/courses/" + courseId + "/sections")
                .addHeader("Cookie", cookieHeader)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String responseBody = response.body().string();
            SectionResponse[] sections = gson.fromJson(responseBody, SectionResponse[].class);
            return Arrays.asList(sections);
        }
    }
}
