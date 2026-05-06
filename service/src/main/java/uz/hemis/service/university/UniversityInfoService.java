package uz.hemis.service.university;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.audit.AuditAction;
import uz.hemis.common.audit.Audited;
import uz.hemis.domain.entity.university.UniversityFounder;
import uz.hemis.domain.entity.university.UniversityLifecycle;
import uz.hemis.service.infrastructure.mapper.BuildingMapper;
import uz.hemis.service.university.dto.UniversityFounderDto;
import uz.hemis.service.university.dto.UniversityLifecycleDto;
import uz.hemis.domain.repository.UniversityBuildingRepository;
import uz.hemis.domain.repository.UniversityFounderRepository;
import uz.hemis.domain.repository.UniversityLifecycleRepository;
import org.springframework.jdbc.core.JdbcTemplate;

import uz.hemis.service.registry.ClassifierLookupService;
import uz.hemis.service.university.dto.RectorDto;
import uz.hemis.service.university.dto.UniversityDashboardDto;

import java.util.List;
import java.util.Map;

/**
 * University Info Service - Aggregates university information for the admin panel
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Founder queries (current and historical)</li>
 *   <li>Lifecycle event tracking</li>
 *   <li>Rector lookup (employee_job position '20')</li>
 *   <li>Dashboard aggregation for a single university</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UniversityInfoService {

    private final UniversityFounderRepository founderRepository;
    private final UniversityLifecycleRepository lifecycleRepository;
    private final UniversityBuildingRepository buildingRepository;
    private final BuildingMapper buildingMapper;
    private final JdbcTemplate jdbcTemplate;
    private final ClassifierLookupService classifiers;

    // =====================================================
    // Founders
    // =====================================================

    @Cacheable(value = "universityFounders", key = "#universityCode")
    public List<UniversityFounder> getFounders(String universityCode) {
        return founderRepository.findByUniversityCode(universityCode);
    }

    // =====================================================
    // Lifecycle
    // =====================================================

    @Cacheable(value = "universityLifecycle", key = "#universityCode")
    public List<UniversityLifecycle> getLifecycle(String universityCode) {
        return lifecycleRepository.findByUniversityCodeOrderByEventDateDesc(universityCode);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "universityDashboard", key = "#event.universityCode"),
        @CacheEvict(value = "universityLifecycle", key = "#event.universityCode")
    })
    @Audited(action = AuditAction.CREATE, entity = "UniversityLifecycle", entityClass = UniversityLifecycle.class)
    public UniversityLifecycle addLifecycleEvent(UniversityLifecycle event) {
        return lifecycleRepository.save(event);
    }

    // =====================================================
    // Dashboard — all info for one university
    // =====================================================

    /**
     * Full university dashboard — founders + lifecycle + buildings + rector.
     *
     * <p><strong>Cache:</strong> {@code universityDashboard} — key = universityCode. TTL 1h.</p>
     *
     * <p>4-5 separate queries aggregated. Caching saves significant DB load for admin panel.</p>
     *
     * <p>Invalidated when founder/lifecycle/building changes (see respective save methods).</p>
     */
    @Cacheable(value = "universityDashboard", key = "#universityCode", unless = "#result == null")
    public UniversityDashboardDto getUniversityDashboard(String universityCode) {
        log.debug("Loading university dashboard (cache MISS) for code: {}", universityCode);
        List<UniversityFounder> founderEntities = getFounders(universityCode);

        return UniversityDashboardDto.builder()
                .founders(founderEntities.stream().map(UniversityFounderDto::from).toList())
                .lifecycle(getLifecycle(universityCode).stream().map(UniversityLifecycleDto::from).toList())
                .buildings(buildingRepository.findByUniversityCodeOrderByNameAsc(universityCode)
                        .stream().map(buildingMapper::toDto).toList())
                .rector(getRector(universityCode))
                .build();
    }

    // =====================================================
    // Rector (from hemishe_e_teacher + hemishe_e_employee_jobs)
    // =====================================================

    /**
     * Find rector — first from NEW tables (employee_jobs), then fallback to OLD (hemishe_e_teacher).
     */
    @Cacheable(value = "universityRector", key = "#universityCode", unless = "#result == null")
    public RectorDto getRector(String universityCode) {
        // 1. NEW: employee + employee_jobs (vazirlik tayinlagan)
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT e.first_name, e.last_name, e.middle_name, e.pinfl, e.phone,
                       m.position_code, p.name as position_name
                FROM employee_job m
                JOIN employee e ON e.id = m.employee_id
                LEFT JOIN h_position p ON p.code = m.position_code
                WHERE m.university_code = ? AND m.is_current = true
                  AND m.position_code = '20' AND m.deleted_at IS NULL
                LIMIT 1
                """, universityCode);
            if (!rows.isEmpty()) {
                Map<String, Object> row = rows.get(0);
                return RectorDto.builder()
                        .firstname(str(row, "first_name"))
                        .lastname(str(row, "last_name"))
                        .fathername(str(row, "middle_name"))
                        .pinfl(str(row, "pinfl"))
                        .phone(str(row, "phone"))
                        .positionCode(str(row, "position_code"))
                        .positionName(str(row, "position_name"))
                        .build();
            }
        } catch (Exception e) {
            log.debug("Error finding rector from new tables: {}", e.getMessage());
        }

        // 2. FALLBACK: hemishe_e_teacher (universitet sync qilgan)
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT t.firstname, t.lastname, t.fathername, t.pinfl, t.phone,
                       j._employee_position as position_code,
                       p.name as position_name
                FROM hemishe_e_teacher t
                JOIN hemishe_e_employee_jobs j ON j._employee = t.id AND j.delete_ts IS NULL
                LEFT JOIN h_position p ON p.code = j._employee_position
                WHERE t._university = ? AND t.delete_ts IS NULL
                  AND j._employee_position = '20'
                ORDER BY j.job_start_date DESC NULLS LAST
                LIMIT 1
                """, universityCode);
            if (!rows.isEmpty()) {
                Map<String, Object> row = rows.get(0);
                return RectorDto.builder()
                        .firstname(str(row, "firstname"))
                        .lastname(str(row, "lastname"))
                        .fathername(str(row, "fathername"))
                        .pinfl(str(row, "pinfl"))
                        .phone(str(row, "phone"))
                        .positionCode(str(row, "position_code"))
                        .positionName(str(row, "position_name"))
                        .build();
            }
        } catch (Exception e) {
            log.debug("Error finding rector from old tables: {}", e.getMessage());
        }

        return null;
    }

    private String str(Map<String, Object> row, String key) {
        Object v = row.get(key);
        return v != null ? v.toString() : null;
    }
}
