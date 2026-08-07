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
 * RATING 2 — ACADEMIC. Ranks universities by {@code AVG(score_percent)} over
 * {@code hemishe_r_academic_score} (the {@code AcademicScore} report entity). Columns used:
 * {@code university_code} / {@code university_name} / {@code score_percent} (DOUBLE) /
 * {@code debitor_count} (DOUBLE) / {@code education_year_code}.
 *
 * <p>Guard: the average is taken only over rows where {@code score_percent IS NOT NULL} (it is already
 * a numeric DOUBLE column, so no cast is needed). {@code Debtors} = {@code SUM(debitor_count)}.</p>
 *
 * @since 3.1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AcademicRatingService {

    private final RatingSupport rating;
    private final ReportSupport support;
    private final ScopeResolver scopeResolver;

    private static final String FROM =
            " FROM hemishe_r_academic_score" +
            " WHERE delete_ts IS NULL AND score_percent IS NOT NULL";

    @Cacheable(value = "ratings",
            key = "'academic:' + (#educationYear ?: '') + ':' + (#universityCode ?: '') + '|' + @scopeResolver.currentScopeKey()")
    public ReportDto build(Integer educationYear, String universityCode) {
        log.info("🎓 Building academic rating (year={}, uni={})", educationYear, universityCode);

        AccessScope scope = scopeResolver.currentScope();

        ReportSupport.Filter f = support.filter()
                .scoped("university_code", scope, universityCode)
                .eq("education_year_code", educationYear == null ? null : String.valueOf(educationYear));
        String w = f.sql();
        Object[] a = f.args();

        // Aggregated per-university leaderboard feed (name, avg score, debtors).
        String aggregated =
                "SELECT university_name AS university," +
                " ROUND(AVG(score_percent)::numeric, 2) AS avg_score," +
                " ROUND(COALESCE(SUM(debitor_count), 0)::numeric, 0) AS debtors" +
                FROM + w +
                " AND university_name IS NOT NULL GROUP BY university_name";

        List<ReportKpiDto> kpis = List.of(
                rating.scalarKpi("averageScore", "Average score",
                        "SELECT ROUND(AVG(score_percent))" + FROM + w, a),
                rating.topKpi("topUniversity",
                        aggregated + " ORDER BY avg_score DESC LIMIT 1", a),
                rating.scalarKpi("debtors", "Debtors",
                        "SELECT ROUND(COALESCE(SUM(debitor_count), 0))" + FROM + w, a)
        );

        List<ColumnDto> cols = List.of(
                new ColumnDto("rank", "Rank"),
                new ColumnDto("university", "University"),
                new ColumnDto("averageScore", "Average score"),
                new ColumnDto("debtors", "Debtors"));

        List<ReportBlockDto> blocks = List.of(
                rating.rankedTable("leaderboard", "Academic leaderboard", cols,
                        aggregated + " ORDER BY avg_score DESC", a),
                support.bar("top15", "Top 15 universities by average score",
                        "SELECT university_name, ROUND(AVG(score_percent))" + FROM + w +
                        " AND university_name IS NOT NULL GROUP BY university_name" +
                        " ORDER BY 2 DESC LIMIT 15", a)
        );

        return new ReportDto(kpis, blocks);
    }
}
