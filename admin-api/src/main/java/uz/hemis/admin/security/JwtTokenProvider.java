package uz.hemis.admin.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token Provider
 *
 * Generates and validates JWT tokens for authentication
 * Token structure: Header.Payload.Signature
 * Algorithm: HS512 (HMAC-SHA512)
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long tokenExpiration;
    private final long refreshTokenExpiration;

    /**
     * Constructor
     *
     * @param secret                 JWT secret key (from application.yml)
     * @param tokenExpiration        Access token expiration in milliseconds (30 days)
     * @param refreshTokenExpiration Refresh token expiration in milliseconds (60 days)
     */
    public JwtTokenProvider(
            @Value("${jwt.secret:xSD6Yc2rihEiA5Ei_HEMIS_ADMIN_SECRET_KEY_2025}") String secret,
            @Value("${jwt.expiration:2592000000}") long tokenExpiration,
            @Value("${jwt.refresh-expiration:5184000000}") long refreshTokenExpiration) {

        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.tokenExpiration = tokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;

        log.info("JWT Token Provider initialized:");
        log.info("  - Access Token Expiration: {} days", tokenExpiration / (1000 * 60 * 60 * 24));
        log.info("  - Refresh Token Expiration: {} days", refreshTokenExpiration / (1000 * 60 * 60 * 24));
    }

    /**
     * Generate access token
     *
     * @param userId       User ID
     * @param username     Username
     * @param universityId University ID (for multi-tenant)
     * @return JWT access token
     */
    public String generateToken(String userId, String username, String universityId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + tokenExpiration);

        String token = Jwts.builder()
                .subject(userId)
                .claim("username", username)
                .claim("universityId", universityId)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();

        log.debug("Generated access token for user: {} (expires: {})", username, expiryDate);
        return token;
    }

    /**
     * Generate refresh token
     *
     * @param userId User ID
     * @return JWT refresh token
     */
    public String generateRefreshToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        String token = Jwts.builder()
                .subject(userId)
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();

        log.debug("Generated refresh token for user: {} (expires: {})", userId, expiryDate);
        return token;
    }

    /**
     * Extract user ID from token
     *
     * @param token JWT token
     * @return User ID
     */
    public String getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    /**
     * Extract username from token
     *
     * @param token JWT token
     * @return Username
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("username", String.class);
    }

    /**
     * Extract university ID from token
     *
     * @param token JWT token
     * @return University ID
     */
    public String getUniversityIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("universityId", String.class);
    }

    /**
     * Get token type (access or refresh)
     *
     * @param token JWT token
     * @return Token type
     */
    public String getTokenType(String token) {
        Claims claims = parseClaims(token);
        return claims.get("type", String.class);
    }

    /**
     * Validate JWT token
     *
     * @param token JWT token
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate token and check type
     *
     * @param token        JWT token
     * @param expectedType Expected token type (access or refresh)
     * @return true if valid and type matches
     */
    public boolean validateTokenWithType(String token, String expectedType) {
        try {
            Claims claims = parseClaims(token);
            String tokenType = claims.get("type", String.class);
            return expectedType.equals(tokenType);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Parse JWT claims
     *
     * @param token JWT token
     * @return Claims
     * @throws JwtException if token is invalid
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Get expiration date from token
     *
     * @param token JWT token
     * @return Expiration date
     */
    public Date getExpirationFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getExpiration();
    }

    /**
     * Check if token is expired
     *
     * @param token JWT token
     * @return true if expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationFromToken(token);
            return expiration.before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }
}
