package uz.hemis.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Error Response DTO
 *
 * <p><strong>Purpose:</strong> Standardize error responses across API</p>
 *
 * <p><strong>CRITICAL - Legacy Compatibility:</strong></p>
 * <ul>
 *   <li>Field names match expected error format (if legacy format exists)</li>
 *   <li>HTTP status codes preserved</li>
 *   <li>Error messages in same format</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Timestamp when error occurred
     * JSON field: "timestamp"
     */
    @JsonProperty("timestamp")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * HTTP status code
     * JSON field: "status"
     */
    @JsonProperty("status")
    private Integer status;

    /**
     * Error type/code
     * JSON field: "error"
     */
    @JsonProperty("error")
    private String error;

    /**
     * Error message
     * JSON field: "message"
     */
    @JsonProperty("message")
    private String message;

    /**
     * Request path where error occurred
     * JSON field: "path"
     */
    @JsonProperty("path")
    private String path;

    /**
     * ⭐ Sentry Event ID for error tracking correlation
     * JSON field: "eventId"
     */
    @JsonProperty("eventId")
    private String eventId;

    /**
     * ⭐ Error code for programmatic error handling
     * JSON field: "errorCode"
     */
    @JsonProperty("errorCode")
    private String errorCode;

    /**
     * Validation errors (field-level)
     * JSON field: "errors"
     */
    @JsonProperty("errors")
    private List<FieldError> errors;

    /**
     * Additional error details
     * JSON field: "details"
     */
    @JsonProperty("details")
    private Map<String, Object> details;

    // =====================================================
    // Field Error (for validation errors)
    // =====================================================

    /**
     * Field-level validation error
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FieldError implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * Field name
         * JSON field: "field"
         */
        @JsonProperty("field")
        private String field;

        /**
         * Rejected value
         * JSON field: "rejectedValue"
         */
        @JsonProperty("rejectedValue")
        private Object rejectedValue;

        /**
         * Error message
         * JSON field: "message"
         */
        @JsonProperty("message")
        private String message;

        /**
         * Error code
         * JSON field: "code"
         */
        @JsonProperty("code")
        private String code;
    }

    // =====================================================
    // Factory Methods
    // =====================================================

    /**
     * Create simple error response
     *
     * @param status HTTP status code
     * @param error error type
     * @param message error message
     * @return error response
     */
    public static ErrorResponse of(Integer status, String error, String message) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .build();
    }

    /**
     * Create error response with path
     *
     * @param status HTTP status code
     * @param error error type
     * @param message error message
     * @param path request path
     * @return error response
     */
    public static ErrorResponse of(Integer status, String error, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .build();
    }

    /**
     * ⭐ Create error response with Sentry Event ID
     *
     * @param status HTTP status code
     * @param error error type
     * @param message error message
     * @param path request path
     * @param eventId Sentry event ID
     * @param errorCode error code
     * @return error response
     */
    public static ErrorResponse of(Integer status, String error, String message,
                                   String path, String eventId, String errorCode) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .eventId(eventId)
                .errorCode(errorCode)
                .build();
    }

    /**
     * Create validation error response
     *
     * @param status HTTP status code
     * @param message error message
     * @param path request path
     * @param errors field errors
     * @return error response
     */
    public static ErrorResponse validationError(
            Integer status,
            String message,
            String path,
            List<FieldError> errors
    ) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error("Validation Failed")
                .message(message)
                .path(path)
                .errors(errors)
                .build();
    }
}
