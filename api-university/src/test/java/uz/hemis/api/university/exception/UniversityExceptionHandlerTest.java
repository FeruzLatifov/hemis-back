package uz.hemis.api.university.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uz.hemis.common.dto.ErrorResponse;
import uz.hemis.common.exception.BadRequestException;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UniversityExceptionHandler")
class UniversityExceptionHandlerTest {

    private final UniversityExceptionHandler handler = new UniversityExceptionHandler();

    @Test
    @DisplayName("ResourceNotFoundException returns 404")
    void handleNotFound() {
        ResponseEntity<ErrorResponse> resp = handler.handleNotFound(
                new ResourceNotFoundException("Student", "code", "STU001"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("BadRequestException returns 400")
    void handleBadRequest() {
        ResponseEntity<ErrorResponse> resp = handler.handleBadRequest(
                new BadRequestException("invalid parameter"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("ValidationException returns 422")
    void handleValidation() {
        ResponseEntity<ErrorResponse> resp = handler.handleValidation(
                new ValidationException("Validation failed"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getStatus()).isEqualTo(422);
    }
}
