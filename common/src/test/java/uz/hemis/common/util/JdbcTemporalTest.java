package uz.hemis.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Native query'dan kelgan vaqt qiymatini aylantirish.
 *
 * <p>Asosiy da'vo: {@code LocalDateTime} ham, {@code java.sql.Timestamp} ham qabul qilinadi.
 * Ilgari kodda to'g'ridan-to'g'ri {@code (java.sql.Timestamp)} kasti bor edi va Hibernate 6
 * {@code LocalDateTime} qaytargani uchun prodda 500 berardi (Sentry MINISTRY-HEMIS-BACK-12/13).</p>
 */
@DisplayName("JdbcTemporal — native query vaqt qiymatini aylantirish")
class JdbcTemporalTest {

    private static final LocalDateTime EXPECTED = LocalDateTime.of(2026, 8, 22, 11, 6, 35);

    @Nested
    @DisplayName("Haqiqiy regressiya")
    class Regression {

        @Test
        @DisplayName("LocalDateTime — Hibernate 6 shu tipni qaytaradi (avval ClassCastException edi)")
        void acceptsLocalDateTime() {
            assertThat(JdbcTemporal.toLocalDateTime(EXPECTED)).isEqualTo(EXPECTED);
        }

        @Test
        @DisplayName("java.sql.Timestamp — Hibernate 5 shu tipni qaytarardi, hamon qo'llab-quvvatlanadi")
        void acceptsSqlTimestamp() {
            assertThat(JdbcTemporal.toLocalDateTime(java.sql.Timestamp.valueOf(EXPECTED)))
                    .isEqualTo(EXPECTED);
        }
    }

    @Nested
    @DisplayName("Boshqa vaqt tiplari")
    class OtherTypes {

        @Test
        @DisplayName("null xavfsiz")
        void nullStaysNull() {
            assertThat(JdbcTemporal.toLocalDateTime(null)).isNull();
        }

        @Test
        @DisplayName("OffsetDateTime tizim mintaqasiga o'tkaziladi")
        void convertsOffsetDateTime() {
            OffsetDateTime odt = EXPECTED.atOffset(ZoneOffset.UTC);
            assertThat(JdbcTemporal.toLocalDateTime(odt))
                    .isEqualTo(odt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime());
        }

        @Test
        @DisplayName("ZonedDateTime tizim mintaqasiga o'tkaziladi")
        void convertsZonedDateTime() {
            ZonedDateTime zdt = EXPECTED.atZone(ZoneOffset.UTC);
            assertThat(JdbcTemporal.toLocalDateTime(zdt))
                    .isEqualTo(zdt.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime());
        }

        @Test
        @DisplayName("Instant tizim mintaqasiga o'tkaziladi")
        void convertsInstant() {
            Instant instant = EXPECTED.toInstant(ZoneOffset.UTC);
            assertThat(JdbcTemporal.toLocalDateTime(instant))
                    .isEqualTo(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
        }

        @Test
        @DisplayName("sana tiplari kun boshiga tushadi")
        void datesBecomeStartOfDay() {
            LocalDate day = LocalDate.of(2026, 8, 22);
            assertThat(JdbcTemporal.toLocalDateTime(day)).isEqualTo(day.atStartOfDay());
            assertThat(JdbcTemporal.toLocalDateTime(java.sql.Date.valueOf(day)))
                    .isEqualTo(day.atStartOfDay());
        }
    }

    @Test
    @DisplayName("vaqt bo'lmagan qiymat — jimgina null emas, BALAND OVOZ bilan yiqiladi")
    void unexpectedTypeFailsLoudly() {
        assertThatThrownBy(() -> JdbcTemporal.toLocalDateTime("2026-08-22"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("java.lang.String");
    }
}
