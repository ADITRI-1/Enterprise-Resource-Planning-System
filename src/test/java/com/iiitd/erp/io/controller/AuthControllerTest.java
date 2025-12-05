package com.iiitd.erp.io.controller;

import com.iiitd.erp.Domain.AuthResultDomain;
import com.iiitd.erp.core.service.AuthSessionService;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.io.dto.request.LoginRequest;
import com.iiitd.erp.io.dto.response.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthSessionService authSessionService;
    @InjectMocks private AuthController controller;

    private static AuthResultDomain sampleAuth() {
        return AuthResultDomain.builder()
                .userId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .username("student1")
                .role("STUDENT")
                .sessionId("sid-123")
                .lastLogin(OffsetDateTime.now())
                .firstLogin(false)
                .build();
    }

    @Test
    void login_success() {
        when(authSessionService.loginAndCreateSession(eq("student1"), eq("password"), any()))
                .thenReturn(sampleAuth());

        ResponseEntity<?> resp = controller.login(
                new LoginRequest("student1", "password"),
                mock(HttpServletRequest.class));

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        LoginResponse body = (LoginResponse) resp.getBody();
        assertNotNull(body);
        assertEquals("student1", body.username());
        assertEquals("STUDENT", body.role());
    }

    @Test
    void login_authFailure() {
        when(authSessionService.loginAndCreateSession(eq("student1"), eq("bad"), any()))
                .thenThrow(ErpException.of(ErrorCode.AUTHENTICATION_FAILED, Map.of("username", "student1")));

        ResponseEntity<?> resp = controller.login(
                new LoginRequest("student1", "bad"),
                mock(HttpServletRequest.class));

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }

    @Test
    void login_unexpected() {
        when(authSessionService.loginAndCreateSession(eq("student1"), eq("password"), any()))
                .thenThrow(new RuntimeException("boom"));

        ResponseEntity<?> resp = controller.login(
                new LoginRequest("student1", "password"),
                mock(HttpServletRequest.class));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
    }

    @Test
    void me_authenticated() {
        when(authSessionService.currentUser(any())).thenReturn(sampleAuth());

        ResponseEntity<?> resp = controller.me(mock(HttpServletRequest.class));

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        AuthResultDomain body = (AuthResultDomain) resp.getBody();
        assertNotNull(body);
        assertEquals("student1", body.getUsername());
    }

    @Test
    void me_notAuthenticated() {
        when(authSessionService.currentUser(any())).thenReturn(null);

        ResponseEntity<?> resp = controller.me(mock(HttpServletRequest.class));

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }

    @Test
    void logout_ok() {
        ResponseEntity<?> resp = controller.logout(mock(HttpServletRequest.class));
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }
}
