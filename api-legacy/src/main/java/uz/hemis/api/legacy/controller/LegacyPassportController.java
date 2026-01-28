package uz.hemis.api.legacy.controller;

/**
 * DELETED - Legacy Passport Data Controller
 *
 * <p><strong>REASON:</strong> Endpoint /app/rest/v2/services/personal-data/getData is deprecated and removed.</p>
 * <p><strong>DATE:</strong> 2025-11-19</p>
 *
 * <p>Use PassportServiceController instead for passport-related endpoints:</p>
 * <ul>
 *   <li>/app/rest/v2/services/passport-data/getData - Get passport by PINFL</li>
 *   <li>/app/rest/v2/services/passport-data/getDataBySN - Get by PINFL + seria/number</li>
 *   <li>/app/rest/v2/services/passport-data/getDataBySNBirthdate - Get by seria/number + birthdate</li>
 *   <li>/app/rest/v2/services/passport-data/getDataByPinflBirthdate - Get by PINFL + birthdate</li>
 *   <li>/app/rest/v2/services/passport-data/getAddress - Get address by PINFL</li>
 * </ul>
 *
 * @deprecated This class has been removed. Use PassportServiceController instead.
 * @since 1.0.0
 */
@Deprecated(forRemoval = true)
public class LegacyPassportController {
    // This controller has been deleted - 2025-11-19
    // All endpoints moved to PassportServiceController
}
