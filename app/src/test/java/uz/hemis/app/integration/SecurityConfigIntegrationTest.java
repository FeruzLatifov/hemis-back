package uz.hemis.app.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@code SecurityConfig} filter chain KONTRAKTI — kim o'tadi, kim to'xtatiladi.
 *
 * <p><b>Bu sinf nimani sinaydi:</b> faqat xavfsizlik qatlamini (autentifikatsiya,
 * avtorizatsiya, CSRF, CORS). Kontroller javobining mazmuni (200 vs 404 vs 500) —
 * bu sinfning ishi EMAS, shuning uchun "o'tdi" holatlari {@link #securityPassed()}
 * bilan tekshiriladi: 401 ham, 403 ham emas. Aks holda security testi baza holatiga
 * bog'lanib qoladi va begona sabablardan yiqiladi.</p>
 *
 * <p><b>Tarixiy eslatma (2026-08-22 tuzatildi).</b> Bu sinfning oldingi tahriri
 * 9/18 yiqilib turardi va sababi security regressiyasi EMAS edi:</p>
 * <ol>
 *   <li><b>Fantom endpointlar.</b> {@code /app/rest/v2/students} va {@code /admin/dashboard}
 *       kodda hech qachon bo'lmagan — har so'rovda {@code Handler: Type = null}. Haqiqiy
 *       talaba entity endpointi — {@link #STUDENTS}
 *       ({@code StudentEntityController} {@code @RequestMapping}).</li>
 *   <li><b>CSRF ataylab YOQILGAN</b> ({@code SecurityConfig} — "SECURITY FIX #7",
 *       {@code CookieCsrfTokenRepository.withHttpOnlyFalse()}), eski test esa uni
 *       "disabled" deb hisoblardi. Endi kutilma teskari: tokensiz yozuv = 403 TO'G'RI xulq.</li>
 *   <li><b>{@code @WithMockUser} filter zanjiriga yetib bormaydi.</b> Spring Boot 4 da
 *       Boot 3 dagi {@code MockMvcSecurityConfiguration} olib tashlangan, ya'ni
 *       {@code @AutoConfigureMockMvc} endi {@code springSecurity()} ni AVTOMATIK
 *       qo'llamaydi. {@code spring-security-test} faqat {@code SecurityContextHolder}
 *       thread-local'ini to'ldiradi, {@code SecurityContextHolderFilter} esa uni bo'sh
 *       kontekst bilan qayta yozadi → mock user yo'qoladi, hamma so'rov anonymous.
 *       Yechim — {@link #setUp()} dagi qo'lda {@code .apply(springSecurity())}.</li>
 * </ol>
 *
 * <p><b>{@code @ActiveProfiles("test")} MAJBURIY.</b> Busiz sinf {@code dev} profilida
 * ko'tariladi ({@code application.yml} {@code spring.profiles.active: ${SPRING_PROFILES_ACTIVE:dev}}),
 * u holda {@code DataSourceConfig} ({@code @Profile("!test")}) yuklanadi va uning
 * {@code liquibase} bean'i ({@code application-dev.yml} {@code spring.liquibase.enabled: true})
 * BUTUN changelog'ni dasturchining REAL ish bazasiga yuritadi. {@code runOnChange: true}
 * changeset'lar (masalan {@code M001_migrate_old_hemis_users} — u {@code users} jadvalidagi
 * {@code password}/{@code email}/{@code enabled} ustunlarini {@code sec_user} dan qayta
 * yozadi) o'zgargan bo'lsa har test yugurishida QAYTA ISHLAYDI. Ya'ni bu bitta annotatsiya
 * test tuzatishi emas — ma'lumot himoyasi.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        // JWT validatsiyasini testda o'chirish
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=",
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=",
        // CORS origin'ini testda QATTIQ belgilaymiz. SecurityConfig uni
        // @Value("${CORS_ALLOWED_ORIGINS:}") dan oladi, Gradle esa .env ning barcha
        // kalitlarini test JVM'iga uzatadi — pin qilmasak CORS assertion'lari
        // dasturchining .env qiymatiga bog'liq bo'lib qoladi.
        "CORS_ALLOWED_ORIGINS=http://localhost:3000"
})
@DisplayName("SecurityConfig — filter chain kontrakti")
class SecurityConfigIntegrationTest extends AbstractIntegrationTest {

    /** Haqiqiy CUBA talaba entity endpointi (StudentEntityController @RequestMapping). */
    private static final String STUDENTS = "/app/rest/v2/entities/hemishe_EStudent";
    private static final String STUDENT_ID = "12345678-1234-1234-1234-123456789012";

    /** SecurityConfig CORS uchun shu origin'ni ruxsat etilgan deb biladi (yuqoridagi pin). */
    private static final String ALLOWED_ORIGIN = "http://localhost:3000";
    private static final String FOREIGN_ORIGIN = "https://evil.example.com";

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    /**
     * "Xavfsizlik qatlami so'rovni o'tkazdi" — 401 ham, 403 ham emas.
     *
     * <p>Ataylab aniq statusni (200) talab qilmaydi: bu sinf security kontraktini
     * sinaydi, kontroller/baza xulqini emas. Aniq status kutilsa, test begona
     * sabablardan (bo'sh jadval, validatsiya, 404) yiqiladi va signal yo'qoladi.</p>
     */
    private static ResultMatcher securityPassed() {
        return result -> assertThat(result.getResponse().getStatus())
                .as("xavfsizlik qatlami so'rovni o'tkazishi kerak edi (401/403 emas)")
                .isNotIn(HttpStatus.UNAUTHORIZED.value(), HttpStatus.FORBIDDEN.value());
    }

    @Nested
    @DisplayName("Ochiq endpointlar")
    class PublicEndpoints {

        @Test
        @DisplayName("/actuator/health — autentifikatsiyasiz ochiq")
        void health() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("/actuator/info — autentifikatsiyasiz ochiq")
        void info() throws Exception {
            mockMvc.perform(get("/actuator/info"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Autentifikatsiya (/app/rest/v2/** → authenticated)")
    class Authentication {

        @Test
        @DisplayName("Anonim so'rov → 401")
        void anonymous() throws Exception {
            mockMvc.perform(get(STUDENTS))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser
        @DisplayName("Autentifikatsiyalangan so'rov xavfsizlik qatlamidan o'tadi")
        void authenticated() throws Exception {
            mockMvc.perform(get(STUDENTS))
                    .andExpect(securityPassed());
        }
    }

    @Nested
    @DisplayName("Avtorizatsiya (/admin/** → hasAuthority('users.manage'))")
    class Authorization {

        @Test
        @DisplayName("Anonim → 401")
        void anonymous() throws Exception {
            mockMvc.perform(get("/admin/dashboard"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(authorities = "students.view")
        @DisplayName("boshqa ruxsat egasi → 403")
        void wrongRole() throws Exception {
            mockMvc.perform(get("/admin/dashboard"))
                    .andExpect(status().isForbidden());
        }

        @Test
        // A USER token's authorities are permission codes — no ROLE_* is ever granted
        // (JwtGrantedAuthoritiesConverter), so the gate names the permission, not the role.
        @WithMockUser(authorities = "users.manage")
        @DisplayName("users.manage → xavfsizlikdan o'tadi (kontroller yo'q, 404 kutiladi)")
        void correctRole() throws Exception {
            mockMvc.perform(get("/admin/dashboard"))
                    .andExpect(securityPassed());
        }
    }

    /**
     * CSRF YOQILGAN — SecurityConfig "SECURITY FIX #7".
     *
     * <p>{@code ignoringRequestMatchers} ro'yxatida faqat token/captcha/actuator kabi
     * mashina-mashina yo'llari bor; {@link #STUDENTS} unda YO'Q. Demak bu endpointga
     * tokensiz yozuv 403 olishi TO'G'RI xulq — bu regressiya emas.</p>
     */
    @Nested
    @DisplayName("CSRF himoyasi (yoqilgan)")
    class Csrf {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("POST tokensiz → 403")
        void postWithoutToken() throws Exception {
            mockMvc.perform(post(STUDENTS)
                            .contentType("application/json")
                            .content("{\"code\":\"STU001\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("POST token bilan → CSRF to'sig'idan o'tadi")
        void postWithToken() throws Exception {
            mockMvc.perform(post(STUDENTS)
                            .with(csrf())
                            .contentType("application/json")
                            .content("{\"code\":\"STU001\"}"))
                    .andExpect(securityPassed());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("DELETE tokensiz → 403")
        void deleteWithoutToken() throws Exception {
            mockMvc.perform(delete(STUDENTS + "/{id}", STUDENT_ID))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("DELETE token bilan → CSRF to'sig'idan o'tadi")
        void deleteWithToken() throws Exception {
            mockMvc.perform(delete(STUDENTS + "/{id}", STUDENT_ID).with(csrf()))
                    .andExpect(securityPassed());
        }
    }

    @Nested
    @DisplayName("CORS")
    class Cors {

        @Test
        @DisplayName("Preflight GET — ruxsat etilgan origin uchun ochiq")
        void preflightGet() throws Exception {
            mockMvc.perform(options(STUDENTS)
                            .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name()))
                    .andExpect(status().isOk())
                    .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
                    .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS));
        }

        /**
         * DELETE CORS'da ATAYLAB ochiq — soft-delete endpointlari uchun
         * ({@code SecurityConfig#corsConfigurationSource}, "DELETE enabled for
         * soft-delete endpoints"). Eski test buni "ruxsat etilmasin" deb kutardi va
         * shu sababli yiqilardi; manba kod bilan izohi o'sha paytda ham zid edi.
         */
        @Test
        @DisplayName("Preflight DELETE — ataylab ruxsat etilgan (soft-delete)")
        void preflightDelete() throws Exception {
            mockMvc.perform(options(STUDENTS)
                            .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.DELETE.name()))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                            containsString(HttpMethod.DELETE.name())));
        }

        @Test
        @DisplayName("Notanish origin → rad etiladi")
        void foreignOrigin() throws Exception {
            mockMvc.perform(options(STUDENTS)
                            .header(HttpHeaders.ORIGIN, FOREIGN_ORIGIN)
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser
        @DisplayName("Oddiy so'rov Access-Control-Allow-Origin sarlavhasini oladi")
        void simpleRequest() throws Exception {
            mockMvc.perform(get(STUDENTS).header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN))
                    .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        }
    }
}
