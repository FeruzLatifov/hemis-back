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
 * REPORT 2 — Institutions. Source: {@code hemishe_e_university} (+ {@code hemishe_h_ownership} /
 * {@code hemishe_h_university_type} classifiers) and {@code hemishe_e_university_department}
 * ({@code _deparment_type '11'} = faculty, {@code '12'} = cathedra). Small central dataset.
 *
 * @since 3.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InstitutionReportService {

    private final ReportSupport support;

    @Cacheable(value = "reports", key = "'institutions:' + (#universityCode ?: '')")
    public ReportDto build(String universityCode) {
        log.info("🏛️ Building institutions report (uni={})", universityCode);

        ReportSupport.Filter uf = support.filter().eq("u.code", universityCode);
        String uw = uf.sql();
        Object[] ua = uf.args();

        // Department counts share the same optional university scope (via university_code).
        ReportSupport.Filter df = support.filter().eq("d.university_code", universityCode);
        String dw = df.sql();
        Object[] da = df.args();

        List<ReportKpiDto> kpis = List.of(
                support.kpi("totalInstitutions", "Total institutions",
                        "SELECT COUNT(*) FROM hemishe_e_university u WHERE u.delete_ts IS NULL" + uw, ua),
                support.kpi("faculties", "Faculties",
                        "SELECT COUNT(*) FROM hemishe_e_university_department d" +
                        " WHERE d.delete_ts IS NULL AND d._deparment_type = '11'" + dw, da),
                support.kpi("cathedras", "Cathedras",
                        "SELECT COUNT(*) FROM hemishe_e_university_department d" +
                        " WHERE d.delete_ts IS NULL AND d._deparment_type = '12'" + dw, da)
        );

        List<ReportBlockDto> blocks = List.of(
                support.pie("byOwnership", "By ownership",
                        "SELECT COALESCE(o.name, u._ownership), COUNT(*) AS cnt" +
                        " FROM hemishe_e_university u" +
                        " LEFT JOIN hemishe_h_ownership o ON o.code = u._ownership AND o.delete_ts IS NULL" +
                        " WHERE u.delete_ts IS NULL" + uw +
                        " GROUP BY COALESCE(o.name, u._ownership) ORDER BY cnt DESC", ua),
                support.pie("byUniversityType", "By university type",
                        "SELECT COALESCE(ut.name, u._university_type), COUNT(*) AS cnt" +
                        " FROM hemishe_e_university u" +
                        " LEFT JOIN hemishe_h_university_type ut ON ut.code = u._university_type AND ut.delete_ts IS NULL" +
                        " WHERE u.delete_ts IS NULL" + uw +
                        " GROUP BY COALESCE(ut.name, u._university_type) ORDER BY cnt DESC", ua),
                support.bar("byRegion", "By region",
                        "SELECT COALESCE(sr.name, u._soato_region), COUNT(*) AS cnt" +
                        " FROM hemishe_e_university u" +
                        " LEFT JOIN hemishe_h_soato sr ON sr.code = u._soato_region AND sr.delete_ts IS NULL" +
                        " WHERE u.delete_ts IS NULL" + uw +
                        " GROUP BY COALESCE(sr.name, u._soato_region) ORDER BY cnt DESC", ua),
                support.table("universityStructure", "University structure",
                        List.of(new ColumnDto("university", "University"),
                                new ColumnDto("faculties", "Faculties"),
                                new ColumnDto("cathedras", "Cathedras")),
                        "SELECT u.name," +
                        " COUNT(d.code) FILTER (WHERE d._deparment_type = '11') AS faculties," +
                        " COUNT(d.code) FILTER (WHERE d._deparment_type = '12') AS cathedras" +
                        " FROM hemishe_e_university u" +
                        " LEFT JOIN hemishe_e_university_department d" +
                        "   ON d.university_code = u.code AND d.delete_ts IS NULL" +
                        "   AND d._deparment_type IN ('11','12')" +
                        " WHERE u.delete_ts IS NULL" + uw +
                        " GROUP BY u.name ORDER BY faculties DESC, u.name", ua)
        );

        return new ReportDto(kpis, blocks);
    }
}
