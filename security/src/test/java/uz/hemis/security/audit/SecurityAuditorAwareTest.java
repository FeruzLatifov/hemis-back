package uz.hemis.security.audit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SecurityAuditorAwareTest {

    private SecurityAuditorAware auditorAware;

    @BeforeEach
    void setUp() {
        auditorAware = new SecurityAuditorAware();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentAuditor_whenAuthenticated_returnsUsername() {
        var auth = new UsernamePasswordAuthenticationToken("admin", "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<String> auditor = auditorAware.getCurrentAuditor();

        assertTrue(auditor.isPresent());
        assertEquals("admin", auditor.get());
    }

    @Test
    void getCurrentAuditor_whenNoAuthentication_returnsEmpty() {
        Optional<String> auditor = auditorAware.getCurrentAuditor();

        assertFalse(auditor.isPresent());
    }

    @Test
    void getCurrentAuditor_whenAnonymous_returnsEmpty() {
        var auth = new AnonymousAuthenticationToken("key", "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<String> auditor = auditorAware.getCurrentAuditor();

        assertFalse(auditor.isPresent());
    }

    @Test
    void getCurrentAuditor_whenNotAuthenticated_returnsEmpty() {
        var auth = new UsernamePasswordAuthenticationToken("user", "password");
        // Not authenticated (no authorities set, isAuthenticated depends on implementation)
        auth.setAuthenticated(false);
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<String> auditor = auditorAware.getCurrentAuditor();

        assertFalse(auditor.isPresent());
    }

    @Test
    void getCurrentAuditor_withDifferentUsername_returnsCorrectName() {
        var auth = new UsernamePasswordAuthenticationToken("john.doe@hemis.uz", "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<String> auditor = auditorAware.getCurrentAuditor();

        assertTrue(auditor.isPresent());
        assertEquals("john.doe@hemis.uz", auditor.get());
    }

    @Test
    void getCurrentAuditor_whenNullContext_returnsEmpty() {
        SecurityContextHolder.getContext().setAuthentication(null);

        Optional<String> auditor = auditorAware.getCurrentAuditor();

        assertFalse(auditor.isPresent());
    }
}
