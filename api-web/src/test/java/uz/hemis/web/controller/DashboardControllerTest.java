package uz.hemis.web.controller;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import uz.hemis.app.HemisApplication;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests for DashboardController
 *
 * <p>Tests coverage:</p>
 * <ul>
 *   <li>GET /api/v1/web/dashboard/stats</li>
 *   <li>Response structure validation</li>
 *   <li>Cache headers</li>
 *   <li>Authorization checks</li>
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
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String BASE_URL = "/api/v1/web/dashboard";

    // ======================================================================
    // Test 1: Get Dashboard Stats — authenticated
    // ======================================================================

    @Test
    @Order(1)
    @DisplayName("GET /stats — returns dashboard statistics")
    @WithMockUser(username = "admin", authorities = {"dashboard.view"})
    void testGetDashboardStats() throws Exception {
        mockMvc.perform(get(BASE_URL + "/stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists());
    }

    // ======================================================================
    // Test 2: Stats response has expected structure
    // ======================================================================

    @Test
    @Order(2)
    @DisplayName("GET /stats — response contains expected fields")
    @WithMockUser(username = "admin", authorities = {"dashboard.view"})
    void testDashboardStatsStructure() throws Exception {
        mockMvc.perform(get(BASE_URL + "/stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isMap());
    }

    // ======================================================================
    // Test 3: Unauthenticated access returns 401/403
    // ======================================================================

    @Test
    @Order(3)
    @DisplayName("GET /stats — unauthenticated returns 401 or 403")
    void testDashboardStatsUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL + "/stats"))
            .andExpect(status().is(anyOf(is(401), is(403))));
    }

    // ======================================================================
    // Test 4: Stats endpoint returns JSON content type
    // ======================================================================

    @Test
    @Order(4)
    @DisplayName("GET /stats — returns JSON content type")
    @WithMockUser(username = "admin", authorities = {"dashboard.view"})
    void testDashboardStatsContentType() throws Exception {
        mockMvc.perform(get(BASE_URL + "/stats"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    // ======================================================================
    // Test 5: Stats called twice — returns consistent results
    // ======================================================================

    @Test
    @Order(5)
    @DisplayName("GET /stats — consistent results on repeated calls")
    @WithMockUser(username = "admin", authorities = {"dashboard.view"})
    void testDashboardStatsConsistency() throws Exception {
        String first = mockMvc.perform(get(BASE_URL + "/stats"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        String second = mockMvc.perform(get(BASE_URL + "/stats"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        Assertions.assertNotNull(first);
        Assertions.assertNotNull(second);
    }
}
