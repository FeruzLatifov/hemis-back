package uz.hemis.service.university;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.university.UniversityCadastre;
import uz.hemis.domain.entity.university.UniversityFounder;
import uz.hemis.domain.entity.university.UniversityLegal;
import uz.hemis.domain.entity.university.UniversityLifecycle;
import uz.hemis.service.university.dto.UniversityCadastreDto;
import uz.hemis.service.university.dto.UniversityFounderDto;
import uz.hemis.service.university.dto.UniversityLegalDto;
import uz.hemis.service.university.dto.UniversityLifecycleDto;
import uz.hemis.domain.repository.UniversityCadastreRepository;
import uz.hemis.domain.repository.UniversityFounderRepository;
import uz.hemis.domain.repository.UniversityLegalRepository;
import uz.hemis.domain.repository.UniversityLifecycleRepository;
import org.springframework.jdbc.core.JdbcTemplate;

import uz.hemis.service.registry.ClassifierLookupService;
import uz.hemis.service.university.dto.RectorDto;
import uz.hemis.service.university.dto.UniversityDashboardDto;

import java.util.List;
import java.util.Map;

/**
 * University Info Service - Aggregates all university information for the admin panel
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Legal information management</li>
 *   <li>Founder queries (current and historical)</li>
 *   <li>Lifecycle event tracking</li>
 *   <li>Cadastre record management</li>
 *   <li>Dashboard aggregation for a single university</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UniversityInfoService {

    private final UniversityLegalRepository legalRepository;
    private final UniversityFounderRepository founderRepository;
    private final UniversityLifecycleRepository lifecycleRepository;
    private final UniversityCadastreRepository cadastreRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ClassifierLookupService classifiers;

    /** Build a Legal DTO with `billingSoatoName` populated from the SOATO cache. */
    private UniversityLegalDto legalDto(UniversityLegal entity) {
        UniversityLegalDto dto = UniversityLegalDto.from(entity);
        if (dto != null) {
            dto.setBillingSoatoName(classifiers.resolveSoato(dto.getBillingSoato()));
        }
        return dto;
    }

    // =====================================================
    // Legal
    // =====================================================

    @Transactional(readOnly = true)
    public UniversityLegal getLegal(String universityCode) {
        return legalRepository.findByUniversityCode(universityCode).orElse(null);
    }

    @Transactional(readOnly = true)
    public UniversityLegalDto getLegalDto(String universityCode) {
        return legalDto(getLegal(universityCode));
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "universityDashboard", key = "#legal.universityCode"),
        @CacheEvict(value = "universityLegal", key = "#legal.universityCode")
    })
    public UniversityLegal saveLegal(UniversityLegal legal) {
        return legalRepository.save(legal);
    }

    // =====================================================
    // Founders
    // =====================================================

    @Transactional(readOnly = true)
    public List<UniversityFounder> getFounders(String universityCode) {
        return founderRepository.findByUniversityCodeAndIsCurrent(universityCode, true);
    }

    @Transactional(readOnly = true)
    public List<UniversityFounder> getAllFounders(String universityCode) {
        return founderRepository.findByUniversityCode(universityCode);
    }

    // =====================================================
    // Lifecycle
    // =====================================================

    @Transactional(readOnly = true)
    public List<UniversityLifecycle> getLifecycle(String universityCode) {
        return lifecycleRepository.findByUniversityCodeOrderByEventDateDesc(universityCode);
    }

    @Transactional
    @CacheEvict(value = "universityDashboard", key = "#event.universityCode")
    public UniversityLifecycle addLifecycleEvent(UniversityLifecycle event) {
        return lifecycleRepository.save(event);
    }

    // =====================================================
    // Cadastre
    // =====================================================

    @Transactional(readOnly = true)
    public List<UniversityCadastre> getCadastre(String universityCode) {
        return cadastreRepository.findByUniversityCode(universityCode);
    }

    @Transactional
    @CacheEvict(value = "universityDashboard", key = "#cadastre.universityCode")
    public UniversityCadastre saveCadastre(UniversityCadastre cadastre) {
        return cadastreRepository.save(cadastre);
    }

    // =====================================================
    // Dashboard — all info for one university
    // =====================================================

    /**
     * Full university dashboard — legal + founders + lifecycle + cadastre + rector.
     *
     * <p><strong>Cache:</strong> {@code universityDashboard} — key = universityCode. TTL 1h.</p>
     *
     * <p>5-6 separate queries aggregated. Caching saves significant DB load for admin panel.</p>
     *
     * <p>Invalidated when legal/founder/lifecycle/cadastre changes (see respective save methods).</p>
     */
    @Cacheable(value = "universityDashboard", key = "#universityCode", unless = "#result == null")
    @Transactional(readOnly = true)
    public UniversityDashboardDto getUniversityDashboard(String universityCode) {
        log.debug("Loading university dashboard (cache MISS) for code: {}", universityCode);
        UniversityLegal legalEntity = getLegal(universityCode);
        List<UniversityFounder> founderEntities = getFounders(universityCode);

        return UniversityDashboardDto.builder()
                .legal(legalDto(legalEntity))
                .founders(founderEntities.stream().map(UniversityFounderDto::from).toList())
                .lifecycle(getLifecycle(universityCode).stream().map(UniversityLifecycleDto::from).toList())
                .cadastre(getCadastre(universityCode).stream().map(UniversityCadastreDto::from).toList())
                .rector(getRector(universityCode))
                .build();
    }

    // =====================================================
    // Rector (from hemishe_e_teacher + hemishe_e_employee_jobs)
    // =====================================================

    /**
     * Find rector — first from NEW tables (employee_jobs), then fallback to OLD (hemishe_e_teacher).
     */
    @Transactional(readOnly = true)
    public RectorDto getRector(String universityCode) {
        // 1. NEW: employee + employee_jobs (vazirlik tayinlagan)
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT e.first_name, e.last_name, e.middle_name, e.pinfl, e.phone,
                       m.position_code, p.name as position_name
                FROM employee_job m
                JOIN employee e ON e.id = m.employee_id
                LEFT JOIN position p ON p.code = m.position_code
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
                LEFT JOIN hemishe_h_teacher_position_type p ON p.code = j._employee_position
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
