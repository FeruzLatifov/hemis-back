package uz.hemis.service.shared;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uz.hemis.service.base.AbstractGovernmentApiService;
import uz.hemis.service.integration.BimmTokenService;

import java.util.LinkedHashMap;
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

        String token = bimmTokenService.getToken();
        if (token == null) {
            return tokenError("BimmService.disabilityCheck");
        }

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

        String token = bimmTokenService.getToken();
        if (token == null) {
            return tokenError("BimmService.provertyRegister");
        }

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

        String token = bimmTokenService.getToken();
        if (token == null) {
            return tokenError("BimmService.certificate");
        }

        String url = apiBaseUrl + "/dtm/certificate-info?pinfl=" + pinfl;

        return proxyExternalApiGet(url, token, "BimmService.certificate");
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

        String token = bimmTokenService.getToken();
        if (token == null) {
            return tokenError("BimmService.academicDegree");
        }

        Map<String, String> body = new LinkedHashMap<>();
        body.put("pinfl", pinfl);

        return proxyExternalApiPost(
                apiBaseUrl + "/sac/academic-degree-title/",
                body, token, "BimmService.academicDegree"
        );
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

        String token = bimmTokenService.getToken();
        if (token == null) {
            return tokenError("BimmService.teacherTraining");
        }

        Map<String, String> body = new LinkedHashMap<>();
        body.put("pinfl", pinfl);

        return proxyExternalApiPost(
                apiBaseUrl + "/bimm/training-history/",
                body, token, "BimmService.teacherTraining"
        );
    }

    private Map<String, Object> tokenError(String serviceName) {
        log.error("{} - BIMM token not available", serviceName);
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("success", false);
        error.put("code", 401);
        error.put("data", "BIMM token not available");
        return error;
    }
}
