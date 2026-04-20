package uz.hemis.api.university.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import uz.hemis.common.error.ApiProblem;
import uz.hemis.common.exception.BadRequestException;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("UniversityExceptionHandler — RFC 7807 Problem Details")
class UniversityExceptionHandlerTest {

    private final UniversityExceptionHandler handler = new UniversityExceptionHandler();
    private final HttpServletRequest req = mock(HttpServletRequest.class);

    @Test
    @DisplayName("ResourceNotFoundException returns 404 Problem+JSON")
    void handleNotFound() {
        when(req.getRequestURI()).thenReturn("/api/university/students/STU001");
        ResponseEntity<ApiProblem> resp = handler.handleNotFound(
                new ResourceNotFoundException("Student", "code", "STU001"), req);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getHeaders().getContentType()).isEqualTo(MediaType.valueOf("application/problem+json"));
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getStatus()).isEqualTo(404);
        assertThat(resp.getBody().getCode()).isEqualTo("NOT_FOUND");
        assertThat(resp.getBody().getTitle()).isEqualTo("Resource Not Found");
        assertThat(resp.getBody().getType()).contains("resource-not-found");
        assertThat(resp.getBody().getInstance()).isEqualTo("/api/university/students/STU001");
        assertThat(resp.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("BadRequestException returns 400 Problem+JSON")
    void handleBadRequest() {
        when(req.getRequestURI()).thenReturn("/api/university/test");
        ResponseEntity<ApiProblem> resp = handler.handleBadRequest(
                new BadRequestException("invalid parameter"), req);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getStatus()).isEqualTo(400);
        assertThat(resp.getBody().getCode()).isEqualTo("BAD_REQUEST");
    }

    @Test
    @DisplayName("ValidationException returns 422 Problem+JSON")
    void handleValidation() {
        when(req.getRequestURI()).thenReturn("/api/university/validate");
        ResponseEntity<ApiProblem> resp = handler.handleValidation(
                new ValidationException("Validation failed"), req);
        assertThat(resp.getStatusCode().value()).isEqualTo(422);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getStatus()).isEqualTo(422);
        assertThat(resp.getBody().getCode()).isEqualTo("VALIDATION_ERROR");
    }
}
