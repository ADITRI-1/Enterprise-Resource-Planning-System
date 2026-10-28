package com.iiitd.erp.config;

import com.iiitd.erp.io.controller.support.AuditInterceptor;
import com.iiitd.erp.io.controller.support.MaintenanceReadOnlyInterceptor;
import com.iiitd.erp.io.controller.support.PasswordChangeRequiredInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final MaintenanceReadOnlyInterceptor maintenanceInterceptor;
    private final PasswordChangeRequiredInterceptor passwordChangeInterceptor;
    private final AuditInterceptor auditInterceptor;

    public WebConfig(MaintenanceReadOnlyInterceptor maintenanceInterceptor,
                     PasswordChangeRequiredInterceptor passwordChangeInterceptor,
                     AuditInterceptor auditInterceptor) {
        this.auditInterceptor = auditInterceptor;
        this.maintenanceInterceptor = maintenanceInterceptor;
        this.passwordChangeInterceptor = passwordChangeInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(auditInterceptor).addPathPatterns("/api/**");
        registry.addInterceptor(passwordChangeInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**", "/api/system/**");
        registry.addInterceptor(maintenanceInterceptor).addPathPatterns("/api/**");
    }
}
