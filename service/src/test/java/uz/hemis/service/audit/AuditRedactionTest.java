package uz.hemis.service.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import org.springframework.mock.env.MockEnvironment;
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

    /**
     * OAuth client maxfiy kalit kalitlari uchun ALOHIDA instansiya — PROD ro'yxati bilan.
     *
     * <p>Yuqoridagi {@code repo} qisqartirilgan ro'yxat ishlatadi, shuning uchun u maxfiy kalit kalitlarini
     * umuman tekshirmaydi. Bu ro'yxat {@code app/src/main/resources/application.yml}
     * ({@code hemis.audit.redact-fields}) va {@code AuditRepository} dagi {@code @Value} sukut
     * qiymati bilan bir xil bo'lishi kerak.</p>
     */
    private final AuditRepository prodRepo = new AuditRepository(
            new JdbcTemplate(),
            new ObjectMapper(),
            List.of("password", "token", "secret", "clientSecret", "client_secret",
                    "plainSecret", "plain_secret", "authorization", "pinfl"));

    @Test
    @DisplayName("YAML ro'yxati haqiqatan bog'lanadi — application.yml'dagi kalitlar maskalanadi")
    void yamlSequenceIsActuallyBound() {
        // The regression this pins: the key list used to be read with @Value, which cannot bind a
        // YAML sequence, so every key beyond the annotation's own default was ignored and passport
        // numbers / FIO / addresses went into old_value in clear text.
        MockEnvironment env = new MockEnvironment();
        env.setProperty("hemis.audit.redact-fields[0]", "passport");
        env.setProperty("hemis.audit.redact-fields[1]", "address");

        AuditRepository fromYaml = new AuditRepository(new JdbcTemplate(), new ObjectMapper(), env);

        Map<String, Object> out = fromYaml.redactSensitiveFields(
                new LinkedHashMap<>(Map.of("passport", "AA0000000", "address", "Toshkent", "code", "301")));

        // The mask SHAPE is the redactor's business (it masks by inferred type); what this test
        // pins is that the YAML-configured keys are honoured at all.
        assertThat(String.valueOf(out.get("passport"))).isNotEqualTo("AA0000000").contains("*");
        assertThat(String.valueOf(out.get("address"))).isNotEqualTo("Toshkent").contains("*");
        assertThat(out.get("code")).isEqualTo("301");
    }

    @Test
    @DisplayName("OAuth client maxfiy kaliti activity_log snapshot'ida maskalanadi (clientSecret + plainSecret)")
    void masksOAuthClientSecrets() {
        // rotateSecret @Audited bilan belgilangan; AuditAspect qaytar qiymatni new_value'ga yozadi,
        // javobda esa markaz generatsiya qilgan OCHIQ maxfiy kalit bor. Kalit moslashuvi ANIQ tenglik bo'yicha,
        // ya'ni 'secret' kaliti 'plainSecret' ni QOPLAMAYDI — shuning uchun ikkalasi ham ro'yxatda.
        String plain = "csec_SUPER_SECRET_VALUE_1234567890";
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("clientId", "otm301");
        snapshot.put("plainSecret", plain);
        snapshot.put("secretVersion", 3);
        snapshot.put("nested", Map.of("clientSecret", plain));

        Map<String, Object> out = prodRepo.redactSensitiveFields(snapshot);
        String rendered = out.toString();

        assertThat(rendered).doesNotContain(plain);
        assertThat(out.get("clientId")).isEqualTo("otm301");   // maxfiy kalit bo'lmagan maydon tegilmaydi
        assertThat(out.get("secretVersion")).isEqualTo(3);
    }

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
