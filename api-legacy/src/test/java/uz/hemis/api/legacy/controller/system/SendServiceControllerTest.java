package uz.hemis.api.legacy.controller.system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SendServiceController — SMTP email (old-hemis: always OK)")
class SendServiceControllerTest {

    private SendServiceController controller;

    @BeforeEach
    void setUp() {
        controller = new SendServiceController();
        // SMTP test env yo'q — fake host (unreachable)
        ReflectionTestUtils.setField(controller, "smtpHost", "smtp.invalid.test");
        ReflectionTestUtils.setField(controller, "smtpPort", "587");
        ReflectionTestUtils.setField(controller, "smtpUsername", "no-reply@invalid.test");
        ReflectionTestUtils.setField(controller, "smtpPassword", "");
    }

    @Test
    @DisplayName("GET /sendEmailNative — SMTP unreachable, lekin response={result:OK}")
    void sendEmail_alwaysOk_evenOnFailure() {
        ResponseEntity<?> response = controller.sendEmailNative(
                "999999", "user@example.com", "123456");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("result", "OK");
    }

    @Test
    @DisplayName("GET /sendEmailNative — invalid email format, lekin OK qaytadi (old-hemis compat)")
    void sendEmail_invalidEmail_stillOk() {
        ResponseEntity<?> response = controller.sendEmailNative(
                "999999", "not-an-email", "123456");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("result", "OK");
    }
}
