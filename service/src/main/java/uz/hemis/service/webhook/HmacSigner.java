package uz.hemis.service.webhook;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * HMAC SHA-256 signature generator — webhook auth.
 *
 * <p>Univer side har request'ni signature orqali tasdiqlaydi:</p>
 * <pre>
 *   timestamp = "1715568000"
 *   body      = "{\"event_type\":\"classifier.updated\", ...}"
 *   secret    = "whsec_xxxxxxxxxxxxxxxx"
 *
 *   signature = hex(HMAC_SHA256(secret, timestamp + "." + body))
 *
 *   POST /api/hemis-callback/event
 *   X-Hemis-Timestamp: 1715568000
 *   X-Hemis-Signature: a3c7...
 * </pre>
 *
 * <p>Univer side {@code .env}'dagi secret bilan signature hisoblaydi va solishtiradi
 * (constant-time compare). 5 daqiqadan eski timestamp rad etiladi (replay attack).</p>
 *
 * @since ADR-0012
 */
@Component
public class HmacSigner {

    private static final String ALGORITHM = "HmacSHA256";

    /**
     * Signature hisoblash — {@code hex(HMAC_SHA256(secret, timestamp + "." + body))}.
     *
     * @param plainSecret  raw secret (whsec_xxx format) — markazda decrypt qilingan
     * @param timestamp    Unix epoch seconds (header X-Hemis-Timestamp)
     * @param body         request body — bayt-baytdan bir xil bo'lishi shart (canonical JSON)
     * @return hex-encoded signature (64 character)
     */
    public String sign(String plainSecret, long timestamp, String body) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(
                    plainSecret.getBytes(StandardCharsets.UTF_8),
                    ALGORITHM
            );
            mac.init(keySpec);

            String payload = timestamp + "." + body;
            byte[] signatureBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            return toHex(signatureBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("HMAC SHA-256 algorithm not available", e);
        } catch (java.security.InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid HMAC secret", e);
        }
    }

    /**
     * Constant-time compare — timing attack himoya.
     *
     * <p>{@link MessageDigest#isEqual(byte[], byte[])} JDK 7+ da constant-time'ga
     * kafolatlangan (`compareTo` JIT branch prediction'ga ochiq emas). Avval `char`
     * XOR ishlatardik — Java `int` arithmetic JIT branch leak xavfini saqlardi.</p>
     */
    public boolean verify(String expected, String received) {
        if (expected == null || received == null) return false;
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                received.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static String toHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
