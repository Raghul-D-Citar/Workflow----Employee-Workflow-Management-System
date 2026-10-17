package com.workflow.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI workflowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("WorkFlow Employee Workflow API")
                        .version("v1")
                        .description("REST API for employee, leave, and task workflow management."))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter a JWT token returned by /api/auth/login as: Bearer <token>")));
    }
}
