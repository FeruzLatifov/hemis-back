package uz.hemis.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.TranslationDto;
import uz.hemis.domain.entity.SystemMessage;
import uz.hemis.domain.entity.SystemMessageTranslation;
import uz.hemis.domain.entity.SystemMessageTranslationId;
import uz.hemis.domain.repository.SystemMessageRepository;
import uz.hemis.domain.repository.SystemMessageTranslationRepository;
import uz.hemis.service.I18nService;
import uz.hemis.service.event.TranslationCacheEventPublisher;
import uz.hemis.service.mapper.SystemMessageMapper;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Translation Admin Service - View and Edit Translations
 *
 * <p>Translation management service for admin users (No Create/Delete)</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>View operations: List, get by ID, search and filter</li>
 *   <li>Update operations: Edit existing translations only</li>
 *   <li>Toggle active status</li>
 *   <li>Cache invalidation after updates</li>
 *   <li>Export to properties files (manual button)</li>
 * </ul>
 *
 * <p><strong>Important:</strong></p>
 * <ul>
 *   <li>❌ NO CREATE: Translation keys are added by developers via code/migration</li>
 *   <li>❌ NO DELETE: Translations are permanent, managed by developers</li>
 *   <li>✅ EDIT ONLY: Users can translate keys to different languages</li>
 * </ul>
 *
 * <p><strong>Best Practices:</strong></p>
 * <ul>
 *   <li>User-controlled updates (manual, not cron)</li>
 *   <li>Cache refresh after each update</li>
 *   <li>Validation before save</li>
 *   <li>Audit trail (createdAt, updatedAt)</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TranslationAdminService {

    private final SystemMessageRepository systemMessageRepository;
    private final SystemMessageTranslationRepository translationRepository;
    private final I18nService i18nService;
    private final TranslationCacheEventPublisher eventPublisher;
    private final SystemMessageMapper messageMapper;

    // =====================================================
    // View & Update Operations (No Create/Delete)
    // =====================================================

    /**
     * Get all translations with pagination and filtering
     *
     * <p><strong>IMPORTANT:</strong> Returns DTOs to decouple API from entities</p>
     */
    @Transactional(readOnly = true)
    public Page<TranslationDto> getAllTranslations(
        String category,
        String searchKey,
        Boolean active,
        Pageable pageable
    ) {
        log.info("Getting translations: category={}, search={}, active={}, page={}",
            category, searchKey, active, pageable.getPageNumber());

        Specification<SystemMessage> spec = (root, query, cb) -> cb.conjunction();

        if (category != null && !category.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("category"), category));
        }

        if (searchKey != null && !searchKey.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                cb.or(
                    cb.like(cb.lower(root.get("messageKey")), "%" + searchKey.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("message")), "%" + searchKey.toLowerCase() + "%")
                ));
        }

        if (active != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("isActive"), active));
        }

        Page<SystemMessage> page = systemMessageRepository.findAll(spec, pageable);

        // Force-fetch translations to avoid LazyInitializationException
        // Convert to DTOs within transaction to ensure lazy collections are accessible
        List<TranslationDto> dtoList = page.getContent().stream()
            .map(msg -> {
                // Force Hibernate to initialize the translations collection
                if (msg.getTranslations() != null) {
                    msg.getTranslations().size(); // Trigger initialization
                }
                return messageMapper.toDto(msg);
            })
            .collect(java.util.stream.Collectors.toList());

        // Create a new Page with the DTOs
        return new org.springframework.data.domain.PageImpl<>(
            dtoList,
            pageable,
            page.getTotalElements()
        );
    }

    /**
     * Get single translation by ID with all languages
     */
    @Transactional(readOnly = true)
    public Optional<TranslationDto> getTranslationById(UUID id) {
        log.info("Getting translation by ID: {}", id);
        // Eagerly load translations to avoid LazyInitializationException
        return systemMessageRepository.findByIdWithTranslations(id)
            .map(messageMapper::toDto);
    }

    /**
     * Get translations by message key
     */
    @Transactional(readOnly = true)
    public Optional<TranslationDto> getTranslationByKey(String messageKey) {
        log.info("Getting translation by key: {}", messageKey);
        return systemMessageRepository.findByMessageKey(messageKey)
            .map(entity -> {
                // Force-fetch translations
                if (entity.getTranslations() != null) {
                    entity.getTranslations().size();
                }
                return messageMapper.toDto(entity);
            });
    }


    /**
     * Update existing translation
     */
    @Transactional
    public SystemMessage updateTranslation(
        UUID id,
        String category,
        String messageKey,
        String messageUz,
        String messageOz,
        String messageRu,
        String messageEn,
        Boolean active
    ) {
        log.info("Updating translation: id={}, key={}", id, messageKey);

        SystemMessage message = systemMessageRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Translation not found: " + id));

        // Enforce immutability for category and messageKey
        if (category != null && !category.equals(message.getCategory())) {
            throw new IllegalArgumentException("Category is immutable and cannot be changed");
        }
        if (messageKey != null && !messageKey.equals(message.getMessageKey())) {
            throw new IllegalArgumentException("Message key is immutable and cannot be changed");
        }

        // Update main message (only translatable content and active flag)
        message.setMessage(messageUz);
        message.setIsActive(active != null ? active : true);
        message.setUpdatedAt(LocalDateTime.now());

        message = systemMessageRepository.save(message);

        // Update translations
        updateTranslationsForLanguages(message, messageOz, messageRu, messageEn);

        // Clear cache
        i18nService.clearCache();

        // Publish event to other servers (Redis Pub/Sub)
        eventPublisher.publishTranslationUpdated(messageKey);

        log.info("✅ Translation updated: id={}, key={}", id, messageKey);
        return message;
    }


    /**
     * Toggle translation active status
     */
    @Transactional
    public TranslationDto toggleActive(UUID id) {
        log.info("Toggling translation active status: id={}", id);

        SystemMessage message = systemMessageRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Translation not found: " + id));

        message.setIsActive(!message.getIsActive());
        message.setUpdatedAt(LocalDateTime.now());
        message = systemMessageRepository.save(message);

        // Force-fetch translations
        if (message.getTranslations() != null) {
            message.getTranslations().size();
        }

        // Clear cache
        i18nService.clearCache();

        // Publish event to other servers (Redis Pub/Sub)
        eventPublisher.publishTranslationUpdated(message.getMessageKey());

        log.info("✅ Translation active toggled: id={}, active={}", id, message.getIsActive());
        return messageMapper.toDto(message);
    }

    // =====================================================
    // Helper Methods
    // =====================================================

    private void createTranslationsForLanguages(
        SystemMessage message,
        String messageOz,
        String messageRu,
        String messageEn
    ) {
        List<SystemMessageTranslation> translations = new ArrayList<>();

        // Uzbek Cyrillic (oz-UZ)
        if (messageOz != null && !messageOz.isEmpty()) {
            translations.add(createTranslation(message, "oz-UZ", messageOz));
        }

        // Russian (ru-RU)
        if (messageRu != null && !messageRu.isEmpty()) {
            translations.add(createTranslation(message, "ru-RU", messageRu));
        }

        // English (en-US)
        if (messageEn != null && !messageEn.isEmpty()) {
            translations.add(createTranslation(message, "en-US", messageEn));
        }

        if (!translations.isEmpty()) {
            translationRepository.saveAll(translations);
        }
    }

    private void updateTranslationsForLanguages(
        SystemMessage message,
        String messageOz,
        String messageRu,
        String messageEn
    ) {
        updateOrCreateTranslation(message, "oz-UZ", messageOz);
        updateOrCreateTranslation(message, "ru-RU", messageRu);
        updateOrCreateTranslation(message, "en-US", messageEn);
    }

    private void updateOrCreateTranslation(SystemMessage message, String language, String translation) {
        if (translation == null || translation.isEmpty()) {
            return;
        }

        SystemMessageTranslationId id = new SystemMessageTranslationId(message.getId(), language);

        Optional<SystemMessageTranslation> existing = translationRepository.findById(id);

        if (existing.isPresent()) {
            // Update existing
            SystemMessageTranslation trans = existing.get();
            trans.setTranslation(translation);
            trans.setUpdatedAt(LocalDateTime.now());
            translationRepository.save(trans);
        } else {
            // Create new
            SystemMessageTranslation trans = createTranslation(message, language, translation);
            translationRepository.save(trans);
        }
    }

    private SystemMessageTranslation createTranslation(
        SystemMessage message,
        String language,
        String translation
    ) {
        SystemMessageTranslation trans = new SystemMessageTranslation();
        trans.setMessageId(message.getId());
        trans.setLanguage(language);
        trans.setTranslation(translation);
        trans.setSystemMessage(message);
        return trans;
    }

    // =====================================================
    // Statistics
    // =====================================================

    /**
     * Get translation statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistics() {
        log.info("Getting translation statistics");

        long totalMessages = systemMessageRepository.count();
        long activeMessages = systemMessageRepository.countByIsActive(true);
        long totalTranslations = translationRepository.count();

        // Count by category
        List<Object[]> categoryCount = systemMessageRepository.countByCategory();
        Map<String, Long> categoryCounts = new HashMap<>();
        for (Object[] row : categoryCount) {
            categoryCounts.put((String) row[0], ((Number) row[1]).longValue());
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMessages", totalMessages);
        stats.put("activeMessages", activeMessages);
        stats.put("inactiveMessages", totalMessages - activeMessages);
        stats.put("totalTranslations", totalTranslations);
        stats.put("categoryBreakdown", categoryCounts);
        stats.put("languages", List.of("uz-UZ", "oz-UZ", "ru-RU", "en-US"));

        return stats;
    }

    // =====================================================
    // Export (Manual Button)
    // =====================================================

    /**
     * Export translations to properties format
     * User clicks "Export" button in admin panel
     */
    @Transactional(readOnly = true)
    public Map<String, String> exportToProperties(String language) {
        log.info("Exporting translations to properties format: language={}", language);

        List<SystemMessage> messages = systemMessageRepository.findByIsActiveTrue();
        Map<String, String> properties = new LinkedHashMap<>();

        for (SystemMessage message : messages) {
            String key = message.getMessageKey();
            String value;

            if ("uz-UZ".equals(language)) {
                // Default language from main table
                value = message.getMessage();
            } else {
                // Get translation
                value = i18nService.getMessage(key, language);
            }

            properties.put(key, value);
        }

        log.info("✅ Exported {} properties for language: {}", properties.size(), language);
        return properties;
    }

    /**
     * Regenerate properties files on disk (fallback support)
     *
     * <p><strong>Purpose:</strong></p>
     * <ul>
     *   <li>Creates/updates static properties files in resources/i18n folder</li>
     *   <li>Used as fallback when database is unavailable</li>
     *   <li>Loaded at application startup</li>
     * </ul>
     *
     * <p><strong>Files Generated:</strong></p>
     * <ul>
     *   <li>menu_uz.properties (uz-UZ - Uzbek Latin)</li>
     *   <li>menu_oz.properties (oz-UZ - Uzbek Cyrillic)</li>
     *   <li>menu_ru.properties (ru-RU - Russian)</li>
     *   <li>menu_en.properties (en-US - English)</li>
     * </ul>
     *
     * @return Map with generation results (files created, translations count)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> regeneratePropertiesFiles() {
        log.info("🔄 Regenerating properties files from database...");

        Map<String, String> languageFiles = Map.of(
            "uz-UZ", "menu_uz.properties",
            "oz-UZ", "menu_oz.properties",
            "ru-RU", "menu_ru.properties",
            "en-US", "menu_en.properties"
        );

        Map<String, Object> results = new HashMap<>();
        List<String> generatedFiles = new ArrayList<>();
        int totalTranslations = 0;

        // Get resource path
        String resourcePath = "service/src/main/resources/i18n/";

        for (Map.Entry<String, String> entry : languageFiles.entrySet()) {
            String language = entry.getKey();
            String fileName = entry.getValue();

            try {
                Map<String, String> properties = exportToProperties(language);

                // Build file path
                Path filePath = Paths.get(resourcePath, fileName);

                // Create directory if not exists
                Files.createDirectories(filePath.getParent());

                // Write to file with header
                try (FileWriter writer = new FileWriter(filePath.toFile(), false)) {
                    // Write header
                    writer.write("# ================================================\n");
                    writer.write("# HEMIS Translation Fallback - " + getLanguageName(language) + "\n");
                    writer.write("# Auto-generated from database\n");
                    writer.write("# Generated: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\n");
                    writer.write("# Count: " + properties.size() + " translations\n");
                    writer.write("# ================================================\n\n");

                    // Write properties (sorted by key)
                    properties.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEach(prop -> {
                            try {
                                writer.write(prop.getKey() + "=" + escapePropertyValue(prop.getValue()) + "\n");
                            } catch (IOException e) {
                                log.error("Failed to write property: " + prop.getKey(), e);
                            }
                        });
                }

                generatedFiles.add(fileName);
                totalTranslations += properties.size();
                log.info("✅ Generated: {} ({} translations)", fileName, properties.size());

            } catch (Exception e) {
                log.error("❌ Failed to generate file: {}", fileName, e);
                List<String> errors = getOrCreateErrorList(results);
                errors.add("Failed to generate " + fileName + ": " + e.getMessage());
            }
        }

        results.put("success", generatedFiles.size() == languageFiles.size());
        results.put("generatedFiles", generatedFiles);
        results.put("totalFiles", generatedFiles.size());
        results.put("totalTranslations", totalTranslations);
        results.put("timestamp", Instant.now());

        log.info("🎉 Properties files regeneration completed: {} files, {} total translations",
            generatedFiles.size(), totalTranslations);

        return results;
    }

    /**
     * Get language display name
     */
    private String getLanguageName(String languageCode) {
        return switch (languageCode) {
            case "uz-UZ" -> "Uzbek Latin (uz-UZ)";
            case "oz-UZ" -> "Uzbek Cyrillic (oz-UZ)";
            case "ru-RU" -> "Russian (ru-RU)";
            case "en-US" -> "English (en-US)";
            default -> languageCode;
        };
    }

    /**
     * Escape property value for Java properties format
     */
    private String escapePropertyValue(String value) {
        if (value == null) {
            return "";
        }
        return value
            .replace("\\", "\\\\")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    @SuppressWarnings("unchecked")
    private List<String> getOrCreateErrorList(Map<String, Object> results) {
        return (List<String>) results.computeIfAbsent("errors", key -> new ArrayList<String>());
    }
}
