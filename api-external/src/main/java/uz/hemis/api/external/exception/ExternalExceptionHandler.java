package uz.hemis.api.external.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import uz.hemis.common.dto.ErrorResponse;
import uz.hemis.common.exception.BadRequestException;
import uz.hemis.common.exception.ResourceNotFoundException;

/**
 * External API Exception Handler
 *
 * <p>Handles exceptions for S2S integration controllers (api-external module)</p>
 * <p>Scope: Only uz.hemis.api.external.controller package</p>
 *
 * @since 1.0.0
 */
@RestControllerAdvice(basePackages = "uz.hemis.api.external.controller")
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class ExternalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        log.warn("External API resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(404, "NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        log.warn("External API bad request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, "BAD_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponse> handleRestClientException(RestClientException ex) {
        log.error("External API integration error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ErrorResponse.of(502, "INTEGRATION_ERROR", "External service unavailable"));
    }
}
