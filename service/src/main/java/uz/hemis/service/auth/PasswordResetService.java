package uz.hemis.service.auth;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.exception.BadRequestException;
import uz.hemis.domain.entity.security.PasswordResetToken;
import uz.hemis.domain.entity.security.User;
import uz.hemis.domain.repository.PasswordResetTokenRepository;
import uz.hemis.domain.repository.UserRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${hemis.mail.smtp.host:mail.hemis.uz}")
    private String smtpHost;

    @Value("${hemis.mail.smtp.port:587}")
    private String smtpPort;

    @Value("${hemis.mail.smtp.username:no-reply@hemis.uz}")
    private String smtpUsername;

    @Value("${hemis.mail.smtp.password:}")
    private String smtpPassword;

    @Value("${hemis.frontend.url:https://hemis.uz}")
    private String frontendUrl;

    private static final int TOKEN_EXPIRY_MINUTES = 15;
    private static final int MAX_REQUESTS_PER_HOUR = 2;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Request password reset — generates token and sends email.
     * Always returns silently (no user enumeration).
     */
    @Transactional
    public void requestReset(String email) {
        if (email == null || email.isBlank()) {
            return; // Silent return for security
        }

        User user = userRepository.findByEmail(email.trim()).orElse(null);
        if (user == null) {
            log.debug("Password reset requested for non-existent email");
            return; // Silent return — prevent user enumeration
        }

        // Rate limit: max 3 requests per hour per user
        // Silent return (no exception) to prevent user enumeration
        long recentRequests = tokenRepository.countByUserAndCreatedAtAfter(
                user, LocalDateTime.now().minus(1, ChronoUnit.HOURS));
        if (recentRequests >= MAX_REQUESTS_PER_HOUR) {
            log.warn("Rate limit exceeded for password reset: user={}", user.getUsername());
            return; // Silent return — same as non-existent email
        }

        // Generate secure token
        String token = generateSecureToken();

        // Save token
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setExpiresAt(LocalDateTime.now().plus(TOKEN_EXPIRY_MINUTES, ChronoUnit.MINUTES));
        resetToken.setUsed(false);
        tokenRepository.save(resetToken);

        // Send email — delete token if send fails (prevents rate limit waste)
        try {
            sendResetEmail(user.getEmail(), token);
        } catch (Exception e) {
            tokenRepository.delete(resetToken);
            log.error("Failed to send reset email, token removed: user={}", user.getUsername(), e);
            return;
        }

        log.info("Password reset token generated for user: {}", user.getUsername());
    }

    /**
     * Reset password using token.
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters.");
        }

        PasswordResetToken resetToken = tokenRepository.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token."));

        if (resetToken.isExpired()) {
            resetToken.setUsed(true);
            tokenRepository.save(resetToken);
            throw new BadRequestException("Reset token has expired. Please request a new one.");
        }

        // Mark token as used immediately to prevent replay attacks
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        User user = resetToken.getUser();

        // Update password
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);

        // Invalidate all other unused tokens for this user
        tokenRepository.markAllUnusedAsUsedByUser(user);

        log.info("Password reset completed for user: {}", user.getUsername());
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        StringBuilder sb = new StringBuilder(64);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private void sendResetEmail(String toEmail, String token) {
        try {
            Properties prop = new Properties();
            prop.put("mail.smtp.auth", "true");
            prop.put("mail.smtp.starttls.enable", "true");
            prop.put("mail.smtp.host", smtpHost);
            prop.put("mail.smtp.port", smtpPort);
            prop.put("mail.smtp.ssl.trust", smtpHost);

            Session session = Session.getInstance(prop, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUsername, smtpPassword);
                }
            });

            String resetLink = frontendUrl + "/reset-password?token=" + token;

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(smtpUsername));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("HEMIS - Password Reset");

            String htmlContent = "<html><body style=\"font-family: Arial, sans-serif; line-height: 1.6;\">"
                    + "<div style=\"max-width: 500px; margin: 0 auto; padding: 20px;\">"
                    + "<h2 style=\"color: #1E2124;\">Password Reset</h2>"
                    + "<p>You requested a password reset for your HEMIS account.</p>"
                    + "<p>Click the button below to set a new password:</p>"
                    + "<div style=\"text-align: center; margin: 30px 0;\">"
                    + "<a href=\"" + resetLink + "\" "
                    + "style=\"background-color: #2F80ED; color: white; padding: 12px 32px; "
                    + "text-decoration: none; border-radius: 6px; font-weight: bold;\">Reset Password</a>"
                    + "</div>"
                    + "<p style=\"color: #6B7280; font-size: 14px;\">This link will expire in "
                    + TOKEN_EXPIRY_MINUTES + " minutes.</p>"
                    + "<p style=\"color: #6B7280; font-size: 14px;\">If you did not request this, "
                    + "please ignore this email.</p>"
                    + "</div></body></html>";

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(htmlContent, "text/html; charset=utf-8");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);

            message.setContent(multipart);
            Transport.send(message);

            log.info("Password reset email sent successfully");
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
}
