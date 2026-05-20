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
import uz.hemis.common.dto.student.StudentDictionariesDto;
import uz.hemis.common.dto.student.StudentStatsDto;
import uz.hemis.service.student.StudentWebService;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = StudentWebController.class,
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class
)
@Import(GlobalExceptionHandler.class)
@DisplayName("StudentWebController — frontend student list + stats")
class StudentWebControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private StudentWebService studentWebService;
    @MockitoBean private uz.hemis.service.shared.I18nService i18nService;

    @SpringBootApplication
    @EnableMethodSecurity
    static class TestApp {}

    private static final String BASE = "/api/v1/web/students";

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
    @DisplayName("GET / — students.view yo'q → 403")
    void list_forbidden() throws Exception {
        auth("other");
        mockMvc.perform(get(BASE)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET / — students.view + filter chain → 200")
    void list_authorized() throws Exception {
        auth("students.view");
        Page<Object> empty = new PageImpl<>(List.of(), Pageable.ofSize(20), 0);
        when(studentWebService.searchStudents(any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any())).thenReturn((Page) empty);

        mockMvc.perform(get(BASE + "?university=337&course=1&educationType=11"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /{id} — by UUID")
    void getById_200() throws Exception {
        auth("students.view");
        UUID id = UUID.randomUUID();
        when(studentWebService.getStudentById(id)).thenReturn(null);

        mockMvc.perform(get(BASE + "/" + id))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /by-code/{code}")
    void getByCode() throws Exception {
        auth("students.view");
        when(studentWebService.getStudentByCode("401242311234")).thenReturn(null);

        mockMvc.perform(get(BASE + "/by-code/401242311234"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /stats — dashboard counts")
    void stats() throws Exception {
        auth("students.view");
        when(studentWebService.getStats(any()))
                .thenReturn(new StudentStatsDto(1000L, 400L, 600L, 50L));

        mockMvc.perform(get(BASE + "/stats?university=337"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1000));
    }

    @Test
    @DisplayName("GET /dictionaries — filter classifiers")
    void dictionaries() throws Exception {
        auth("students.view");
        when(studentWebService.getDictionaries())
                .thenReturn(StudentDictionariesDto.builder().build());

        mockMvc.perform(get(BASE + "/dictionaries"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /directions — students.directions.view permission")
    void directions_forbidden() throws Exception {
        auth("students.view");
        mockMvc.perform(get(BASE + "/directions"))
                .andExpect(status().isForbidden());
    }
}
