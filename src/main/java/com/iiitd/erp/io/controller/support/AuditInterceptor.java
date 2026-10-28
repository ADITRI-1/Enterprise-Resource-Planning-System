package com.iiitd.erp.io.controller.support;

import com.iiitd.erp.persistent.entity.erp_db.AuditEntry;
import com.iiitd.erp.persistent.persistence.repository.AuditRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.OffsetDateTime;
import java.util.Set;

/**
 * Writes one audit row per state-changing API call, after the response status is known.
 * Registered first so it also records calls that later interceptors reject (e.g. maintenance).
 */
@Component
public class AuditInterceptor implements HandlerInterceptor {

    /** Set by handlers that know who is acting before authentication exists (login). */
    public static final String ACTOR_ATTRIBUTE = AuditInterceptor.class.getName() + ".actor";

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditInterceptor.class);
    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final AuditRepository audit;

    public AuditInterceptor(AuditRepository audit) {
        this.audit = audit;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (!WRITE_METHODS.contains(request.getMethod())) {
            return;
        }
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean known = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
            Object hinted = request.getAttribute(ACTOR_ATTRIBUTE);
            String actor = known ? auth.getName() : hinted != null ? hinted.toString() : "anonymous";
            String role = known ? auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst().orElse(null) : null;
            audit.save(AuditEntry.builder()
                    .at(OffsetDateTime.now())
                    .actor(truncate(actor, 100))
                    .role(role == null ? null : role.toLowerCase())
                    .method(request.getMethod())
                    .path(truncate(request.getRequestURI(), 255))
                    .status(response.getStatus())
                    .ip(truncate(request.getRemoteAddr(), 64))
                    .build());
        } catch (Exception e) {
            // Auditing must never turn a successful request into a failure.
            LOGGER.warn("Could not write audit entry for {} {}", request.getMethod(), request.getRequestURI(), e);
        }
    }

    private static String truncate(String value, int max) {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }
}
