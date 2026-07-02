package uz.hemis.common.dto.report;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * A single visualisation block of a ministry analytics report.
 *
 * <p>The shape depends on {@link #viz}:</p>
 * <ul>
 *   <li>{@code 'bar'} / {@code 'pie'} — use {@link #categories}; {@link #columns}/{@link #rows} are null.</li>
 *   <li>{@code 'table'} — use {@link #columns} + {@link #rows}; {@link #categories} is null.</li>
 * </ul>
 *
 * <p>Each {@code rows} element is a {@code columnKey -> (String|Number)} map (serialised as a JSON object).
 * The first column carries the row label (text); the remaining columns are numeric.</p>
 *
 * @param key        stable block key (e.g. {@code "byEducationType"})
 * @param title      English i18n key resolved by the frontend {@code t()}
 * @param viz        one of {@code "bar"}, {@code "pie"}, {@code "table"}
 * @param categories data points for {@code bar}/{@code pie} (null for {@code table})
 * @param columns    column descriptors for {@code table} (null for {@code bar}/{@code pie})
 * @param rows       row objects for {@code table} (null for {@code bar}/{@code pie})
 * @since 3.0.0
 */
public record ReportBlockDto(
        String key,
        String title,
        String viz,
        List<CategoryDto> categories,
        List<ColumnDto> columns,
        List<Map<String, Object>> rows
) implements Serializable {

    /** Build a {@code 'bar'} or {@code 'pie'} block. */
    public static ReportBlockDto chart(String key, String title, String viz, List<CategoryDto> categories) {
        return new ReportBlockDto(key, title, viz, categories, null, null);
    }

    /** Build a {@code 'table'} block. */
    public static ReportBlockDto table(String key, String title, List<ColumnDto> columns, List<Map<String, Object>> rows) {
        return new ReportBlockDto(key, title, "table", null, columns, rows);
    }
}
