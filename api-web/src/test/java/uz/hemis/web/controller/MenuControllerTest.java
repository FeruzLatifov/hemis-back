package uz.hemis.web.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uz.hemis.app.exception.GlobalExceptionHandler;
import uz.hemis.service.cache.CacheEvictionService;
import uz.hemis.service.config.LanguageProperties;
import uz.hemis.service.menu.MenuService;
import uz.hemis.service.menu.PermissionService;
import uz.hemis.service.menu.dto.MenuResponse;
import uz.hemis.service.shared.I18nService;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link MenuController} slice testlari — @WebMvcTest (faqat web qatlam).
 *
 * <p>Test case'lar:
 * <ul>
 *   <li>GET /menu — authenticated, qaytaradi struktura</li>
 *   <li>POST /check-access — authenticated, qaytaradi accessible bool</li>
 *   <li>POST /clear-cache — system.view permission talab qiladi</li>
 *   <li>GET /structure — system.menu.view permission talab qiladi</li>
 * </ul></p>
 */
@WebMvcTest(
        controllers = MenuController.class,
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class
)
@Import({MenuControllerTest.Config.class, GlobalExceptionHandler.class})
@DisplayName("MenuController Web Layer Tests")
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MenuService menuService;

    @MockitoBean
    private PermissionService permissionService;

    @MockitoBean
    private CacheEvictionService cacheEvictionService;

    @MockitoBean
    private LanguageProperties languageProperties;

    @SpringBootApplication
    @EnableMethodSecurity
    static class TestApp {
    }

    @TestConfiguration
    static class Config {
        @Bean
        I18nService i18nService() {
            I18nService mock = Mockito.mock(I18nService.class);
            when(mock.getMessage(anyString(), anyString()))
                    .thenAnswer(inv -> inv.getArgument(0));
            return mock;
        }

        /** OAuth2 ResourceServer auto-config disabled bo'lganda `@AuthenticationPrincipal Jwt`
         *  argument resolver ham ulanmaydi. Manual register qilamiz. */
        @Bean
        WebMvcConfigurer authenticationPrincipalConfigurer() {
            return new WebMvcConfigurer() {
                @Override
                public void addArgumentResolvers(java.util.List<HandlerMethodArgumentResolver> resolvers) {
                    resolvers.add(new AuthenticationPrincipalArgumentResolver());
                }
            };
        }
    }

    private static final String BASE_URL = "/api/v1/web/menu";
    private static final String ADMIN_USER_ID = "60885987-1b61-4247-94c7-dff348347f93";

    /** Har test'dan oldin SecurityContext'ga JWT-based authentication qo'yamiz —
     *  `with(jwt())` post-processor disabled OAuth2 sababli ulanmaydi, shuning uchun manual. */
    private void setAuthenticated(String... authorities) {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(ADMIN_USER_ID)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .claim("scope", "rest-api")
                .build();
        List<SimpleGrantedAuthority> grants = Arrays.stream(authorities)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, grants);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /menu — authenticated user, 200 OK")
    void getMenu_authenticated_returnsOk() throws Exception {
        setAuthenticated("system.view");
        when(languageProperties.getOrDefault(anyString())).thenReturn("uz-UZ");
        MenuResponse response = MenuResponse.builder().menu(List.of()).locale("uz-UZ").build();
        when(menuService.getMenuForUser(any(UUID.class), eq("uz-UZ"))).thenReturn(response);

        mockMvc.perform(get(BASE_URL).param("locale", "uz-UZ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("GET /menu — boshqa locale qaytaradi response")
    void getMenu_withDifferentLocale_returnsOk() throws Exception {
        setAuthenticated("system.view");
        when(languageProperties.getOrDefault("ru-RU")).thenReturn("ru-RU");
        MenuResponse response = MenuResponse.builder().menu(List.of()).locale("ru-RU").build();
        when(menuService.getMenuForUser(any(UUID.class), eq("ru-RU"))).thenReturn(response);

        mockMvc.perform(get(BASE_URL).param("locale", "ru-RU"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /check-access — authenticated, accessible field qaytaradi")
    void checkAccess_authenticated_returnsResult() throws Exception {
        setAuthenticated("system.view");
        when(permissionService.canAccessPath(any(UUID.class), eq("/dashboard"))).thenReturn(true);

        mockMvc.perform(post(BASE_URL + "/check-access")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"path\":\"/dashboard\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessible").value(true));
    }

    @Test
    @DisplayName("POST /clear-cache — system.view permission bilan 200 OK")
    void clearCache_withPermission_returnsOk() throws Exception {
        setAuthenticated("system.view");
        mockMvc.perform(post(BASE_URL + "/clear-cache").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /clear-cache — ruxsatsiz 403")
    void clearCache_withoutPermission_returns403() throws Exception {
        setAuthenticated("basic.view");
        mockMvc.perform(post(BASE_URL + "/clear-cache").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /structure — system.menu.view permission bilan 200 OK")
    void getStructure_withPermission_returnsOk() throws Exception {
        setAuthenticated("system.menu.view");
        when(languageProperties.getOrDefault(anyString())).thenReturn("uz-UZ");
        MenuResponse response = MenuResponse.builder().menu(List.of()).locale("uz-UZ").build();
        when(menuService.getMenuForUser(any(UUID.class), eq("uz-UZ"))).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/structure").param("locale", "uz-UZ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /structure — ruxsatsiz 403")
    void getStructure_withoutPermission_returns403() throws Exception {
        setAuthenticated("basic.view");
        mockMvc.perform(get(BASE_URL + "/structure"))
                .andExpect(status().isForbidden());
    }
}
