package uz.hemis.api.legacy.controller.system;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uz.hemis.service.legacy.TestLegacyService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TestServiceController — healthcheck + students (CUBA)")
class TestServiceControllerTest {

    @Mock private TestLegacyService testLegacyService;

    @InjectMocks
    private TestServiceController controller;

    @Test
    @DisplayName("GET /healthcheck — anonymous, status=ok")
    void healthcheck_returnsStatusOk() {
        ResponseEntity<?> response = controller.healthcheck();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("status", "ok");
    }

    @Test
    @DisplayName("GET /healthcheck — LinkedHashMap qaytaradi (CUBA order)")
    void healthcheck_usesLinkedHashMap() {
        ResponseEntity<?> response = controller.healthcheck();

        assertThat(response.getBody()).isInstanceOf(LinkedHashMap.class);
    }

    @Test
    @DisplayName("GET /students — service ro'yxat qaytaradi")
    void students_returnsList() {
        Map<String, Object> row = Map.of("id", "uuid-1", "pinfl", "12345678901234");
        when(testLegacyService.getStudentsUpdatedYesterday()).thenReturn(List.of(row));

        ResponseEntity<?> response = controller.students();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(List.class);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> body = (List<Map<String, Object>>) response.getBody();
        assertThat(body).hasSize(1);
        assertThat(body.get(0)).containsEntry("pinfl", "12345678901234");
    }

    @Test
    @DisplayName("GET /students — service exception → null body, 200")
    void students_exceptionSilencedToNull() {
        when(testLegacyService.getStudentsUpdatedYesterday())
                .thenThrow(new RuntimeException("DB unreachable"));

        ResponseEntity<?> response = controller.students();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
    }

    @Test
    @DisplayName("GET /students — null result old-hemis bilan mos")
    void students_nullResultPassedThrough() {
        when(testLegacyService.getStudentsUpdatedYesterday()).thenReturn(null);

        ResponseEntity<?> response = controller.students();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
    }
}
