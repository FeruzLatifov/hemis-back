package uz.hemis.common.dto.report;

import java.io.Serializable;

/**
 * A single labelled data point for a {@code 'bar'} or {@code 'pie'} report block.
 *
 * @param label human-readable category value (classifier / university / region name — NOT an i18n key)
 * @param value numeric measure (decimal — a row count serialises as {@code 42.0}, an average as {@code 3.75})
 * @since 3.0.0
 */
public record CategoryDto(String label, double value) implements Serializable {
}
