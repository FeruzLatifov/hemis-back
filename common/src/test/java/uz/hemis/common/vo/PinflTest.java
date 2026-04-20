package uz.hemis.common.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Pinfl value object")
class PinflTest {

    @Test
    @DisplayName("accepts valid 14-digit PINFL")
    void acceptsValid() {
        Pinfl p = Pinfl.of("12345678901234");
        assertThat(p.value()).isEqualTo("12345678901234");
    }

    @Test
    @DisplayName("rejects null")
    void rejectsNull() {
        assertThatThrownBy(() -> Pinfl.of(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("rejects wrong length")
    void rejectsWrongLength() {
        assertThatThrownBy(() -> Pinfl.of("123")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Pinfl.of("123456789012345")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("rejects non-digit characters")
    void rejectsNonDigits() {
        assertThatThrownBy(() -> Pinfl.of("1234567890ABCD"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("isValid returns false for invalid inputs")
    void isValidReturnsFalse() {
        assertThat(Pinfl.isValid(null)).isFalse();
        assertThat(Pinfl.isValid("")).isFalse();
        assertThat(Pinfl.isValid("123")).isFalse();
        assertThat(Pinfl.isValid("1234567890ABCD")).isFalse();
    }

    @Test
    @DisplayName("isValid returns true for 14 digits")
    void isValidReturnsTrue() {
        assertThat(Pinfl.isValid("12345678901234")).isTrue();
    }

    @Test
    @DisplayName("masked hides middle digits")
    void maskedHidesMiddle() {
        Pinfl p = Pinfl.of("12345678901234");
        assertThat(p.masked()).isEqualTo("12345*****1234");
        assertThat(p.toString()).isEqualTo("12345*****1234");
    }

    @Test
    @DisplayName("records with same value are equal")
    void equalsByValue() {
        assertThat(Pinfl.of("12345678901234")).isEqualTo(Pinfl.of("12345678901234"));
        assertThat(Pinfl.of("12345678901234")).isNotEqualTo(Pinfl.of("12345678901235"));
    }
}
