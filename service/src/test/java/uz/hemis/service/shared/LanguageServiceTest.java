package uz.hemis.service.shared;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.hemis.common.dto.system.LanguageDto;
import uz.hemis.domain.entity.Language;
import uz.hemis.domain.repository.LanguageRepository;
import uz.hemis.service.shared.mapper.LanguageMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link LanguageService}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>getAllLanguages - returns all languages ordered by position</li>
 *   <li>getActiveLanguages - returns only active languages ordered by position</li>
 *   <li>getLanguageByCode - found and not-found scenarios</li>
 *   <li>toggleLanguage - enable/disable with system default protection</li>
 * </ul>
 *
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LanguageService Unit Tests")
class LanguageServiceTest {

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private LanguageMapper languageMapper;

    @InjectMocks
    private LanguageService languageService;

    private Language uzbekLatin;
    private Language russian;
    private Language english;
    private LanguageDto uzbekLatinDto;
    private LanguageDto russianDto;
    private LanguageDto englishDto;

    @BeforeEach
    void setUp() {
        // Uzbek Latin - system default
        uzbekLatin = Language.builder()
                .code("uz-UZ")
                .name("Uzbek (Latin)")
                .nativeName("O'zbekcha")
                .isoCode("uz")
                .position(1)
                .isActive(true)
                .isDefault(true)
                .isRtl(false)
                .build();
        uzbekLatin.setId(UUID.randomUUID());

        // Russian - system default
        russian = Language.builder()
                .code("ru-RU")
                .name("Russian")
                .nativeName("Русский")
                .isoCode("ru")
                .position(3)
                .isActive(true)
                .isDefault(true)
                .isRtl(false)
                .build();
        russian.setId(UUID.randomUUID());

        // English - NOT system default
        english = Language.builder()
                .code("en-US")
                .name("English")
                .nativeName("English")
                .isoCode("en")
                .position(4)
                .isActive(true)
                .isDefault(false)
                .isRtl(false)
                .build();
        english.setId(UUID.randomUUID());

        uzbekLatinDto = LanguageDto.builder()
                .code("uz-UZ")
                .name("Uzbek (Latin)")
                .nativeName("O'zbekcha")
                .isoCode("uz")
                .position(1)
                .isActive(true)
                .isDefault(true)
                .isRtl(false)
                .canDisable(false)
                .build();

        russianDto = LanguageDto.builder()
                .code("ru-RU")
                .name("Russian")
                .nativeName("Русский")
                .isoCode("ru")
                .position(3)
                .isActive(true)
                .isDefault(true)
                .isRtl(false)
                .canDisable(false)
                .build();

        englishDto = LanguageDto.builder()
                .code("en-US")
                .name("English")
                .nativeName("English")
                .isoCode("en")
                .position(4)
                .isActive(true)
                .isDefault(false)
                .isRtl(false)
                .canDisable(true)
                .build();
    }

    // =====================================================
    // getAllLanguages tests
    // =====================================================

    @Nested
    @DisplayName("getAllLanguages")
    class GetAllLanguages {

        @Test
        @DisplayName("returns all languages ordered by position")
        void returnsAllLanguagesOrderedByPosition() {
            // Given
            List<Language> entities = List.of(uzbekLatin, russian, english);
            List<LanguageDto> dtos = List.of(uzbekLatinDto, russianDto, englishDto);

            when(languageRepository.findAllOrderedByPosition()).thenReturn(entities);
            when(languageMapper.toDtoList(entities)).thenReturn(dtos);

            // When
            List<LanguageDto> result = languageService.getAllLanguages();

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getCode()).isEqualTo("uz-UZ");
            assertThat(result.get(1).getCode()).isEqualTo("ru-RU");
            assertThat(result.get(2).getCode()).isEqualTo("en-US");

            verify(languageRepository).findAllOrderedByPosition();
            verify(languageMapper).toDtoList(entities);
        }

        @Test
        @DisplayName("returns empty list when no languages exist")
        void returnsEmptyListWhenNoLanguages() {
            // Given
            when(languageRepository.findAllOrderedByPosition()).thenReturn(Collections.emptyList());
            when(languageMapper.toDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

            // When
            List<LanguageDto> result = languageService.getAllLanguages();

            // Then
            assertThat(result).isEmpty();

            verify(languageRepository).findAllOrderedByPosition();
            verify(languageMapper).toDtoList(Collections.emptyList());
        }
    }

    // =====================================================
    // getActiveLanguages tests
    // =====================================================

    @Nested
    @DisplayName("getActiveLanguages")
    class GetActiveLanguages {

        @Test
        @DisplayName("returns only active languages ordered by position")
        void returnsOnlyActiveLanguages() {
            // Given
            List<Language> activeEntities = List.of(uzbekLatin, russian, english);
            List<LanguageDto> activeDtos = List.of(uzbekLatinDto, russianDto, englishDto);

            when(languageRepository.findAllActiveOrderedByPosition()).thenReturn(activeEntities);
            when(languageMapper.toDtoList(activeEntities)).thenReturn(activeDtos);

            // When
            List<LanguageDto> result = languageService.getActiveLanguages();

            // Then
            assertThat(result).hasSize(3);
            assertThat(result).allMatch(dto -> Boolean.TRUE.equals(dto.getIsActive()));

            verify(languageRepository).findAllActiveOrderedByPosition();
            verify(languageMapper).toDtoList(activeEntities);
        }

        @Test
        @DisplayName("returns empty list when no active languages")
        void returnsEmptyListWhenNoActiveLanguages() {
            // Given
            when(languageRepository.findAllActiveOrderedByPosition()).thenReturn(Collections.emptyList());
            when(languageMapper.toDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

            // When
            List<LanguageDto> result = languageService.getActiveLanguages();

            // Then
            assertThat(result).isEmpty();

            verify(languageRepository).findAllActiveOrderedByPosition();
        }
    }

    // =====================================================
    // getLanguageByCode tests
    // =====================================================

    @Nested
    @DisplayName("getLanguageByCode")
    class GetLanguageByCode {

        @Test
        @DisplayName("found - returns Optional with DTO")
        void found_returnsOptionalWithDto() {
            // Given
            when(languageRepository.findByCode("uz-UZ")).thenReturn(Optional.of(uzbekLatin));
            when(languageMapper.toDto(uzbekLatin)).thenReturn(uzbekLatinDto);

            // When
            Optional<LanguageDto> result = languageService.getLanguageByCode("uz-UZ");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getCode()).isEqualTo("uz-UZ");
            assertThat(result.get().getName()).isEqualTo("Uzbek (Latin)");
            assertThat(result.get().getNativeName()).isEqualTo("O'zbekcha");
            assertThat(result.get().getIsoCode()).isEqualTo("uz");
            assertThat(result.get().getCanDisable()).isFalse();

            verify(languageRepository).findByCode("uz-UZ");
            verify(languageMapper).toDto(uzbekLatin);
        }

        @Test
        @DisplayName("not found - returns empty Optional")
        void notFound_returnsEmptyOptional() {
            // Given
            when(languageRepository.findByCode("xx-XX")).thenReturn(Optional.empty());

            // When
            Optional<LanguageDto> result = languageService.getLanguageByCode("xx-XX");

            // Then
            assertThat(result).isEmpty();

            verify(languageRepository).findByCode("xx-XX");
            verify(languageMapper, never()).toDto(any());
        }

        @Test
        @DisplayName("English language - canDisable is true")
        void englishLanguage_canDisableIsTrue() {
            // Given
            when(languageRepository.findByCode("en-US")).thenReturn(Optional.of(english));
            when(languageMapper.toDto(english)).thenReturn(englishDto);

            // When
            Optional<LanguageDto> result = languageService.getLanguageByCode("en-US");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getCode()).isEqualTo("en-US");
            assertThat(result.get().getCanDisable()).isTrue();

            verify(languageRepository).findByCode("en-US");
            verify(languageMapper).toDto(english);
        }
    }

    // =====================================================
    // toggleLanguage tests
    // =====================================================

    @Nested
    @DisplayName("toggleLanguage")
    class ToggleLanguage {

        @Test
        @DisplayName("enable non-default language - returns true and saves")
        void enableNonDefaultLanguage_returnsTrueAndSaves() {
            // Given
            Language karakalpak = Language.builder()
                    .code("kk-UZ")
                    .name("Karakalpak")
                    .nativeName("Qaraqalpaqsha")
                    .isActive(false)
                    .isDefault(false)
                    .build();
            karakalpak.setId(UUID.randomUUID());

            when(languageRepository.findByCode("kk-UZ")).thenReturn(Optional.of(karakalpak));
            when(languageRepository.save(karakalpak)).thenReturn(karakalpak);

            // When
            boolean result = languageService.toggleLanguage("kk-UZ", true);

            // Then
            assertThat(result).isTrue();
            assertThat(karakalpak.getIsActive()).isTrue();

            verify(languageRepository).findByCode("kk-UZ");
            verify(languageRepository).save(karakalpak);
        }

        @Test
        @DisplayName("disable non-default language - returns true and saves")
        void disableNonDefaultLanguage_returnsTrueAndSaves() {
            // Given - english is NOT a system default (isSystemDefault checks code)
            when(languageRepository.findByCode("en-US")).thenReturn(Optional.of(english));
            when(languageRepository.save(english)).thenReturn(english);

            // When
            boolean result = languageService.toggleLanguage("en-US", false);

            // Then
            assertThat(result).isTrue();
            assertThat(english.getIsActive()).isFalse();

            verify(languageRepository).findByCode("en-US");
            verify(languageRepository).save(english);
        }

        @Test
        @DisplayName("disable system default language (uz-UZ) - returns false and does not save")
        void disableSystemDefaultLanguage_returnsFalseAndDoesNotSave() {
            // Given - uz-UZ is system default (isSystemDefault returns true)
            when(languageRepository.findByCode("uz-UZ")).thenReturn(Optional.of(uzbekLatin));

            // When
            boolean result = languageService.toggleLanguage("uz-UZ", false);

            // Then
            assertThat(result).isFalse();
            // isActive should NOT have changed
            assertThat(uzbekLatin.getIsActive()).isTrue();

            verify(languageRepository).findByCode("uz-UZ");
            verify(languageRepository, never()).save(any());
        }

        @Test
        @DisplayName("disable system default language (ru-RU) - returns false and does not save")
        void disableSystemDefaultRussian_returnsFalseAndDoesNotSave() {
            // Given - ru-RU is system default
            when(languageRepository.findByCode("ru-RU")).thenReturn(Optional.of(russian));

            // When
            boolean result = languageService.toggleLanguage("ru-RU", false);

            // Then
            assertThat(result).isFalse();
            assertThat(russian.getIsActive()).isTrue();

            verify(languageRepository).findByCode("ru-RU");
            verify(languageRepository, never()).save(any());
        }

        @Test
        @DisplayName("enable system default language - returns true (enabling is always allowed)")
        void enableSystemDefaultLanguage_returnsTrue() {
            // Given - even though it's system default, enabling is OK
            when(languageRepository.findByCode("uz-UZ")).thenReturn(Optional.of(uzbekLatin));
            when(languageRepository.save(uzbekLatin)).thenReturn(uzbekLatin);

            // When
            boolean result = languageService.toggleLanguage("uz-UZ", true);

            // Then
            assertThat(result).isTrue();

            verify(languageRepository).findByCode("uz-UZ");
            verify(languageRepository).save(uzbekLatin);
        }

        @Test
        @DisplayName("language not found - returns false")
        void languageNotFound_returnsFalse() {
            // Given
            when(languageRepository.findByCode("xx-XX")).thenReturn(Optional.empty());

            // When
            boolean result = languageService.toggleLanguage("xx-XX", true);

            // Then
            assertThat(result).isFalse();

            verify(languageRepository).findByCode("xx-XX");
            verify(languageRepository, never()).save(any());
        }
    }
}
