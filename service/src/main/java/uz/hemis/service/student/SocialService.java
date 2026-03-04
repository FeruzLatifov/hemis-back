package uz.hemis.service.student;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uz.hemis.service.base.AbstractGovernmentApiService;
import uz.hemis.service.integration.ApiMspdTokenService;
import uz.hemis.service.integration.GuvdTokenService;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Social Service - Government Social Services Integration via API-MSPD (172.18.9.171)
 *
 * <p>Old-hemis: SocialServiceBean — har bir method o'z egov.uz endpointiga POST qilardi</p>
 * <p>Yangi: Barcha so'rovlar API-MSPD gateway orqali yuboriladi</p>
 *
 * @since 2.0.0
 */
@Service
@Slf4j
public class SocialService extends AbstractGovernmentApiService {

    @Autowired
    private ApiMspdTokenService apiMspdTokenService;

    @Autowired
    private GuvdTokenService guvdTokenService; // faqat daftarFull/daftarShort uchun (api_mspd da yo'q)

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Yagona ijtimoiy reestr tekshirish
     * API-MSPD: POST /social/social-protection/
     */
    public Object singleRegister(String pinfl) {
        log.info("Checking single register - PINFL: {}", pinfl);
        Map<String, String> body = new HashMap<>();
        body.put("pinfl", pinfl);
        return callApiMspd("/social/social-protection/", body, "SocialService.singleRegister");
    }

    /**
     * To'liq daftar ma'lumotlari
     * API-MSPD da yo'q — egov.uz ga to'g'ridan-to'g'ri
     */
    public Object daftarFull(String pinfl) {
        log.info("Fetching full daftar info - PINFL: {}", pinfl);
        String body = String.format("{\"pinfl\":\"%s\"}", pinfl);
        return callEgovDirect("https://apimgw.egov.uz:8243/services/daftar/v1/full-info", body, "SocialService.daftarFull");
    }

    /**
     * Qisqacha daftar ma'lumotlari
     * API-MSPD da yo'q — egov.uz ga to'g'ridan-to'g'ri
     */
    public Object daftarShort(String pinfl) {
        log.info("Fetching short daftar info - PINFL: {}", pinfl);
        String body = String.format("{\"pinfl\":\"%s\"}", pinfl);
        return callEgovDirect("https://apimgw.egov.uz:8243/services/daftar/v1/short-info", body, "SocialService.daftarShort");
    }

    /**
     * Ayollar daftari tekshirish
     * API-MSPD: POST /women/women-register/
     */
    public Object women(String pinfl, String sn) {
        log.info("Checking women registry - PINFL: {}, SN: {}", pinfl, sn);
        Map<String, String> body = new HashMap<>();
        body.put("pinfl", pinfl);
        body.put("document", sn != null ? sn : "");
        return callApiMspd("/women/women-register/", body, "SocialService.women");
    }

    /**
     * Yoshlar daftari tekshirish
     * API-MSPD: POST /youth/youth-register/
     */
    public Object young(String pinfl, String seria, String number) {
        log.info("Checking youth registry - PINFL: {}, Seria: {}, Number: {}", pinfl, seria, number);
        Map<String, String> body = new HashMap<>();
        body.put("pinfl", pinfl);
        body.put("passport_seria", seria != null ? seria : "");
        body.put("passport_number", number != null ? number : "");
        return callApiMspd("/youth/youth-register/", body, "SocialService.young");
    }

    /**
     * API-MSPD gateway orqali so'rov yuborish
     */
    @SuppressWarnings("unchecked")
    private Object callApiMspd(String path, Map<String, String> body, String serviceName) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            String token = apiMspdTokenService.getAccessToken();
            if (token == null) {
                result.put("success", false);
                result.put("data", "API-MSPD token not available");
                return result;
            }

            String url = apiMspdTokenService.getBaseUrl() + path;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            log.info("Calling API-MSPD: POST {}", url);
            var response = restTemplate.postForEntity(url, entity, Object.class);

            result.put("success", true);
            result.put("data", response.getBody());
            return result;

        } catch (Exception e) {
            log.error("{} - Error: {}", serviceName, e.getMessage(), e);
            result.put("success", false);
            result.put("data", e.getMessage());
            return result;
        }
    }

    /**
     * egov.uz ga to'g'ridan-to'g'ri GUVD token bilan (API-MSPD da yo'q endpointlar uchun)
     * TODO: api_mspd ga daftar routerlar qo'shilganda bu method o'chiriladi
     */
    private Object callEgovDirect(String url, String body, String serviceName) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            String token = guvdTokenService.getToken();
            if (token == null) {
                result.put("success", false);
                result.put("data", "GUVD token not available");
                return result;
            }
            Object response = proxyExternalApiPost(url, body, token, serviceName);

            if (response instanceof Map) {
                Map<String, Object> responseMap = (Map<String, Object>) response;
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
