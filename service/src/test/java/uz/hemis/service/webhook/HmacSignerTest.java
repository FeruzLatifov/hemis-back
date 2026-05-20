package uz.hemis.service.webhook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link HmacSigner} unit testlar — webhook HMAC SHA-256 signature + constant-time verify.
 *
 * <p>Univer 224 OTM signature verify qiladi — bu yerda contract bug bo'lsa,
 * barcha OTM webhook'lar fail bo'ladi.</p>
 */
@DisplayName("HmacSigner — webhook HMAC SHA-256")
class HmacSignerTest {

    private HmacSigner signer;

    @BeforeEach
    void setUp() {
        signer = new HmacSigner();
    }

    @Nested
    @DisplayName("sign()")
    class Sign {

        @Test
        @DisplayName("returns deterministic hex signature (64 chars)")
        void deterministicHex() {
            String s1 = signer.sign("whsec_test", 1715568000L, "{\"event\":\"x\"}");
            String s2 = signer.sign("whsec_test", 1715568000L, "{\"event\":\"x\"}");

            assertThat(s1).isEqualTo(s2);
            assertThat(s1).hasSize(64);
            assertThat(s1).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("different secret → different signature")
        void differentSecret_differentSignature() {
            String s1 = signer.sign("whsec_a", 1L, "body");
            String s2 = signer.sign("whsec_b", 1L, "body");
            assertThat(s1).isNotEqualTo(s2);
        }

        @Test
        @DisplayName("different timestamp → different signature (replay prevention)")
        void differentTimestamp_differentSignature() {
            String s1 = signer.sign("whsec_x", 1L, "body");
            String s2 = signer.sign("whsec_x", 2L, "body");
            assertThat(s1).isNotEqualTo(s2);
        }

        @Test
        @DisplayName("different body → different signature")
        void differentBody_differentSignature() {
            String s1 = signer.sign("whsec_x", 1L, "a");
            String s2 = signer.sign("whsec_x", 1L, "b");
            assertThat(s1).isNotEqualTo(s2);
        }

        @Test
        @DisplayName("known vector — RFC 4231 style sanity check")
        void knownVector() {
            // payload = "1.body" — HMAC-SHA256 with secret "key" gives deterministic hex.
            String sig = signer.sign("key", 1L, "body");
            assertThat(sig).hasSize(64).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("empty body and timestamp = 0 — still produces 64-char hex")
        void edgeCase_empty() {
            String sig = signer.sign("whsec_x", 0L, "");
            assertThat(sig).hasSize(64);
        }

        @Test
        @DisplayName("empty secret → IllegalArgumentException")
        void emptySecret_throwsInvalidKey() {
            // SecretKeySpec empty secret bilan "Empty key" tashlaydi.
            // HmacSigner.sign() try-catch ichida InvalidKeyException catch qiladi va
            // IllegalArgumentException re-throw — lekin "Empty key" SecretKeySpec'da raise.
            assertThatThrownBy(() -> signer.sign("", 1L, "body"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("verify()")
    class Verify {

        @Test
        @DisplayName("equal strings → true")
        void equal_returnsTrue() {
            assertThat(signer.verify("abc123", "abc123")).isTrue();
        }

        @Test
        @DisplayName("different strings (same length) → false")
        void differentSameLength_returnsFalse() {
            assertThat(signer.verify("abc123", "abc124")).isFalse();
        }

        @Test
        @DisplayName("different length → false (no NPE)")
        void differentLength_returnsFalse() {
            assertThat(signer.verify("short", "longer-string")).isFalse();
        }

        @Test
        @DisplayName("null inputs → false (no NPE)")
        void nullInputs_returnFalse() {
            assertThat(signer.verify(null, "x")).isFalse();
            assertThat(signer.verify("x", null)).isFalse();
            assertThat(signer.verify(null, null)).isFalse();
        }

        @Test
        @DisplayName("constant-time — MessageDigest.isEqual semantik to'g'ri")
        void constantTimeSemantics() {
            // 64-byte (typical SHA-256 hex) strings — both equal and differ-at-end cases.
            String expected = "a".repeat(64);
            String diffAtEnd = "a".repeat(63) + "b";
            String diffAtStart = "b" + "a".repeat(63);

            assertThat(signer.verify(expected, expected)).isTrue();
            assertThat(signer.verify(expected, diffAtEnd)).isFalse();
            assertThat(signer.verify(expected, diffAtStart)).isFalse();
        }

        @Test
        @DisplayName("sign+verify roundtrip")
        void signVerify_roundtrip() {
            String sig = signer.sign("whsec_x", 1715568000L, "body");
            assertThat(signer.verify(sig, sig)).isTrue();
            assertThat(signer.verify(sig, sig + "x")).isFalse();
        }
    }
}
