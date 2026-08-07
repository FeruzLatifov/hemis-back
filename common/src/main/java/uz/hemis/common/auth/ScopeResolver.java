package uz.hemis.common.auth;

/**
 * Resolves the {@link AccessScope} (OTM data-boundary) of the current authenticated caller.
 *
 * <p>The interface lives in {@code common} so that {@code service} beans (report / rating / dashboard,
 * which do NOT depend on the {@code security} module) can inject it; the implementation lives in
 * {@code security} where the authenticated {@link SubjectInfo} is available.</p>
 *
 * <p><strong>Fail-closed contract:</strong> when the caller is anonymous, unresolvable, or an OTM-tier
 * user with no university binding, this returns {@link AccessScope#denyAll()} — never {@link AccessScope#global()}.
 * Global is granted only to explicitly ministry/system-tier subjects.</p>
 *
 * <p>Scope is always derived from the authenticated subject (a human's {@code users.university_id} +
 * {@code user_type}, resolved server-side; a machine client's tenancy claim), NEVER from a request
 * parameter. Callers may only <em>validate</em> a request-supplied {@code universityCode} against the
 * returned scope via {@link AccessScope#allows(String)}.</p>
 *
 * @since 2.2.0
 */
public interface ScopeResolver {

    /**
     * Resolve the current caller's OTM data-scope from the security context.
     *
     * @return the caller's {@link AccessScope}; {@link AccessScope#denyAll()} when unresolvable
     */
    AccessScope currentScope();

    /**
     * Stable cache-key fragment for the current caller's scope — use in {@code @Cacheable} keys of
     * scope-sensitive results so entries never leak across OTMs. Equivalent to
     * {@code currentScope().cacheKey()}.
     */
    default String currentScopeKey() {
        return currentScope().cacheKey();
    }
}
