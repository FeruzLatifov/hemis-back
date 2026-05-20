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
import uz.hemis.service.university.UniversityExternalDataService;
import uz.hemis.service.university.UniversityInfoService;
import uz.hemis.service.university.UniversityOfficialService;
import uz.hemis.service.university.UniversityProfileService;
import uz.hemis.service.university.dto.UniversityDashboardDto;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UniversityInfoController.class,
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class
)
@Import(GlobalExceptionHandler.class)
@DisplayName("UniversityInfoController — OTM dashboard + officials + profile")
class UniversityInfoControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private UniversityInfoService universityInfoService;
    @MockitoBean private UniversityExternalDataService externalDataService;
    @MockitoBean private UniversityOfficialService officialService;
    @MockitoBean private UniversityProfileService profileService;
    @MockitoBean private uz.hemis.service.shared.I18nService i18nService;

    @SpringBootApplication
    @EnableMethodSecurity
    static class TestApp {}

    private static final String BASE = "/api/v1/web/university";

    private void auth(String... authorities) {
        Jwt jwt = Jwt.withTokenValue("t").header("alg", "none")
                .subject("00000000-0000-0000-0000-000000000001")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        List<SimpleGrantedAuthority> grants = Arrays.stream(authorities)
                .map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, grants));
    }

    @AfterEach
    void clear() { SecurityContextHolder.clearContext(); }

    @Test
    @DisplayName("GET /{code}/dashboard — authenticated")
    void dashboard_authenticated() throws Exception {
        auth("any.permission");
        when(universityInfoService.getUniversityDashboard("337"))
                .thenReturn(new UniversityDashboardDto());

        mockMvc.perform(get(BASE + "/337/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /{code}/founders — null safe, empty list")
    void founders_nullSafe() throws Exception {
        auth("any");
        when(universityInfoService.getFounders("337")).thenReturn(null);

        mockMvc.perform(get(BASE + "/337/founders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /{code}/lifecycle — null safe")
    void lifecycle_nullSafe() throws Exception {
        auth("any");
        when(universityInfoService.getLifecycle("337")).thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE + "/337/lifecycle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("POST /{code}/sync — external sync trigger")
    void sync_callsExternalService() throws Exception {
        auth("any");
        when(universityInfoService.getUniversityDashboard("337"))
                .thenReturn(new UniversityDashboardDto());

        mockMvc.perform(post(BASE + "/337/sync").with(csrf()))
                .andExpect(status().isOk());

        verify(externalDataService).syncAll("337");
    }

    @Test
    @DisplayName("GET /{code}/officials?history=true — barchasi")
    void officials_history() throws Exception {
        auth("any");
        when(officialService.getOfficials("337", false)).thenReturn(List.of());

        mockMvc.perform(get(BASE + "/337/officials?history=true"))
                .andExpect(status().isOk());

        verify(officialService).getOfficials("337", false);
    }

    @Test
    @DisplayName("DELETE /{code}/officials/{metaId} — 204")
    void removeOfficial_204() throws Exception {
        auth("any");
        UUID metaId = UUID.randomUUID();

        mockMvc.perform(delete(BASE + "/337/officials/" + metaId + "?decree=PQ-123").with(csrf()))
                .andExpect(status().isNoContent());

        verify(officialService).removeOfficial(metaId, "PQ-123");
    }

    @Test
    @DisplayName("GET /lookup/person/{pinfl} — masking + service call")
    void lookupPerson() throws Exception {
        auth("any");
        when(officialService.lookupByPinfl(anyString(), any(), any()))
                .thenReturn(Map.of("fullName", "Karimov"));

        mockMvc.perform(get(BASE + "/lookup/person/12345678901234?birthDate=1990-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Karimov"));
    }

    @Test
    @DisplayName("GET /positions — leadership positions classifier")
    void positions() throws Exception {
        auth("any");
        when(officialService.getLeadershipPositions())
                .thenReturn(List.of(Map.of("code", "RECTOR", "name", "Rektor")));

        mockMvc.perform(get(BASE + "/positions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("RECTOR"));
    }
}
