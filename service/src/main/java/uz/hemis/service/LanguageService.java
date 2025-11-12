package uz.hemis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.LanguageDto;
import uz.hemis.domain.entity.Language;
import uz.hemis.domain.repository.LanguageRepository;
import uz.hemis.service.mapper.LanguageMapper;

import java.util.List;
import java.util.Optional;

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

    private final LanguageRepository languageRepository;
    private final LanguageMapper languageMapper;

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
}
