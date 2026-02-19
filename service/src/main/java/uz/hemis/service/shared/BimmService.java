package uz.hemis.service.shared;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uz.hemis.service.base.AbstractGovernmentApiService;
import uz.hemis.service.integration.BimmTokenService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BIMM Service - Government Disability and Social Benefits Integration
 *
 * <p><strong>CRITICAL - External Service Integration:</strong></p>
 * <ul>
 *   <li>Calls BIMM (api-mspd.edu.uz) API with Bearer token</li>
 *   <li>Checks disability status, poverty register, academic degrees</li>
 *   <li>Used for student benefits and scholarship eligibility</li>
 * </ul>
 *
 * <p><strong>OLD-HEMIS Compatibility:</strong></p>
 * <ul>
 *   <li>5 methods exposed via CUBA REST pattern</li>
 *   <li>Same external API endpoints as old-hemis BimmServiceBean</li>
 *   <li>Raw response proxy — no wrapping (matches old-hemis getBodyAsArray/getBodyAsMap)</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Service
@Slf4j
public class BimmService extends AbstractGovernmentApiService {

    @Autowired
    private BimmTokenService bimmTokenService;

    @Value("${hemis.integration.bimm.api.base-url:https://api-mspd.edu.uz}")
    private String apiBaseUrl;

    /**
     * Get BIMM token — returns token or empty string.
     * Old-hemis sends "Bearer null" when token is null; we send actual request too
     * so that the error response matches old-hemis behavior exactly.
     */
    private String getTokenOrEmpty() {
        String token = bimmTokenService.getToken();
        // Old-hemis: token = myTokenService.getBimmToken() → can be null
        // Then does: "Bearer " + token → "Bearer null"
        // We replicate: send request even with bad token, API returns error, we proxy it
        return token != null ? token : "null";
    }

    /**
     * Check disability status
     *
     * <p>Old-hemis: POST https://api-mspd.edu.uz/disability/disability-pinfl-document</p>
     * <p>Body: {"pinfl": "X", "document": "Y"}</p>
     * <p>Response: getBodyAsArray() — raw proxy</p>
     *
     * @param pinfl PINFL (14 digits)
     * @param document Passport or disability certificate number
     * @return raw API response (array or object)
     */
    public Object disabilityCheck(String pinfl, String document) {
        log.info("Checking disability status - PINFL: {}, Document: {}", pinfl, document);

        String token = getTokenOrEmpty();

        Map<String, String> body = new LinkedHashMap<>();
        body.put("pinfl", pinfl);
        body.put("document", document);

        return proxyExternalApiPost(
                apiBaseUrl + "/disability/disability-pinfl-document/",
                body, token, "BimmService.disabilityCheck"
        );
    }

    /**
     * Check poverty register status
     *
     * <p>Old-hemis: POST https://api-mspd.edu.uz/ihma/reestr-family</p>
     * <p>Body: {"pinfl": "X"}</p>
     * <p>Response: getBodyAsArray() — raw proxy</p>
     *
     * @param pinfl PINFL
     * @return raw API response (array or object)
     */
    public Object provertyRegister(String pinfl) {
        log.info("Checking poverty register - PINFL: {}", pinfl);

        String token = getTokenOrEmpty();

        Map<String, String> body = new LinkedHashMap<>();
        body.put("pinfl", pinfl);

        return proxyExternalApiPost(
                apiBaseUrl + "/ihma/reestr-family/",
                body, token, "BimmService.provertyRegister"
        );
    }

    /**
     * Get certificate information
     *
     * <p>Old-hemis: GET https://api-mspd.edu.uz/dtm/certificate-info?pinfl=X</p>
     * <p>Response: getBodyAsArray() — raw proxy (typically array at root)</p>
     *
     * @param pinfl PINFL
     * @return raw API response (array or object)
     */
    public Object certificate(String pinfl) {
        log.info("Fetching certificate info - PINFL: {}", pinfl);

        String token = getTokenOrEmpty();
        String url = apiBaseUrl + "/dtm/certificate-info?pinfl=" + pinfl;

        return stringifyDataField(proxyExternalApiGet(url, token, "BimmService.certificate"));
    }

    /**
     * Get academic degree information
     *
     * <p>Old-hemis: POST https://api-mspd.edu.uz/sac/academic-degree-title/</p>
     * <p>Body: {"pinfl": "X"}</p>
     * <p>Response: getBodyAsMap() — raw proxy</p>
     *
     * @param pinfl PINFL
     * @return raw API response (object or array)
     */
    public Object academicDegree(String pinfl) {
        log.info("Fetching academic degree info - PINFL: {}", pinfl);

        String token = getTokenOrEmpty();

        Map<String, String> body = new LinkedHashMap<>();
        body.put("pinfl", pinfl);

        return stringifyDataField(proxyExternalApiPost(
                apiBaseUrl + "/sac/academic-degree-title/",
                body, token, "BimmService.academicDegree"
        ));
    }

    /**
     * Get teacher training information
     *
     * <p>Old-hemis: POST https://api-mspd.edu.uz/bimm/training-history/</p>
     * <p>Body: {"pinfl": "X"}</p>
     * <p>Response: getBodyAsMap() — raw proxy</p>
     *
     * @param pinfl PINFL
     * @return raw API response (object or array)
     */
    public Object teacherTraining(String pinfl) {
        log.info("Fetching teacher training info - PINFL: {}", pinfl);

        String token = getTokenOrEmpty();

        Map<String, String> body = new LinkedHashMap<>();
        body.put("pinfl", pinfl);

        return proxyExternalApiPost(
                apiBaseUrl + "/bimm/training-history/",
                body, token, "BimmService.teacherTraining"
        );
    }

    /**
     * Send SMS via BIMM API
     *
     * <p>Old-hemis: POST https://api-mspd.edu.uz/sms/user-pays</p>
     * <p>Body: {"message": "X", "phone_number": "Y"}</p>
     * <p>Response: getBodyAsMap() — raw proxy</p>
     *
     * @param message SMS text
     * @param phone Phone number
     * @return raw API response (object)
     */
    public Object smsUserPay(String message, String phone) {
        log.info("Sending SMS via BIMM - phone: {}", phone);

        String token = getTokenOrEmpty();

        // Old-hemis sends raw JSON string body (not object)
        String body = String.format("{\n    \"message\": \"%s\",\n    \"phone_number\": \"%s\"\n}", message, phone);

        return stringifyDataField(proxyExternalApiPost(
                apiBaseUrl + "/sms/user-pays",
                body, token, "BimmService.smsUserPay"
        ));
    }

    /**
     * Get bank requisites by INN
     *
     * <p>Old-hemis: POST https://api-mspd.edu.uz/legalentity/legalentity-bankrequisites/</p>
     * <p>Body: {"tin": "X"}</p>
     * <p>Response: getBodyAsMap() — raw proxy</p>
     *
     * @param inn INN (TIN)
     * @return raw API response (object)
     */
    public Object bankRequisites(String inn) {
        log.info("Fetching bank requisites - INN: {}", inn);

        String token = getTokenOrEmpty();

        Map<String, String> body = new LinkedHashMap<>();
        body.put("tin", inn);

        return stringifyDataField(proxyExternalApiPost(
                apiBaseUrl + "/legalentity/legalentity-bankrequisites/",
                body, token, "BimmService.bankRequisites"
        ));
    }

    /**
     * Convert "data" field from parsed JSON (List/Map) to JSON string.
     *
     * <p>Old-hemis uses Gson which re-serializes nested data differently.
     * When BIMM API returns {"data": [...], "success": true}, old-hemis
     * serializes the data field as a JSON string. This method replicates that.</p>
     */
    @SuppressWarnings("unchecked")
    private Object stringifyDataField(Object response) {
        if (response instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) response;
            Object data = map.get("data");
            if (data instanceof List || data instanceof Map) {
                try {
                    map.put("data", objectMapper.writeValueAsString(data));
                } catch (Exception e) {
                    log.debug("Failed to stringify data field: {}", e.getMessage());
                }
            }
        }
        return response;
    }
}
