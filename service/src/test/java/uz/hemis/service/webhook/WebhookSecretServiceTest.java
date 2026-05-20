package uz.hemis.service.webhook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WebhookSecretService — secret generation + bcrypt hash")
class WebhookSecretServiceTest {

    private WebhookSecretService service;

    @BeforeEach
    void setUp() {
        service = new WebhookSecretService();
    }

    @Test
    @DisplayName("generatePlainSecret — whsec_ prefix + 48 base64url chars (~288-bit entropy)")
    void generatePlainSecret_format() {
        String secret = service.generatePlainSecret();

        assertThat(secret).startsWith("whsec_");
        // 36 bytes → 48 base64url chars (no padding)
        assertThat(secret).hasSize("whsec_".length() + 48);
        // Base64url alphabet: A-Z a-z 0-9 - _
        assertThat(secret.substring("whsec_".length())).matches("[A-Za-z0-9_-]+");
    }

    @Test
    @DisplayName("generatePlainSecret — har chaqiriqda unique (random)")
    void generatePlainSecret_unique() {
        String s1 = service.generatePlainSecret();
        String s2 = service.generatePlainSecret();
        String s3 = service.generatePlainSecret();

        assertThat(s1).isNotEqualTo(s2);
        assertThat(s2).isNotEqualTo(s3);
        assertThat(s1).isNotEqualTo(s3);
    }

    @Test
    @DisplayName("hash() — BCrypt strength 12 (OWASP 2025)")
    void hash_bcryptStrength12() {
        String hash = service.hash("whsec_test_secret_value");

        // BCrypt $2a$12$ prefix — cost 12
        assertThat(hash).startsWith("$2a$12$");
        assertThat(hash).hasSize(60);  // BCrypt always 60 chars
    }

    @Test
    @DisplayName("hash() — different calls produce different hashes (random salt)")
    void hash_differentSalts() {
        String h1 = service.hash("same-secret");
        String h2 = service.hash("same-secret");

        assertThat(h1).isNotEqualTo(h2);  // BCrypt salt is random
    }

    @Test
    @DisplayName("matches() — roundtrip success")
    void matches_roundtrip() {
        String plain = "whsec_test_secret_value";
        String hash = service.hash(plain);

        assertThat(service.matches(plain, hash)).isTrue();
    }

    @Test
    @DisplayName("matches() — wrong secret returns false")
    void matches_wrongSecret() {
        String hash = service.hash("correct-secret");

        assertThat(service.matches("wrong-secret", hash)).isFalse();
    }

    @Test
    @DisplayName("End-to-end — generate → hash → matches")
    void endToEnd() {
        String generated = service.generatePlainSecret();
        String hash = service.hash(generated);

        assertThat(service.matches(generated, hash)).isTrue();
        assertThat(service.matches("attacker-guess", hash)).isFalse();
    }
}
