package uz.hemis.service.webhook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.hemis.common.dto.webhook.WebhookTargetDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.university.University;
import uz.hemis.domain.entity.webhook.WebhookTarget;
import uz.hemis.domain.repository.UniversityRepository;
import uz.hemis.domain.repository.webhook.WebhookDeliveryLogRepository;
import uz.hemis.domain.repository.webhook.WebhookTargetRepository;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link WebhookTargetService} unit testlar — N+1 fix verify + CRUD basics.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WebhookTargetService")
class WebhookTargetServiceTest {

    @Mock private WebhookTargetRepository targetRepository;
    @Mock private UniversityRepository universityRepository;
    @Mock private WebhookDeliveryLogRepository deliveryLogRepository;
    @Mock private WebhookSecretService secretService;
    @Mock private WebhookSecretVault secretVault;
    @Mock private WebhookDispatcher dispatcher;
    @Mock private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @InjectMocks
    private WebhookTargetService service;

    @BeforeEach
    void setUp() throws Exception {
        // @Value field'lar Mockito InjectMocks tomonidan to'ldirilmaydi — manual set.
        setField(service, "callbackProtocol", "https");
        setField(service, "callbackSuffix", "/rest/v1/hemis-callback/event");
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Nested
    @DisplayName("findAll() — N+1 prevention")
    class FindAll {

        @Test
        @DisplayName("empty repo → empty list, no university lookup")
        void emptyRepo_returnsEmptyAndNoLookup() {
            when(targetRepository.findAll()).thenReturn(List.of());

            List<WebhookTargetDto> result = service.findAll();

            assertThat(result).isEmpty();
            verifyNoInteractions(universityRepository);
        }

        @Test
        @DisplayName("3 targets → 1 IN-clause university query (not 3 separate findById)")
        void threeTargets_singleUniversityLookup() {
            WebhookTarget t1 = makeTarget("337");
            WebhookTarget t2 = makeTarget("401");
            WebhookTarget t3 = makeTarget("501");
            University u337 = makeUni("337", "student.adu.uz", true, false);
            University u401 = makeUni("401", "student.tuit.uz", true, false);
            University u501 = makeUni("501", null, false, false);

            when(targetRepository.findAll()).thenReturn(List.of(t1, t2, t3));
            when(universityRepository.findAllById(any(Iterable.class)))
                    .thenReturn(List.of(u337, u401, u501));

            List<WebhookTargetDto> result = service.findAll();

            assertThat(result).hasSize(3);
            // findAllById bitta marta chaqirilgan (IN-clause), per-row findById emas.
            verify(universityRepository).findAllById(anyIterable());
            verify(universityRepository, org.mockito.Mockito.never()).findById(any());

            // active flag university'dan derive bo'lganligini tekshirish
            WebhookTargetDto dto337 = result.stream().filter(d -> "337".equals(d.universityCode())).findFirst().orElseThrow();
            assertThat(dto337.active()).isTrue();
            assertThat(dto337.callbackUrl()).isEqualTo("https://student.adu.uz/rest/v1/hemis-callback/event");

            WebhookTargetDto dto501 = result.stream().filter(d -> "501".equals(d.universityCode())).findFirst().orElseThrow();
            // university.active=false → active=false, callbackUrl null (student_url null)
            assertThat(dto501.active()).isFalse();
            assertThat(dto501.callbackUrl()).isNull();
        }

        @Test
        @DisplayName("target university not found in DB → active=false, callback null")
        void universityMissing_activeFalse() {
            WebhookTarget t = makeTarget("999");
            when(targetRepository.findAll()).thenReturn(List.of(t));
            when(universityRepository.findAllById(any(Iterable.class))).thenReturn(List.of());

            List<WebhookTargetDto> result = service.findAll();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).active()).isFalse();
            assertThat(result.get(0).callbackUrl()).isNull();
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("found → DTO with derived callback URL")
        void found_returnsDto() {
            UUID id = UUID.randomUUID();
            WebhookTarget t = makeTarget("337");
            t.setId(id);
            University u = makeUni("337", "student.adu.uz", true, false);

            when(targetRepository.findById(id)).thenReturn(Optional.of(t));
            when(universityRepository.findById("337")).thenReturn(Optional.of(u));

            WebhookTargetDto dto = service.findById(id);

            assertThat(dto.universityCode()).isEqualTo("337");
            assertThat(dto.callbackUrl()).contains("student.adu.uz");
            assertThat(dto.active()).isTrue();
        }

        @Test
        @DisplayName("not found → ResourceNotFoundException")
        void notFound_throws() {
            UUID id = UUID.randomUUID();
            when(targetRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(id))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findByUniversityCode()")
    class FindByUniversityCode {

        @Test
        @DisplayName("found → DTO")
        void found_returnsDto() {
            WebhookTarget t = makeTarget("337");
            University u = makeUni("337", "student.adu.uz", true, false);
            when(targetRepository.findByUniversityCode("337")).thenReturn(Optional.of(t));
            when(universityRepository.findById("337")).thenReturn(Optional.of(u));

            WebhookTargetDto dto = service.findByUniversityCode("337");
            assertThat(dto.universityCode()).isEqualTo("337");
        }

        @Test
        @DisplayName("not found → ResourceNotFoundException")
        void notFound_throws() {
            when(targetRepository.findByUniversityCode("999")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.findByUniversityCode("999"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // =====================================================
    // helpers
    // =====================================================

    private static WebhookTarget makeTarget(String universityCode) {
        WebhookTarget t = new WebhookTarget();
        t.setId(UUID.randomUUID());
        t.setUniversityCode(universityCode);
        t.setTimeoutMs(30000);
        t.setMaxRetries(5);
        return t;
    }

    private static University makeUni(String code, String studentUrl, boolean active, boolean deleted) {
        University u = new University();
        u.setCode(code);
        u.setStudentUrl(studentUrl);
        u.setActive(active);
        if (deleted) {
            u.setDeleteTs(java.time.LocalDateTime.now());
        }
        return u;
    }
}
