package uz.hemis.service.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import uz.hemis.common.exception.BadRequestException;
import uz.hemis.domain.entity.security.PasswordResetToken;
import uz.hemis.domain.entity.security.User;
import uz.hemis.domain.repository.PasswordResetTokenRepository;
import uz.hemis.domain.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordResetService — token generation + reset flow")
class PasswordResetServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordResetTokenRepository tokenRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetService service;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@hemis.uz");
        user.setUsername("testuser");

        lenient().when(tokenRepository.countByUserAndCreatedAtAfter(any(User.class), any(LocalDateTime.class)))
                .thenReturn(0L);
    }

    @Nested
    @DisplayName("requestReset() — anti-enumeration silent")
    class RequestReset {

        @Test
        @DisplayName("null email — silent return (no enumeration)")
        void nullEmail_silentReturn() {
            service.requestReset(null);

            verify(userRepository, never()).findByEmail(anyString());
            verify(tokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("blank email — silent return")
        void blankEmail_silentReturn() {
            service.requestReset("   ");

            verify(userRepository, never()).findByEmail(anyString());
        }

        @Test
        @DisplayName("user not found — silent return (no enumeration)")
        void userNotFound_silentReturn() {
            when(userRepository.findByEmail("ghost@hemis.uz")).thenReturn(Optional.empty());

            service.requestReset("ghost@hemis.uz");

            verify(tokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("email trimmed before lookup")
        void emailTrimmed() {
            when(userRepository.findByEmail("user@hemis.uz")).thenReturn(Optional.empty());

            service.requestReset("  user@hemis.uz  ");

            verify(userRepository).findByEmail("user@hemis.uz");
        }

        @Test
        @DisplayName("rate limit exceeded (>=2 requests/hour) — silent return, no token saved")
        void rateLimit_silentReturn() {
            when(userRepository.findByEmail("user@hemis.uz")).thenReturn(Optional.of(user));
            when(tokenRepository.countByUserAndCreatedAtAfter(eq(user), any(LocalDateTime.class)))
                    .thenReturn(2L); // limit reached

            service.requestReset("user@hemis.uz");

            verify(tokenRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("resetPassword()")
    class ResetPassword {

        @Test
        @DisplayName("password < 6 chars — BadRequestException")
        void shortPassword_throws() {
            assertThatThrownBy(() -> service.resetPassword("token", "abc"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("6 characters");
        }

        @Test
        @DisplayName("null password — BadRequestException")
        void nullPassword_throws() {
            assertThatThrownBy(() -> service.resetPassword("token", null))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("invalid token — BadRequestException")
        void invalidToken_throws() {
            when(tokenRepository.findByTokenAndUsedFalse("bad-token"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.resetPassword("bad-token", "NewPassword123!"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid or expired");
        }

        @Test
        @DisplayName("expired token — marked used, throws")
        void expiredToken_markedUsed_throws() {
            PasswordResetToken token = new PasswordResetToken();
            token.setUser(user);
            token.setToken("expired-token");
            token.setExpiresAt(LocalDateTime.now().minusMinutes(20));  // expired
            token.setUsed(false);

            when(tokenRepository.findByTokenAndUsedFalse("expired-token"))
                    .thenReturn(Optional.of(token));

            assertThatThrownBy(() -> service.resetPassword("expired-token", "NewPassword123!"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("expired");

            assertThat(token.isUsed()).isTrue();
            // Token saved as 'used' to prevent retry
            verify(tokenRepository).save(token);
        }

        @Test
        @DisplayName("happy path — password encoded, token used, other tokens invalidated")
        void happyPath() {
            PasswordResetToken token = new PasswordResetToken();
            token.setUser(user);
            token.setToken("valid-token");
            token.setExpiresAt(LocalDateTime.now().plusMinutes(10));
            token.setUsed(false);

            when(tokenRepository.findByTokenAndUsedFalse("valid-token"))
                    .thenReturn(Optional.of(token));
            when(passwordEncoder.encode("NewSecurePassword123!"))
                    .thenReturn("$2a$12$hashedPassword");

            service.resetPassword("valid-token", "NewSecurePassword123!");

            // Token marked used (replay prevention)
            assertThat(token.isUsed()).isTrue();
            // Password updated with encoder
            assertThat(user.getPassword()).isEqualTo("$2a$12$hashedPassword");
            verify(userRepository).save(user);
            // Other tokens for this user invalidated
            verify(tokenRepository).markAllUnusedAsUsedByUser(user);
        }
    }
}
