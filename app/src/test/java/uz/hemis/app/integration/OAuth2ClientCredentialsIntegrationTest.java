package uz.hemis.app.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.auth.ClientType;
import uz.hemis.common.auth.SubjectType;
import uz.hemis.domain.entity.security.OAuthClient;
import uz.hemis.domain.entity.security.Permission;
import uz.hemis.domain.entity.security.Role;
import uz.hemis.domain.repository.OAuthClientRepository;
import uz.hemis.domain.repository.PermissionRepository;
import uz.hemis.domain.repository.RoleRepository;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end tests for {@code POST /app/rest/v2/oauth/token} with
 * {@code grant_type=client_credentials} — B2B machine account flow.
 *
 * <p>Exercises the full stack: Liquibase migration, {@link OAuthClient} JPA mapping,
 * {@code OAuthClientAuthenticationService}, {@code TokenService#issueClientToken},
 * and the dual-auth branch in {@code LegacyOAuthTokenController}.</p>
 *
 * <p>The password grant regression tests live in {@link OAuth2LoginIntegrationTest}.
 * This class focuses only on the new CLIENT subject path.</p>
 *
 * @since 2.1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("OAuth2 client_credentials grant (Phase 2)")
@EnabledIf("uz.hemis.app.integration.AbstractIntegrationTest#isDockerAvailable")
class OAuth2ClientCredentialsIntegrationTest extends AbstractIntegrationTest {

    private static final String TOKEN_ENDPOINT = "/api/v1/university/oauth/token";
    private static final String TEST_CLIENT_ID = "integration_univer_101";
    private static final String TEST_CLIENT_SECRET = "s3cret-for-int-test";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private OAuthClientRepository clientRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PermissionRepository permissionRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private OAuthClient testClient;

    @BeforeEach
    void setUp() {
        clientRepository.findByClientId(TEST_CLIENT_ID).ifPresent(clientRepository::delete);

        Permission studentsView = permission("students.view");
        Role otmApi = roleRepository.findByCode("OTM_API")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setCode("OTM_API");
                    r.setName("OTM B2B API access");
                    r.setActive(true);
                    r.setPermissions(new HashSet<>(List.of(studentsView)));
                    return roleRepository.save(r);
                });

        testClient = new OAuthClient();
        testClient.setClientId(TEST_CLIENT_ID);
        testClient.setClientSecretHash(passwordEncoder.encode(TEST_CLIENT_SECRET));
        testClient.setClientName("Integration Test OTM 101");
        testClient.setClientType(ClientType.UNIVERSITY_BACKEND);
        testClient.setGrantTypes(new ArrayList<>(List.of("client_credentials")));
        testClient.setScopes(new ArrayList<>(List.of("rest-api", "students.view")));
        testClient.setIsActive(Boolean.TRUE);
        testClient.setRateLimitRpm(60);
        testClient.setRateLimitBurst(10);
        testClient.setAccessTokenTtlSeconds(3600);
        testClient.setSecretVersion(1);
        testClient.getRoles().add(otmApi);
        testClient = clientRepository.save(testClient);
    }

    @AfterEach
    void tearDown() {
        clientRepository.findByClientId(TEST_CLIENT_ID).ifPresent(clientRepository::delete);
    }

    // =====================================================
    // Happy path
    // =====================================================

    @Test
    @DisplayName("Valid client_id + secret → 200 with machine JWT (full granted scope)")
    @Transactional
    void validCredentials_returnsMachineToken() throws Exception {
        MvcResult result = mockMvc.perform(post(TOKEN_ENDPOINT)
                        .header("Authorization", basic(TEST_CLIENT_ID, TEST_CLIENT_SECRET))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "client_credentials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token", notNullValue()))
                .andExpect(jsonPath("$.token_type").value("bearer"))
                .andExpect(jsonPath("$.expires_in").value(3600))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = objectMapper.readValue(body, Map.class);
        String accessToken = (String) payload.get("access_token");
        assertThat(accessToken).startsWith("eyJ");

        Map<String, Object> claims = decodeJwtClaims(accessToken);
        assertThat(claims).containsEntry("typ", SubjectType.CLIENT.name());
        assertThat(claims).containsEntry("client_type", ClientType.UNIVERSITY_BACKEND.name());
        assertThat(claims).containsEntry("username", TEST_CLIENT_ID);
        assertThat(claims.get("sub")).isEqualTo(testClient.getId().toString());
        assertThat(claims).containsKey("authorities");
        @SuppressWarnings("unchecked")
        List<String> jwtAuthorities = (List<String>) claims.get("authorities");
        // Effective = role permissions ∪ scopes
        assertThat(jwtAuthorities)
                .contains("students.view", "ROLE_OTM_API", "rest-api");

        // scope claim contains all granted scopes
        String scopeClaim = (String) claims.get("scope");
        assertThat(scopeClaim).contains("rest-api").contains("students.view");

        // Refresh token intentionally absent — machines re-issue via client_credentials.
        assertThat(payload).doesNotContainKey("refresh_token");
    }

    @Test
    @DisplayName("scope=students.view → token narrowed to single authority")
    @Transactional
    void requestedScope_narrowsToken() throws Exception {
        MvcResult result = mockMvc.perform(post(TOKEN_ENDPOINT)
                        .header("Authorization", basic(TEST_CLIENT_ID, TEST_CLIENT_SECRET))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "client_credentials")
                        .param("scope", "students.view"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scope").value("students.view"))
                .andReturn();

        String accessToken = (String) objectMapper
                .readValue(result.getResponse().getContentAsString(), Map.class)
                .get("access_token");
        Map<String, Object> claims = decodeJwtClaims(accessToken);
        @SuppressWarnings("unchecked")
        List<String> authorities = (List<String>) claims.get("authorities");
        // Narrowed: only the requested scope, not the umbrella 'rest-api'
        assertThat(authorities).containsExactly("students.view");
    }

    @Test
    @DisplayName("Requested scope outside granted set → 400 invalid_scope")
    void ungrantedScope_returns400() throws Exception {
        mockMvc.perform(post(TOKEN_ENDPOINT)
                        .header("Authorization", basic(TEST_CLIENT_ID, TEST_CLIENT_SECRET))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "client_credentials")
                        .param("scope", "students.delete"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_scope"));
    }

    // =====================================================
    // Negative paths
    // =====================================================

    @Test
    @DisplayName("Invalid secret → 401 invalid_client")
    void invalidSecret_returns401() throws Exception {
        mockMvc.perform(post(TOKEN_ENDPOINT)
                        .header("Authorization", basic(TEST_CLIENT_ID, "wrong-secret"))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "client_credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid_client"));
    }

    @Test
    @DisplayName("Unknown client_id → 401 invalid_client")
    void unknownClient_returns401() throws Exception {
        mockMvc.perform(post(TOKEN_ENDPOINT)
                        .header("Authorization", basic("nonexistent_client", "whatever"))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "client_credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid_client"));
    }

    @Test
    @DisplayName("Missing Basic auth header → 401 invalid_client")
    void missingBasicAuth_returns401() throws Exception {
        mockMvc.perform(post(TOKEN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "client_credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid_client"));
    }

    @Test
    @DisplayName("Inactive client → 401 invalid_client")
    @Transactional
    void inactiveClient_returns401() throws Exception {
        testClient.setIsActive(Boolean.FALSE);
        clientRepository.saveAndFlush(testClient);

        mockMvc.perform(post(TOKEN_ENDPOINT)
                        .header("Authorization", basic(TEST_CLIENT_ID, TEST_CLIENT_SECRET))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "client_credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid_client"));
    }

    @Test
    @DisplayName("Grant not in client.grant_types → 400 unsupported_grant_type")
    @Transactional
    void grantTypeNotAllowed_returns400() throws Exception {
        testClient.setGrantTypes(new ArrayList<>(List.of("password")));
        clientRepository.saveAndFlush(testClient);

        mockMvc.perform(post(TOKEN_ENDPOINT)
                        .header("Authorization", basic(TEST_CLIENT_ID, TEST_CLIENT_SECRET))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "client_credentials"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("unsupported_grant_type"));
    }

    @Test
    @DisplayName("IP not in whitelist → 401 invalid_client")
    @Transactional
    void ipNotWhitelisted_returns401() throws Exception {
        // Restrict to an address the test servlet can never present.
        testClient.setAllowedIpCidr(new ArrayList<>(List.of("203.0.113.42/32")));
        clientRepository.saveAndFlush(testClient);

        mockMvc.perform(post(TOKEN_ENDPOINT)
                        .header("Authorization", basic(TEST_CLIENT_ID, TEST_CLIENT_SECRET))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "client_credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid_client"));
    }

    // =====================================================
    // Helpers
    // =====================================================

    private static String basic(String id, String secret) {
        return "Basic " + Base64.getEncoder().encodeToString((id + ":" + secret).getBytes());
    }

    private Permission permission(String code) {
        return permissionRepository.findByCode(code).orElseGet(() -> {
            Permission p = new Permission();
            p.setCode(code);
            p.setName(code);
            p.setDescription("Integration: " + code);
            return permissionRepository.save(p);
        });
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> decodeJwtClaims(String jwt) throws Exception {
        String[] parts = jwt.split("\\.");
        assertThat(parts).hasSize(3);
        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
        return objectMapper.readValue(payloadJson, Map.class);
    }
}
