package uz.hemis.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import uz.hemis.app.HemisApplication;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests for WebAuthController
 *
 * <p>Tests coverage:</p>
 * <ul>
 *   <li>POST /login — success and failure</li>
 *   <li>POST /logout</li>
 *   <li>POST /refresh</li>
 *   <li>GET /me — current user info</li>
 *   <li>POST /cache/clear</li>
 * </ul>
 */
@EnabledIfEnvironmentVariable(named = "TESTS_ENABLED", matches = "(?i)true")
@SpringBootTest(
    classes = HemisApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.datasource.master.jdbc-url=jdbc:postgresql://${DB_MASTER_HOST}:${DB_MASTER_PORT}/${DB_MASTER_NAME}",
        "spring.datasource.replica.jdbc-url=jdbc:postgresql://${DB_REPLICA_HOST:${DB_MASTER_HOST}}:${DB_REPLICA_PORT:${DB_MASTER_PORT}}/${DB_REPLICA_NAME:${DB_MASTER_NAME}}"
    }
)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WebAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/web/auth";

    // ======================================================================
    // Test 1: Login with invalid credentials
    // ======================================================================

    @Test
    @Order(1)
    @DisplayName("POST /login — invalid credentials returns 401")
    void testLoginInvalidCredentials() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "nonexistent_user");
        loginRequest.put("password", "wrong_password");

        mockMvc.perform(post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isUnauthorized());
    }

    // ======================================================================
    // Test 2: Login with empty body returns 400
    // ======================================================================

    @Test
    @Order(2)
    @DisplayName("POST /login — empty body returns 400")
    void testLoginEmptyBody() throws Exception {
        mockMvc.perform(post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    // ======================================================================
    // Test 3: Login without content type
    // ======================================================================

    @Test
    @Order(3)
    @DisplayName("POST /login — missing content type returns 415")
    void testLoginMissingContentType() throws Exception {
        mockMvc.perform(post(BASE_URL + "/login")
                .content("{\"username\":\"test\",\"password\":\"test\"}"))
            .andExpect(status().isUnsupportedMediaType());
    }

    // ======================================================================
    // Test 4: GET /me — unauthenticated
    // ======================================================================

    @Test
    @Order(4)
    @DisplayName("GET /me — unauthenticated returns 401 or 403")
    void testGetMeUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL + "/me"))
            .andExpect(status().is(anyOf(is(401), is(403))));
    }

    // ======================================================================
    // Test 5: GET /me — with mock user
    // ======================================================================

    @Test
    @Order(5)
    @DisplayName("GET /me — authenticated returns user info")
    @WithMockUser(username = "admin", authorities = {"system.view"})
    void testGetMeAuthenticated() throws Exception {
        mockMvc.perform(get(BASE_URL + "/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    // ======================================================================
    // Test 6: POST /logout — unauthenticated
    // ======================================================================

    @Test
    @Order(6)
    @DisplayName("POST /logout — unauthenticated returns 401 or 403")
    void testLogoutUnauthorized() throws Exception {
        mockMvc.perform(post(BASE_URL + "/logout"))
            .andExpect(status().is(anyOf(is(401), is(403))));
    }

    // ======================================================================
    // Test 7: POST /refresh — without token returns error
    // ======================================================================

    @Test
    @Order(7)
    @DisplayName("POST /refresh — without refresh token returns error")
    void testRefreshWithoutToken() throws Exception {
        mockMvc.perform(post(BASE_URL + "/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().is(anyOf(is(400), is(401), is(403))));
    }

    // ======================================================================
    // Test 8: POST /cache/clear — authenticated
    // ======================================================================

    @Test
    @Order(8)
    @DisplayName("POST /cache/clear — authenticated clears cache")
    @WithMockUser(username = "admin", authorities = {"system.view"})
    void testCacheClear() throws Exception {
        mockMvc.perform(post(BASE_URL + "/cache/clear"))
            .andExpect(status().isOk());
    }
}
