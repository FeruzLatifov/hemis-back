package uz.hemis.service.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * P1 guardrail exit-criterion: audit JSONB snapshots must NEVER carry a plaintext PINFL or FIO.
 * If a future change makes {@link AuditRepository#redactSensitiveFields} leaky, this build fails.
 */
@DisplayName("AuditRepository PII/PINFL redaction guardrail")
class AuditRedactionTest {

    /** A syntactically valid 14-digit PINFL. */
    private static final String PINFL = "31234567890123";

    private final AuditRepository repo = new AuditRepository(
            new JdbcTemplate(),
            new ObjectMapper(),
            List.of("password", "pinfl", "firstName", "lastName", "middleName",
                    "fullName", "phone", "email"));

    @Test
    @DisplayName("nested PINFL under an arbitrary key is masked (value-based safety net)")
    void masksNestedPinflUnderUnknownKey() {
        Map<String, Object> snapshot = Map.of(
                "student", Map.of(
                        "someUnexpectedField", PINFL,
                        "children", List.of(Map.of("guardianId", PINFL))));

        String rendered = repo.redactSensitiveFields(snapshot).toString();

        assertThat(rendered).doesNotContain(PINFL);
    }

    @Test
    @DisplayName("PINFL keyed field masked; plain fields survive untouched")
    void masksPinflKeyKeepsOthers() {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("pinfl", PINFL);
        snapshot.put("facultyId", 42);

        Map<String, Object> out = repo.redactSensitiveFields(snapshot);

        assertThat(out.get("pinfl").toString()).doesNotContain(PINFL);
        assertThat(out.get("facultyId")).isEqualTo(42);
    }

    @Test
    @DisplayName("FIO + phone masked by inferred type (snake_case and camelCase both match)")
    void masksFioAndPhone() {
        Map<String, Object> snapshot = Map.of(
                "firstName", "Vali",
                "full_name", "Aliyev Vali Salimovich",
                "phone", "+998901234567");

        Map<String, Object> out = repo.redactSensitiveFields(snapshot);

        assertThat(out.get("firstName").toString()).isNotEqualTo("Vali");
        assertThat(out.get("full_name").toString()).doesNotContain("Aliyev");
        assertThat(out.get("phone").toString()).contains("****");
    }

    @Test
    @DisplayName("14-digit PINFL embedded in free text is masked; a 16-digit number is left intact")
    void masksEmbeddedPinflButNotLongerNumbers() {
        Map<String, Object> snapshot = Map.of(
                "_raw", "duplicate key uq_users_pinfl=(" + PINFL + ") failed",
                "cardLike", "1234567890123456"); // 16 digits — NOT a PINFL run

        Map<String, Object> out = repo.redactSensitiveFields(snapshot);

        assertThat(out.get("_raw").toString()).doesNotContain(PINFL);
        assertThat(out.get("cardLike")).isEqualTo("1234567890123456");
    }

    @Test
    @DisplayName("PINFL as a numeric literal is masked (error_log.request_body numeric edge)")
    void masksNumericPinfl() {
        Map<String, Object> snapshot = Map.of(
                "pinfl", 12345678901234L,      // sensitive key, numeric
                "someId", 12345678901234L,     // arbitrary key, bare 14-digit numeric
                "smallNumber", 42);            // ordinary number, must survive

        Map<String, Object> out = repo.redactSensitiveFields(snapshot);

        assertThat(out.get("pinfl").toString()).doesNotContain("12345678901234");
        assertThat(out.get("someId").toString()).doesNotContain("12345678901234");
        assertThat(out.get("smallNumber")).isEqualTo(42);
    }
}
