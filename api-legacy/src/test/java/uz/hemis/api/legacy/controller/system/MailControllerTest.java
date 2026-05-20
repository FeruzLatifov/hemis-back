package uz.hemis.api.legacy.controller.system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uz.hemis.common.dto.ResponseWrapper;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MailController — stub email service (CUBA wrapper)")
class MailControllerTest {

    private MailController controller;

    @BeforeEach
    void setUp() {
        controller = new MailController();
    }

    @Test
    @DisplayName("POST /send — status=sent + messageId qaytaradi")
    void sendEmail_returnsStatusSent() {
        Map<String, String> req = new HashMap<>();
        req.put("to", "user@example.com");
        req.put("subject", "Test");
        req.put("body", "Hello");

        ResponseEntity<ResponseWrapper<Map<String, Object>>> response = controller.sendEmail(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ResponseWrapper<Map<String, Object>> wrapper = response.getBody();
        assertThat(wrapper.getSuccess()).isTrue();

        Map<String, Object> body = wrapper.getData();
        assertThat(body).containsEntry("status", "sent");
        assertThat(body).containsEntry("to", "user@example.com");
        assertThat(body).containsEntry("subject", "Test");
        assertThat(body).containsKey("messageId");
        assertThat(body.get("messageId").toString()).startsWith("msg-");
        assertThat(body).isInstanceOf(LinkedHashMap.class);
    }

    @Test
    @DisplayName("GET /status/{messageId} — delivered status")
    void emailStatus_returnsDelivered() {
        ResponseEntity<ResponseWrapper<Map<String, Object>>> response =
                controller.getEmailStatus("msg-12345");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> data = response.getBody().getData();
        assertThat(data).containsEntry("messageId", "msg-12345");
        assertThat(data).containsEntry("status", "delivered");
        assertThat(data).containsKey("deliveredAt");
    }

    @Test
    @DisplayName("GET /templates — 3 ta shablon")
    void emailTemplates_returnsAll3() {
        ResponseEntity<ResponseWrapper<Map<String, String>>> response =
                controller.getEmailTemplates();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, String> templates = response.getBody().getData();
        assertThat(templates).hasSize(3);
        assertThat(templates).containsKeys("welcome", "verification", "notification");
    }

    @Test
    @DisplayName("Sequential calls — unique messageId (timestamp-based)")
    void sendEmail_uniqueMessageIds() throws InterruptedException {
        Map<String, String> req = new HashMap<>();
        req.put("to", "a@example.com");
        req.put("subject", "x");

        String id1 = (String) controller.sendEmail(req).getBody().getData().get("messageId");
        Thread.sleep(2);
        String id2 = (String) controller.sendEmail(req).getBody().getData().get("messageId");

        assertThat(id1).isNotEqualTo(id2);
    }
}
