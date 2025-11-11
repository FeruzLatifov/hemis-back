package uz.hemis.security.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for JwtGrantedAuthoritiesConverter
 *
 * <p><strong>Test Coverage:</strong></p>
 * <ul>
 *   <li>Simple 'roles' claim extraction</li>
 *   <li>Simple 'authorities' claim extraction</li>
 *   <li>Keycloak 'realm_access.roles' extraction</li>
 *   <li>Keycloak 'resource_access.{client}.roles' extraction</li>
 *   <li>Role prefix handling (ROLE_)</li>
 *   <li>Case normalization (uppercase)</li>
 *   <li>Duplicate removal</li>
 * </ul>
 *
 * @since 1.0.0
 */
@DisplayName("JwtGrantedAuthoritiesConverter Tests")
class JwtGrantedAuthoritiesConverterTest {

    private JwtGrantedAuthoritiesConverter converter;

    @BeforeEach
    void setUp() {
        converter = new JwtGrantedAuthoritiesConverter();
    }

    // =====================================================
    // Simple Claims Tests
    // =====================================================

    @Test
    @DisplayName("Should extract roles from 'roles' claim")
    void extractRoles_FromRolesClaim() {
        // Given
        Jwt jwt = createJwt(Map.of("roles", List.of("ADMIN", "USER")));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities).hasSize(2);
        assertThat(authorities).extracting("authority")
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    @DisplayName("Should extract roles from 'authorities' claim")
    void extractRoles_FromAuthoritiesClaim() {
        // Given
        Jwt jwt = createJwt(Map.of("authorities", List.of("ADMIN", "TEACHER")));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities).hasSize(2);
        assertThat(authorities).extracting("authority")
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_TEACHER");
    }

    @Test
    @DisplayName("Should add ROLE_ prefix if not present")
    void addRolePrefix_IfNotPresent() {
        // Given
        Jwt jwt = createJwt(Map.of("roles", List.of("admin", "user")));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities).extracting("authority")
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    @DisplayName("Should not duplicate ROLE_ prefix")
    void notDuplicateRolePrefix() {
        // Given
        Jwt jwt = createJwt(Map.of("roles", List.of("ROLE_ADMIN", "ROLE_USER")));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities).extracting("authority")
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    @DisplayName("Should normalize roles to uppercase")
    void normalizeRolesToUppercase() {
        // Given
        Jwt jwt = createJwt(Map.of("roles", List.of("admin", "User", "TEACHER")));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities).extracting("authority")
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER", "ROLE_TEACHER");
    }

    // =====================================================
    // Keycloak Realm Roles Tests
    // =====================================================

    @Test
    @DisplayName("Should extract Keycloak realm roles")
    void extractKeycloakRealmRoles() {
        // Given
        Map<String, Object> realmAccess = Map.of("roles", List.of("ADMIN", "USER"));
        Jwt jwt = createJwt(Map.of("realm_access", realmAccess));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities).hasSize(2);
        assertThat(authorities).extracting("authority")
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
    }

    // =====================================================
    // Keycloak Resource Roles Tests
    // =====================================================

    @Test
    @DisplayName("Should extract Keycloak resource/client roles")
    void extractKeycloakResourceRoles() {
        // Given
        Map<String, Object> hemisApiAccess = Map.of("roles", List.of("ADMIN", "UNIVERSITY_ADMIN"));
        Map<String, Object> resourceAccess = Map.of("hemis-api", hemisApiAccess);
        Jwt jwt = createJwt(Map.of("resource_access", resourceAccess));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities).hasSize(2);
        assertThat(authorities).extracting("authority")
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_UNIVERSITY_ADMIN");
    }

    @Test
    @DisplayName("Should extract roles from multiple resource clients")
    void extractRolesFromMultipleClients() {
        // Given
        Map<String, Object> hemisApiAccess = Map.of("roles", List.of("ADMIN"));
        Map<String, Object> otherClientAccess = Map.of("roles", List.of("USER"));
        Map<String, Object> resourceAccess = Map.of(
                "hemis-api", hemisApiAccess,
                "other-client", otherClientAccess
        );
        Jwt jwt = createJwt(Map.of("resource_access", resourceAccess));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities).hasSize(2);
        assertThat(authorities).extracting("authority")
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
    }

    // =====================================================
    // Combined Claims Tests
    // =====================================================

    @Test
    @DisplayName("Should extract roles from multiple claims")
    void extractRolesFromMultipleClaims() {
        // Given
        Map<String, Object> realmAccess = Map.of("roles", List.of("ADMIN"));
        Map<String, Object> hemisApiAccess = Map.of("roles", List.of("UNIVERSITY_ADMIN"));
        Map<String, Object> resourceAccess = Map.of("hemis-api", hemisApiAccess);

        Jwt jwt = createJwt(Map.of(
                "roles", List.of("USER"),
                "authorities", List.of("TEACHER"),
                "realm_access", realmAccess,
                "resource_access", resourceAccess
        ));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities).hasSize(4);
        assertThat(authorities).extracting("authority")
                .containsExactlyInAnyOrder(
                        "ROLE_USER",
                        "ROLE_TEACHER",
                        "ROLE_ADMIN",
                        "ROLE_UNIVERSITY_ADMIN"
                );
    }

    @Test
    @DisplayName("Should remove duplicate roles from multiple claims")
    void removeDuplicateRoles() {
        // Given
        Map<String, Object> realmAccess = Map.of("roles", List.of("ADMIN"));
        Jwt jwt = createJwt(Map.of(
                "roles", List.of("ADMIN", "USER"),
                "authorities", List.of("admin", "TEACHER"), // lowercase 'admin' should be deduplicated
                "realm_access", realmAccess
        ));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities).hasSize(3); // ADMIN deduplicated
        assertThat(authorities).extracting("authority")
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER", "ROLE_TEACHER");
    }

    // =====================================================
    // Empty Claims Tests
    // =====================================================

    @Test
    @DisplayName("Should return empty collection when no roles present")
    void returnEmptyWhenNoRoles() {
        // Given
        Jwt jwt = createJwt(Map.of("sub", "user123"));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities).isEmpty();
    }

    @Test
    @DisplayName("Should handle null roles claim")
    void handleNullRolesClaim() {
        // Given
        Jwt jwt = createJwt(Map.of("roles", null));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities).isEmpty();
    }

    @Test
    @DisplayName("Should handle empty roles list")
    void handleEmptyRolesList() {
        // Given
        Jwt jwt = createJwt(Map.of("roles", List.of()));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities).isEmpty();
    }

    // =====================================================
    // Helper Methods
    // =====================================================

    /**
     * Create test JWT with specified claims
     *
     * @param claims JWT claims
     * @return JWT instance
     */
    private Jwt createJwt(Map<String, Object> claims) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .subject("test-user")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .claims(claimsMap -> claimsMap.putAll(claims))
                .build();
    }

    // =====================================================
    // Legacy CUBA Platform Roles Tests
    // =====================================================

    @Test
    @DisplayName("Should support legacy CUBA Platform roles")
    void supportLegacyCubaRoles() {
        // Given
        Jwt jwt = createJwt(Map.of(
                "roles", List.of(
                        "ADMIN",
                        "UNIVERSITY_ADMIN",
                        "USER",
                        "TEACHER",
                        "STUDENT"
                )
        ));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities).hasSize(5);
        assertThat(authorities).extracting("authority")
                .containsExactlyInAnyOrder(
                        "ROLE_ADMIN",
                        "ROLE_UNIVERSITY_ADMIN",
                        "ROLE_USER",
                        "ROLE_TEACHER",
                        "ROLE_STUDENT"
                );
    }
}
