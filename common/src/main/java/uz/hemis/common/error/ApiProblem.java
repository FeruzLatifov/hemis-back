package uz.hemis.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RFC 7807 Problem Details for HTTP APIs.
 *
 * <p>Modern REST error response — {@code Content-Type: application/problem+json}.
 * {@code api-web}, {@code api-external} va {@code api-university} ushbu formatni
 * qaytaradi. {@code api-legacy} CUBA {@code [{error, details}]} formatini saqlaydi
 * (backward compat).</p>
 *
 * <p><b>Misol:</b></p>
 * <pre>
 * HTTP/1.1 404 Not Found
 * Content-Type: application/problem+json
 *
 * {
 *   "type": "https://hemis.uz/errors/resource-not-found",
 *   "title": "Student Not Found",
 *   "status": 404,
 *   "detail": "Student with id abc-123 does not exist",
 *   "instance": "/api/v1/web/students/abc-123",
 *   "code": "STUDENT_NOT_FOUND",
 *   "timestamp": "2026-04-20T10:15:30Z",
 *   "traceId": "5f3d4a..."
 * }
 * </pre>
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7807">RFC 7807</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ApiProblem {

    /** URI reference to error type docs (client-stable, don't change). */
    private String type;

    /** Short, human-readable summary (SHOULD NOT change for same {@code type}). */
    private String title;

    /** HTTP status code. */
    private int status;

    /** Human-readable detail specific to this occurrence. */
    private String detail;

    /** URI reference of the request that produced the problem (fill from servlet path). */
    private String instance;

    /** Application-specific error code (e.g., "STUDENT_NOT_FOUND"). Extension field. */
    private String code;

    /** When this problem occurred (UTC). Extension field. */
    private Instant timestamp;

    /** Distributed tracing ID (copy from MDC). Extension field. */
    private String traceId;

    /** Field-level validation errors: {@code {"pinfl": "must be 14 digits"}}. */
    private Map<String, String> errors;

    public ApiProblem() {
        this.timestamp = Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final ApiProblem p = new ApiProblem();

        public Builder type(String type) { p.type = type; return this; }
        public Builder title(String title) { p.title = title; return this; }
        public Builder status(int status) { p.status = status; return this; }
        public Builder detail(String detail) { p.detail = detail; return this; }
        public Builder instance(String instance) { p.instance = instance; return this; }
        public Builder code(String code) { p.code = code; return this; }
        public Builder traceId(String traceId) { p.traceId = traceId; return this; }

        public Builder fieldError(String field, String message) {
            if (p.errors == null) p.errors = new LinkedHashMap<>();
            p.errors.put(field, message);
            return this;
        }

        public ApiProblem build() { return p; }
    }

    // Getters (Jackson)
    public String getType() { return type; }
    public String getTitle() { return title; }
    public int getStatus() { return status; }
    public String getDetail() { return detail; }
    public String getInstance() { return instance; }
    public String getCode() { return code; }
    public Instant getTimestamp() { return timestamp; }
    public String getTraceId() { return traceId; }
    public Map<String, String> getErrors() { return errors; }
}
