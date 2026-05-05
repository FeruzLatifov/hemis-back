package uz.hemis.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PiiMask helper")
class PiiMaskTest {

    @Nested
    @DisplayName("phone()")
    class PhoneMask {
        @Test
        @DisplayName("standart 13-raqamli xalqaro telefon — oxirgi 4 ochiq")
        void mask13DigitInternational() {
            assertThat(PiiMask.phone("+998901234567")).isEqualTo("+998*****4567");
        }

        @Test
        @DisplayName("9-raqamli mahalliy telefon — oxirgi 4 ochiq")
        void mask9DigitLocal() {
            assertThat(PiiMask.phone("901234567")).isEqualTo("9012*4567");
        }

        @Test
        @DisplayName("4 yoki kam belgi — to'liq mask")
        void maskShortInput() {
            assertThat(PiiMask.phone("1234")).isEqualTo("****");
            assertThat(PiiMask.phone("12")).isEqualTo("****");
        }

        @Test
        @DisplayName("null va blank — defensive return")
        void nullAndBlank() {
            assertThat(PiiMask.phone(null)).isEqualTo("null");
            assertThat(PiiMask.phone("")).isEqualTo("(blank)");
            assertThat(PiiMask.phone("   ")).isEqualTo("(blank)");
        }
    }

    @Nested
    @DisplayName("email()")
    class EmailMask {
        @Test
        @DisplayName("standart email — birinchi harf + domen")
        void maskStandardEmail() {
            assertThat(PiiMask.email("john.doe@example.com")).isEqualTo("j***@example.com");
        }

        @Test
        @DisplayName("eng kichik email")
        void maskMinimalEmail() {
            assertThat(PiiMask.email("a@b.c")).isEqualTo("a***@b.c");
        }

        @Test
        @DisplayName("@ yo'q yoki noto'g'ri format")
        void invalidFormat() {
            assertThat(PiiMask.email("abc")).isEqualTo("(invalid)");
            assertThat(PiiMask.email("@example.com")).isEqualTo("(invalid)");
            assertThat(PiiMask.email("user@")).isEqualTo("(invalid)");
        }

        @Test
        @DisplayName("null va blank — defensive return")
        void nullAndBlank() {
            assertThat(PiiMask.email(null)).isEqualTo("null");
            assertThat(PiiMask.email("")).isEqualTo("(blank)");
        }
    }

    @Nested
    @DisplayName("name()")
    class NameMask {
        @Test
        @DisplayName("standart ism — birinchi harf ko'rinadi")
        void maskStandardName() {
            assertThat(PiiMask.name("Aliyev Ali")).isEqualTo("A*********");
        }

        @Test
        @DisplayName("bir harfli ism")
        void maskSingleChar() {
            assertThat(PiiMask.name("A")).isEqualTo("*");
        }

        @Test
        @DisplayName("null va blank — defensive return")
        void nullAndBlank() {
            assertThat(PiiMask.name(null)).isEqualTo("null");
            assertThat(PiiMask.name("")).isEqualTo("(blank)");
        }
    }
}
