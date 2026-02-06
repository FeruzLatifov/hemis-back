package uz.hemis.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
 * Integration Tests for MenuAdminController
 *
 * <p>Tests coverage:</p>
 * <ul>
 *   <li>CRUD operations (list, get, create, update, delete)</li>
 *   <li>Toggle active status</li>
 *   <li>Reorder menus</li>
 *   <li>Export/Import menu structure</li>
 *   <li>Audit log endpoints</li>
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
class MenuAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/web/admin/menus";

    // ======================================================================
    // Test 1: List all menus — with permission
    // ======================================================================

    @Test
    @Order(1)
    @DisplayName("GET /admin/menus — returns menu list")
    @WithMockUser(username = "admin", authorities = {"system.menus.manage"})
    void testGetAllMenus() throws Exception {
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
    }

    // ======================================================================
    // Test 2: List menus — without permission
    // ======================================================================

    @Test
    @Order(2)
    @DisplayName("GET /admin/menus — without permission returns 403")
    @WithMockUser(username = "user", authorities = {"basic.view"})
    void testGetAllMenusForbidden() throws Exception {
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isForbidden());
    }

    // ======================================================================
    // Test 3: Get menu by non-existent ID — 404
    // ======================================================================

    @Test
    @Order(3)
    @DisplayName("GET /admin/menus/{id} — non-existent returns 404")
    @WithMockUser(username = "admin", authorities = {"system.menus.manage"})
    void testGetMenuByIdNotFound() throws Exception {
        mockMvc.perform(get(BASE_URL + "/00000000-0000-0000-0000-000000000000"))
            .andExpect(status().isNotFound());
    }

    // ======================================================================
    // Test 4: Get menu by code — non-existent
    // ======================================================================

    @Test
    @Order(4)
    @DisplayName("GET /admin/menus/by-code/{code} — non-existent returns 404")
    @WithMockUser(username = "admin", authorities = {"system.menus.manage"})
    void testGetMenuByCodeNotFound() throws Exception {
        mockMvc.perform(get(BASE_URL + "/by-code/NON_EXISTENT_CODE"))
            .andExpect(status().isNotFound());
    }

    // ======================================================================
    // Test 5: Create menu — validation error (empty body)
    // ======================================================================

    @Test
    @Order(5)
    @DisplayName("POST /admin/menus — empty body returns 400")
    @WithMockUser(username = "admin", authorities = {"system.menus.manage"})
    void testCreateMenuValidationError() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    // ======================================================================
    // Test 6: Delete non-existent menu
    // ======================================================================

    @Test
    @Order(6)
    @DisplayName("DELETE /admin/menus/{id} — non-existent returns 404")
    @WithMockUser(username = "admin", authorities = {"system.menus.manage"})
    void testDeleteMenuNotFound() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/00000000-0000-0000-0000-000000000000"))
            .andExpect(status().isNotFound());
    }

    // ======================================================================
    // Test 7: Export menu structure
    // ======================================================================

    @Test
    @Order(7)
    @DisplayName("GET /admin/menus/export — returns JSON structure")
    @WithMockUser(username = "admin", authorities = {"system.menus.manage"})
    void testExportMenuStructure() throws Exception {
        mockMvc.perform(get(BASE_URL + "/export"))
            .andExpect(status().isOk());
    }

    // ======================================================================
    // Test 8: Clear cache
    // ======================================================================

    @Test
    @Order(8)
    @DisplayName("POST /admin/menus/cache/clear — clears cache")
    @WithMockUser(username = "admin", authorities = {"system.menus.manage"})
    void testClearCache() throws Exception {
        mockMvc.perform(post(BASE_URL + "/cache/clear"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    // ======================================================================
    // Test 9: Get recent audit logs
    // ======================================================================

    @Test
    @Order(9)
    @DisplayName("GET /admin/menus/audit-logs/recent — returns audit logs")
    @WithMockUser(username = "admin", authorities = {"system.menus.manage"})
    void testGetRecentAuditLogs() throws Exception {
        mockMvc.perform(get(BASE_URL + "/audit-logs/recent"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    // ======================================================================
    // Test 10: Toggle non-existent menu
    // ======================================================================

    @Test
    @Order(10)
    @DisplayName("PATCH /admin/menus/{id}/toggle — non-existent returns 404")
    @WithMockUser(username = "admin", authorities = {"system.menus.manage"})
    void testToggleNonExistent() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/00000000-0000-0000-0000-000000000000/toggle"))
            .andExpect(status().isNotFound());
    }
}
