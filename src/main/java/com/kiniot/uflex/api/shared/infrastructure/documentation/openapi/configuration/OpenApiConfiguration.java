package com.kiniot.uflex.api.shared.infrastructure.documentation.openapi.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    // General Info
    @Value("${documentation.application.title}")
    private String applicationTitle;

    @Value("${documentation.application.description}")
    private String applicationDescription;

    @Value("${documentation.application.version}")
    private String applicationVersion;

    @Bean
    public OpenAPI iamServiceOpenApi() {
        // Define the security scheme for JWT
        var jwtSecurityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        // Define the security requirement
        var securityRequirement =  new SecurityRequirement()
                .addList("Bearer Authentication");

        // Configure API information
        var info = new Info()
                .title(applicationTitle)
                .description(applicationDescription)
                .version(applicationVersion);

        return new OpenAPI()
                .openapi("3.1.0")
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", jwtSecurityScheme));
    }
}