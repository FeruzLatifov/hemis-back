package uz.hemis.web.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uz.hemis.app.exception.GlobalExceptionHandler;
import uz.hemis.common.dto.webhook.WebhookSecretResponse;
import uz.hemis.common.dto.webhook.WebhookTargetDto;
import uz.hemis.service.webhook.WebhookTargetService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = WebhookTargetController.class,
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class
)
@Import(GlobalExceptionHandler.class)
@DisplayName("WebhookTargetController — 224 OTM webhook admin")
class WebhookTargetControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private WebhookTargetService service;
    @MockitoBean private uz.hemis.service.shared.I18nService i18nService;

    @SpringBootApplication
    @EnableMethodSecurity
    static class TestApp {}

    private static final String BASE = "/api/v1/web/admin/webhooks";

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

    private WebhookTargetDto dto(UUID id) {
        return new WebhookTargetDto(
                id, "337", "https://337.univer.uz/callback", "Andijon DU",
                true, 30000, 3,
                LocalDateTime.now(), "system", LocalDateTime.now(), "system");
    }

    @Test
    @DisplayName("GET / — webhook.view authorized → list")
    void list_authorized() throws Exception {
        auth("webhook.view");
        when(service.findAll()).thenReturn(List.of(dto(UUID.randomUUID())));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].universityCode").value("337"));
    }

    @Test
    @DisplayName("GET / — webhook.view yo'q → 403")
    void list_forbidden() throws Exception {
        auth("other.view");
        mockMvc.perform(get(BASE)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /{id} — by ID")
    void getById() throws Exception {
        auth("webhook.view");
        UUID id = UUID.randomUUID();
        when(service.findById(id)).thenReturn(dto(id));

        mockMvc.perform(get(BASE + "/" + id))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /by-university/{code} — by university code")
    void getByUniversity() throws Exception {
        auth("webhook.view");
        when(service.findByUniversityCode("337")).thenReturn(dto(UUID.randomUUID()));

        mockMvc.perform(get(BASE + "/by-university/337"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.universityCode").value("337"));
    }

    @Test
    @DisplayName("POST / — webhook.create authorized → 201 + plain secret")
    void create_returns201WithSecret() throws Exception {
        auth("webhook.create");
        UUID id = UUID.randomUUID();
        WebhookSecretResponse resp = new WebhookSecretResponse(id, "337", "whsec_xxx", LocalDateTime.now(), "save it");
        when(service.create(any())).thenReturn(resp);

        mockMvc.perform(post(BASE).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"universityCode":"337","description":"Andijon DU","timeoutMs":30000,"maxRetries":3}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.plainSecret").value("whsec_xxx"));
    }

    @Test
    @DisplayName("POST / — webhook.create yo'q → 403")
    void create_forbidden() throws Exception {
        auth("webhook.view");

        mockMvc.perform(post(BASE).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"universityCode":"337"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST / — universityCode pattern xato → 400")
    void create_invalidUniversityCode() throws Exception {
        auth("webhook.create");

        mockMvc.perform(post(BASE).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"universityCode":"ABC"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /{id}/regenerate-secret — secret rotation")
    void regenerateSecret_authorized() throws Exception {
        auth("webhook.manage");
        UUID id = UUID.randomUUID();
        WebhookSecretResponse resp = new WebhookSecretResponse(id, "337", "whsec_new", LocalDateTime.now(), "rotate");
        when(service.regenerateSecret(id)).thenReturn(resp);

        mockMvc.perform(post(BASE + "/" + id + "/regenerate-secret").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.plainSecret").value("whsec_new"));
    }

    @Test
    @DisplayName("PUT /{id} — partial update")
    void update_authorized() throws Exception {
        auth("webhook.update");
        UUID id = UUID.randomUUID();
        when(service.update(eq(id), any())).thenReturn(dto(id));

        mockMvc.perform(put(BASE + "/" + id).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"Updated","timeoutMs":30000,"maxRetries":3,"active":true}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /{id} — soft delete")
    void delete_authorized() throws Exception {
        auth("webhook.delete");
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete(BASE + "/" + id).with(csrf()))
                .andExpect(status().isOk());

        verify(service).delete(id);
    }

    @Test
    @DisplayName("GET /{id}/deliveries — paginated history")
    void deliveries_paginated() throws Exception {
        auth("webhook.view");
        UUID id = UUID.randomUUID();
        Page<Object> empty = new PageImpl<>(List.of(), Pageable.ofSize(50), 0);
        when(service.findDeliveriesByTarget(eq(id), any())).thenReturn((Page) empty);

        mockMvc.perform(get(BASE + "/" + id + "/deliveries"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /events/{eventId}/deliveries — all attempts")
    void deliveriesByEvent() throws Exception {
        auth("webhook.view");
        UUID eventId = UUID.randomUUID();
        when(service.findDeliveriesByEvent(eventId)).thenReturn(List.of());

        mockMvc.perform(get(BASE + "/events/" + eventId + "/deliveries"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /dlq — DLQ entries")
    void dlqEntries() throws Exception {
        auth("webhook.view");
        Page<Object> empty = new PageImpl<>(List.of(), Pageable.ofSize(50), 0);
        when(service.findDlqEntries(any())).thenReturn((Page) empty);

        mockMvc.perform(get(BASE + "/dlq"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /{id}/test — sandbox test")
    void sendTestEvent_authorized() throws Exception {
        auth("webhook.manage");
        UUID id = UUID.randomUUID();
        when(service.sendTestEvent(id)).thenReturn(null);

        mockMvc.perform(post(BASE + "/" + id + "/test").with(csrf()))
                .andExpect(status().isOk());
    }
}
