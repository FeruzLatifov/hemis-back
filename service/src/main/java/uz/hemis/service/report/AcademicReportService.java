package uz.hemis.service.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uz.hemis.common.dto.report.ColumnDto;
import uz.hemis.common.dto.report.ReportBlockDto;
import uz.hemis.common.dto.report.ReportDto;
import uz.hemis.common.dto.report.ReportKpiDto;

import java.util.List;

/**
 * REPORT 5 — ACADEMIC performance. Sources (both central report tables, {@code delete_ts IS NULL}):
 * {@code hemishe_r_academic_score} ({@code AcademicScore}: {@code university_code}/{@code university_name},
 * {@code education_type_code}/{@code education_type_name}, {@code education_year_code},
 * {@code score_percent} DOUBLE, {@code debitor_count} DOUBLE) and
 * {@code hemishe_r_academic_attendance} ({@code RAcademicAttendance}: {@code university_code}/{@code university_name},
 * {@code attendance_percent} DOUBLE, {@code bad_attendance_student_count} INT).
 *
 * <p>Both tables carry the same {@code university_code} / {@code education_year_code} /
 * {@code education_type_code} dimensions, so ONE {@link ReportSupport.Filter} drives every KPI/block.
 * Guards: {@code delete_ts IS NULL}, {@code university_name IS NOT NULL} and the measured column
 * ({@code score_percent} / {@code attendance_percent}) {@code IS NOT NULL}. Reads the REPLICA via
 * {@link ReportSupport}. No mutations.</p>
 *
 * @since 3.2.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AcademicReportService {

    private final ReportSupport support;

    /** Score fact — guard soft-delete + non-null name + non-null measure. */
    private static final String SCORE =
            " FROM hemishe_r_academic_score" +
            " WHERE delete_ts IS NULL AND university_name IS NOT NULL AND score_percent IS NOT NULL";

    /** Attendance fact — guard soft-delete + non-null name + non-null measure. */
    private static final String ATTENDANCE =
            " FROM hemishe_r_academic_attendance" +
            " WHERE delete_ts IS NULL AND university_name IS NOT NULL AND attendance_percent IS NOT NULL";

    @Cacheable(value = "reports",
            key = "'academic:' + (#educationYear ?: '') + ':' + (#educationType ?: '') + ':' + (#universityCode ?: '')")
    public ReportDto build(Integer educationYear, String universityCode, String educationType) {
        log.info("🎓 Building academic report (year={}, eduType={}, uni={})",
                educationYear, educationType, universityCode);

        // Same dimension columns on both fact tables → one shared filter.
        ReportSupport.Filter f = support.filter()
                .eq("university_code", universityCode)
                .eq("education_year_code", educationYear == null ? null : String.valueOf(educationYear))
                .eq("education_type_code", educationType);
        String w = f.sql();
        Object[] a = f.args();

        List<ReportKpiDto> kpis = List.of(
                support.kpi("averageScore", "Average score",
                        "SELECT ROUND(AVG(score_percent))" + SCORE + w, a),
                support.kpi("debtors", "Debtors",
                        "SELECT ROUND(COALESCE(SUM(debitor_count), 0))" + SCORE + w, a),
                support.kpi("averageAttendance", "Average attendance",
                        "SELECT ROUND(AVG(attendance_percent))" + ATTENDANCE + w, a),
                support.kpi("universitiesCovered", "Universities covered",
                        "SELECT COUNT(DISTINCT university_code)" + SCORE + w, a)
        );

        List<ColumnDto> perUniversityCols = List.of(
                new ColumnDto("university", "University"),
                new ColumnDto("averageScore", "Average score"),
                new ColumnDto("debtors", "Debtors"));

        List<ReportBlockDto> blocks = List.of(
                support.bar("topUniversitiesByScore", "Top universities by average score",
                        "SELECT university_name, ROUND(AVG(score_percent))" + SCORE + w +
                        " GROUP BY university_name ORDER BY 2 DESC LIMIT 15", a),
                support.pie("byEducationType", "By education type",
                        "SELECT education_type_name, COUNT(*) AS cnt" + SCORE + w +
                        " GROUP BY education_type_name ORDER BY cnt DESC", a),
                support.table("perUniversityAcademicPerformance", "Per-university academic performance",
                        perUniversityCols,
                        "SELECT university_name, ROUND(AVG(score_percent))," +
                        " ROUND(COALESCE(SUM(debitor_count), 0))" + SCORE + w +
                        " GROUP BY university_name ORDER BY 2 DESC", a),
                support.bar("absenteeStudents", "Absentee students",
                        "SELECT university_name, SUM(bad_attendance_student_count)" + ATTENDANCE + w +
                        " GROUP BY university_name ORDER BY 2 DESC LIMIT 15", a)
        );

        return new ReportDto(kpis, blocks);
    }
}
