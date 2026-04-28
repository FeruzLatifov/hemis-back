package uz.hemis.service.shared;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.audit.AuditAction;
import uz.hemis.common.audit.Audited;
import uz.hemis.common.dto.system.LanguageDto;
import uz.hemis.domain.entity.reference.Language;
import uz.hemis.domain.entity.system.SystemConfiguration;
import uz.hemis.domain.repository.LanguageRepository;
import uz.hemis.domain.repository.SystemConfigurationRepository;
import uz.hemis.service.shared.mapper.LanguageMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Language Service - System language management
 * 
 * <p>Provides language configuration for UI and translations</p>
 * 
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LanguageService {

    private static final String DEFAULT_LANGUAGE_KEY = "system.default_language";

    private final LanguageRepository languageRepository;
    private final LanguageMapper languageMapper;
    private final SystemConfigurationRepository systemConfigurationRepository;

    /**
     * Get all languages ordered by position
     */
    public List<LanguageDto> getAllLanguages() {
        log.debug("Getting all languages");
        List<Language> languages = languageRepository.findAllOrderedByPosition();
        return languageMapper.toDtoList(languages);
    }

    /**
     * Get only active languages
     */
    public List<LanguageDto> getActiveLanguages() {
        log.debug("Getting active languages");
        List<Language> languages = languageRepository.findAllActiveOrderedByPosition();
        return languageMapper.toDtoList(languages);
    }

    /**
     * Get language by code
     */
    public Optional<LanguageDto> getLanguageByCode(String code) {
        log.debug("Getting language by code: {}", code);
        return languageRepository.findByCode(code)
            .map(languageMapper::toDto);
    }

    /**
     * Update language active status
     * (Only non-system-default languages can be toggled)
     */
    @Transactional
    @Audited(action = AuditAction.UPDATE, entity = "Language", entityClass = Language.class, keyArg = "code")
    public boolean toggleLanguage(String code, boolean enabled) {
        log.info("Toggling language {} to {}", code, enabled);
        
        Optional<Language> languageOpt = languageRepository.findByCode(code);
        
        if (languageOpt.isEmpty()) {
            log.warn("Language not found: {}", code);
            return false;
        }
        
        Language language = languageOpt.get();
        
        // Don't allow disabling system default languages
        if (language.isSystemDefault() && !enabled) {
            log.warn("Cannot disable system default language: {}", code);
            return false;
        }
        
        language.setIsActive(enabled);
        languageRepository.save(language);

        log.info("Language {} set to {}", code, enabled);
        return true;
    }

    /**
     * Get all language-related system configurations as a map of path → boolean value.
     */
    public Map<String, Boolean> getLanguageConfigurationsMap() {
        return systemConfigurationRepository.findAllLanguageConfigurations().stream()
                .collect(Collectors.toMap(
                        SystemConfiguration::getPath,
                        SystemConfiguration::getBooleanValue
                ));
    }

    /**
     * Get the currently configured default language code, or empty if unset.
     */
    public Optional<String> getDefaultLanguageCode() {
        return systemConfigurationRepository.findByPath(DEFAULT_LANGUAGE_KEY)
                .map(SystemConfiguration::getValue);
    }

    /**
     * Update the default language code in system_configuration. No-op if the row does not exist.
     */
    @Transactional
    @Audited(action = AuditAction.UPDATE, entity = "SystemConfiguration", entityClass = SystemConfiguration.class)
    public void setDefaultLanguageCode(String languageCode) {
        systemConfigurationRepository.findByPath(DEFAULT_LANGUAGE_KEY).ifPresent(config -> {
            config.setValue(languageCode);
            systemConfigurationRepository.save(config);
            log.info("Default language set to {}", languageCode);
        });
    }
}
