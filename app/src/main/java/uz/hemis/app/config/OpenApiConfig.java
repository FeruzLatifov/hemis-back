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
 * 2. Tag Hierarchy (70 kategoriya - endpoint_tester.html bilan mos)
 *    - Raqamlangan kategoriyalar (01-70)
 *    - endpoint_tester.html bilan sinxronlashtirilgan
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

    @Value("${hemis.swagger.server-url:http://localhost:8082}")
    private String swaggerServerUrl;

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

                ## 🏷️ API Tags (70 kategoriya)

                APIs are organized by **numbered categories** (01-70):

                **01-15:** Token, Captcha, Passport, Talaba, O'qituvchi, Xodim, OTM, Klassifikatorlar
                **16-30:** Ilmiy doktorantlar, Dissertatsiya, Loyihalar, Nashrlar, Mualliflar
                **31-44:** Akademik hisobotlar, Inspeksiya, Statistika
                **48-57:** Mehnat, Fakultetlar, Guruhlar, Mail, DTM, OAK
                **58-70:** UzASBO, Soliq, Ijtimoiy himoya, Stipendiya, Billing, BIMM, Sertifikat

                Barcha kategoriyalar **endpoint_tester.html** bilan sinxronlashtirilgan.

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
        return List.of(
            new Server()
                .url(swaggerServerUrl)
                .description("🔗 Configured Base URL")
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

                        **MUHIM:** Swagger UI orqali to'g'ridan-to'g'ri token olish imkoni yo'q!
                        Sabab: Token endpoint Basic Authorization header talab qiladi (old-hemis compatibility).

                        **Token olish uchun Postman yoki curl ishlating:**
                        ```bash
                        curl -X POST "http://localhost:8081/app/rest/v2/oauth/token" \\
                          -H "Authorization: Basic Y2xpZW50OnNlY3JldA==" \\
                          -H "Content-Type: application/x-www-form-urlencoded" \\
                          -d "grant_type=password&username=YOUR_USERNAME&password=YOUR_PASSWORD"
                        ```

                        **Javobdan access_token ni oling va Swagger UI da Authorize bosib kiriting.**

                        **Token expires in:** 30 days (legacy compatibility)
                        **Refresh token:** Use refresh_token to get new access_token
                        """)
            )
            // NOTE: OAuth2 Password Flow Swagger UI dan ishlamaydi!
            // Sabab: Endpoint Basic Authorization header talab qiladi (old-hemis compatibility),
            // lekin Swagger UI client_id/client_secret ni form body sifatida yuboradi.
            //
            // Token olish uchun Postman yoki curl ishlating:
            // curl -X POST "http://localhost:8081/app/rest/v2/oauth/token" \
            //   -H "Authorization: Basic Y2xpZW50OnNlY3JldA==" \
            //   -H "Content-Type: application/x-www-form-urlencoded" \
            //   -d "grant_type=password&username=feruz&password=your_password"
            ;
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
     * API Tags - endpoint_tester.html BILAN MOSLASHTIRILGAN
     * =====================================================
     *
     * Total: 70 kategoriya (endpoint_tester.html dan)
     * =====================================================
     */
    private List<Tag> apiTags() {
        return Arrays.asList(
            // === 01-15: ASOSIY TIZIM ===
            new Tag().name("01.Token, Foydalanuvchilar").description("OAuth2 autentifikatsiya - token olish, yangilash, foydalanuvchi boshqaruvi"),
            new Tag().name("02.Captcha").description("Captcha generatsiya va validatsiya"),
            new Tag().name("03.Passport ma'lumotlari").description("GUVD passport ma'lumotlarini olish va tekshirish"),
            new Tag().name("04.Talaba").description("Talabalar CRUD operatsiyalari"),
            new Tag().name("05.O'qituvchi").description("O'qituvchilar ma'lumotlari"),
            new Tag().name("06.Xodim lavozimlari").description("Xodim lavozim CRUD operatsiyalari"),
            new Tag().name("07.OTM bo'linmalari").description("Kafedra va bo'limlar"),
            new Tag().name("08.OTM bo'linma turlari").description("Bo'linma turlari klassifikatori"),
            new Tag().name("09.OTM xodimlari kategoriyasi").description("Xodim kategoriyalari"),
            new Tag().name("10.Talaba holati").description("Talaba holati klassifikatori"),
            new Tag().name("11.Fuqarolik holatlari").description("Fuqarolik klassifikatori"),
            new Tag().name("12.Diplomlar").description("Diplom berish va tekshirish"),
            new Tag().name("13.Klassifikatorlar").description("Umumiy klassifikatorlar"),
            new Tag().name("14.Tarjima").description("Tillar va tarjima"),
            new Tag().name("15.OTM").description("OTM asosiy ma'lumotlari"),

            // === 16-30: ILMIY FAOLIYAT ===
            new Tag().name("16.Ilmiy doktorant talabalari").description("Doktorantlar ro'yxati"),
            new Tag().name("17.Ilmiy dissertasiya himoyalari").description("Dissertatsiya himoyalari"),
            new Tag().name("18.Ilmiy faollik").description("Ilmiy faoliyat ko'rsatkichlari"),
            new Tag().name("19.Ilmiy loyihalar").description("Ilmiy loyihalar"),
            new Tag().name("20.Ilmiy loyiha meta ma'lumotlari").description("Loyiha qo'shimcha ma'lumotlari"),
            new Tag().name("21.Ilmiy loyiha ijrochilari").description("Loyiha ijrochilari"),
            new Tag().name("22.Ilmiy nashrlar").description("Ilmiy maqola va nashrlar"),
            new Tag().name("23.Ilmiy ishlanmalar").description("Ilmiy ishlanmalar"),
            new Tag().name("24.Ilmiy uslubiy nashlar").description("Uslubiy nashrlar"),
            new Tag().name("25.Ilmiy nashr mualliflari meta ma'lumotlari").description("Muallif ma'lumotlari"),
            new Tag().name("26.Ilmiy nashrlarni baholash mezonlari").description("Baholash mezonlari"),
            new Tag().name("27.Ilmiy uslubiy nashr turlari").description("Nashr turlari klassifikatori"),
            new Tag().name("28.Ilmiy doktorantura talabalari statusi").description("Doktorant holati"),
            new Tag().name("29.Ilmiy doktorantura talabalari turlari").description("Doktorant turlari"),
            new Tag().name("30.Ilmiy nashr etish hududlari turlari").description("Nashr hududlari"),

            // === 31-44: AKADEMIK HISOBOTLAR VA INSPEKSIYA ===
            new Tag().name("31.Akademik hisobotlar chetlashgan talabalar").description("Chetlashgan talabalar hisoboti"),
            new Tag().name("32.Akademik hisobotlar akademik guruhlar").description("Guruhlar hisoboti"),
            new Tag().name("33.Akademik hisobotlar fanlar").description("Fanlar hisoboti"),
            new Tag().name("34.Akademik hisobotlar o'zlashtirish").description("O'zlashtirish hisoboti"),
            new Tag().name("35.Akademik hisobotlar davomat").description("Davomat hisoboti"),
            new Tag().name("36.Shartnoma statistikasi").description("Shartnoma statistikasi"),
            new Tag().name("37.Bandlik statistikasi").description("Mehnat bozori statistikasi"),
            new Tag().name("38.Inspeksiya administrative teacher").description("O'qituvchi inspeksiyasi"),
            new Tag().name("39.Xorijiy OTMda malaka oshirish").description("Xorijda malaka oshirish"),
            new Tag().name("40.OTMda xorijiy o'qituvchilar").description("Xorijlik o'qituvchilar"),
            new Tag().name("41.Inspeksiya administrative student2 - Akademik almashinuv").description("Akademik almashinuv"),
            new Tag().name("42.Inspeksiya administrative student3 - Bitiruvchilar band bo'lishi").description("Bitiruvchilar bandligi"),
            new Tag().name("43.Inspeksiya administrative student4 - Talaba olimpiadalari").description("Olimpiadalar"),
            new Tag().name("44.Inspeksiya administrative StudentSport - Talaba sport yutuqlari").description("Sport yutuqlari"),

            // === 48-57: XIZMATLAR ===
            new Tag().name("48.Mehnat").description("Mehnat bozori xizmatlari"),
            new Tag().name("49.Fakultetlar").description("Fakultetlar xizmati"),
            new Tag().name("50.Mutaxassisliklar").description("Mutaxassisliklar xizmati"),
            new Tag().name("51.Guruhlar").description("Guruhlar xizmati"),
            new Tag().name("52.Mail").description("Email yuborish xizmati"),
            new Tag().name("53.Healthcheck").description("Tizim salomatligi tekshiruvi"),
            new Tag().name("54.Transkript").description("Transkript xizmati"),
            new Tag().name("55.DTM").description("DTM integratsiya - mandat ma'lumotlari"),
            new Tag().name("56.OAK").description("OAK integratsiya - ilmiy darajalar"),
            new Tag().name("57.Contract").description("Shartnoma xizmati"),

            // === 58-70: INTEGRATSIYALAR ===
            new Tag().name("58.UzASBO").description("UzASBO integratsiya - stipendiya"),
            new Tag().name("59.Test").description("Test endpointlar"),
            new Tag().name("60.Soliq").description("Soliq integratsiya - ijara shartnomasi"),
            new Tag().name("61.Ijtimoiy himoya").description("Ijtimoiy himoya integratsiya"),
            new Tag().name("62.Stipendiya").description("Stipendiya boshqaruvi"),
            new Tag().name("63.Billing").description("Billing va to'lov xizmatlari"),
            new Tag().name("64.OTM").description("OTM xizmatlari - talaba info"),
            new Tag().name("65.Xo'jalik hisobot").description("Xo'jalik hisobotlari"),
            new Tag().name("66.BIMM").description("BIMM integratsiya"),
            new Tag().name("67.OTM Config").description("OTM konfiguratsiyasi"),
            new Tag().name("68.Sertifikat").description("Sertifikatlar boshqaruvi"),
            new Tag().name("69.Amaliyot").description("Amaliyot boshqaruvi"),
            new Tag().name("70.Qo'shimcha xizmatlar").description("Qo'shimcha xizmatlar")
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
            .group("Web Frontend API v1")
            .displayName("Web Frontend API v1")
            .packagesToScan("uz.hemis.api.web.controller", "uz.hemis.web.controller")
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
            .pathsToMatch("/app/rest/v2/**", "/services/**", "/entities/**")
            .pathsToExclude("/actuator/**", "/error", "/api/v1/web/**")
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

    /**
     * Group 3: University New API v1
     *
     * Target Audience: Yangi university endpointlar
     *
     * Includes:
     * - /api/v1/university/** (Yangi university API'lar)
     *
     * Access URL: /swagger-ui.html?urls.primaryName=university-new
     */
    @Bean
    public GroupedOpenApi universityNewApi() {
        return GroupedOpenApi.builder()
            .group("university-new")
            .displayName("University API v1 (New)")
            .packagesToScan("uz.hemis.api.university.controller")
            .pathsToMatch("/api/v1/university/**")
            .pathsToExclude("/actuator/**", "/error")
            .addOpenApiCustomizer(openApi -> {
                openApi.info(new Info()
                    .title("University API v1")
                    .version("1.0.0")
                    .description("""
                        # University API v1

                        Yangi university endpointlar uchun zamonaviy REST API.

                        ## Authentication

                        Barcha endpoint'lar JWT token talab qiladi.
                        `Authorization: Bearer <token>`
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

    // DEFERRED: Add global response examples
    // DEFERRED: Add error code dictionary
    // DEFERRED: Add request/response validators
}
