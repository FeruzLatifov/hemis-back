package uz.hemis.api.legacy.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import uz.hemis.domain.entity.University;
import uz.hemis.domain.entity.User;
import uz.hemis.domain.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LegacySecurityHelperTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LegacySecurityHelper legacySecurityHelper;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // =========================================================================
    // getCurrentUsername tests
    // =========================================================================

    @Test
    @DisplayName("getCurrentUsername: no authentication in context -> returns 'system'")
    void getCurrentUsername_noAuth_returnsSystem() {
        // SecurityContext has no authentication set (default after clearContext)
        SecurityContextHolder.clearContext();

        String result = legacySecurityHelper.getCurrentUsername();

        assertThat(result).isEqualTo("system");
    }

    @Test
    @DisplayName("getCurrentUsername: simple Authentication with getName() -> returns name")
    void getCurrentUsername_withAuth_returnsName() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin_user");
        SecurityContextHolder.getContext().setAuthentication(auth);

        String result = legacySecurityHelper.getCurrentUsername();

        assertThat(result).isEqualTo("admin_user");
    }

    @Test
    @DisplayName("getCurrentUsername: JwtAuthenticationToken with 'username' claim -> returns username claim")
    void getCurrentUsername_withJwt_returnsUsername() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("username", "otm401")
                .subject("sub-fallback")
                .build();
        JwtAuthenticationToken jwtAuth = new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(jwtAuth);

        String result = legacySecurityHelper.getCurrentUsername();

        assertThat(result).isEqualTo("otm401");
    }

    @Test
    @DisplayName("getCurrentUsername: JwtAuthenticationToken without 'username' claim -> falls back to subject")
    void getCurrentUsername_withJwt_noUsernameClaim_returnsSubject() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("jwt-subject-value")
                .build();
        JwtAuthenticationToken jwtAuth = new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(jwtAuth);

        String result = legacySecurityHelper.getCurrentUsername();

        assertThat(result).isEqualTo("jwt-subject-value");
    }

    // =========================================================================
    // getUniversityCodeFromContext tests
    // =========================================================================

    @Test
    @DisplayName("getUniversityCodeFromContext: no authentication -> returns null")
    void getUniversityCodeFromContext_noAuth_returnsNull() {
        SecurityContextHolder.clearContext();

        String result = legacySecurityHelper.getUniversityCodeFromContext();

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getUniversityCodeFromContext: user has university -> returns university code from DB")
    void getUniversityCodeFromContext_userHasUniversity_returnsUniversityCode() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("username", "otm401")
                .subject("otm401")
                .build();
        JwtAuthenticationToken jwtAuth = new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(jwtAuth);

        University university = new University();
        university.setCode("TATU_CODE");
        User user = new User();
        user.setUniversity(university);
        when(userRepository.findByUsername("otm401")).thenReturn(Optional.of(user));

        String result = legacySecurityHelper.getUniversityCodeFromContext();

        assertThat(result).isEqualTo("TATU_CODE");
        verify(userRepository).findByUsername("otm401");
    }

    @Test
    @DisplayName("getUniversityCodeFromContext: user has no university -> extracts from username 'otm401' -> '401'")
    void getUniversityCodeFromContext_noUniversity_extractsFromUsername() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("username", "otm401")
                .subject("otm401")
                .build();
        JwtAuthenticationToken jwtAuth = new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(jwtAuth);

        User user = new User();
        // No university set — getUniversityCode() returns null
        when(userRepository.findByUsername("otm401")).thenReturn(Optional.of(user));

        String result = legacySecurityHelper.getUniversityCodeFromContext();

        assertThat(result).isEqualTo("401");
    }

    @Test
    @DisplayName("getUniversityCodeFromContext: username is 'admin' (no otm prefix) -> returns null")
    void getUniversityCodeFromContext_invalidUsernameFormat_returnsNull() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin");
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());

        String result = legacySecurityHelper.getUniversityCodeFromContext();

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getUniversityCodeFromContext: username is 'otmABC' (non-numeric after otm) -> returns null")
    void getUniversityCodeFromContext_otmNonNumeric_returnsNull() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("otmABC");
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByUsername("otmABC")).thenReturn(Optional.empty());

        String result = legacySecurityHelper.getUniversityCodeFromContext();

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getUniversityCodeFromContext: userRepository throws exception -> falls back to username extraction")
    void getUniversityCodeFromContext_dbException_fallsBackToUsername() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("username", "otm555")
                .subject("otm555")
                .build();
        JwtAuthenticationToken jwtAuth = new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(jwtAuth);

        when(userRepository.findByUsername("otm555")).thenThrow(new RuntimeException("DB connection failed"));

        String result = legacySecurityHelper.getUniversityCodeFromContext();

        assertThat(result).isEqualTo("555");
        verify(userRepository).findByUsername("otm555");
    }
}
