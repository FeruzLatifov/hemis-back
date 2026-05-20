package uz.hemis.api.legacy.controller.system;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uz.hemis.service.shared.BimmService;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MailServiceController — OLD-HEMIS mail + SMS verify code")
class MailServiceControllerTest {

    @Mock private BimmService bimmService;

    @InjectMocks
    private MailServiceController controller;

    @Test
    @DisplayName("POST /mail/send — request echo with success=true")
    void sendMail_echoesData() {
        Map<String, Object> req = new HashMap<>();
        req.put("id", "999999");
        req.put("resetLink", "https://hemis.uz/reset_url");
        req.put("to", "no-reply@hemis.uz");

        ResponseEntity<Map<String, Object>> response = controller.sendMail(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = response.getBody();
        assertThat(body).containsEntry("success", true);
        assertThat(body).containsEntry("id", "999999");
        assertThat(body).containsEntry("reset_link", "https://hemis.uz/reset_url");
        assertThat(body).containsEntry("to", "no-reply@hemis.uz");
        assertThat(body).isInstanceOf(LinkedHashMap.class);
    }

    @Test
    @DisplayName("POST /verifyCode — email only → email.success=false (SMTP stub)")
    void verifyCode_emailOnly() {
        Map<String, Object> req = new HashMap<>();
        req.put("id", "999999");
        req.put("phone", "");
        req.put("email", "user@example.com");
        req.put("verify_code", "123456");

        ResponseEntity<Map<String, Object>> response = controller.sendVerifyCode(req);

        @SuppressWarnings("unchecked")
        Map<String, Object> emailBlock = (Map<String, Object>) response.getBody().get("email");
        assertThat(emailBlock).containsEntry("success", false);
        assertThat(emailBlock).containsEntry("verify_code", "123456");
        assertThat(emailBlock).containsEntry("email", "user@example.com");
        verify(bimmService, never()).smsUserPay(anyString(), anyString());
    }

    @Test
    @DisplayName("POST /verifyCode — phone berilgan → BIMM SMS chaqirilad")
    void verifyCode_phone_callsBimmSms() {
        when(bimmService.smsUserPay(anyString(), anyString()))
                .thenReturn(Map.of("success", true));

        Map<String, Object> req = new HashMap<>();
        req.put("id", "999999");
        req.put("phone", "+998901234567");
        req.put("email", "");
        req.put("verify_code", "123456");
        req.put("hash", "");

        ResponseEntity<Map<String, Object>> response = controller.sendVerifyCode(req);

        assertThat(response.getBody()).containsKey("sms");
        verify(bimmService).smsUserPay(anyString(), anyString().toString());
    }

    @Test
    @DisplayName("POST /verifyCode — hash uzunligi noto'g'ri → incorrect_hash_data")
    void verifyCode_wrongHashLength() {
        Map<String, Object> req = new HashMap<>();
        req.put("id", "999999");
        req.put("phone", "+998901234567");
        req.put("verify_code", "123456");
        req.put("hash", "wrong");  // hash length != 11

        ResponseEntity<Map<String, Object>> response = controller.sendVerifyCode(req);

        @SuppressWarnings("unchecked")
        Map<String, Object> sms = (Map<String, Object>) response.getBody().get("sms");
        assertThat(sms).containsEntry("success", false);
        assertThat(sms).containsEntry("code", "incorrect_hash_data");
        verify(bimmService, never()).smsUserPay(anyString(), anyString());
    }

    @Test
    @DisplayName("POST /verifyCode — BIMM exception → service_not_available")
    void verifyCode_bimmException() {
        when(bimmService.smsUserPay(anyString(), anyString()))
                .thenThrow(new RuntimeException("Connection refused"));

        Map<String, Object> req = new HashMap<>();
        req.put("id", "999999");
        req.put("phone", "+998901234567");
        req.put("verify_code", "123456");

        ResponseEntity<Map<String, Object>> response = controller.sendVerifyCode(req);

        @SuppressWarnings("unchecked")
        Map<String, Object> sms = (Map<String, Object>) response.getBody().get("sms");
        assertThat(sms).containsEntry("success", false);
        assertThat(sms).containsEntry("code", "service_not_available");
    }

    @Test
    @DisplayName("POST /verifyCode — phone va email — ikkalasi response'da")
    void verifyCode_phoneAndEmail() {
        when(bimmService.smsUserPay(anyString(), anyString()))
                .thenReturn(Map.of("success", true));

        Map<String, Object> req = new HashMap<>();
        req.put("id", "999999");
        req.put("phone", "+998901234567");
        req.put("email", "user@example.com");
        req.put("verify_code", "123456");

        ResponseEntity<Map<String, Object>> response = controller.sendVerifyCode(req);

        assertThat(response.getBody()).containsKeys("sms", "email");
    }
}
