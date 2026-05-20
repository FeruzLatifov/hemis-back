package uz.hemis.api.legacy.controller.system;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uz.hemis.common.dto.system.TranslationFilterRequest;
import uz.hemis.service.legacy.TranslationLegacyService;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TranslationServiceController — i18n CUBA get + filtered")
class TranslationServiceControllerTest {

    @Mock private TranslationLegacyService translationService;

    @InjectMocks
    private TranslationServiceController controller;

    @Test
    @DisplayName("GET /translate/get — barcha tarjimalar (null filter)")
    void getAll_returnsAllTranslations() {
        Map<String, Object> t1 = Map.of("_entityName", "hemishe_ETranslation", "message", "Save");
        when(translationService.loadTranslations(null)).thenReturn(List.of(t1));

        ResponseEntity<Map<String, Object>> response = controller.getAllTranslations();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = response.getBody();
        assertThat(body).containsEntry("success", true);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> translations = (List<Map<String, Object>>) body.get("translations");
        assertThat(translations).hasSize(1);
        assertThat(body).isInstanceOf(LinkedHashMap.class);
    }

    @Test
    @DisplayName("POST /translate/get — messages list bo'lsa autoCreate")
    void postFiltered_withMessages_callsAutoCreate() {
        when(translationService.loadTranslationsWithAutoCreate(eq("button"), any()))
                .thenReturn(List.of(Map.of("message", "Save")));

        Map<String, Object> req = new HashMap<>();
        req.put("category", "button");
        req.put("messages", List.of("Save", "Cancel"));

        ResponseEntity<Map<String, Object>> response = controller.getTranslationsFiltered(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("success", true);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> messagesCaptor = ArgumentCaptor.forClass(List.class);
        verify(translationService).loadTranslationsWithAutoCreate(eq("button"), messagesCaptor.capture());
        assertThat(messagesCaptor.getValue()).containsExactly("Save", "Cancel");
    }

    @Test
    @DisplayName("POST /translate/get — messages yo'q → loadTranslations(filter)")
    void postFiltered_withoutMessages_callsLoadTranslations() {
        when(translationService.loadTranslations(any(TranslationFilterRequest.class)))
                .thenReturn(List.of());

        Map<String, Object> req = new HashMap<>();
        req.put("category", "menu");

        ResponseEntity<Map<String, Object>> response = controller.getTranslationsFiltered(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ArgumentCaptor<TranslationFilterRequest> captor = ArgumentCaptor.forClass(TranslationFilterRequest.class);
        verify(translationService).loadTranslations(captor.capture());
        assertThat(captor.getValue().getCategory()).isEqualTo("menu");
    }

    @Test
    @DisplayName("POST /translate/get — null body → loadTranslations(filter) with empty")
    void postFiltered_nullBody() {
        when(translationService.loadTranslations(any(TranslationFilterRequest.class)))
                .thenReturn(List.of());

        ResponseEntity<Map<String, Object>> response = controller.getTranslationsFiltered(null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(translationService).loadTranslations(any(TranslationFilterRequest.class));
    }
}
