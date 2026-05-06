package uz.hemis.service.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.hemis.common.exception.BadRequestException;
import uz.hemis.service.integration.model.GatewayResult;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * api-mspd HTTP klienti — barcha tashqi integratsiyalar uchun yagona kirish nuqtasi.
 * Token olish, POST yuborish, status va JSON body bilan {@link GatewayResult} qaytarish.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApiMspdClient {

    private final ApiMspdTokenService tokenService;
    private final ObjectMapper objectMapper;

    /**
     * api-mspd ga POST yuboradi. 2xx ham, 4xx/5xx ham original status va body bilan qaytariladi.
     * Body Jackson orqali JSON ga aylantiriladi — ekranlash avtomatik (injection xavfi yo'q).
     * Token yo'q yoki tarmoq xatosi bo'lsa {@link BadRequestException} tashlanadi.
     */
    public GatewayResult post(String path, Object body) {
        String token = tokenService.getAccessToken();
        if (token == null) {
            log.error("No api-mspd token available — cannot call {}", path);
            throw new BadRequestException("api-mspd token unavailable");
        }
        byte[] payload;
        try {
            payload = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            log.error("api-mspd {} request serialization failed: {}", path, e.getMessage());
            throw new BadRequestException("api-mspd request serialization failed");
        }
        String url = tokenService.getBaseUrl() + path;
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setConnectTimeout(30_000);
            conn.setReadTimeout(30_000);

            try (var os = conn.getOutputStream()) {
                os.write(payload);
            }

            int status = conn.getResponseCode();
            InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
            byte[] bytes = is != null ? is.readAllBytes() : new byte[0];
            JsonNode responseBody = parseBody(bytes);
            return new GatewayResult(status, responseBody);
        } catch (Exception e) {
            log.error("api-mspd {} error: {}", path, e.getMessage());
            throw new BadRequestException("api-mspd call failed: " + e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private JsonNode parseBody(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return objectMapper.nullNode();
        }
        try {
            return objectMapper.readTree(bytes);
        } catch (Exception e) {
            return objectMapper.getNodeFactory().textNode(new String(bytes, StandardCharsets.UTF_8));
        }
    }
}
