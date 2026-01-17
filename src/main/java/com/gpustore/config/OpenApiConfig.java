package com.gpustore.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI/Swagger documentation.
 *
 * <p>Configures the OpenAPI specification for the GPU E-commerce Platform API,
 * including API metadata, contact information, and JWT bearer token security scheme.</p>
 *
 * <p>The Swagger UI is accessible at {@code /swagger-ui.html} and the API docs
 * at {@code /api-docs}.</p>
 *
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates and configures the custom OpenAPI specification bean.
     *
     * <p>Configures:</p>
     * <ul>
     *   <li>API title, version, and description</li>
     *   <li>Contact information for the GPU Store Team</li>
     *   <li>JWT Bearer authentication security scheme</li>
     * </ul>
     *
     * @return the configured {@link OpenAPI} instance
     */
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
            .info(new Info()
                .title("GPU E-commerce Platform API")
                .version("1.0.0")
                .description("REST API for GPU E-commerce Platform - Users, Authentication, Products, and Orders")
                .contact(new Contact()
                    .name("GPU Store Team")
                    .email("support@gpustore.com")))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Enter JWT token")));
    }
}
