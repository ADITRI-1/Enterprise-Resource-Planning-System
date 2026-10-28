package com.iiitd.erp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** API docs at /swagger-ui.html (raw spec at /v3/api-docs). */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI erpOpenApi() {
        return new OpenAPI().info(new Info()
                .title("University ERP API")
                .version("v1")
                .description("""
                        Session-cookie auth: call POST /api/auth/login first (Swagger UI keeps the cookie).
                        Then GET /api/student/me or /api/instructor/me for the id used in your URLs.

                        Demo accounts: admin1 / Admin@123, inst1 / Inst@123, stu1 / Stu@123.

                        Errors always look like {"errorCode", "message", "details"}.
                        """));
    }
}
