package uz.hemis.api.web.controller;

import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Translation Admin (View & Edit)", description = "View and edit system translations. No create/delete - keys managed by developers.")
@RestController
@RequestMapping("/api/v1/web/system/translation")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Authentication")
public class TranslationAdminController {

    private final TranslationAdminService translationService;
    private final I18nService i18nService;

    // =====================================================
    // View & Update Operations (No Create/Delete)
    // =====================================================

    /**
     * GET /api/v1/admin/translations
     * List all translations with pagination and filtering
     */
    @Operation(
        summary = "List translations",
        description = "Get paginated list of translations with optional filtering by category, search key, and active status"
    )
    @GetMapping
    @PreAuthorize("hasAuthority('system.translation.view')")
    public ResponseEntity<Map<String, Object>> listTranslations(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Boolean active,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "category") String sortBy,
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

    /**
     * GET /api/v1/web/system/translation/{id}
     * Get single translation by ID
     */
    @Operation(summary = "Get translation by ID", description = "Get single translation with all language variants")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system.translation.view')")
    public ResponseEntity<TranslationDto> getTranslation(@PathVariable UUID id) {
        log.info("GET /api/v1/web/system/translation/{}", id);

        return translationService.getTranslationById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }


    /**
     * PUT /api/v1/admin/translations/{id}
     * Update existing translation
     */
    @Operation(summary = "Update translation", description = "Update existing translation - all 4 languages")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system.translation.manage')")
    public ResponseEntity<TranslationDto> updateTranslation(
        @PathVariable UUID id,
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
     * POST /api/v1/admin/translations/cache/clear
     * Clear translation cache (manual button)
     */
    @Operation(summary = "Clear cache", description = "Manual cache refresh - user clicks button")
    @PostMapping("/cache/clear")
    @PreAuthorize("hasAuthority('system.translation.manage')")
    public ResponseEntity<Map<String, Object>> clearCache() {
        log.info("POST /api/v1/admin/translations/cache/clear - Manual cache clear requested");

        i18nService.clearCache();

        // Localized message (Accept-Language → Locale → languageTag mapping)
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

        // Try i18n; fallback to sensible defaults per language
        String key = "admin.translation.cache.cleared";
        String localized = i18nService.getMessage(key, language);
        if (key.equals(localized)) {
            localized = switch (language) {
                case "ru-RU" -> "Кэш переводов успешно очищен";
                case "en-US" -> "Translation cache cleared successfully";
                case "oz-UZ" -> "Таржима кеши муваффақиятли тозаланди";
                default -> "Tarjima keshi muvaffaqiyatli tozalandi";
            };
        }

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", localized,
            "timestamp", System.currentTimeMillis()
        ));
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

    /**
     * Alias endpoint to support older frontend path
     * POST /api/v1/web/system/translation/regenerate-properties
     */
    @Operation(hidden = true)
    @PostMapping("/regenerate-properties")
    @PreAuthorize("hasAuthority('system.translation.manage')")
    public ResponseEntity<Map<String, Object>> regeneratePropertiesFilesAlias() {
        log.info("POST /api/v1/admin/translations/regenerate-properties (alias)");
        Map<String, Object> results = translationService.regeneratePropertiesFiles();
        return ResponseEntity.ok(results);
    }

    // =====================================================
    // Statistics
    // =====================================================

    /**
     * GET /api/v1/admin/translations/stats
     * Get translation statistics
     */
    @Operation(summary = "Get statistics", description = "Get translation statistics and cache info")
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('system.translation.view')")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/v1/admin/translations/stats");

        Map<String, Object> stats = translationService.getStatistics();
        Map<String, Object> cacheStats = i18nService.getCacheStats();

        Map<String, Object> response = new HashMap<>();
        response.put("translations", stats);
        response.put("cache", cacheStats);

        return ResponseEntity.ok(response);
    }

}
