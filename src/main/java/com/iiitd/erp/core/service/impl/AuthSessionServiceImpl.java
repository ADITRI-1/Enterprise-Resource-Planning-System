package com.iiitd.erp.core.service.impl;

import com.iiitd.erp.Domain.AuthResultDomain;
import com.iiitd.erp.Domain.UserAuthDomain;
import com.iiitd.erp.core.service.AuthSessionService;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.persistent.persistence.repository.UserAuthRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static com.iiitd.erp.mappers.EntityToDomainMapper.ENTITY_TO_DOMAIN_MAPPER_INSTANCE;

@Service
public class AuthSessionServiceImpl implements AuthSessionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthSessionServiceImpl.class);

    private final AuthenticationManager authManager;
    private final UserAuthRepository users;

    public AuthSessionServiceImpl(AuthenticationManager authManager, UserAuthRepository users) {
        this.authManager = authManager;
        this.users = users;
    }

    @Override
    @Transactional
    public AuthResultDomain loginAndCreateSession(String username, String password, String role, HttpServletRequest req) {
        LOGGER.info("Login attempt for {} with role {}", username,role);
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            UserAuthDomain user = findUser(username);

            if(role!=null && !role.isBlank() && !role.equalsIgnoreCase(user.getRole())){
                LOGGER.warn("Role mismatch for {} (requested {}, actual {})",username,role,user.getRole());
                SecurityContextHolder.clearContext();
                throw ErpException.of(ErrorCode.AUTHENTICATION_FAILED,
                        Map.of("username",username,"role",role,"expectedRole",user.getRole()));
            }

            HttpSession session =req.getSession(true);
            session.setAttribute("uid",user.getUserId());
            session.setAttribute("role",user.getRole());

            OffsetDateTime previousLogin = user.getLastLogin();
            boolean firstLogin = (previousLogin == null);

            int updated = users.updateLastLogin(username, OffsetDateTime.now());
            if (updated == 0) {
                LOGGER.warn("Last login update affected 0 rows for {}", username);
            }

            LOGGER.info("Login successful for {}", username);

            return AuthResultDomain.builder()
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .role(user.getRole())
                    .sessionId(session.getId())
                    .lastLogin(previousLogin)
                    .firstLogin(firstLogin)
                    .build();
        } catch (ErpException ex) {
            LOGGER.error("Known auth failure for {}: {}", username, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected authentication failure for {}", username, ex);
            throw ErpException.wrap(
                    ErrorCode.AUTHENTICATION_FAILED, ex, Map.of("username", username));
        }
    }

    @Override
    public AuthResultDomain currentUser(HttpServletRequest req) {
        try {
            var ctx = SecurityContextHolder.getContext();
            var auth = ctx.getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                LOGGER.debug("No authenticated user in context");
                return null;
            }

            HttpSession session = req.getSession(false);
            String username = auth.getName();
            UserAuthDomain user = findUser(username);

            List<String> authorities = auth.getAuthorities().stream()
                    .map(Object::toString)
                    .toList();
            String sessionId = (session != null) ? session.getId() : null;

            LOGGER.debug("Resolved current user {} with authorities {}", username, authorities);

            return AuthResultDomain.builder()
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .sessionId(sessionId)
                    .firstLogin(false)
                    .lastLogin(user.getLastLogin())
                    .role(user.getRole())
                    .build();
        } catch (ErpException ex) {
            LOGGER.error("Failed to resolve current user: {}", ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error resolving current user", ex);
            throw ErpException.wrap(ErrorCode.AUTHENTICATION_FAILED, ex, Map.of());
        }
    }

    private UserAuthDomain findUser(String username) {
        return users.findByUsername(username)
                .map(ENTITY_TO_DOMAIN_MAPPER_INSTANCE::userAuthToDomain)
                .orElseThrow(() -> ErpException.of(
                        ErrorCode.USERAUTH_FETCH_FAILED, Map.of("username", username)));
    }
}
