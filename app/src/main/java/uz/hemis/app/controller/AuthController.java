package uz.hemis.app.controller;

/**
 * REMOVED — Duplicate of LegacyOAuthTokenController
 *
 * <p>This controller was mapping POST /app/rest/v2/oauth/token with
 * subject=username in JWT (incorrect). The correct implementation is in
 * {@code uz.hemis.api.legacy.controller.auth.LegacyOAuthTokenController}
 * which uses TokenService with subject=userId (UUID).</p>
 *
 * <p>Removed to prevent:</p>
 * <ul>
 *   <li>Spring "Ambiguous mapping" conflict</li>
 *   <li>JWT sub=username breaking MenuController, JwtGrantedAuthoritiesConverter, etc.</li>
 *   <li>Missing rate limiting, audit, account lockout</li>
 * </ul>
 *
 * @deprecated Use LegacyOAuthTokenController (api-legacy module) instead
 * @since 2.0.0
 */
// Intentionally left empty — class kept for git history reference
