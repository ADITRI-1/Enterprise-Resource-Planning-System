package com.iiitd.erp.io.controller.support;

import com.iiitd.erp.core.service.MaintenanceService;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.Set;

@Component
public class MaintenanceReadOnlyInterceptor implements HandlerInterceptor {

    private static final Set<String> WRITE_WHITELIST = Set.of("/api/admin/settings/maintenance");

    private final MaintenanceService maintenance;

    public MaintenanceReadOnlyInterceptor(MaintenanceService maintenance) {
        this.maintenance = maintenance;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!maintenance.isReadOnly()) {
            return true;
        }

        String method = request.getMethod();
        if (!("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method) || "DELETE".equals(method))) {
            return true;
        }

        String path = request.getRequestURI();
        if (!path.startsWith("/api/student")
                && !path.startsWith("/api/instructor")
                && !path.startsWith("/api/admin")) {
            return true;
        }

        if (WRITE_WHITELIST.stream().anyMatch(path::startsWith)) {
            return true;
        }

        throw ErpException.of(ErrorCode.MAINTENANCE_WRITE_BLOCKED,
                Map.of("path", path, "method", method));
    }
}
