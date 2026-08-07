package uz.hemis.service.report;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import uz.hemis.common.auth.AccessScope;
import uz.hemis.common.dto.report.CategoryDto;
import uz.hemis.common.dto.report.ColumnDto;
import uz.hemis.common.dto.report.ReportBlockDto;
import uz.hemis.common.dto.report.ReportKpiDto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Shared execution primitives for the 4 ministry analytics reports.
 *
 * <p>All 4 report services follow ONE pattern: build a KPI list + a block list, each backed by a
 * {@code GROUP BY} / {@code COUNT} over the READ REPLICA. This component centralises replica access
 * and DTO assembly so the services stay declarative and never hand-roll JDBC plumbing.</p>
 *
 * <p><strong>Reads exclusively from the replica</strong> via {@code @Qualifier("dashboardJdbcTemplate")}
 * (same source as {@link uz.hemis.service.dashboard.DashboardService}). NO mutations.</p>
 *
 * @since 3.0.0
 */
@Component
@RequiredArgsConstructor
public class ReportSupport {

    @Qualifier("dashboardJdbcTemplate")
    private final JdbcTemplate jdbcTemplate; // REPLICA

    /** Fallback label for NULL classifier / dimension values. */
    private static final String UNKNOWN = "—";

    // ---------------------------------------------------------------------
    // Scalar counts
    // ---------------------------------------------------------------------

    public long count(String sql, Object... args) {
        Long v = jdbcTemplate.queryForObject(sql, Long.class, args);
        return v == null ? 0L : v;
    }

    public ReportKpiDto kpi(String key, String label, String countSql, Object... args) {
        return new ReportKpiDto(key, label, count(countSql, args));
    }

    // ---------------------------------------------------------------------
    // Chart blocks (bar / pie) — SQL must select col1 = label, col2 = count
    // ---------------------------------------------------------------------

    /**
     * Execute a two-column {@code GROUP BY} query into {@link CategoryDto}s.
     * Column 1 is the (nullable) label, column 2 the numeric measure. Read as {@code double} so an
     * average (e.g. GPA {@code 3.75}) keeps its decimals; a plain count still arrives as {@code 42.0}.
     */
    public List<CategoryDto> categories(String sql, Object... args) {
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new CategoryDto(labelOr(rs.getString(1)), rs.getDouble(2)),
                args);
    }

    public ReportBlockDto pie(String key, String title, String sql, Object... args) {
        return ReportBlockDto.chart(key, title, "pie", categories(sql, args));
    }

    public ReportBlockDto bar(String key, String title, String sql, Object... args) {
        return ReportBlockDto.chart(key, title, "bar", categories(sql, args));
    }

    // ---------------------------------------------------------------------
    // Table blocks
    // ---------------------------------------------------------------------

    /**
     * Two-column "label + single count" table (the common "Top N by count" shape).
     * SQL must select col1 = row label (text), col2 = numeric count.
     */
    public ReportBlockDto countTable(String key, String title,
                                     String labelColKey, String labelColLabel,
                                     String valueColKey, String valueColLabel,
                                     String sql, Object... args) {
        List<Map<String, Object>> rows = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put(labelColKey, labelOr(rs.getString(1)));
            row.put(valueColKey, rs.getLong(2));
            return row;
        }, args);
        List<ColumnDto> columns = List.of(
                new ColumnDto(labelColKey, labelColLabel),
                new ColumnDto(valueColKey, valueColLabel));
        return ReportBlockDto.table(key, title, columns, rows);
    }

    /**
     * Generic table block. Each SQL row is mapped positionally onto {@code columns}
     * (column i ← select position i+1). The first column is treated as text, the rest as longs.
     */
    public ReportBlockDto table(String key, String title, List<ColumnDto> columns, String sql, Object... args) {
        List<Map<String, Object>> rows = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 0; i < columns.size(); i++) {
                ColumnDto col = columns.get(i);
                if (i == 0) {
                    row.put(col.key(), labelOr(rs.getString(1)));
                } else {
                    row.put(col.key(), rs.getLong(i + 1));
                }
            }
            return row;
        }, args);
        return ReportBlockDto.table(key, title, columns, rows);
    }

    // ---------------------------------------------------------------------
    // Filter helper — shared dynamic WHERE builder
    // ---------------------------------------------------------------------

    /**
     * Accumulates optional equality predicates + their bind params so every KPI/block in a report
     * shares one filter. Classifier/status codes are inlined as literals by callers (constant, not
     * user input); only user-supplied values pass through {@link #eq(String, Object)}.
     */
    public static final class Filter {
        private final StringBuilder sql = new StringBuilder();
        private final List<Object> params = new ArrayList<>();

        public Filter eq(String columnExpr, Object value) {
            if (value != null && !(value instanceof String s && s.isBlank())) {
                sql.append(" AND ").append(columnExpr).append(" = ?");
                params.add(value);
            }
            return this;
        }

        /**
         * MANDATORY per-OTM scope clamp — the fail-closed heart of the cross-OTM IDOR fix.
         *
         * <p>The {@code scope} is the caller's server-derived {@link AccessScope} (from
         * {@code ScopeResolver.currentScope()}); {@code requestedCode} is the untrusted
         * {@code universityCode} request param. The param is only ever <em>validated against</em> the
         * scope, never trusted to widen it:</p>
         * <ul>
         *   <li><b>deny-all</b> (unresolved / org-tier) → {@code AND 1=0} — returns nothing, never all.</li>
         *   <li><b>unrestricted</b> (ministry/system) → honour an optional voluntary narrow, else no filter.</li>
         *   <li><b>restricted</b> + requested in scope → that one OTM.</li>
         *   <li><b>restricted</b> + requested OUT of scope → {@link AccessDeniedException} (403).</li>
         *   <li><b>restricted</b> + no request param → clamp to the caller's own OTM(s)
         *       ({@code = ?} for one, {@code IN (…)} for several) — omission never means "all".</li>
         * </ul>
         *
         * @param columnExpr the university-code column expression for this report
         *                   (e.g. {@code "s.university_code"}, {@code "u.code"})
         * @param scope      the caller's resolved access scope (never null)
         * @param requestedCode the request-supplied university code (may be null/blank)
         */
        public Filter scoped(String columnExpr, AccessScope scope, String requestedCode) {
            boolean hasRequested = requestedCode != null && !requestedCode.isBlank();
            if (scope.isDenyAll()) {
                sql.append(" AND 1=0");
                return this;
            }
            if (scope.unrestricted()) {
                // ministry / system: may voluntarily narrow to one OTM, otherwise see all
                return eq(columnExpr, hasRequested ? requestedCode : null);
            }
            // restricted OTM-tier caller
            if (hasRequested) {
                if (!scope.allows(requestedCode)) {
                    throw new AccessDeniedException(
                            "Requested university is outside the caller's access scope");
                }
                sql.append(" AND ").append(columnExpr).append(" = ?");
                params.add(requestedCode);
                return this;
            }
            List<String> codes = new ArrayList<>(scope.universityCodes());
            if (codes.size() == 1) {
                sql.append(" AND ").append(columnExpr).append(" = ?");
                params.add(codes.get(0));
            } else {
                sql.append(" AND ").append(columnExpr).append(" IN (")
                   .append(codes.stream().map(c -> "?").collect(Collectors.joining(", ")))
                   .append(")");
                params.addAll(codes);
            }
            return this;
        }

        /** The accumulated {@code " AND ... = ?"} fragment (may be empty). */
        public String sql() {
            return sql.toString();
        }

        public Object[] args() {
            return params.toArray();
        }
    }

    public Filter filter() {
        return new Filter();
    }

    private static String labelOr(String raw) {
        return (raw == null || raw.isBlank()) ? UNKNOWN : raw;
    }
}
