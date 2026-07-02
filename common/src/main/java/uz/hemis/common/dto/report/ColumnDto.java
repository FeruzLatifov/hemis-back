package uz.hemis.common.dto.report;

import java.io.Serializable;

/**
 * Column descriptor for a {@code 'table'} report block.
 *
 * @param key   stable column key used as the {@link ReportBlockDto} row-map key
 * @param label English i18n key resolved by the frontend {@code t()} (e.g. {@code "University"})
 * @since 3.0.0
 */
public record ColumnDto(String key, String label) implements Serializable {
}
