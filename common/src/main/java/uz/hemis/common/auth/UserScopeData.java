package uz.hemis.common.auth;

import uz.hemis.common.enums.UserType;

/**
 * Raw scope inputs for a human {@code users} row — the two columns that decide a caller's OTM data-scope.
 *
 * <p>Loaded from the DB by {@link uz.hemis.common.port.security.UserScopeLoadingPort} and turned into an
 * {@link AccessScope} by the scope resolver. Deliberately dumb data (no policy) so the tier→scope decision
 * lives in exactly one place (the resolver), not scattered.</p>
 *
 * @param userType        {@code users.user_type} — UNIVERSITY (one OTM) / MINISTRY / SYSTEM / ORGANIZATION
 * @param universityCode  {@code users.university_id} → {@code hemishe_e_university.code}; null for
 *                        ministry / system accounts
 * @since 2.2.0
 */
public record UserScopeData(UserType userType, String universityCode) {
}
