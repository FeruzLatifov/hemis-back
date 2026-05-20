package uz.hemis.service.legacy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StipendClassifierService — OLD-HEMIS 13 classifier aggregator")
class StipendClassifierServiceTest {

    @Mock private HokimiyatClassifierService hokimiyat;

    @InjectMocks
    private StipendClassifierService service;

    @Test
    @DisplayName("getStipendClassifiers — 13 ta classifier (1 university + 12 regular)")
    void allClassifiers_aggregated() {
        Map<String, Object> uniData = Map.of("apiKey", "h_university", "items", List.of());
        when(hokimiyat.getUniversityClassifierForHokimiyat()).thenReturn(uniData);

        // Boshqa 12 ta classifier — har biri uchun mock
        Map<String, Object> regular = Map.of("apiKey", "regular", "items", List.of());
        lenient().when(hokimiyat.getClassifierWithItemsForHokimiyat(anyString(), anyString()))
                .thenReturn(regular);

        Map<String, Object> result = service.getStipendClassifiers();

        assertThat(result).containsEntry("success", true);
        List<?> classifiers = (List<?>) result.get("classifiers");
        // 13 ta classifier: 1 ta university + 12 ta regular
        assertThat(classifiers).hasSize(13);

        // University maxsus chaqiriq
        verify(hokimiyat).getUniversityClassifierForHokimiyat();
        // Boshqa 12 ta classifier umumiy method bilan
        verify(hokimiyat).getClassifierWithItemsForHokimiyat(eq("h_soato"), eq("hemishe_h_soato"));
        verify(hokimiyat).getClassifierWithItemsForHokimiyat(eq("h_course"), eq("hemishe_h_course"));
    }

    @Test
    @DisplayName("getStipendClassifiers — bir classifier xato bo'lsa, qolganlari davom etadi")
    void oneClassifierFails_othersContinue() {
        when(hokimiyat.getUniversityClassifierForHokimiyat()).thenReturn(null);
        // 12 ta regular call'dan bittasi exception
        lenient().when(hokimiyat.getClassifierWithItemsForHokimiyat(anyString(), anyString()))
                .thenReturn(Map.of("apiKey", "k", "items", List.of()));
        lenient().when(hokimiyat.getClassifierWithItemsForHokimiyat(eq("h_nationality"), anyString()))
                .thenThrow(new RuntimeException("DB error"));

        Map<String, Object> result = service.getStipendClassifiers();

        assertThat(result).containsEntry("success", true);
        List<?> classifiers = (List<?>) result.get("classifiers");
        // university null + h_nationality exception = 11 ta qolgan
        assertThat(classifiers).hasSize(11);
    }

    @Test
    @DisplayName("getStipendClassifiersInfo — meta-only (no items)")
    void info_metaOnly() {
        Map<String, Object> uniInfo = Map.of("apiKey", "h_university");
        Map<String, Object> info = Map.of("apiKey", "k");
        when(hokimiyat.getUniversityClassifierInfoCompat()).thenReturn(uniInfo);
        lenient().when(hokimiyat.getClassifierInfoForGroup(anyString(), anyString())).thenReturn(info);

        Map<String, Object> result = service.getStipendClassifiersInfo();

        assertThat(result).containsEntry("success", true);
        List<?> classifiers = (List<?>) result.get("classifiers");
        assertThat(classifiers).hasSize(13);
    }

    @Test
    @DisplayName("getStipendClassifiersInfo — regular classifier null'lar skipped, university null qo'shiladi")
    void info_regularNullsSkipped_universityNotChecked() {
        // Service code: university null check qilmaydi (add); regular methods null check qiladi (skip).
        when(hokimiyat.getUniversityClassifierInfoCompat()).thenReturn(null);
        lenient().when(hokimiyat.getClassifierInfoForGroup(anyString(), anyString())).thenReturn(null);

        Map<String, Object> result = service.getStipendClassifiersInfo();

        assertThat(result).containsEntry("success", true);
        List<?> classifiers = (List<?>) result.get("classifiers");
        // university null qo'shiladi (1 ta), regular 12 ta null skipped
        assertThat(classifiers).hasSize(1);
        assertThat(classifiers.get(0)).isNull();
    }
}
