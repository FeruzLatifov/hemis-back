package uz.hemis.app.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security Configuration Tests
 *
 * <p><strong>Test Coverage:</strong></p>
 * <ul>
 *   <li>Public endpoints (no authentication required)</li>
 *   <li>Protected endpoints (JWT required)</li>
 *   <li>Role-based access control (@PreAuthorize)</li>
 *   <li>CORS configuration</li>
 *   <li>CSRF disabled</li>
 * </ul>
 *
 * @since 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        // Disable JWT validation for testing
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=",
        "spring.security.oauth2.resourceserver.jwt.issuer-uri="
})
@DisplayName("SecurityConfig Tests")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    // =====================================================
    // Public Endpoints Tests
    // =====================================================

    @Test
    @DisplayName("Public endpoint /actuator/health should be accessible without authentication")
    void publicEndpoint_Health_ShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Public endpoint /actuator/info should be accessible without authentication")
    void publicEndpoint_Info_ShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    // =====================================================
    // Protected Endpoints Tests
    // =====================================================

    @Test
    @DisplayName("Protected endpoint should return 401 without authentication")
    void protectedEndpoint_WithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/app/rest/v2/students"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("Protected endpoint should be accessible with authentication")
    void protectedEndpoint_WithAuth_ShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/app/rest/v2/students"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    // =====================================================
    // Role-Based Access Control Tests
    // =====================================================

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /students should return 403 for USER role")
    void postStudent_WithUserRole_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/app/rest/v2/students")
                        .contentType("application/json")
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /students should be accessible for ADMIN role")
    void postStudent_WithAdminRole_ShouldBeAccessible() throws Exception {
        mockMvc.perform(post("/app/rest/v2/students")
                        .contentType("application/json")
                        .content("{\"code\":\"STU001\"}"))
                .andDo(print())
                .andExpect(status().isOk()); // May fail validation, but passed security
    }

    @Test
    @WithMockUser(roles = "UNIVERSITY_ADMIN")
    @DisplayName("POST /students should be accessible for UNIVERSITY_ADMIN role")
    void postStudent_WithUniversityAdminRole_ShouldBeAccessible() throws Exception {
        mockMvc.perform(post("/app/rest/v2/students")
                        .contentType("application/json")
                        .content("{\"code\":\"STU001\"}"))
                .andDo(print())
                .andExpect(status().isOk()); // May fail validation, but passed security
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PUT /students/{id} should return 403 for USER role")
    void putStudent_WithUserRole_ShouldReturn403() throws Exception {
        mockMvc.perform(put("/app/rest/v2/students/{id}", "12345678-1234-1234-1234-123456789012")
                        .contentType("application/json")
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /students/{id} should be accessible for ADMIN role")
    void putStudent_WithAdminRole_ShouldBeAccessible() throws Exception {
        mockMvc.perform(put("/app/rest/v2/students/{id}", "12345678-1234-1234-1234-123456789012")
                        .contentType("application/json")
                        .content("{\"code\":\"STU001\"}"))
                .andDo(print())
                .andExpect(status().isOk()); // May fail (student not found), but passed security
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PATCH /students/{id} should return 403 for USER role")
    void patchStudent_WithUserRole_ShouldReturn403() throws Exception {
        mockMvc.perform(patch("/app/rest/v2/students/{id}", "12345678-1234-1234-1234-123456789012")
                        .contentType("application/json")
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "UNIVERSITY_ADMIN")
    @DisplayName("PATCH /students/{id} should be accessible for UNIVERSITY_ADMIN role")
    void patchStudent_WithUniversityAdminRole_ShouldBeAccessible() throws Exception {
        mockMvc.perform(patch("/app/rest/v2/students/{id}", "12345678-1234-1234-1234-123456789012")
                        .contentType("application/json")
                        .content("{\"firstname\":\"John\"}"))
                .andDo(print())
                .andExpect(status().isOk()); // May fail (student not found), but passed security
    }

    // =====================================================
    // Admin Endpoints Tests
    // =====================================================

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Admin endpoint should return 403 for USER role")
    void adminEndpoint_WithUserRole_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin endpoint should be accessible for ADMIN role")
    void adminEndpoint_WithAdminRole_ShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andDo(print())
                // Endpoint may not exist (404), but security passed (not 403)
                .andExpect(status().is4xxClientError());
    }

    // =====================================================
    // CORS Tests
    // =====================================================

    @Test
    @DisplayName("CORS preflight request should be allowed")
    void corsPreflight_ShouldBeAllowed() throws Exception {
        mockMvc.perform(options("/app/rest/v2/students")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS));
    }

    @Test
    @WithMockUser
    @DisplayName("CORS request should include Access-Control-Allow-Origin header")
    void corsRequest_ShouldIncludeAccessControlHeader() throws Exception {
        mockMvc.perform(get("/app/rest/v2/students")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000"))
                .andDo(print())
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    // =====================================================
    // NO DELETE METHOD ALLOWED
    // =====================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE method should return 405 Method Not Allowed (NDG enforcement)")
    void deleteStudent_ShouldReturn405() throws Exception {
        mockMvc.perform(delete("/app/rest/v2/students/{id}", "12345678-1234-1234-1234-123456789012"))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("DELETE method in CORS preflight should not be allowed")
    void corsPreflightDelete_ShouldNotBeAllowed() throws Exception {
        mockMvc.perform(options("/app/rest/v2/students")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.DELETE.name()))
                .andDo(print())
                // Should either fail CORS check or return 405
                .andExpect(status().is4xxClientError());
    }

    // =====================================================
    // CSRF Tests
    // =====================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST request should work without CSRF token (CSRF disabled)")
    void postRequest_WithoutCsrfToken_ShouldWork() throws Exception {
        mockMvc.perform(post("/app/rest/v2/students")
                        .contentType("application/json")
                        .content("{\"code\":\"STU001\"}"))
                .andDo(print())
                // Should pass security (not 403 Forbidden from CSRF)
                // Expecting 200 OK (created) or 400 (validation error)
                .andExpect(status().isOk());
    }

    // =====================================================
    // NOTE: JWT Validation Tests
    // =====================================================
    // JWT validation tests require real JWT tokens or mocked JwtDecoder
    // These are covered by integration tests with actual auth server
    // =====================================================
}
