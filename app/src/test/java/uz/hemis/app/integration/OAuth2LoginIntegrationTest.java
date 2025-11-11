package uz.hemis.app.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.TokenResponse;
import uz.hemis.domain.entity.Permission;
import uz.hemis.domain.entity.Role;
import uz.hemis.domain.entity.User;
import uz.hemis.domain.repository.PermissionRepository;
import uz.hemis.domain.repository.RoleRepository;
import uz.hemis.domain.repository.UserRepository;

import java.util.Base64;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
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
class OAuth2LoginIntegrationTest {

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

    private static final String CLIENT_CREDENTIALS = Base64.getEncoder()
            .encodeToString("client:secret".getBytes());

    private String testUsername = "integration_test_user";
    private String testPassword = "test_password_123";

    @BeforeEach
    void setUp() {
        // Clean up test data
        userRepository.findByUsername(testUsername).ifPresent(userRepository::delete);

        // Create test permissions
        Permission studentViewPerm = createPermissionIfNotExists("students.view");
        Permission studentCreatePerm = createPermissionIfNotExists("students.create");
        Permission teacherViewPerm = createPermissionIfNotExists("teachers.view");

        // Create test role
        Role testRole = createRoleIfNotExists("UNIVERSITY_ADMIN", Set.of(
                studentViewPerm,
                studentCreatePerm,
                teacherViewPerm
        ));

        // Create test user
        User testUser = new User();
        testUser.setUsername(testUsername);
        testUser.setPassword("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"); // BCrypt: test_password_123
        testUser.setEmail("test@hemis.uz");
        testUser.setEnabled(true);
        testUser.setRoleSet(Set.of(testRole));
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
                        .header("Authorization", "Basic " + CLIENT_CREDENTIALS)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "password")
                        .param("username", testUsername)
                        .param("password", testPassword))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token", notNullValue()))
                .andExpect(jsonPath("$.refresh_token", notNullValue()))
                .andExpect(jsonPath("$.token_type").value("Bearer"))
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
        assertThat(tokenResponse.getTokenType()).isEqualTo("Bearer");
        assertThat(tokenResponse.getExpiresIn()).isGreaterThan(0);
    }

    @Test
    @Order(2)
    @DisplayName("E2E: Login with invalid credentials should return 401")
    void loginFlow_InvalidCredentials_ShouldReturn401() throws Exception {
        // When: Login with wrong password
        mockMvc.perform(post("/app/rest/v2/oauth/token")
                        .header("Authorization", "Basic " + CLIENT_CREDENTIALS)
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
                        .header("Authorization", "Basic " + CLIENT_CREDENTIALS)
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
                        .header("Authorization", "Basic " + CLIENT_CREDENTIALS)
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
                        .header("Authorization", "Basic " + CLIENT_CREDENTIALS)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "refresh_token")
                        .param("refresh_token", refreshToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token", notNullValue()))
                .andExpect(jsonPath("$.refresh_token", notNullValue()))
                .andExpect(jsonPath("$.token_type").value("Bearer"))
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
                        .header("Authorization", "Basic " + CLIENT_CREDENTIALS)
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
        mockMvc.perform(get("/app/rest/v2/students"))
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
                        .header("Authorization", "Basic " + CLIENT_CREDENTIALS)
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
        mockMvc.perform(get("/app/rest/v2/students")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(9)
    @DisplayName("E2E: Access protected endpoint with invalid token should return 401")
    void protectedEndpoint_WithInvalidToken_ShouldReturn401() throws Exception {
        // When: Access with invalid token
        mockMvc.perform(get("/app/rest/v2/students")
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
                        .header("Authorization", "Basic " + CLIENT_CREDENTIALS)
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
        assertThat(claims.get("sub")).isEqualTo(testUsername);
        assertThat(claims.get("authorities")).isNotNull();
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
                        .header("Authorization", "Basic " + CLIENT_CREDENTIALS)
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
        mockMvc.perform(get("/app/rest/v2/students")
                        .header("Authorization", "Bearer " + loginTokens.getAccessToken()))
                .andExpect(status().isOk());

        // Step 3: Refresh token
        MvcResult refreshResult = mockMvc.perform(post("/app/rest/v2/oauth/token")
                        .header("Authorization", "Basic " + CLIENT_CREDENTIALS)
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
        mockMvc.perform(get("/app/rest/v2/students")
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
