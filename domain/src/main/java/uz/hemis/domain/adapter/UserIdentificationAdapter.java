package uz.hemis.domain.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.repository.SecUserRepository;
import uz.hemis.domain.repository.UserRepository;
import uz.hemis.common.port.security.UserIdentificationPort;

import java.util.Optional;
import java.util.UUID;

/**
 * User Identification Adapter - Implements UserIdentificationPort
 *
 * <p><strong>Clean Architecture:</strong></p>
 * <ul>
 *   <li>This adapter is in domain module (outer layer)</li>
 *   <li>Implements port defined in security module (inner layer)</li>
 *   <li>Provides hybrid user ID lookup</li>
 * </ul>
 *
 * <p><strong>Hybrid Lookup Strategy:</strong></p>
 * <ol>
 *   <li>First try users table (modern system)</li>
 *   <li>If not found, fallback to sec_user table (legacy CUBA)</li>
 * </ol>
 *
 * @since 2.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserIdentificationAdapter implements UserIdentificationPort {

    private final UserRepository userRepository;
    private final SecUserRepository secUserRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> getUserIdByUsername(String username) {
        log.debug("Looking up userId for username: {}", username);

        // 1. Try users table first (modern system)
        var userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            UUID userId = userOpt.get().getId();
            log.debug("User found in 'users' table: {} -> {}", username, userId);
            return Optional.of(userId);
        }

        // 2. Fallback to sec_user table (legacy CUBA)
        var secUserOpt = secUserRepository.findByLogin(username);
        if (secUserOpt.isPresent()) {
            UUID userId = secUserOpt.get().getId();
            log.debug("User found in 'sec_user' table (fallback): {} -> {}", username, userId);
            return Optional.of(userId);
        }

        // 3. Not found in either table
        log.warn("User not found in 'users' or 'sec_user' tables: {}", username);
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserInfo> getUserInfoByUsername(String username) {
        // 1. users table (modern) — full_name ustuni bor
        var userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            var u = userOpt.get();
            return Optional.of(new UserInfo(u.getId(), u.getFullName()));
        }

        // 2. sec_user fallback (legacy) — `name` field full name sifatida ishlatiladi
        var secUserOpt = secUserRepository.findByLogin(username);
        if (secUserOpt.isPresent()) {
            var s = secUserOpt.get();
            return Optional.of(new UserInfo(s.getId(), s.getName()));
        }

        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public String getUserSource(String username) {
        // Check users table first
        if (userRepository.findByUsername(username).isPresent()) {
            return "users";
        }

        // Check sec_user table
        if (secUserRepository.findByLogin(username).isPresent()) {
            return "sec_user";
        }

        return null;
    }
}
