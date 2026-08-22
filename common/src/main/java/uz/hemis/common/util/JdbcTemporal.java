package uz.hemis.common.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Native query natijasidagi vaqt qiymatini {@link LocalDateTime} ga xavfsiz aylantiradi.
 *
 * <p><strong>Nega kerak:</strong> {@code entityManager.createNativeQuery(...)} {@code Object[]}
 * qaytaradi va {@code timestamp} ustuni uchun QAYSI Java tipi kelishi Hibernate versiyasiga,
 * JDBC drayveriga va ustun tipiga bog'liq. Hibernate 5 da {@code java.sql.Timestamp} kelardi,
 * Hibernate 6+ da esa {@code java.time.LocalDateTime}. Kodda to'g'ridan-to'g'ri
 * {@code (java.sql.Timestamp) row[i]} deb kastlash shu sabab prodda yiqilgan:
 *
 * <pre>
 * ClassCastException: class java.time.LocalDateTime cannot be cast to class java.sql.Timestamp
 *   GET /api/v1/web/registry/faculties/{code}     (Sentry MINISTRY-HEMIS-BACK-12)
 *   GET /api/v1/web/registry/departments/{code}   (Sentry MINISTRY-HEMIS-BACK-13)
 * </pre>
 *
 * <p>Xato kompilyatsiyada ko'rinmaydi (kast — runtime amali) va faqat o'sha endpoint
 * ochilganda 500 beradi. Shuning uchun kast o'rniga tipni TEKSHIRISH kerak.
 *
 * <p><strong>Vaqt mintaqasi:</strong> {@code timestamp without time zone} uchun konversiya
 * kerak emas. Mintaqali tiplar ({@code timestamptz}) uchun tizim mintaqasiga o'tkaziladi —
 * bu ilovaning qolgan qismidagi {@code LocalDateTime} konvensiyasiga mos.
 */
public final class JdbcTemporal {

    private JdbcTemporal() {
    }

    /**
     * @param value native query qatoridan olingan xom qiymat (null bo'lishi mumkin)
     * @return {@link LocalDateTime} yoki {@code null}
     * @throws IllegalStateException qiymat vaqt tipi bo'lmasa — jimgina {@code null}
     *         qaytarmaymiz, aks holda sana yo'qolgani sezilmay qoladi
     */
    public static LocalDateTime toLocalDateTime(Object value) {
        return switch (value) {
            case null -> null;
            case LocalDateTime ldt -> ldt;
            case java.sql.Timestamp ts -> ts.toLocalDateTime();
            case OffsetDateTime odt -> odt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
            case ZonedDateTime zdt -> zdt.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
            case Instant instant -> LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            case java.sql.Date sqlDate -> sqlDate.toLocalDate().atStartOfDay();
            case LocalDate localDate -> localDate.atStartOfDay();
            case java.util.Date date -> LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            default -> throw new IllegalStateException(
                    "Vaqt qiymati kutilgan edi, lekin keldi: " + value.getClass().getName() + " (" + value + ")");
        };
    }

    /**
     * Xuddi shunday, lekin {@link LocalDate} uchun.
     *
     * <p>Registry servislarida shu yordamchining nusxalari bor edi va ularda
     * {@code LocalDateTime} holati YO'Q edi — ular {@code null} qaytarardi. Bu
     * ClassCastException'dan ko'ra yomonroq: xato ham, Sentry izi ham bo'lmaydi,
     * sana UI'dan shunchaki yo'qoladi. Bazada 944 ta {@code timestamp} ustunga
     * qarshi atigi 115 ta {@code date} bor, ya'ni ustun turi o'zgarsa yoki query
     * boshqa ustunga ko'chsa — sana jimgina g'oyib bo'lardi.
     */
    public static LocalDate toLocalDate(Object value) {
        LocalDateTime ldt = toLocalDateTime(value);
        return ldt == null ? null : ldt.toLocalDate();
    }
}
