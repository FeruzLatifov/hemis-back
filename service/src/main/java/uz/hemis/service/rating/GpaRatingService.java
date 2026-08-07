package uz.hemis.service.rating;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uz.hemis.common.dto.report.ColumnDto;
import uz.hemis.common.dto.report.ReportBlockDto;
import uz.hemis.common.dto.report.ReportDto;
import uz.hemis.common.dto.report.ReportKpiDto;
import uz.hemis.common.auth.AccessScope;
import uz.hemis.common.auth.ScopeResolver;
import uz.hemis.service.report.ReportSupport;

import java.util.List;

/**
 * RATING 4 — STUDENT GPA. Ranks universities by {@code AVG(CAST(gpa AS numeric))} over
 * {@code hemishe_e_student_gpa} (the {@code StudentGpa} entity; {@code gpa} is stored as VARCHAR).
 *
 * <p>Join to the university: {@code hemishe_e_student_gpa.student_id → hemishe_e_student.id}, then
 * {@code hemishe_e_student._university → hemishe_e_university.code}. {@code hemishe_e_student_gpa} has
 * NO {@code delete_ts} column, so soft-delete is enforced on the joined student row instead.</p>
 *
 * <p><strong>Numeric guard:</strong> {@code WHERE gpa ~ '^[0-9]+(\.[0-9]+)?$'} — non-numeric GPA
 * strings are excluded so the {@code CAST(... AS numeric)} never crashes.</p>
 *
 * <p><strong>Precision note:</strong> the shared {@link ReportKpiDto}/{@code CategoryDto} carry a
 * {@code double} value, so the KPI, the top-university leader and the bar chart all send the real
 * rounded average GPA (e.g. {@code 3.75}) directly — the same decimal the leaderboard table's
 * {@code Average GPA} column keeps. No scaling; the frontend formats the value as-is.</p>
 *
 * @since 3.1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GpaRatingService {

    private final RatingSupport rating;
    private final ReportSupport support;
    private final ScopeResolver scopeResolver;

    /** Numeric-only guard so {@code CAST(gpa AS numeric)} is always safe. */
    private static final String NUMERIC_GUARD = "g.gpa ~ '^[0-9]+(\\.[0-9]+)?$'";

    /** Lightweight source for overall scalar KPIs (no university join needed). */
    private static final String SCALAR_FROM =
            " FROM hemishe_e_student_gpa g" +
            " JOIN hemishe_e_student s ON s.id = g.student_id AND s.delete_ts IS NULL" +
            " WHERE " + NUMERIC_GUARD;

    @Cacheable(value = "ratings", key = "'gpa:' + (#universityCode ?: '') + '|' + @scopeResolver.currentScopeKey()")
    public ReportDto build(String universityCode) {
        log.info("📊 Building GPA rating (uni={})", universityCode);

        AccessScope scope = scopeResolver.currentScope();
        ReportSupport.Filter f = support.filter().scoped("s._university", scope, universityCode);
        String w = f.sql();
        Object[] a = f.args();

        // Per-university leaderboard feed (name, avg gpa [decimals kept], students counted).
        String aggregated =
                "SELECT u.name AS university," +
                " ROUND(AVG(CAST(g.gpa AS numeric)), 2) AS avg_gpa," +
                " COUNT(*) AS students" +
                " FROM hemishe_e_student_gpa g" +
                " JOIN hemishe_e_student s ON s.id = g.student_id AND s.delete_ts IS NULL" +
                " LEFT JOIN hemishe_e_university u ON u.code = s._university AND u.delete_ts IS NULL" +
                " WHERE " + NUMERIC_GUARD + w +
                " AND u.name IS NOT NULL GROUP BY u.name";

        List<ReportKpiDto> kpis = List.of(
                rating.scalarKpi("averageGpa", "Average GPA",
                        "SELECT ROUND(AVG(CAST(g.gpa AS numeric)), 2)" + SCALAR_FROM + w, a),
                rating.topKpi("topUniversity",
                        "SELECT university, avg_gpa FROM (" + aggregated + ") r" +
                        " ORDER BY avg_gpa DESC LIMIT 1", a),
                rating.scalarKpi("studentsCounted", "Students counted",
                        "SELECT COUNT(*)" + SCALAR_FROM + w, a)
        );

        List<ColumnDto> cols = List.of(
                new ColumnDto("rank", "Rank"),
                new ColumnDto("university", "University"),
                new ColumnDto("averageGpa", "Average GPA"),
                new ColumnDto("students", "Students counted"));

        List<ReportBlockDto> blocks = List.of(
                rating.rankedTable("leaderboard", "GPA leaderboard", cols,
                        aggregated + " ORDER BY avg_gpa DESC", a),
                support.bar("top15", "Top universities",
                        "SELECT u.name, ROUND(AVG(CAST(g.gpa AS numeric)), 2)" +
                        " FROM hemishe_e_student_gpa g" +
                        " JOIN hemishe_e_student s ON s.id = g.student_id AND s.delete_ts IS NULL" +
                        " LEFT JOIN hemishe_e_university u ON u.code = s._university AND u.delete_ts IS NULL" +
                        " WHERE " + NUMERIC_GUARD + w +
                        " AND u.name IS NOT NULL GROUP BY u.name ORDER BY 2 DESC LIMIT 15", a)
        );

        return new ReportDto(kpis, blocks);
    }
}
