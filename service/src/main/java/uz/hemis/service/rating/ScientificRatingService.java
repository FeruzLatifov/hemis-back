package uz.hemis.service.rating;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uz.hemis.common.dto.report.ColumnDto;
import uz.hemis.common.dto.report.ReportBlockDto;
import uz.hemis.common.dto.report.ReportDto;
import uz.hemis.common.dto.report.ReportKpiDto;
import uz.hemis.service.report.ReportSupport;

import java.util.List;

/**
 * RATING 3 — SCIENTIFIC (best data readiness). Ranks universities by
 * {@code publications + projects + doctoral students}. Reuses the exact sources of
 * {@code ScientificReportService} (all central, {@code delete_ts IS NULL}, {@code _university} code):
 * {@code hemishe_e_publication_scientific}, {@code hemishe_e_project}, {@code hemishe_e_doctorate_student}.
 *
 * <p>Ranked {@code ORDER BY total DESC}. Only universities with at least one contribution appear.</p>
 *
 * @since 3.1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScientificRatingService {

    private final RatingSupport rating;
    private final ReportSupport support;

    @Cacheable(value = "ratings", key = "'scientific:' + (#universityCode ?: '')")
    public ReportDto build(String universityCode) {
        log.info("🔬 Building scientific rating (uni={})", universityCode);

        // Outer university filter (applied once to hemishe_e_university).
        ReportSupport.Filter uf = support.filter().eq("u.code", universityCode);
        String uw = uf.sql();
        Object[] ua = uf.args();

        // Direct per-source filters for the scalar KPIs.
        ReportSupport.Filter pf = support.filter().eq("_university", universityCode);
        String pw = pf.sql();
        Object[] pa = pf.args();

        String sub =
                " LEFT JOIN (SELECT _university AS code, COUNT(*) AS c FROM hemishe_e_publication_scientific" +
                "   WHERE delete_ts IS NULL GROUP BY _university) pub ON pub.code = u.code" +
                " LEFT JOIN (SELECT _university AS code, COUNT(*) AS c FROM hemishe_e_project" +
                "   WHERE delete_ts IS NULL GROUP BY _university) prj ON prj.code = u.code" +
                " LEFT JOIN (SELECT _university AS code, COUNT(*) AS c FROM hemishe_e_doctorate_student" +
                "   WHERE delete_ts IS NULL GROUP BY _university) doc ON doc.code = u.code";

        String aggregated =
                "SELECT u.name AS university," +
                " COALESCE(pub.c, 0) AS publications," +
                " COALESCE(prj.c, 0) AS projects," +
                " COALESCE(doc.c, 0) AS doctoral," +
                " (COALESCE(pub.c, 0) + COALESCE(prj.c, 0) + COALESCE(doc.c, 0)) AS total" +
                " FROM hemishe_e_university u" + sub +
                " WHERE u.delete_ts IS NULL" + uw +
                " AND (COALESCE(pub.c, 0) + COALESCE(prj.c, 0) + COALESCE(doc.c, 0)) > 0";

        List<ReportKpiDto> kpis = List.of(
                rating.scalarKpi("totalPublications", "Total publications",
                        "SELECT COUNT(*) FROM hemishe_e_publication_scientific WHERE delete_ts IS NULL" + pw, pa),
                rating.scalarKpi("totalProjects", "Total projects",
                        "SELECT COUNT(*) FROM hemishe_e_project WHERE delete_ts IS NULL" + pw, pa),
                rating.topKpi("topUniversity",
                        "SELECT university, total FROM (" + aggregated + ") r ORDER BY total DESC LIMIT 1", ua)
        );

        List<ColumnDto> cols = List.of(
                new ColumnDto("rank", "Rank"),
                new ColumnDto("university", "University"),
                new ColumnDto("publications", "Publications"),
                new ColumnDto("projects", "Projects"),
                new ColumnDto("doctoral", "Doctoral students"),
                new ColumnDto("total", "Total"));

        List<ReportBlockDto> blocks = List.of(
                rating.rankedTable("leaderboard", "Scientific leaderboard", cols,
                        aggregated + " ORDER BY total DESC", ua),
                support.bar("top15", "Top 15 universities by total output",
                        "SELECT university, total FROM (" + aggregated + ") r ORDER BY total DESC LIMIT 15", ua)
        );

        return new ReportDto(kpis, blocks);
    }
}
