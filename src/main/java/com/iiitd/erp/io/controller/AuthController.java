package com.iiitd.erp.io.controller;

import com.iiitd.erp.Domain.AuthResultDomain;
import com.iiitd.erp.core.service.AccountService;
import com.iiitd.erp.core.service.AuthSessionService;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.io.controller.support.AuditInterceptor;
import com.iiitd.erp.io.controller.support.CurrentUser;
import com.iiitd.erp.io.dto.request.ChangePasswordRequest;
import com.iiitd.erp.io.dto.request.LoginRequest;
import com.iiitd.erp.io.dto.response.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.iiitd.erp.mappers.DomainToResponseMapper.DOMAIN_TO_RESPONSE_MAPPER_INSTANCE;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Login, session and password management")
public class AuthController {

    private final AuthSessionService authSessionService;
    private final AccountService accountService;
    private final CurrentUser currentUser;

    public AuthController(AuthSessionService authSessionService, AccountService accountService, CurrentUser currentUser) {
        this.authSessionService = authSessionService;
        this.accountService = accountService;
        this.currentUser = currentUser;
    }

    @PostMapping("/login")
    @Operation(summary = "Log in and start a session",
            description = "Returns 401 with details.remainingAttempts on a wrong password and 423 once the account is "
                    + "locked. When mustChangePassword is true, only /api/auth/** works until the password is changed.")
    public LoginResponse login(@RequestBody @Valid LoginRequest body, HttpServletRequest request) {
        request.setAttribute(AuditInterceptor.ACTOR_ATTRIBUTE, body.username());
        AuthResultDomain result = authSessionService.loginAndCreateSession(
                body.username(), body.password(), body.role(), request);
        return DOMAIN_TO_RESPONSE_MAPPER_INSTANCE.domainToLoginResponse(result).withMessage("Login Successful");
    }

    @GetMapping("/me")
    @Operation(summary = "Current session's account")
    public AuthResultDomain me(HttpServletRequest request) {
        AuthResultDomain result = authSessionService.currentUser(request);
        if (result == null) {
            throw ErpException.of(ErrorCode.AUTH_NOT_AUTHENTICATED, Map.of());
        }
        return result;
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change your own password",
            description = "At least 8 characters with a letter and a digit, not containing the username, "
                    + "and different from the last 3 passwords.")
    public Map<String, String> changePassword(@RequestBody @Valid ChangePasswordRequest body) {
        String username = currentUser.requireAccount().getUsername();
        accountService.changePassword(username, body.currentPassword(), body.newPassword());
        return Map.of("message", "Password changed");
    }

    @PostMapping("/logout")
    @Operation(summary = "End the session")
    public Map<String, String> logout(HttpServletRequest request) {
        request.setAttribute(AuditInterceptor.ACTOR_ATTRIBUTE, currentUser.username());
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return Map.of("message", "Logged out");
    }
}
