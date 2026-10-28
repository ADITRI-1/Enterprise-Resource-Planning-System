package com.iiitd.erp.integration;

import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.support.PostgresIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AccountSecurityIntegrationTest extends PostgresIntegrationTest {

    private String changeBody(String current, String next) {
        return "{\"currentPassword\":\"%s\",\"newPassword\":\"%s\"}".formatted(current, next);
    }

    @Test
    void fiveWrongPasswordsLockTheAccountUntilAdminUnlocks() throws Exception {
        attemptLogin("stu1", "wrong")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.details.remainingAttempts").value(4));
        for (int i = 0; i < 3; i++) {
            attemptLogin("stu1", "wrong").andExpect(status().isUnauthorized());
        }
        attemptLogin("stu1", "wrong")
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.ACCOUNT_LOCKED.getCode()));

        // Even the right password is refused while locked
        attemptLogin("stu1", "Stu@123").andExpect(status().isLocked());

        MockHttpSession admin = login("admin1", "Admin@123");
        mvc.perform(get("/api/admin/users").session(admin).param("q", "stu1"))
                .andExpect(jsonPath("$.items[0].locked").value(true));
        mvc.perform(post("/api/admin/users/stu1/unlock").session(admin)).andExpect(status().isOk());

        login("stu1", "Stu@123");
    }

    @Test
    void successfulLoginResetsTheFailureCounter() throws Exception {
        for (int i = 0; i < 4; i++) {
            attemptLogin("stu1", "wrong").andExpect(status().isUnauthorized());
        }
        login("stu1", "Stu@123");
        attemptLogin("stu1", "wrong").andExpect(jsonPath("$.details.remainingAttempts").value(4));
    }

    @Test
    void unknownUserLooksLikeAWrongPassword() throws Exception {
        attemptLogin("nobody", "x")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Incorrect username or password"))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    void changePasswordEnforcesRulesAndHistory() throws Exception {
        MockHttpSession stu1 = login("stu1", "Stu@123");

        mvc.perform(post("/api/auth/change-password").session(stu1).contentType(MediaType.APPLICATION_JSON)
                        .content(changeBody("not-it", "Better2026")))
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.CURRENT_PASSWORD_INCORRECT.getCode()));
        mvc.perform(post("/api/auth/change-password").session(stu1).contentType(MediaType.APPLICATION_JSON)
                        .content(changeBody("Stu@123", "short1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.PASSWORD_POLICY_VIOLATION.getCode()));
        mvc.perform(post("/api/auth/change-password").session(stu1).contentType(MediaType.APPLICATION_JSON)
                        .content(changeBody("Stu@123", "Better2026")))
                .andExpect(status().isOk());

        // Going back to the previous password is refused
        mvc.perform(post("/api/auth/change-password").session(stu1).contentType(MediaType.APPLICATION_JSON)
                        .content(changeBody("Better2026", "Stu@1234")))
                .andExpect(status().isOk());
        mvc.perform(post("/api/auth/change-password").session(stu1).contentType(MediaType.APPLICATION_JSON)
                        .content(changeBody("Stu@1234", "Better2026")))
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.PASSWORD_REUSED.getCode()));

        attemptLogin("stu1", "Stu@123").andExpect(status().isUnauthorized());
        login("stu1", "Stu@1234");
    }

    @Test
    void adminResetIssuesTemporaryPasswordThatMustBeChanged() throws Exception {
        String body = mvc.perform(post("/api/admin/users/inst1/reset-password").session(login("admin1", "Admin@123")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String temp = JsonPath.read(body, "$.temporaryPassword");

        attemptLogin("inst1", "Inst@123").andExpect(status().isUnauthorized());
        MockHttpSession inst1 = login("inst1", temp);
        attemptLogin("inst1", temp).andExpect(jsonPath("$.mustChangePassword").value(true));

        mvc.perform(get("/api/instructor/INS001/sections").session(inst1))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.PASSWORD_CHANGE_REQUIRED.getCode()));
        mvc.perform(post("/api/auth/change-password").session(inst1).contentType(MediaType.APPLICATION_JSON)
                        .content(changeBody(temp, "Teach2026x")))
                .andExpect(status().isOk());
        mvc.perform(get("/api/instructor/INS001/sections").session(inst1)).andExpect(status().isOk());
    }

    @Test
    void changePasswordNeedsALogin() throws Exception {
        mvc.perform(post("/api/auth/change-password").contentType(MediaType.APPLICATION_JSON)
                        .content(changeBody("a", "b")))
                .andExpect(status().isUnauthorized());
    }
}
