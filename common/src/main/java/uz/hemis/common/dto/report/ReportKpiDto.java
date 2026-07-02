package uz.hemis.common.dto.report;

import java.io.Serializable;

/**
 * A single headline metric ("KPI card") of a ministry analytics report.
 *
 * @param key   stable metric key (e.g. {@code "totalStudents"})
 * @param label English i18n key resolved by the frontend {@code t()} (e.g. {@code "Total students"})
 * @param value numeric measure (decimal — carries whole counts as {@code 42.0} and averages as {@code 3.75})
 * @since 3.0.0
 */
public record ReportKpiDto(String key, String label, double value) implements Serializable {
}
