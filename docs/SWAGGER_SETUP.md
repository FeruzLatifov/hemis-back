# Swagger / OpenAPI Documentation Setup

**Maqsad:** API dokumentatsiyasini avtomatik yaratish
**Framework:** SpringDoc OpenAPI 3.0
**Sana:** 2025-11-09

---

## 🎯 Nima uchun Swagger?

### Foydalari:

1. **Avtomatik dokumentatsiya** - Kod yozilganida avtomatik
2. **Interactive testing** - Brauzerda API sinab ko'rish
3. **Frontend uchun** - API kontraktini ko'rish
4. **Postman alternative** - Alohida tool kerak emas

---

## 📦 Swagger ko'rsatkich

### Backend ishga tushgandan keyin:

```
Swagger UI: http://localhost:8081/swagger-ui.html
OpenAPI JSON: http://localhost:8081/v3/api-docs
```

### Qanday ko'rinadi:

```
┌─────────────────────────────────────────┐
│  HEMIS Backend API v2.0                 │
├─────────────────────────────────────────┤
│                                         │
│  🔐 Authentication                      │
│    POST /app/rest/v2/oauth/token       │
│    POST /app/rest/v2/oauth/refresh     │
│                                         │
│  👥 Users                               │
│    GET    /api/users                   │
│    GET    /api/users/{id}              │
│    POST   /api/users                   │
│    PUT    /api/users/{id}              │
│    DELETE /api/users/{id}              │
│                                         │
│  📊 Roles                               │
│    GET /api/roles                      │
│    GET /api/roles/{id}/permissions     │
└─────────────────────────────────────────┘
```

---

## 🛠️ Setup (Allaqachon qilingan)

### 1. Dependency (build.gradle)

```gradle
dependencies {
    // SpringDoc OpenAPI 3.0
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0'
}
```

### 2. Configuration (application.yml)

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operationsSorter: alpha
    tagsSorter: alpha
  show-actuator: false
```

### 3. OpenAPI Configuration Class

Fayl: `app/src/main/java/uz/hemis/app/config/OpenApiConfig.java`

```java
package uz.hemis.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hemisOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("HEMIS Backend API")
                .description("Higher Education Management Information System")
                .version("v2.0.0")
                .contact(new Contact()
                    .name("HEMIS Development Team")
                    .email("support@hemis.uz")
                    .url("https://hemis.uz"))
                .license(new License()
                    .name("Proprietary")
                    .url("https://hemis.uz/license")))
            .components(new Components()
                .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT token from /app/rest/v2/oauth/token")))
            .addSecurityItem(new SecurityRequirement()
                .addList("bearer-jwt"));
    }
}
```

---

## 📝 Controller Annotations

### Minimal (Automatic)

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping
    public List<User> getAllUsers() {
        // Swagger avtomatik dokumentatsiya yaratadi
        return userService.findAll();
    }
}
```

### Advanced (Custom Documentation)

```java
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management APIs")
public class UserController {

    @Operation(
        summary = "Get all users",
        description = "Retrieve all users with pagination support",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successful operation",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserDTO.class)
                )
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized - Invalid or missing JWT token"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - Insufficient permissions"
            )
        }
    )
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers(
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") int page,

        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(userService.findAll(page, size));
    }

    @Operation(summary = "Get user by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(
        @PathVariable UUID id
    ) {
        return userService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
```

---

## 🔐 Security Examples

### DTO with Swagger Annotations

```java
@Schema(description = "User data transfer object")
public class UserDTO {

    @Schema(
        description = "User UUID",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private UUID id;

    @Schema(
        description = "Username (unique)",
        example = "admin",
        required = true,
        minLength = 3,
        maxLength = 50
    )
    private String username;

    @Schema(
        description = "User's email",
        example = "admin@hemis.uz"
    )
    private String email;

    @Schema(
        description = "User roles",
        example = "[\"SUPER_ADMIN\", \"UNIVERSITY_ADMIN\"]"
    )
    private Set<String> roles;

    // Getters/Setters
}
```

---

## 🎨 Swagger UI Customization

### Custom CSS (opsional)

Fayl: `app/src/main/resources/static/swagger-ui-custom.css`

```css
/* HEMIS brand colors */
.swagger-ui .topbar {
    background-color: #0066cc;
}

.swagger-ui .info .title {
    color: #003366;
}
```

### Enable Custom CSS

```yaml
springdoc:
  swagger-ui:
    path: /swagger-ui.html
    custom-css-url: /swagger-ui-custom.css
```

---

## 📱 Swagger UI Usage

### 1. Open Swagger UI

```
http://localhost:8081/swagger-ui.html
```

### 2. Authorize (JWT Token)

1. Click "Authorize" button (🔓)
2. Login qiling va token oling:
   ```bash
   curl -X POST http://localhost:8081/app/rest/v2/oauth/token \
     -d "grant_type=password&username=admin&password=admin"
   ```
3. Token ni ko'chiring
4. "bearer-jwt" ga joylashtiring: `eyJhbGc...`
5. "Authorize" bosing

### 3. Test API

1. Endpoint tanlang (masalan, `GET /api/users`)
2. "Try it out" bosing
3. Parametrlarni kiriting
4. "Execute" bosing
5. Response ko'ring

---

## 📥 OpenAPI Export

### JSON Format

```bash
# OpenAPI 3.0 JSON
curl http://localhost:8081/v3/api-docs > hemis-api.json
```

### YAML Format

```bash
# OpenAPI 3.0 YAML
curl http://localhost:8081/v3/api-docs.yaml > hemis-api.yaml
```

### Postman Import

1. Postman ochish
2. Import → Link → `http://localhost:8081/v3/api-docs`
3. Collection yaratiladi avtomatik

---

## 🔗 API Groups

### Tag bo'yicha guruhlash

```java
@RestController
@RequestMapping("/api/students")
@Tag(name = "Students", description = "Student management endpoints")
public class StudentController {
    // ...
}

@RestController
@RequestMapping("/api/teachers")
@Tag(name = "Teachers", description = "Teacher management endpoints")
public class TeacherController {
    // ...
}
```

Swagger UI da:
```
📚 Students
  GET    /api/students
  POST   /api/students
  PUT    /api/students/{id}
  DELETE /api/students/{id}

👨‍🏫 Teachers
  GET    /api/teachers
  POST   /api/teachers
  ...
```

---

## 🎯 Best Practices

### 1. Har doim description yozing

```java
@Operation(
    summary = "Short summary",
    description = "Detailed description with examples..."
)
```

### 2. Response types aniqlang

```java
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(responseCode = "400", description = "Bad Request"),
    @ApiResponse(responseCode = "401", description = "Unauthorized"),
    @ApiResponse(responseCode = "403", description = "Forbidden"),
    @ApiResponse(responseCode = "404", description = "Not Found"),
    @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
```

### 3. Examples qo'shing

```java
@Schema(
    description = "User email",
    example = "user@example.com",
    pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
)
private String email;
```

### 4. Deprecated endpointlarni belgilang

```java
@Deprecated
@Operation(
    summary = "Get users (deprecated)",
    description = "Use GET /api/v2/users instead",
    deprecated = true
)
@GetMapping("/old-users")
public List<User> getOldUsers() {
    // ...
}
```

---

## 📊 Swagger Statistics

Loyihada dokumentatsiya:

| Resource | Endpoints | Documented |
|----------|-----------|------------|
| Authentication | 2 | ✅ Yes |
| Users | 5 | ✅ Yes |
| Roles | 3 | ✅ Yes |
| Permissions | 2 | ✅ Yes |
| Health | 1 | ✅ Yes |

---

## 🔍 Troubleshooting

### Swagger UI ochilmayapti

```bash
# 1. Backend ishlaganligini tekshir
curl http://localhost:8081/actuator/health

# 2. Swagger endpoint tekshir
curl http://localhost:8081/v3/api-docs

# 3. application.yml da enabled=true ekanligini tekshir
springdoc:
  swagger-ui:
    enabled: true
```

### 401 Unauthorized swagger'da

1. "Authorize" tugmasini bosing
2. Token kiriting (boshida `Bearer` kerak emas, faqat token)
3. Authorize bosing
4. Qayta sinab ko'ring

### Endpoint ko'rinmayapti

```java
// Controller @RestController annotation borligini tekshir
@RestController  // Bu kerak!
@RequestMapping("/api/users")
public class UserController {
    // ...
}
```

---

## 📚 Additional Resources

- [SpringDoc OpenAPI Docs](https://springdoc.org/)
- [OpenAPI 3.0 Specification](https://spec.openapis.org/oas/v3.0.0)
- [Swagger UI](https://swagger.io/tools/swagger-ui/)

---

**Status:** ✅ Swagger UI tayyor
**URL:** http://localhost:8081/swagger-ui.html
**Last Updated:** 2025-11-09
