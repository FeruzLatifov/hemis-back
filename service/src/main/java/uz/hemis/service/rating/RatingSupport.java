package uz.hemis.service.rating;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uz.hemis.common.dto.report.ColumnDto;
import uz.hemis.common.dto.report.ReportBlockDto;
import uz.hemis.common.dto.report.ReportKpiDto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared execution primitives for the 4 ministry RATING leaderboard cards.
 *
 * <p>A rating is a ranked-by-university {@link uz.hemis.common.dto.report.ReportDto} — it reuses the
 * exact same {@code kpis + blocks} contract as the analytics reports. This component adds the two
 * shapes the plain {@link uz.hemis.service.report.ReportSupport} does not cover:</p>
 * <ul>
 *   <li>{@link #rankedTable} — a leaderboard table whose FIRST column is an injected ordinal
 *       {@code Rank} (1-based over the already {@code ORDER BY metric DESC} SQL), then the University
 *       label, then numeric metric columns (read as {@link java.math.BigDecimal} so averages keep
 *       their decimals). {@code ReportSupport.table()} cannot be reused here because it assumes
 *       column 0 = text and every other column = long, which breaks a Rank-first / decimal layout.</li>
 *   <li>{@link #topKpi} — a "leader" KPI whose <em>label</em> is the winning university name and whose
 *       <em>value</em> is that university's ranking metric (the shared {@link ReportKpiDto} has no text
 *       value slot, so the dynamic name rides in the label — the frontend {@code t()} passes unknown
 *       keys through unchanged).</li>
 * </ul>
 *
 * <p><strong>Reads exclusively from the read replica</strong> via
 * {@code @Qualifier("dashboardJdbcTemplate")} (same source as {@code ReportSupport}). NO mutations.</p>
 *
 * @since 3.1.0
 */
@Component
@RequiredArgsConstructor
public class RatingSupport {

    @Qualifier("dashboardJdbcTemplate")
    private final JdbcTemplate jdbcTemplate; // REPLICA

    private static final String UNKNOWN = "—";

    // ---------------------------------------------------------------------
    // Scalar KPIs
    // ---------------------------------------------------------------------

    /**
     * Numeric scalar (COUNT / AVG / SUM) → 0 when NULL. Read as {@code double} so an average metric
     * (e.g. GPA {@code 3.75}, score percent {@code 87.42}) keeps its decimals; a count arrives as {@code 42.0}.
     */
    public double scalar(String sql, Object... args) {
        Double v = jdbcTemplate.queryForObject(sql, Double.class, args);
        return v == null ? 0.0 : v;
    }

    public ReportKpiDto scalarKpi(String key, String label, String sql, Object... args) {
        return new ReportKpiDto(key, label, scalar(sql, args));
    }

    /**
     * "Leader" KPI. {@code sql} must select col1 = university name (text), col2 = its ranking metric
     * (numeric), already {@code ORDER BY metric DESC LIMIT 1}. The winning name becomes the KPI label.
     * The metric is read as {@code double} so a decimal ranking value (average GPA / score) is preserved.
     */
    public ReportKpiDto topKpi(String key, String sql, Object... args) {
        List<ReportKpiDto> hit = jdbcTemplate.query(sql,
                (rs, rowNum) -> new ReportKpiDto(key, labelOr(rs.getString(1)), rs.getDouble(2)),
                args);
        return hit.isEmpty() ? new ReportKpiDto(key, UNKNOWN, 0.0) : hit.get(0);
    }

    // ---------------------------------------------------------------------
    // Ranked leaderboard table (Rank ordinal injected in Java)
    // ---------------------------------------------------------------------

    /**
     * Build a pre-sorted leaderboard {@code 'table'} block.
     *
     * <p>{@code columns} layout MUST be: index 0 = Rank column, index 1 = University column, index 2..n
     * = numeric metric columns (last one is usually the ranking total). The {@code sql} selects the
     * University label (position 1) followed by the metric columns (positions 2..n) — it must NOT
     * select a Rank column and MUST already be {@code ORDER BY <ranking metric> DESC}. The Rank ordinal
     * is injected here (row order + 1). Metric columns are read as {@link java.math.BigDecimal} so
     * averages keep decimals while counts still serialise as plain integers.</p>
     */
    public ReportBlockDto rankedTable(String key, String title, List<ColumnDto> columns,
                                      String sql, Object... args) {
        List<Map<String, Object>> rows = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put(columns.get(0).key(), (long) (rowNum + 1));          // Rank ordinal
            row.put(columns.get(1).key(), labelOr(rs.getString(1)));      // University (select pos 1)
            for (int i = 2; i < columns.size(); i++) {
                row.put(columns.get(i).key(), rs.getBigDecimal(i));       // metric (select pos i)
            }
            return row;
        }, args);
        return ReportBlockDto.table(key, title, columns, rows);
    }

    private static String labelOr(String raw) {
        return (raw == null || raw.isBlank()) ? UNKNOWN : raw;
    }
}
