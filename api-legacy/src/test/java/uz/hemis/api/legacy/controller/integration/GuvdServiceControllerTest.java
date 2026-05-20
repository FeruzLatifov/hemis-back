package uz.hemis.api.legacy.controller.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uz.hemis.service.legacy.GuvdLegacyService;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GuvdServiceController — CUBA GUVD endpoints")
class GuvdServiceControllerTest {

    @Mock private GuvdLegacyService guvdLegacyService;

    @InjectMocks
    private GuvdServiceController controller;

    @Test
    @DisplayName("GET /classifiers — service delegate + 200")
    void classifiers_delegatesToService() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("classifiers", java.util.List.of(Map.of("name", "h_soato")));

        when(guvdLegacyService.getClassifiers()).thenReturn(result);

        ResponseEntity<Map<String, Object>> response = controller.classifiers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("success", true);
        verify(guvdLegacyService).getClassifiers();
    }

    @Test
    @DisplayName("GET /objects — service delegate + 200")
    void objects_delegatesToService() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("count", 224);
        result.put("data", java.util.List.of());

        when(guvdLegacyService.getObjects()).thenReturn(result);

        ResponseEntity<Map<String, Object>> response = controller.objects();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("count", 224);
    }

    @Test
    @DisplayName("Classifier error response — success=false ham 200 qaytaradi (old-hemis match)")
    void classifiers_errorResponse_is200() {
        Map<String, Object> err = new LinkedHashMap<>();
        err.put("success", false);
        err.put("message", "Server error");

        when(guvdLegacyService.getClassifiers()).thenReturn(err);

        ResponseEntity<Map<String, Object>> response = controller.classifiers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("success", false);
    }
}
