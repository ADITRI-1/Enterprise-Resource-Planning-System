package com.iiitd.erp.config;

import com.iiitd.erp.io.controller.support.MaintenanceReadOnlyInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final MaintenanceReadOnlyInterceptor maintenanceInterceptor;

    public WebConfig(MaintenanceReadOnlyInterceptor maintenanceInterceptor) {
        this.maintenanceInterceptor = maintenanceInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(maintenanceInterceptor).addPathPatterns("/api/**");
    }
}
