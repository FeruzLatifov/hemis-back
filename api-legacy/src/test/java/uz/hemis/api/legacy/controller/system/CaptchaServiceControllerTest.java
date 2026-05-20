package uz.hemis.api.legacy.controller.system;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uz.hemis.common.dto.system.CaptchaResponse;
import uz.hemis.service.shared.CaptchaService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CaptchaServiceController — CUBA-format captcha endpoints")
class CaptchaServiceControllerTest {

    @Mock private CaptchaService captchaService;

    @InjectMocks
    private CaptchaServiceController controller;

    @Test
    @DisplayName("GET /captcha/getNumericCaptcha — service'ga delegate qiladi")
    void numericCaptcha_delegatesToService() {
        CaptchaResponse expected = new CaptchaResponse();
        expected.setId("uuid-1");
        expected.setImage("data:image/png;base64,iVBORw0KG");
        when(captchaService.generateNumericCaptcha()).thenReturn(expected);

        ResponseEntity<CaptchaResponse> response = controller.getNumericCaptcha();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expected);
        verify(captchaService).generateNumericCaptcha();
        verifyNoMoreInteractions(captchaService);
    }

    @Test
    @DisplayName("GET /captcha/getArithmeticCaptcha — service'ga delegate qiladi")
    void arithmeticCaptcha_delegatesToService() {
        CaptchaResponse expected = new CaptchaResponse();
        expected.setId("uuid-2");
        expected.setImage("data:image/png;base64,iVBORw0KG-arith");
        when(captchaService.generateArithmeticCaptcha()).thenReturn(expected);

        ResponseEntity<CaptchaResponse> response = controller.getArithmeticCaptcha();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo("uuid-2");
        verify(captchaService).generateArithmeticCaptcha();
    }

    @Test
    @DisplayName("Numeric va Arithmetic — alohida service method'larini chaqiradi")
    void numericAndArithmetic_callDifferentMethods() {
        CaptchaResponse num = new CaptchaResponse();
        num.setId("num-id");
        CaptchaResponse arith = new CaptchaResponse();
        arith.setId("arith-id");

        when(captchaService.generateNumericCaptcha()).thenReturn(num);
        when(captchaService.generateArithmeticCaptcha()).thenReturn(arith);

        controller.getNumericCaptcha();
        controller.getArithmeticCaptcha();

        verify(captchaService).generateNumericCaptcha();
        verify(captchaService).generateArithmeticCaptcha();
    }
}
