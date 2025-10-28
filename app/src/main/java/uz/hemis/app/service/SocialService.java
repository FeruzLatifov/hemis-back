package uz.hemis.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uz.hemis.app.service.base.AbstractGovernmentApiService;

import java.util.Map;

/**
 * Social Service - Government Social Services Integration
 *
 * <p><strong>CRITICAL - External Service Integration:</strong></p>
 * <ul>
 *   <li>Calls government Social Services API</li>
 *   <li>Checks single register, daftar (registry), women's registry, youth registry, VTEK</li>
 *   <li>Used for student social benefits and support programs</li>
 * </ul>
 *
 * <p><strong>OLD-HEMIS Compatibility:</strong></p>
 * <ul>
 *   <li>6 methods exposed via CUBA REST pattern</li>
 *   <li>Same external API endpoints</li>
 *   <li>Same request/response format</li>
 * </ul>
 *
 * <p><strong>OPTIMIZATION:</strong></p>
 * <ul>
 *   <li>Extends AbstractGovernmentApiService</li>
 *   <li>No code duplication (SSL, error handling, etc. in base class)</li>
 *   <li>Clean, focused business logic only</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Service
@Slf4j
public class SocialService extends AbstractGovernmentApiService {

    @Value("${hemis.external.social.url:https://api.gov.uz/social}")
    private String externalApiUrl;

    @Value("${hemis.external.social.token:}")
    private String externalApiToken;

    /**
     * Check single register status
     *
     * @param pinfl PINFL (14 digits)
     * @return single register data map
     */
    public Map<String, Object> singleRegister(String pinfl) {
        log.info("Checking single register - PINFL: {}", pinfl);

        Map<String, String> params = params();
        addParam(params, "pinfl", pinfl);
        addParam(params, "token", externalApiToken);

        return callExternalApi(
                externalApiUrl + "/single-register",
                params,
                null, // Returns full data or error
                "SocialService.singleRegister"
        );
    }

    /**
     * Get full daftar (registry) information
     *
     * @param pinfl PINFL
     * @return daftar data map (full)
     */
    public Map<String, Object> daftarFull(String pinfl) {
        log.info("Fetching full daftar info - PINFL: {}", pinfl);

        Map<String, String> params = params();
        addParam(params, "pinfl", pinfl);
        addParam(params, "token", externalApiToken);

        return callExternalApi(
                externalApiUrl + "/daftar/full",
                params,
                null,
                "SocialService.daftarFull"
        );
    }

    /**
     * Get short daftar (registry) information
     *
     * @param pinfl PINFL
     * @return daftar data map (short)
     */
    public Map<String, Object> daftarShort(String pinfl) {
        log.info("Fetching short daftar info - PINFL: {}", pinfl);

        Map<String, String> params = params();
        addParam(params, "pinfl", pinfl);
        addParam(params, "token", externalApiToken);

        return callExternalApi(
                externalApiUrl + "/daftar/short",
                params,
                null,
                "SocialService.daftarShort"
        );
    }

    /**
     * Check women's registry (Ayollar daftari)
     *
     * @param pinfl PINFL
     * @param sn Passport serial number
     * @return women registry data map
     */
    public Map<String, Object> women(String pinfl, String sn) {
        log.info("Checking women registry - PINFL: {}, SN: {}", pinfl, sn);

        Map<String, String> params = params();
        addParam(params, "pinfl", pinfl);
        addParam(params, "sn", sn);
        addParam(params, "token", externalApiToken);

        return callExternalApi(
                externalApiUrl + "/women",
                params,
                "in_women_registry",
                "SocialService.women"
        );
    }

    /**
     * Check youth registry (Yoshlar daftari)
     *
     * @param pinfl PINFL
     * @param seria Passport series (e.g., AA)
     * @param number Passport number (e.g., 0000000)
     * @return youth registry data map
     */
    public Map<String, Object> young(String pinfl, String seria, String number) {
        log.info("Checking youth registry - PINFL: {}, Seria: {}, Number: {}", pinfl, seria, number);

        Map<String, String> params = params();
        addParam(params, "pinfl", pinfl);
        addParam(params, "seria", seria);
        addParam(params, "number", number);
        addParam(params, "token", externalApiToken);

        return callExternalApi(
                externalApiUrl + "/young",
                params,
                "in_youth_registry",
                "SocialService.young"
        );
    }

    /**
     * Check VTEK (Medical-Social Expert Commission) status
     *
     * @param pinfl PINFL
     * @param birthDate Birth date (format: YYYY-MM-DD)
     * @param birthDocument Birth certificate or passport number
     * @return VTEK data map
     */
    public Map<String, Object> vtek(String pinfl, String birthDate, String birthDocument) {
        log.info("Checking VTEK status - PINFL: {}, BirthDate: {}", pinfl, birthDate);

        Map<String, String> params = params();
        addParam(params, "pinfl", pinfl);
        addParam(params, "birth_date", birthDate);
        addParam(params, "birth_document", birthDocument);
        addParam(params, "token", externalApiToken);

        return callExternalApi(
                externalApiUrl + "/vtek",
                params,
                "has_vtek_disability",
                "SocialService.vtek"
        );
    }
}
