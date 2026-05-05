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
import uz.hemis.service.favorite.UserFavoriteService;
import uz.hemis.service.shared.I18nService;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link UserFavoriteController} slice testlari — @WebMvcTest (faqat web qatlam).
 *
 * <p>Test case'lar:
 * <ul>
 *   <li>GET /favorites — authenticated, list qaytaradi</li>
 *   <li>POST /favorites — empty body 400 (Bean Validation)</li>
 *   <li>DELETE /favorites/{code} — 204 No Content</li>
 *   <li>PATCH /favorites/reorder — 204 No Content</li>
 * </ul></p>
 */
@WebMvcTest(
        controllers = UserFavoriteController.class,
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class
)
@Import({UserFavoriteControllerTest.Config.class, GlobalExceptionHandler.class})
@DisplayName("UserFavoriteController Web Layer Tests")
class UserFavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserFavoriteService favoriteService;

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

    private static final String BASE_URL = "/api/v1/web/favorites";
    private static final String ADMIN_USER_ID = "60885987-1b61-4247-94c7-dff348347f93";

    private void setAuthenticated(String... authorities) {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(ADMIN_USER_ID)
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
    @DisplayName("GET /favorites — authenticated, ro'yxat qaytaradi")
    void getFavorites_authenticated_returnsList() throws Exception {
        setAuthenticated("system.view");
        when(favoriteService.getUserFavorites(Mockito.any())).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("POST /favorites — empty body 400 (Bean Validation)")
    void addFavorite_emptyBody_returns400() throws Exception {
        setAuthenticated("system.view");

        mockMvc.perform(post(BASE_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /favorites/{code} — 204 No Content")
    void removeFavorite_authenticated_returns204() throws Exception {
        setAuthenticated("system.view");

        mockMvc.perform(delete(BASE_URL + "/test_code").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /favorites/reorder — 204 No Content with empty list")
    void reorderFavorites_emptyList_returns204() throws Exception {
        setAuthenticated("system.view");

        mockMvc.perform(patch(BASE_URL + "/reorder").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isNoContent());
    }
}
