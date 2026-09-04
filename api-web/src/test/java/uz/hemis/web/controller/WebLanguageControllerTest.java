package uz.hemis.web.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uz.hemis.app.exception.GlobalExceptionHandler;
import uz.hemis.common.dto.system.LanguageDto;
import uz.hemis.service.shared.LanguageService;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = WebLanguageController.class,
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class
)
@Import(GlobalExceptionHandler.class)
@DisplayName("WebLanguageController — language list + system config")
class WebLanguageControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private LanguageService languageService;
    @MockitoBean private uz.hemis.service.shared.I18nService i18nService;

    @SpringBootApplication
    @EnableMethodSecurity
    static class TestApp {}

    private static final String BASE = "/api/v1/web";

    private void authenticate(String... authorities) {
        Jwt jwt = Jwt.withTokenValue("test")
                .header("alg", "none")
                .subject("00000000-0000-0000-0000-000000000001")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        List<SimpleGrantedAuthority> grants = Arrays.stream(authorities)
                .map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, grants));
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private LanguageDto lang(String code, String name, boolean active, boolean canDisable) {
        LanguageDto d = new LanguageDto();
        d.setCode(code);
        d.setName(name);
        d.setNativeName(name);
        d.setIsActive(active);
        d.setCanDisable(canDisable);
        d.setPosition(1);
        return d;
    }

    @Test
    @DisplayName("GET /languages — barcha tillarni qaytaradi")
    void getAllLanguages_listReturned() throws Exception {
        when(languageService.getAllLanguages())
                .thenReturn(List.of(lang("uz-UZ", "O'zbekcha", true, false),
                        lang("en-US", "English", true, true)));

        mockMvc.perform(get(BASE + "/languages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].code").value("uz-UZ"))
                .andExpect(jsonPath("$.data[1].code").value("en-US"));
    }

    @Test
    @DisplayName("GET /languages/active — faqat aktivlar")
    void getActiveLanguages() throws Exception {
        when(languageService.getActiveLanguages())
                .thenReturn(List.of(lang("uz-UZ", "O'zbekcha", true, false)));

        mockMvc.perform(get(BASE + "/languages/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("uz-UZ"));
    }

    @Test
    @DisplayName("GET /system/configuration — languages + defaultLanguage")
    void getSystemConfiguration() throws Exception {
        when(languageService.getAllLanguages())
                .thenReturn(List.of(lang("uz-UZ", "O'zbekcha", true, false)));
        when(languageService.getDefaultLanguageCode()).thenReturn(Optional.of("uz-UZ"));

        mockMvc.perform(get(BASE + "/system/configuration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.defaultLanguage").value("uz-UZ"))
                .andExpect(jsonPath("$.data.languages[0].enabled").value(true));
    }

    @Test
    @DisplayName("POST /system/configuration — non-admin → 403")
    void updateConfig_forbidden() throws Exception {
        authenticate("OTHER_AUTH");

        mockMvc.perform(post(BASE + "/system/configuration")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /system/configuration — settings.edit, language toggle + default set")
    void updateConfig_adminToggles() throws Exception {
        // Permission, not role: the endpoint moved off hasRole('ADMIN') when S038 renamed
        // MINISTRY_ADMIN to ADMIN and a dead role gate would have come alive as a new capability.
        authenticate("settings.edit");

        String body = """
                {
                  "languages": {"en-US": true, "kk-UZ": false},
                  "defaultLanguage": "uz-UZ"
                }
                """;

        mockMvc.perform(post(BASE + "/system/configuration")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        verify(languageService).toggleLanguage("en-US", true);
        verify(languageService).toggleLanguage("kk-UZ", false);
        verify(languageService).setDefaultLanguageCode("uz-UZ");
    }
}
