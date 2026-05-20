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
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uz.hemis.app.exception.GlobalExceptionHandler;
import uz.hemis.common.dto.outbox.OutboxStatsDto;
import uz.hemis.service.outbox.OutboxAdminService;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = OutboxAdminController.class,
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class
)
@Import(GlobalExceptionHandler.class)
@DisplayName("OutboxAdminController — outbox observability + manual retry")
class OutboxAdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private OutboxAdminService service;
    @MockitoBean private uz.hemis.service.shared.I18nService i18nService;

    @SpringBootApplication
    @EnableMethodSecurity
    static class TestApp {}

    private static final String BASE = "/api/v1/web/admin/outbox";

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
    @DisplayName("GET / — outbox.view yo'q → 403")
    void list_forbidden() throws Exception {
        auth("system.view");
        mockMvc.perform(get(BASE)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET / — outbox.view + pagination OK")
    void list_paginated() throws Exception {
        auth("outbox.view");
        Page<Object> empty = new PageImpl<>(List.of(), Pageable.ofSize(25), 0);
        when(service.list(any(), any(), any())).thenReturn((Page) empty);

        mockMvc.perform(get(BASE + "?status=PENDING&aggregateType=employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service).list(eq("PENDING"), eq("employee"), any());
    }

    @Test
    @DisplayName("GET /stats — outbox.view")
    void stats_authorized() throws Exception {
        auth("outbox.view");
        when(service.stats()).thenReturn(new OutboxStatsDto(100, 5, 90, 5, 3));

        mockMvc.perform(get(BASE + "/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(100))
                .andExpect(jsonPath("$.data.pending").value(5));
    }

    @Test
    @DisplayName("POST /{id}/retry — outbox.manage yo'q → 403")
    void retry_forbidden() throws Exception {
        auth("outbox.view");
        mockMvc.perform(post(BASE + "/" + UUID.randomUUID() + "/retry").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /{id}/retry — outbox.manage authorized")
    void retry_authorized() throws Exception {
        auth("outbox.manage");
        UUID id = UUID.randomUUID();
        when(service.retry(id)).thenReturn(null);

        mockMvc.perform(post(BASE + "/" + id + "/retry").with(csrf()))
                .andExpect(status().isOk());

        verify(service).retry(id);
    }

    @Test
    @DisplayName("POST /{id}/discard — reason validation OK")
    void discard_validReason() throws Exception {
        auth("outbox.manage");
        UUID id = UUID.randomUUID();

        mockMvc.perform(post(BASE + "/" + id + "/discard?reason=Stale").with(csrf()))
                .andExpect(status().isOk());

        verify(service).discard(id, "Stale");
    }

    @Test
    @DisplayName("POST /{id}/discard — reason juda uzun → 400")
    void discard_reasonTooLong() throws Exception {
        auth("outbox.manage");
        UUID id = UUID.randomUUID();
        String tooLong = "a".repeat(260);

        mockMvc.perform(post(BASE + "/" + id + "/discard?reason=" + tooLong).with(csrf()))
                .andExpect(status().is4xxClientError());
    }
}
