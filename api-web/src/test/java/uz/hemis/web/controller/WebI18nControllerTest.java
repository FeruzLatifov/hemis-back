package uz.hemis.web.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uz.hemis.app.exception.GlobalExceptionHandler;
import uz.hemis.service.shared.I18nService;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = WebI18nController.class,
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class
)
@Import(GlobalExceptionHandler.class)
@DisplayName("WebI18nController — i18n bulk + scoped + cache mgmt")
class WebI18nControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private I18nService i18nService;

    @SpringBootApplication
    @EnableMethodSecurity
    static class TestApp {}

    private static final String BASE = "/api/v1/web/i18n";

    private void authenticate(String... authorities) {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject("00000000-0000-0000-0000-000000000001")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        List<SimpleGrantedAuthority> grants = Arrays.stream(authorities)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, grants));
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /messages — bulk, default lang=uz-UZ")
    void getAllMessages_defaultLang() throws Exception {
        when(i18nService.getAllMessages("uz-UZ"))
                .thenReturn(Map.of("Save", "Saqlash", "Cancel", "Bekor qilish"));

        mockMvc.perform(get(BASE + "/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.Save").value("Saqlash"));
    }

    @Test
    @DisplayName("GET /messages?lang=ru-RU — language passed through")
    void getAllMessages_ruLang() throws Exception {
        when(i18nService.getAllMessages("ru-RU"))
                .thenReturn(Map.of("Save", "Сохранить"));

        mockMvc.perform(get(BASE + "/messages?lang=ru-RU"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.Save").value("Сохранить"));
    }

    @Test
    @DisplayName("GET /messages/{key} — single translation")
    void getMessage_singleKey() throws Exception {
        when(i18nService.getMessage("Save", "ru-RU")).thenReturn("Сохранить");

        mockMvc.perform(get(BASE + "/messages/Save?lang=ru-RU"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /messages/category/{category} — kategoriya filter")
    void getByCategory_returnsFiltered() throws Exception {
        when(i18nService.getMessagesByCategory("action", "uz-UZ"))
                .thenReturn(Map.of("Save", "Saqlash"));

        mockMvc.perform(get(BASE + "/messages/category/action"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.Save").value("Saqlash"));
    }

    @Test
    @DisplayName("GET /messages/scopes — comma-separated scopes parse")
    void getByScopes_parsesCommaList() throws Exception {
        when(i18nService.getMessagesByScopes(anyList(), eq("uz-UZ")))
                .thenReturn(Map.of("Save", "Saqlash"));

        mockMvc.perform(get(BASE + "/messages/scopes?scopes=auth,menu,action"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.Save").value("Saqlash"));

        verify(i18nService).getMessagesByScopes(anyList(), eq("uz-UZ"));
    }

    @Test
    @DisplayName("POST /cache/invalidate — permission yetishmasa → 403")
    void invalidateCache_forbidden() throws Exception {
        authenticate("other.permission");

        mockMvc.perform(post(BASE + "/cache/invalidate").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /cache/invalidate?lang=ru-RU — admin, single language")
    void invalidateCache_singleLanguage() throws Exception {
        authenticate("system.translation.view");

        mockMvc.perform(post(BASE + "/cache/invalidate?lang=ru-RU").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(i18nService).invalidateCache("ru-RU");
    }

    @Test
    @DisplayName("POST /cache/invalidate — lang yo'q → invalidateAll")
    void invalidateCache_allLanguages() throws Exception {
        authenticate("system.translation.view");

        mockMvc.perform(post(BASE + "/cache/invalidate").with(csrf()))
                .andExpect(status().isOk());

        verify(i18nService).invalidateAllCaches();
    }

    @Test
    @DisplayName("GET /cache/stats — admin → 200 with stats")
    void cacheStats_admin() throws Exception {
        authenticate("system.translation.view");
        when(i18nService.getCacheStats()).thenReturn(Map.of(
                "cachedLanguages", 3,
                "languages", List.of("uz-UZ", "ru-RU", "en-US")));

        mockMvc.perform(get(BASE + "/cache/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cachedLanguages").value(3));
    }

    @Test
    @DisplayName("GET /health — public, always 200")
    void health_returnsUp() throws Exception {
        mockMvc.perform(get(BASE + "/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UP"));
    }
}
