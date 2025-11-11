package uz.hemis.security.service;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Test Security Configuration
 *
 * <p>Provides JwtEncoder and JwtDecoder beans for TokenServiceTest</p>
 *
 * <p><strong>IMPORTANT:</strong> This is a test configuration only.
 * Production configuration is in SecurityConfig.</p>
 *
 * @since 1.0.0
 */
@TestConfiguration
public class TestSecurityConfig {

    @Value("${hemis.security.jwt.secret:test-secret-key-minimum-256-bits-required-for-hmac-sha256}")
    private String jwtSecret;

    /**
     * JWT Encoder for generating tokens in tests
     */
    @Bean
    public JwtEncoder jwtEncoder() {
        SecretKey secretKey = new SecretKeySpec(
                jwtSecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
    }

    /**
     * JWT Decoder for validating tokens in tests
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKey secretKey = new SecretKeySpec(
                jwtSecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
