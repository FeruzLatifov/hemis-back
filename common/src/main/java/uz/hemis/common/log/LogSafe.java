package uz.hemis.common.log;

/**
 * PII maskalash — log'larga shaxsiy ma'lumotlarni to'liq yozmaslik uchun.
 *
 * <p>O'zbek qonunchiligi (PDn — "Shaxsga doir ma'lumotlar" qonuni) bo'yicha PINFL, passport
 * va email log fayllarda oqib qolmasligi kerak. Audit log kompromissiya bo'lsa,
 * fuqarolar identifikatori tiklanishi mumkin bo'lmaydi.</p>
 *
 * <p><b>Qo'llanishi:</b> Log statementlarida raw PINFL/email o'rniga {@code LogSafe.pinfl(x)}
 * ishlating. Production log lar maskalangan bo'lishi kerak; debug rejim bundan mustasno
 * bo'lishi mumkin (masalan, {@code if (log.isDebugEnabled())} ichida to'liq qiymat).</p>
 */
public final class LogSafe {

    private LogSafe() {}

    /**
     * PINFL ni maskalash: dastlabki 4 raqam + {@code "****"}. Null xavfsiz.
     * Misol: {@code "00000000000000"} → {@code "0000****"}.
     */
    public static String pinfl(String pinfl) {
        if (pinfl == null) return "null";
        if (pinfl.length() <= 4) return "****";
        return pinfl.substring(0, 4) + "****";
    }

    /**
     * Passport seriya + raqamni maskalash: {@code "AA0000000"} → {@code "AA*****"}. Null xavfsiz.
     */
    public static String passport(String passport) {
        if (passport == null) return "null";
        if (passport.length() <= 2) return "**";
        return passport.substring(0, 2) + "*****";
    }

    /**
     * Email ni maskalash: lokal qismi maskalangan, domen saqlanadi — domen odatda PII emas.
     * {@code "user@example.com"} → {@code "us**@example.com"}.
     * {@code "a@example.com"} → {@code "*@example.com"}.
     * Null xavfsiz.
     */
    public static String email(String email) {
        if (email == null) return "null";
        int at = email.indexOf('@');
        if (at < 0) return "***";  // email @ belgisiz — noto'g'ri format
        if (at == 0) return "*" + email.substring(at);  // @example.com
        String local = email.substring(0, at);
        String domain = email.substring(at);
        if (local.length() <= 2) {
            return "*".repeat(local.length()) + domain;
        }
        return local.substring(0, 2) + "**" + domain;
    }

    /**
     * Telefon raqamni maskalash: oxirgi 4 raqamdan boshqalarini yashirish.
     * Misol: {@code "+998901234567"} → {@code "*********4567"}.
     */
    public static String phone(String phone) {
        if (phone == null) return "null";
        int n = phone.length();
        if (n <= 4) return "****";
        return "*".repeat(n - 4) + phone.substring(n - 4);
    }
}
