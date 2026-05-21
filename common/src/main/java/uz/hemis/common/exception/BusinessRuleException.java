package uz.hemis.common.exception;

/**
 * Business Rule Exception — biznes qoidasi buzilgan (HTTP 422 UNPROCESSABLE ENTITY).
 *
 * <p><strong>Tafovut:</strong></p>
 * <ul>
 *   <li>{@link ValidationException} (400) — input format/syntax xato
 *       (PINFL 14 raqam emas, email format buzilgan)</li>
 *   <li>{@link ConflictException} (409) — mavjud yozuv bilan to'qnashuv
 *       (duplicate PINFL, OTM code mavjud)</li>
 *   <li>{@code BusinessRuleException} (422) — input sintaktik to'g'ri, lekin
 *       biznes qoidasi bo'yicha amalga oshirib bo'lmaydi
 *       (CLOSED OTM'ga talaba kiritish, semestr yopilgandan keyin baho o'zgartirish,
 *        registratsiya muddati o'tgan, OTM bloklangan)</li>
 * </ul>
 *
 * <p>Loyihaning 3-asosiy maqsadi (vazirlik qoidalarini joriy qilish) uchun
 * markaziy exception turi. Kelajakda <code>StudentInsertionPolicy</code>,
 * <code>GradeEditPolicy</code>, <code>EnrollmentWindowGuard</code> kabi
 * policy klasslar shu exception'ni ko'taradi.</p>
 *
 * <p><strong>Misol:</strong></p>
 * <pre>
 * if (university.getLifecycleStatus() == LifecycleStatus.CLOSED) {
 *     throw new BusinessRuleException(
 *         "OTM_CLOSED",
 *         "Yopilgan OTM'ga yangi talaba kiritib bo'lmaydi: " + universityCode
 *     );
 * }
 * </pre>
 *
 * @since ADR-0013 (Rules engine foundation)
 */
public class BusinessRuleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Machine-readable rule code (e.g. {@code OTM_CLOSED}, {@code GRADE_FINALIZED}, {@code ENROLLMENT_WINDOW_EXPIRED}). */
    private final String ruleCode;

    public BusinessRuleException(String ruleCode, String message) {
        super(message);
        this.ruleCode = ruleCode;
    }

    public BusinessRuleException(String ruleCode, String message, Throwable cause) {
        super(message, cause);
        this.ruleCode = ruleCode;
    }

    public String getRuleCode() {
        return ruleCode;
    }
}
