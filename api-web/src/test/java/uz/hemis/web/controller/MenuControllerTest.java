package uz.hemis.web.controller;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import uz.hemis.app.HemisApplication;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests for MenuController
 *
 * <p>Tests coverage:</p>
 * <ul>
 *   <li>GET /api/v1/web/menu — dynamic menu</li>
 *   <li>POST /api/v1/web/menu/check-access — path access check</li>
 *   <li>POST /api/v1/web/menu/clear-cache — cache clearing</li>
 *   <li>GET /api/v1/web/menu/structure — full structure</li>
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
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String BASE_URL = "/api/v1/web/menu";

    // ======================================================================
    // Test 1: Get Menu — authenticated
    // ======================================================================

    @Test
    @Order(1)
    @DisplayName("GET /menu — returns menu structure")
    @WithMockUser(username = "admin", authorities = {"system.view"})
    void testGetMenu() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .param("locale", "uz-UZ"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists());
    }

    // ======================================================================
    // Test 2: Get Menu — unauthenticated
    // ======================================================================

    @Test
    @Order(2)
    @DisplayName("GET /menu — unauthenticated returns 401 or 403")
    void testGetMenuUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().is(anyOf(is(401), is(403))));
    }

    // ======================================================================
    // Test 3: Get Menu with different locale
    // ======================================================================

    @Test
    @Order(3)
    @DisplayName("GET /menu — different locale returns menu")
    @WithMockUser(username = "admin", authorities = {"system.view"})
    void testGetMenuWithLocale() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .param("locale", "ru-RU"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    // ======================================================================
    // Test 4: Check Access
    // ======================================================================

    @Test
    @Order(4)
    @DisplayName("POST /check-access — returns access result")
    @WithMockUser(username = "admin", authorities = {"system.view"})
    void testCheckAccess() throws Exception {
        mockMvc.perform(post(BASE_URL + "/check-access")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"path\": \"/dashboard\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    // ======================================================================
    // Test 5: Clear Cache — requires permission
    // ======================================================================

    @Test
    @Order(5)
    @DisplayName("POST /clear-cache — with permission clears cache")
    @WithMockUser(username = "admin", authorities = {"system.view"})
    void testClearCache() throws Exception {
        mockMvc.perform(post(BASE_URL + "/clear-cache"))
            .andExpect(status().isOk());
    }

    // ======================================================================
    // Test 6: Clear Cache — without permission
    // ======================================================================

    @Test
    @Order(6)
    @DisplayName("POST /clear-cache — without permission returns 403")
    @WithMockUser(username = "user", authorities = {"basic.view"})
    void testClearCacheForbidden() throws Exception {
        mockMvc.perform(post(BASE_URL + "/clear-cache"))
            .andExpect(status().isForbidden());
    }

    // ======================================================================
    // Test 7: Get Structure — requires system.menu.view
    // ======================================================================

    @Test
    @Order(7)
    @DisplayName("GET /structure — with permission returns full structure")
    @WithMockUser(username = "admin", authorities = {"system.menu.view"})
    void testGetStructure() throws Exception {
        mockMvc.perform(get(BASE_URL + "/structure")
                .param("locale", "uz-UZ"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    // ======================================================================
    // Test 8: Get Structure — without permission
    // ======================================================================

    @Test
    @Order(8)
    @DisplayName("GET /structure — without permission returns 403")
    @WithMockUser(username = "user", authorities = {"basic.view"})
    void testGetStructureForbidden() throws Exception {
        mockMvc.perform(get(BASE_URL + "/structure"))
            .andExpect(status().isForbidden());
    }
}
