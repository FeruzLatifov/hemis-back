package uz.hemis.web.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uz.hemis.app.exception.GlobalExceptionHandler;
import uz.hemis.security.service.RateLimitService;
import uz.hemis.service.auth.PasswordResetService;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PasswordResetController.class,
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class
)
@Import(GlobalExceptionHandler.class)
@DisplayName("PasswordResetController — forgot + reset + rate limit")
class PasswordResetControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private PasswordResetService passwordResetService;
    @MockitoBean private RateLimitService rateLimitService;
    @MockitoBean private uz.hemis.service.shared.I18nService i18nService;

    @SpringBootApplication
    @EnableMethodSecurity
    static class TestApp {}

    private static final String BASE = "/api/v1/web/auth";

    @Test
    @DisplayName("POST /forgot-password — happy path 200 (generic message)")
    void forgotPassword_happy() throws Exception {
        when(rateLimitService.isAllowed(anyString(), anyString(), anyInt(), anyLong())).thenReturn(true);

        mockMvc.perform(post(BASE + "/forgot-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(passwordResetService).requestReset("user@example.com");
    }

    @Test
    @DisplayName("POST /forgot-password — bo'sh email → 400 Bean Validation")
    void forgotPassword_invalidEmail_400() throws Exception {
        mockMvc.perform(post(BASE + "/forgot-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest());

        verify(passwordResetService, never()).requestReset(anyString());
    }

    @Test
    @DisplayName("POST /forgot-password — rate limit → 429")
    void forgotPassword_rateLimited() throws Exception {
        when(rateLimitService.isAllowed(anyString(), anyString(), anyInt(), anyLong())).thenReturn(false);

        mockMvc.perform(post(BASE + "/forgot-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\"}"))
                .andExpect(status().isTooManyRequests());

        verify(passwordResetService, never()).requestReset(anyString());
    }

    @Test
    @DisplayName("POST /reset-password — happy 200")
    void resetPassword_happy() throws Exception {
        when(rateLimitService.isAllowed(anyString(), anyString(), anyInt(), anyLong())).thenReturn(true);

        String body = """
                {"token":"valid-token","password":"Strong1!Password"}
                """;

        mockMvc.perform(post(BASE + "/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(passwordResetService).resetPassword("valid-token", "Strong1!Password");
    }

    @Test
    @DisplayName("POST /reset-password — kuchsiz parol → 400")
    void resetPassword_weakPassword_400() throws Exception {
        String body = """
                {"token":"valid","password":"weak"}
                """;

        mockMvc.perform(post(BASE + "/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(passwordResetService, never()).resetPassword(anyString(), anyString());
    }

    @Test
    @DisplayName("POST /reset-password — rate limit → 429")
    void resetPassword_rateLimited() throws Exception {
        when(rateLimitService.isAllowed(anyString(), anyString(), anyInt(), anyLong())).thenReturn(false);

        String body = """
                {"token":"valid","password":"Strong1!Password"}
                """;

        mockMvc.perform(post(BASE + "/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("POST /forgot-password — X-Forwarded-For header IP'sini honor qiladi")
    void forgotPassword_honorsXForwardedFor() throws Exception {
        when(rateLimitService.isAllowed(anyString(), anyString(), anyInt(), anyLong())).thenReturn(true);

        mockMvc.perform(post(BASE + "/forgot-password")
                        .with(csrf())
                        .header("X-Forwarded-For", "203.0.113.10, 10.0.0.1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\"}"))
                .andExpect(status().isOk());

        verify(rateLimitService).isAllowed("ratelimit:forgot-password:ip:", "203.0.113.10", 5, 15L);
    }
}
