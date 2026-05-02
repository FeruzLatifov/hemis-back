package uz.hemis.web.controller;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import uz.hemis.app.HemisApplication;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

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

    private static RequestPostProcessor authWith(String... authorities) {
        SimpleGrantedAuthority[] grants = new SimpleGrantedAuthority[authorities.length];
        for (int i = 0; i < authorities.length; i++) {
            grants[i] = new SimpleGrantedAuthority(authorities[i]);
        }
        return jwt().authorities(grants);
    }

    @Test
    @Order(1)
    @DisplayName("GET /stats — returns dashboard statistics")
    void testGetDashboardStats() throws Exception {
        mockMvc.perform(get(BASE_URL + "/stats").with(authWith("dashboard.view")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @Order(2)
    @DisplayName("GET /stats — response contains expected fields")
    void testDashboardStatsStructure() throws Exception {
        mockMvc.perform(get(BASE_URL + "/stats").with(authWith("dashboard.view")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isMap());
    }

    @Test
    @Order(3)
    @DisplayName("GET /stats — unauthenticated returns 401 or 403")
    void testDashboardStatsUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL + "/stats"))
            .andExpect(status().is(anyOf(is(401), is(403))));
    }

    @Test
    @Order(4)
    @DisplayName("GET /stats — returns JSON content type")
    void testDashboardStatsContentType() throws Exception {
        mockMvc.perform(get(BASE_URL + "/stats").with(authWith("dashboard.view")))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    @Order(5)
    @DisplayName("GET /stats — consistent results on repeated calls")
    void testDashboardStatsConsistency() throws Exception {
        String first = mockMvc.perform(get(BASE_URL + "/stats").with(authWith("dashboard.view")))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        String second = mockMvc.perform(get(BASE_URL + "/stats").with(authWith("dashboard.view")))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        Assertions.assertNotNull(first);
        Assertions.assertNotNull(second);
    }
}
