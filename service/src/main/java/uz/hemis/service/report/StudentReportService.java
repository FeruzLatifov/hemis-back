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
 * REPORT 1 — Students. Source: {@code hemishe_r_student_full} (denormalised central fact table).
 *
 * <p>Default population: active ({@code status_code = '11'}), not expelled — matching
 * {@link uz.hemis.service.dashboard.DashboardService}. Reads the REPLICA via {@link ReportSupport}.</p>
 *
 * @since 3.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StudentReportService {

    private final ReportSupport support;

    /** Base predicate: active + not expelled (shared by every KPI/block). */
    private static final String BASE =
            "FROM hemishe_r_student_full s " +
            "WHERE (s.is_expel IS NULL OR s.is_expel = false) AND s.status_code = '11'";

    @Cacheable(value = "reports",
            key = "'students:' + (#educationYear ?: '') + ':' + (#educationType ?: '') + ':' + (#universityCode ?: '')")
    public ReportDto build(Integer educationYear, String educationType, String universityCode) {
        log.info("📈 Building students report (year={}, eduType={}, uni={})",
                educationYear, educationType, universityCode);

        // NOTE: educationYear has no confirmed column on hemishe_r_student_full — accepted but not
        // applied to the WHERE (see report package docs / task issues).
        ReportSupport.Filter f = support.filter()
                .eq("s.university_code", universityCode)
                .eq("s.education_type_code", educationType);
        String w = f.sql();
        Object[] a = f.args();

        List<ReportKpiDto> kpis = List.of(
                support.kpi("totalStudents", "Total students", "SELECT COUNT(*) " + BASE + w, a),
                support.kpi("grant", "Grant", "SELECT COUNT(*) " + BASE + w + " AND s.payment_form_code = '11'", a),
                support.kpi("contract", "Contract", "SELECT COUNT(*) " + BASE + w + " AND s.payment_form_code = '12'", a),
                support.kpi("male", "Male", "SELECT COUNT(*) " + BASE + w + " AND s.gender_code = '11'", a),
                support.kpi("female", "Female", "SELECT COUNT(*) " + BASE + w + " AND s.gender_code = '12'", a)
        );

        List<ReportBlockDto> blocks = List.of(
                support.pie("byEducationType", "By education type", groupBy("education_type_name", w), a),
                support.bar("byEducationForm", "By education form", groupBy("education_form_name", w), a),
                support.pie("byGender", "By gender", groupBy("gender_name", w), a),
                support.pie("byPaymentForm", "By payment form", groupBy("payment_form_name", w), a),
                support.bar("byRegion", "By region", groupBy("university_region_name", w), a),
                support.countTable("topUniversities", "Top universities",
                        "university", "University", "students", "Students count",
                        "SELECT s.university_name, COUNT(*) AS cnt " + BASE + w +
                        " AND s.university_name IS NOT NULL GROUP BY s.university_name" +
                        " ORDER BY cnt DESC LIMIT 25", a)
        );

        return new ReportDto(kpis, blocks);
    }

    /** {@code label, count} grouped over one denormalised column, non-null, top 25. */
    private static String groupBy(String column, String where) {
        return "SELECT s." + column + ", COUNT(*) AS cnt " + BASE + where +
                " AND s." + column + " IS NOT NULL GROUP BY s." + column + " ORDER BY cnt DESC LIMIT 25";
    }
}
