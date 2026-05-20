package uz.hemis.api.legacy.controller.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uz.hemis.service.shared.BimmService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BimmServiceController — raw proxy CUBA endpoints")
class BimmServiceControllerTest {

    @Mock private BimmService bimmService;

    @InjectMocks
    private BimmServiceController controller;

    private static final String PINFL = "12345678901234";

    @Test
    @DisplayName("GET /disabilityCheck — service raw response qaytarib beradi")
    void disabilityCheck() {
        Map<String, Object> proxied = Map.of("has_disability", true);
        when(bimmService.disabilityCheck(PINFL, "AA1234567")).thenReturn(proxied);

        ResponseEntity<?> response = controller.disabilityCheck(PINFL, "AA1234567");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(proxied);
    }

    @Test
    @DisplayName("GET /provertyRegister — service chaqirildi")
    void provertyRegister() {
        when(bimmService.provertyRegister(PINFL)).thenReturn(Map.of("in_register", false));

        ResponseEntity<?> response = controller.provertyRegister(PINFL);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(bimmService).provertyRegister(PINFL);
    }

    @Test
    @DisplayName("GET /certificate — proxy passed through")
    void certificate() {
        when(bimmService.certificate(PINFL))
                .thenReturn(java.util.List.of(Map.of("certificate", "DTM-2024")));

        ResponseEntity<?> response = controller.certificate(PINFL);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(java.util.List.class);
    }

    @Test
    @DisplayName("GET /academicDegree — service chaqirildi")
    void academicDegree() {
        when(bimmService.academicDegree(PINFL)).thenReturn(Map.of("degree", "DSc"));

        ResponseEntity<?> response = controller.academicDegree(PINFL);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(bimmService).academicDegree(PINFL);
    }

    @Test
    @DisplayName("GET /teacherTraining — service chaqirildi")
    void teacherTraining() {
        when(bimmService.teacherTraining(PINFL)).thenReturn(Map.of("trainings", java.util.List.of()));

        ResponseEntity<?> response = controller.teacherTraining(PINFL);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(bimmService).teacherTraining(PINFL);
    }

    @Test
    @DisplayName("disabilityCheck — null document parametr (optional)")
    void disabilityCheck_nullDocument() {
        when(bimmService.disabilityCheck(PINFL, null)).thenReturn(Map.of());

        ResponseEntity<?> response = controller.disabilityCheck(PINFL, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(bimmService).disabilityCheck(PINFL, null);
    }

    @Test
    @DisplayName("BimmService raw response (List) — to'g'ridan-to'g'ri qaytadi")
    void disabilityCheck_listResponseProxied() {
        java.util.List<Object> proxied = java.util.List.of(Map.of("k", "v"));
        when(bimmService.disabilityCheck(PINFL, "doc")).thenReturn(proxied);

        ResponseEntity<?> response = controller.disabilityCheck(PINFL, "doc");

        assertThat(response.getBody()).isEqualTo(proxied);
    }
}
