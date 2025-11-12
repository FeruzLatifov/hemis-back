package uz.hemis.service.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.hemis.service.PassportDataService;
import uz.hemis.service.PersonalDataService;

import java.util.Map;

/**
 * Passport Integration Service - Facade for all passport-related operations
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Unified interface for passport data retrieval</li>
 *   <li>Delegates to PassportDataService and PersonalDataService</li>
 *   <li>Provides backward compatibility for legacy endpoints</li>
 * </ul>
 *
 * <p><strong>Clean Architecture:</strong></p>
 * <ul>
 *   <li>Controller → Integration Service → Domain Service → External API</li>
 *   <li>Separation of concerns: API contract vs business logic</li>
 *   <li>Easy to mock/test</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PassportIntegrationService {

    private final PassportDataService passportDataService;
    private final PersonalDataService personalDataService;

    /**
     * Get passport data by PINFL
     *
     * @param pinfl Person PINFL
     * @param givenDate Optional passport given date
     * @return Passport data from GUVD
     */
    public Map<String, Object> getPassportDataByPinfl(String pinfl, String givenDate) {
        log.info("Integration: getPassportDataByPinfl - pinfl: {}", pinfl);
        
        // Use PersonalDataService for simple PINFL lookup
        return personalDataService.getData(pinfl, null);
    }

    /**
     * Get passport data by PINFL and serial number
     *
     * @param pinfl Person PINFL
     * @param seriaNumber Passport serial number
     * @param captchaId Captcha session ID
     * @param captchaValue Captcha value
     * @return Passport data
     */
    public Map<String, Object> getPassportDataBySerialNumber(String pinfl, String seriaNumber,
                                                              String captchaId, String captchaValue) {
        log.info("Integration: getPassportDataBySerialNumber - pinfl: {}, seriaNumber: {}", pinfl, seriaNumber);
        return passportDataService.getDataBySN(pinfl, seriaNumber, captchaId, captchaValue);
    }

    /**
     * Get passport data by serial number and birthdate
     *
     * @param seriaNumber Passport serial number
     * @param birthdate Person birthdate
     * @param captchaId Captcha session ID
     * @param captchaValue Captcha value
     * @return Passport data
     */
    public Map<String, Object> getPassportDataBySerialAndBirthdate(String seriaNumber, String birthdate,
                                                                    String captchaId, String captchaValue) {
        log.info("Integration: getPassportDataBySerialAndBirthdate - seriaNumber: {}, birthdate: {}", 
            seriaNumber, birthdate);
        return passportDataService.getDataBySNBirthdate(seriaNumber, birthdate, captchaId, captchaValue);
    }

    /**
     * Get passport data by PINFL and birthdate
     *
     * @param pinfl Person PINFL
     * @param birthdate Person birthdate
     * @param captchaId Captcha session ID
     * @param captchaValue Captcha value
     * @return Passport data
     */
    public Map<String, Object> getPassportDataByPinflAndBirthdate(String pinfl, String birthdate,
                                                                   String captchaId, String captchaValue) {
        log.info("Integration: getPassportDataByPinflAndBirthdate - pinfl: {}, birthdate: {}", pinfl, birthdate);
        return passportDataService.getDataByPinflBirthdate(pinfl, birthdate, captchaId, captchaValue);
    }

    /**
     * Get address by PINFL
     *
     * @param pinfl Person PINFL
     * @return Address data
     */
    public Map<String, Object> getAddress(String pinfl) {
        log.info("Integration: getAddress - pinfl: {}", pinfl);
        return passportDataService.getAddress(pinfl);
    }
}
