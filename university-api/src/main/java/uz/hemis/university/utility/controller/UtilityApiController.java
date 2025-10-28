package uz.hemis.university.utility.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.university.service.UtilityCubaService;

import java.util.List;
import java.util.Map;

/**
 * Utility API Controller
 *
 * <p><strong>Feature:</strong> System Utility Services</p>
 * <p><strong>OLD-HEMIS Equivalent:</strong></p>
 * <ul>
 *   <li>TranslateServiceBean.java - Multi-language translations</li>
 *   <li>MailServiceBean.java - Email notifications</li>
 *   <li>SendServiceBean.java - SMS/Email verification</li>
 *   <li>CaptchaServiceBean.java - Security captcha generation</li>
 * </ul>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Translation management (Uzbek, Russian, English)</li>
 *   <li>Email sending (password reset, notifications)</li>
 *   <li>Verification code delivery (SMS, Email)</li>
 *   <li>Captcha generation and validation</li>
 * </ul>
 *
 * <p><strong>CUBA Pattern (Backward Compatible):</strong></p>
 * <pre>
 * GET /app/rest/v2/services/hemishe_TranslateService/get
 * GET /app/rest/v2/services/hemishe_MailService/send
 * GET /app/rest/v2/services/hemishe_CaptchaService/getNumericCaptcha
 * </pre>
 *
 * <p><strong>Endpoints:</strong> 7 utility endpoints</p>
 * <p><strong>Users:</strong> All system users (universities, students, staff)</p>
 *
 * @since 1.0.0
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class UtilityApiController {

    private final UtilityCubaService utilityCubaService;

    // =====================================================
    // TRANSLATE SERVICE (2 endpoints)
    // =====================================================

    /**
     * Get all translations
     *
     * <p><strong>OLD-HEMIS URL:</strong></p>
     * <pre>
     * GET /app/rest/v2/services/hemishe_TranslateService/get
     * </pre>
     *
     * <p><strong>Use Case:</strong> Frontend loads all translations for UI</p>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "translations": {
     *     "common.save": {
     *       "uz": "Saqlash",
     *       "ru": "Сохранить",
     *       "en": "Save"
     *     },
     *     "common.cancel": {
     *       "uz": "Bekor qilish",
     *       "ru": "Отменить",
     *       "en": "Cancel"
     *     },
     *     ...
     *   },
     *   "total": 1250
     * }
     * </pre>
     *
     * @return all system translations in all languages
     */
    @GetMapping("/app/rest/v2/services/hemishe_TranslateService/get")
    public ResponseEntity<Map<String, Object>> translateGet() {
        log.info("🌐 Translate get - Fetching all translations");
        Map<String, Object> result = utilityCubaService.translateGet();
        log.info("✅ Translate get completed - Total: {}", result.get("total"));
        return ResponseEntity.ok(result);
    }

    /**
     * Get translations by category and message keys
     *
     * <p><strong>Use Case:</strong> Frontend loads specific category translations</p>
     *
     * <p><strong>Request Body Example:</strong></p>
     * <pre>
     * {
     *   "category": "student",
     *   "messages": ["student.list", "student.create", "student.edit"]
     * }
     * </pre>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "category": "student",
     *   "translations": {
     *     "student.list": {
     *       "uz": "Talabalar ro'yxati",
     *       "ru": "Список студентов",
     *       "en": "Student List"
     *     },
     *     ...
     *   }
     * }
     * </pre>
     *
     * @param request Category and message keys
     * @return filtered translations
     */
    @PostMapping("/app/rest/v2/services/hemishe_TranslateService/get")
    public ResponseEntity<Map<String, Object>> translateGetByCategory(@RequestBody Map<String, Object> request) {
        String category = (String) request.get("category");
        @SuppressWarnings("unchecked")
        List<String> messages = (List<String>) request.get("messages");

        log.info("🌐 Translate get by category - Category: {}, Messages: {}",
                category, messages != null ? messages.size() : 0);

        Map<String, Object> result = utilityCubaService.translateGetByCategory(category, messages);
        log.info("✅ Translate get by category completed");
        return ResponseEntity.ok(result);
    }

    // =====================================================
    // MAIL SERVICE (1 endpoint)
    // =====================================================

    /**
     * Send password reset email
     *
     * <p><strong>OLD-HEMIS URL:</strong></p>
     * <pre>
     * GET /app/rest/v2/services/hemishe_MailService/send?id={id}&resetLink={link}&to={email}
     * </pre>
     *
     * <p><strong>Use Case:</strong> User requests password reset</p>
     *
     * <p><strong>Example:</strong></p>
     * <pre>
     * curl 'https://ministry.hemis.uz/app/rest/v2/services/hemishe_MailService/send?id=uuid-123&resetLink=https://hemis.uz/reset/token&to=user@example.com' \
     *   -H 'Authorization: Bearer {token}'
     * </pre>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "message": "Email sent successfully",
     *   "to": "user@example.com",
     *   "sent_at": "2024-10-27T10:30:00Z"
     * }
     * </pre>
     *
     * @param id User ID
     * @param resetLink Password reset link
     * @param to Recipient email address
     * @return email sending result
     */
    @GetMapping("/app/rest/v2/services/hemishe_MailService/send")
    public ResponseEntity<Map<String, Object>> mailSend(
            @RequestParam("id") String id,
            @RequestParam("resetLink") String resetLink,
            @RequestParam("to") String to) {
        log.info("📧 Mail send - To: {}", to);
        Map<String, Object> result = utilityCubaService.mailSend(id, resetLink, to);
        log.info("✅ Mail send completed - Success: {}", result.get("success"));
        return ResponseEntity.ok(result);
    }

    // =====================================================
    // SEND SERVICE (2 endpoints)
    // =====================================================

    /**
     * Send verification code via SMS or Email
     *
     * <p><strong>OLD-HEMIS URL:</strong></p>
     * <pre>
     * GET /app/rest/v2/services/hemishe_SendService/verifyCode?id={id}&phone={phone}&verify_code={code}
     * GET /app/rest/v2/services/hemishe_SendService/verifyCode?id={id}&email={email}&verify_code={code}&hash={hash}
     * </pre>
     *
     * <p><strong>Use Case:</strong> Send OTP for 2FA, registration, password reset</p>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "message": "Verification code sent",
     *   "method": "sms",
     *   "sent_to": "+998901234567",
     *   "expires_in": 300
     * }
     * </pre>
     *
     * @param id User/session ID
     * @param phone Phone number (optional)
     * @param email Email address (optional)
     * @param verifyCode 6-digit verification code
     * @param hash Optional security hash
     * @return sending result
     */
    @GetMapping("/app/rest/v2/services/hemishe_SendService/verifyCode")
    public ResponseEntity<Map<String, Object>> sendVerifyCode(
            @RequestParam("id") String id,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam("verify_code") String verifyCode,
            @RequestParam(value = "hash", required = false) String hash) {

        log.info("📲 Send verifyCode - ID: {}, Phone: {}, Email: {}", id, phone != null, email != null);

        Map<String, Object> result;
        if (hash != null && !hash.isEmpty()) {
            result = utilityCubaService.sendVerifyCodeWithHash(id, phone, email, verifyCode, hash);
        } else {
            result = utilityCubaService.sendVerifyCode(id, phone, email, verifyCode);
        }

        log.info("✅ Send verifyCode completed - Success: {}", result.get("success"));
        return ResponseEntity.ok(result);
    }

    /**
     * Send email verification (native method)
     *
     * <p><strong>Use Case:</strong> Email-only verification (no SMS)</p>
     *
     * @param id User/session ID
     * @param email Email address
     * @param verifyCode 6-digit verification code
     * @return sending result
     */
    @GetMapping("/app/rest/v2/services/hemishe_SendService/sendEmailNative")
    public ResponseEntity<Map<String, Object>> sendEmailNative(
            @RequestParam("id") String id,
            @RequestParam("email") String email,
            @RequestParam("verify_code") String verifyCode) {

        log.info("📧 Send emailNative - Email: {}", email);
        Map<String, Object> result = utilityCubaService.sendEmailNative(id, email, verifyCode);
        log.info("✅ Send emailNative completed - Success: {}", result.get("success"));
        return ResponseEntity.ok(result);
    }

    // =====================================================
    // CAPTCHA SERVICE (2 endpoints)
    // =====================================================

    /**
     * Get numeric captcha
     *
     * <p><strong>OLD-HEMIS URL:</strong></p>
     * <pre>
     * GET /app/rest/v2/services/hemishe_CaptchaService/getNumericCaptcha
     * </pre>
     *
     * <p><strong>Use Case:</strong> Generate captcha for form validation</p>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "captcha_id": "uuid-abc123",
     *   "captcha_image": "data:image/png;base64,iVBORw0KGgo...",
     *   "expires_in": 300
     * }
     * </pre>
     *
     * @return captcha image and ID
     */
    @GetMapping("/app/rest/v2/services/hemishe_CaptchaService/getNumericCaptcha")
    public ResponseEntity<Map<String, Object>> captchaGetNumeric() {
        log.debug("🔐 Captcha getNumericCaptcha");
        Map<String, Object> result = utilityCubaService.captchaGetNumeric();
        log.debug("✅ Captcha getNumericCaptcha completed - ID: {}", result.get("captcha_id"));
        return ResponseEntity.ok(result);
    }

    /**
     * Get arithmetic captcha
     *
     * <p><strong>Use Case:</strong> Generate math problem captcha (e.g., "5 + 3 = ?")</p>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "captcha_id": "uuid-def456",
     *   "question": "5 + 3 = ?",
     *   "captcha_image": "data:image/png;base64,iVBORw0KGgo...",
     *   "expires_in": 300
     * }
     * </pre>
     *
     * @return arithmetic captcha with question
     */
    @GetMapping("/app/rest/v2/services/hemishe_CaptchaService/getArithmeticCaptcha")
    public ResponseEntity<Map<String, Object>> captchaGetArithmetic() {
        log.debug("🔐 Captcha getArithmeticCaptcha");
        Map<String, Object> result = utilityCubaService.captchaGetArithmetic();
        log.debug("✅ Captcha getArithmeticCaptcha completed - ID: {}", result.get("captcha_id"));
        return ResponseEntity.ok(result);
    }

    // =====================================================
    // ✅ 7 UTILITY ENDPOINTS IMPLEMENTED
    // =====================================================
    // Following OLD-HEMIS pattern: Grouped utility services
    // Equivalent to: TranslateServiceBean, MailServiceBean,
    //                SendServiceBean, CaptchaServiceBean
    // =====================================================
}
