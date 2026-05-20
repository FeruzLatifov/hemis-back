package uz.hemis.web.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uz.hemis.app.exception.GlobalExceptionHandler;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.service.audit.AuditService;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuditLogController.class,
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class
)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = {"hemis.audit.enabled=true"})
@DisplayName("AuditLogController — 4 log turi + statistika")
class AuditLogControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private AuditService auditService;
    @MockitoBean private uz.hemis.service.shared.I18nService i18nService;

    @SpringBootApplication
    @EnableMethodSecurity
    static class TestApp {}

    private static final String BASE = "/api/v1/web/audit";

    private void authAs(String... authorities) {
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

    @Test
    @DisplayName("GET /activities — permission bor lekin role yo'q → 403")
    void activities_permButNoRole_forbidden() throws Exception {
        authAs("audit.view");

        mockMvc.perform(get(BASE + "/activities"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /activities — permission yo'q → 403")
    void activities_noPermission_forbidden() throws Exception {
        authAs("ROLE_SUPER_ADMIN");

        mockMvc.perform(get(BASE + "/activities"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /activities — SUPER_ADMIN + audit.view → 200")
    void activities_authorized_200() throws Exception {
        authAs("audit.view", "ROLE_SUPER_ADMIN");

        PageResponse<Map<String, Object>> page = PageResponse.<Map<String, Object>>builder()
                .content(List.of(Map.of("id", "1", "action", "CREATE")))
                .number(0).size(20).totalElements(1L).totalPages(1).build();
        when(auditService.getActivities(any(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get(BASE + "/activities?action=CREATE&entityType=User"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].action").value("CREATE"));
    }

    @Test
    @DisplayName("GET /activities/{id} — topilmadi → 404")
    void activityDetail_notFound() throws Exception {
        authAs("audit.view", "ROLE_MINISTRY_ADMIN");
        when(auditService.getActivityDetail("abc")).thenReturn(null);

        mockMvc.perform(get(BASE + "/activities/abc"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /activities/{id} — topilgan 200")
    void activityDetail_found_200() throws Exception {
        authAs("audit.view", "ROLE_MINISTRY_ADMIN");
        when(auditService.getActivityDetail("abc"))
                .thenReturn(Map.of("id", "abc", "action", "UPDATE"));

        mockMvc.perform(get(BASE + "/activities/abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.action").value("UPDATE"));
    }

    @Test
    @DisplayName("GET /entities/{type}/{id}/history — entity history")
    void entityHistory() throws Exception {
        authAs("audit.view", "ROLE_SUPER_ADMIN");

        PageResponse<Map<String, Object>> page = PageResponse.<Map<String, Object>>builder()
                .content(List.of(Map.of("rev", 1)))
                .number(0).size(50).totalElements(1L).totalPages(1).build();
        when(auditService.getEntityHistory(eq("User"), eq("uuid-x"), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get(BASE + "/entities/User/uuid-x/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].rev").value(1));
    }

    @Test
    @DisplayName("GET /errors — error logs")
    void errors_200() throws Exception {
        authAs("audit.view", "ROLE_SUPER_ADMIN");

        when(auditService.getErrors(any(), anyInt(), anyInt()))
                .thenReturn(PageResponse.<Map<String, Object>>builder()
                        .content(List.of()).number(0).size(20).totalElements(0L).totalPages(0).build());

        mockMvc.perform(get(BASE + "/errors?errorType=NPE"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /errors/{id} — topilmadi 404")
    void errorDetail_notFound() throws Exception {
        authAs("audit.view", "ROLE_SUPER_ADMIN");
        when(auditService.getErrorDetail(anyString())).thenReturn(null);

        mockMvc.perform(get(BASE + "/errors/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /logins — login event logs")
    void logins_200() throws Exception {
        authAs("audit.view", "ROLE_MINISTRY_ADMIN");

        when(auditService.getLogins(any(), anyInt(), anyInt()))
                .thenReturn(PageResponse.<Map<String, Object>>builder()
                        .content(List.of()).number(0).size(20).totalElements(0L).totalPages(0).build());

        mockMvc.perform(get(BASE + "/logins?eventType=SUCCESS"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /stats — admin statistika")
    void stats_200() throws Exception {
        authAs("audit.view", "ROLE_SUPER_ADMIN");
        when(auditService.getStats(any(), any())).thenReturn(Map.of("total", 42));

        mockMvc.perform(get(BASE + "/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(42));
    }
}
