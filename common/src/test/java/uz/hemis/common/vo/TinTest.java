package uz.hemis.common.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Tin value object")
class TinTest {

    @Test
    @DisplayName("accepts valid 9-digit TIN")
    void acceptsValid() {
        assertThat(Tin.of("201555878").value()).isEqualTo("201555878");
    }

    @Test
    @DisplayName("rejects null")
    void rejectsNull() {
        assertThatThrownBy(() -> Tin.of(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("rejects wrong length or non-digits")
    void rejectsInvalid() {
        assertThatThrownBy(() -> Tin.of("123")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Tin.of("1234567890")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Tin.of("12345678A")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("isValid static check")
    void isValidStatic() {
        assertThat(Tin.isValid("201555878")).isTrue();
        assertThat(Tin.isValid(null)).isFalse();
        assertThat(Tin.isValid("123")).isFalse();
    }
}
