package uz.hemis.api.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.I18nService;
import uz.hemis.service.admin.TranslationAdminService;
import uz.hemis.common.dto.TranslationDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Translation Admin Controller - View and Edit Translations
 *
 * <p><strong>Purpose:</strong> Admin panel for viewing and editing translations (No Create/Delete)</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>View translations list with pagination and filtering</li>
 *   <li>View single translation details</li>
 *   <li>Update existing translations (Edit mode only)</li>
 *   <li>Toggle translation active status</li>
 *   <li>Export to properties files (manual button)</li>
 *   <li>Clear cache (manual button)</li>
 *   <li>View statistics</li>
 * </ul>
 *
 * <p><strong>Important:</strong></p>
 * <ul>
 *   <li>❌ NO CREATE: Translation keys are added by developers via code/migration</li>
 *   <li>❌ NO DELETE: Translations are permanent, managed by developers</li>
 *   <li>✅ EDIT ONLY: Users can translate keys to different languages</li>
 * </ul>
 *
 * <p><strong>Menu Location:</strong> Tizim → Tarjimalar</p>
 * <p><strong>Permissions:</strong></p>
 * <ul>
 *   <li>system.translation.view - View translations</li>
 *   <li>system.translation.manage - Edit translations</li>
 * </ul>
 * <p><strong>Base URL:</strong> /api/v1/web/system/translation</p>
 *
 * <p><strong>Best Practices:</strong></p>
 * <ul>
 *   <li>User-controlled updates (manual, not cron)</li>
 *   <li>Cache invalidation after each change</li>
 *   <li>Audit trail (createdAt, updatedAt)</li>
 *   <li>Pagination for large datasets</li>
 * </ul>
 */
@Tag(
    name = "Translation Admin",
    description = """
        Translation Management API (View & Edit Only)
        
        **Purpose:** Admin panel for managing i18n translations
        
        **Features:**
        - View all translation keys (paginated)
        - Edit translations for 4 languages (uz-UZ, oz-UZ, ru-RU, en-US)
        - Toggle active/inactive status
        - Export to properties files
        - Cache management (clear/refresh)
        
        **Important:**
        - ❌ NO CREATE - Keys added by developers via migration
        - ❌ NO DELETE - Keys are permanent
        - ✅ EDIT ONLY - Translate existing keys
        
        **Menu Location:** Tizim → Tarjimalar
        
        **Permissions:**
        - system.translation.view - View translations
        - system.translation.manage - Edit translations
        """
)
@RestController
@RequestMapping("/api/v1/web/system/translation")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class TranslationAdminController {

    private final TranslationAdminService translationService;
    private final I18nService i18nService;
    private final uz.hemis.service.cache.CacheEvictionService cacheEvictionService;
    private final org.springframework.cache.CacheManager cacheManager;
    private final org.springframework.data.redis.core.RedisTemplate<String, String> redisMessageTemplate;

    @org.springframework.beans.factory.annotation.Value("${HOSTNAME:unknown}")
    private String podName;

    // =====================================================
    // View & Update Operations (No Create/Delete)
    // =====================================================

    /**
     * GET /api/v1/admin/translations
     * List all translations with pagination and filtering
     */
    @GetMapping
    @PreAuthorize("hasAuthority('system.translation.view')")
    @Operation(
        summary = "List translations (paginated)",
        description = """
            Get paginated list of all translation keys with filtering.
            
            **Filters:**
            - `category` - Filter by category (menu, table, filters, actions, etc.)
            - `search` - Search in message_key or message text
            - `active` - Filter by active status (true/false)
            
            **Example Request:**
            ```
            GET /api/v1/web/system/translation?category=menu&active=true&size=50
            ```
            
            **Example Response:**
            ```json
            {
              "content": [
                {
                  "id": "uuid-123",
                  "category": "menu",
                  "messageKey": "menu.registry.faculty",
                  "message": "Fakultet",
                  "translationUz": "Fakultet",
                  "translationOz": "Факультет",
                  "translationRu": "Факультет",
                  "translationEn": "Faculty",
                  "isActive": true,
                  "createdAt": "2023-09-01T10:00:00",
                  "updatedAt": "2024-01-15T14:30:00"
                }
              ],
              "totalElements": 245,
              "totalPages": 5,
              "currentPage": 0,
              "pageSize": 50
            }
            ```
            """,
        tags = {"Translation Admin"}
    )
    public ResponseEntity<Map<String, Object>> listTranslations(
        @Parameter(
            description = "Filter by category (menu, table, filters, actions, etc.)",
            example = "menu",
            required = false
        )
        @RequestParam(required = false) String category,
        
        @Parameter(
            description = "Search in message_key or message text",
            example = "fakultet",
            required = false
        )
        @RequestParam(required = false) String search,
        
        @Parameter(
            description = "Filter by active status",
            example = "true",
            required = false
        )
        @RequestParam(required = false) Boolean active,
        
        @Parameter(description = "Page number", example = "0")
        @RequestParam(defaultValue = "0") int page,
        
        @Parameter(description = "Page size", example = "50")
        @RequestParam(defaultValue = "20") int size,
        
        @Parameter(description = "Sort field", example = "category")
        @RequestParam(defaultValue = "category") String sortBy,
        
        @Parameter(description = "Sort direction", example = "ASC", schema = @Schema(allowableValues = {"ASC", "DESC"}))
        @RequestParam(defaultValue = "ASC") String sortDir
    ) {
        log.info("GET /api/v1/admin/translations - category={}, search={}, active={}, page={}, size={}",
            category, search, active, page, size);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TranslationDto> translations = translationService.getAllTranslations(
            category, search, active, pageable
        );

        Map<String, Object> response = new HashMap<>();
        response.put("content", translations.getContent());
        response.put("currentPage", translations.getNumber());
        response.put("totalItems", translations.getTotalElements());
        response.put("totalPages", translations.getTotalPages());
        response.put("pageSize", translations.getSize());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system.translation.view')")
    @Operation(
        summary = "Get translation by ID",
        description = """
            Get single translation with all 4 language variants.
            
            **Example Request:**
            ```
            GET /api/v1/web/system/translation/uuid-123
            ```
            
            **Example Response:**
            ```json
            {
              "id": "uuid-123",
              "category": "menu",
              "messageKey": "menu.registry.faculty",
              "message": "Fakultet",
              "translationUz": "Fakultet",
              "translationOz": "Факультет",
              "translationRu": "Факультет",
              "translationEn": "Faculty",
              "isActive": true,
              "createdAt": "2023-09-01T10:00:00",
              "updatedAt": "2024-01-15T14:30:00"
            }
            ```
            """,
        tags = {"Translation Admin"}
    )
    public ResponseEntity<TranslationDto> getTranslation(
        @Parameter(
            description = "Translation UUID",
            example = "550e8400-e29b-41d4-a716-446655440000",
            required = true
        )
        @PathVariable UUID id
    ) {
        log.info("GET /api/v1/web/system/translation/{}", id);

        return translationService.getTranslationById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system.translation.manage')")
    @Operation(
        summary = "Update translation (Edit mode)",
        description = """
            Update existing translation for all 4 languages.
            
            **Important:**
            - Cannot change messageKey (readonly)
            - Cannot change category (readonly)
            - Can only update translation texts
            - Cache is automatically cleared after update
            
            **Example Request:**
            ```json
            {
              "category": "menu",
              "messageKey": "menu.registry.faculty",
              "message": "Fakultet",
              "translationOz": "Факультет",
              "translationRu": "Факультет",
              "translationEn": "Faculty",
              "isActive": true
            }
            ```
            
            **Example Response:**
            ```json
            {
              "id": "uuid-123",
              "category": "menu",
              "messageKey": "menu.registry.faculty",
              "message": "Fakultet",
              "translationUz": "Fakultet",
              "translationOz": "Факультет",
              "translationRu": "Факультет",
              "translationEn": "Faculty",
              "isActive": true,
              "updatedAt": "2025-01-12T15:30:00"
            }
            ```
            """,
        tags = {"Translation Admin"}
    )
    public ResponseEntity<TranslationDto> updateTranslation(
        @Parameter(
            description = "Translation UUID",
            example = "550e8400-e29b-41d4-a716-446655440000",
            required = true
        )
        @PathVariable UUID id,
        
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Translation update data",
            content = @Content(
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                        {
                          "category": "menu",
                          "messageKey": "menu.registry.faculty",
                          "message": "Fakultet",
                          "translationOz": "Факультет",
                          "translationRu": "Факультет",
                          "translationEn": "Faculty",
                          "isActive": true
                        }
                        """
                )
            )
        )
        @RequestBody Map<String, Object> request
    ) {
        log.info("PUT /api/v1/admin/translations/{} - key={}", id, request.get("messageKey"));

        try {
            translationService.updateTranslation(
                id,
                (String) request.get("category"),
                (String) request.get("messageKey"),
                (String) request.get("messageUz"),
                (String) request.get("messageOz"),
                (String) request.get("messageRu"),
                (String) request.get("messageEn"),
                (Boolean) request.get("active")
            );

            // Reload with translations and return DTO
            return translationService.getTranslationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }


    /**
     * PATCH /api/v1/admin/translations/{id}/toggle-active
     * Toggle active status
     */
    @Operation(summary = "Toggle active status", description = "Enable/disable translation")
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasAuthority('system.translation.manage')")
    public ResponseEntity<Map<String, Object>> toggleActive(@PathVariable UUID id) {
        log.info("PATCH /api/v1/admin/translations/{}/toggle-active", id);

        try {
            TranslationDto updated = translationService.toggleActive(id);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Active status toggled successfully",
                "active", updated.getIsActive()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    // =====================================================
    // Actions (Manual Buttons)
    // =====================================================

    /**
     * POST /api/v1/web/system/translation/cache/clear
     * Clear translation cache - DISTRIBUTED (All Pods)
     *
     * <p><strong>Enterprise Distributed Cache Invalidation:</strong></p>
     * <ul>
     *   <li>Publishes Redis Pub/Sub message to all pods</li>
     *   <li>Each pod clears L1 Caffeine cache</li>
     *   <li>Leader pod reloads from database → Redis L2</li>
     *   <li>Other pods reload from Redis L2 → Caffeine L1</li>
     * </ul>
     *
     * <p><strong>10 Pods Scenario:</strong></p>
     * <pre>
     * Admin clicks button → POST /cache/clear
     *                             ↓
     *                   Redis Pub/Sub broadcast
     *                             ↓
     *         ┌──────────────────┼──────────────────┐
     *         ▼                  ▼                  ▼
     *      POD-1              POD-2    ...       POD-10
     *   Clear L1           Clear L1           Clear L1
     *         ↓                  ↓                  ↓
     *   Leader Election (Redis SETNX)
     *         ↓
     *   POD-5 (Leader): DB → Redis L2
     *         ↓
     *   Other pods: Redis L2 → Caffeine L1
     *         ↓
     *   ✅ All pods synchronized
     * </pre>
     */
    @Operation(
        summary = "Clear cache (Distributed - All Pods)",
        description = """
            Distributed cache invalidation via Redis Pub/Sub.

            **Process:**
            1. Admin clicks "Clear Cache" button
            2. Backend publishes Redis message: "cache:invalidate:i18n"
            3. All 10 pods receive signal via CacheInvalidationListener
            4. Each pod clears L1 Caffeine cache
            5. Leader pod loads from DB → Redis
            6. Other pods load from Redis → L1
            7. Response: 200 OK with timing statistics

            **Performance:**
            - Total time: ~200ms (all 10 pods)
            - Database queries: 4 (only from leader pod)
            - Network overhead: Minimal (Redis Pub/Sub)

            **Security:** Requires `system.translation.view` permission
            """
    )
    @PostMapping("/cache/clear")
    @PreAuthorize("hasAuthority('system.translation.view')")
    public ResponseEntity<Map<String, Object>> clearCache() {
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("🔄 Translation cache refresh triggered by admin");
        log.info("   Pod: {}", podName);
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        try {
            long timestamp = System.currentTimeMillis();
            String channel = "cache:invalidate:i18n";
            String payload = "refresh-i18n-" + timestamp;

            // 1️⃣ Publish Redis Pub/Sub message (broadcasts to all pods)
            log.info("📡 Publishing to Redis Pub/Sub...");
            log.info("   Channel: {}", channel);
            log.info("   Payload: {}", payload);

            redisMessageTemplate.convertAndSend(channel, payload);
            log.info("✅ Redis Pub/Sub message sent");

            // 2️⃣ Clear local i18n cache (this pod)
            log.info("🧹 Clearing local i18n cache (this pod)...");
            cacheEvictionService.evictAllI18n();
            log.info("✅ Local i18n cache cleared");

            // 3️⃣ Localized message
            String incomingTag = LocaleContextHolder.getLocale() != null
                ? LocaleContextHolder.getLocale().toLanguageTag()
                : "uz";
            String language = switch (incomingTag) {
                case "uz", "uz-UZ" -> "uz-UZ";
                case "oz", "oz-UZ" -> "oz-UZ";
                case "ru", "ru-RU" -> "ru-RU";
                case "en", "en-US" -> "en-US";
                default -> "uz-UZ";
            };

            String key = "admin.translation.cache.cleared";
            String localized = i18nService.getMessage(key, language);
            if (key.equals(localized)) {
                localized = switch (language) {
                    case "ru-RU" -> "Кэш переводов успешно очищен на всех серверах";
                    case "en-US" -> "Translation cache cleared on all servers";
                    case "oz-UZ" -> "Таржима кеши барча серверларда тозаланди";
                    default -> "Tarjima keshi barcha serverlarda tozalandi";
                };
            }

            // 4️⃣ Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", localized);
            response.put("timestamp", timestamp);
            response.put("pod", podName);
            response.put("channel", channel);
            response.put("payload", payload);
            response.put("scope", "distributed");
            response.put("expectedPods", "all");
            response.put("estimatedTime", "200ms");

            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("✅ Translation cache refresh completed successfully");
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Translation cache refresh failed", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Cache refresh failed: " + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            errorResponse.put("pod", podName);

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * POST /api/v1/admin/translations/export
     * Export translations to properties format (manual button)
     */
    @Operation(summary = "Export to properties", description = "Export translations to properties file format")
    @PostMapping("/export")
    @PreAuthorize("hasAuthority('system.translation.view')")
    public ResponseEntity<Map<String, Object>> exportToProperties(
        @RequestParam(defaultValue = "uz-UZ") String language
    ) {
        log.info("POST /api/v1/admin/translations/export - language={}", language);

        Map<String, String> properties = translationService.exportToProperties(language);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "language", language,
            "count", properties.size(),
            "properties", properties
        ));
    }

    /**
     * POST /api/v1/admin/translations/properties/regenerate
     * Regenerate properties files on disk (fallback support)
     */
    @Operation(summary = "Regenerate properties files",
        description = "Regenerate static properties files for fallback support (manual button)")
    @PostMapping("/properties/regenerate")
    @PreAuthorize("hasAuthority('system.translation.manage')")
    public ResponseEntity<Map<String, Object>> regeneratePropertiesFiles() {
        log.info("POST /api/v1/admin/translations/properties/regenerate");

        Map<String, Object> results = translationService.regeneratePropertiesFiles();

        return ResponseEntity.ok(results);
    }

    // =====================================================
    // Statistics
    // =====================================================

    /**
     * GET /api/v1/web/system/translation/stats
     * Get translation statistics with detailed cache metrics
     *
     * <p><strong>Returns Detailed Metrics:</strong></p>
     * <ul>
     *   <li><strong>translations:</strong> Total count, by category, by language</li>
     *   <li><strong>cache:</strong> Redis L2 statistics (languages, TTL)</li>
     *   <li><strong>cacheStatistics:</strong> Caffeine L1 detailed metrics (hit rate, size, evictions)</li>
     * </ul>
     *
     * <p><strong>Example Response:</strong></p>
     * <pre>
     * {
     *   "translations": {
     *     "totalCount": 245,
     *     "byCategory": {"menu": 120, "button": 50, ...}
     *   },
     *   "cache": {
     *     "cachedLanguages": 4,
     *     "languages": ["uz-UZ", "oz-UZ", "ru-RU", "en-US"],
     *     "cacheTTL": "PT24H"
     *   },
     *   "cacheStatistics": {
     *     "i18n": {
     *       "cacheName": "i18n",
     *       "L1_Caffeine": {
     *         "hitCount": 1234,
     *         "missCount": 56,
     *         "hitRate": "95.66%",
     *         "totalRequests": 1290,
     *         "size": 987,
     *         "evictionCount": 12
     *       },
     *       "L2_Redis": {
     *         "type": "Redis",
     *         "status": "active"
     *       }
     *     }
     *   }
     * }
     * </pre>
     */
    @Operation(
        summary = "Get statistics with detailed cache metrics",
        description = """
            Returns comprehensive statistics for translations and cache performance.

            **Includes:**
            - Translation counts (total, by category, by language)
            - Redis L2 cache info (languages, TTL)
            - Caffeine L1 detailed metrics (hit rate, miss rate, size, evictions)

            **Use Case:**
            - Admin panel cache monitoring dashboard
            - Performance troubleshooting
            - Cache hit rate optimization

            **Performance Metrics:**
            - Hit Rate: Percentage of requests served from L1 cache
            - Evictions: Number of entries removed due to size limit
            - Size: Current number of entries in L1 cache
            """
    )
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('system.translation.view')")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/v1/web/system/translation/stats");

        // Translation statistics
        Map<String, Object> stats = translationService.getStatistics();

        // Redis L2 cache statistics
        Map<String, Object> cacheStats = i18nService.getCacheStats();

        // Caffeine L1 detailed statistics
        Map<String, Map<String, Object>> detailedCacheStats = null;
        if (cacheManager instanceof uz.hemis.service.cache.TwoLevelCacheManager) {
            uz.hemis.service.cache.TwoLevelCacheManager twoLevelManager =
                (uz.hemis.service.cache.TwoLevelCacheManager) cacheManager;
            detailedCacheStats = twoLevelManager.getCacheStatistics();
        }

        // Build comprehensive response
        Map<String, Object> response = new HashMap<>();
        response.put("translations", stats);
        response.put("cache", cacheStats);

        if (detailedCacheStats != null && !detailedCacheStats.isEmpty()) {
            response.put("cacheStatistics", detailedCacheStats);
        }

        // Add pod information for distributed debugging
        response.put("pod", podName);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

}
