package uz.hemis.service.student;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.hemis.service.base.AbstractGovernmentApiService;
import uz.hemis.service.integration.GuvdTokenService;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Social Service - Government Social Services Integration
 *
 * <p>Old-hemis: SocialServiceBean — har bir method o'z egov.uz endpointiga POST qiladi</p>
 * <p>Auth: GUVD Bearer token (MyTokenService.getToken())</p>
 *
 * @since 1.0.0
 */
@Service
@Slf4j
public class SocialService extends AbstractGovernmentApiService {

    @Autowired
    private GuvdTokenService guvdTokenService;

    /**
     * Yagona ijtimoiy reestr tekshirish
     * OLD-HEMIS: POST https://apimgw.egov.uz:8243/minfin/services/socialprov/v1
     */
    public Object singleRegister(String pinfl) {
        log.info("Checking single register - PINFL: {}", pinfl);
        String body = String.format("{\"pinfl\":\"%s\"}", pinfl);
        return callEgov("https://apimgw.egov.uz:8243/minfin/services/socialprov/v1", body, "SocialService.singleRegister");
    }

    /**
     * To'liq daftar ma'lumotlari
     * OLD-HEMIS: POST https://apimgw.egov.uz:8243/services/daftar/v1/full-info
     */
    public Object daftarFull(String pinfl) {
        log.info("Fetching full daftar info - PINFL: {}", pinfl);
        String body = String.format("{\"pinfl\":\"%s\"}", pinfl);
        return callEgov("https://apimgw.egov.uz:8243/services/daftar/v1/full-info", body, "SocialService.daftarFull");
    }

    /**
     * Qisqacha daftar ma'lumotlari
     * OLD-HEMIS: POST https://apimgw.egov.uz:8243/services/daftar/v1/short-info
     */
    public Object daftarShort(String pinfl) {
        log.info("Fetching short daftar info - PINFL: {}", pinfl);
        String body = String.format("{\"pinfl\":\"%s\"}", pinfl);
        return callEgov("https://apimgw.egov.uz:8243/services/daftar/v1/short-info", body, "SocialService.daftarShort");
    }

    /**
     * Ayollar daftari tekshirish
     * OLD-HEMIS: POST https://apimgw.egov.uz:8243/mahalla/service/women/v1
     */
    public Object women(String pinfl, String sn) {
        log.info("Checking women registry - PINFL: {}, SN: {}", pinfl, sn);
        String body = String.format("{\"pinfl\":\"%s\", \"passport_sn\": \"%s\"}", pinfl, sn != null ? sn : "");
        return callEgov("https://apimgw.egov.uz:8243/mahalla/service/women/v1", body, "SocialService.women");
    }

    /**
     * Yoshlar daftari tekshirish
     * OLD-HEMIS: POST https://apimgw.egov.uz:8243/labour/service/daftar/v1
     */
    public Object young(String pinfl, String seria, String number) {
        log.info("Checking youth registry - PINFL: {}, Seria: {}, Number: {}", pinfl, seria, number);
        String body = String.format("{\"type\":\"young\", \"pinfl\":\"%s\", \"passport_seria\": \"%s\", \"passport_number\":\"%s\"}",
                pinfl, seria != null ? seria : "", number != null ? number : "");
        return callEgov("https://apimgw.egov.uz:8243/labour/service/daftar/v1", body, "SocialService.young");
    }

    /**
     * GUVD token bilan egov.uz API ga POST qilish
     * Old-hemis pattern: MyRequest.post().https(url).header("Authorization", "Bearer " + token).body(body).send()
     * Response: {success: true/false, data: ...}
     */
    private Object callEgov(String url, String body, String serviceName) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            String token = guvdTokenService.getToken();
            if (token == null) {
                result.put("success", false);
                result.put("data", "GUVD token not available");
                return result;
            }

            Object response = proxyExternalApiPost(url, body, token, serviceName);

            // old-hemis wraps response: {success: true, data: responseBody}
            if (response instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseMap = (Map<String, Object>) response;
                // If proxyExternalApiPost returned error (has "success":false), return as-is
                if (Boolean.FALSE.equals(responseMap.get("success"))) {
                    return responseMap;
                }
                result.put("success", true);
                result.put("data", responseMap);
            } else {
                result.put("success", true);
                result.put("data", response);
            }
            return result;

        } catch (Exception e) {
            log.error("{} - Error: {}", serviceName, e.getMessage(), e);
            result.put("success", false);
            result.put("data", e.getMessage());
            return result;
        }
    }
}
