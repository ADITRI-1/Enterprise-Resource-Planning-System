package com.iiitd.erp.io.controller;

import com.iiitd.erp.Domain.AuthResultDomain;
import com.iiitd.erp.core.service.AuthSessionService;
import com.iiitd.erp.core.service.MaintenanceService;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.core.service.AccountService;
import com.iiitd.erp.io.controller.support.AuditInterceptor;
import com.iiitd.erp.io.controller.support.CurrentUser;
import com.iiitd.erp.persistent.persistence.repository.AuditRepository;
import com.iiitd.erp.io.controller.support.MaintenanceReadOnlyInterceptor;
import com.iiitd.erp.io.controller.support.PasswordChangeRequiredInterceptor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({MaintenanceReadOnlyInterceptor.class, PasswordChangeRequiredInterceptor.class, AuditInterceptor.class})
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired MockMvc mvc;

    @MockitoBean AuthSessionService authSessionService;
    @MockitoBean MaintenanceService maintenanceService;
    @MockitoBean AccountService accountService;
    @MockitoBean CurrentUser currentUser;
    @MockitoBean AuditRepository auditRepository;

    @Test
    void login_success() throws Exception {
        var domain = AuthResultDomain.builder()
                .userId(UUID.randomUUID())
                .username("admin1")
                .role("ADMIN")
                .sessionId("sid123")
                .build();

        Mockito.when(authSessionService.loginAndCreateSession(eq("admin1"), eq("password"), any(), any()))
                .thenReturn(domain);

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin1\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("username").value("admin1"))
                .andExpect(jsonPath("role").value("ADMIN"));
    }

    @Test
    void login_business_error_returns_401() throws Exception {
        Mockito.when(authSessionService.loginAndCreateSession(eq("admin1"), eq("wrong"), any(), any()))
                .thenThrow(ErpException.of(ErrorCode.AUTHENTICATION_FAILED, Map.of()));

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin1\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("errorCode").value(ErrorCode.AUTHENTICATION_FAILED.getCode()))
                .andExpect(jsonPath("message").value(ErrorCode.AUTHENTICATION_FAILED.getDefaultMessage()));
    }

    @Test
    void login_unexpected_error_returns_500() throws Exception {
        Mockito.when(authSessionService.loginAndCreateSession(eq("admin1"), eq("password"), any(), any()))
                .thenThrow(new RuntimeException("boom"));

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin1\",\"password\":\"password\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("errorCode").value(ErrorCode.UNEXPECTED_SERVER_ERROR.getCode()));
    }

    @Test
    void me_returns_current_user() throws Exception {
        UUID userId = UUID.randomUUID();
        var domain = AuthResultDomain.builder()
                .userId(userId)
                .username("admin1")
                .role("ADMIN")
                .sessionId("sid123")
                .build();
        Mockito.when(authSessionService.currentUser(any())).thenReturn(domain);

        mvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("userId").value(userId.toString()))
                .andExpect(jsonPath("username").value("admin1"))
                .andExpect(jsonPath("role").value("ADMIN"));
    }

    @Test
    void me_returns_401_when_not_authenticated() throws Exception {
        Mockito.when(authSessionService.currentUser(any())).thenReturn(null);

        mvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("errorCode").value(ErrorCode.AUTH_NOT_AUTHENTICATED.getCode()))
                .andExpect(jsonPath("message").value(ErrorCode.AUTH_NOT_AUTHENTICATED.getDefaultMessage()));
    }

    @Test
    void logout_invalidates_session() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("key", "value");

        mvc.perform(post("/api/auth/logout").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value("Logged out"));

        assertThrows(IllegalStateException.class, () -> session.getAttribute("key"));
    }
}
