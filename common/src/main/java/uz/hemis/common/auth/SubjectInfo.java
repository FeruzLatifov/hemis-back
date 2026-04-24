package uz.hemis.common.auth;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * Polymorphic subject abstraction — covers both human users and OAuth clients.
 *
 * <p>JWT {@code typ} claim determines which variant was issued:</p>
 * <ul>
 *   <li>{@link SubjectType#USER} — subject ID is {@code users.id}, {@code subjectName} is username</li>
 *   <li>{@link SubjectType#CLIENT} — subject ID is {@code oauth_client.id}, {@code subjectName} is {@code client_id}</li>
 * </ul>
 *
 * <p>Designed so authorization code (controllers, {@code @PreAuthorize}) can treat
 * both transparently — e.g. {@code #subject.hasAuthority('students.view')} works
 * for a rektor login and a univer_101 machine account alike.</p>
 *
 * <p>This record lives in {@code common} — no Spring / JPA dependencies —
 * so {@code api-legacy}, {@code api-web}, {@code security} can all reference it
 * without violating the module DAG (rules.md Module Guidelines).</p>
 *
 * @param type         USER or CLIENT discriminator
 * @param id           subject UUID (users.id or oauth_client.id)
 * @param subjectName  username (USER) or client_id (CLIENT) — never null
 * @param authorities  granted authority strings (permissions + role prefixes)
 * @param universityCode tenant scope — may be {@code null} for SYSTEM users / INTERNAL_SERVICE clients
 * @param employeeId   person identity — non-null only for USER linked to employee
 * @param clientType   machine account discriminator — non-null only for CLIENT subjects
 *
 * @since 2.1.0
 */
public record SubjectInfo(
        SubjectType type,
        UUID id,
        String subjectName,
        Set<String> authorities,
        String universityCode,
        UUID employeeId,
        ClientType clientType
) {

    public SubjectInfo {
        if (type == null) {
            throw new IllegalArgumentException("SubjectType is required");
        }
        if (id == null) {
            throw new IllegalArgumentException("Subject id is required");
        }
        if (subjectName == null || subjectName.isBlank()) {
            throw new IllegalArgumentException("subjectName is required");
        }
        authorities = authorities == null ? Set.of() : Set.copyOf(authorities);
    }

    /** Factory for human (USER) subject. */
    public static SubjectInfo forUser(UUID userId,
                                      String username,
                                      Set<String> authorities,
                                      String universityCode,
                                      UUID employeeId) {
        return new SubjectInfo(
                SubjectType.USER,
                userId,
                username,
                authorities == null ? Collections.emptySet() : authorities,
                universityCode,
                employeeId,
                null
        );
    }

    /** Factory for machine (CLIENT) subject. */
    public static SubjectInfo forClient(UUID clientPk,
                                        String clientId,
                                        Set<String> authorities,
                                        String universityCode,
                                        ClientType clientType) {
        return new SubjectInfo(
                SubjectType.CLIENT,
                clientPk,
                clientId,
                authorities == null ? Collections.emptySet() : authorities,
                universityCode,
                null,
                clientType
        );
    }

    public boolean isUser() {
        return type == SubjectType.USER;
    }

    public boolean isClient() {
        return type == SubjectType.CLIENT;
    }

    /**
     * Check if the subject holds a given authority (role or permission).
     * Used from SpEL: {@code @PreAuthorize("@subject.hasAuthority('students.view')")}.
     */
    public boolean hasAuthority(String authority) {
        return authority != null && authorities.contains(authority);
    }

    /**
     * Check if the subject has any of the given authorities.
     */
    public boolean hasAnyAuthority(String... required) {
        if (required == null || required.length == 0) {
            return false;
        }
        for (String a : required) {
            if (authorities.contains(a)) return true;
        }
        return false;
    }
}
