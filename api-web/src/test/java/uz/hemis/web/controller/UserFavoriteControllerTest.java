package uz.hemis.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import uz.hemis.app.HemisApplication;

import java.util.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

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

    private static final String ADMIN_USER_ID = "60885987-1b61-4247-94c7-dff348347f93";

    private static RequestPostProcessor authWith(String... authorities) {
        SimpleGrantedAuthority[] grants = new SimpleGrantedAuthority[authorities.length];
        for (int i = 0; i < authorities.length; i++) {
            grants[i] = new SimpleGrantedAuthority(authorities[i]);
        }
        return jwt()
                .jwt(j -> j.subject(ADMIN_USER_ID))
                .authorities(grants);
    }

    @Test
    @Order(1)
    @DisplayName("GET /favorites — returns user favorites list")
    void testGetFavorites() throws Exception {
        mockMvc.perform(get(BASE_URL).with(authWith("system.view")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @Order(2)
    @DisplayName("GET /favorites — unauthenticated returns 401 or 403")
    void testGetFavoritesUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().is(anyOf(is(401), is(403))));
    }

    @Test
    @Order(3)
    @DisplayName("POST /favorites — empty body returns 400")
    void testAddFavoriteEmptyBody() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .with(authWith("system.view"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    @DisplayName("DELETE /favorites/{code} — non-existent returns 204 or 404")
    void testDeleteNonExistentFavorite() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/NON_EXISTENT_CODE").with(authWith("system.view")))
            .andExpect(status().is(anyOf(is(200), is(204), is(404))));
    }

    @Test
    @Order(5)
    @DisplayName("PATCH /favorites/reorder — empty list")
    void testReorderFavoritesEmpty() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/reorder")
                .with(authWith("system.view"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
            .andExpect(status().is(anyOf(is(200), is(204))));
    }

    @Test
    @Order(6)
    @DisplayName("POST + DELETE /favorites — add and remove flow")
    void testAddAndDeleteFavorite() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("menuCode", "test_menu_code");
        body.put("title", "Test Favorite");
        body.put("path", "/test/path");
        body.put("icon", "Star");

        mockMvc.perform(post(BASE_URL)
                .with(authWith("system.view"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().is(anyOf(is(200), is(201), is(400))));

        mockMvc.perform(delete(BASE_URL + "/test_menu_code").with(authWith("system.view")))
            .andExpect(status().is(anyOf(is(200), is(204), is(404))));
    }

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
