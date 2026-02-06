package uz.hemis.api.legacy.controller.classifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uz.hemis.service.legacy.ClassifierLegacyService;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClassifierServicesController Tests")
class ClassifierServicesControllerTest {

    @Mock
    private ClassifierLegacyService classifierService;

    @InjectMocks
    private ClassifierServicesController controller;

    @Test
    @DisplayName("allItems - barcha klassifikatorlar items bilan qaytariladi")
    void allItemsReturnsAllClassifiers() {
        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("success", true);
        expected.put("classifiers", List.of(
                Map.of("h_gender", Map.of("title", "Jinslar", "version", 4, "count", 4, "items", List.of()))
        ));

        when(classifierService.getAllClassifiersWithItems()).thenReturn(expected);

        ResponseEntity<Map<String, Object>> response = controller.allItems();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("success", true);
        assertThat(response.getBody()).containsKey("classifiers");
        verify(classifierService).getAllClassifiersWithItems();
    }

    @Test
    @DisplayName("info - klassifikator metadatasi qaytariladi")
    void infoReturnsMetadata() {
        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("success", true);
        expected.put("classifiers", List.of(
                Map.of("h_gender", Map.of("title", "Jinslar", "version", 4, "count", 4))
        ));

        when(classifierService.getAllClassifiersInfo()).thenReturn(expected);

        ResponseEntity<Map<String, Object>> response = controller.info();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("success", true);
        verify(classifierService).getAllClassifiersInfo();
    }

    @Test
    @DisplayName("single - bitta klassifikator qaytariladi")
    void singleReturnsClassifier() {
        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("success", true);
        expected.put("classifier", Map.of("h_university", Map.of("title", "OTMlar", "count", 238)));

        when(classifierService.getSingleClassifier("h_university")).thenReturn(expected);

        ResponseEntity<Map<String, Object>> response = controller.single("h_university");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("success", true);
        verify(classifierService).getSingleClassifier("h_university");
    }

    @Test
    @DisplayName("single - topilmagan klassifikator uchun 400 qaytaradi")
    void singleReturns400ForUnknown() {
        when(classifierService.getSingleClassifier("h_nonexistent")).thenReturn(null);

        ResponseEntity<Map<String, Object>> response = controller.single("h_nonexistent");

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).containsEntry("success", false);
        assertThat(response.getBody()).containsKey("error");
    }

    @Test
    @DisplayName("hokimiyat - hokimiyat klassifikatorlari qaytariladi")
    void hokimiyatReturnsClassifiers() {
        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("success", true);
        expected.put("classifiers", List.of());

        when(classifierService.getHokimiyatClassifiers()).thenReturn(expected);

        ResponseEntity<Map<String, Object>> response = controller.hokimiyat();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("success", true);
        verify(classifierService).getHokimiyatClassifiers();
    }
}
