package uz.hemis.service.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uz.hemis.common.dto.report.ReportBlockDto;
import uz.hemis.common.dto.report.ReportDto;
import uz.hemis.common.dto.report.ReportKpiDto;

import java.util.List;

/**
 * REPORT 6 — ECONOMIC (partial). Only the metrics whose facts are already CENTRAL are built here;
 * contract-revenue and scholarship-payment stay Univer-only and are intentionally excluded.
 *
 * <p>Sources:</p>
 * <ul>
 *   <li>{@code hemishe_r_employment} ({@code REmployment}: {@code _university}, {@code _education_year},
 *       {@code _gender}, {@code _workplace_compatibility}, {@code qty}). This CUBA report entity does NOT
 *       map a {@code delete_ts} column (no {@code @SQLRestriction}) → no soft-delete guard is applied.</li>
 *   <li>{@code hemishe_r_laboratories} ({@code Laboratories}: {@code university_code},
 *       {@code education_year_code}, {@code total_laboratories}, … — soft-deletable).</li>
 *   <li>{@code hemishe_r_ict_equipment} ({@code IctEquipment}: {@code university_code},
 *       {@code education_year_code}, {@code total_count}, … — soft-deletable).</li>
 * </ul>
 *
 * <p>Classifier labels come from {@code hemishe_h_gender} / {@code hemishe_h_workplace_compatibility}
 * (LEFT JOIN, {@code COALESCE(name, code)} fallback); university names from {@code hemishe_e_university}.
 * Reads the REPLICA via {@link ReportSupport}. No mutations.</p>
 *
 * @since 3.2.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EconomicReportService {

    private final ReportSupport support;

    @Cacheable(value = "reports",
            key = "'economic:' + (#educationYear ?: '') + ':' + (#universityCode ?: '')")
    public ReportDto build(Integer educationYear, String universityCode) {
        log.info("💵 Building economic report (year={}, uni={})", educationYear, universityCode);
        String year = educationYear == null ? null : String.valueOf(educationYear);

        // Employment fact: no delete_ts column on this entity.
        ReportSupport.Filter ef = support.filter()
                .eq("e._university", universityCode)
                .eq("e._education_year", year);
        String ew = ef.sql();
        Object[] ea = ef.args();

        // Infrastructure facts (laboratories / ict): soft-deletable, distinct column names.
        ReportSupport.Filter lf = support.filter()
                .eq("l.university_code", universityCode)
                .eq("l.education_year_code", year);
        String lw = lf.sql();
        Object[] la = lf.args();

        ReportSupport.Filter icf = support.filter()
                .eq("i.university_code", universityCode)
                .eq("i.education_year_code", year);
        String iw = icf.sql();
        Object[] ia = icf.args();

        List<ReportKpiDto> kpis = List.of(
                support.kpi("totalGraduates", "Total graduates",
                        "SELECT COALESCE(SUM(e.qty), 0) FROM hemishe_r_employment e WHERE 1=1" + ew, ea),
                support.kpi("laboratories", "Laboratories",
                        "SELECT COALESCE(SUM(l.total_laboratories), 0) FROM hemishe_r_laboratories l" +
                        " WHERE l.delete_ts IS NULL" + lw, la),
                support.kpi("ictEquipment", "ICT equipment",
                        "SELECT COALESCE(SUM(i.total_count), 0) FROM hemishe_r_ict_equipment i" +
                        " WHERE i.delete_ts IS NULL" + iw, ia)
        );

        List<ReportBlockDto> blocks = List.of(
                support.bar("graduatesByYear", "Graduates by year",
                        "SELECT e._education_year, SUM(e.qty) AS cnt FROM hemishe_r_employment e WHERE 1=1" + ew +
                        " GROUP BY e._education_year ORDER BY e._education_year", ea),
                support.pie("byGender", "By gender",
                        "SELECT COALESCE(g.name, e._gender), SUM(e.qty) AS cnt FROM hemishe_r_employment e" +
                        " LEFT JOIN hemishe_h_gender g ON g.code = e._gender AND g.delete_ts IS NULL" +
                        " WHERE 1=1" + ew +
                        " GROUP BY COALESCE(g.name, e._gender) ORDER BY cnt DESC", ea),
                support.pie("byWorkplaceCompatibility", "By workplace compatibility",
                        "SELECT COALESCE(w.name, e._workplace_compatibility), SUM(e.qty) AS cnt" +
                        " FROM hemishe_r_employment e" +
                        " LEFT JOIN hemishe_h_workplace_compatibility w" +
                        "   ON w.code = e._workplace_compatibility AND w.delete_ts IS NULL" +
                        " WHERE 1=1" + ew +
                        " GROUP BY COALESCE(w.name, e._workplace_compatibility) ORDER BY cnt DESC", ea),
                support.countTable("topUniversitiesByGraduates", "Top universities by graduate count",
                        "university", "University", "graduates", "Graduates",
                        "SELECT u.name, SUM(e.qty) AS cnt FROM hemishe_r_employment e" +
                        " LEFT JOIN hemishe_e_university u ON u.code = e._university AND u.delete_ts IS NULL" +
                        " WHERE 1=1" + ew +
                        " GROUP BY u.name ORDER BY cnt DESC LIMIT 25", ea),
                support.bar("laboratoriesByUniversity", "Laboratories by university",
                        "SELECT COALESCE(u.name, l.university_code), SUM(l.total_laboratories) AS cnt" +
                        " FROM hemishe_r_laboratories l" +
                        " LEFT JOIN hemishe_e_university u ON u.code = l.university_code AND u.delete_ts IS NULL" +
                        " WHERE l.delete_ts IS NULL" + lw +
                        " GROUP BY COALESCE(u.name, l.university_code) ORDER BY cnt DESC LIMIT 15", la)
        );

        return new ReportDto(kpis, blocks);
    }
}
