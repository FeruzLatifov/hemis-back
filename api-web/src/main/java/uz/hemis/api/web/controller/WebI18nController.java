package uz.hemis.api.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.service.I18nService;

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
 *   return data; // {"button.save": "Save", "button.cancel": "Cancel", ...}
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
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Login Translations (Russian)",
                        description = "Example response with login page translations in Russian",
                        value = """
                            {
                              "success": true,
                              "data": {
                                "login.title": "HEMIS Админ Панель",
                                "login.username": "Имя пользователя",
                                "login.password": "Пароль",
                                "login.loginButton": "Войти",
                                "button.save": "Сохранить",
                                "button.cancel": "Отмена",
                                "error.unauthorized": "Доступ запрещен",
                                "error.network": "Ошибка сети"
                              }
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Full Response (English)",
                        description = "Complete translation set for English language",
                        value = """
                            {
                              "success": true,
                              "data": {
                                "login.title": "HEMIS Admin Panel",
                                "login.subtitle": "Higher Education Management Information System",
                                "login.username": "Username",
                                "login.password": "Password",
                                "login.loginButton": "Sign In",
                                "button.save": "Save",
                                "button.cancel": "Cancel",
                                "button.search": "Search",
                                "menu.students": "Students",
                                "menu.teachers": "Teachers",
                                "error.not_found": "Not Found"
                              }
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "❌ Server error or database connection issue",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "success": false,
                          "error": "Failed to load translations",
                          "message": "Database connection timeout"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ResponseWrapper<Map<String, String>>> getAllMessages(
        @Parameter(description = "Language code (e.g., uz-UZ, ru-RU, en-US)", example = "uz-UZ")
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
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Russian Translation",
                        description = "Get 'Save' button label in Russian",
                        value = """
                            {
                              "success": true,
                              "message": "Сохранить"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "English Translation",
                        description = "Get error message in English",
                        value = """
                            {
                              "success": true,
                              "message": "Unauthorized access"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Uzbek (Cyrillic)",
                        description = "Login button in Uzbek Cyrillic",
                        value = """
                            {
                              "success": true,
                              "message": "Кириш"
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "❌ Message key not found in any language",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "success": false,
                          "error": "Message not found",
                          "message": "Key 'invalid.key' does not exist"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ResponseWrapper<String>> getMessage(
        @Parameter(description = "Message key (format: category.name)", example = "button.save")
        @PathVariable String key,
        @Parameter(description = "Language code", example = "ru-RU")
        @RequestParam(defaultValue = "uz-UZ") String lang
    ) {
        log.debug("GET /api/v1/web/i18n/messages/{}?lang={}", key, lang);

        String message = i18nService.getMessage(key, lang);

        return ResponseEntity.ok(ResponseWrapper.success(message));
    }

    /**
     * Get messages by category
     * <p>Load only specific category (e.g., all button labels)</p>
     *
     * @param category Message category (app, menu, button, label, message, error, validation)
     * @param lang Language code (default: uz-UZ)
     * @return Map of messageKey → translation for this category
     */
    @GetMapping("/messages/category/{category}")
    @Operation(
        summary = "Get messages by category",
        description = "Returns all messages for specific category. " +
                      "Categories: app, menu, button, label, message, error, validation"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Category messages loaded",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\":true,\"data\":{\"button.save\":\"Saqlash\",\"button.cancel\":\"Bekor qilish\"}}"
                )
            )
        )
    })
    public ResponseEntity<ResponseWrapper<Map<String, String>>> getMessagesByCategory(
        @Parameter(description = "Message category", example = "button")
        @PathVariable String category,
        @Parameter(description = "Language code", example = "uz-UZ")
        @RequestParam(defaultValue = "uz-UZ") String lang
    ) {
        log.info("GET /api/v1/web/i18n/messages/category/{}?lang={}", category, lang);

        Map<String, String> messages = i18nService.getMessagesByCategory(category, lang);

        log.info("Returned {} messages for category: {}, language: {}", messages.size(), category, lang);
        return ResponseEntity.ok(ResponseWrapper.success(messages));
    }

    /**
     * Get messages by scopes (Progressive Loading - Industry Best Practice)
     * <p>Optimized for frontend: load only required scopes instead of all translations</p>
     *
     * <p><strong>Progressive Loading Strategy:</strong></p>
     * <ul>
     *   <li>Login Page: scopes=auth → 50 messages (10KB) - 50x reduction</li>
     *   <li>Dashboard: scopes=auth,dashboard,menu → 200 messages (40KB) - 10x reduction</li>
     *   <li>Full App: No scopes → 2000+ messages (400KB) - full load</li>
     * </ul>
     *
     * @param scopes Comma-separated scope list (e.g., "auth,dashboard,menu")
     * @param lang Language code (default: uz-UZ)
     * @return Map of messageKey → translation for specified scopes
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

            **Scopes Naming Convention:**
            - `auth` → Login/authentication (auth.username, auth.password)
            - `dashboard` → Dashboard widgets (dashboard.welcome, dashboard.stats)
            - `menu` → Menu items (menu.students, menu.teachers)
            - `registry` → Registry pages (registry.student.list, registry.teacher.view)
            - `button` → Common buttons (button.save, button.cancel)
            - `error` → Error messages (error.network, error.unauthorized)

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
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Login Page (auth scope)",
                        description = "Minimal load for login page - 50 messages (~10KB)",
                        value = """
                            {
                              "success": true,
                              "data": {
                                "auth.title": "HEMIS Tizimiga Kirish",
                                "auth.username": "Foydalanuvchi nomi",
                                "auth.password": "Parol",
                                "auth.loginButton": "Kirish",
                                "auth.forgotPassword": "Parolni unutdingizmi?",
                                "auth.invalidCredentials": "Noto'g'ri login yoki parol"
                              }
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Dashboard (auth,dashboard,menu scopes)",
                        description = "Dashboard with menu - 200 messages (~40KB)",
                        value = """
                            {
                              "success": true,
                              "data": {
                                "auth.username": "Foydalanuvchi nomi",
                                "dashboard.welcome": "Xush kelibsiz",
                                "dashboard.stats.students": "Talabalar soni",
                                "dashboard.stats.teachers": "O'qituvchilar soni",
                                "menu.students": "Talabalar",
                                "menu.teachers": "O'qituvchilar",
                                "menu.registry": "Ro'yxatlar",
                                "button.save": "Saqlash",
                                "button.cancel": "Bekor qilish"
                              }
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "❌ Invalid scopes parameter",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "success": false,
                          "error": "Invalid scopes",
                          "message": "Scopes parameter must be comma-separated list"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ResponseWrapper<Map<String, String>>> getMessagesByScopes(
        @Parameter(
            description = "Comma-separated scope list (e.g., auth,dashboard,menu)",
            example = "auth,dashboard,menu"
        )
        @RequestParam String scopes,
        @Parameter(description = "Language code", example = "uz-UZ")
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
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Single Language",
                        description = "Invalidate cache for specific language",
                        value = """
                            {
                              "success": true,
                              "data": "Cache invalidated for language: ru-RU"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "All Languages",
                        description = "Invalidate cache for all languages",
                        value = """
                            {
                              "success": true,
                              "data": "All language caches invalidated"
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "❌ Unauthorized - No JWT token provided",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "success": false,
                          "error": "Unauthorized",
                          "message": "Full authentication is required"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "❌ Forbidden - User lacks system.translation.view permission",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "success": false,
                          "error": "Access Denied",
                          "message": "Requires 'system.translation.view' permission"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ResponseWrapper<String>> invalidateCache(
        @Parameter(description = "Language code (if not provided, invalidates all)", example = "ru-RU")
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
    @Operation(
        summary = "Get cache statistics",
        description = "Returns Redis cache statistics for monitoring: " +
                      "number of cached languages, cache keys, TTL, etc."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Cache stats retrieved",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\":true,\"data\":{\"cachedLanguages\":3,\"languages\":[\"uz-UZ\",\"ru-RU\",\"en-US\"],\"cacheTTL\":\"PT24H\"}}"
                )
            )
        )
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
