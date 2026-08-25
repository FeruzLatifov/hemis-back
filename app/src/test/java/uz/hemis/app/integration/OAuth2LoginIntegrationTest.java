package uz.hemis.app.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.security.service.RateLimitService;
import uz.hemis.common.dto.TokenResponse;
import uz.hemis.domain.entity.security.Permission;
import uz.hemis.domain.entity.security.Role;
import uz.hemis.domain.entity.security.User;
import uz.hemis.domain.repository.PermissionRepository;
import uz.hemis.domain.repository.RoleRepository;
import uz.hemis.domain.repository.UserRepository;

import java.util.Base64;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * OAuth2 Login Flow End-to-End Integration Tests
 *
 * <p><strong>Test Strategy:</strong></p>
 * <ul>
 *   <li>@SpringBootTest - Full application context</li>
 *   <li>@AutoConfigureMockMvc - MockMvc for HTTP testing</li>
 *   <li>Real database (H2 in-memory for tests)</li>
 *   <li>@Transactional - Rollback after each test</li>
 *   <li>Real authentication flow (no mocks)</li>
 * </ul>
 *
 * <p><strong>Test Coverage:</strong></p>
 * <ul>
 *   <li>Complete login flow (password grant)</li>
 *   <li>Token refresh flow (refresh_token grant)</li>
 *   <li>Protected endpoint access with token</li>
 *   <li>Token expiration and refresh</li>
 *   <li>User permissions extraction from JWT</li>
 *   <li>OLD-HEMIS compatibility (sec_user table)</li>
 * </ul>
 *
 * @since 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("OAuth2 Login Flow End-to-End Integration Tests")
class OAuth2LoginIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    /**
     * Login rate-limiter ATAYLAB mock qilingan.
     *
     * <p>Sabab: {@code LegacyOAuthTokenController} har {@code grant_type=password}
     * so'rovida {@link RateLimitService#isAllowed(String)} ni chaqiradi, chegara esa
     * {@code RateLimitService} da 15 daqiqada 5 urinish va hisoblagich REDIS'da
     * ({@code ratelimit:login:<ip>}) yashaydi. Bundan ikkita mustaqil muammo kelib
     * chiqardi va ikkalasi ham bu sinfni 429 bilan yiqitardi:</p>
     * <ol>
     *   <li>Hisoblagich test yugurishlari orasida TOZALANMAYDI — u ephemeral emas,
     *       dasturchining lokal Redis'ida qoladi (oldingi run'dan 10 ta urinish
     *       qolgani o'lchangan: "11 attempts in 15 min window").</li>
     *   <li>Toza Redis bilan ham bu sinf BITTA run'da 8 ta password-grant yuboradi —
     *       chegaradan (5) yuqori. Ya'ni o'zini o'zi bloklaydi.</li>
     * </ol>
     *
     * <p>Qo'shimcha: {@code isAllowed} fail-closed — Redis ishlamasa {@code false}
     * qaytaradi, ya'ni Redis'siz muhitda HAR password-grant 429 bo'lardi. Mock shu
     * infratuzilma bog'liqligini butunlay olib tashlaydi.</p>
     *
     * <p><b>Bu sinf login OQIMINI sinaydi, rate-limiterni emas.</b> Rate-limiter
     * xulqi uchun alohida test kerak (hozircha yo'q — {@code RateLimitService}
     * chegaralari {@code private static final}, ya'ni konfiguratsiya orqali
     * sozlanmaydi ham).</p>
     */
    @MockitoBean
    private RateLimitService rateLimitService;

    /**
     * Legacy token endpointi uchun Basic auth.
     *
     * <p>Qiymatlar {@code application-test.yml} dagi
     * {@code hemis.security.oauth.client-id/client-secret} dan olinadi —
     * {@code LegacyOAuthTokenController#validateClientCredentials} aynan
     * {@code OAuthClientProperties} bilan solishtiradi. Ilgari bu yerda
     * qattiq yozilgan {@code "client:secret"} turardi va u konfiguratsiyaga mos
     * kelmasdi → har so'rov {@code invalid_client}. Bu ilgari ko'rinmagan, chunki
     * testlar bazaga yetib bormay turib yiqilardi.</p>
     */
    @Value("${hemis.security.oauth.client-id}")
    private String oauthClientId;

    @Value("${hemis.security.oauth.client-secret}")
    private String oauthClientSecret;

    private String clientCredentials() {
        return Base64.getEncoder()
                .encodeToString((oauthClientId + ":" + oauthClientSecret).getBytes());
    }

    private String testUsername = "integration_test_user";
    private String testPassword = "test_password_123";

    @BeforeEach
    void setUp() {
        // Rate-limiter har testda ochiq (yuqoridagi maydon izohiga qarang)
        when(rateLimitService.isAllowed(anyString())).thenReturn(true);


        // Create test permissions
        Permission studentViewPerm = createPermissionIfNotExists("students.view");
        Permission studentCreatePerm = createPermissionIfNotExists("students.create");
        Permission teacherViewPerm = createPermissionIfNotExists("teachers.view");

        // Create test role
        Role testRole = createRoleIfNotExists("OTM_API", Set.of(
                studentViewPerm,
                studentCreatePerm,
                teacherViewPerm
        ));

        // Test foydalanuvchisi — IDEMPOTENT (delete+insert EMAS).
        //
        // Sabab: eski tahrir har @BeforeEach da o'chirib qayta yaratardi. Bu ikki holatda
        // buziladi: (a) @Transactional test'dan keyin rollback eski qatorni tiklaydi,
        // (b) parallel Gradle fork'lari bitta konteynerni baham ko'rsa poyga chiqadi.
        // Ikkalasi ham "Key (username)=(integration_test_user) already exists" beradi.
        // Mavjud qatorni yangilash ikkala holatda ham xavfsiz.
        User testUser = userRepository.findByUsername(testUsername).orElseGet(User::new);
        testUser.setUsername(testUsername);
        // Parolni ilovaning O'Z encoder'i bilan hash qilamiz. Ilgari bu yerda qattiq
        // yozilgan BCrypt hash turardi va u izohda da'vo qilingan parolga MOS EMAS edi
        // (aslida "password" ning hash'i) → har login "Invalid username or password".
        testUser.setPassword(passwordEncoder.encode(testPassword));
        testUser.setEmail("test@hemis.uz");
        testUser.setEnabled(true);
        // MUTABLE set SHART: entity bazadan topilganda Hibernate kolleksiyani o'zi
        // boshqaradi va Set.of(...) (immutable) UnsupportedOperationException beradi.
        testUser.setRoles(new HashSet<>(Set.of(testRole)));
        userRepository.save(testUser);
    }

    // =====================================================
    // Login Flow Tests (Password Grant)
    // =====================================================

    @Test
    @Order(1)
    @DisplayName("E2E: Complete login flow - valid credentials should return token")
    @Transactional
    void loginFlow_ValidCredentials_ShouldReturnToken() throws Exception {
        // When: Login with valid credentials
        MvcResult result = mockMvc.perform(post("/app/rest/v2/oauth/token")
                        .header("Authorization", "Basic " + clientCredentials())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "password")
                        .param("username", testUsername)
                        .param("password", testPassword))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token", notNullValue()))
                .andExpect(jsonPath("$.refresh_token", notNullValue()))
                .andExpect(jsonPath("$.token_type").value("bearer"))  // CUBA/Univer kontrakti: kichik harf
                .andExpect(jsonPath("$.expires_in").isNumber())
                .andReturn();

        // Then: Parse token response
        String responseBody = result.getResponse().getContentAsString();
        TokenResponse tokenResponse = objectMapper.readValue(responseBody, TokenResponse.class);

        // Validate token structure
        assertThat(tokenResponse.getAccessToken()).isNotNull();
        assertThat(tokenResponse.getAccessToken()).startsWith("eyJ"); // JWT prefix
        assertThat(tokenResponse.getRefreshToken()).isNotNull();
        assertThat(tokenResponse.getRefreshToken()).startsWith("eyJ");
        // CUBA/Univer kontrakti token_type ni KICHIK harfda qaytaradi ("bearer")
        assertThat(tokenResponse.getTokenType()).isEqualTo("bearer");
        assertThat(tokenResponse.getExpiresIn()).isGreaterThan(0);
    }

    @Test
    @Order(2)
    @DisplayName("E2E: Login with invalid credentials should return 401")
    void loginFlow_InvalidCredentials_ShouldReturn401() throws Exception {
        // When: Login with wrong password
        mockMvc.perform(post("/app/rest/v2/oauth/token")
                        .header("Authorization", "Basic " + clientCredentials())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "password")
                        .param("username", testUsername)
                        .param("password", "wrong_password"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid_grant"))
                .andExpect(jsonPath("$.error_description").exists());
    }

    @Test
    @Order(3)
    @DisplayName("E2E: Login with non-existent user should return 401")
    void loginFlow_NonExistentUser_ShouldReturn401() throws Exception {
        // When: Login with non-existent user
        mockMvc.perform(post("/app/rest/v2/oauth/token")
                        .header("Authorization", "Basic " + clientCredentials())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "password")
                        .param("username", "non_existent_user")
                        .param("password", "any_password"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid_grant"));
    }

    @Test
    @Order(4)
    @DisplayName("E2E: Login with invalid client credentials should return 401")
    void loginFlow_InvalidClientCredentials_ShouldReturn401() throws Exception {
        // When: Login with wrong client credentials
        String wrongClientCreds = Base64.getEncoder().encodeToString("wrong:wrong".getBytes());

        mockMvc.perform(post("/app/rest/v2/oauth/token")
                        .header("Authorization", "Basic " + wrongClientCreds)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "password")
                        .param("username", testUsername)
                        .param("password", testPassword))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid_client"));
    }

    // =====================================================
    // Token Refresh Flow Tests
    // =====================================================

    @Test
    @Order(5)
    @DisplayName("E2E: Token refresh with valid refresh token should return new tokens")
    @Transactional
    void refreshTokenFlow_ValidToken_ShouldReturnNewTokens() throws Exception {
        // Step 1: Login to get refresh token
        MvcResult loginResult = mockMvc.perform(post("/app/rest/v2/oauth/token")
                        .header("Authorization", "Basic " + clientCredentials())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "password")
                        .param("username", testUsername)
                        .param("password", testPassword))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        TokenResponse loginTokens = objectMapper.readValue(loginResponse, TokenResponse.class);
        String refreshToken = loginTokens.getRefreshToken();

        // Step 2: Use refresh token to get new access token
        MvcResult refreshResult = mockMvc.perform(post("/app/rest/v2/oauth/token")
                        .header("Authorization", "Basic " + clientCredentials())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "refresh_token")
                        .param("refresh_token", refreshToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token", notNullValue()))
                .andExpect(jsonPath("$.refresh_token", notNullValue()))
                .andExpect(jsonPath("$.token_type").value("bearer"))  // CUBA/Univer kontrakti: kichik harf
                .andReturn();

        // Then: Validate new tokens
        String refreshResponse = refreshResult.getResponse().getContentAsString();
        TokenResponse newTokens = objectMapper.readValue(refreshResponse, TokenResponse.class);

        assertThat(newTokens.getAccessToken()).isNotNull();
        assertThat(newTokens.getAccessToken()).isNotEqualTo(loginTokens.getAccessToken()); // New access token
        assertThat(newTokens.getRefreshToken()).isNotNull();
    }

    @Test
    @Order(6)
    @DisplayName("E2E: Token refresh with invalid token should return 401")
    void refreshTokenFlow_InvalidToken_ShouldReturn401() throws Exception {
        // When: Use invalid refresh token
        mockMvc.perform(post("/app/rest/v2/oauth/token")
                        .header("Authorization", "Basic " + clientCredentials())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "refresh_token")
                        .param("refresh_token", "invalid-token-xyz"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid_grant"));
    }

    // =====================================================
    // Protected Endpoint Access Tests
    // =====================================================

    @Test
    @Order(7)
    @DisplayName("E2E: Access protected endpoint without token should return 401")
    void protectedEndpoint_WithoutToken_ShouldReturn401() throws Exception {
        // When: Access protected endpoint without token
        mockMvc.perform(get("/app/rest/v2/entities/hemishe_EStudent"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(8)
    @DisplayName("E2E: Access protected endpoint with valid token should return 200")
    @Transactional
    void protectedEndpoint_WithValidToken_ShouldReturn200() throws Exception {
        // Step 1: Login to get access token
        MvcResult loginResult = mockMvc.perform(post("/app/rest/v2/oauth/token")
                        .header("Authorization", "Basic " + clientCredentials())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "password")
                        .param("username", testUsername)
                        .param("password", testPassword))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        TokenResponse tokens = objectMapper.readValue(loginResponse, TokenResponse.class);
        String accessToken = tokens.getAccessToken();

        // Step 2: Access protected endpoint with token
        mockMvc.perform(get("/app/rest/v2/entities/hemishe_EStudent")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(9)
    @DisplayName("E2E: Access protected endpoint with invalid token should return 401")
    void protectedEndpoint_WithInvalidToken_ShouldReturn401() throws Exception {
        // When: Access with invalid token
        mockMvc.perform(get("/app/rest/v2/entities/hemishe_EStudent")
                        .header("Authorization", "Bearer invalid-token-xyz"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    // =====================================================
    // JWT Claims Tests
    // =====================================================

    @Test
    @Order(10)
    @DisplayName("E2E: JWT should contain correct user claims")
    @Transactional
    void jwtClaims_ShouldContainCorrectUserInfo() throws Exception {
        // Step 1: Login
        MvcResult loginResult = mockMvc.perform(post("/app/rest/v2/oauth/token")
                        .header("Authorization", "Basic " + clientCredentials())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "password")
                        .param("username", testUsername)
                        .param("password", testPassword))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        TokenResponse tokens = objectMapper.readValue(loginResponse, TokenResponse.class);
        String accessToken = tokens.getAccessToken();

        // Step 2: Decode JWT (base64 decode payload)
        String[] jwtParts = accessToken.split("\\.");
        assertThat(jwtParts).hasSize(3); // header.payload.signature

        String payload = new String(Base64.getUrlDecoder().decode(jwtParts[1]));
        @SuppressWarnings("unchecked")
        Map<String, Object> claims = objectMapper.readValue(payload, Map.class);

        // Validate claims
        // sub = foydalanuvchi UUID'si (login EMAS) — login alohida "username" claim'ida.
        // Eski kutilma sub'da loginni kutardi va bu hech qachon sinalmagan edi.
        // sub QIYMATI ham tekshiriladi — shunchaki "null emas" degani sub'ning
        // boshqa qiymatga almashishini o'tkazib yuborardi.
        String expectedUserId = userRepository.findByUsername(testUsername)
                .orElseThrow().getId().toString();
        assertThat(claims.get("sub")).isEqualTo(expectedUserId);
        assertThat(claims.get("username")).isEqualTo(testUsername);
// DIQQAT: password-grant JWT'sida "authorities" claim'i YO'Q va bu ATAYLAB.
        // TokenService faqat username + scope yozadi; ruxsatlar server tomonda
        // (Redis kesh: user:permissions:{userId}) hal qilinadi — token PII/huquq
        // tashimasin. Eski kutilma "authorities" ni talab qilardi va hech qachon
        // sinalmagan edi. Mashina tokeni (client_credentials) esa authorities'ni
        // O'Z ICHIGA OLADI — u boshqa oqim (OAuth2ClientCredentialsIntegrationTest).
        assertThat(claims.get("scope")).isEqualTo("rest-api");
        assertThat(claims.get("iat")).isNotNull(); // issued at
        assertThat(claims.get("exp")).isNotNull(); // expiration
    }

    // =====================================================
    // Complete Authentication Cycle Test
    // =====================================================

    @Test
    @Order(11)
    @DisplayName("E2E: Complete authentication cycle (login → access → refresh → access)")
    @Transactional
    void completeAuthenticationCycle_ShouldWork() throws Exception {
        // Step 1: Login
        MvcResult loginResult = mockMvc.perform(post("/app/rest/v2/oauth/token")
                        .header("Authorization", "Basic " + clientCredentials())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "password")
                        .param("username", testUsername)
                        .param("password", testPassword))
                .andExpect(status().isOk())
                .andReturn();

        TokenResponse loginTokens = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(),
                TokenResponse.class
        );

        // Step 2: Access protected endpoint with access token
        mockMvc.perform(get("/app/rest/v2/entities/hemishe_EStudent")
                        .header("Authorization", "Bearer " + loginTokens.getAccessToken()))
                .andExpect(status().isOk());

        // Step 3: Refresh token
        MvcResult refreshResult = mockMvc.perform(post("/app/rest/v2/oauth/token")
                        .header("Authorization", "Basic " + clientCredentials())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "refresh_token")
                        .param("refresh_token", loginTokens.getRefreshToken()))
                .andExpect(status().isOk())
                .andReturn();

        TokenResponse newTokens = objectMapper.readValue(
                refreshResult.getResponse().getContentAsString(),
                TokenResponse.class
        );

        // Step 4: Access protected endpoint with new access token
        mockMvc.perform(get("/app/rest/v2/entities/hemishe_EStudent")
                        .header("Authorization", "Bearer " + newTokens.getAccessToken()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    // =====================================================
    // Helper Methods
    // =====================================================

    private Permission createPermissionIfNotExists(String code) {
        return permissionRepository.findByCode(code)
                .orElseGet(() -> {
                    Permission perm = new Permission();
                    perm.setCode(code);
                    perm.setName(code);
                    perm.setDescription("Test permission: " + code);
                    return permissionRepository.save(perm);
                });
    }

    private Role createRoleIfNotExists(String code, Set<Permission> permissions) {
        return roleRepository.findByCode(code)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setCode(code);
                    role.setName(code);
                    role.setDescription("Test role: " + code);
                    role.setActive(true);
                    role.setPermissions(permissions);
                    return roleRepository.save(role);
                });
    }
}
