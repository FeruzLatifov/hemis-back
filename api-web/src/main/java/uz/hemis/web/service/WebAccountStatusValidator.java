package uz.hemis.web.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import uz.hemis.domain.entity.security.SecUser;
import uz.hemis.domain.entity.security.User;
import uz.hemis.domain.repository.SecUserRepository;
import uz.hemis.domain.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Validates user account status (enabled, locked, etc.) for auth flows.
 *
 * <p>Handles both new-system (users table) and legacy (sec_user table) accounts.</p>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebAccountStatusValidator {

    private final UserRepository userRepository;
    private final SecUserRepository secUserRepository;

    /**
     * Resolve userId for a given username.
     *
     * <p>New-system users get their real UUID; legacy sec_user-only accounts
     * receive a deterministic synthetic UUID.</p>
     *
     * @return resolved userId
     */
    public UUID resolveUserId(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            UUID id = userOpt.get().getId();
            log.info("NEW system user - userId: {}", id);
            return id;
        }

        UUID syntheticId = generateLegacyUserId(username);
        log.warn("LEGACY system user - generated synthetic userId: {} for username: {}", syntheticId, username);
        return syntheticId;
    }

    /**
     * Validate that the account behind a refresh token is still active.
     *
     * @throws AccountDisabledException  if account disabled
     * @throws AccountLockedException    if account locked
     * @throws CredentialsExpiredException if legacy user must change password
     * @throws InvalidTokenException     if token lacks required claims
     */
    public void validateForRefresh(UUID userId, Jwt decodedToken) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            validateNewSystemUser(userOpt.get(), userId);
        } else {
            validateLegacyUser(userId, decodedToken);
        }
    }

    // ---------------------------------------------------------------
    // Account Lockout
    // ---------------------------------------------------------------

    private static final int MAX_FAILED_ATTEMPTS = 10;
    private static final int AUTO_UNLOCK_MINUTES = 15;

    /**
     * Record a failed login attempt. Locks account after {@value MAX_FAILED_ATTEMPTS} failures.
     * Uses atomic UPDATE to avoid race conditions with @Version optimistic locking.
     */
    @Transactional
    public void recordFailedAttempt(String username) {
        int updated = userRepository.incrementFailedAttemptsAndLockIfNeeded(username, MAX_FAILED_ATTEMPTS);
        if (updated > 0) {
            log.warn("Recorded failed login attempt for: {}", username);
        }
    }

    /**
     * Reset failed attempts on successful login.
     * Uses atomic UPDATE to avoid race conditions.
     */
    @Transactional
    public void resetFailedAttempts(String username) {
        userRepository.resetFailedAttemptsByUsername(username);
    }

    // ---------------------------------------------------------------

    private void validateNewSystemUser(User user, UUID userId) {
        if (!user.getEnabled()) {
            log.warn("Refresh blocked - account disabled: userId={}", userId);
            throw new AccountDisabledException();
        }
        if (!Boolean.TRUE.equals(user.getAccountNonLocked())) {
            // Auto-unlock: if locked_at + 15 min has passed, unlock automatically
            LocalDateTime lockedAt = user.getLockedAt();
            if (lockedAt != null && lockedAt.plusMinutes(AUTO_UNLOCK_MINUTES).isBefore(LocalDateTime.now())) {
                log.info("Auto-unlocking expired lock for userId={}, lockedAt={}", userId, lockedAt);
                user.setAccountNonLocked(true);
                user.setFailedAttempts(0);
                user.setLockedAt(null);
                userRepository.save(user);
            } else {
                log.warn("Refresh blocked - account locked: userId={}", userId);
                throw new AccountLockedException();
            }
        }
        log.info("Account status OK - userId: {}", userId);
    }

    private void validateLegacyUser(UUID userId, Jwt decodedToken) {
        String username = decodedToken.getClaimAsString("username");
        if (username == null || username.isEmpty()) {
            log.warn("Refresh blocked - no username in token: userId={}", userId);
            throw new InvalidTokenException("Token'da username ma'lumoti yo'q.");
        }

        Optional<SecUser> secUserOpt = secUserRepository.findByLoginAndActiveTrue(username);
        if (secUserOpt.isEmpty()) {
            log.warn("Refresh blocked - legacy user inactive/deleted: username={}, userId={}", username, userId);
            throw new AccountDisabledException("Akkaunt faolsizlantirilgan yoki o'chirilgan. Administrator bilan bog'laning.");
        }

        SecUser secUser = secUserOpt.get();
        if (Boolean.TRUE.equals(secUser.getChangePasswordAtLogon())) {
            log.warn("Refresh blocked - legacy user must change password: username={}", username);
            throw new CredentialsExpiredException();
        }

        log.info("Legacy user status OK - username: {}, userId: {}", username, userId);
    }

    private UUID generateLegacyUserId(String username) {
        UUID namespace = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");
        return UUID.nameUUIDFromBytes((namespace.toString() + ":" + username).getBytes());
    }

    // -- Typed exceptions for the controller to map to HTTP responses --

    public static class AccountDisabledException extends RuntimeException {
        public AccountDisabledException() {
            super("Akkaunt faolsizlantirilgan. Administrator bilan bog'laning.");
        }
        public AccountDisabledException(String msg) { super(msg); }
    }

    public static class AccountLockedException extends RuntimeException {
        public AccountLockedException() {
            super("Akkaunt bloklangan. Administrator bilan bog'laning.");
        }
    }

    public static class CredentialsExpiredException extends RuntimeException {
        public CredentialsExpiredException() {
            super("Parolni o'zgartirish talab qilinadi.");
        }
    }

    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException(String msg) { super(msg); }
    }
}
