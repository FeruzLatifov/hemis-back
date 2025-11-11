package uz.hemis.admin.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) Configuration
 *
 * API Documentation:
 * - Admin API: /app/rest/v2/admin/** (JWT authentication)
 * - Public API: /app/rest/v2/public/** (API key authentication)
 * - Integration API: /app/rest/v2/integration/** (OAuth2)
 *
 * Access:
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 * - OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml
 */
@Configuration
public class OpenApiConfig {

    @Value("${app.version:2.0.0}")
    private String appVersion;

    @Value("${app.name:HEMIS}")
    private String appName;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(apiServers())
                .components(securityComponents())
                .addSecurityItem(jwtSecurityRequirement())
                .addSecurityItem(apiKeySecurityRequirement());
    }

    /**
     * API Information
     */
    private Info apiInfo() {
        return new Info()
                .title(appName + " API Documentation")
                .version(appVersion)
                .description("""
                        # HEMIS REST API Documentation

                        Oliy ta'lim boshqaruv axborot tizimi (HEMIS) REST API.

                        ## API Endpoints

                        ### Admin Panel API
                        - **Base Path:** `/app/rest/v2/admin/**`
                        - **Authentication:** JWT Bearer Token
                        - **Purpose:** Admin panel backend
                        - **Access:** Admin users only

                        ### Public API (Universities)
                        - **Base Path:** `/app/rest/v2/public/**`
                        - **Authentication:** API Key (Header: X-API-Key)
                        - **Purpose:** University integration
                        - **Access:** 200+ universities

                        ### Integration API (External Systems)
                        - **Base Path:** `/app/rest/v2/integration/**`
                        - **Authentication:** OAuth2
                        - **Purpose:** Government systems integration
                        - **Access:** E-imzo, TIZIM, etc.

                        ## Authentication Methods

                        ### 1. JWT (Admin Panel)
                        ```
                        POST /app/rest/v2/auth/login
                        {
                          "username": "admin@tatu",
                          "password": "password"
                        }

                        Response:
                        {
                          "token": "eyJhbGciOiJIUzUxMiJ9...",
                          "refreshToken": "..."
                        }
                        ```

                        Use token in header:
                        ```
                        Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
                        ```

                        ### 2. API Key (Universities)
                        ```
                        X-API-Key: your-api-key-here
                        ```

                        Contact admin to get API key for your university.

                        ## Rate Limiting
                        - Admin API: 1000 requests/minute
                        - Public API: 100 requests/minute
                        - Integration API: 500 requests/minute

                        ## Error Handling
                        All errors follow this format:
                        ```json
                        {
                          "success": false,
                          "error": {
                            "code": "ERROR_CODE",
                            "message": "Error description",
                            "details": {}
                          },
                          "timestamp": "2025-01-28T10:00:00Z"
                        }
                        ```
                        """)
                .contact(new Contact()
                        .name("HEMIS Support")
                        .email("support@hemis.uz")
                        .url("https://hemis.uz"))
                .license(new License()
                        .name("Proprietary")
                        .url("https://hemis.uz/license"));
    }

    /**
     * API Servers
     */
    private List<Server> apiServers() {
        return List.of(
                new Server()
                        .url("http://localhost:8080")
                        .description("Local Development"),
                new Server()
                        .url("https://hemis.uz")
                        .description("Production Server"),
                new Server()
                        .url("https://staging.hemis.uz")
                        .description("Staging Server")
        );
    }

    /**
     * Security Components (Authentication Schemes)
     */
    private Components securityComponents() {
        return new Components()
                // JWT Bearer Token
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .name("bearerAuth")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT authentication for admin panel. " +
                                "Obtain token via POST /app/rest/v2/auth/login"))

                // API Key
                .addSecuritySchemes("apiKeyAuth", new SecurityScheme()
                        .name("X-API-Key")
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .description("API key for university integration. " +
                                "Contact admin to get your API key."));
    }

    /**
     * JWT Security Requirement (for @SecurityRequirement annotation)
     */
    private SecurityRequirement jwtSecurityRequirement() {
        return new SecurityRequirement().addList("bearerAuth");
    }

    /**
     * API Key Security Requirement
     */
    private SecurityRequirement apiKeySecurityRequirement() {
        return new SecurityRequirement().addList("apiKeyAuth");
    }
}
