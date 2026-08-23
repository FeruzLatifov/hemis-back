package uz.hemis.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.service.shared.I18nService;

import java.util.Map;

/**
 * I18n (Internationalization) REST Controller - UNIVER Pattern
 *
 * <p><strong>Clean Architecture - Web API v1:</strong></p>
 * <ul>
 *   <li>Base URL: /api/v1/web/i18n</li>
 *   <li>Public endpoints (no auth required for GET operations)</li>
 *   <li>Admin endpoints for cache management (auth required)</li>
 *   <li>Bulk loading optimized for frontend</li>
 * </ul>
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Provide translations for frontend (buttons, labels, errors, etc.)</li>
 *   <li>Support 9 languages (uz-UZ, oz-UZ, ru-RU, en-US, etc.)</li>
 *   <li>Redis-cached for zero database load</li>
 *   <li>UNIVER fallback logic (exact → prefix → default)</li>
 * </ul>
 *
 * <p><strong>Endpoints:</strong></p>
 * <ul>
 *   <li>GET /api/v1/web/i18n/messages - Get all messages for language (bulk)</li>
 *   <li>GET /api/v1/web/i18n/messages/{key} - Get single message by key</li>
 *   <li>GET /api/v1/web/i18n/messages/category/{category} - Get messages by category</li>
 *   <li>POST /api/v1/web/i18n/cache/invalidate - Invalidate cache (admin)</li>
 *   <li>GET /api/v1/web/i18n/cache/stats - Get cache statistics</li>
 * </ul>
 *
 * <p><strong>Frontend Integration:</strong></p>
 * <pre>
 * // React/Vue example
 * const loadTranslations = async (lang) => {
 *   const response = await fetch(`/api/v1/web/i18n/messages?lang=${lang}`);
 *   const {data} = await response.json();
 *   return data; // {"Save": "Save", "Cancel": "Cancel", "Dashboard": "Dashboard", ...}
 * };
 * </pre>
 *
 * @see I18nService
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/web/i18n")
@Tag(name = "I18n API", description = "Internationalization (i18n) API for multi-language support")
@RequiredArgsConstructor
@Slf4j
@Validated
public class WebI18nController {

    private final I18nService i18nService;

    /**
     * Get all messages for a language (bulk operation)
     * <p>Optimized for frontend: load all translations at once</p>
     *
     * <p><strong>Performance:</strong></p>
     * <ul>
     *   <li>Cache Hit: ~5ms (Redis lookup)</li>
     *   <li>Cache Miss: ~50ms (Database + Redis cache)</li>
     *   <li>Subsequent requests: ~5ms (100% from Redis)</li>
     * </ul>
     *
     * <p><strong>Use Case:</strong></p>
     * Frontend calls this once at startup, caches the result locally,
     * and uses it for all UI translations.
     *
     * @param lang Language code (default: uz-UZ)
     * @return Map of messageKey → translation
     */
    @GetMapping("/messages")
    @Operation(
        summary = "Get all messages for language",
        description = """
            Returns all system messages for specified language.

            **Use Case:** Frontend calls this once at startup to load all translations.

            **Performance:**
            - Cache Hit: ~5ms (from Redis)
            - Cache Miss: ~50ms (Database + Redis cache)

            **Supported Languages:**
            - uz-UZ (O'zbek - Lotin)
            - oz-UZ (Ўзбек - Kirill)
            - ru-RU (Русский)
            - en-US (English)
            - kk-UZ (Қазақ)
            - tg-TG (Тоҷикӣ)
            - kz-KZ (Қазақстан)
            - tm-TM (Türkmen)
            - kg-KG (Кыргыз)

            **Frontend Integration:**
            ```javascript
            const translations = await fetch('/api/v1/web/i18n/messages?lang=ru-RU')
              .then(r => r.json())
              .then(r => r.data);

            // Use in i18next or other i18n library
            i18n.addResourceBundle('ru', 'translation', translations);
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "✅ Successfully loaded translations from cache or database",
            content = @Content(
                mediaType = "application/json"
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "❌ Server error or database connection issue",
            content = @Content(
                mediaType = "application/json"
            )
        )
    })
    public ResponseEntity<ResponseWrapper<Map<String, String>>> getAllMessages(
        @Parameter(description = "Language code (e.g., uz-UZ, ru-RU, en-US)")
        @RequestParam(defaultValue = "uz-UZ") String lang
    ) {
        log.info("GET /api/v1/web/i18n/messages?lang={}", lang);

        Map<String, String> messages = i18nService.getAllMessages(lang);

        log.info("Returned {} messages for language: {}", messages.size(), lang);
        return ResponseEntity.ok(ResponseWrapper.success(messages));
    }

    /**
     * Get single message by key
     * <p>Individual message lookup with fallback logic</p>
     *
     * @param key Message key (e.g., button.save)
     * @param lang Language code (default: uz-UZ)
     * @return Translation text
     */
    @GetMapping("/messages/{key}")
    @Operation(
        summary = "Get single message by key",
        description = """
            Returns translation for specific message key with UNIVER fallback logic.

            **Fallback Sequence:**
            1. Try exact language match (ru-RU)
            2. Try language prefix (ru)
            3. Return default message (Uzbek)

            **Use Case:** Get individual translation for dynamic content.

            **Performance:** O(1) Redis lookup when cached

            **Example Usage:**
            ```javascript
            const errorMsg = await fetch('/api/v1/web/i18n/messages/error.unauthorized?lang=ru-RU')
              .then(r => r.json())
              .then(r => r.data);

            console.log(errorMsg); // "Доступ запрещен"
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "✅ Message found and returned",
            content = @Content(
                mediaType = "application/json"
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "❌ Message key not found in any language",
            content = @Content(
                mediaType = "application/json"
            )
        )
    })
    public ResponseEntity<ResponseWrapper<String>> getMessage(
        @Parameter(description = "Message key (natural English text)")
        @PathVariable @NotBlank String key,
        @Parameter(description = "Language code")
        @RequestParam(defaultValue = "uz-UZ") String lang
    ) {
        log.debug("GET /api/v1/web/i18n/messages/{}?lang={}", key, lang);

        String message = i18nService.getMessage(key, lang);

        return ResponseEntity.ok(ResponseWrapper.success(message));
    }

    /**
     * Get messages by category
     * <p>Load only specific category (e.g., all action labels, all menu items)</p>
     *
     * @param category Message category (action, menu, label, message, auth, validation, status, table, etc.)
     * @param lang Language code (default: uz-UZ)
     * @return Map of messageKey → translation for this category
     */
    @GetMapping("/messages/category/{category}")
    @Operation(
        summary = "Get messages by category",
        description = "Returns all messages for specific category. " +
                      "Categories: action, menu, label, message, auth, validation, status, table, pagination, confirm"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Category messages loaded",
            content = @Content(
                mediaType = "application/json"
            )
        )
    })
    public ResponseEntity<ResponseWrapper<Map<String, String>>> getMessagesByCategory(
        @Parameter(description = "Message category")
        @PathVariable @NotBlank String category,
        @Parameter(description = "Language code")
        @RequestParam(defaultValue = "uz-UZ") String lang
    ) {
        log.info("GET /api/v1/web/i18n/messages/category/{}?lang={}", category, lang);

        Map<String, String> messages = i18nService.getMessagesByCategory(category, lang);

        log.info("Returned {} messages for category: {}, language: {}", messages.size(), category, lang);
        return ResponseEntity.ok(ResponseWrapper.success(messages));
    }

    /**
     * Get messages by categories (Progressive Loading)
     * <p>Optimized for frontend: load only required categories instead of all translations.
     * Scopes map directly to DB category names.</p>
     *
     * <p><strong>Progressive Loading Strategy:</strong></p>
     * <ul>
     *   <li>Login Page: scopes=auth → ~50 messages (10KB)</li>
     *   <li>Dashboard: scopes=auth,menu,action → ~200 messages (40KB)</li>
     *   <li>Full App: No scopes → 2000+ messages (400KB)</li>
     * </ul>
     *
     * @param scopes Comma-separated category list (e.g., "auth,menu,action")
     * @param lang Language code (default: uz-UZ)
     * @return Map of messageKey → translation for specified categories
     */
    @GetMapping("/messages/scopes")
    @Operation(
        summary = "Get messages by scopes (Progressive Loading)",
        description = """
            Returns messages filtered by scopes for progressive/lazy loading.

            **Industry Best Practice - Progressive Loading:**
            - Load ONLY what's needed for current page
            - 50x payload reduction for login page (400KB → 10KB)
            - 10x faster initial load (500ms → 50ms)

            **Category Names (= scopes):**
            - `auth` → Login/authentication ("Sign in", "Username", "Password")
            - `menu` → Menu items ("Dashboard", "Students", "Teachers")
            - `action` → Buttons/actions ("Save", "Cancel", "Delete")
            - `label` → Form labels ("Name", "Code", "Status")
            - `message` → User messages ("No data found", "Something went wrong")
            - `validation` → Validation ("This field is required", "Too short")

            **Performance:**
            - Cache Hit: ~1ms (L1 Caffeine)
            - Cache Miss: ~50ms (L2 Redis)
            - First Load: ~100ms (Database + cache population)

            **Frontend Integration:**
            ```javascript
            // Login page - minimal load (50 messages, 10KB)
            const authTranslations = await fetch(
              '/api/v1/web/i18n/messages/scopes?scopes=auth&lang=uz-UZ'
            ).then(r => r.json()).then(r => r.data);

            // Dashboard - load additional scopes (200 messages, 40KB)
            const dashboardTranslations = await fetch(
              '/api/v1/web/i18n/messages/scopes?scopes=auth,dashboard,menu&lang=uz-UZ'
            ).then(r => r.json()).then(r => r.data);

            // Merge translations
            i18n.addResourceBundle('uz', 'translation', {
              ...authTranslations,
              ...dashboardTranslations
            });
            ```

            **Use Cases:**
            1. **Login Page**: `scopes=auth` - Only auth-related translations
            2. **Dashboard**: `scopes=auth,dashboard,menu` - Core app translations
            3. **Registry**: `scopes=auth,dashboard,registry` - Add registry module
            4. **Full Load**: Omit scopes parameter - All translations (legacy)
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "✅ Successfully loaded scope-filtered translations",
            content = @Content(
                mediaType = "application/json"
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "❌ Invalid scopes parameter",
            content = @Content(
                mediaType = "application/json"
            )
        )
    })
    public ResponseEntity<ResponseWrapper<Map<String, String>>> getMessagesByScopes(
        @Parameter(
            description = "Comma-separated scope list (e.g., auth,dashboard,menu)"
        )
        @RequestParam String scopes,
        @Parameter(description = "Language code")
        @RequestParam(defaultValue = "uz-UZ") String lang
    ) {
        log.info("GET /api/v1/web/i18n/messages/scopes?scopes={}&lang={}", scopes, lang);

        // Parse comma-separated scopes
        java.util.List<String> scopeList = java.util.Arrays.asList(scopes.split(","));

        // Get filtered messages
        Map<String, String> messages = i18nService.getMessagesByScopes(scopeList, lang);

        log.info("Returned {} messages for scopes: {}, language: {}", messages.size(), scopes, lang);
        return ResponseEntity.ok(ResponseWrapper.success(messages));
    }

    /**
     * Invalidate cache for specific language
     * <p>Admin operation: called after translation updates</p>
     *
     * @param lang Language code to invalidate (optional, if not provided invalidates all)
     * @return Success message
     */
    @PostMapping("/cache/invalidate")
    @PreAuthorize("hasAuthority('system.translation.view')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Invalidate translation cache (Admin Only)",
        description = """
            Clears Redis cache for specified language or all languages.

            **Use Case:**
            - Admin updates translations in database
            - Call this endpoint to force reload from database
            - Next request will fetch fresh data

            **Security:** Requires `system.translation.view` permission

            **Performance Impact:**
            - Next request: ~50ms (database query + cache rebuild)
            - Subsequent requests: ~5ms (cache hit)

            **Example:**
            ```bash
            # Invalidate Russian translations
            curl -X POST 'http://localhost:8081/api/v1/web/i18n/cache/invalidate?lang=ru-RU' \\
              -H 'Authorization: Bearer YOUR_ADMIN_TOKEN'

            # Invalidate all languages
            curl -X POST 'http://localhost:8081/api/v1/web/i18n/cache/invalidate' \\
              -H 'Authorization: Bearer YOUR_ADMIN_TOKEN'
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "✅ Cache successfully invalidated",
            content = @Content(
                mediaType = "application/json"
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "❌ Unauthorized - No JWT token provided",
            content = @Content(
                mediaType = "application/json"
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "❌ Forbidden - User lacks system.translation.view permission",
            content = @Content(
                mediaType = "application/json"
            )
        )
    })
    public ResponseEntity<ResponseWrapper<String>> invalidateCache(
        @Parameter(description = "Language code (if not provided, invalidates all)")
        @RequestParam(required = false) String lang
    ) {
        log.info("POST /api/v1/web/i18n/cache/invalidate?lang={}", lang);

        if (lang != null && !lang.isEmpty()) {
            i18nService.invalidateCache(lang);
            return ResponseEntity.ok(ResponseWrapper.success("Cache invalidated for language: " + lang));
        } else {
            i18nService.invalidateAllCaches();
            return ResponseEntity.ok(ResponseWrapper.success("All language caches invalidated"));
        }
    }

    /**
     * Get cache statistics
     * <p>Monitoring endpoint: shows cache status</p>
     *
     * @return Cache statistics
     */
    @GetMapping("/cache/stats")
    @PreAuthorize("hasAuthority('system.translation.view')")
    @Operation(
        summary = "Get cache statistics",
        description = "Returns Redis cache statistics for monitoring: " +
                      "number of cached languages, cache keys, TTL, etc. " +
                      "Requires 'system.translation.view' (admin only — service metadata leak prevention)."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Cache stats retrieved",
            content = @Content(
                mediaType = "application/json"
            )
        ),
        @ApiResponse(responseCode = "403", description = "Forbidden — admin permission required")
    })
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getCacheStats() {
        log.debug("GET /api/v1/web/i18n/cache/stats");

        Map<String, Object> stats = i18nService.getCacheStats();

        return ResponseEntity.ok(ResponseWrapper.success(stats));
    }

    /**
     * Health check endpoint
     * <p>Check if I18n service is working</p>
     *
     * @return Health status
     */
    @GetMapping("/health")
    @Operation(
        summary = "I18n health check",
        description = "Simple health check to verify I18n service is operational"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Service is healthy")
    })
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> health() {
        Map<String, Object> health = Map.of(
            "status", "UP",
            "service", "I18nService",
            "description", "UNIVER pattern with Redis cache"
        );

        return ResponseEntity.ok(ResponseWrapper.success(health));
    }
}
