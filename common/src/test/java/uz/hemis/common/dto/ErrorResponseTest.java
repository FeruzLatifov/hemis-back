package uz.hemis.common.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ErrorResponse DTO Unit Tests
 *
 * <p>Covers:</p>
 * <ul>
 *   <li>No-arg constructor and default values</li>
 *   <li>Builder pattern and field accessors</li>
 *   <li>Factory methods: of(3-arg), of(4-arg), of(6-arg), validationError</li>
 *   <li>Nested FieldError construction</li>
 *   <li>Validation error list handling</li>
 *   <li>Details map handling</li>
 * </ul>
 */
@DisplayName("ErrorResponse Tests")
class ErrorResponseTest {

    // ==================================================================
    // Default constructor
    // ==================================================================

    @Nested
    @DisplayName("no-arg constructor")
    class NoArgConstructor {

        @Test
        @DisplayName("Should create instance with timestamp default and nulls elsewhere")
        void shouldCreateInstanceWithDefaults() {
            ErrorResponse response = new ErrorResponse();

            // @Builder.Default sets timestamp to LocalDateTime.now()
            assertThat(response.getTimestamp()).isNotNull();
            assertThat(response.getStatus()).isNull();
            assertThat(response.getError()).isNull();
            assertThat(response.getMessage()).isNull();
            assertThat(response.getPath()).isNull();
            assertThat(response.getEventId()).isNull();
            assertThat(response.getErrorCode()).isNull();
            assertThat(response.getErrors()).isNull();
            assertThat(response.getDetails()).isNull();
        }
    }

    // ==================================================================
    // Builder
    // ==================================================================

    @Nested
    @DisplayName("builder")
    class BuilderTests {

        @Test
        @DisplayName("Should build with all fields set")
        void shouldBuildWithAllFields() {
            LocalDateTime ts = LocalDateTime.of(2025, 1, 15, 10, 30);
            List<ErrorResponse.FieldError> fieldErrors = List.of(
                    ErrorResponse.FieldError.builder()
                            .field("email")
                            .message("must not be empty")
                            .build()
            );
            Map<String, Object> details = Map.of("retryAfter", 30);

            ErrorResponse response = ErrorResponse.builder()
                    .timestamp(ts)
                    .status(400)
                    .error("Bad Request")
                    .message("Validation failed")
                    .path("/api/v1/users")
                    .eventId("evt-123")
                    .errorCode("VALIDATION_ERROR")
                    .errors(fieldErrors)
                    .details(details)
                    .build();

            assertThat(response.getTimestamp()).isEqualTo(ts);
            assertThat(response.getStatus()).isEqualTo(400);
            assertThat(response.getError()).isEqualTo("Bad Request");
            assertThat(response.getMessage()).isEqualTo("Validation failed");
            assertThat(response.getPath()).isEqualTo("/api/v1/users");
            assertThat(response.getEventId()).isEqualTo("evt-123");
            assertThat(response.getErrorCode()).isEqualTo("VALIDATION_ERROR");
            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getDetails()).containsEntry("retryAfter", 30);
        }

        @Test
        @DisplayName("Builder default should set timestamp when not explicitly provided")
        void builderDefaultShouldSetTimestamp() {
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            ErrorResponse response = ErrorResponse.builder()
                    .status(500)
                    .build();

            LocalDateTime after = LocalDateTime.now().plusSeconds(1);
            assertThat(response.getTimestamp()).isNotNull();
            assertThat(response.getTimestamp()).isAfterOrEqualTo(before);
            assertThat(response.getTimestamp()).isBeforeOrEqualTo(after);
        }
    }

    // ==================================================================
    // Field accessors (Lombok @Data getters/setters)
    // ==================================================================

    @Nested
    @DisplayName("field accessors")
    class FieldAccessors {

        @Test
        @DisplayName("Should set and get status")
        void shouldSetAndGetStatus() {
            ErrorResponse response = new ErrorResponse();
            response.setStatus(404);

            assertThat(response.getStatus()).isEqualTo(404);
        }

        @Test
        @DisplayName("Should set and get error")
        void shouldSetAndGetError() {
            ErrorResponse response = new ErrorResponse();
            response.setError("Not Found");

            assertThat(response.getError()).isEqualTo("Not Found");
        }

        @Test
        @DisplayName("Should set and get message")
        void shouldSetAndGetMessage() {
            ErrorResponse response = new ErrorResponse();
            response.setMessage("Resource not found");

            assertThat(response.getMessage()).isEqualTo("Resource not found");
        }

        @Test
        @DisplayName("Should set and get path")
        void shouldSetAndGetPath() {
            ErrorResponse response = new ErrorResponse();
            response.setPath("/api/v1/students/123");

            assertThat(response.getPath()).isEqualTo("/api/v1/students/123");
        }

        @Test
        @DisplayName("Should set and get eventId")
        void shouldSetAndGetEventId() {
            ErrorResponse response = new ErrorResponse();
            response.setEventId("sentry-event-abc");

            assertThat(response.getEventId()).isEqualTo("sentry-event-abc");
        }

        @Test
        @DisplayName("Should set and get errorCode")
        void shouldSetAndGetErrorCode() {
            ErrorResponse response = new ErrorResponse();
            response.setErrorCode("STUDENT_NOT_FOUND");

            assertThat(response.getErrorCode()).isEqualTo("STUDENT_NOT_FOUND");
        }

        @Test
        @DisplayName("Should set and get details map")
        void shouldSetAndGetDetails() {
            ErrorResponse response = new ErrorResponse();
            Map<String, Object> details = Map.of("key", "value", "count", 5);
            response.setDetails(details);

            assertThat(response.getDetails()).hasSize(2);
            assertThat(response.getDetails()).containsEntry("key", "value");
            assertThat(response.getDetails()).containsEntry("count", 5);
        }
    }

    // ==================================================================
    // Factory method: of(status, error, message)
    // ==================================================================

    @Nested
    @DisplayName("of(status, error, message) factory method")
    class OfThreeArgs {

        @Test
        @DisplayName("Should create error response with status, error, and message")
        void shouldCreateWithThreeArgs() {
            ErrorResponse response = ErrorResponse.of(500, "Internal Server Error", "Unexpected failure");

            assertThat(response.getStatus()).isEqualTo(500);
            assertThat(response.getError()).isEqualTo("Internal Server Error");
            assertThat(response.getMessage()).isEqualTo("Unexpected failure");
            assertThat(response.getTimestamp()).isNotNull();
            assertThat(response.getPath()).isNull();
            assertThat(response.getEventId()).isNull();
            assertThat(response.getErrorCode()).isNull();
            assertThat(response.getErrors()).isNull();
        }
    }

    // ==================================================================
    // Factory method: of(status, error, message, path)
    // ==================================================================

    @Nested
    @DisplayName("of(status, error, message, path) factory method")
    class OfFourArgs {

        @Test
        @DisplayName("Should create error response with path included")
        void shouldCreateWithFourArgs() {
            ErrorResponse response = ErrorResponse.of(404, "Not Found", "Student not found", "/api/v1/students/999");

            assertThat(response.getStatus()).isEqualTo(404);
            assertThat(response.getError()).isEqualTo("Not Found");
            assertThat(response.getMessage()).isEqualTo("Student not found");
            assertThat(response.getPath()).isEqualTo("/api/v1/students/999");
            assertThat(response.getTimestamp()).isNotNull();
            assertThat(response.getEventId()).isNull();
        }
    }

    // ==================================================================
    // Factory method: of(status, error, message, path, eventId, errorCode)
    // ==================================================================

    @Nested
    @DisplayName("of(status, error, message, path, eventId, errorCode) factory method")
    class OfSixArgs {

        @Test
        @DisplayName("Should create error response with Sentry event ID and error code")
        void shouldCreateWithSixArgs() {
            ErrorResponse response = ErrorResponse.of(
                    503, "Service Unavailable", "Database connection failed",
                    "/api/v1/data", "sentry-evt-xyz", "DB_CONNECTION_FAILED"
            );

            assertThat(response.getStatus()).isEqualTo(503);
            assertThat(response.getError()).isEqualTo("Service Unavailable");
            assertThat(response.getMessage()).isEqualTo("Database connection failed");
            assertThat(response.getPath()).isEqualTo("/api/v1/data");
            assertThat(response.getEventId()).isEqualTo("sentry-evt-xyz");
            assertThat(response.getErrorCode()).isEqualTo("DB_CONNECTION_FAILED");
            assertThat(response.getTimestamp()).isNotNull();
        }
    }

    // ==================================================================
    // Factory method: validationError
    // ==================================================================

    @Nested
    @DisplayName("validationError factory method")
    class ValidationError {

        @Test
        @DisplayName("Should create validation error with field errors list")
        void shouldCreateValidationError() {
            List<ErrorResponse.FieldError> fieldErrors = List.of(
                    ErrorResponse.FieldError.builder()
                            .field("firstName")
                            .rejectedValue("")
                            .message("must not be blank")
                            .code("NotBlank")
                            .build(),
                    ErrorResponse.FieldError.builder()
                            .field("email")
                            .rejectedValue("not-an-email")
                            .message("must be a valid email address")
                            .code("Email")
                            .build()
            );

            ErrorResponse response = ErrorResponse.validationError(
                    422, "Validation failed", "/api/v1/users", fieldErrors
            );

            assertThat(response.getStatus()).isEqualTo(422);
            assertThat(response.getError()).isEqualTo("Validation Failed");
            assertThat(response.getMessage()).isEqualTo("Validation failed");
            assertThat(response.getPath()).isEqualTo("/api/v1/users");
            assertThat(response.getTimestamp()).isNotNull();
            assertThat(response.getErrors()).hasSize(2);
        }

        @Test
        @DisplayName("Should create validation error with empty field errors list")
        void shouldCreateValidationErrorWithEmptyList() {
            ErrorResponse response = ErrorResponse.validationError(
                    400, "No errors provided", "/api/v1/test", List.of()
            );

            assertThat(response.getErrors()).isEmpty();
            assertThat(response.getError()).isEqualTo("Validation Failed");
        }
    }

    // ==================================================================
    // Nested FieldError
    // ==================================================================

    @Nested
    @DisplayName("FieldError nested class")
    class FieldErrorTests {

        @Test
        @DisplayName("Should create FieldError with no-arg constructor")
        void shouldCreateWithNoArgConstructor() {
            ErrorResponse.FieldError fieldError = new ErrorResponse.FieldError();

            assertThat(fieldError.getField()).isNull();
            assertThat(fieldError.getRejectedValue()).isNull();
            assertThat(fieldError.getMessage()).isNull();
            assertThat(fieldError.getCode()).isNull();
        }

        @Test
        @DisplayName("Should create FieldError with all-arg constructor")
        void shouldCreateWithAllArgConstructor() {
            ErrorResponse.FieldError fieldError = new ErrorResponse.FieldError(
                    "password", "short", "must be at least 8 characters", "Size"
            );

            assertThat(fieldError.getField()).isEqualTo("password");
            assertThat(fieldError.getRejectedValue()).isEqualTo("short");
            assertThat(fieldError.getMessage()).isEqualTo("must be at least 8 characters");
            assertThat(fieldError.getCode()).isEqualTo("Size");
        }

        @Test
        @DisplayName("Should create FieldError with builder")
        void shouldCreateWithBuilder() {
            ErrorResponse.FieldError fieldError = ErrorResponse.FieldError.builder()
                    .field("age")
                    .rejectedValue(-1)
                    .message("must be positive")
                    .code("Positive")
                    .build();

            assertThat(fieldError.getField()).isEqualTo("age");
            assertThat(fieldError.getRejectedValue()).isEqualTo(-1);
            assertThat(fieldError.getMessage()).isEqualTo("must be positive");
            assertThat(fieldError.getCode()).isEqualTo("Positive");
        }

        @Test
        @DisplayName("Should set and get FieldError fields via setters")
        void shouldSetAndGetViaSetters() {
            ErrorResponse.FieldError fieldError = new ErrorResponse.FieldError();
            fieldError.setField("username");
            fieldError.setRejectedValue(null);
            fieldError.setMessage("must not be null");
            fieldError.setCode("NotNull");

            assertThat(fieldError.getField()).isEqualTo("username");
            assertThat(fieldError.getRejectedValue()).isNull();
            assertThat(fieldError.getMessage()).isEqualTo("must not be null");
            assertThat(fieldError.getCode()).isEqualTo("NotNull");
        }

        @Test
        @DisplayName("FieldError rejectedValue should accept any object type")
        void rejectedValueShouldAcceptAnyType() {
            ErrorResponse.FieldError withString = ErrorResponse.FieldError.builder()
                    .field("f1").rejectedValue("text").build();
            ErrorResponse.FieldError withInt = ErrorResponse.FieldError.builder()
                    .field("f2").rejectedValue(42).build();
            ErrorResponse.FieldError withList = ErrorResponse.FieldError.builder()
                    .field("f3").rejectedValue(List.of(1, 2, 3)).build();

            assertThat(withString.getRejectedValue()).isInstanceOf(String.class);
            assertThat(withInt.getRejectedValue()).isInstanceOf(Integer.class);
            assertThat(withList.getRejectedValue()).isInstanceOf(List.class);
        }
    }

    // ==================================================================
    // Validation errors list on ErrorResponse
    // ==================================================================

    @Nested
    @DisplayName("errors list")
    class ErrorsList {

        @Test
        @DisplayName("Should set and get field errors list via setter")
        void shouldSetAndGetFieldErrorsList() {
            ErrorResponse response = new ErrorResponse();
            List<ErrorResponse.FieldError> errors = List.of(
                    ErrorResponse.FieldError.builder().field("name").message("required").build()
            );
            response.setErrors(errors);

            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors().get(0).getField()).isEqualTo("name");
            assertThat(response.getErrors().get(0).getMessage()).isEqualTo("required");
        }

        @Test
        @DisplayName("Errors list should be null by default (not empty)")
        void errorsListShouldBeNullByDefault() {
            ErrorResponse response = new ErrorResponse();

            assertThat(response.getErrors()).isNull();
        }
    }

    // ==================================================================
    // @Data (equals, hashCode, toString)
    // ==================================================================

    @Nested
    @DisplayName("@Data generated methods")
    class DataMethods {

        @Test
        @DisplayName("Two ErrorResponses with same fields should be equal")
        void twoResponsesWithSameFieldsShouldBeEqual() {
            LocalDateTime ts = LocalDateTime.of(2025, 6, 1, 12, 0);

            ErrorResponse a = ErrorResponse.builder()
                    .timestamp(ts).status(404).error("Not Found").message("gone").build();
            ErrorResponse b = ErrorResponse.builder()
                    .timestamp(ts).status(404).error("Not Found").message("gone").build();

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("toString should contain key field values")
        void toStringShouldContainKeyFields() {
            ErrorResponse response = ErrorResponse.of(400, "Bad Request", "Invalid input");

            String str = response.toString();

            assertThat(str).contains("400");
            assertThat(str).contains("Bad Request");
            assertThat(str).contains("Invalid input");
        }
    }
}
