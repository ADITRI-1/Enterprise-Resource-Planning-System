package com.iiitd.erp.ui.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class AuthClient {

    private final String baseUrl;
    private final ObjectMapper mapper = new ObjectMapper();
    private String cookieHeader;

    public AuthClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public LoginResult login(String username, String password) throws IOException {
        URL url = new URL(baseUrl + "/api/auth/login");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String payload = mapper.writeValueAsString(new LoginRequest(username, password));
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        List<String> cookies = conn.getHeaderFields().get("Set-Cookie");
        if (cookies != null && !cookies.isEmpty()) {
            cookieHeader = cookies.stream()
                    .map(c -> c.split(";",2)[0])
                    .collect(Collectors.joining("; "));
        }

        InputStream is = status >= 200 && status < 300 ? conn.getInputStream() : conn.getErrorStream();
        if (is == null) {
            throw new IOException("No response from server (status " + status + ")");
        }
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        if (status >= 200 && status < 300) {
            LoginResponse resp = mapper.readValue(body, LoginResponse.class);
            return new LoginResult(true, resp.message, resp.username, resp.role, resp.sessionId, cookieHeader);
        } else {
            return new LoginResult(false, extractMessage(body), null, null, null, cookieHeader);
        }
    }

    private String extractMessage(String body) {
        try {
            ApiError err = mapper.readValue(body, ApiError.class);
            return err.message == null ? body : err.message;
        } catch (Exception ex) {
            return body;
        }
    }

    // DTOs for deserialization
    private record LoginRequest(String username, String password) {}
    private record LoginResponse(String message, String username, String role, String sessionId) {}
    private record ApiError(String errorCode, String message) {}


    public record LoginResult(boolean success, String message, String username, String role, String sessionId, String cookieHeader) {}
}
