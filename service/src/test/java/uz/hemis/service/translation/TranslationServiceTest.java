package uz.hemis.service.translation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.hemis.domain.entity.system.Translation;
import uz.hemis.domain.repository.TranslationRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * {@link TranslationService} unit testlar.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TranslationService")
class TranslationServiceTest {

    @Mock private TranslationRepository translationRepository;

    @InjectMocks
    private TranslationService service;

    @Nested
    @DisplayName("translate()")
    class Translate {

        @Test
        @DisplayName("found — qiymat qaytariladi")
        void found_returnsValue() {
            Translation t = new Translation();
            t.setKey("welcome");
            t.setValue("Xush kelibsiz");
            when(translationRepository.findByKeyAndLocale("welcome", "uz-UZ"))
                    .thenReturn(Optional.of(t));

            assertThat(service.translate("welcome", "uz-UZ")).isEqualTo("Xush kelibsiz");
        }

        @Test
        @DisplayName("not found — key fallback")
        void notFound_returnsKey() {
            when(translationRepository.findByKeyAndLocale("missing.key", "ru-RU"))
                    .thenReturn(Optional.empty());

            assertThat(service.translate("missing.key", "ru-RU")).isEqualTo("missing.key");
        }

        @Test
        @DisplayName("null key — null qaytariladi")
        void nullKey_returnsNull() {
            assertThat(service.translate(null, "uz-UZ")).isNull();
        }

        @Test
        @DisplayName("empty key — empty qaytariladi")
        void emptyKey_returnsEmpty() {
            assertThat(service.translate("", "uz-UZ")).isEmpty();
            assertThat(service.translate("   ", "uz-UZ")).isEqualTo("   ");
        }
    }

    @Nested
    @DisplayName("translateBatch()")
    class TranslateBatch {

        @Test
        @DisplayName("topilganlar mapga to'planadi")
        void multipleKeys_mapResult() {
            Translation t1 = makeT("welcome", "Salom");
            Translation t2 = makeT("bye", "Xayr");
            when(translationRepository.findByKeysAndLocale(List.of("welcome", "bye", "missing"), "uz-UZ"))
                    .thenReturn(List.of(t1, t2));

            Map<String, String> result = service.translateBatch(List.of("welcome", "bye", "missing"), "uz-UZ");

            assertThat(result).hasSize(2);
            assertThat(result).containsEntry("welcome", "Salom");
            assertThat(result).containsEntry("bye", "Xayr");
            assertThat(result).doesNotContainKey("missing");
        }

        @Test
        @DisplayName("duplicate key — birinchi qiymat saqlanadi")
        void duplicateKey_keepsFirst() {
            Translation t1 = makeT("key", "first");
            Translation t2 = makeT("key", "second");
            when(translationRepository.findByKeysAndLocale(List.of("key"), "uz-UZ"))
                    .thenReturn(List.of(t1, t2));

            Map<String, String> result = service.translateBatch(List.of("key"), "uz-UZ");

            assertThat(result).containsEntry("key", "first");
        }
    }

    @Nested
    @DisplayName("getAllTranslations() / getTranslationsByCategory()")
    class GetMaps {

        @Test
        @DisplayName("null Map — empty qaytariladi")
        void nullMap_returnsEmpty() {
            when(translationRepository.getTranslationsMap("uz-UZ")).thenReturn(null);

            Map<String, String> result = service.getAllTranslations("uz-UZ");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("immutable copy qaytariladi (cache invariant)")
        void returnsImmutableCopy() {
            Map<String, String> raw = new java.util.HashMap<>();
            raw.put("a", "A");
            when(translationRepository.getTranslationsMap("uz-UZ")).thenReturn(raw);

            Map<String, String> result = service.getAllTranslations("uz-UZ");

            assertThat(result).containsEntry("a", "A");
            // Map.copyOf — immutable
            assertThatThrownBy(() -> result.put("x", "X"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("category map — null xavfsiz")
        void categoryNullSafe() {
            when(translationRepository.getTranslationsMapByCategory("uz-UZ", "menu")).thenReturn(null);

            assertThat(service.getTranslationsByCategory("uz-UZ", "menu")).isEmpty();
        }
    }

    @Nested
    @DisplayName("normalizeLocale()")
    class NormalizeLocale {

        @Test
        @DisplayName("null / blank → uz-UZ default")
        void blank_returnsDefault() {
            assertThat(service.normalizeLocale(null)).isEqualTo("uz-UZ");
            assertThat(service.normalizeLocale("")).isEqualTo("uz-UZ");
            assertThat(service.normalizeLocale("   ")).isEqualTo("uz-UZ");
        }

        @Test
        @DisplayName("short forms — full BCP-47 ga aylantiriladi")
        void shortForm_expanded() {
            assertThat(service.normalizeLocale("uz")).isEqualTo("uz-UZ");
            assertThat(service.normalizeLocale("UZ")).isEqualTo("uz-UZ");
            assertThat(service.normalizeLocale("uzb")).isEqualTo("uz-UZ");
            assertThat(service.normalizeLocale("ru")).isEqualTo("ru-RU");
            assertThat(service.normalizeLocale("rus")).isEqualTo("ru-RU");
            assertThat(service.normalizeLocale("en")).isEqualTo("en-US");
            assertThat(service.normalizeLocale("eng")).isEqualTo("en-US");
        }

        @Test
        @DisplayName("BCP-47 format — o'zgarmaydi")
        void fullForm_unchanged() {
            assertThat(service.normalizeLocale("uz-UZ")).isEqualTo("uz-UZ");
            assertThat(service.normalizeLocale("ru-RU")).isEqualTo("ru-RU");
            // unknown locale — raw qaytariladi
            assertThat(service.normalizeLocale("fr-FR")).isEqualTo("fr-FR");
        }

        @Test
        @DisplayName("trim — bo'sh joy olib tashlanadi")
        void trims_whitespace() {
            assertThat(service.normalizeLocale("  uz  ")).isEqualTo("uz-UZ");
        }
    }

    // =====================================================
    // helpers
    // =====================================================

    private static Translation makeT(String key, String value) {
        Translation t = new Translation();
        t.setKey(key);
        t.setValue(value);
        return t;
    }
}
