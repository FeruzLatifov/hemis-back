package uz.hemis.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
 * 2. Tag Hierarchy (~83 kategoriya - endpoint_tester.html bilan mos)
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

    /**
     * Application version — Spring Boot Gradle plugin'i tomonidan jar MANIFEST'ga yoziladi
     * (Implementation-Version). Build vaqtida `gradle.properties` versiyasi avtomatik kiradi.
     * Dev (IDE'dan run) uchun fallback "dev".
     */
    private final String applicationVersion = Optional.ofNullable(
            OpenApiConfig.class.getPackage().getImplementationVersion()
    ).orElse("dev");

    @Value("${hemis.swagger.server-url:}")
    private String swaggerServerUrl;

    // =====================================================
    // Main OpenAPI Configuration
    // =====================================================

    @Bean
    public OpenAPI hemisOpenAPI() {
        // DIQQAT: `.tags(apiTags())` chaqirig'i olib tashlandi.
        // Sabab: 70 ta numbered tag faqat api-legacy uchun. Global Bean'ga
        // o'rnatilsa, web/university/external group'lariga ham meros qoladi
        // (foydalanuvchi UI'da bo'sh "01.Token" — "70.Qo'shimcha" tag'larini ko'radi).
        // Endi har group o'z customizer'ida o'ziga mos tag'larni o'rnatadi:
        //   - legacyApi → setTags(apiTags()) — 70 ta numbered
        //   - web/university/external → controller @Tag annotatsiyasidan avto-discover
        return new OpenAPI()
            .info(apiInfo())
            .servers(apiServers())
            .components(apiComponents())
            .security(apiSecurity());
    }

    /**
     * API Information
     */
    private Info apiInfo() {
        return new Info()
            .title(applicationName + " Backend API Documentation")
            .version(applicationVersion)
            .description("""
                # HEMIS - Higher Education Management Information System

                ## Overview

                Markaziy vazirlik server (Oliy ta'lim vazirligi).

                - **Stack:** Spring Boot 4.0.6, Java 25 LTS, PostgreSQL 18, Redis 7
                - **Mijozlar:** 230 OTM (224 Univer + 6 markaziy admin) + davlat sistemalari
                - **Endpoints:** 780+ REST endpoint
                - **Backward compatibility:** api-legacy 175/175 contract test (old-hemis CUBA 7.3)

                ---

                ## API Groups

                | Group | URL | Mijoz |
                |-------|-----|-------|
                | **web** | `/api/v1/web/**` | Markaziy React UI (vazirlik admin + UNIVERSITY_ADMIN) |
                | **legacy** | `/app/rest/v2/**` | 224 ta OTM Univer (Yii2 PHP, CUBA legacy) |
                | **university** | `/api/v1/university/**` | 224 ta OTM Univer (yangi OAuth 2.1) |
                | **external** | `/api/v1/external/**` | Davlat sistemalari (MyGov, MSPD, GUVD, BIMM, Tax) |

                Group tanlash uchun yuqoridagi dropdown'dan foydalaning.

                ---

                ## Authentication

                ### Web Frontend (cookie + JWT)

                ```bash
                POST /api/v1/web/auth/login
                Content-Type: application/json

                {"username": "user", "password": "pass"}
                ```

                Javob HTTPOnly cookie'da access/refresh token o'rnatadi.

                ### Univer Legacy (CUBA Basic + Bearer)

                ```bash
                POST /app/rest/v2/oauth/token
                Authorization: Basic Y2xpZW50OnNlY3JldA==
                Content-Type: application/x-www-form-urlencoded

                grant_type=password&username=USER&password=PASS
                ```

                ### Univer/External (OAuth 2.1 client_credentials)

                ```bash
                POST /api/v1/university/oauth/token
                Content-Type: application/x-www-form-urlencoded

                grant_type=client_credentials&client_id=CID&client_secret=SECRET
                ```

                **Token TTL:** Access — 1 soat, Refresh — 7 kun.
                ADR-0009 (implemented): 1 soat access TTL + refresh rotation.

                Olingan token'ni "Authorize" tugmasiga kiriting (yoki `Authorization: Bearer ...` header).

                ---

                ## Error Format

                ```json
                {
                  "success": false,
                  "error": {
                    "code": "VALIDATION_ERROR",
                    "message": "Invalid PINFL format: must be 14 digits",
                    "details": [{"field": "pinfl", "code": "Pattern", "message": "..."}]
                  }
                }
                ```

                **HTTP status'lar:** 400 (validation), 401 (token), 403 (permission), 404 (not found),
                409 (conflict), 422 (business rule), 429 (rate limit), 500 (server error).

                ---

                ## API Tags

                api-legacy raqamlangan kategoriyalar (01-70) — `endpoint_tester.html` bilan sinxron.
                Yangi modullar (web, university, external) — semantik tag nomlari.
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
        // PRIMARY = relative "/": Swagger UI "Try it out" targets the SAME origin the docs page was
        // loaded from — localhost:8081, a LAN IP (172.18.x:8081), or a public domain — so it works
        // no matter which host opened the docs, with no cross-origin/CORS failure. A hard-coded
        // absolute URL is what made remote hosts fail ("Failed to fetch"), since the browser then
        // hit its OWN localhost. Swagger UI defaults to servers[0], so the relative entry must be first.
        List<Server> servers = new java.util.ArrayList<>();
        servers.add(new Server().url("/").description("Same-origin — brauzer manzilига ergashadi"));
        // Optional SECONDARY: an explicitly pinned PUBLIC url (prod behind a domain). localhost /
        // 127.0.0.1 values are skipped on purpose — those are exactly what breaks remote access.
        if (swaggerServerUrl != null && !swaggerServerUrl.isBlank()
                && !swaggerServerUrl.contains("localhost") && !swaggerServerUrl.contains("127.0.0.1")) {
            servers.add(new Server().url(swaggerServerUrl).description("🔗 Configured Base URL"));
        }
        return servers;
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
                        JWT Bearer Token Authentication.

                        Token olish:
                        - Web (cookie): `POST /api/v1/web/auth/login` (JSON body)
                        - Univer Legacy: `POST /app/rest/v2/oauth/token` (Basic header — `basicAuth` scheme orqali)
                        - Univer/External: `POST /api/v1/{university|external}/oauth/token` (client_credentials)

                        **TTL:** Access — 1 soat, Refresh — 7 kun (ADR-0009 implemented).
                        """)
            )
            .addSecuritySchemes("basicAuth",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("basic")
                    .description("""
                        HTTP Basic Authentication — CUBA legacy `/app/rest/v2/oauth/token` uchun.

                        Format: `Authorization: Basic base64(client_id:client_secret)`

                        Faqat token olish endpoint'ida ishlatiladi. Boshqa endpoint'lar `bearerAuth` (JWT) talab qiladi.
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
     * API Tags - endpoint_tester.html BILAN MOSLASHTIRILGAN
     * =====================================================
     *
     * Total: ~83 kategoriya (endpoint_tester.html dan)
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
            new Tag().name("70.Qo'shimcha xizmatlar").description("Qo'shimcha xizmatlar"),

            // ─── ADR-0011 sync: api-legacy controller @Tag annotation'lardan
            //     keladi, ammo bu numbered ro'yxat default tartiblash uchun
            //     kerak. Sub-numbered tag'lar (07., 08., …) controller'larda
            //     parallel mavjud — Swagger UI alphabetic sort qiladi.
            new Tag().name("07.Doktorant").description("Doktorant talabalar boshqaruvi"),
            new Tag().name("08.O'quv reja").description("O'quv reja va kurikulum"),
            new Tag().name("08.Yuridik shaxs").description("Yuridik shaxslar (GNK integratsiya)"),
            new Tag().name("10.Imtihonlar").description("Imtihon va baholash boshqaruvi"),
            new Tag().name("11.Fanlar").description("Fan va kurslar"),
            new Tag().name("12.Dars jadvali").description("Dars jadvali va davomat"),
            new Tag().name("14.GUVD").description("GUVD passport integratsiyasi"),
            new Tag().name("30.Inspeksiya").description("Inspeksiya — talaba ro'yxati"),
            new Tag().name("32.Akademik").description("Akademik faoliyat ko'rsatkichlari"),
            new Tag().name("39.Inspeksiya administrative teacher2").description("Akademik darajalar inspeksiya"),
            new Tag().name("40.Inspeksiya administrative teacher3").description("Treninglar inspeksiya"),
            new Tag().name("62.Hokimiyat").description("Hokimiyat ma'lumot bazasi"),
            new Tag().name("63.Shaxsiy ma'lumotlar").description("Foydalanuvchi shaxsiy ma'lumotlari"),
            new Tag().name("64.Yuridik shaxslar").description("Yuridik shaxs ro'yxati"),
            new Tag().name("98.Xabarlar").description("Tizim xabarlari va notification"),
            new Tag().name("99.Test").description("Test va diagnostik endpoint'lar")
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
    public GroupedOpenApi webApi() {
        return GroupedOpenApi.builder()
            .group("web")
            .displayName("Web Frontend API")
            .packagesToScan("uz.hemis.api.web.controller", "uz.hemis.web.controller")
            .pathsToMatch("/api/v1/web/**")
            .pathsToExclude("/actuator/**", "/error")
            // Default 401/403/500 + avto summary (97 ta undocumented endpoint web ichida)
            .addOpenApiCustomizer(defaultResponsesCustomizer())
            .addOpenApiCustomizer(fallbackSummaryCustomizer())
            .addOpenApiCustomizer(openApi -> {
                // Tag'lar avtomatik discover qilinadi (controller @Tag annotatsiyalaridan).
                // Eskirgan setTags ro'yxati olib tashlandi — controller'lar @Tag(description=...)
                // bilan o'z hujjatini beradi (MenuController, I18nController, va h.k.).
                openApi.info(new Info()
                    .title("Web Frontend API")
                    .version("1.0.0")
                    .description("""
                        # Web Frontend API

                        hemis-front React dasturchilar uchun modern REST API.

                        **Auth:** JWT (cookie + Authorization header).

                        1. `POST /api/v1/web/auth/login` — token olish (HTTPOnly cookie o'rnatiladi)
                        2. Keyingi so'rovlar: cookie avtomatik yoki `Authorization: Bearer <token>`

                        Barcha endpoint'lar `ResponseWrapper<T>` formatida: `{success, message, data, error}`.
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
    public GroupedOpenApi legacyApi() {
        return GroupedOpenApi.builder()
            .group("legacy")
            .displayName("Univer Legacy API (CUBA 7.3)")
            // `/app/rest/**` — `/app/rest/v2/**` va `/app/rest/user/info` (LegacyUserInfo
            // legacy fallback) ikkalasini ham qamrab oladi.
            .pathsToMatch("/app/rest/**", "/services/**", "/entities/**")
            .pathsToExclude("/actuator/**", "/error", "/api/v1/**")
            // Default 401/403/500 + avto summary (429 ta undocumented endpoint legacy ichida)
            .addOpenApiCustomizer(defaultResponsesCustomizer())
            .addOpenApiCustomizer(fallbackSummaryCustomizer())
            .addOpenApiCustomizer(openApi -> {
                // 70 ta numbered tag (01-70) faqat shu group uchun (api-legacy controller'lari).
                // endpoint_tester.html bilan sinxron tartiblangan.
                openApi.setTags(apiTags());

                openApi.info(new Info()
                    .title("Univer Legacy API (CUBA 7.3)")
                    .version("2.0.0")
                    .description("""
                        # Univer Legacy API

                        **Mijoz:** 224 ta OTM Univer (Yii2 PHP) — old-hemis CUBA Platform 7.3 1:1 mosligi.

                        **Backward compatibility:** 175/175 contract test (compare_endpoints.js).

                        ## Authentication

                        Token endpoint Basic Authorization header talab qiladi (CUBA legacy):

                        1. `POST /app/rest/v2/oauth/token` — `Authorization: Basic <client:secret>` + form body (grant_type, username, password)
                        2. Keyingi so'rovlarda: `Authorization: Bearer <access_token>`

                        ## Response Format

                        CUBA Platform shartnomasi:
                        - `LinkedHashMap<String, Object>` (field order saqlanadi)
                        - `_entityName`, `_instanceName` xizmatchi maydonlar
                        - FK serializatsiya: nested object
                        - Pagination: `{offset, limit, data, totalCount}`
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
    public GroupedOpenApi universityApi() {
        return GroupedOpenApi.builder()
            .group("university")
            .displayName("Univer API v1 (OAuth 2.1)")
            .packagesToScan("uz.hemis.api.university.controller")
            .pathsToMatch("/api/v1/university/**")
            .pathsToExclude("/actuator/**", "/error")
            // Default 401/403/500 + avto summary (4 ta undocumented endpoint university ichida)
            .addOpenApiCustomizer(defaultResponsesCustomizer())
            .addOpenApiCustomizer(fallbackSummaryCustomizer())
            .addOpenApiCustomizer(openApi -> {
                // Best-practice: YAGONA professional auth yo'li — OAuth2 client_credentials flow.
                // Swagger "Authorize"da client_id (login, masalan otm401) + client_secret (parol)
                // kiritilsa, token /oauth/token'dan AVTOMAT olinadi va barcha so'rovga Bearer bo'lib
                // qo'shiladi — qo'lda Basic/base64 yoki token copy-paste KERAK EMAS. Token endpoint'i
                // @Hidden (chalkash qo'lbola forma yo'q). Data endpointlar @SecurityRequirement(oauth2).
                if (openApi.getComponents() == null) {
                    openApi.setComponents(new Components());
                }
                openApi.getComponents().addSecuritySchemes("oauth2", new SecurityScheme()
                        .type(SecurityScheme.Type.OAUTH2)
                        .description("OTM client_credentials — **client_id** = Login, "
                                + "**client_secret** = Parol. Authorize bosing: Swagger token'ni "
                                + "/oauth/token'dan avtomat oladi va har so'rovga qo'shadi.")
                        .flows(new OAuthFlows().clientCredentials(new OAuthFlow()
                                .tokenUrl("/api/v1/university/oauth/token")
                                .scopes(new Scopes()))));
                // Bu group'da Authorize dialogi FAQAT oauth2 ko'rsatsin — global bearerAuth/basicAuth
                // sxemalari bu yerda chalkashlik qiladi (OTM faqat client_credentials ishlatadi).
                if (openApi.getComponents().getSecuritySchemes() != null) {
                    openApi.getComponents().getSecuritySchemes().remove("bearerAuth");
                    openApi.getComponents().getSecuritySchemes().remove("basicAuth");
                }
                // Group-darajali security → oauth2 (global bearerAuth requirement'ini almashtiradi,
                // shunda hech bir operatsiya olib tashlangan sxemaga ishora qilmaydi).
                openApi.setSecurity(List.of(new SecurityRequirement().addList("oauth2")));
                openApi.info(new Info()
                    .title("Univer API v1 (OAuth 2.1)")
                    .version("1.0.0")
                    .description("""
                        # Univer API v1

                        **Mijoz:** 224 ta OTM Univer — yangi REST integratsiya (ADR-0005).

                        **Auth:** OAuth 2.1 client_credentials per-OTM (`client_id` + secret + IP whitelist).

                        ---

                        ## Token olish — Swagger'da (tavsiya etiladi)

                        1. Yuqoridagi yashil **Authorize** tugmasini bosing.
                        2. **client_id** = Login, **client_secret** = Parol.
                        3. **Authorize** → Swagger token'ni avtomat oladi va har so'rovga
                           `Authorization: Bearer ...` bo'lib qo'shadi. Boshqa hech narsa kiritilmaydi.

                        ## Token olish — dastur uchun

                        ```bash
                        curl -X POST "https://api-test.hemis.uz/api/v1/university/oauth/token" \\
                          -H "Content-Type: application/x-www-form-urlencoded" \\
                          -d "grant_type=client_credentials&client_id=<Login>&client_secret=<Parol>"
                        # → {"access_token":"eyJ...","token_type":"bearer","expires_in":3600}
                        ```

                        Keyingi so'rovlarda: `Authorization: Bearer <access_token>`.
                        """)
                    .contact(new Contact()
                        .name("HEMIS Development Team")
                        .email("support@hemis.uz"))
                );
            })
            .build();
    }

    /**
     * Group 4: Davlat tashkilotlari API (S2S)
     *
     * Target Audience: Davlat sistemalari (MyGov, MSPD, GUVD, BIMM, Tax)
     *
     * Includes:
     * - /api/v1/external/oauth/token (S2S OAuth client_credentials)
     * - /api/v1/external/** (kelajakdagi S2S endpointlar)
     *
     * Access URL: /swagger-ui.html?urls.primaryName=external
     */
    @Bean
    public GroupedOpenApi externalApi() {
        return GroupedOpenApi.builder()
            .group("external")
            .displayName("Davlat tashkilotlari API")
            .packagesToScan("uz.hemis.api.external.controller")
            .pathsToMatch("/api/v1/external/**")
            .pathsToExclude("/actuator/**", "/error")
            // Default 401/403/500 + avto summary (2 ta undocumented endpoint external ichida)
            .addOpenApiCustomizer(defaultResponsesCustomizer())
            .addOpenApiCustomizer(fallbackSummaryCustomizer())
            .addOpenApiCustomizer(openApi -> {
                openApi.info(new Info()
                    .title("Davlat tashkilotlari API")
                    .version("1.0.0")
                    .description("""
                        # External S2S API

                        **Mijoz:** Davlat sistemalari (MyGov, MSPD, GUVD, BIMM, Tax/Soliq).

                        **Auth:** OAuth 2.1 client_credentials + IP whitelist (S2S only).

                        ## Authentication

                        1. `POST /api/v1/external/oauth/token` — client_credentials grant
                        2. Keyingi so'rovlarda: `Authorization: Bearer <access_token>`
                        """)
                    .contact(new Contact()
                        .name("HEMIS Development Team")
                        .email("support@hemis.uz"))
                );
            })
            .build();
    }

    // =====================================================
    // OpenAPI Customizers
    // =====================================================

    /**
     * Har bir endpoint'ga avtomatik 401/403/500 default ApiResponse'larni qo'shadi.
     *
     * Maqsad: manuel `@ApiResponses` yozmasdan ham qoplama 100% bo'lishi.
     * Manual `@ApiResponse` qo'shilgan bo'lsa — saqlanadi (computeIfAbsent).
     *
     * Ushbu Customizer barcha 4 ta GroupedOpenApi'ga avtomatik qo'llaniladi
     * (springdoc default mexanizmi).
     */
    @Bean
    public OpenApiCustomizer defaultResponsesCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) return;
            openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(operation -> {
                    if (operation.getResponses() == null) return;
                    operation.getResponses().computeIfAbsent("401",
                        k -> new ApiResponse().description("Unauthorized — token yo'q yoki noto'g'ri"));
                    operation.getResponses().computeIfAbsent("403",
                        k -> new ApiResponse().description("Forbidden — ruxsat yetarli emas"));
                    operation.getResponses().computeIfAbsent("500",
                        k -> new ApiResponse().description("Internal Server Error"));
                })
            );
        };
    }

    /**
     * Method nomidan summary avtomatik generatsiya qilish (manuel `@Operation(summary=...)` yo'q paytda).
     *
     * Sabab: 500+ legacy endpoint manuel `@Operation`'siz qoldirilgan. Manuel yozish haftalar
     * ish — buning o'rniga method nomi (`operationId`) va HTTP method'dan ma'no chiqaramiz:
     *
     * - `loadStudentByPinfl` (GET) → "Get: load student by pinfl"
     * - `createGroupItem` (POST) → "Create: create group item"
     * - `updateMetaInfo` (PUT) → "Update: update meta info"
     *
     * Manuel `@Operation(summary=...)` yozilgan bo'lsa — saqlanadi (computeIfAbsent bilan).
     *
     * Aralash holat: dasturchi qachon manuel summary yozsa — avto fallback bekor qilinadi.
     */
    @Bean
    public OpenApiCustomizer fallbackSummaryCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) return;
            openApi.getPaths().forEach((path, pathItem) ->
                pathItem.readOperationsMap().forEach((httpMethod, operation) -> {
                    if (operation.getSummary() == null || operation.getSummary().isBlank()) {
                        String operationId = operation.getOperationId();
                        if (operationId != null && !operationId.isBlank()) {
                            String prefix = switch (httpMethod) {
                                case GET -> "Get";
                                case POST -> "Create";
                                case PUT -> "Update";
                                case PATCH -> "Patch";
                                case DELETE -> "Delete";
                                default -> "Action";
                            };
                            operation.setSummary(prefix + ": " + humanize(operationId));
                        } else {
                            operation.setSummary(httpMethod + " " + path);
                        }
                    }
                })
            );
        };
    }

    /**
     * camelCase yoki snake_case operationId'ni o'qilishi qulay matnga aylantiradi.
     * Misollar:
     *   "loadStudentByPinfl" → "load student by pinfl"
     *   "find_All_Groups"    → "find all groups"
     */
    private static String humanize(String operationId) {
        // Spring/Springdoc operationId'larida `_1`, `_2` suffikslar bo'lishi mumkin (overload uchun) — olib tashlash
        String cleaned = operationId.replaceAll("_\\d+$", "");
        return cleaned
            .replaceAll("([a-z])([A-Z])", "$1 $2")
            .replace("_", " ")
            .toLowerCase()
            .strip();
    }
}
