package uz.hemis.common.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PhoneNumber value object")
class PhoneNumberTest {

    @Test
    @DisplayName("canonical constructor accepts +998 format")
    void acceptsCanonical() {
        assertThat(new PhoneNumber("+998901234567").value()).isEqualTo("+998901234567");
    }

    @Test
    @DisplayName("rejects non-canonical in constructor")
    void rejectsNonCanonicalInConstructor() {
        assertThatThrownBy(() -> new PhoneNumber("901234567"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("parse normalizes all common formats to canonical")
    void parseNormalizes() {
        assertThat(PhoneNumber.parse("+998901234567").value()).isEqualTo("+998901234567");
        assertThat(PhoneNumber.parse("998901234567").value()).isEqualTo("+998901234567");
        assertThat(PhoneNumber.parse("8901234567").value()).isEqualTo("+998901234567");
        assertThat(PhoneNumber.parse("901234567").value()).isEqualTo("+998901234567");
        assertThat(PhoneNumber.parse("+998 (90) 123-45-67").value()).isEqualTo("+998901234567");
        assertThat(PhoneNumber.parse("90 123 45 67").value()).isEqualTo("+998901234567");
    }

    @Test
    @DisplayName("parse rejects garbage")
    void parseRejectsGarbage() {
        assertThatThrownBy(() -> PhoneNumber.parse("abc")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PhoneNumber.parse("123")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("isValid static check")
    void isValidStatic() {
        assertThat(PhoneNumber.isValid("+998901234567")).isTrue();
        assertThat(PhoneNumber.isValid("901234567")).isTrue();
        assertThat(PhoneNumber.isValid(null)).isFalse();
        assertThat(PhoneNumber.isValid("garbage")).isFalse();
    }
}
