package com.iiitd.erp.io.controller;

import com.iiitd.erp.Domain.AuthResultDomain;
import com.iiitd.erp.Domain.LoginRequestDomain;
import com.iiitd.erp.core.service.AuthSessionService;
import com.iiitd.erp.io.dto.request.LoginRequest;
import com.iiitd.erp.io.dto.response.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.iiitd.erp.mappers.RequestToDomainMapper.REQUEST_TO_DOMAIN_MAPPER_INSTANCE;
import static com.iiitd.erp.mappers.DomainToResponseMapper.DOMAIN_TO_RESPONSE_MAPPER_INSTANCE;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthSessionService authSessionService;

  public AuthController(AuthSessionService authSessionService){
      this.authSessionService = authSessionService;
  }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest body, HttpServletRequest req){
      LoginRequestDomain loginRequestDomain = REQUEST_TO_DOMAIN_MAPPER_INSTANCE.loginRequestToDomain(body);

        AuthResultDomain authResultDomain = authSessionService.loginAndCreateSession(loginRequestDomain.getUsername(), loginRequestDomain.getPassword(),req);
        LoginResponse loginResponse = DOMAIN_TO_RESPONSE_MAPPER_INSTANCE.domainToLoginResponse(authResultDomain);
        return ResponseEntity.ok(loginResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest req) {
        AuthResultDomain result = authSessionService.currentUser(req);
        if (result == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "Not authenticated"));
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest req) {
        var session = req.getSession(false);
        if (session != null) session.invalidate();
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        return ResponseEntity.ok(java.util.Map.of("message", "Logged out"));
    }
}
