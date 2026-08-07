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
 * REPORT 3 — Scientific activity. Sources (all central, {@code delete_ts IS NULL}):
 * {@code hemishe_e_publication_scientific}, {@code hemishe_e_project}, {@code hemishe_e_doctorate_student};
 * classifiers {@code hemishe_h_publication_type}, {@code hemishe_h_project_type}.
 *
 * <p>{@code educationYear} narrows publications by {@code issue_year} when supplied (it is the only
 * source with a clear year column); projects/doctorates are scoped by {@code universityCode} only.</p>
 *
 * @since 3.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScientificReportService {

    private final ReportSupport support;
    private final ScopeResolver scopeResolver;

    @Cacheable(value = "reports",
            key = "'scientific:' + (#educationYear ?: '') + ':' + (#universityCode ?: '') + '|' + @scopeResolver.currentScopeKey()")
    public ReportDto build(Integer educationYear, String universityCode) {
        log.info("🔬 Building scientific report (year={}, uni={})", educationYear, universityCode);

        AccessScope scope = scopeResolver.currentScope();

        // Publications: university + issue_year filters.
        ReportSupport.Filter pf = support.filter()
                .scoped("p._university", scope, universityCode)
                .eq("p.issue_year", educationYear);
        String pw = pf.sql();
        Object[] pa = pf.args();

        // Projects / doctorates: university scope only.
        ReportSupport.Filter prf = support.filter().scoped("pr._university", scope, universityCode);
        String prw = prf.sql();
        Object[] pra = prf.args();

        ReportSupport.Filter dsf = support.filter().scoped("ds._university", scope, universityCode);
        String dsw = dsf.sql();
        Object[] dsa = dsf.args();

        List<ReportKpiDto> kpis = List.of(
                support.kpi("totalPublications", "Total publications",
                        "SELECT COUNT(*) FROM hemishe_e_publication_scientific p WHERE p.delete_ts IS NULL" + pw, pa),
                support.kpi("totalProjects", "Total projects",
                        "SELECT COUNT(*) FROM hemishe_e_project pr WHERE pr.delete_ts IS NULL" + prw, pra),
                support.kpi("doctoralStudents", "Doctoral students",
                        "SELECT COUNT(*) FROM hemishe_e_doctorate_student ds WHERE ds.delete_ts IS NULL" + dsw, dsa)
        );

        List<ReportBlockDto> blocks = List.of(
                support.pie("publicationsByType", "Publications by type",
                        "SELECT COALESCE(pt.name, p._scientific_publication_type), COUNT(*) AS cnt" +
                        " FROM hemishe_e_publication_scientific p" +
                        " LEFT JOIN hemishe_h_publication_type pt" +
                        "   ON pt.code = p._scientific_publication_type AND pt.delete_ts IS NULL" +
                        " WHERE p.delete_ts IS NULL" + pw +
                        " GROUP BY COALESCE(pt.name, p._scientific_publication_type) ORDER BY cnt DESC", pa),
                support.countTable("publicationsByUniversity", "Publications by university",
                        "university", "University", "publications", "Publications",
                        "SELECT u.name, COUNT(*) AS cnt" +
                        " FROM hemishe_e_publication_scientific p" +
                        " LEFT JOIN hemishe_e_university u ON u.code = p._university AND u.delete_ts IS NULL" +
                        " WHERE p.delete_ts IS NULL" + pw +
                        " GROUP BY u.name ORDER BY cnt DESC LIMIT 25", pa),
                support.pie("projectsByType", "Projects by type",
                        "SELECT COALESCE(pt.name, pr._project_type), COUNT(*) AS cnt" +
                        " FROM hemishe_e_project pr" +
                        " LEFT JOIN hemishe_h_project_type pt ON pt.code = pr._project_type AND pt.delete_ts IS NULL" +
                        " WHERE pr.delete_ts IS NULL" + prw +
                        " GROUP BY COALESCE(pt.name, pr._project_type) ORDER BY cnt DESC", pra),
                support.countTable("projectsByUniversity", "Projects by university",
                        "university", "University", "projects", "Projects",
                        "SELECT u.name, COUNT(*) AS cnt" +
                        " FROM hemishe_e_project pr" +
                        " LEFT JOIN hemishe_e_university u ON u.code = pr._university AND u.delete_ts IS NULL" +
                        " WHERE pr.delete_ts IS NULL" + prw +
                        " GROUP BY u.name ORDER BY cnt DESC LIMIT 25", pra)
        );

        return new ReportDto(kpis, blocks);
    }
}
