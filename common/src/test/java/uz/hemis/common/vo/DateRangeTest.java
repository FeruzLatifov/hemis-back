package uz.hemis.common.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("DateRange value object")
class DateRangeTest {

    private final LocalDate d1 = LocalDate.of(2024, 1, 1);
    private final LocalDate d2 = LocalDate.of(2024, 6, 30);
    private final LocalDate d3 = LocalDate.of(2025, 1, 1);

    @Test
    @DisplayName("accepts valid range")
    void acceptsValidRange() {
        DateRange r = DateRange.of(d1, d2);
        assertThat(r.from()).isEqualTo(d1);
        assertThat(r.to()).isEqualTo(d2);
    }

    @Test
    @DisplayName("rejects to < from")
    void rejectsInverted() {
        assertThatThrownBy(() -> DateRange.of(d2, d1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("allows open-ended range")
    void allowsOpenEnded() {
        DateRange r = DateRange.openEnded(d1);
        assertThat(r.isOpenEnded()).isTrue();
        assertThat(r.to()).isNull();
    }

    @Test
    @DisplayName("contains checks inclusive bounds")
    void containsChecksInclusive() {
        DateRange r = DateRange.of(d1, d2);
        assertThat(r.contains(d1)).isTrue();       // from bound
        assertThat(r.contains(d2)).isTrue();       // to bound
        assertThat(r.contains(d1.minusDays(1))).isFalse();
        assertThat(r.contains(d2.plusDays(1))).isFalse();
    }

    @Test
    @DisplayName("open-ended contains anything after from")
    void openEndedContainsAfterFrom() {
        DateRange r = DateRange.openEnded(d1);
        assertThat(r.contains(d1)).isTrue();
        assertThat(r.contains(d3)).isTrue();
        assertThat(r.contains(d1.minusDays(1))).isFalse();
    }

    @Test
    @DisplayName("overlaps detects overlapping ranges")
    void overlapsDetects() {
        DateRange a = DateRange.of(d1, d2);
        DateRange b = DateRange.of(d2, d3);
        DateRange c = DateRange.of(d3, d3.plusMonths(1));
        assertThat(a.overlaps(b)).isTrue();   // share d2
        assertThat(a.overlaps(c)).isFalse();  // disjoint
    }
}
