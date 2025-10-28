package uz.hemis.app.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * University Access Validator
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Validate university-based access control</li>
 *   <li>Extract university code from JWT token</li>
 *   <li>Ensure universities can only access their own data</li>
 * </ul>
 *
 * <p><strong>JWT Token Structure:</strong></p>
 * <pre>
 * {
 *   "sub": "university_user_123",
 *   "university_code": "00123",
 *   "roles": ["UNIVERSITY_USER"],
 *   "permissions": ["READ_STUDENTS", "READ_TEACHERS"],
 *   "iss": "https://auth.hemis.uz/realms/hemis",
 *   "exp": 1234567890
 * }
 * </pre>
 *
 * <p><strong>Usage in Services:</strong></p>
 * <pre>
 * &#64;PreAuthorize("@universityAccessValidator.canAccessUniversity(authentication, #university)")
 * public Map&lt;String, Object&gt; getFaculties(String university) {
 *     // Only accessible if JWT token's university_code matches the parameter
 * }
 * </pre>
 *
 * @since 1.0.0
 */
@Component("universityAccessValidator")
@Slf4j
public class UniversityAccessValidator {

    /**
     * JWT claim for university code
     */
    private static final String CLAIM_UNIVERSITY_CODE = "university_code";

    /**
     * JWT claim for roles
     */
    private static final String CLAIM_ROLES = "roles";

    /**
     * Admin role name
     */
    private static final String ROLE_ADMIN = "ADMIN";

    /**
     * System role name (for internal services)
     */
    private static final String ROLE_SYSTEM = "SYSTEM";

    /**
     * Check if authenticated user can access data for specified university
     *
     * <p><strong>Access Rules:</strong></p>
     * <ul>
     *   <li>ADMIN role - can access any university</li>
     *   <li>SYSTEM role - can access any university (internal services)</li>
     *   <li>University user - can only access their own university</li>
     * </ul>
     *
     * @param authentication Spring Security authentication object
     * @param requestedUniversity University code from request parameter
     * @return true if access is allowed, false otherwise
     */
    public boolean canAccessUniversity(Authentication authentication, String requestedUniversity) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Access denied: No authentication");
            return false;
        }

        // Extract JWT token
        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            log.warn("Access denied: Invalid authentication principal");
            return false;
        }

        // Check if user has ADMIN or SYSTEM role (can access any university)
        if (hasRole(jwt, ROLE_ADMIN) || hasRole(jwt, ROLE_SYSTEM)) {
            log.debug("Access granted: User has ADMIN/SYSTEM role");
            return true;
        }

        // Extract university code from JWT token
        String tokenUniversityCode = jwt.getClaimAsString(CLAIM_UNIVERSITY_CODE);

        if (tokenUniversityCode == null || tokenUniversityCode.isEmpty()) {
            log.warn("Access denied: No university_code in JWT token");
            return false;
        }

        // Check if token's university matches requested university
        boolean hasAccess = tokenUniversityCode.equals(requestedUniversity);

        if (hasAccess) {
            log.debug("Access granted: University {} matches token", requestedUniversity);
        } else {
            log.warn("Access denied: University {} does not match token university {}",
                    requestedUniversity, tokenUniversityCode);
        }

        return hasAccess;
    }

    /**
     * Get university code from authenticated user's JWT token
     *
     * @param authentication Spring Security authentication object
     * @return University code or null if not found
     */
    public String getUniversityCode(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return null;
        }

        return jwt.getClaimAsString(CLAIM_UNIVERSITY_CODE);
    }

    /**
     * Check if user is admin
     *
     * @param authentication Spring Security authentication object
     * @return true if user has ADMIN role
     */
    public boolean isAdmin(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return false;
        }

        return hasRole(jwt, ROLE_ADMIN);
    }

    /**
     * Check if user is system (internal service)
     *
     * @param authentication Spring Security authentication object
     * @return true if user has SYSTEM role
     */
    public boolean isSystem(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return false;
        }

        return hasRole(jwt, ROLE_SYSTEM);
    }

    /**
     * Check if JWT token has specific role
     *
     * @param jwt JWT token
     * @param role Role name to check
     * @return true if role exists in token
     */
    private boolean hasRole(Jwt jwt, String role) {
        var roles = jwt.getClaimAsStringList(CLAIM_ROLES);
        return roles != null && (roles.contains(role) || roles.contains("ROLE_" + role));
    }

    /**
     * Validate that student belongs to university
     *
     * <p>Used when querying student data by PINFL</p>
     *
     * @param authentication Spring Security authentication object
     * @param studentUniversityCode University code from student record
     * @return true if access is allowed
     */
    public boolean canAccessStudent(Authentication authentication, String studentUniversityCode) {
        return canAccessUniversity(authentication, studentUniversityCode);
    }

    /**
     * Validate that teacher belongs to university
     *
     * <p>Used when querying teacher data by PINFL</p>
     *
     * @param authentication Spring Security authentication object
     * @param teacherUniversityCode University code from teacher record
     * @return true if access is allowed
     */
    public boolean canAccessTeacher(Authentication authentication, String teacherUniversityCode) {
        return canAccessUniversity(authentication, teacherUniversityCode);
    }
}
