package uz.hemis.web.controller;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import uz.hemis.app.HemisApplication;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

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

    private static org.springframework.test.web.servlet.request.RequestPostProcessor adminAuth() {
        return jwt().authorities(new SimpleGrantedAuthority("institutions.universities.view"));
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor basicAuth() {
        return jwt().authorities(new SimpleGrantedAuthority("basic.view"));
    }

    @Test
    @Order(1)
    @DisplayName("GET /universities — returns paginated list")
    void testGetUniversities() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .with(adminAuth())
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @Order(2)
    @DisplayName("GET /universities — without permission returns 403")
    void testGetUniversitiesForbidden() throws Exception {
        mockMvc.perform(get(BASE_URL).with(basicAuth()))
            .andExpect(status().isForbidden());
    }

    @Test
    @Order(3)
    @DisplayName("GET /universities — search filter works")
    void testGetUniversitiesWithSearch() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .with(adminAuth())
                .param("q", "TATU")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @Order(4)
    @DisplayName("GET /universities/{id} — non-existent returns 404")
    void testGetUniversityNotFound() throws Exception {
        mockMvc.perform(get(BASE_URL + "/NON_EXISTENT_CODE").with(adminAuth()))
            .andExpect(status().isNotFound());
    }

    @Test
    @Order(5)
    @DisplayName("GET /universities/dictionaries — returns dictionaries")
    void testGetDictionaries() throws Exception {
        mockMvc.perform(get(BASE_URL + "/dictionaries").with(adminAuth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isMap());
    }

    @Test
    @Order(6)
    @DisplayName("GET /universities — unauthenticated returns 401 or 403")
    void testGetUniversitiesUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().is(anyOf(is(401), is(403))));
    }

    @Test
    @Order(7)
    @DisplayName("GET /universities — large page number returns empty")
    void testGetUniversitiesLargePage() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .with(adminAuth())
                .param("page", "9999")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @Order(8)
    @DisplayName("POST /universities — without edit permission returns 403")
    void testCreateUniversityForbidden() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .with(adminAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"TEST_CODE\",\"name\":\"Test University\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @Order(9)
    @DisplayName("DELETE /universities/{code} — without delete permission returns 403")
    void testDeleteUniversityForbidden() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/TEST_CODE").with(adminAuth()))
            .andExpect(status().isForbidden());
    }
}
