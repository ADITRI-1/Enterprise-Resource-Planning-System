package com.iiitd.erp.core.service.impl;

import com.iiitd.erp.Domain.AuthResultDomain;
import com.iiitd.erp.Domain.UserAuthDomain;
import com.iiitd.erp.core.service.AccountService;
import com.iiitd.erp.core.service.AuthSessionService;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.persistent.persistence.repository.UserAuthRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.iiitd.erp.mappers.EntityToDomainMapper.ENTITY_TO_DOMAIN_MAPPER_INSTANCE;

@Service
public class AuthSessionServiceImpl implements AuthSessionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthSessionServiceImpl.class);

    private final AuthenticationManager authManager;
    private final UserAuthRepository users;
    private final AccountService accounts;

    public AuthSessionServiceImpl(AuthenticationManager authManager, UserAuthRepository users, AccountService accounts) {
        this.authManager = authManager;
        this.users = users;
        this.accounts = accounts;
    }

    @Override
    @Transactional
    public AuthResultDomain loginAndCreateSession(String username, String password, String role, HttpServletRequest req) {
        LOGGER.info("Login attempt for {} with role {}", username, role);
        Optional<UserAuthDomain> account = findUser(username);

        // Checked before the password so a locked account can't keep being guessed at.
        account.filter(u -> u.getLockedUntil() != null && u.getLockedUntil().isAfter(OffsetDateTime.now()))
                .ifPresent(u -> {
                    throw ErpException.of(ErrorCode.ACCOUNT_LOCKED, Map.of("lockedUntil", u.getLockedUntil()));
                });

        Authentication auth;
        try {
            auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (BadCredentialsException ex) {
            throw failedLogin(username);
        } catch (AuthenticationException ex) {
            LOGGER.warn("Login rejected for {}: {}", username, ex.getMessage());
            throw ErpException.of(ErrorCode.AUTHENTICATION_FAILED, Map.of());
        }

        UserAuthDomain user = account.orElseThrow();
        if (role != null && !role.isBlank() && !role.equalsIgnoreCase(user.getRole())) {
            LOGGER.warn("Role mismatch for {} (requested {}, actual {})", username, role, user.getRole());
            throw ErpException.of(ErrorCode.AUTHENTICATION_FAILED, Map.of());
        }

        SecurityContextHolder.getContext().setAuthentication(auth);
        HttpSession session = req.getSession(true);
        session.setAttribute("uid", user.getUserId());
        session.setAttribute("role", user.getRole());
        accounts.recordSuccessfulLogin(username);

        LOGGER.info("Login successful for {}", username);
        OffsetDateTime previousLogin = user.getLastLogin();
        return AuthResultDomain.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .role(user.getRole())
                .sessionId(session.getId())
                .lastLogin(previousLogin)
                .firstLogin(previousLogin == null)
                .mustChangePassword(user.isMustChangePassword())
                .build();
    }

    private ErpException failedLogin(String username) {
        UserAuthDomain updated = accounts.recordFailedLogin(username);
        if (updated == null) {
            // Unknown username: same response as a wrong password.
            return ErpException.of(ErrorCode.AUTHENTICATION_FAILED, Map.of());
        }
        if (updated.getLockedUntil() != null && updated.getLockedUntil().isAfter(OffsetDateTime.now())) {
            return ErpException.of(ErrorCode.ACCOUNT_LOCKED, Map.of("lockedUntil", updated.getLockedUntil()));
        }
        Map<String, Object> details = new HashMap<>();
        details.put("remainingAttempts", AccountService.MAX_FAILED_ATTEMPTS - updated.getFailedAttempts());
        return ErpException.of(ErrorCode.AUTHENTICATION_FAILED, details);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResultDomain currentUser(HttpServletRequest req) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        HttpSession session = req.getSession(false);
        return findUser(auth.getName())
                .map(user -> AuthResultDomain.builder()
                        .userId(user.getUserId())
                        .username(user.getUsername())
                        .sessionId(session != null ? session.getId() : null)
                        .firstLogin(false)
                        .lastLogin(user.getLastLogin())
                        .role(user.getRole())
                        .mustChangePassword(user.isMustChangePassword())
                        .build())
                .orElse(null);
    }

    private Optional<UserAuthDomain> findUser(String username) {
        return users.findByUsername(username).map(ENTITY_TO_DOMAIN_MAPPER_INSTANCE::userAuthToDomain);
    }
}
