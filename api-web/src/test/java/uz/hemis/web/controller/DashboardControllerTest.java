package uz.hemis.web.controller;

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
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uz.hemis.app.exception.GlobalExceptionHandler;
import uz.hemis.service.dashboard.DashboardService;
import uz.hemis.service.dashboard.dto.DashboardResponse;
import uz.hemis.service.shared.I18nService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * {@link DashboardController} slice testlari — @WebMvcTest (faqat web qatlam).
 *
 * <p>Test case'lar:
 * <ul>
 *   <li>GET /stats — 200 OK with permission</li>
 *   <li>GET /stats — 403 without permission</li>
 *   <li>GET /stats — 401/403 unauthenticated</li>
 *   <li>GET /stats — JSON content type</li>
 * </ul></p>
 */
@WebMvcTest(
        controllers = DashboardController.class,
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class
)
@Import({DashboardControllerTest.Config.class, GlobalExceptionHandler.class})
@DisplayName("DashboardController Web Layer Tests")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

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
    }

    private static final String BASE_URL = "/api/v1/web/dashboard";

    @Test
    @DisplayName("GET /stats — 200 OK ruxsat berilgan foydalanuvchi uchun")
    @WithMockUser(authorities = "dashboard.view")
    void getDashboardStats_returnsOk() throws Exception {
        DashboardResponse stats = new DashboardResponse();
        stats.setTimestamp(LocalDateTime.now());
        when(dashboardService.getDashboardStats()).thenReturn(stats);

        mockMvc.perform(get(BASE_URL + "/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("GET /stats — JSON content type qaytaradi")
    @WithMockUser(authorities = "dashboard.view")
    void getDashboardStats_returnsJsonContentType() throws Exception {
        DashboardResponse stats = new DashboardResponse();
        stats.setTimestamp(LocalDateTime.now());
        when(dashboardService.getDashboardStats()).thenReturn(stats);

        mockMvc.perform(get(BASE_URL + "/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    @DisplayName("GET /stats — ruxsatsiz 403 qaytaradi")
    @WithMockUser(authorities = "basic.view")
    void getDashboardStats_withoutPermission_returns403() throws Exception {
        mockMvc.perform(get(BASE_URL + "/stats"))
                .andExpect(status().isForbidden());
    }
}
