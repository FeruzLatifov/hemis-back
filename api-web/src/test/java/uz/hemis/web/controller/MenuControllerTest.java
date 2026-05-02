package uz.hemis.web.controller;

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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

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
    @DisplayName("GET /menu — returns menu structure")
    void testGetMenu() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .with(authWith("system.view"))
                .param("locale", "uz-UZ"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @Order(2)
    @DisplayName("GET /menu — unauthenticated returns 401 or 403")
    void testGetMenuUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().is(anyOf(is(401), is(403))));
    }

    @Test
    @Order(3)
    @DisplayName("GET /menu — different locale returns menu")
    void testGetMenuWithLocale() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .with(authWith("system.view"))
                .param("locale", "ru-RU"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @Order(4)
    @DisplayName("POST /check-access — returns access result")
    void testCheckAccess() throws Exception {
        mockMvc.perform(post(BASE_URL + "/check-access")
                .with(authWith("system.view"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"path\": \"/dashboard\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @Order(5)
    @DisplayName("POST /clear-cache — with permission clears cache")
    void testClearCache() throws Exception {
        mockMvc.perform(post(BASE_URL + "/clear-cache").with(authWith("system.view")))
            .andExpect(status().isOk());
    }

    @Test
    @Order(6)
    @DisplayName("POST /clear-cache — without permission returns 403")
    void testClearCacheForbidden() throws Exception {
        mockMvc.perform(post(BASE_URL + "/clear-cache").with(authWith("basic.view")))
            .andExpect(status().isForbidden());
    }

    @Test
    @Order(7)
    @DisplayName("GET /structure — with permission returns full structure")
    void testGetStructure() throws Exception {
        mockMvc.perform(get(BASE_URL + "/structure")
                .with(authWith("system.menu.view"))
                .param("locale", "uz-UZ"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @Order(8)
    @DisplayName("GET /structure — without permission returns 403")
    void testGetStructureForbidden() throws Exception {
        mockMvc.perform(get(BASE_URL + "/structure").with(authWith("basic.view")))
            .andExpect(status().isForbidden());
    }
}
