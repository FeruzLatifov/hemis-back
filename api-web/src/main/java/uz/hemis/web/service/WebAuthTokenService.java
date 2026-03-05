package uz.hemis.web.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * JWT token generation and decoding for Web Authentication.
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebAuthTokenService {

    private static final long ACCESS_TOKEN_TTL = 900L;       // 15 minutes
    private static final long REFRESH_TOKEN_TTL = 604800L;   // 7 days

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public record TokenPair(
            String accessToken,
            String accessJti,
            String refreshToken,
            String refreshJti,
            long accessExpiresIn
    ) {}

    /**
     * Generate access + refresh token pair.
     *
     * @param userId   user UUID (real or synthetic for legacy)
     * @param username username (stored in refresh token for legacy validation)
     * @return token pair with JTI identifiers
     */
    public TokenPair generateTokenPair(UUID userId, String username) {
        Instant now = Instant.now();
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        // Access token
        String accessJti = UUID.randomUUID().toString();
        JwtClaimsSet.Builder accessBuilder = JwtClaimsSet.builder()
                .issuer("hemis")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(ACCESS_TOKEN_TTL))
                .subject(userId.toString())
                .id(accessJti);
        if (username != null) {
            accessBuilder.claim("username", username);
        }
        JwtClaimsSet accessClaims = accessBuilder.build();
        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, accessClaims)).getTokenValue();

        // Refresh token
        String refreshJti = UUID.randomUUID().toString();
        JwtClaimsSet.Builder refreshBuilder = JwtClaimsSet.builder()
                .issuer("hemis")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(REFRESH_TOKEN_TTL))
                .subject(userId.toString())
                .id(refreshJti)
                .claim("type", "refresh");

        if (username != null) {
            refreshBuilder.claim("username", username);
        }

        String refreshToken = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, refreshBuilder.build())).getTokenValue();

        return new TokenPair(accessToken, accessJti, refreshToken, refreshJti, ACCESS_TOKEN_TTL);
    }

    /**
     * Decode and validate a JWT token.
     */
    public Jwt decode(String token) {
        return jwtDecoder.decode(token);
    }

    public long getAccessTokenTtl() {
        return ACCESS_TOKEN_TTL;
    }

    public long getRefreshTokenTtl() {
        return REFRESH_TOKEN_TTL;
    }
}
