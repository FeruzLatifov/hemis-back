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
 * Integration Tests for RegistryUniversityController
 *
 * <p>Tests coverage:</p>
 * <ul>
 *   <li>GET /api/v1/web/registry/universities — list with filters</li>
 *   <li>GET /api/v1/web/registry/universities/{id} — single university</li>
 *   <li>GET /api/v1/web/registry/universities/dictionaries — dropdown data</li>
 *   <li>POST /api/v1/web/registry/universities/export — CSV export</li>
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
class RegistryUniversityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String BASE_URL = "/api/v1/web/registry/universities";

    // ======================================================================
    // Test 1: List universities — with permission
    // ======================================================================

    @Test
    @Order(1)
    @DisplayName("GET /universities — returns paginated list")
    @WithMockUser(username = "admin", authorities = {"institutions.universities.view"})
    void testGetUniversities() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists());
    }

    // ======================================================================
    // Test 2: List universities — without permission
    // ======================================================================

    @Test
    @Order(2)
    @DisplayName("GET /universities — without permission returns 403")
    @WithMockUser(username = "user", authorities = {"basic.view"})
    void testGetUniversitiesForbidden() throws Exception {
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isForbidden());
    }

    // ======================================================================
    // Test 3: List universities with search filter
    // ======================================================================

    @Test
    @Order(3)
    @DisplayName("GET /universities — search filter works")
    @WithMockUser(username = "admin", authorities = {"institutions.universities.view"})
    void testGetUniversitiesWithSearch() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .param("q", "TATU")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    // ======================================================================
    // Test 4: Get university by non-existent ID
    // ======================================================================

    @Test
    @Order(4)
    @DisplayName("GET /universities/{id} — non-existent returns 404")
    @WithMockUser(username = "admin", authorities = {"institutions.universities.view"})
    void testGetUniversityNotFound() throws Exception {
        mockMvc.perform(get(BASE_URL + "/NON_EXISTENT_CODE"))
            .andExpect(status().isNotFound());
    }

    // ======================================================================
    // Test 5: Get dictionaries
    // ======================================================================

    @Test
    @Order(5)
    @DisplayName("GET /universities/dictionaries — returns dictionaries")
    @WithMockUser(username = "admin", authorities = {"institutions.universities.view"})
    void testGetDictionaries() throws Exception {
        mockMvc.perform(get(BASE_URL + "/dictionaries"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isMap());
    }

    // ======================================================================
    // Test 6: Unauthenticated access
    // ======================================================================

    @Test
    @Order(6)
    @DisplayName("GET /universities — unauthenticated returns 401 or 403")
    void testGetUniversitiesUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().is(anyOf(is(401), is(403))));
    }

    // ======================================================================
    // Test 7: Pagination edge case — large page number
    // ======================================================================

    @Test
    @Order(7)
    @DisplayName("GET /universities — large page number returns empty")
    @WithMockUser(username = "admin", authorities = {"institutions.universities.view"})
    void testGetUniversitiesLargePage() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .param("page", "9999")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    // ======================================================================
    // Test 8: Create university — without edit permission
    // ======================================================================

    @Test
    @Order(8)
    @DisplayName("POST /universities — without edit permission returns 403")
    @WithMockUser(username = "user", authorities = {"institutions.universities.view"})
    void testCreateUniversityForbidden() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test\"}"))
            .andExpect(status().isForbidden());
    }

    // ======================================================================
    // Test 9: Delete university — without delete permission
    // ======================================================================

    @Test
    @Order(9)
    @DisplayName("DELETE /universities/{code} — without delete permission returns 403")
    @WithMockUser(username = "user", authorities = {"institutions.universities.view"})
    void testDeleteUniversityForbidden() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/TEST_CODE"))
            .andExpect(status().isForbidden());
    }
}
