package com.iiitd.erp.io.controller;

import com.iiitd.erp.Domain.AuthResultDomain;
import com.iiitd.erp.Domain.LoginRequestDomain;
import com.iiitd.erp.core.service.AuthSessionService;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.io.dto.request.LoginRequest;
import com.iiitd.erp.io.dto.response.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.iiitd.erp.io.controller.support.ApiResponseSupport.error;
import static com.iiitd.erp.io.controller.support.ApiResponseSupport.from;
import static com.iiitd.erp.mappers.DomainToResponseMapper.DOMAIN_TO_RESPONSE_MAPPER_INSTANCE;
import static com.iiitd.erp.mappers.RequestToDomainMapper.REQUEST_TO_DOMAIN_MAPPER_INSTANCE;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final AuthSessionService authSessionService;

    public AuthController(AuthSessionService authSessionService){
        this.authSessionService = authSessionService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest body, HttpServletRequest request){
        LoginRequestDomain loginRequest = REQUEST_TO_DOMAIN_MAPPER_INSTANCE.loginRequestToDomain(body);
        String username = loginRequest.getUsername();
        LOGGER.info("Login attempt for {}", username);
        try {
            AuthResultDomain result = authSessionService.loginAndCreateSession(username, loginRequest.getPassword(), request);
            LoginResponse response = DOMAIN_TO_RESPONSE_MAPPER_INSTANCE.domainToLoginResponse(result).withMessage("Login Successful");
            return ResponseEntity.ok(response);
        } catch (ErpException ex) {
            LOGGER.warn("Login failed for {}: {}", username, ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during login for {}", username, ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.AUTHENTICATION_FAILED,
                    ErrorCode.AUTHENTICATION_FAILED.format(username));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        try {
            AuthResultDomain result = authSessionService.currentUser(request);
            if (result == null) {
                LOGGER.debug("Current user lookup returned empty");
                return error(HttpStatus.UNAUTHORIZED, ErrorCode.AUTH_NOT_AUTHENTICATED);
            }
            return ResponseEntity.ok(result);
        } catch (ErpException ex) {
            LOGGER.error("Failed to fetch current user: {}", ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while checking current user", ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.AUTHENTICATION_FAILED,
                    "Failed to resolve current session");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            var session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
            LOGGER.info("Session invalidated for {}", request.getRemoteAddr());
            return ResponseEntity.ok(java.util.Map.of("message", "Logged out"));
        } catch (ErpException ex) {
            LOGGER.error("Business error during logout: {}", ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during logout", ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.AUTHENTICATION_FAILED,
                    "Unable to logout at this time");
        }
    }
}