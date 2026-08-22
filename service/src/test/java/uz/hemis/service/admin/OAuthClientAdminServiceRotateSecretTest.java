package uz.hemis.service.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.hemis.common.exception.BadRequestException;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.security.OAuthClient;
import uz.hemis.domain.repository.OAuthClientRepository;
import uz.hemis.domain.repository.RoleRepository;
import uz.hemis.domain.repository.UniversityRepository;
import uz.hemis.service.admin.dto.OAuthClientSecretResponse;
import uz.hemis.service.admin.dto.OAuthClientSecretRotateRequest;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OTM API-client maxfiy kalit rotatsiyasi.
 *
 * <p>{@link OAuthClientSecretService} MOCK QILINMAYDI — haqiqiy BCrypt ishlatiladi. Sababi shundaki,
 * bu yerdagi yagona muhim savol "saqlangan hash yangi ochiq maxfiy kalitga haqiqatan mos keladimi" —
 * mock bilan bu savol umuman tekshirilmay qolardi.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OAuthClientAdminService — maxfiy kalit rotatsiyasi")
class OAuthClientAdminServiceRotateSecretTest {

    private static final UUID CLIENT_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String CLIENT_ID = "otm301";
    private static final String OLD_SECRET = "Xm4!vQ8wRt2$Lp";  // siyosatdan o'tadi

    @Mock private OAuthClientRepository repository;
    @Mock private UniversityRepository universityRepository;
    @Mock private RoleRepository roleRepository;

    private final OAuthClientSecretService secretService = new OAuthClientSecretService();
    private OAuthClientAdminService service;
    private OAuthClient client;

    @BeforeEach
    void setUp() {
        service = new OAuthClientAdminService(repository, universityRepository, roleRepository, secretService);

        client = new OAuthClient();
        client.setId(CLIENT_UUID);
        client.setClientId(CLIENT_ID);
        client.setClientSecretHash(secretService.hash(OLD_SECRET));
        client.setSecretVersion(1);
    }

    /** save(...) argumentini qaytaradi — servis saqlangan obyektdan javob quradi. */
    private void stubSaveEcho() {
        when(repository.save(any(OAuthClient.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Nested
    @DisplayName("Markaz generatsiya qiladi (tana bo'sh)")
    class Generated {

        @Test
        @DisplayName("Ochiq maxfiy kalit qaytariladi va saqlangan hashga MOS KELADI")
        void generatesSecretThatVerifies() {
            when(repository.findByIdWithRoles(CLIENT_UUID)).thenReturn(Optional.of(client));
            stubSaveEcho();

            OAuthClientSecretResponse response = service.rotateSecret(CLIENT_UUID, null);

            assertThat(response.plainSecret()).isNotBlank().startsWith("csec_");
            // Asosiy tasdiq: OTM shu qiymat bilan token ola oladi.
            assertThat(secretService.matches(response.plainSecret(), client.getClientSecretHash())).isTrue();
            // Eski maxfiy kalit endi ishlamaydi.
            assertThat(secretService.matches(OLD_SECRET, client.getClientSecretHash())).isFalse();
        }

        @Test
        @DisplayName("secret_version oshadi, secret_rotated_at to'ldiriladi")
        void bumpsVersionAndTimestamp() {
            when(repository.findByIdWithRoles(CLIENT_UUID)).thenReturn(Optional.of(client));
            stubSaveEcho();

            OAuthClientSecretResponse response = service.rotateSecret(CLIENT_UUID, null);

            assertThat(response.secretVersion()).isEqualTo(2);
            assertThat(response.rotatedAt()).isNotNull();
            assertThat(client.getSecretRotatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Bo'sh satr ham 'generatsiya qil' degani (bo'sh maxfiy kalit o'rnatilmaydi)")
        void blankSuppliedSecretIsTreatedAsGenerate() {
            when(repository.findByIdWithRoles(CLIENT_UUID)).thenReturn(Optional.of(client));
            stubSaveEcho();
            OAuthClientSecretRotateRequest request = new OAuthClientSecretRotateRequest();
            request.setClientSecret("   ");

            OAuthClientSecretResponse response = service.rotateSecret(CLIENT_UUID, request);

            assertThat(response.plainSecret()).startsWith("csec_");
            assertThat(secretService.matches("   ", client.getClientSecretHash())).isFalse();
        }

        @Test
        @DisplayName("Har rotatsiya har xil maxfiy kalit beradi")
        void everyRotationDiffers() {
            when(repository.findByIdWithRoles(CLIENT_UUID)).thenReturn(Optional.of(client));
            stubSaveEcho();

            String first = service.rotateSecret(CLIENT_UUID, null).plainSecret();
            String second = service.rotateSecret(CLIENT_UUID, null).plainSecret();

            assertThat(first).isNotEqualTo(second);
        }
    }

    @Nested
    @DisplayName("Admin o'z qiymatini beradi")
    class Supplied {

        @Test
        @DisplayName("Ochiq matn javobda QAYTARILMAYDI, lekin hash yangilanadi")
        void doesNotEchoSuppliedSecret() {
            when(repository.findByIdWithRoles(CLIENT_UUID)).thenReturn(Optional.of(client));
            stubSaveEcho();
            OAuthClientSecretRotateRequest request = new OAuthClientSecretRotateRequest();
            request.setClientSecret("7Kq!zR4$mW9pXv2#");

            OAuthClientSecretResponse response = service.rotateSecret(CLIENT_UUID, request);

            assertThat(response.plainSecret()).isNull();
            assertThat(secretService.matches("7Kq!zR4$mW9pXv2#", client.getClientSecretHash())).isTrue();
        }

        @Test
        @DisplayName("Bosh/oxirgi bo'shliqlar olib tashlanadi (nusxa-joylashtirish tuzog'i)")
        void trimsSuppliedSecret() {
            when(repository.findByIdWithRoles(CLIENT_UUID)).thenReturn(Optional.of(client));
            stubSaveEcho();
            OAuthClientSecretRotateRequest request = new OAuthClientSecretRotateRequest();
            request.setClientSecret("  7Kq!zR4$mW9pXv2#  ");

            service.rotateSecret(CLIENT_UUID, request);

            assertThat(secretService.matches("7Kq!zR4$mW9pXv2#", client.getClientSecretHash())).isTrue();
        }

        @Test
        @DisplayName("Eski maxfiy kalitni 'yangi' deb berish RAD ETILADI — bu rotatsiya emas")
        void rejectsSameSecret() {
            when(repository.findByIdWithRoles(CLIENT_UUID)).thenReturn(Optional.of(client));
            OAuthClientSecretRotateRequest request = new OAuthClientSecretRotateRequest();
            request.setClientSecret(OLD_SECRET);

            assertThatThrownBy(() -> service.rotateSecret(CLIENT_UUID, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("farq qilishi kerak");

            verify(repository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Client topilmasa — 404, hech narsa saqlanmaydi")
    void notFound() {
        when(repository.findByIdWithRoles(CLIENT_UUID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.rotateSecret(CLIENT_UUID, null))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("client_id rotatsiyada O'ZGARMAYDI (OTM konfiguratsiyasi buzilmasin)")
    void clientIdIsNeverChanged() {
        when(repository.findByIdWithRoles(CLIENT_UUID)).thenReturn(Optional.of(client));
        stubSaveEcho();

        OAuthClientSecretResponse response = service.rotateSecret(CLIENT_UUID, null);

        ArgumentCaptor<OAuthClient> captor = ArgumentCaptor.forClass(OAuthClient.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getClientId()).isEqualTo(CLIENT_ID);
        assertThat(response.clientId()).isEqualTo(CLIENT_ID);
    }
}
