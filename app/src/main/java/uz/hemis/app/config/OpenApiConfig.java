package uz.hemis.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * =====================================================
 * HEMIS OpenAPI 3.0 Configuration - OPTIMIZED
 * =====================================================
 *
 * Senior Architecture Best Practices:
 *
 * 1. API Grouping Strategy
 *    - Frontend APIs: React adminka uchun (Modern REST)
 *    - Legacy APIs: Old-HEMIS compatible (Univer uchun)
 *    - External APIs: Tashqi tizimlar (S2S integration)
 *    - All APIs: To'liq hujjat (default)
 *
 * 2. Tag Hierarchy (12 category)
 *    - Emoji prefixes for visual clarity
 *    - Logical grouping by domain
 *    - Clear, concise names
 *
 * 3. Documentation Standards
 *    - Every endpoint has @Operation
 *    - Request/Response examples
 *    - Error code documentation
 *    - Authentication clearly explained
 *
 * =====================================================
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:HEMIS}")
    private String applicationName;

    @Value("${spring.application.version:3.0.0}")
    private String applicationVersion;

    // =====================================================
    // Main OpenAPI Configuration
    // =====================================================

    @Bean
    public OpenAPI hemisOpenAPI() {
        return new OpenAPI()
            .info(apiInfo())
            .servers(apiServers())
            .components(apiComponents())
            .security(apiSecurity())
            .tags(apiTags());
    }

    /**
     * API Information
     */
    private Info apiInfo() {
        return new Info()
            .title("HEMIS Backend API Documentation")
            .version(applicationVersion)
            .description("""
                # 🎓 HEMIS - Higher Education Management Information System

                ## 📖 Overview

                HEMIS tizimi API hujjatlari - Spring Boot 3.5.7, Java 21 LTS

                - **200+ Universities** using this API
                - **20,000+ Concurrent Users** supported
                - **170+ REST Endpoints** available
                - **100% Backward Compatible** with old-hemis

                ---

                ## 🔐 Authentication

                ### Step 1: Get Access Token

                ```bash
                POST /app/rest/v2/oauth/token
                Content-Type: application/x-www-form-urlencoded

                grant_type=password&username=your_username&password=your_password
                ```

                **Response:**
                ```json
                {
                  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                  "token_type": "Bearer",
                  "expires_in": 28800,
                  "refresh_token": "..."
                }
                ```

                ### Step 2: Use Token in Requests

                ```bash
                GET /app/rest/v2/students?pinfl=12345678901234
                Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
                ```

                **Test Token (Demo):**
                Click "Authorize" button above and paste your token.

                ---

                ## 📚 API Groups

                Use the dropdown above to filter APIs:

                | Group | Description | For |
                |-------|-------------|-----|
                | **🎯 All APIs** | Complete documentation | All developers |
                | **⚛️ Frontend APIs** | Modern REST endpoints | React/Vue developers |
                | **📦 Legacy APIs** | Old-HEMIS compatible | Univer (Yii2) |
                | **🔗 External APIs** | S2S integrations | Government systems |

                ---

                ## 🏷️ API Tags (Resource-Based)

                APIs are organized by **specific resources** (45+ tags):

                **Students:** Students | Student Status | Enrollment
                **Teachers:** Teachers | Employee Categories | Employee Jobs | Employment
                **Organization:** Departments | Faculty | Groups
                **Academic:** Courses | Specialties | Curriculum | Grades | Exams | Schedule | Attendance
                **Documents:** Diplomas | Diploma Blanks | Certificates | QR Diplomas | Transcripts
                **Universities:** Universities | University Settings | University Additional
                **Financial:** Contracts | Scholarships | Billing
                **Science:** Doctoral Students | Dissertation Defense | Research Activity | Projects | Publications
                **Reports:** Academic Reports | Economic Reports | Administrative Reports
                **Integrations:** DTM | Tax | UzASBO | OAK | Social Protection | Contract Integration
                **System:** Authentication | Classifiers | Mail | Health | Captcha | Translation | Services

                Each resource has its **own dedicated tag** for easy API discovery.

                ---

                ## ⚠️ Error Handling

                All errors return standard format:

                ```json
                {
                  "status": 400,
                  "error": "VALIDATION_ERROR",
                  "message": "Invalid PINFL format: must be 14 digits",
                  "timestamp": "2025-11-06T10:30:00Z"
                }
                ```

                **Common Error Codes:**
                - `400` - Bad Request (validation error)
                - `401` - Unauthorized (token invalid/expired)
                - `403` - Forbidden (insufficient permissions)
                - `404` - Not Found
                - `500` - Internal Server Error

                [Full Error Code Documentation →](/docs/error-codes)

                ---

                ## 📖 Additional Resources

                - [Migration Guide](https://docs.hemis.uz/migration)
                - [Authentication Tutorial](https://docs.hemis.uz/auth)
                - [Code Examples (Java, PHP, Python)](https://docs.hemis.uz/examples)
                - [Postman Collection](https://docs.hemis.uz/postman)
                - [GitHub Repository](https://github.com/hemis-uz)

                ---

                ## 📞 Support

                - **Email:** support@hemis.uz
                - **Telegram:** @hemis_support
                - **Phone:** +998 71 123 4567
                - **Working Hours:** Mon-Fri, 9:00-18:00 (UTC+5)

                """)
            .contact(new Contact()
                .name("HEMIS Development Team")
                .email("support@hemis.uz")
                .url("https://hemis.uz"))
            .license(new License()
                .name("Proprietary License")
                .url("https://hemis.uz/license"));
    }

    /**
     * API Servers (Environment-based)
     */
    private List<Server> apiServers() {
        return Arrays.asList(
            new Server()
                .url("http://localhost:8080")
                .description("🖥️ Local Development"),
            new Server()
                .url("https://test-hemis.uz")
                .description("🧪 Test Environment"),
            new Server()
                .url("https://staging-hemis.uz")
                .description("🎭 Staging Environment"),
            new Server()
                .url("https://api.hemis.uz")
                .description("🚀 Production")
        );
    }

    /**
     * API Components (Security Schemes, Schemas)
     */
    private Components apiComponents() {
        return new Components()
            .addSecuritySchemes("bearerAuth",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("""
                        JWT Bearer Token Authentication

                        **How to get token:**
                        1. POST /app/rest/v2/oauth/token with credentials
                        2. Copy access_token from response
                        3. Click "Authorize" button above
                        4. Paste token (without "Bearer" prefix)

                        **Token expires in:** 8 hours
                        **Refresh token:** Use refresh_token to get new access_token
                        """)
            );
    }

    /**
     * Global Security Requirement
     */
    private List<SecurityRequirement> apiSecurity() {
        return Arrays.asList(
            new SecurityRequirement().addList("bearerAuth")
        );
    }

    /**
     * =====================================================
     * API Tags - SPECIFIC RESOURCE-BASED TAGS
     * =====================================================
     *
     * Senior Developer Principle:
     * - Each resource gets its OWN dedicated tag
     * - No broad grouping (e.g., NOT "Academic" with 14 controllers)
     * - Clear, specific naming for easy API discovery
     * - Professional, no emojis in production
     *
     * Total: 45+ specific tags
     * =====================================================
     */
    private List<Tag> apiTags() {
        return Arrays.asList(
            // === AUTHENTICATION ===
            new Tag().name("Authentication")
                .description("Token olish, login, logout, refresh token, OAuth2"),

            // === STUDENTS ===
            new Tag().name("Students")
                .description("Talabalar CRUD: qidirish, yaratish, yangilash, o'chirish, GPA hisoblash"),
            new Tag().name("Student Status")
                .description("Talaba holati: aktiv, akademik ta'til, o'qishdan chetlashtirilgan, bitirgan"),
            new Tag().name("Enrollment")
                .description("Qabul jarayoni: arizalar, ro'yxatga olish, qabul qilish"),

            // === TEACHERS & EMPLOYEES ===
            new Tag().name("Teachers")
                .description("O'qituvchilar: shaxsiy ma'lumotlar, akademik daraja, malaka oshirish"),
            new Tag().name("Employee Categories")
                .description("Xodim toifalari: professor, dotsent, o'qituvchi, assistent"),
            new Tag().name("Employee Jobs")
                .description("Xodim lavozimlar: asosiy, o'rindosh, soatbay, ichki o'rindosh"),
            new Tag().name("Employment")
                .description("Mehnatga joylashish: shartnomalar, ish joyi, bandlik ma'lumotlari"),

            // === ORGANIZATION STRUCTURE ===
            new Tag().name("Departments")
                .description("Kafedra va bo'limlar: hierarchiya, xodimlar, fakultetga bog'lash"),
            new Tag().name("Faculty")
                .description("Fakultetlar: asosiy ma'lumotlar, dekanlar, tuzilma"),
            new Tag().name("Registry - Faculties")
                .description("Fakultetlar Reestri: lazy tree, pagination, search, export (Frontend UI API)"),
            new Tag().name("Groups")
                .description("O'quv guruhlari: talabalar ro'yxati, yo'nalish, kurs"),

            // === ACADEMIC PROCESS ===
            new Tag().name("Courses")
                .description("Fanlar: o'quv fanlari, soat hajmi, kredit, pre-requisite"),
            new Tag().name("Specialties")
                .description("Mutaxassisliklar: yo'nalishlar, shifr, ta'lim turi"),
            new Tag().name("Curriculum")
                .description("O'quv rejalari: fanlar ro'yxati, kreditslar, semestr bo'yicha taqsimot"),
            new Tag().name("Grades")
                .description("Baholar: oraliq nazorat, yakuniy nazorat, GPA"),
            new Tag().name("Exams")
                .description("Imtihonlar: jadvali, o'tkazish, natijalar"),
            new Tag().name("Schedule")
                .description("Dars jadvali: kunlik, haftalik, oylik jadval"),
            new Tag().name("Attendance")
                .description("Davomat: yo'qlama, kechikish, sababsiz qoldirish"),

            // === DOCUMENTS ===
            new Tag().name("Diplomas")
                .description("Diplomlar: berish, ro'yxat, tekshirish, ma'lumot olish"),
            new Tag().name("Diploma Blanks")
                .description("Diplom blanklari: mavjudlik, tarqatish, hisobotlar"),
            new Tag().name("Certificates")
                .description("Sertifikatlar: o'quv, malaka oshirish, tadbirlar"),
            new Tag().name("QR Diplomas")
                .description("QR kodli diplomlar: yaratish, tekshirish, validatsiya"),
            new Tag().name("Transcripts")
                .description("Akademik ma'lumotlar: baholar, kreditlar, GPA"),

            // === UNIVERSITY MANAGEMENT ===
            new Tag().name("Universities")
                .description("OTM ma'lumotlari: asosiy ma'lumotlar, rektor, joylashuv"),
            new Tag().name("University Settings")
                .description("OTM sozlamalari: semestr, akademik yil, parametrlar"),
            new Tag().name("University Additional")
                .description("OTM qo'shimcha: infratuzilma, resurslar, imkoniyatlar"),
            new Tag().name("University Department Types")
                .description("OTM bo'lim turlari: kafedra, fakultet, bo'lim, markaz"),

            // === FINANCIAL ===
            new Tag().name("Contracts")
                .description("Shartnomalar: to'lov, kontrakt, grant, kvota"),
            new Tag().name("Scholarships")
                .description("Stipendiyalar: akademik, ijtimoiy, mahsus stipendiya"),
            new Tag().name("Billing")
                .description("To'lovlar: faktura, to'lov tarixi, qarzdorlik"),

            // === SCIENCE & RESEARCH ===
            new Tag().name("Doctoral Students")
                .description("Doktorantlar: qabul, ilmiy rahbar, dissertatsiya mavzusi"),
            new Tag().name("Dissertation Defense")
                .description("Dissertatsiya himoyasi: ilmiy kengash, rad etilgan, himoya qilingan"),
            new Tag().name("Research Activity")
                .description("Ilmiy faoliyat: loyihalar, grantlar, tadqiqotlar"),
            new Tag().name("Projects")
                .description("Ilmiy loyihalar: amaliy, fundamental, xalqaro"),
            new Tag().name("Publications - Methodical")
                .description("Metodik nashrlar: darslik, o'quv qo'llanma, uslubiy ko'rsatma"),
            new Tag().name("Publications - Scientific")
                .description("Ilmiy nashrlar: maqola, monografiya, patent"),
            new Tag().name("Publication Authors")
                .description("Nashrilar mualliflari: asosiy muallif, hammuallif"),
            new Tag().name("Publication Properties")
                .description("Nashrlar xususiyatlari: impakt-faktor, indeks, rang"),

            // === LABOR & STATISTICS ===
            new Tag().name("Labor Statistics")
                .description("Mehnat statistikasi: bo'sh ish o'rinlar, bandlik darajasi"),

            // === INFRASTRUCTURE ===
            new Tag().name("ICT Equipment")
                .description("IKT jihozlar: kompyuter, server, tarmoq"),
            new Tag().name("Laboratories")
                .description("Laboratoriyalar: o'quv, ilmiy, amaliy"),
            new Tag().name("Education Materials")
                .description("O'quv materiallari: darslik, qo'llanma, multimedia"),

            // === REPORTS ===
            new Tag().name("Academic Reports")
                .description("Akademik hisobotlar: talabalar, baholar, o'zlashtirish"),
            new Tag().name("Economic Reports")
                .description("Iqtisodiy hisobotlar: moliya, xarajatlar, daromad"),
            new Tag().name("Administrative Reports - Students")
                .description("Ma'muriy hisobotlar - Talabalar: ro'yxat, statistika, monitoring"),
            new Tag().name("Administrative Reports - Employees")
                .description("Ma'muriy hisobotlar - Xodimlar: shtat, ish haqi, ish vaqti"),

            // === INSPECTION ===
            new Tag().name("Inspection")
                .description("Nazorat va inspeksiya: tekshirish, tavsiyalar, xulosalar"),

            // === EXTERNAL INTEGRATIONS ===
            new Tag().name("DTM Integration")
                .description("DTM integratsiya: passport ma'lumotlari, jismoniy shaxs"),
            new Tag().name("Tax Integration")
                .description("Soliq integratsiya: INN, korxona, soliq to'lovchi"),
            new Tag().name("Contract Integration")
                .description("Shartnoma integratsiya: ro'yxat, holat, to'lov"),
            new Tag().name("UzASBO Integration")
                .description("UzASBO integratsiya: ta'lim muassasalari reestri"),
            new Tag().name("OAK Integration")
                .description("OAK integratsiya: ilmiy darajalar, dissertatsiyalar"),
            new Tag().name("Social Protection")
                .description("Ijtimoiy himoya: imtiyozlar, nafaqalar, to'lovlar"),

            // === UTILITIES & SYSTEM ===
            new Tag().name("Classifiers")
                .description("Klassifikatorlar: davlat, shahar, tuman, til, millat"),
            new Tag().name("Citizenship")
                .description("Fuqarolik: O'zbekiston, chet el fuqarolari"),
            new Tag().name("Mail")
                .description("Pochta xizmati: email yuborish, xabarnomalar"),
            new Tag().name("Health")
                .description("Health check: tizim holati, database, xotira"),
            new Tag().name("Captcha")
                .description("Captcha: yaratish, tekshirish, validatsiya"),
            new Tag().name("Translation")
                .description("Tarjima: til, matn, xabarlar"),
            new Tag().name("Services")
                .description("Xizmatlar: mijoz xizmatlari, CUBA service method'lar"),
            new Tag().name("Legacy Operations")
                .description("Legacy operatsiyalar: CUBA/JDBC direct operations, ma'lumotlar ko'chirish")
        );
    }

    // =====================================================
    // API Groups - FAQAT 2 TA!
    // =====================================================
    // KRITIK: Har bir audience faqat o'ziniki ko'radi!
    // 1. Web Frontend dasturchilar → /api/v1/web/**
    // 2. Universitet dasturchilar → /app/rest/v2/**
    // =====================================================

    /**
     * Group 1: Web Frontend API (hemis-front)
     *
     * Target Audience: hemis-front dasturlash jamoasi
     *
     * Includes:
     * - /api/v1/web/auth/** (Login, Logout, Refresh)
     * - /api/v1/web/students/** (CRUD operations)
     * - /api/v1/web/teachers/** (CRUD operations)
     * - /api/v1/web/** (All new web frontend endpoints)
     *
     * Access URL: /swagger-ui.html?urls.primaryName=web
     *
     * Note: Faqat yangi API'lar, universitet API'lari ko'rinmaydi
     */
    @Bean
    public GroupedOpenApi webFrontendApi() {
        return GroupedOpenApi.builder()
            .group("web")
            .displayName("Web Frontend API v1")
            .pathsToMatch("/api/v1/web/**")
            .pathsToExclude("/actuator/**", "/error")
            .addOpenApiCustomizer(openApi -> {
                // KRITIK: Faqat web frontend tag'larini ko'rsatamiz
                // Universitet API tag'lari ko'rinmasin!
                openApi.setTags(Arrays.asList(
                    new Tag()
                        .name("Web Authentication v1")
                        .description("hemis-front uchun authentication API - Login, Logout, Refresh, Me"),
                    new Tag()
                        .name("📊 Dashboard Statistics")
                        .description("Dashboard statistika - 30-min Redis cache, REPLICA database, 3M+ records"),
                    new Tag()
                        .name("Registry - Faculties")
                        .description("Fakultetlar Reestri - Lazy tree, pagination, search, export"),
                    new Tag()
                        .name("Registry - Universities")
                        .description("Muassasalar Reestri - Advanced filtering, export"),
                    new Tag()
                        .name("Menu API")
                        .description("Dynamic menu structure - Permission-filtered, multilingual"),
                    new Tag()
                        .name("I18n API")
                        .description("Internationalization - Message translation, bulk loading"),
                    new Tag()
                        .name("Translation Admin")
                        .description("Tizim tarjimalarini boshqarish - CRUD, cache, export"),
                    new Tag()
                        .name("Language API")
                        .description("Language management - Available languages, locale settings")
                ));

                // Sodda description - ortiqcha ma'lumotlar kerak emas
                openApi.info(new Info()
                    .title("Web Frontend API v1")
                    .version("1.0.0")
                    .description("""
                        # Web Frontend API

                        hemis-front React dasturchilar uchun modern REST API.

                        ## Authentication

                        Barcha endpoint'lar JWT token talab qiladi.

                        1. `/api/v1/web/auth/login` - Token olish
                        2. Keyingi so'rovlarda: `Authorization: Bearer <token>`
                        
                        ## Registry APIs
                        
                        - **/registry/faculties** - Fakultetlar (lazy tree)
                        - **/registry/universities** - Muassasalar (advanced filters)
                        
                        ## System APIs
                        
                        - **/menu** - Dynamic menu structure
                        - **/i18n** - Multilingual support
                        - **/admin/translations** - Translation management
                        """)
                    .contact(new Contact()
                        .name("HEMIS Development Team")
                        .email("support@hemis.uz"))
                );
            })
            .build();
    }

    /**
     * Group 2: Universitet va Tashkilotlar API (200+ OTM)
     *
     * Target Audience: Universitet IT dasturchilar, tashqi integratorlar
     *
     * Includes:
     * - /app/rest/v2/oauth/token (Authentication)
     * - /app/rest/v2/students/** (Talabalar)
     * - /app/rest/v2/teachers/** (O'qituvchilar)
     * - /app/rest/v2/departments/** (Kafedra)
     * - /app/rest/v2/diplomas/** (Diplomlar)
     * - /app/rest/v2/** (Barcha old-hemis compatible API'lar)
     *
     * Access URL: /swagger-ui.html?urls.primaryName=university
     *
     * Note:
     * - CUBA Platform backward compatible
     * - old_hemis.json dagi API'lar bilan bir xil
     * - Web frontend API'lari (/api/v1/web/**) ko'rinmaydi
     * - 200+ OTM ishlatmoqda
     */
    @Bean
    public GroupedOpenApi universityApi() {
        return GroupedOpenApi.builder()
            .group("university")
            .displayName("Universitet va Tashkilotlar API")
            .pathsToMatch("/app/rest/v2/**")
            .pathsToExclude("/actuator/**", "/error")
            .addOpenApiCustomizer(openApi -> {
                // Sodda description - ortiqcha ma'lumotlar olib tashlangan
                openApi.info(new Info()
                    .title("Universitet va Tashkilotlar API")
                    .version("2.0.0")
                    .description("""
                        # Universitet API

                        Universitet IT dasturchilar uchun REST API.

                        ## Authentication

                        Barcha endpoint'lar JWT token talab qiladi.

                        1. `/app/rest/v2/oauth/token` - Token olish
                        2. Keyingi so'rovlarda: `Authorization: Bearer <token>`
                        """)
                    .contact(new Contact()
                        .name("HEMIS Development Team")
                        .email("support@hemis.uz"))
                );
            })
            .build();
    }

    // =====================================================
    // OpenAPI Customizers (Advanced)
    // =====================================================

    // TODO: Add global response examples
    // TODO: Add error code dictionary
    // TODO: Add request/response validators
}
