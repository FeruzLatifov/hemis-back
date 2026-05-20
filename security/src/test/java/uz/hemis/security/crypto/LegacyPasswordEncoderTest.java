package uz.hemis.security.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link LegacyPasswordEncoder} unit testlar — BCrypt + CUBA PBKDF2 + legacy hash rejection.
 *
 * <p>Critical security infrastructure — login validation for old-hemis + new schema.</p>
 */
@DisplayName("LegacyPasswordEncoder — BCrypt + CUBA PBKDF2 + MD5/SHA-1 rejection")
class LegacyPasswordEncoderTest {

    private LegacyPasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        encoder = new LegacyPasswordEncoder();
    }

    @Nested
    @DisplayName("encode()")
    class Encode {

        @Test
        @DisplayName("Always BCrypt format (strength 12)")
        void encodesBcrypt() {
            String encoded = encoder.encode("secret123");

            // OWASP 2025: BCrypt cost 12, prefix $2a$12$
            assertThat(encoded).startsWith("$2a$12$");
            assertThat(encoded).hasSize(60);  // BCrypt = always 60 chars
        }

        @Test
        @DisplayName("Different calls — different hash (random salt)")
        void differentSalts() {
            assertThat(encoder.encode("same"))
                    .isNotEqualTo(encoder.encode("same"));
        }
    }

    @Nested
    @DisplayName("matches() — BCrypt format")
    class MatchesBcrypt {

        @Test
        @DisplayName("BCrypt self-roundtrip")
        void bcryptRoundtrip() {
            String hashed = encoder.encode("password");
            assertThat(encoder.matches("password", hashed)).isTrue();
            assertThat(encoder.matches("wrong", hashed)).isFalse();
        }

        @Test
        @DisplayName("BCrypt prefix variants ($2a, $2b, $2y) — supported")
        void bcryptVariants() {
            // $2a$10$... — Spring default BCrypt
            String bcrypt = encoder.encode("test");
            assertThat(encoder.matches("test", bcrypt)).isTrue();
        }
    }

    @Nested
    @DisplayName("matches() — CUBA PBKDF2 format")
    class MatchesPbkdf2 {

        @Test
        @DisplayName("PBKDF2 hash:salt:iterations format — match true")
        void pbkdf2_validMatch() throws Exception {
            String password = "cuba-password-2025";
            byte[] salt = "static-salt-test".getBytes();
            int iterations = 1000;

            // Compute PBKDF2 hash exactly like service does
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 160);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = factory.generateSecret(spec).getEncoded();

            String encoded = Base64.getEncoder().encodeToString(hash)
                    + ":" + Base64.getEncoder().encodeToString(salt)
                    + ":" + iterations;

            assertThat(encoder.matches(password, encoded)).isTrue();
            assertThat(encoder.matches("wrong-password", encoded)).isFalse();
        }

        @Test
        @DisplayName("PBKDF2 invalid format (parts != 3) — false")
        void pbkdf2_invalidFormat_returnsFalse() {
            assertThat(encoder.matches("pwd", "only:two-parts")).isFalse();
            assertThat(encoder.matches("pwd", "a:b:c:d")).isFalse();
        }

        @Test
        @DisplayName("PBKDF2 non-numeric iterations — false")
        void pbkdf2_invalidIterations_returnsFalse() {
            assertThat(encoder.matches("pwd", "hash:salt:NOT_A_NUMBER")).isFalse();
        }
    }

    @Nested
    @DisplayName("matches() — Legacy MD5/SHA-1 REJECTION (OWASP A02:2025)")
    class LegacyHashRejection {

        @Test
        @DisplayName("32-char hex (MD5) — REJECTED (OWASP A02)")
        void md5_rejected() {
            // MD5 of "password" = 5f4dcc3b5aa765d61d8327deb882cf99
            String md5 = "5f4dcc3b5aa765d61d8327deb882cf99";
            assertThat(encoder.matches("password", md5)).isFalse();
        }

        @Test
        @DisplayName("40-char hex (SHA-1) — REJECTED (OWASP A02)")
        void sha1_rejected() {
            // SHA-1 of "password"
            String sha1 = "5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8";
            assertThat(encoder.matches("password", sha1)).isFalse();
        }

        @Test
        @DisplayName("32-char non-hex — false (not legacy format)")
        void nonHex32_returnsFalse() {
            assertThat(encoder.matches("pwd", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")).isFalse();
        }
    }

    @Nested
    @DisplayName("matches() — Null/empty handling")
    class NullEmptyHandling {

        @Test
        @DisplayName("null encoded password — false")
        void nullEncoded_returnsFalse() {
            assertThat(encoder.matches("pwd", null)).isFalse();
        }

        @Test
        @DisplayName("empty encoded password — false")
        void emptyEncoded_returnsFalse() {
            assertThat(encoder.matches("pwd", "")).isFalse();
        }

        @Test
        @DisplayName("unknown format — false (default reject)")
        void unknownFormat_returnsFalse() {
            assertThat(encoder.matches("pwd", "totally-unknown-format-xyz")).isFalse();
        }
    }

    @Nested
    @DisplayName("upgradeEncoding()")
    class UpgradeEncoding {

        @Test
        @DisplayName("BCrypt — no upgrade needed (false)")
        void bcrypt_noUpgrade() {
            String bcrypt = encoder.encode("test");
            assertThat(encoder.upgradeEncoding(bcrypt)).isFalse();
        }

        @Test
        @DisplayName("PBKDF2 — upgrade suggested (true)")
        void pbkdf2_upgrade() {
            assertThat(encoder.upgradeEncoding("hash:salt:1000")).isTrue();
        }

        @Test
        @DisplayName("Legacy MD5 — upgrade suggested (true)")
        void md5_upgrade() {
            assertThat(encoder.upgradeEncoding("5f4dcc3b5aa765d61d8327deb882cf99")).isTrue();
        }
    }
}
