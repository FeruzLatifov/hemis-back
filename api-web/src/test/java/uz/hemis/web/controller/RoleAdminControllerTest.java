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
import uz.hemis.common.enums.RoleType;
import uz.hemis.service.admin.RoleAdminService;
import uz.hemis.service.admin.dto.RoleResponse;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
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
        controllers = RoleAdminController.class,
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class
)
@Import(GlobalExceptionHandler.class)
@DisplayName("RoleAdminController — RBAC CRUD")
class RoleAdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private RoleAdminService roleAdminService;
    @MockitoBean private uz.hemis.service.shared.I18nService i18nService;

    @SpringBootApplication
    @EnableMethodSecurity
    static class TestApp {}

    private static final String BASE = "/api/v1/web/admin/roles";

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

    private RoleResponse buildResponse(UUID id) {
        return RoleResponse.builder()
                .id(id).code("CUSTOM_ROLE").name("Custom Role")
                .roleType(RoleType.CUSTOM).active(true).build();
    }

    @Test
    @DisplayName("GET / — roles.manage yo'q → 403")
    void list_forbidden() throws Exception {
        auth("other");
        mockMvc.perform(get(BASE)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET / — sort whitelist + pagination")
    void list_authorized() throws Exception {
        auth("roles.manage");
        UUID id = UUID.randomUUID();
        Page<RoleResponse> page = new PageImpl<>(List.of(buildResponse(id)), Pageable.ofSize(20), 1);
        when(roleAdminService.getAll(any(), any())).thenReturn(page);

        mockMvc.perform(get(BASE + "?search=admin&sort=code,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].code").value("CUSTOM_ROLE"));

        verify(roleAdminService).getAll(eq("admin"), any());
    }

    @Test
    @DisplayName("GET /{id} — single role")
    void getById_authorized() throws Exception {
        auth("roles.manage");
        UUID id = UUID.randomUUID();
        when(roleAdminService.getById(id)).thenReturn(buildResponse(id));

        mockMvc.perform(get(BASE + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("CUSTOM_ROLE"));
    }

    @Test
    @DisplayName("POST / — created 201")
    void create_returns201() throws Exception {
        auth("roles.manage");
        UUID id = UUID.randomUUID();
        when(roleAdminService.create(any())).thenReturn(buildResponse(id));

        mockMvc.perform(post(BASE).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"NEW_ROLE","name":"New Role","roleType":"CUSTOM","permissionIds":[]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("CUSTOM_ROLE"));
    }

    @Test
    @DisplayName("POST / — invalid body 400")
    void create_invalidBody() throws Exception {
        auth("roles.manage");

        mockMvc.perform(post(BASE).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /{id} — role updated")
    void update_returns200() throws Exception {
        auth("roles.manage");
        UUID id = UUID.randomUUID();
        when(roleAdminService.update(eq(id), any())).thenReturn(buildResponse(id));

        mockMvc.perform(put(BASE + "/" + id).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Updated","permissionIds":[]}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /{id} — 204 No Content")
    void delete_returns204() throws Exception {
        auth("roles.manage");
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete(BASE + "/" + id).with(csrf()))
                .andExpect(status().isNoContent());

        verify(roleAdminService).delete(id);
    }

    @Test
    @DisplayName("GET /permissions — admin")
    void getAllPermissions() throws Exception {
        auth("roles.manage");
        when(roleAdminService.getAllPermissions()).thenReturn(List.of());

        mockMvc.perform(get(BASE + "/permissions"))
                .andExpect(status().isOk());
    }
}
