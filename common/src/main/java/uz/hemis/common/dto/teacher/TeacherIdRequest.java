package uz.hemis.common.dto.teacher;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Teacher ID Request DTO - OLD-HEMIS Compatible
 *
 * <p>CUBA REST API formatida o'qituvchi ID olish uchun so'rov parametrlari</p>
 *
 * <p><strong>Old-hemis endpoint:</strong> POST /app/rest/v2/services/teacher/id</p>
 *
 * <p><strong>Request format:</strong></p>
 * <pre>
 * {
 *   "data": {
 *     "citizenship": "11",
 *     "pinfl": "32305967340015",
 *     "serial": "XX1111117",
 *     "year": "2019",
 *     "gender": "11",
 *     "main_university": "380"
 *   }
 * }
 * </pre>
 *
 * @since 1.0.0
 */
@Data
public class TeacherIdRequest {

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
     * Barcha o'qituvchilar uchun majburiy
     */
    private String serial;

    /**
     * Ishga kirgan yil kodi
     * Format: YYYY (masalan: 2019)
     */
    private String year;

    /**
     * Jinsi kodi
     * 11 = Erkak
     * 12 = Ayol
     */
    private String gender;

    /**
     * Asosiy ish joyi universiteti kodi (ixtiyoriy)
     * Agar boshqa universitetda asosiy ish joyi bo'lsa, shu kod beriladi
     */
    @JsonProperty("main_university")
    private String mainUniversity;

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
            throw new IllegalArgumentException("Year value incorrect");
        }
        if (gender == null || gender.isEmpty()) {
            throw new IllegalArgumentException("Gender value incorrect");
        }

        return true;
    }
}
