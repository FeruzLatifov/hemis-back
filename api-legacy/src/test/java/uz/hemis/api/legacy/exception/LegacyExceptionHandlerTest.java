package uz.hemis.api.legacy.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import uz.hemis.common.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LegacyExceptionHandlerTest {

    private LegacyExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new LegacyExceptionHandler();
        request = mock(HttpServletRequest.class);
    }

    // ---- handleBadJson ----

    @Test
    void handleBadJson_returnsArrayWithMessage() {
        HttpMessageNotReadableException ex =
                new HttpMessageNotReadableException("bad json", (org.springframework.http.HttpInputMessage) null);

        ResponseEntity<List<Map<String, String>>> response = handler.handleBadJson(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull().hasSize(1);
        Map<String, String> error = response.getBody().get(0);
        assertThat(error).containsEntry("id", "request");
        assertThat(error).containsEntry("message", "Malformed JSON request");
    }

    // ---- handleNotFound ----

    @Test
    void handleNotFound_returns404WithDetails() {
        UUID id = UUID.randomUUID();
        ResourceNotFoundException ex = new ResourceNotFoundException("Student", "id", id);

        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry("error", "Not found");
        assertThat(response.getBody().get("details").toString())
                .contains("Student not found with id: '" + id + "'");
    }

    // ---- handleDataIntegrity ----

    @Test
    void handleDataIntegrity_duplicateKey_returnsProperMessage() {
        DataIntegrityViolationException ex =
                new DataIntegrityViolationException("msg", new RuntimeException("duplicate key violation"));

        ResponseEntity<List<Map<String, String>>> response = handler.handleDataIntegrity(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull().hasSize(1);
        Map<String, String> error = response.getBody().get(0);
        assertThat(error).containsEntry("id", "database");
        assertThat(error).containsEntry("message", "Duplicate entry: record already exists");
    }

    @Test
    void handleDataIntegrity_notNull_returnsProperMessage() {
        DataIntegrityViolationException ex =
                new DataIntegrityViolationException("msg", new RuntimeException("not-null constraint violated"));

        ResponseEntity<List<Map<String, String>>> response = handler.handleDataIntegrity(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull().hasSize(1);
        Map<String, String> error = response.getBody().get(0);
        assertThat(error).containsEntry("id", "database");
        assertThat(error).containsEntry("message", "Required field is missing");
    }

    @Test
    void handleDataIntegrity_foreignKey_returnsGenericMessage() {
        DataIntegrityViolationException ex =
                new DataIntegrityViolationException("msg", new RuntimeException("foreign key constraint violation"));

        ResponseEntity<List<Map<String, String>>> response = handler.handleDataIntegrity(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull().hasSize(1);
        Map<String, String> error = response.getBody().get(0);
        assertThat(error).containsEntry("id", "database");
        assertThat(error).containsEntry("message", "Referenced record not found");
    }

    @Test
    void handleDataIntegrity_other_returnsGenericMessage() {
        DataIntegrityViolationException ex =
                new DataIntegrityViolationException("msg", new RuntimeException("some other db error"));

        ResponseEntity<List<Map<String, String>>> response = handler.handleDataIntegrity(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull().hasSize(1);
        Map<String, String> error = response.getBody().get(0);
        assertThat(error).containsEntry("id", "database");
        assertThat(error).containsEntry("message", "Data integrity violation");
    }

    // ---- handleNoResourceFound ----

    @Test
    void handleNoResourceFound_servicesPath_returnsServiceFormat() throws NoResourceFoundException {
        when(request.getRequestURI()).thenReturn("/app/rest/v2/services/MyService/myMethod");
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "resource", null);

        ResponseEntity<Map<String, Object>> response = handler.handleNoResourceFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry("error", "Service method not found");
        assertThat(response.getBody()).containsEntry("details", "MyService.myMethod()");
    }

    @Test
    void handleNoResourceFound_entitiesPath_returnsEntityFormat() throws NoResourceFoundException {
        when(request.getRequestURI()).thenReturn("/app/rest/v2/entities/hemishe_MyEntity/123");
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "resource", null);

        ResponseEntity<Map<String, Object>> response = handler.handleNoResourceFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry("error", "MetaClass not found");
        assertThat(response.getBody()).containsEntry("details", "MetaClass hemishe_MyEntity not found");
    }

    @Test
    void handleNoResourceFound_otherPath_returnsGenericNotFound() throws NoResourceFoundException {
        when(request.getRequestURI()).thenReturn("/app/rest/v2/other");
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "resource", null);

        ResponseEntity<Map<String, Object>> response = handler.handleNoResourceFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry("error", "Not found");
        assertThat(response.getBody()).containsEntry("details", "/app/rest/v2/other");
    }

    // ---- handleRuntimeException ----

    @Test
    void handleRuntime_postMethod_returns400() {
        when(request.getMethod()).thenReturn("POST");
        RuntimeException ex = new RuntimeException("something went wrong");

        ResponseEntity<?> response = handler.handleRuntimeException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        @SuppressWarnings("unchecked")
        List<Map<String, String>> body = (List<Map<String, String>>) response.getBody();
        assertThat(body).hasSize(1);
        assertThat(body.get(0)).containsEntry("id", "server");
        assertThat(body.get(0)).containsEntry("message", "something went wrong");
    }

    @Test
    void handleRuntime_getMethod_returns500() {
        when(request.getMethod()).thenReturn("GET");
        RuntimeException ex = new RuntimeException("unexpected error");

        ResponseEntity<?> response = handler.handleRuntimeException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("error", "Server error");
        assertThat(body).containsEntry("details", "unexpected error");
    }
}
