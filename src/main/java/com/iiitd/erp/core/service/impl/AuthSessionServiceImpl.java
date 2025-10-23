package com.iiitd.erp.core.service.impl;

import com.iiitd.erp.Domain.AuthResultDomain;
import com.iiitd.erp.Domain.UserAuthDomain;
import com.iiitd.erp.core.service.AuthSessionService;
import com.iiitd.erp.persistent.persistence.repository.UsersAuthRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import static com.iiitd.erp.mappers.DomainToEntityMapper.DOMAIN_TO_ENTITY_MAPPER_INSTANCE;
import static com.iiitd.erp.mappers.EntityToDomainMapper.ENTITY_TO_DOMAIN_MAPPER_INSTANCE;

import java.time.OffsetDateTime;
import java.util.List;


@Service
public class AuthSessionServiceImpl implements AuthSessionService {

    private final AuthenticationManager authManager;
    private final UsersAuthRepository users;

    public AuthSessionServiceImpl(AuthenticationManager authManager, UsersAuthRepository users) {
        this.authManager = authManager;
        this.users = users;
    }

    /**
     * Authenticate credentials, create server session, enrich from DB, and return a rich result.
     */
    @Transactional
    public AuthResultDomain loginAndCreateSession(String username, String password, HttpServletRequest req) {
        // 1) Delegate credential check to Spring Security
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        // 2) Put the Authentication into the SecurityContext
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 3) Create/get session and enrich it
        HttpSession session = req.getSession(true);

        UserAuthDomain u = ENTITY_TO_DOMAIN_MAPPER_INSTANCE.userAuthToDomain(users.findByUsername(username).orElseThrow());
        // shouldn't happen after auth
        session.setAttribute("uid", u.getUserId());
        session.setAttribute("role", u.getRole());

        // 4) (Optional) Update last_login (track previous value to return)
        OffsetDateTime previousLogin = u.getLastLogin();
        boolean firstLogin = (previousLogin == null);
        users.updateLastLogin(username,OffsetDateTime.now());

        return AuthResultDomain.builder()
                .userId(u.getUserId())
                .username(u.getUsername())
                .role(u.getRole())
                .sessionId(session.getId())
                .lastLogin(previousLogin)
                .firstLogin(firstLogin)
                .build();
    }

    /**
     * Read current user/session info (for /me).
     */
    public AuthResultDomain currentUser(HttpServletRequest req) {
        var ctx  = SecurityContextHolder.getContext();
        var auth = ctx.getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null; // controller returns 401
        }

        var session = req.getSession(false);
        String username = auth.getName();

        UserAuthDomain u = ENTITY_TO_DOMAIN_MAPPER_INSTANCE.userAuthToDomain(users.findByUsername(username).orElseThrow());
        List<String> authorities = auth.getAuthorities().stream().map(Object::toString).toList();
        String sessionId = (session != null) ? session.getId() : null;

        return AuthResultDomain.builder()
                .userId(u.getUserId())
                .username(u.getUsername())
                .sessionId(sessionId)
                .firstLogin(false)
                .lastLogin(u.getLastLogin())
                .role(u.getRole())
                .build();
    }
}
