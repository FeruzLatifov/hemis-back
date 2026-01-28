package uz.hemis.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Student ID Request DTO - OLD-HEMIS Compatible
 *
 * <p>CUBA REST API formatida talaba ID olish uchun so'rov parametrlari</p>
 *
 * <p><strong>Old-hemis endpoint:</strong> POST /app/rest/v2/services/student/id</p>
 *
 * <p><strong>Request format:</strong></p>
 * <pre>
 * {
 *   "data": {
 *     "citizenship": "11",
 *     "pinfl": "31507976020031",
 *     "serial": "AA6970877",
 *     "year": "2024",
 *     "education_type": "11",
 *     "education_form": "11"
 *   }
 * }
 * </pre>
 *
 * @since 1.0.0
 */
@Data
public class StudentIdRequest {

    /**
     * Fuqarolik kodi
     * 11 = O'zbekiston fuqarosi
     * Boshqa kodlar = chet el fuqarosi
     */
    private String citizenship;

    /**
     * PINFL - Jismoniy shaxsning shaxsiy identifikatsiya raqami
     * O'zbekiston fuqarolari uchun majburiy (citizenship = 11)
     */
    private String pinfl;

    /**
     * Passport seria va raqami
     * Barcha talabalar uchun majburiy
     */
    private String serial;

    /**
     * Ta'lim yili kodi
     * Format: YYYY (masalan: 2024)
     */
    private String year;

    /**
     * Ta'lim turi kodi
     * 11 = Bakalavr
     * 12 = Magistratura
     * 13 = Doktorantura
     */
    @JsonProperty("education_type")
    private String educationType;

    /**
     * Ta'lim shakli kodi
     * 11 = Kunduzgi
     * 12 = Sirtqi
     * 13 = Kechki
     */
    @JsonProperty("education_form")
    private String educationForm;

    /**
     * Parametrlarni validatsiya qilish (old-hemis compatible)
     *
     * @return true - parametrlar to'g'ri
     * @throws IllegalArgumentException agar parametrlar noto'g'ri bo'lsa
     */
    public boolean validate() throws IllegalArgumentException {
        if (citizenship == null || citizenship.isEmpty()) {
            throw new IllegalArgumentException("Citizenship value incorrect");
        }
        if (citizenship.equals("11")) {
            if (pinfl == null || pinfl.isEmpty()) {
                throw new IllegalArgumentException("PINFL value incorrect");
            }
        }
        if (serial == null || serial.isEmpty()) {
            throw new IllegalArgumentException("Passport serial value incorrect");
        }
        if (year == null || year.isEmpty()) {
            throw new IllegalArgumentException("Education year value incorrect");
        }
        if (educationType == null || educationType.isEmpty()) {
            throw new IllegalArgumentException("Education type value incorrect");
        }

        return true;
    }
}
