package uz.hemis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import uz.hemis.common.dto.CaptchaResponse;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Captcha Service - Numeric Captcha Generation
 *
 * <p><strong>OLD-HEMIS Compatibility:</strong></p>
 * <ul>
 *   <li>Generates numeric captcha (5 digits)</li>
 *   <li>Returns base64-encoded PNG image</li>
 *   <li>Stores captcha in Redis with 300 second expiration</li>
 *   <li>100% compatible with old-hemis response format</li>
 * </ul>
 *
 * <p><strong>Security:</strong></p>
 * <ul>
 *   <li>Uses SecureRandom for unpredictable captcha values</li>
 *   <li>Redis TTL prevents replay attacks</li>
 *   <li>captchaValue only returned in development mode</li>
 * </ul>
 *
 * <p><strong>Redis Storage:</strong></p>
 * <pre>
 * Key: captcha:{captchaId}
 * Value: {captchaValue}
 * TTL: 300 seconds (5 minutes)
 * </pre>
 *
 * @author HEMIS Backend Team
 * @since 2025-11-19
 */
@Service
@Slf4j
public class CaptchaService {

    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public CaptchaService(@Qualifier("stringRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int CAPTCHA_LENGTH = 5;
    private static final int CAPTCHA_EXPIRATION_SECONDS = 300; // 5 minutes
    private static final int IMAGE_WIDTH = 200;
    private static final int IMAGE_HEIGHT = 60;

    @Value("${hemis.captcha.return-value:false}")
    private boolean returnCaptchaValue;

    /**
     * Generate numeric captcha
     * <p>
     * Old-hemis endpoint: GET /app/rest/v2/services/captcha/getNumericCaptcha
     * </p>
     *
     * @return CaptchaResponse with image and metadata
     */
    public CaptchaResponse generateNumericCaptcha() {
        log.debug("🔢 Generating numeric captcha...");

        // 1. Generate random 5-digit numeric code
        String captchaValue = generateRandomNumericCode();
        log.debug("Generated captcha value: {}", captchaValue);

        // 2. Generate unique IDs
        String id = UUID.randomUUID().toString();
        String captchaId = UUID.randomUUID().toString();

        // 3. Create PNG image
        String base64Image;
        try {
            base64Image = createCaptchaImage(captchaValue);
        } catch (IOException e) {
            log.error("❌ Failed to create captcha image", e);
            throw new RuntimeException("Failed to generate captcha image", e);
        }

        // 4. Store in Redis
        String redisKey = "captcha:" + captchaId;
        redisTemplate.opsForValue().set(redisKey, captchaValue, CAPTCHA_EXPIRATION_SECONDS, TimeUnit.SECONDS);
        log.debug("✅ Stored captcha in Redis: key={}, ttl={}s", redisKey, CAPTCHA_EXPIRATION_SECONDS);

        // 5. Build response
        CaptchaResponse response = CaptchaResponse.builder()
                .id(id)
                .image(base64Image)
                .captchaId(captchaId)
                .captchaType("numeric")
                .expiresIn(CAPTCHA_EXPIRATION_SECONDS)
                .build();

        // Only return captcha value in development mode (for testing)
        if (returnCaptchaValue) {
            response.setCaptchaValue(captchaValue);
            log.warn("⚠️ DEVELOPMENT MODE: Returning captcha value in response!");
        }

        log.info("✅ Generated captcha: id={}, captchaId={}, expiresIn={}s", id, captchaId, CAPTCHA_EXPIRATION_SECONDS);
        return response;
    }

    /**
     * Validate captcha
     *
     * @param captchaId    Captcha ID from response
     * @param captchaValue User-entered captcha value
     * @return true if valid, false otherwise
     */
    public boolean validateCaptcha(String captchaId, String captchaValue) {
        if (captchaId == null || captchaValue == null) {
            log.warn("⚠️ Captcha validation failed: null parameters");
            return false;
        }

        String redisKey = "captcha:" + captchaId;
        String storedValue = redisTemplate.opsForValue().get(redisKey);

        if (storedValue == null) {
            log.warn("⚠️ Captcha validation failed: captcha not found or expired (key={})", redisKey);
            return false;
        }

        boolean isValid = storedValue.equals(captchaValue);

        if (isValid) {
            // Delete after successful validation (one-time use)
            redisTemplate.delete(redisKey);
            log.info("✅ Captcha validated successfully: captchaId={}", captchaId);
        } else {
            log.warn("⚠️ Captcha validation failed: incorrect value (captchaId={})", captchaId);
        }

        return isValid;
    }

    /**
     * Generate random 5-digit numeric code
     *
     * @return String with 5 digits (e.g., "61343")
     */
    private String generateRandomNumericCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            code.append(RANDOM.nextInt(10)); // 0-9
        }
        return code.toString();
    }

    /**
     * Create captcha image as base64-encoded PNG
     *
     * @param captchaValue The text to render
     * @return Base64-encoded data URI (data:image/png;base64,...)
     * @throws IOException If image generation fails
     */
    private String createCaptchaImage(String captchaValue) throws IOException {
        // Create buffered image
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Background (white)
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

        // Draw noise lines (security)
        g2d.setColor(new Color(220, 220, 220));
        for (int i = 0; i < 8; i++) {
            int x1 = RANDOM.nextInt(IMAGE_WIDTH);
            int y1 = RANDOM.nextInt(IMAGE_HEIGHT);
            int x2 = RANDOM.nextInt(IMAGE_WIDTH);
            int y2 = RANDOM.nextInt(IMAGE_HEIGHT);
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Draw captcha text
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        g2d.setColor(new Color(50, 50, 50));

        // Center the text
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(captchaValue);
        int x = (IMAGE_WIDTH - textWidth) / 2;
        int y = ((IMAGE_HEIGHT - fm.getHeight()) / 2) + fm.getAscent();

        // Draw each character with slight random offset
        int charX = x;
        for (char c : captchaValue.toCharArray()) {
            int offsetY = RANDOM.nextInt(10) - 5; // -5 to +5 pixels
            g2d.drawString(String.valueOf(c), charX, y + offsetY);
            charX += fm.charWidth(c);
        }

        // Add noise dots
        g2d.setColor(new Color(180, 180, 180));
        for (int i = 0; i < 50; i++) {
            int nx = RANDOM.nextInt(IMAGE_WIDTH);
            int ny = RANDOM.nextInt(IMAGE_HEIGHT);
            g2d.fillRect(nx, ny, 2, 2);
        }

        g2d.dispose();

        // Convert to base64 PNG
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();
        String base64 = Base64.getEncoder().encodeToString(imageBytes);

        return "data:image/png;base64," + base64;
    }
}
