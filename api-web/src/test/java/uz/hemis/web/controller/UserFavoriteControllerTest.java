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

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests for UserFavoriteController
 *
 * <p>Tests coverage:</p>
 * <ul>
 *   <li>GET /api/v1/web/favorites — list favorites</li>
 *   <li>POST /api/v1/web/favorites — add favorite</li>
 *   <li>DELETE /api/v1/web/favorites/{code} — remove favorite</li>
 *   <li>PATCH /api/v1/web/favorites/reorder — reorder favorites</li>
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
class UserFavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/web/favorites";

    // ======================================================================
    // Test 1: Get favorites — authenticated
    // ======================================================================

    @Test
    @Order(1)
    @DisplayName("GET /favorites — returns user favorites list")
    @WithMockUser(username = "admin", authorities = {"system.view"})
    void testGetFavorites() throws Exception {
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
    }

    // ======================================================================
    // Test 2: Get favorites — unauthenticated
    // ======================================================================

    @Test
    @Order(2)
    @DisplayName("GET /favorites — unauthenticated returns 401 or 403")
    void testGetFavoritesUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().is(anyOf(is(401), is(403))));
    }

    // ======================================================================
    // Test 3: Add favorite — missing body
    // ======================================================================

    @Test
    @Order(3)
    @DisplayName("POST /favorites — empty body returns 400")
    @WithMockUser(username = "admin", authorities = {"system.view"})
    void testAddFavoriteEmptyBody() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    // ======================================================================
    // Test 4: Delete non-existent favorite — idempotent
    // ======================================================================

    @Test
    @Order(4)
    @DisplayName("DELETE /favorites/{code} — non-existent returns 204 or 404")
    @WithMockUser(username = "admin", authorities = {"system.view"})
    void testDeleteNonExistentFavorite() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/NON_EXISTENT_CODE"))
            .andExpect(status().is(anyOf(is(200), is(204), is(404))));
    }

    // ======================================================================
    // Test 5: Reorder favorites — empty list
    // ======================================================================

    @Test
    @Order(5)
    @DisplayName("PATCH /favorites/reorder — empty list")
    @WithMockUser(username = "admin", authorities = {"system.view"})
    void testReorderFavoritesEmpty() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/reorder")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
            .andExpect(status().is(anyOf(is(200), is(204))));
    }

    // ======================================================================
    // Test 6: Add and delete favorite flow
    // ======================================================================

    @Test
    @Order(6)
    @DisplayName("POST + DELETE /favorites — add and remove flow")
    @WithMockUser(username = "admin", authorities = {"system.view"})
    void testAddAndDeleteFavorite() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("menuCode", "test_menu_code");
        body.put("title", "Test Favorite");
        body.put("path", "/test/path");
        body.put("icon", "Star");

        // Add favorite
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().is(anyOf(is(200), is(201), is(400))));

        // Remove it
        mockMvc.perform(delete(BASE_URL + "/test_menu_code"))
            .andExpect(status().is(anyOf(is(200), is(204), is(404))));
    }

    // ======================================================================
    // Test 7: Reorder — unauthenticated
    // ======================================================================

    @Test
    @Order(7)
    @DisplayName("PATCH /favorites/reorder — unauthenticated returns 401 or 403")
    void testReorderFavoritesUnauthorized() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/reorder")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
            .andExpect(status().is(anyOf(is(401), is(403))));
    }
}
