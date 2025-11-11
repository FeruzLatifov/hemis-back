package uz.hemis.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uz.hemis.app.service.base.AbstractGovernmentApiService;

import java.util.Map;

/**
 * Passport Data Service - Government Passport API Integration
 *
 * <p><strong>CRITICAL - External Service Integration:</strong></p>
 * <ul>
 *   <li>Calls government Passport API for address and personal data</li>
 *   <li>Validates PINFL, passport serial number, birth date</li>
 *   <li>Requires captcha validation for security</li>
 * </ul>
 *
 * <p><strong>OLD-HEMIS Compatibility:</strong></p>
 * <ul>
 *   <li>4 methods exposed via CUBA REST pattern</li>
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
public class PassportDataService extends AbstractGovernmentApiService {

    @Value("${hemis.external.passport-data.url:https://api.gov.uz/passport}")
    private String externalApiUrl;

    @Value("${hemis.external.passport-data.token:}")
    private String externalApiToken;

    /**
     * Get address data by PINFL
     *
     * <p><strong>Method:</strong> getAddress</p>
     * <p><strong>URL:</strong> /app/rest/v2/services/hemishe_PassportDataService/getAddress?pinfl={pinfl}</p>
     *
     * @param pinfl PINFL (14 digits)
     * @return address data map
     */
    public Map<String, Object> getAddress(String pinfl) {
        log.info("Fetching address data - PINFL: {}", pinfl);

        Map<String, String> params = params();
        addParam(params, "pinfl", pinfl);
        addParam(params, "token", externalApiToken);

        return callExternalApi(
                externalApiUrl + "/address",
                params,
                null,
                "PassportDataService.getAddress"
        );
    }

    /**
     * Get passport data by PINFL and birth date
     *
     * <p><strong>Method:</strong> getDataByPinflBirthdate</p>
     * <p><strong>URL:</strong> /app/rest/v2/services/hemishe_PassportDataService/getDataByPinflBirthdate</p>
     *
     * @param pinfl PINFL
     * @param birthdate Birth date (format: YYYY-MM-DD)
     * @param captchaId Captcha ID
     * @param captchaValue User's captcha answer
     * @return passport data map
     */
    public Map<String, Object> getDataByPinflBirthdate(String pinfl, String birthdate,
                                                        String captchaId, String captchaValue) {
        log.info("Fetching passport data by PINFL and birthdate - PINFL: {}, Birthdate: {}", pinfl, birthdate);

        // Validate captcha
        if (!validateCaptcha(captchaId, captchaValue)) {
            return errorResponse("invalid_captcha", "Invalid captcha value");
        }

        Map<String, String> params = params();
        addParam(params, "pinfl", pinfl);
        addParam(params, "birthdate", birthdate);
        addParam(params, "token", externalApiToken);

        Map<String, Object> response = callExternalApi(
                externalApiUrl + "/data",
                params,
                null,
                "PassportDataService.getDataByPinflBirthdate"
        );

        // Override error message for this method
        if (Boolean.FALSE.equals(response.get("success")) && "not_found".equals(response.get("code"))) {
            response.put("code", "incorrect_data");
            response.put("message", "Incorrect PINFL or birth date");
        }

        return response;
    }

    /**
     * Get passport data by serial number and birth date
     *
     * <p><strong>Method:</strong> getDataBySNBirthdate</p>
     * <p><strong>URL:</strong> /app/rest/v2/services/hemishe_PassportDataService/getDataBySNBirthdate</p>
     *
     * @param seriaNumber Passport seria-number (e.g., AA0000000)
     * @param birthdate Birth date
     * @param captchaId Captcha ID
     * @param captchaValue Captcha answer
     * @return passport data map
     */
    public Map<String, Object> getDataBySNBirthdate(String seriaNumber, String birthdate,
                                                     String captchaId, String captchaValue) {
        log.info("Fetching passport data by serial number and birthdate - SN: {}, Birthdate: {}", seriaNumber, birthdate);

        if (!validateCaptcha(captchaId, captchaValue)) {
            return errorResponse("invalid_captcha", "Invalid captcha value");
        }

        Map<String, String> params = params();
        addParam(params, "serial", seriaNumber);
        addParam(params, "birthdate", birthdate);
        addParam(params, "token", externalApiToken);

        Map<String, Object> response = callExternalApi(
                externalApiUrl + "/data",
                params,
                null,
                "PassportDataService.getDataBySNBirthdate"
        );

        if (Boolean.FALSE.equals(response.get("success")) && "not_found".equals(response.get("code"))) {
            response.put("code", "incorrect_data");
            response.put("message", "Incorrect serial number or birth date");
        }

        return response;
    }

    /**
     * Get passport data by PINFL and serial number
     *
     * <p><strong>Method:</strong> getDataBySN</p>
     * <p><strong>URL:</strong> /app/rest/v2/services/hemishe_PassportDataService/getDataBySN</p>
     *
     * @param pinfl PINFL
     * @param seriaNumber Passport serial number
     * @param captchaId Captcha ID
     * @param captchaValue Captcha answer
     * @return passport data map
     */
    public Map<String, Object> getDataBySN(String pinfl, String seriaNumber,
                                           String captchaId, String captchaValue) {
        log.info("Fetching passport data by PINFL and SN - PINFL: {}, SN: {}", pinfl, seriaNumber);

        if (!validateCaptcha(captchaId, captchaValue)) {
            return errorResponse("invalid_captcha", "Invalid captcha value");
        }

        Map<String, String> params = params();
        addParam(params, "pinfl", pinfl);
        addParam(params, "serial", seriaNumber);
        addParam(params, "token", externalApiToken);

        Map<String, Object> response = callExternalApi(
                externalApiUrl + "/data",
                params,
                null,
                "PassportDataService.getDataBySN"
        );

        if (Boolean.FALSE.equals(response.get("success")) && "not_found".equals(response.get("code"))) {
            response.put("code", "incorrect_data");
            response.put("message", "Incorrect PINFL or serial number");
        }

        return response;
    }
}
