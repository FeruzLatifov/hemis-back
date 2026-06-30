package uz.hemis.security.crypto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Legacy Password Encoder - Verifies BCrypt and CUBA Platform PBKDF2 formats only
 *
 * <p><strong>Verified Formats (matches() == true possible):</strong></p>
 * <ol>
 *   <li><strong>BCrypt:</strong> $2a$10$... (new system - users table)</li>
 *   <li><strong>CUBA PBKDF2:</strong> hash:salt:iteration (colon-separated)</li>
 * </ol>
 *
 * <p><strong>Rejected legacy formats (OWASP A02 — 2025):</strong> plain MD5 (32-char hex)
 * and SHA-1 (40-char hex) are detected only to log a SECURITY event; matches() always
 * returns {@code false}. Old CUBA users with these hashes must reset their password.</p>
 *
 * <p><strong>Detection Logic:</strong></p>
 * <ul>
 *   <li>Starts with $2a$/$2b$/$2y$ → BCrypt (verified)</li>
 *   <li>Contains ":" with 3 parts → PBKDF2 (verified)</li>
 *   <li>32-char hex string → MD5 (REJECTED — returns false)</li>
 *   <li>40-char hex string → SHA-1 (REJECTED — returns false)</li>
 * </ul>
 */
@Slf4j
public class LegacyPasswordEncoder implements PasswordEncoder {

    // OWASP 2025: BCrypt strength 12 minimum (was default 10).
    // Backward compat: BCrypt stores cost in hash prefix, so existing $2a$10$... passwords still verify.
    private final BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder(12);
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final int KEY_LENGTH = 160; // 160 bits = 20 bytes

    // Hex pattern: only 0-9, a-f, A-F
    private static final java.util.regex.Pattern HEX_PATTERN =
            java.util.regex.Pattern.compile("^[0-9a-fA-F]+$");

    @Override
    public String encode(CharSequence rawPassword) {
        // Always encode new passwords with BCrypt
        return bcryptEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isEmpty()) {
            log.warn("Empty encoded password");
            return false;
        }

        // 1. Check if BCrypt format ($2a$, $2b$, $2y$)
        if (isBCrypt(encodedPassword)) {
            log.debug("Using BCrypt encoder for password verification");
            return bcryptEncoder.matches(rawPassword, encodedPassword);
        }

        // 2. Check if CUBA PBKDF2 format (hash:salt:iteration)
        if (encodedPassword.contains(":")) {
            log.debug("Using CUBA PBKDF2 encoder for password verification");
            return matchesCubaPbkdf2(rawPassword, encodedPassword);
        }

        // 3. Legacy MD5/SHA-1 hashes — REMOVED (OWASP A02 — explicitly forbidden in 2025).
        // Plain MD5 (32-char hex) and SHA-1 (40-char hex) hashes are vulnerable to rainbow
        // table attacks — a leaked DB dump = 100% credential compromise. Detection is
        // retained only to log security events; matching always returns false.
        if ((encodedPassword.length() == 32 || encodedPassword.length() == 40)
                && isHex(encodedPassword)) {
            log.warn("SECURITY: Legacy {} hash detected — rejected. User must reset password.",
                    encodedPassword.length() == 32 ? "MD5" : "SHA-1");
            return false;
        }

        // 4. Unknown format
        log.error("Unknown password format (length={}, prefix={})",
                encodedPassword.length(),
                encodedPassword.substring(0, Math.min(10, encodedPassword.length())));
        return false;
    }

    // =====================================================
    // Format Detection Helpers
    // =====================================================

    private boolean isBCrypt(String encoded) {
        return encoded.startsWith("$2a$") ||
               encoded.startsWith("$2b$") ||
               encoded.startsWith("$2y$");
    }

    private boolean isHex(String value) {
        return HEX_PATTERN.matcher(value).matches();
    }

    // =====================================================
    // MD5/SHA-1 Verification — REMOVED (OWASP A02 — 2025)
    // =====================================================
    // Plain MD5/SHA-1 hashes are vulnerable to rainbow tables. Old CUBA users
    // with these hashes must reset password (force flow on next login).
    // Detection in matches() above logs a SECURITY warning but always returns false.

    // =====================================================
    // PBKDF2 Verification (CUBA Platform alternative)
    // =====================================================

    /**
     * Verify password against CUBA PBKDF2 format
     *
     * @param rawPassword plain text password
     * @param encodedPassword CUBA format: hash:salt:iteration
     * @return true if password matches
     */
    private boolean matchesCubaPbkdf2(CharSequence rawPassword, String encodedPassword) {
        try {
            String[] parts = encodedPassword.split(":");
            if (parts.length != 3) {
                log.error("Invalid CUBA PBKDF2 format (expected hash:salt:iteration): length={}",
                        parts.length);
                return false;
            }

            String storedHash = parts[0];
            String saltBase64 = parts[1];
            int iterations = Integer.parseInt(parts[2]);

            // Decode salt from Base64
            byte[] salt = Base64.getDecoder().decode(saltBase64);

            // Hash the raw password with same salt and iterations
            byte[] computedHash = hashPbkdf2(rawPassword.toString(), salt, iterations);
            String computedHashBase64 = Base64.getEncoder().encodeToString(computedHash);

            // Boolean natijani log'ga yozish login enumeration leak — log.debug ham
            // production'da DEBUG profile'da yoqilgan bo'lsa, parol mosligi
            // ko'rinib qoladi. Faqat iteration soni log'ga (operational).
            return storedHash.equals(computedHashBase64);

        } catch (Exception e) {
            log.error("Error verifying CUBA PBKDF2 password: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Hash password using PBKDF2WithHmacSHA1 (CUBA Platform algorithm)
     */
    private byte[] hashPbkdf2(String password, byte[] salt, int iterations)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                iterations,
                KEY_LENGTH
        );

        SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        return factory.generateSecret(spec).getEncoded();
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        // Suggest upgrading from any legacy format to BCrypt
        return !isBCrypt(encodedPassword);
    }
}
