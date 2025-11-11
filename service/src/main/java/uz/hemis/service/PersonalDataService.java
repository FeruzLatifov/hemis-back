package uz.hemis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uz.hemis.service.base.AbstractGovernmentApiService;

import java.util.Map;

/**
 * Personal Data Service - Government Integration (MVD)
 *
 * <p><strong>CRITICAL - External Service Integration:</strong></p>
 * <ul>
 *   <li>Calls MVD (Ministry of Internal Affairs) API</li>
 *   <li>Validates PINFL and passport data</li>
 *   <li>Used during student registration</li>
 * </ul>
 *
 * <p><strong>OLD-HEMIS Compatibility:</strong></p>
 * <ul>
 *   <li>Same external API endpoint</li>
 *   <li>Same request/response format</li>
 *   <li>Exposed via REST: /app/rest/v2/services/hemishe_PersonalDataService/getData</li>
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
public class PersonalDataService extends AbstractGovernmentApiService {

    @Value("${hemis.external.personal-data.url:https://talaba.edu.uz/api/my_edu_uz/student_mvd_hemis.php}")
    private String externalApiUrl;

    @Value("${hemis.external.personal-data.token:12345}")
    private String externalApiToken;

    /**
     * Get personal data from government service
     *
     * <p><strong>External API:</strong> https://talaba.edu.uz/api/my_edu_uz/student_mvd_hemis.php</p>
     *
     * <p><strong>Request Parameters:</strong></p>
     * <ul>
     *   <li>TOKEN - API token (default: 12345)</li>
     *   <li>pinfl - Personal ID (14 digits)</li>
     *   <li>p_seriya - Passport series (e.g., AA0000000)</li>
     * </ul>
     *
     * <p><strong>Success Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "name_latin": "ISM",
     *   "surname_latin": "FAMILIYA",
     *   "patronym_latin": "OTASINING ISMI",
     *   "birth_date": "1996-01-01",
     *   "sex": "1",
     *   "document": "AA0000000",
     *   "citizenship": "ЎЗБЕКИСТОН",
     *   "nationality": "ҚОРАҚАЛПОҚ",
     *   ...
     * }
     * </pre>
     *
     * <p><strong>Error Response:</strong></p>
     * <pre>
     * {
     *   "success": false,
     *   "code": "incorrect_data",
     *   "message": "Incorrect PINFL or passport number"
     * }
     * </pre>
     *
     * @param pinfl PINFL (14 digits)
     * @param serial Passport series
     * @return personal data map
     */
    public Map<String, Object> getData(String pinfl, String serial) {
        log.info("Fetching personal data - PINFL: {}, Serial: {}", pinfl, serial);

        // Build query parameters
        Map<String, String> params = params();
        addParam(params, "TOKEN", externalApiToken);
        addParam(params, "pinfl", pinfl);
        addParam(params, "p_seriya", serial);

        // Call external API (base class handles SSL, error handling, parsing)
        Map<String, Object> response = callExternalApi(
                externalApiUrl,
                params,
                null, // No flag needed (returns full data or error)
                "PersonalDataService"
        );

        // Check for "incorrect_data" case
        if (Boolean.FALSE.equals(response.get("success")) && "not_found".equals(response.get("code"))) {
            // Override error code for MVD API
            response.put("code", "incorrect_data");
            response.put("message", "Incorrect PINFL or passport number");
        }

        return response;
    }

    /**
     * Get personal data (alternative method for internal use)
     *
     * <p>Same as getData() but with additional code field</p>
     *
     * @param pinfl PINFL
     * @param serial Passport series
     * @return personal data map
     */
    public Map<String, Object> getPersonalData(String pinfl, String serial) {
        log.debug("Internal call: getPersonalData() - PINFL: {}, Serial: {}", pinfl, serial);

        try {
            Map<String, Object> result = getData(pinfl, serial);

            // Add code field for success cases
            if (Boolean.TRUE.equals(result.get("success"))) {
                result.put("code", "success");
            }

            return result;

        } catch (Exception e) {
            log.error("getPersonalData failed - PINFL: {}, Serial: {}", pinfl, serial, e);

            Map<String, Object> error = errorResponse("service_not_available",
                    "Personal data service not available");
            error.put("exception", e.getMessage());
            return error;
        }
    }
}
