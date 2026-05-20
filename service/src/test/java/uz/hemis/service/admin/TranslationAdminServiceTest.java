package uz.hemis.service.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.hemis.common.dto.system.TranslationDto;
import uz.hemis.domain.entity.system.SystemMessage;
import uz.hemis.domain.entity.system.SystemMessageTranslation;
import uz.hemis.domain.entity.system.SystemMessageTranslationId;
import uz.hemis.domain.repository.SystemMessageRepository;
import uz.hemis.domain.repository.SystemMessageTranslationRepository;
import uz.hemis.service.config.LanguageProperties;
import uz.hemis.service.event.TranslationCacheEventPublisher;
import uz.hemis.service.shared.I18nService;
import uz.hemis.service.shared.mapper.SystemMessageMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TranslationAdminService — i18n CRUD (no create/delete)")
class TranslationAdminServiceTest {

    @Mock private SystemMessageRepository systemMessageRepository;
    @Mock private SystemMessageTranslationRepository translationRepository;
    @Mock private I18nService i18nService;
    @Mock private TranslationCacheEventPublisher eventPublisher;
    @Mock private SystemMessageMapper messageMapper;
    @Mock private LanguageProperties languageProperties;

    @InjectMocks
    private TranslationAdminService service;

    private SystemMessage message;
    private UUID id;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        message = new SystemMessage();
        message.setId(id);
        message.setMessageKey("button.save");
        message.setCategory("button");
        message.setMessage("Saqlash");
        message.setIsActive(true);
        message.setTranslations(new HashSet<>());
    }

    @Test
    @DisplayName("getTranslationById — topilgan")
    void getById_found() {
        TranslationDto dto = TranslationDto.builder().id(id).messageKey("button.save").build();
        when(systemMessageRepository.findByIdWithTranslations(id)).thenReturn(Optional.of(message));
        when(messageMapper.toDto(message)).thenReturn(dto);

        Optional<TranslationDto> result = service.getTranslationById(id);

        assertThat(result).contains(dto);
    }

    @Test
    @DisplayName("getTranslationById — topilmadi")
    void getById_notFound() {
        when(systemMessageRepository.findByIdWithTranslations(id)).thenReturn(Optional.empty());

        assertThat(service.getTranslationById(id)).isEmpty();
    }

    @Test
    @DisplayName("getTranslationByKey — translations force-fetch + map")
    void getByKey_forceFetch() {
        TranslationDto dto = TranslationDto.builder().messageKey("button.save").build();
        when(systemMessageRepository.findByMessageKey("button.save")).thenReturn(Optional.of(message));
        when(messageMapper.toDto(message)).thenReturn(dto);

        Optional<TranslationDto> result = service.getTranslationByKey("button.save");

        assertThat(result).contains(dto);
    }

    @Test
    @DisplayName("updateTranslation — topilmagan → IllegalArgumentException")
    void update_notFound() {
        when(systemMessageRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateTranslation(
                id, "button", "button.save", "Saqlash", null, null, null, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("updateTranslation — immutable category o'zgartirilsa exception")
    void update_immutableCategoryChange() {
        when(systemMessageRepository.findById(id)).thenReturn(Optional.of(message));

        assertThatThrownBy(() -> service.updateTranslation(
                id, "DIFFERENT_CATEGORY", "button.save", "Saqlash", null, null, null, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("immutable");

        verify(systemMessageRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateTranslation — immutable messageKey o'zgartirilsa exception")
    void update_immutableKeyChange() {
        when(systemMessageRepository.findById(id)).thenReturn(Optional.of(message));

        assertThatThrownBy(() -> service.updateTranslation(
                id, "button", "NEW_KEY", "Saqlash", null, null, null, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("immutable");
    }

    @Test
    @DisplayName("updateTranslation — happy path: clearCache + publishUpdated")
    void update_happyPath() {
        when(systemMessageRepository.findById(id)).thenReturn(Optional.of(message));
        when(systemMessageRepository.save(any(SystemMessage.class))).thenReturn(message);
        when(translationRepository.findById(any(SystemMessageTranslationId.class)))
                .thenReturn(Optional.empty());

        service.updateTranslation(id, "button", "button.save", "Saqlash (yangi)",
                "Сакъланг", "Сохранить", "Save", true);

        assertThat(message.getMessage()).isEqualTo("Saqlash (yangi)");
        verify(i18nService).clearCache();
        verify(eventPublisher).publishTranslationUpdated("button.save");
        // 3 new translations created (oz/ru/en)
        verify(translationRepository, org.mockito.Mockito.times(3))
                .save(any(SystemMessageTranslation.class));
    }

    @Test
    @DisplayName("toggleActive — boolean flip + clearCache + publishUpdated")
    void toggleActive_flips() {
        when(systemMessageRepository.findById(id)).thenReturn(Optional.of(message));
        when(systemMessageRepository.save(any(SystemMessage.class))).thenReturn(message);
        when(messageMapper.toDto(message)).thenReturn(TranslationDto.builder().id(id).build());

        service.toggleActive(id);

        assertThat(message.getIsActive()).isFalse();
        verify(i18nService).clearCache();
        verify(eventPublisher).publishTranslationUpdated("button.save");
    }

    @Test
    @DisplayName("toggleActive — not found → IllegalArgumentException")
    void toggleActive_notFound() {
        when(systemMessageRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.toggleActive(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("getStatistics — message count + breakdown")
    void getStatistics_aggregation() {
        when(systemMessageRepository.count()).thenReturn(100L);
        when(systemMessageRepository.countByIsActive(true)).thenReturn(80L);
        when(translationRepository.count()).thenReturn(240L);
        when(systemMessageRepository.countByCategory()).thenReturn(
                List.of(new Object[]{"button", 30L}, new Object[]{"menu", 20L}));
        when(languageProperties.getSupported()).thenReturn(List.of("uz-UZ", "ru-RU"));

        var stats = service.getStatistics();

        assertThat(stats).containsEntry("totalMessages", 100L);
        assertThat(stats).containsEntry("activeMessages", 80L);
        assertThat(stats).containsEntry("inactiveMessages", 20L);
        assertThat(stats).containsEntry("totalTranslations", 240L);
        @SuppressWarnings("unchecked")
        var breakdown = (java.util.Map<String, Long>) stats.get("categoryBreakdown");
        assertThat(breakdown).containsEntry("button", 30L).containsEntry("menu", 20L);
    }

    @Test
    @DisplayName("exportToProperties — uz-UZ default message bilan")
    void exportToProperties_uz_usesDefaultMessage() {
        SystemMessage m1 = new SystemMessage();
        m1.setMessageKey("button.save");
        m1.setMessage("Saqlash");
        SystemMessage m2 = new SystemMessage();
        m2.setMessageKey("button.cancel");
        m2.setMessage("Bekor qilish");

        when(systemMessageRepository.findByIsActiveTrue()).thenReturn(List.of(m1, m2));

        var props = service.exportToProperties("uz-UZ");

        assertThat(props).containsEntry("button.save", "Saqlash");
        assertThat(props).containsEntry("button.cancel", "Bekor qilish");
    }

    @Test
    @DisplayName("exportToProperties — boshqa til I18nService.getMessage chaqiradi")
    void exportToProperties_otherLanguage_callsI18nService() {
        SystemMessage m1 = new SystemMessage();
        m1.setMessageKey("button.save");
        m1.setMessage("Saqlash");

        when(systemMessageRepository.findByIsActiveTrue()).thenReturn(List.of(m1));
        when(i18nService.getMessage("button.save", "ru-RU")).thenReturn("Сохранить");

        var props = service.exportToProperties("ru-RU");

        assertThat(props).containsEntry("button.save", "Сохранить");
        verify(i18nService).getMessage("button.save", "ru-RU");
    }
}
