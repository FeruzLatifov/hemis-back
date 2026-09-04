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
    @DisplayName("GET /activities — audit.view yetarli (rol tekshiruvi yo'q)")
    void activities_permissionIsEnough() throws Exception {
        // The gate is the permission alone. It used to also demand hasRole('SUPER_ADMIN'|'ADMIN'),
        // which no real caller could satisfy: a USER token's authorities are permission codes and
        // JwtGrantedAuthoritiesConverter grants no ROLE_*, so the whole audit API answered 403 to
        // everyone. The audience now lives in the role→permission mapping: audit.view is granted to
        // SUPER_ADMIN and ADMIN only (seed S038, pinned by RoleModelIntegrationTest).
        authAs("audit.view");

        mockMvc.perform(get(BASE + "/activities"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /activities — audit.view yo'q → 403 (rol nomi yordam bermaydi)")
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
        authAs("audit.view", "ROLE_ADMIN");
        when(auditService.getActivityDetail("abc")).thenReturn(null);

        mockMvc.perform(get(BASE + "/activities/abc"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /activities/{id} — topilgan 200")
    void activityDetail_found_200() throws Exception {
        authAs("audit.view", "ROLE_ADMIN");
        when(auditService.getActivityDetail("abc"))
                .thenReturn(Map.of("id", "abc", "action", "UPDATE"));

        mockMvc.perform(get(BASE + "/activities/abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.action").value("UPDATE"));
    }

    @Test
    @DisplayName("audit.history.view — klassifikator yozuvining tafsiloti ochiladi")
    void activityDetail_narrowPermission_allowedForCuratedRegistries() throws Exception {
        // The owner-scoped history lists entries WITHOUT their before/after images and fetches them
        // one at a time. If this endpoint stayed audit.view-only, an operator's scoped dialog would
        // expand to an empty diff — which reads as "every field was cleared to nothing".
        authAs("audit.history.view");
        when(auditService.getActivityDetail("abc")).thenReturn(Map.of(
                "id", "abc", "action", "UPDATE", "entityType", "UniversitySpecialityAttachment"));

        mockMvc.perform(get(BASE + "/activities/abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.action").value("UPDATE"));
    }

    @Test
    @DisplayName("audit.history.view — User yozuvining tafsiloti yopiq qoladi")
    void activityDetail_narrowPermission_deniedForSensitiveTypes() throws Exception {
        authAs("audit.history.view");
        when(auditService.getActivityDetail("abc")).thenReturn(Map.of(
                "id", "abc", "action", "UPDATE", "entityType", "User"));

        mockMvc.perform(get(BASE + "/activities/abc"))
                .andExpect(status().isForbidden());
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
    @DisplayName("audit.history.view — bitta yozuv tarixi ochiladi (klassifikator reyestrlari uchun)")
    void entityHistory_narrowPermission_allowedForCuratedRegistries() throws Exception {
        // An operator curates specialities; asking who changed one is not the same capability as
        // reading the ministry-wide journal, so it has its own permission.
        authAs("audit.history.view");
        when(auditService.getEntityHistory(eq("HSpeciality"), eq("uuid-x"), anyInt(), anyInt()))
                .thenReturn(PageResponse.<Map<String, Object>>builder()
                        .content(List.of(Map.of("action", "UPDATE")))
                        .number(0).size(50).totalElements(1L).totalPages(1).build());

        mockMvc.perform(get(BASE + "/entities/HSpeciality/uuid-x/history"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("audit.history.view — User tarixiga yetmaydi (PII snapshot'lari audit.view'da qoladi)")
    void entityHistory_narrowPermission_deniedForSensitiveTypes() throws Exception {
        authAs("audit.history.view");

        mockMvc.perform(get(BASE + "/entities/User/uuid-x/history"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("audit.history.view — jurnalning o'zi (activities) yopiq qoladi")
    void activities_narrowPermission_forbidden() throws Exception {
        authAs("audit.history.view");

        mockMvc.perform(get(BASE + "/activities"))
                .andExpect(status().isForbidden());
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
        authAs("audit.view", "ROLE_ADMIN");

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
