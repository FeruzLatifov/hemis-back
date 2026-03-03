package uz.hemis.app.controller;

/**
 * REMOVED — Replaced by WebAuthController
 *
 * <p>This controller was mapping POST /api/admin/login with
 * subject=username in JWT (incorrect). The correct implementation is in
 * {@code uz.hemis.web.controller.WebAuthController} at /api/v1/web/auth/login
 * which uses HTTPOnly cookies, CSRF, rate limiting, audit events, and account lockout.</p>
 *
 * <p>Removed to prevent:</p>
 * <ul>
 *   <li>JWT sub=username breaking MenuController, WebAuthController.getCurrentUser()</li>
 *   <li>No rate limiting (brute force vulnerability)</li>
 *   <li>No account lockout</li>
 *   <li>No audit trail</li>
 *   <li>Hardcoded /me response</li>
 * </ul>
 *
 * @deprecated Use WebAuthController (api-web module) at /api/v1/web/auth/login
 * @since 2.0.0
 */
// Intentionally left empty — class kept for git history reference
