package uz.hemis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uz.hemis.service.base.AbstractGovernmentApiService;

import java.util.HashMap;
import java.util.Map;

/**
 * Government Minor API Services - Additional External Integrations
 *
 * <p><strong>CRITICAL - OLD-HEMIS Compatibility:</strong></p>
 * <ul>
 *   <li>Implements minor government API integrations from rest-services.xml</li>
 *   <li>GUVD (Police), Tax services</li>
 *   <li>Used for additional government data verification</li>
 * </ul>
 *
 * <p><strong>OPTIMIZATION:</strong></p>
 * <ul>
 *   <li>Extends AbstractGovernmentApiService</li>
 *   <li>Zero code duplication - base class handles SSL, HTTP, JSON</li>
 *   <li>Each method = 5-10 lines of clean business logic</li>
 * </ul>
 *
 * <p><strong>Services (2 services, 3 methods):</strong></p>
 * <ul>
 *   <li>GUVD Service - 2 methods (classifiers, objects)</li>
 *   <li>Tax Service - 1 method (rent payment check)</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Service
@Slf4j
public class GovernmentMinorApiService extends AbstractGovernmentApiService {

    @Value("${external.api.guvd.url:https://api.guvd.gov.uz}")
    private String guvdApiUrl;

    @Value("${external.api.guvd.token:}")
    private String guvdApiToken;

    @Value("${external.api.tax.url:https://api.soliq.uz}")
    private String taxApiUrl;

    @Value("${external.api.tax.token:}")
    private String taxApiToken;

    // =====================================================
    // GUVD SERVICE (2 methods) - Police Integration
    // =====================================================
    // GUVD = Ichki Ishlar Vazirligi (Ministry of Internal Affairs)

    /**
     * Get GUVD classifiers
     *
     * <p><strong>Method:</strong> classifiers</p>
     * <p><strong>URL:</strong> /app/rest/v2/services/hemishe_GuvdService/classifiers</p>
     *
     * <p>Returns police/security-related classifiers (crime types, document types, etc.)</p>
     *
     * @return Classifiers data from GUVD
     */
    public Map<String, Object> guvdClassifiers() {
        log.info("Getting GUVD classifiers");

        Map<String, String> params = params();
        addParam(params, "token", guvdApiToken);

        return callExternalApi(
                guvdApiUrl + "/classifiers",
                params,
                null,
                "GuvdService.classifiers"
        );
    }

    /**
     * Get GUVD objects by criteria
     *
     * <p><strong>Method:</strong> objects</p>
     * <p><strong>URL:</strong> /app/rest/v2/services/hemishe_GuvdService/objects?type={type}&query={query}</p>
     *
     * <p>Query police database for objects (people, documents, etc.)</p>
     *
     * @param type Object type (e.g., "person", "document", "vehicle")
     * @param query Search query
     * @return Objects data from GUVD
     */
    public Map<String, Object> guvdObjects(String type, String query) {
        log.info("Getting GUVD objects - Type: {}, Query: {}", type, query);

        if (isEmpty(type)) {
            return errorResponse("invalid_parameter", "Object type required");
        }

        if (isEmpty(query)) {
            return errorResponse("invalid_parameter", "Search query required");
        }

        Map<String, String> params = params();
        addParam(params, "token", guvdApiToken);
        addParam(params, "type", type);
        addParam(params, "query", query);

        return callExternalApi(
                guvdApiUrl + "/objects",
                params,
                null,
                "GuvdService.objects"
        );
    }

    // =====================================================
    // TAX SERVICE (1 method) - Tax Payment Verification
    // =====================================================

    /**
     * Check rent payment status
     *
     * <p><strong>Method:</strong> rent</p>
     * <p><strong>URL:</strong> /app/rest/v2/services/hemishe_TaxService/rent?pinfl={pinfl}&period={period}</p>
     *
     * <p>Checks if person has paid rent tax for specified period</p>
     *
     * @param pinfl PINFL (14 digits)
     * @param period Period (e.g., "2024-Q1", "2024-01")
     * @return Rent payment status
     */
    public Map<String, Object> taxRentCheck(String pinfl, String period) {
        log.info("Checking rent tax payment - PINFL: {}, Period: {}", pinfl, period);

        if (isEmpty(pinfl)) {
            return errorResponse("invalid_parameter", "PINFL required");
        }

        if (isEmpty(period)) {
            return errorResponse("invalid_parameter", "Period required");
        }

        Map<String, String> params = params();
        addParam(params, "token", taxApiToken);
        addParam(params, "pinfl", pinfl);
        addParam(params, "period", period);

        return callExternalApi(
                taxApiUrl + "/rent/check",
                params,
                "is_paid",
                "TaxService.rentCheck"
        );
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * Check if string is empty
     *
     * @param str string to check
     * @return true if null or empty
     */
    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
