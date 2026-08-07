package uz.hemis.service.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uz.hemis.common.auth.AccessScope;
import uz.hemis.common.auth.ScopeResolver;
import uz.hemis.common.dto.report.ReportBlockDto;
import uz.hemis.common.dto.report.ReportDto;
import uz.hemis.common.dto.report.ReportKpiDto;

import java.util.List;

/**
 * REPORT 4 — Teachers (teaching staff). Base fact table: {@code hemishe_e_teacher} — the central
 * teaching-staff registry that carries the demographic dimensions ({@code _academic_degree},
 * {@code _academic_rank}, {@code _gender}, {@code birthday}, {@code _university}). Classifiers
 * {@code hemishe_h_academic_degree} / {@code hemishe_h_academic_rank} / {@code hemishe_h_gender}.
 *
 * <p>ON-DEMAND {@code GROUP BY} (no cube). Age bands are computed with a {@code CASE} over
 * {@code age(birthday)}. Reads the REPLICA via {@link ReportSupport}.</p>
 *
 * <p><strong>Assumption (see task issues):</strong> {@code DashboardService} exposes no explicit
 * teaching-staff job form/status/type filter (it counts raw job rows), so this report treats each
 * {@code hemishe_e_teacher} row as one teaching-staff member. "PhD holders" / "Professors" are
 * resolved by classifier NAME (codes unconfirmed): doctoral degrees (PhD/DSc/doktor) and the
 * professor rank respectively.</p>
 *
 * @since 3.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherReportService {

    private final ReportSupport support;
    private final ScopeResolver scopeResolver;

    private static final String BASE = "FROM hemishe_e_teacher t WHERE t.delete_ts IS NULL";

    // Doctoral-degree name match (uz/ru), excluding "candidate of sciences / nomzod".
    private static final String PHD_MATCH =
            " AND (LOWER(ad.name) LIKE '%phd%' OR LOWER(ad.name) LIKE '%dsc%'" +
            " OR LOWER(ad.name) LIKE '%doktor%' OR LOWER(ad.name) LIKE '%доктор%')";
    // Professor rank name match (uz "Professor" / ru "Профессор"); "Dotsent" is excluded naturally.
    private static final String PROF_MATCH =
            " AND (LOWER(ar.name) LIKE '%professor%' OR LOWER(ar.name) LIKE '%профессор%')";

    @Cacheable(value = "reports",
            key = "'teachers:' + (#universityCode ?: '') + ':' + (#academicDegree ?: '') + '|' + @scopeResolver.currentScopeKey()")
    public ReportDto build(String universityCode, String academicDegree) {
        log.info("👩‍🏫 Building teachers report (uni={}, degree={})", universityCode, academicDegree);

        AccessScope scope = scopeResolver.currentScope();

        ReportSupport.Filter f = support.filter()
                .scoped("t._university", scope, universityCode)
                .eq("t._academic_degree", academicDegree);
        String w = f.sql();
        Object[] a = f.args();

        List<ReportKpiDto> kpis = List.of(
                support.kpi("totalTeachers", "Total teachers", "SELECT COUNT(*) " + BASE + w, a),
                support.kpi("phdHolders", "PhD holders",
                        "SELECT COUNT(*) FROM hemishe_e_teacher t" +
                        " LEFT JOIN hemishe_h_academic_degree ad ON ad.code = t._academic_degree AND ad.delete_ts IS NULL" +
                        " WHERE t.delete_ts IS NULL" + w + PHD_MATCH, a),
                support.kpi("professors", "Professors",
                        "SELECT COUNT(*) FROM hemishe_e_teacher t" +
                        " LEFT JOIN hemishe_h_academic_rank ar ON ar.code = t._academic_rank AND ar.delete_ts IS NULL" +
                        " WHERE t.delete_ts IS NULL" + w + PROF_MATCH, a)
        );

        List<ReportBlockDto> blocks = List.of(
                support.pie("byAcademicDegree", "By academic degree",
                        "SELECT COALESCE(ad.name, t._academic_degree), COUNT(*) AS cnt" +
                        " FROM hemishe_e_teacher t" +
                        " LEFT JOIN hemishe_h_academic_degree ad ON ad.code = t._academic_degree AND ad.delete_ts IS NULL" +
                        " WHERE t.delete_ts IS NULL AND t._academic_degree IS NOT NULL" + w +
                        " GROUP BY COALESCE(ad.name, t._academic_degree) ORDER BY cnt DESC", a),
                support.bar("byAcademicRank", "By academic rank",
                        "SELECT COALESCE(ar.name, t._academic_rank), COUNT(*) AS cnt" +
                        " FROM hemishe_e_teacher t" +
                        " LEFT JOIN hemishe_h_academic_rank ar ON ar.code = t._academic_rank AND ar.delete_ts IS NULL" +
                        " WHERE t.delete_ts IS NULL AND t._academic_rank IS NOT NULL" + w +
                        " GROUP BY COALESCE(ar.name, t._academic_rank) ORDER BY cnt DESC", a),
                support.bar("byAgeBand", "By age",
                        "SELECT band, COUNT(*) AS cnt FROM (" +
                        "  SELECT CASE" +
                        "    WHEN age < 30 THEN '<30'" +
                        "    WHEN age < 40 THEN '30-39'" +
                        "    WHEN age < 50 THEN '40-49'" +
                        "    WHEN age < 60 THEN '50-59'" +
                        "    ELSE '60+' END AS band, age FROM (" +
                        "      SELECT date_part('year', age(t.birthday))::int AS age" +
                        "      FROM hemishe_e_teacher t" +
                        "      WHERE t.delete_ts IS NULL AND t.birthday IS NOT NULL" + w +
                        "  ) ages" +
                        ") banded GROUP BY band ORDER BY MIN(age)", a),
                support.pie("byGender", "By gender",
                        "SELECT COALESCE(g.name, t._gender), COUNT(*) AS cnt" +
                        " FROM hemishe_e_teacher t" +
                        " LEFT JOIN hemishe_h_gender g ON g.code = t._gender AND g.delete_ts IS NULL" +
                        " WHERE t.delete_ts IS NULL AND t._gender IS NOT NULL" + w +
                        " GROUP BY COALESCE(g.name, t._gender) ORDER BY cnt DESC", a),
                support.countTable("byUniversity", "By university",
                        "university", "University", "teachers", "Teachers count",
                        "SELECT u.name, COUNT(*) AS cnt" +
                        " FROM hemishe_e_teacher t" +
                        " LEFT JOIN hemishe_e_university u ON u.code = t._university AND u.delete_ts IS NULL" +
                        " WHERE t.delete_ts IS NULL" + w +
                        " GROUP BY u.name ORDER BY cnt DESC LIMIT 25", a)
        );

        return new ReportDto(kpis, blocks);
    }
}
