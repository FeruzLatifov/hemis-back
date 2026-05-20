package uz.hemis.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.hemis.domain.entity.security.Role;
import uz.hemis.domain.entity.security.User;
import uz.hemis.domain.entity.university.University;
import uz.hemis.domain.repository.UserRepository;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserLegacyService — OLD-HEMIS CUBA user/me + validate response mapping")
class UserLegacyServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks
    private UserLegacyService service;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("admin@337");
        user.setFullName("Admin User");
        user.setEmail("admin@337.uz");
        user.setPhone("+998901234567");
        University uni = new University();
        uni.setCode("337");
        uni.setName("Andijon DU");
        user.setUniversity(uni);
        user.setEnabled(true);
        user.setAccountNonLocked(true);
    }

    @Test
    void findByUsername_delegates() {
        when(userRepository.findByUsername("admin@337")).thenReturn(Optional.of(user));
        assertThat(service.findByUsername("admin@337")).contains(user);
    }

    @Test
    void findById_delegates() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        assertThat(service.findById(user.getId())).contains(user);
    }

    @Test
    void findByIdWithUniversity_delegates() {
        when(userRepository.findByIdWithUniversity(user.getId())).thenReturn(Optional.of(user));
        assertThat(service.findByIdWithUniversity(user.getId())).contains(user);
    }

    @Test
    void findUniversityCodeById_delegates() {
        when(userRepository.findUniversityCodeById(user.getId())).thenReturn(Optional.of("337"));
        assertThat(service.findUniversityCodeById(user.getId())).contains("337");
    }

    @Test
    @DisplayName("toUserMeResponse — CUBA field tartibi (LinkedHashMap order)")
    void toUserMeResponse_orderedFields() {
        Role role = new Role();
        role.setCode("MINISTRY_ADMIN");
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        Map<String, Object> response = service.toUserMeResponse(user);

        assertThat(response).containsKeys("id", "username", "fullName", "email", "phone",
                "roles", "university", "enabled", "accountNonLocked");
        assertThat(response).containsEntry("username", "admin@337");
        assertThat(response).containsEntry("email", "admin@337.uz");
        assertThat(response).containsEntry("university", "337");
        assertThat(response).containsEntry("enabled", true);

        // roles serialized as String array
        String[] rolesArr = (String[]) response.get("roles");
        assertThat(rolesArr).containsExactly("MINISTRY_ADMIN");
    }

    @Test
    void toUserMeResponse_noRoles_emptyArray() {
        user.setRoles(null);

        Map<String, Object> response = service.toUserMeResponse(user);

        String[] rolesArr = (String[]) response.get("roles");
        assertThat(rolesArr).isEmpty();
    }

    @Test
    void toValidateResponse_minimalShape() {
        Map<String, Object> response = service.toValidateResponse(user);

        assertThat(response).containsEntry("valid", true);
        assertThat(response).containsEntry("username", "admin@337");
        assertThat(response.get("userId")).isInstanceOf(String.class);
    }

    @Test
    @DisplayName("getUniversityName — university bor → name qaytariladi")
    void universityName_present() {
        University uni = new University();
        uni.setName("Andijon DU");
        user.setUniversity(uni);

        assertThat(service.getUniversityName(user)).isEqualTo("Andijon DU");
    }

    @Test
    @DisplayName("getUniversityName — university null/empty → ''")
    void universityName_emptyFallback() {
        user.setUniversity(null);
        assertThat(service.getUniversityName(user)).isEmpty();
    }

    @Test
    @DisplayName("buildInstanceName — universityName bor → '{uni} [{login}]'")
    void buildInstanceName_withUniversity() {
        assertThat(service.buildInstanceName(user, "Andijon DU"))
                .isEqualTo("Andijon DU [admin@337]");
    }

    @Test
    @DisplayName("buildInstanceName — universityName bo'sh → '{login} [{login}]'")
    void buildInstanceName_emptyUniversity() {
        assertThat(service.buildInstanceName(user, ""))
                .isEqualTo("admin@337 [admin@337]");

        assertThat(service.buildInstanceName(user, null))
                .isEqualTo("admin@337 [admin@337]");
    }
}
