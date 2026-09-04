package uz.hemis.service.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import uz.hemis.common.exception.BadRequestException;
import uz.hemis.common.exception.ConflictException;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.port.cache.CacheEvictionPort;
import uz.hemis.domain.entity.security.Role;
import uz.hemis.domain.entity.security.User;
import uz.hemis.domain.entity.university.University;
import uz.hemis.domain.repository.RoleRepository;
import uz.hemis.domain.repository.UniversityRepository;
import uz.hemis.domain.repository.UserRepository;
import uz.hemis.service.admin.dto.UserCreateRequest;
import uz.hemis.service.admin.dto.UserUpdateRequest;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserAdminService — RBAC scope + CRUD")
class UserAdminServiceTest {

    /** PERSON akkaunt PINFL'i — endi LOGIN EMAS, faqat users.pinfl ustuni + dublikat tekshiruvi. */
    private static final String PINFL = "00000000000000";

    /** PERSON login manbai — ism + familiya; kutilgan slug LOGIN_SLUG. */
    private static final String FIRST_NAME = "Utkir";
    private static final String LAST_NAME = "Xamdamov";
    private static final String LOGIN_SLUG = "utkir_xamdamov";

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UniversityRepository universityRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private CacheEvictionPort cacheEvictionPort;

    /**
     * Qo'lda quriladi (@InjectMocks emas): LoginNameGenerator — HAQIQIY, mock qilingan
     * UserRepository ustida. Shunda createUser testi login slug'ining o'zini tekshiradi,
     * "generator nima qaytarsa shu" degan tavtologiyani emas.
     */
    private UserAdminService service;

    private UUID callerId;
    private UUID targetId;
    private User superAdminCaller;
    private User otmCaller;
    private User ministryCaller;
    private University uni337;

    @BeforeEach
    void setUp() {
        callerId = UUID.randomUUID();
        targetId = UUID.randomUUID();

        uni337 = new University();
        uni337.setCode("337");
        uni337.setName("Andijon DU");

        Role superRole = role("SUPER_ADMIN", false);
        Role otmRole = role("OTM_API", false);
        Role minRole = role("ADMIN", true);

        superAdminCaller = makeUser(callerId, "super", Set.of(superRole), null);
        otmCaller = makeUser(callerId, "otm-admin", Set.of(otmRole), uni337);
        ministryCaller = makeUser(callerId, "min-admin", Set.of(minRole), null);

        service = new UserAdminService(userRepository, roleRepository, universityRepository,
                passwordEncoder, cacheEvictionPort, new LoginNameGenerator(userRepository));
    }

    private Role role(String code, boolean systemRole) {
        Role r = new Role();
        r.setId(UUID.randomUUID());
        r.setCode(code);
        r.setName(code);
        r.setRoleType(systemRole
                ? uz.hemis.common.enums.RoleType.SYSTEM
                : uz.hemis.common.enums.RoleType.CUSTOM);
        r.setActive(true);
        return r;
    }

    private User makeUser(UUID id, String username, Set<Role> roles, University uni) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setEnabled(true);
        u.setAccountNonLocked(true);
        u.setRoles(new HashSet<>(roles));
        if (uni != null) {
            u.setUniversity(uni);
        }
        return u;
    }

    @Test
    @DisplayName("getUserById — caller topilmasa → ResourceNotFoundException")
    void getUserById_callerNotFound() {
        when(userRepository.findByIdWithRolesAndUniversity(callerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserById(targetId, callerId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getUserById — SUPER_ADMIN — istalgan user ko'rinadi")
    void getUserById_superAdmin_seesAny() {
        User target = makeUser(targetId, "stu", Set.of(role("OTM_API", false)), uni337);
        when(userRepository.findByIdWithRolesAndUniversity(callerId)).thenReturn(Optional.of(superAdminCaller));
        when(userRepository.findByIdWithRolesAndUniversity(targetId)).thenReturn(Optional.of(target));

        var resp = service.getUserById(targetId, callerId);

        assertThat(resp).isNotNull();
        assertThat(resp.getUsername()).isEqualTo("stu");
    }

    @Test
    @DisplayName("getUserById — OTM_API boshqa OTM user — AccessDenied")
    void getUserById_otmCrossUniversity_denied() {
        University otherUni = new University();
        otherUni.setCode("401");
        User target = makeUser(targetId, "stu", Set.of(role("OTM_API", false)), otherUni);

        when(userRepository.findByIdWithRolesAndUniversity(callerId)).thenReturn(Optional.of(otmCaller));
        when(userRepository.findByIdWithRolesAndUniversity(targetId)).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> service.getUserById(targetId, callerId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("createUser — UNIVERSITY_LOGIN: username band bo'lsa → Conflict (409)")
    void createUser_duplicateUsername() {
        when(userRepository.findByIdWithRolesAndUniversity(callerId)).thenReturn(Optional.of(superAdminCaller));
        when(userRepository.existsByUsernameIgnoreCase("dup")).thenReturn(true);

        // UNIVERSITY_LOGIN — login qo'lda kiritiladi (PINFL talab qilinmaydi)
        UserCreateRequest req = UserCreateRequest.builder()
                .accountType("UNIVERSITY_LOGIN")
                .username("dup").password("secret123").roleIds(Set.of(UUID.randomUUID())).build();

        // 400 emas 409 — dublikat PINFL bilan bir xil semantika
        assertThatThrownBy(() -> service.createUser(req, callerId))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Username already exists");
    }

    @Test
    @DisplayName("createUser — PERSON happy path: login = ism_familiya slug (PINFL EMAS), BCrypt encode + save")
    void createUser_happy() {
        Role minRole = role("ADMIN", true);
        when(userRepository.findByIdWithRolesAndUniversity(callerId)).thenReturn(Optional.of(superAdminCaller));
        when(userRepository.existsByPinfl(PINFL)).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase(LOGIN_SLUG)).thenReturn(false);
        when(roleRepository.findById(minRole.getId())).thenReturn(Optional.of(minRole));
        when(universityRepository.findById("337")).thenReturn(Optional.of(uni337));
        when(passwordEncoder.encode("secret123")).thenReturn("BCRYPT$xxx");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        // PERSON — accountType default; username YUBORILMAYDI, login ism+familiyadan hosil bo'ladi
        UserCreateRequest req = UserCreateRequest.builder()
                .pinfl(PINFL).password("secret123").confirmPassword("secret123")
                .firstName(FIRST_NAME).lastName(LAST_NAME)
                .universityCode("337").roleIds(Set.of(minRole.getId())).build();

        var resp = service.createUser(req, callerId);

        assertThat(resp.getUsername()).isEqualTo(LOGIN_SLUG);
        verify(passwordEncoder).encode("secret123");

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertThat(savedUser.getValue().getUsername()).isEqualTo(LOGIN_SLUG);
        // PINFL o'z ustunida saqlanadi, lekin login sifatida ISHLATILMAYDI
        assertThat(savedUser.getValue().getPinfl()).isEqualTo(PINFL);
        assertThat(savedUser.getValue().getUsername()).isNotEqualTo(PINFL);
        assertThat(savedUser.getValue().getPassword()).isEqualTo("BCRYPT$xxx");  // xom parol saqlanmaydi
    }

    @Test
    @DisplayName("createUser — PERSON: operator loginni qo'lda bergan → o'sha login ishlatiladi")
    void createUser_personWithOperatorSuppliedLogin() {
        Role minRole = role("ADMIN", true);
        when(userRepository.findByIdWithRolesAndUniversity(callerId)).thenReturn(Optional.of(superAdminCaller));
        when(userRepository.existsByPinfl(PINFL)).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase("u.xamdamov")).thenReturn(false);
        when(roleRepository.findById(minRole.getId())).thenReturn(Optional.of(minRole));
        when(passwordEncoder.encode("secret123")).thenReturn("BCRYPT$xxx");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        // Operator taklif qilingan loginni tahrirlab yuborgan — generator chetlab o'tiladi
        UserCreateRequest req = UserCreateRequest.builder()
                .pinfl(PINFL).username("  u.xamdamov  ").password("secret123")
                .firstName(FIRST_NAME).lastName(LAST_NAME)
                .roleIds(Set.of(minRole.getId())).build();

        var resp = service.createUser(req, callerId);

        assertThat(resp.getUsername()).isEqualTo("u.xamdamov");   // trim qilinadi
        // Generator ishga tushmagan: avtomatik slug hech qachon tekshirilmagan
        verify(userRepository, never()).existsByUsernameIgnoreCase(LOGIN_SLUG);
    }

    @Test
    @DisplayName("createUser — PERSON: operator bergan login band → Conflict (409)")
    void createUser_personLoginTaken_throwsConflict() {
        when(userRepository.findByIdWithRolesAndUniversity(callerId)).thenReturn(Optional.of(superAdminCaller));
        when(userRepository.existsByPinfl(PINFL)).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase("u.xamdamov")).thenReturn(true);

        UserCreateRequest req = UserCreateRequest.builder()
                .pinfl(PINFL).username("u.xamdamov").password("secret123")
                .firstName(FIRST_NAME).lastName(LAST_NAME)
                .roleIds(Set.of(UUID.randomUUID())).build();

        // Jimgina suffiks QO'SHILMAYDI — operator boshqa login tanlashi uchun 409
        assertThatThrownBy(() -> service.createUser(req, callerId))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Username already exists");
    }

    @Test
    @DisplayName("createUser — PERSON: avtomatik login band bo'lsa suffiks qo'shiladi (utkir_xamdamov2)")
    void createUser_generatedLoginCollision_suffixed() {
        Role minRole = role("ADMIN", true);
        when(userRepository.findByIdWithRolesAndUniversity(callerId)).thenReturn(Optional.of(superAdminCaller));
        when(userRepository.existsByPinfl(PINFL)).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase(LOGIN_SLUG)).thenReturn(true);
        when(userRepository.existsByUsernameIgnoreCase(LOGIN_SLUG + "2")).thenReturn(false);
        when(roleRepository.findById(minRole.getId())).thenReturn(Optional.of(minRole));
        when(passwordEncoder.encode("secret123")).thenReturn("BCRYPT$xxx");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        UserCreateRequest req = UserCreateRequest.builder()
                .pinfl(PINFL).password("secret123")
                .firstName(FIRST_NAME).lastName(LAST_NAME)
                .roleIds(Set.of(minRole.getId())).build();

        assertThat(service.createUser(req, callerId).getUsername()).isEqualTo(LOGIN_SLUG + "2");
    }

    @Test
    @DisplayName("createUser — confirmPassword mos kelmasa → BadRequest 'Passwords do not match'")
    void createUser_confirmPasswordMismatch() {
        // Hech narsadan OLDIN tekshiriladi: caller ham yuklanmaydi
        UserCreateRequest req = UserCreateRequest.builder()
                .pinfl(PINFL).password("secret123").confirmPassword("secret124")
                .firstName(FIRST_NAME).lastName(LAST_NAME)
                .roleIds(Set.of(UUID.randomUUID())).build();

        assertThatThrownBy(() -> service.createUser(req, callerId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Passwords do not match");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("toggleStatus — o'z accountni disable qilish TAQIQ")
    void toggleStatus_selfDisable_throws() {
        when(userRepository.findByIdWithRolesAndUniversity(callerId)).thenReturn(Optional.of(superAdminCaller));

        assertThatThrownBy(() -> service.toggleStatus(callerId, callerId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("own account");
    }

    @Test
    @DisplayName("softDelete — o'z accountni o'chirish TAQIQ")
    void softDelete_self_throws() {
        when(userRepository.findByIdWithRolesAndUniversity(callerId)).thenReturn(Optional.of(superAdminCaller));

        assertThatThrownBy(() -> service.softDelete(callerId, callerId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("own account");
    }

    @Test
    @DisplayName("softDelete — happy path: enabled=false + softDelete + cache evict")
    void softDelete_happy() {
        User target = makeUser(targetId, "stu", Set.of(role("OTM_API", false)), uni337);
        when(userRepository.findByIdWithRolesAndUniversity(callerId)).thenReturn(Optional.of(superAdminCaller));
        when(userRepository.findByIdWithRolesAndUniversity(targetId)).thenReturn(Optional.of(target));
        when(userRepository.save(any(User.class))).thenReturn(target);

        service.softDelete(targetId, callerId);

        assertThat(target.getEnabled()).isFalse();
        verify(cacheEvictionPort).evictPermissionsForUser(targetId.toString());
        verify(cacheEvictionPort).evictMenuForUser(targetId.toString());
    }

    @Test
    @DisplayName("unlockAccount — failedAttempts=0 + lockedAt=null + non-locked")
    void unlockAccount_resetsCounters() {
        User target = makeUser(targetId, "stu", Set.of(role("OTM_API", false)), uni337);
        target.setAccountNonLocked(false);
        target.setFailedAttempts(5);
        target.setLockedAt(java.time.LocalDateTime.now());

        when(userRepository.findByIdWithRolesAndUniversity(callerId)).thenReturn(Optional.of(superAdminCaller));
        when(userRepository.findByIdWithRolesAndUniversity(targetId)).thenReturn(Optional.of(target));
        when(userRepository.save(any(User.class))).thenReturn(target);

        service.unlockAccount(targetId, callerId);

        assertThat(target.getAccountNonLocked()).isTrue();
        assertThat(target.getFailedAttempts()).isZero();
        assertThat(target.getLockedAt()).isNull();
    }

    @Test
    @DisplayName("changePassword — encode + saveAndFlush (no logging of password)")
    void changePassword_savesEncoded() {
        User target = makeUser(targetId, "stu", Set.of(role("OTM_API", false)), uni337);
        when(userRepository.findByIdWithRolesAndUniversity(callerId)).thenReturn(Optional.of(superAdminCaller));
        when(userRepository.findByIdWithRolesAndUniversity(targetId)).thenReturn(Optional.of(target));
        when(passwordEncoder.encode("newpass")).thenReturn("BCRYPT$new");

        service.changePassword(targetId, "newpass", callerId);

        assertThat(target.getPassword()).isEqualTo("BCRYPT$new");
        verify(userRepository).saveAndFlush(target);
    }

    @Test
    @DisplayName("validateWriteScope — ADMIN SUPER_ADMIN'ni o'zgartira olmaydi")
    void writeScope_ministryCannotEditSuperAdmin() {
        User target = makeUser(targetId, "super2",
                Set.of(role("SUPER_ADMIN", false)), null);

        when(userRepository.findByIdWithRolesAndUniversity(callerId)).thenReturn(Optional.of(ministryCaller));
        when(userRepository.findByIdWithRolesAndUniversity(targetId)).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> service.toggleStatus(targetId, callerId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("SUPER_ADMIN");
    }

    @Test
    @DisplayName("getActiveRoles — OTM_API system role'larni ko'rmaydi")
    void getActiveRoles_otmFiltersSystemRoles() {
        Role userRole = role("USER", false);
        Role sysRole = role("ADMIN", true);

        when(userRepository.findByIdWithRolesAndUniversity(callerId)).thenReturn(Optional.of(otmCaller));
        when(roleRepository.findAllActive()).thenReturn(java.util.List.of(userRole, sysRole));

        var roles = service.getActiveRoles(callerId);

        assertThat(roles).hasSize(1);
        assertThat(roles.get(0).getCode()).isEqualTo("USER");
    }

    @Test
    @DisplayName("getRolePermissions — role topilmasa → ResourceNotFound")
    void getRolePermissions_notFound() {
        UUID roleId = UUID.randomUUID();
        when(roleRepository.findByIdWithPermissions(roleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRolePermissions(roleId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
