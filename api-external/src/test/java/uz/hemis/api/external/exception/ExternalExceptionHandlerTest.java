package uz.hemis.api.external.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import uz.hemis.common.dto.ErrorResponse;
import uz.hemis.common.exception.BadRequestException;
import uz.hemis.common.exception.ResourceNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ExternalExceptionHandler")
class ExternalExceptionHandlerTest {

    private final ExternalExceptionHandler handler = new ExternalExceptionHandler();

    @Test
    @DisplayName("ResourceNotFoundException returns 404")
    void handleNotFound() {
        ResponseEntity<ErrorResponse> resp = handler.handleNotFound(
                new ResourceNotFoundException("Entity", "id", "123"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("BadRequestException returns 400")
    void handleBadRequest() {
        ResponseEntity<ErrorResponse> resp = handler.handleBadRequest(
                new BadRequestException("bad input"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("RestClientException returns 502")
    void handleRestClientException() {
        ResponseEntity<ErrorResponse> resp = handler.handleRestClientException(
                new RestClientException("connection refused"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getStatus()).isEqualTo(502);
    }
}
