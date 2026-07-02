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
import java.util.stream.Collectors;

/**
 * RATING 1 — ADMINISTRATIVE. Ranks universities by the TOTAL number of central "RI administrative
 * indicator" rows they own, summed across every {@code hemishe_ri_administrative_*} table that carries
 * a university code ({@code _university}) column.
 *
 * <p>Central RI administrative tables used (all extend BaseEntity → {@code delete_ts IS NULL}, all
 * expose {@code _university} + {@code _education_year} VARCHAR codes):</p>
 * <ul>
 *   <li>{@code hemishe_ri_administrative_student2} / {@code _student3} / {@code _student4} / {@code _student_sport}</li>
 *   <li>{@code hemishe_ri_administrative_sport_facilities}</li>
 *   <li>{@code hemishe_ri_administrative_employee1} / {@code _employee2} / {@code _employee3}</li>
 * </ul>
 *
 * <p>Metric = SUM of per-university counts across those 8 tables (one {@code UNION ALL} feed).
 * If none of the tables are populated the leaderboard is empty — the controller/caller surfaces this
 * (KPIs return 0 and the blocks are empty rather than failing).</p>
 *
 * @since 3.1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdministrativeRatingService {

    private final RatingSupport rating;
    private final ReportSupport support;

    /** Central RI administrative indicator tables carrying a {@code _university} column. */
    private static final List<String> RI_TABLES = List.of(
            "hemishe_ri_administrative_student2",
            "hemishe_ri_administrative_student3",
            "hemishe_ri_administrative_student4",
            "hemishe_ri_administrative_student_sport",
            "hemishe_ri_administrative_sport_facilities",
            "hemishe_ri_administrative_employee1",
            "hemishe_ri_administrative_employee2",
            "hemishe_ri_administrative_employee3");

    @Cacheable(value = "ratings",
            key = "'administrative:' + (#educationYear ?: '') + ':' + (#universityCode ?: '')")
    public ReportDto build(Integer educationYear, String universityCode) {
        log.info("🏛️ Building administrative rating (year={}, uni={})", educationYear, universityCode);

        // Per-table WHERE fragment is identical (same _university / _education_year columns), so we
        // build one filter and repeat its args once per UNION ALL branch.
        ReportSupport.Filter f = support.filter()
                .eq("_university", universityCode)
                .eq("_education_year", educationYear == null ? null : String.valueOf(educationYear));
        String w = f.sql();
        Object[] one = f.args();

        String feed = RI_TABLES.stream()
                .map(t -> "SELECT _university AS code FROM " + t + " WHERE delete_ts IS NULL" + w)
                .collect(Collectors.joining(" UNION ALL "));
        Object[] feedArgs = repeat(one, RI_TABLES.size());

        // rank/table/bar all read from the same aggregated feed.
        String aggregated =
                "SELECT u.name AS university, COUNT(*) AS indicators" +
                " FROM (" + feed + ") t" +
                " LEFT JOIN hemishe_e_university u ON u.code = t.code AND u.delete_ts IS NULL" +
                " GROUP BY u.name";

        List<ReportKpiDto> kpis = List.of(
                rating.scalarKpi("universitiesRanked", "Universities ranked",
                        "SELECT COUNT(*) FROM (" + aggregated + ") r", feedArgs),
                rating.topKpi("topUniversity",
                        "SELECT university, indicators FROM (" + aggregated + ") r" +
                        " ORDER BY indicators DESC LIMIT 1", feedArgs),
                rating.scalarKpi("indicators", "Indicators",
                        "SELECT COUNT(*) FROM (" + feed + ") t", feedArgs)
        );

        List<ColumnDto> cols = List.of(
                new ColumnDto("rank", "Rank"),
                new ColumnDto("university", "University"),
                new ColumnDto("indicators", "Indicators"));

        List<ReportBlockDto> blocks = List.of(
                rating.rankedTable("leaderboard", "Administrative leaderboard", cols,
                        aggregated + " ORDER BY indicators DESC", feedArgs),
                support.bar("top15", "Top 15 universities by indicators",
                        aggregated + " ORDER BY indicators DESC LIMIT 15", feedArgs)
        );

        return new ReportDto(kpis, blocks);
    }

    /** Repeat {@code base} args {@code times} (once per UNION ALL branch). */
    private static Object[] repeat(Object[] base, int times) {
        if (base.length == 0) return base;
        Object[] out = new Object[base.length * times];
        for (int i = 0; i < times; i++) {
            System.arraycopy(base, 0, out, i * base.length, base.length);
        }
        return out;
    }
}
