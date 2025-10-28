package uz.hemis.security.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JWT Granted Authorities Converter
 *
 * <p>Extracts roles/authorities from JWT claims and converts them to Spring Security GrantedAuthority.</p>
 *
 * <p><strong>Supported JWT Claim Structures:</strong></p>
 * <ul>
 *   <li><strong>roles</strong> claim: List of role names (e.g., ["ADMIN", "USER"])</li>
 *   <li><strong>authorities</strong> claim: List of authority names</li>
 *   <li><strong>realm_access.roles</strong>: Keycloak-style nested roles</li>
 *   <li><strong>resource_access.{client-id}.roles</strong>: Keycloak client-specific roles</li>
 * </ul>
 *
 * <p><strong>Role Prefix:</strong></p>
 * <p>All roles are prefixed with "ROLE_" (Spring Security convention).</p>
 * <p>Example: "ADMIN" → "ROLE_ADMIN"</p>
 *
 * <p><strong>Legacy Compatibility:</strong></p>
 * <ul>
 *   <li>Preserves CUBA Platform role names (ADMIN, USER, UNIVERSITY_ADMIN, etc.)</li>
 *   <li>Case-insensitive role matching</li>
 *   <li>Supports multiple claim structures for flexibility</li>
 * </ul>
 *
 * @since 1.0.0
 */
public class JwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String ROLE_PREFIX = "ROLE_";
    private static final String ROLES_CLAIM = "roles";
    private static final String AUTHORITIES_CLAIM = "authorities";
    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String RESOURCE_ACCESS_CLAIM = "resource_access";

    /**
     * Convert JWT to collection of GrantedAuthority
     *
     * <p>Extracts roles from multiple possible claim locations:</p>
     * <ol>
     *   <li>Direct 'roles' claim (simple list)</li>
     *   <li>Direct 'authorities' claim (simple list)</li>
     *   <li>'realm_access.roles' (Keycloak realm roles)</li>
     *   <li>'resource_access.{client}.roles' (Keycloak client roles)</li>
     * </ol>
     *
     * @param jwt JWT token
     * @return collection of granted authorities
     */
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Extract roles from 'roles' claim
        authorities.addAll(extractRolesFromClaim(jwt, ROLES_CLAIM));

        // Extract roles from 'authorities' claim
        authorities.addAll(extractRolesFromClaim(jwt, AUTHORITIES_CLAIM));

        // Extract Keycloak realm roles
        authorities.addAll(extractKeycloakRealmRoles(jwt));

        // Extract Keycloak resource/client roles
        authorities.addAll(extractKeycloakResourceRoles(jwt));

        // Remove duplicates
        return authorities.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Extract roles from simple list claim
     *
     * <p>Expects claim structure: ["ADMIN", "USER"]</p>
     *
     * @param jwt JWT token
     * @param claimName claim name (e.g., "roles", "authorities")
     * @return list of granted authorities
     */
    private List<GrantedAuthority> extractRolesFromClaim(Jwt jwt, String claimName) {
        List<String> roles = jwt.getClaimAsStringList(claimName);

        if (roles == null || roles.isEmpty()) {
            return List.of();
        }

        return roles.stream()
                .map(this::convertToAuthority)
                .collect(Collectors.toList());
    }

    /**
     * Extract Keycloak realm roles
     *
     * <p>Expects claim structure:</p>
     * <pre>
     * {
     *   "realm_access": {
     *     "roles": ["ADMIN", "USER"]
     *   }
     * }
     * </pre>
     *
     * @param jwt JWT token
     * @return list of granted authorities
     */
    @SuppressWarnings("unchecked")
    private List<GrantedAuthority> extractKeycloakRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim(REALM_ACCESS_CLAIM);

        if (realmAccess == null) {
            return List.of();
        }

        Object rolesObj = realmAccess.get(ROLES_CLAIM);

        if (rolesObj instanceof List<?>) {
            List<String> roles = (List<String>) rolesObj;
            return roles.stream()
                    .map(this::convertToAuthority)
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    /**
     * Extract Keycloak resource/client roles
     *
     * <p>Expects claim structure:</p>
     * <pre>
     * {
     *   "resource_access": {
     *     "hemis-api": {
     *       "roles": ["ADMIN", "USER"]
     *     }
     *   }
     * }
     * </pre>
     *
     * @param jwt JWT token
     * @return list of granted authorities
     */
    @SuppressWarnings("unchecked")
    private List<GrantedAuthority> extractKeycloakResourceRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaim(RESOURCE_ACCESS_CLAIM);

        if (resourceAccess == null) {
            return List.of();
        }

        List<GrantedAuthority> authorities = new ArrayList<>();

        // Iterate over all clients in resource_access
        for (Map.Entry<String, Object> entry : resourceAccess.entrySet()) {
            if (entry.getValue() instanceof Map) {
                Map<String, Object> clientAccess = (Map<String, Object>) entry.getValue();
                Object rolesObj = clientAccess.get(ROLES_CLAIM);

                if (rolesObj instanceof List<?>) {
                    List<String> roles = (List<String>) rolesObj;
                    authorities.addAll(
                            roles.stream()
                                    .map(this::convertToAuthority)
                                    .collect(Collectors.toList())
                    );
                }
            }
        }

        return authorities;
    }

    /**
     * Convert role name to GrantedAuthority
     *
     * <p>Adds "ROLE_" prefix if not already present.</p>
     * <p>Converts to uppercase for consistency.</p>
     *
     * <p><strong>Examples:</strong></p>
     * <ul>
     *   <li>"admin" → "ROLE_ADMIN"</li>
     *   <li>"ADMIN" → "ROLE_ADMIN"</li>
     *   <li>"ROLE_ADMIN" → "ROLE_ADMIN"</li>
     * </ul>
     *
     * @param role role name
     * @return GrantedAuthority
     */
    private GrantedAuthority convertToAuthority(String role) {
        String roleName = role.toUpperCase().trim();

        if (!roleName.startsWith(ROLE_PREFIX)) {
            roleName = ROLE_PREFIX + roleName;
        }

        return new SimpleGrantedAuthority(roleName);
    }

    // =====================================================
    // Legacy CUBA Platform Roles (Preserved)
    // =====================================================
    // Common roles in HEMIS:
    // - ROLE_ADMIN          - System administrators
    // - ROLE_UNIVERSITY_ADMIN - University administrators
    // - ROLE_USER           - Regular users
    // - ROLE_TEACHER        - Teachers/staff
    // - ROLE_STUDENT        - Students (read-only access)
    //
    // All roles are case-insensitive and prefixed with ROLE_
    // =====================================================
}
