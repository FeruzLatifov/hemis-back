package uz.hemis.common.auth;

import java.util.Set;

/**
 * Data-scope of an authenticated caller — <em>which OTM(s) their requests may touch</em>.
 *
 * <p>This is the SECOND authorization dimension, orthogonal to the permission/function dimension
 * carried by {@link SubjectInfo#authorities()}. Effective access is always:</p>
 * <pre>hasAuthority(function)  AND  accessScope.allows(row.universityCode)</pre>
 *
 * <p>It is deliberately derived <strong>server-side from the authenticated subject</strong>
 * (a human's {@code users.university_id} + {@code user_type}, or a machine client's tenancy),
 * never from an untrusted request parameter. A {@code universityCode} arriving on the request
 * is only ever <em>validated against</em> this scope (see {@link #allows(String)}), never used to
 * define it. This is the substrate that closes the cross-OTM IDOR on the analytics surface.</p>
 *
 * <p><strong>Three shapes:</strong></p>
 * <ul>
 *   <li>{@link #global()} — ministry / oversight / SYSTEM tier: may see every OTM (no row filter).</li>
 *   <li>{@link #restrictedTo(Set)} — OTM tier: may see only the listed university code(s)
 *       (usually one; a {@code Set} so multi-OTM oversight can be added additively later).</li>
 *   <li>{@link #denyAll()} — <strong>fail-closed default</strong>: an OTM-tier caller whose scope
 *       could not be resolved sees nothing, never everything.</li>
 * </ul>
 *
 * <p>Lives in {@code common} (no Spring / JPA) so {@code security}, {@code service} and
 * {@code api-web} can all consume it without violating the module DAG. SQL construction from a
 * scope belongs in the service layer, not here — this type only models the scope and answers
 * membership questions.</p>
 *
 * @param unrestricted     {@code true} → all OTMs allowed; {@code universityCodes} is ignored
 * @param universityCodes  when restricted, the exact set of allowed {@code hemishe_e_university.code}
 *                         values; empty = deny-all
 * @since 2.2.0
 */
public record AccessScope(boolean unrestricted, Set<String> universityCodes) {

    public AccessScope {
        universityCodes = universityCodes == null ? Set.of() : Set.copyOf(universityCodes);
    }

    /** Unrestricted scope — ministry / oversight / SYSTEM tier sees every OTM. */
    public static AccessScope global() {
        return new AccessScope(true, Set.of());
    }

    /**
     * Restricted to the given university code(s). An empty/blank-only set collapses to
     * {@link #denyAll()} so a mis-resolved OTM scope can never widen to global.
     */
    public static AccessScope restrictedTo(Set<String> universityCodes) {
        if (universityCodes == null || universityCodes.stream().allMatch(AccessScope::isBlank)) {
            return denyAll();
        }
        Set<String> cleaned = universityCodes.stream()
                .filter(c -> !isBlank(c))
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        return new AccessScope(false, cleaned);
    }

    /** Convenience for the common single-OTM human user. */
    public static AccessScope restrictedTo(String universityCode) {
        return isBlank(universityCode) ? denyAll() : new AccessScope(false, Set.of(universityCode));
    }

    /** Fail-closed empty scope: allows no OTM at all. The safe default when resolution fails. */
    public static AccessScope denyAll() {
        return new AccessScope(false, Set.of());
    }

    /**
     * Does this scope permit access to a given OTM's row?
     * Global allows everything; a restricted scope allows only its listed codes; deny-all allows nothing.
     * A {@code null} row code is treated as not-owned (denied for restricted scopes).
     */
    public boolean allows(String universityCode) {
        if (unrestricted) {
            return true;
        }
        return universityCode != null && universityCodes.contains(universityCode);
    }

    /** True when this scope permits no OTM at all (fail-closed / unresolved OTM caller). */
    public boolean isDenyAll() {
        return !unrestricted && universityCodes.isEmpty();
    }

    /**
     * Stable cache-key fragment identifying this scope — so per-caller scoped results never collide
     * across OTMs in a shared cache. {@code "*"} = global, {@code "!"} = deny-all, else the allowed
     * codes sorted and comma-joined (order-independent).
     */
    public String cacheKey() {
        if (unrestricted) {
            return "*";
        }
        if (universityCodes.isEmpty()) {
            return "!";
        }
        return universityCodes.stream().sorted().collect(java.util.stream.Collectors.joining(","));
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
