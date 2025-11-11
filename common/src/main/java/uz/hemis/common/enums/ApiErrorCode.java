package uz.hemis.common.enums;

import lombok.Getter;

/**
 * =====================================================
 * HEMIS API Error Code Dictionary
 * =====================================================
 *
 * Barcha API error code'lar bilan ishlash uchun enum.
 *
 * Arxitektura Best Practice:
 * 1. Error code'lar HTTP status code bilan mos keladi
 * 2. Har bir error code'ning o'z message va description'i bor
 * 3. Frontend developerlarga aniq xatolik ma'lumotlari beradi
 * 4. Logging va monitoring uchun qulay
 *
 * @since 1.0.0
 */
@Getter
public enum ApiErrorCode {

    // =====================================================
    // 400 - Bad Request (Client Errors)
    // =====================================================

    /**
     * Umumiy validatsiya xatosi
     * <p>Misol: Majburiy maydon to'ldirilmagan</p>
     */
    VALIDATION_ERROR(
            "VALIDATION_ERROR",
            "Request validation failed",
            "So'rov validatsiyadan o'tmadi. Majburiy maydonlarni to'ldiring.",
            400
    ),

    /**
     * PINFL formati xato
     * <p>PINFL 14 raqamdan iborat bo'lishi kerak</p>
     */
    INVALID_PINFL_FORMAT(
            "INVALID_PINFL_FORMAT",
            "PINFL must be exactly 14 digits",
            "PINFL 14 raqamdan iborat bo'lishi kerak (masalan: 12345678901234)",
            400
    ),

    /**
     * Email formati xato
     */
    INVALID_EMAIL_FORMAT(
            "INVALID_EMAIL_FORMAT",
            "Invalid email format",
            "Email formati noto'g'ri (masalan: user@example.com)",
            400
    ),

    /**
     * Universitet kodi topilmadi
     */
    INVALID_UNIVERSITY_CODE(
            "INVALID_UNIVERSITY_CODE",
            "Invalid university code",
            "Universitet kodi noto'g'ri yoki topilmadi",
            400
    ),

    /**
     * Sana formati xato
     */
    INVALID_DATE_FORMAT(
            "INVALID_DATE_FORMAT",
            "Invalid date format. Expected: yyyy-MM-dd",
            "Sana formati xato. To'g'ri format: yyyy-MM-dd (masalan: 2024-01-15)",
            400
    ),

    /**
     * JSON formati xato
     */
    INVALID_JSON_FORMAT(
            "INVALID_JSON_FORMAT",
            "Invalid JSON format in request body",
            "So'rovdagi JSON formati xato",
            400
    ),

    /**
     * Parametr qiymati xato
     */
    INVALID_PARAMETER_VALUE(
            "INVALID_PARAMETER_VALUE",
            "Invalid parameter value",
            "Parametr qiymati noto'g'ri",
            400
    ),

    /**
     * Pagination parametr xato
     */
    INVALID_PAGINATION(
            "INVALID_PAGINATION",
            "Invalid pagination parameters. Page must be >= 0, size must be 1-100",
            "Pagination parametrlari xato. page >= 0, size 1-100 oralig'ida bo'lishi kerak",
            400
    ),

    // =====================================================
    // 401 - Unauthorized (Authentication Errors)
    // =====================================================

    /**
     * Avtorizatsiya xatosi - Token mavjud emas
     */
    UNAUTHORIZED(
            "UNAUTHORIZED",
            "Authentication required. Please provide valid JWT token",
            "Avtorizatsiya talab qilinadi. JWT token'ni kiriting",
            401
    ),

    /**
     * Token muddati o'tgan
     */
    TOKEN_EXPIRED(
            "TOKEN_EXPIRED",
            "JWT token has expired. Please refresh your token",
            "Token muddati o'tgan. Yangi token oling",
            401
    ),

    /**
     * Token yaroqsiz
     */
    INVALID_TOKEN(
            "INVALID_TOKEN",
            "Invalid JWT token",
            "Token yaroqsiz yoki buzilgan",
            401
    ),

    /**
     * Login yoki parol xato
     */
    INVALID_CREDENTIALS(
            "INVALID_CREDENTIALS",
            "Invalid username or password",
            "Login yoki parol xato",
            401
    ),

    // =====================================================
    // 403 - Forbidden (Authorization Errors)
    // =====================================================

    /**
     * Ruxsat yo'q
     */
    FORBIDDEN(
            "FORBIDDEN",
            "You don't have permission to access this resource",
            "Sizda bu resursga kirish huquqi yo'q",
            403
    ),

    /**
     * Universitet ma'lumotlariga ruxsat yo'q
     */
    UNIVERSITY_ACCESS_DENIED(
            "UNIVERSITY_ACCESS_DENIED",
            "You don't have permission to access this university's data",
            "Sizda bu universitet ma'lumotlariga kirish huquqi yo'q",
            403
    ),

    /**
     * Admin roli kerak
     */
    ADMIN_ROLE_REQUIRED(
            "ADMIN_ROLE_REQUIRED",
            "This operation requires ADMIN or UNIVERSITY_ADMIN role",
            "Bu amal uchun ADMIN yoki UNIVERSITY_ADMIN roli kerak",
            403
    ),

    // =====================================================
    // 404 - Not Found
    // =====================================================

    /**
     * Resurs topilmadi (umumiy)
     */
    RESOURCE_NOT_FOUND(
            "RESOURCE_NOT_FOUND",
            "Requested resource not found",
            "So'ralgan resurs topilmadi",
            404
    ),

    /**
     * Talaba topilmadi
     */
    STUDENT_NOT_FOUND(
            "STUDENT_NOT_FOUND",
            "Student not found with the given ID",
            "Berilgan ID bilan talaba topilmadi",
            404
    ),

    /**
     * O'qituvchi topilmadi
     */
    TEACHER_NOT_FOUND(
            "TEACHER_NOT_FOUND",
            "Teacher not found with the given ID",
            "Berilgan ID bilan o'qituvchi topilmadi",
            404
    ),

    /**
     * Universitet topilmadi
     */
    UNIVERSITY_NOT_FOUND(
            "UNIVERSITY_NOT_FOUND",
            "University not found with the given code",
            "Berilgan kod bilan universitet topilmadi",
            404
    ),

    /**
     * Diplom topilmadi
     */
    DIPLOMA_NOT_FOUND(
            "DIPLOMA_NOT_FOUND",
            "Diploma not found with the given ID or number",
            "Berilgan ID yoki raqam bilan diplom topilmadi",
            404
    ),

    /**
     * Endpoint topilmadi
     */
    ENDPOINT_NOT_FOUND(
            "ENDPOINT_NOT_FOUND",
            "API endpoint not found. Check your URL and HTTP method",
            "API endpoint topilmadi. URL va HTTP method'ni tekshiring",
            404
    ),

    // =====================================================
    // 405 - Method Not Allowed
    // =====================================================

    /**
     * HTTP method qo'llab-quvvatlanmaydi
     */
    METHOD_NOT_ALLOWED(
            "METHOD_NOT_ALLOWED",
            "HTTP method not allowed for this endpoint",
            "Bu endpoint uchun HTTP method qo'llab-quvvatlanmaydi",
            405
    ),

    /**
     * DELETE method taqiqlangan (NDG - Non-Deletion Guarantee)
     */
    DELETE_NOT_ALLOWED(
            "DELETE_NOT_ALLOWED",
            "DELETE operation is not allowed due to Non-Deletion Guarantee (NDG) policy",
            "DELETE operatsiyasi taqiqlangan (NDG - ma'lumot o'chirilmaydi)",
            405
    ),

    // =====================================================
    // 409 - Conflict
    // =====================================================

    /**
     * Resurs allaqachon mavjud (umumiy)
     */
    DUPLICATE_RESOURCE(
            "DUPLICATE_RESOURCE",
            "Resource with the same unique identifier already exists",
            "Bir xil unique identifier bilan resurs allaqachon mavjud",
            409
    ),

    /**
     * PINFL allaqachon mavjud
     */
    DUPLICATE_PINFL(
            "DUPLICATE_PINFL",
            "Student or Teacher with this PINFL already exists",
            "Bu PINFL bilan talaba yoki o'qituvchi allaqachon mavjud",
            409
    ),

    /**
     * Kod allaqachon mavjud
     */
    DUPLICATE_CODE(
            "DUPLICATE_CODE",
            "Resource with this code already exists",
            "Bu kod bilan resurs allaqachon mavjud",
            409
    ),

    /**
     * Email allaqachon mavjud
     */
    DUPLICATE_EMAIL(
            "DUPLICATE_EMAIL",
            "User with this email already exists",
            "Bu email bilan foydalanuvchi allaqachon mavjud",
            409
    ),

    /**
     * Diplom raqami allaqachon mavjud
     */
    DUPLICATE_DIPLOMA_NUMBER(
            "DUPLICATE_DIPLOMA_NUMBER",
            "Diploma with this number already exists",
            "Bu raqamli diplom allaqachon mavjud",
            409
    ),

    /**
     * Conflict - Resurs o'zgartirilgan
     */
    RESOURCE_MODIFIED(
            "RESOURCE_MODIFIED",
            "Resource has been modified by another user. Please refresh and try again",
            "Resurs boshqa foydalanuvchi tomonidan o'zgartirilgan. Yangilang va qayta urinib ko'ring",
            409
    ),

    // =====================================================
    // 422 - Unprocessable Entity (Business Logic Errors)
    // =====================================================

    /**
     * Business logic xatosi (umumiy)
     */
    BUSINESS_LOGIC_ERROR(
            "BUSINESS_LOGIC_ERROR",
            "Business logic validation failed",
            "Biznes logika validatsiyadan o'tmadi",
            422
    ),

    /**
     * Talaba allaqachon bitiruv olgan
     */
    STUDENT_ALREADY_GRADUATED(
            "STUDENT_ALREADY_GRADUATED",
            "Student has already graduated. Cannot modify",
            "Talaba allaqachon bitiruv olgan. O'zgartirish mumkin emas",
            422
    ),

    /**
     * Talaba o'chirilgan
     */
    STUDENT_DELETED(
            "STUDENT_DELETED",
            "Student has been soft-deleted. Cannot modify",
            "Talaba o'chirilgan. O'zgartirish mumkin emas",
            422
    ),

    /**
     * Diplom allaqachon berilgan
     */
    DIPLOMA_ALREADY_ISSUED(
            "DIPLOMA_ALREADY_ISSUED",
            "Diploma has already been issued. Cannot modify",
            "Diplom allaqachon berilgan. O'zgartirish mumkin emas",
            422
    ),

    /**
     * Yosh talabi bajarilmagan
     */
    INVALID_AGE(
            "INVALID_AGE",
            "Age requirement not met. Minimum age is 16",
            "Yosh talabi bajarilmadi. Minimal yosh: 16",
            422
    ),

    /**
     * GPA qiymati xato
     */
    INVALID_GPA(
            "INVALID_GPA",
            "GPA must be between 0.0 and 4.0",
            "GPA 0.0 va 4.0 orasida bo'lishi kerak",
            422
    ),

    // =====================================================
    // 429 - Too Many Requests (Rate Limiting)
    // =====================================================

    /**
     * Rate limit oshib ketgan
     */
    RATE_LIMIT_EXCEEDED(
            "RATE_LIMIT_EXCEEDED",
            "Too many requests. Please try again later",
            "Juda ko'p so'rovlar yuborildi. Keyinroq urinib ko'ring",
            429
    ),

    // =====================================================
    // 500 - Internal Server Error
    // =====================================================

    /**
     * Serverda ichki xatolik
     */
    INTERNAL_SERVER_ERROR(
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred. Please contact support",
            "Kutilmagan xatolik yuz berdi. Support bilan bog'laning",
            500
    ),

    /**
     * Ma'lumotlar bazasi xatosi
     */
    DATABASE_ERROR(
            "DATABASE_ERROR",
            "Database operation failed. Please try again",
            "Ma'lumotlar bazasi xatosi. Qayta urinib ko'ring",
            500
    ),

    /**
     * External API xatosi
     */
    EXTERNAL_API_ERROR(
            "EXTERNAL_API_ERROR",
            "External API call failed. Please try again later",
            "Tashqi API so'rovi xato. Keyinroq urinib ko'ring",
            500
    ),

    /**
     * File upload xatosi
     */
    FILE_UPLOAD_ERROR(
            "FILE_UPLOAD_ERROR",
            "File upload failed. Please check file size and format",
            "Fayl yuklash xato. Fayl hajmi va formatini tekshiring",
            500
    ),

    // =====================================================
    // 503 - Service Unavailable
    // =====================================================

    /**
     * Servis mavjud emas
     */
    SERVICE_UNAVAILABLE(
            "SERVICE_UNAVAILABLE",
            "Service is temporarily unavailable. Please try again later",
            "Servis vaqtincha mavjud emas. Keyinroq urinib ko'ring",
            503
    ),

    /**
     * Database connection xatosi
     */
    DATABASE_UNAVAILABLE(
            "DATABASE_UNAVAILABLE",
            "Database is temporarily unavailable. Please try again later",
            "Ma'lumotlar bazasi vaqtincha mavjud emas. Keyinroq urinib ko'ring",
            503
    );

    // =====================================================
    // Fields
    // =====================================================

    /**
     * Error code (masalan: VALIDATION_ERROR)
     */
    private final String code;

    /**
     * Inglizcha xabar (API response uchun)
     */
    private final String messageEn;

    /**
     * O'zbekcha xabar (display uchun)
     */
    private final String messageUz;

    /**
     * HTTP status code
     */
    private final int httpStatus;

    // =====================================================
    // Constructor
    // =====================================================

    ApiErrorCode(String code, String messageEn, String messageUz, int httpStatus) {
        this.code = code;
        this.messageEn = messageEn;
        this.messageUz = messageUz;
        this.httpStatus = httpStatus;
    }

    // =====================================================
    // Utility Methods
    // =====================================================

    /**
     * Error code orqali enum topish
     *
     * @param code error code
     * @return ApiErrorCode
     */
    public static ApiErrorCode fromCode(String code) {
        for (ApiErrorCode errorCode : values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        return INTERNAL_SERVER_ERROR; // default
    }

    /**
     * HTTP status code orqali default error code topish
     *
     * @param httpStatus HTTP status code
     * @return ApiErrorCode
     */
    public static ApiErrorCode fromHttpStatus(int httpStatus) {
        for (ApiErrorCode errorCode : values()) {
            if (errorCode.getHttpStatus() == httpStatus) {
                return errorCode; // Birinchi topilgan qaytariladi
            }
        }
        return INTERNAL_SERVER_ERROR; // default
    }

    /**
     * To'liq xabar (English + Uzbek)
     *
     * @return String
     */
    public String getFullMessage() {
        return messageEn + " | " + messageUz;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s: %s", httpStatus, code, messageEn);
    }
}
