package uz.hemis.common.vo;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Closed date range {@code [from, to]} — half-open at {@code to} (to is nullable for "open-ended").
 *
 * <p>Invariant: when both bounds are present, {@code to >= from}.</p>
 *
 * <p>Usage: historical tracking like {@code effective_from/effective_to} in university_founder.</p>
 *
 * @param from start date (inclusive; nullable for "since always")
 * @param to   end date (inclusive; nullable for "open-ended / still valid")
 * @since 2.0.0
 */
public record DateRange(LocalDate from, LocalDate to) {

    public DateRange {
        if (from != null && to != null && to.isBefore(from)) {
            throw new IllegalArgumentException("DateRange.to must be >= from, got from=" + from + ", to=" + to);
        }
    }

    public static DateRange of(LocalDate from, LocalDate to) {
        return new DateRange(from, to);
    }

    /** Open-ended range starting at given date. */
    public static DateRange openEnded(LocalDate from) {
        return new DateRange(from, null);
    }

    /** @return {@code true} if {@code date} is within this range (inclusive on both bounds). */
    public boolean contains(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        if (from != null && date.isBefore(from)) return false;
        if (to != null && date.isAfter(to)) return false;
        return true;
    }

    /** @return {@code true} if this range has no {@code to} bound (still active). */
    public boolean isOpenEnded() {
        return to == null;
    }

    /** @return {@code true} if this range overlaps with {@code other}. */
    public boolean overlaps(DateRange other) {
        Objects.requireNonNull(other, "other must not be null");
        if (to != null && other.from != null && to.isBefore(other.from)) return false;
        if (other.to != null && from != null && other.to.isBefore(from)) return false;
        return true;
    }
}
