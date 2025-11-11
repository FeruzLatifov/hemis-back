package uz.hemis.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uz.hemis.app.service.base.AbstractGovernmentApiService;

import java.util.Map;

/**
 * BIMM Service - Government Disability and Social Benefits Integration
 *
 * <p><strong>CRITICAL - External Service Integration:</strong></p>
 * <ul>
 *   <li>Calls BIMM (Beneficiary Information Management System) API</li>
 *   <li>Checks disability status, poverty register, academic degrees</li>
 *   <li>Used for student benefits and scholarship eligibility</li>
 * </ul>
 *
 * <p><strong>OLD-HEMIS Compatibility:</strong></p>
 * <ul>
 *   <li>5 methods exposed via CUBA REST pattern</li>
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
public class BimmService extends AbstractGovernmentApiService {

    @Value("${hemis.external.bimm.url:https://api.gov.uz/bimm}")
    private String externalApiUrl;

    @Value("${hemis.external.bimm.token:}")
    private String externalApiToken;

    /**
     * Check disability status
     *
     * @param pinfl PINFL (14 digits)
     * @param document Passport or disability certificate number
     * @return disability data map
     */
    public Map<String, Object> disabilityCheck(String pinfl, String document) {
        log.info("Checking disability status - PINFL: {}, Document: {}", pinfl, document);

        Map<String, String> params = params();
        addParam(params, "pinfl", pinfl);
        addParam(params, "document", document);
        addParam(params, "token", externalApiToken);

        return callExternalApi(
                externalApiUrl + "/disability",
                params,
                "has_disability", // Flag: true if has disability, false if not
                "BimmService.disabilityCheck"
        );
    }

    /**
     * Check poverty register status
     *
     * @param pinfl PINFL
     * @return poverty register data map
     */
    public Map<String, Object> provertyRegister(String pinfl) {
        log.info("Checking poverty register - PINFL: {}", pinfl);

        Map<String, String> params = params();
        addParam(params, "pinfl", pinfl);
        addParam(params, "token", externalApiToken);

        return callExternalApi(
                externalApiUrl + "/poverty",
                params,
                "in_poverty_register",
                "BimmService.provertyRegister"
        );
    }

    /**
     * Get certificate information
     *
     * @param pinfl PINFL
     * @return certificate data map
     */
    public Map<String, Object> certificate(String pinfl) {
        log.info("Fetching certificate info - PINFL: {}", pinfl);

        Map<String, String> params = params();
        addParam(params, "pinfl", pinfl);
        addParam(params, "token", externalApiToken);

        return callExternalApi(
                externalApiUrl + "/certificate",
                params,
                "has_certificate",
                "BimmService.certificate"
        );
    }

    /**
     * Get academic degree information
     *
     * @param pinfl PINFL
     * @return academic degree data map
     */
    public Map<String, Object> academicDegree(String pinfl) {
        log.info("Fetching academic degree info - PINFL: {}", pinfl);

        Map<String, String> params = params();
        addParam(params, "pinfl", pinfl);
        addParam(params, "token", externalApiToken);

        return callExternalApi(
                externalApiUrl + "/academic-degree",
                params,
                "has_degree",
                "BimmService.academicDegree"
        );
    }

    /**
     * Get teacher training information
     *
     * @param pinfl PINFL
     * @return teacher training data map
     */
    public Map<String, Object> teacherTraining(String pinfl) {
        log.info("Fetching teacher training info - PINFL: {}", pinfl);

        Map<String, String> params = params();
        addParam(params, "pinfl", pinfl);
        addParam(params, "token", externalApiToken);

        return callExternalApi(
                externalApiUrl + "/teacher-training",
                params,
                "has_training",
                "BimmService.teacherTraining"
        );
    }
}
