package uz.hemis.service.shared;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.hemis.common.dto.system.CaptchaResponse;
import uz.hemis.common.port.cache.DistributedCachePort;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CaptchaService — numeric + arithmetic + validation")
class CaptchaServiceTest {

    @Mock private DistributedCachePort cachePort;

    @InjectMocks
    private CaptchaService service;

    @Nested
    @DisplayName("generateNumericCaptcha()")
    class Numeric {

        @Test
        @DisplayName("response — id + base64 PNG image, Redis store chaqiriladi")
        void numericCaptcha_responseAndStore() {
            CaptchaResponse response = service.generateNumericCaptcha();

            assertThat(response.getId()).isNotBlank();
            assertThat(response.getImage()).startsWith("data:image/png;base64,");

            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
            verify(cachePort).store(keyCaptor.capture(), valueCaptor.capture(), eq(Duration.ofSeconds(300)));

            assertThat(keyCaptor.getValue()).startsWith("captcha:");
            // 5-digit numeric
            assertThat(valueCaptor.getValue()).matches("\\d{5}");
        }

        @Test
        @DisplayName("ikki marta chaqiriq — har xil id (unique UUID)")
        void multipleCalls_uniqueIds() {
            CaptchaResponse r1 = service.generateNumericCaptcha();
            CaptchaResponse r2 = service.generateNumericCaptcha();

            assertThat(r1.getId()).isNotEqualTo(r2.getId());
        }
    }

    @Nested
    @DisplayName("generateArithmeticCaptcha()")
    class Arithmetic {

        @Test
        @DisplayName("response — javob Redis'da saqlanadi (non-negative natija)")
        void arithmeticCaptcha_nonNegativeAnswer() {
            CaptchaResponse response = service.generateArithmeticCaptcha();

            assertThat(response.getId()).isNotBlank();
            assertThat(response.getImage()).startsWith("data:image/png;base64,");

            ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
            verify(cachePort).store(anyString(), valueCaptor.capture(), any(Duration.class));

            // Answer non-negative integer (service handles subtraction order)
            int answer = Integer.parseInt(valueCaptor.getValue());
            assertThat(answer).isGreaterThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("validateCaptcha()")
    class Validate {

        @Test
        @DisplayName("to'g'ri qiymat — true qaytariladi + cache delete")
        void correctValue_trueAndDelete() {
            when(cachePort.<String>retrieve("captcha:abc")).thenReturn(Optional.of("12345"));

            assertThat(service.validateCaptcha("abc", "12345")).isTrue();

            verify(cachePort).delete("captcha:abc");
        }

        @Test
        @DisplayName("noto'g'ri qiymat — false, cache saqlangan")
        void wrongValue_falseAndNoDelete() {
            when(cachePort.<String>retrieve("captcha:abc")).thenReturn(Optional.of("12345"));

            assertThat(service.validateCaptcha("abc", "99999")).isFalse();

            verify(cachePort, org.mockito.Mockito.never()).delete(anyString());
        }

        @Test
        @DisplayName("Redis topilmagan/expired — false")
        void notInCache_false() {
            when(cachePort.<String>retrieve("captcha:expired")).thenReturn(Optional.empty());

            assertThat(service.validateCaptcha("expired", "12345")).isFalse();
        }

        @Test
        @DisplayName("null parametr — false, Redis ham chaqirilmaydi")
        void nullParams_false() {
            assertThat(service.validateCaptcha(null, "12345")).isFalse();
            assertThat(service.validateCaptcha("abc", null)).isFalse();

            verify(cachePort, org.mockito.Mockito.never()).retrieve(anyString());
        }
    }
}
