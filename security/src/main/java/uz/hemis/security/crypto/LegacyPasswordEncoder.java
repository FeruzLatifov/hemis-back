package uz.hemis.security.crypto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Legacy Password Encoder - Supports both CUBA Platform and BCrypt formats
 *
 * <p><strong>CUBA Platform Format:</strong> PBKDF2WithHmacSHA1</p>
 * <ul>
 *   <li>Format: hash:salt:iteration (colon-separated)</li>
 *   <li>Example: dGVzdA==:c2FsdA==:1000</li>
 *   <li>Used in old-hemis (sec_user table)</li>
 * </ul>
 *
 * <p><strong>BCrypt Format:</strong></p>
 * <ul>
 *   <li>Format: $2a$10$... (standard BCrypt)</li>
 *   <li>Used in new system (users table)</li>
 * </ul>
 */
@Slf4j
public class LegacyPasswordEncoder implements PasswordEncoder {

    private final BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final int KEY_LENGTH = 160; // 160 bits = 20 bytes

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

        // Check if BCrypt format ($2a$, $2b$, $2y$)
        if (encodedPassword.startsWith("$2a$") ||
            encodedPassword.startsWith("$2b$") ||
            encodedPassword.startsWith("$2y$")) {
            log.debug("Using BCrypt encoder for password verification");
            return bcryptEncoder.matches(rawPassword, encodedPassword);
        }

        // Otherwise, assume CUBA Platform format (hash:salt:iteration)
        log.debug("Using CUBA Platform encoder for password verification");
        return matchesCubaFormat(rawPassword, encodedPassword);
    }

    /**
     * Verify password against CUBA Platform format
     *
     * @param rawPassword plain text password
     * @param encodedPassword CUBA format: hash:salt:iteration
     * @return true if password matches
     */
    private boolean matchesCubaFormat(CharSequence rawPassword, String encodedPassword) {
        try {
            String[] parts = encodedPassword.split(":");
            if (parts.length != 3) {
                log.error("Invalid CUBA password format (expected hash:salt:iteration): {}", encodedPassword);
                return false;
            }

            String storedHash = parts[0];
            String saltBase64 = parts[1];
            int iterations = Integer.parseInt(parts[2]);

            // Decode salt from Base64
            byte[] salt = Base64.getDecoder().decode(saltBase64);

            // Hash the raw password with same salt and iterations
            byte[] computedHash = hashPassword(rawPassword.toString(), salt, iterations);
            String computedHashBase64 = Base64.getEncoder().encodeToString(computedHash);

            boolean matches = storedHash.equals(computedHashBase64);
            log.debug("CUBA password match: {} (iterations: {})", matches, iterations);

            return matches;

        } catch (Exception e) {
            log.error("Error verifying CUBA password: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Hash password using PBKDF2WithHmacSHA1 (CUBA Platform algorithm)
     *
     * @param password plain text password
     * @param salt salt bytes
     * @param iterations iteration count
     * @return hashed password bytes
     */
    private byte[] hashPassword(String password, byte[] salt, int iterations)
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
        // Suggest upgrading from CUBA format to BCrypt
        return !encodedPassword.startsWith("$2a$") &&
               !encodedPassword.startsWith("$2b$") &&
               !encodedPassword.startsWith("$2y$");
    }
}
