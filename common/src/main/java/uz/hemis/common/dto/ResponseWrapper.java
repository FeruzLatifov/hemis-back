package uz.hemis.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Generic API Response Wrapper
 *
 * <p><strong>Purpose:</strong> Standardize all API responses</p>
 *
 * <p><strong>Legacy Compatibility:</strong></p>
 * <ul>
 *   <li>Field names may need adjustment based on CUBA Platform response format</li>
 *   <li>This is OPTIONAL wrapper - can be added without breaking contract</li>
 *   <li>If legacy API returns direct object, continue doing so</li>
 * </ul>
 *
 * @param <T> response data type
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseWrapper<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Success flag
     * JSON field: "success"
     */
    @JsonProperty("success")
    private Boolean success;

    /**
     * Response message
     * JSON field: "message"
     */
    @JsonProperty("message")
    private String message;

    /**
     * Response data (generic)
     * JSON field: "data"
     */
    @JsonProperty("data")
    private T data;

    /**
     * Error details (if any)
     * JSON field: "error"
     */
    @JsonProperty("error")
    private ErrorResponse error;

    // =====================================================
    // Factory Methods - Success Responses
    // =====================================================

    /**
     * Create success response with data
     *
     * @param data response data
     * @param <T> data type
     * @return wrapped response
     */
    public static <T> ResponseWrapper<T> success(T data) {
        ResponseWrapper<T> response = new ResponseWrapper<>();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }

    /**
     * Create success response with data and message
     *
     * @param data response data
     * @param message success message
     * @param <T> data type
     * @return wrapped response
     */
    public static <T> ResponseWrapper<T> success(T data, String message) {
        ResponseWrapper<T> response = new ResponseWrapper<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    /**
     * Create success response with only message (no data)
     *
     * @param message success message
     * @param <T> data type
     * @return wrapped response
     */
    public static <T> ResponseWrapper<T> success(String message) {
        ResponseWrapper<T> response = new ResponseWrapper<>();
        response.setSuccess(true);
        response.setMessage(message);
        return response;
    }

    // =====================================================
    // Factory Methods - Error Responses
    // =====================================================

    /**
     * Create error response
     *
     * @param error error details
     * @param <T> data type
     * @return wrapped response
     */
    public static <T> ResponseWrapper<T> error(ErrorResponse error) {
        ResponseWrapper<T> response = new ResponseWrapper<>();
        response.setSuccess(false);
        response.setError(error);
        return response;
    }

    /**
     * Create error response with message
     *
     * @param message error message
     * @param <T> data type
     * @return wrapped response
     */
    public static <T> ResponseWrapper<T> error(String message) {
        ResponseWrapper<T> response = new ResponseWrapper<>();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    /**
     * Create error response with message and error details
     *
     * @param message error message
     * @param error error details
     * @param <T> data type
     * @return wrapped response
     */
    public static <T> ResponseWrapper<T> error(String message, ErrorResponse error) {
        ResponseWrapper<T> response = new ResponseWrapper<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setError(error);
        return response;
    }
}
