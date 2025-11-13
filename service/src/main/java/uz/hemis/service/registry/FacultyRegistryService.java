package uz.hemis.service.registry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.service.registry.dto.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Faculty Registry Service - Business logic for Faculty Registry API
 *
 * <p><strong>Purpose:</strong> Lazy-loading tree structure for Faculties</p>
 * <ul>
 *   <li>Level 1 (Groups): Universities with faculty counts</li>
 *   <li>Level 2 (Children): Faculties under each university</li>
 * </ul>
 *
 * <p><strong>Performance:</strong></p>
 * <ul>
 *   <li>N+1 Prevention: Using native queries with joins</li>
 *   <li>Caching: Dictionary data cached (1-hour TTL)</li>
 *   <li>Pagination: Both levels support server-side pagination</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FacultyRegistryService {

    private final EntityManager entityManager;

    private static final String FACULTY_DEPT_TYPE = "11"; // Faculty department type code

    // =====================================================
    // Groups API (Universities with faculty counts)
    // =====================================================

    /**
     * Get university groups with faculty counts (Lazy loading - Level 1)
     *
     * @param q Search query (university name/code)
     * @param status Filter by faculty status
     * @param pageable Pagination parameters
     * @return Page of university groups
     */
    public Page<FacultyGroupRowDto> getFacultyGroups(String q, Boolean status, Pageable pageable) {
        log.debug("Getting faculty groups: q={}, status={}, page={}", q, status, pageable.getPageNumber());

        String countSql = "SELECT COUNT(DISTINCT code) FROM hemishe_r_university_department WHERE _department_type = '0'";
        Query countQuery = entityManager.createNativeQuery(countSql);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        if (total == 0) {
            return Page.empty(pageable);
        }

        String dataSql = 
            "SELECT parent.code, parent.name_uz, " +
            "  (SELECT COUNT(*) FROM hemishe_r_university_department f WHERE f.parent_id = parent.id AND f._department_type = '11') as faculty_count, " +
            "  (SELECT COUNT(*) FROM hemishe_r_university_department f WHERE f.parent_id = parent.id AND f._department_type = '11') as active_count " +
            "FROM hemishe_r_university_department parent " +
            "WHERE parent._department_type = '0' " +
            "ORDER BY parent.name_uz ASC " +
            "LIMIT " + pageable.getPageSize() + " OFFSET " + pageable.getOffset();

        Query dataQuery = entityManager.createNativeQuery(dataSql);

        @SuppressWarnings("unchecked")
        List<Object[]> results = dataQuery.getResultList();

        List<FacultyGroupRowDto> groups = results.stream()
            .map(row -> new FacultyGroupRowDto(
                (String) row[0],
                (String) row[1],
                ((Number) row[2]).longValue(),
                ((Number) row[3]).longValue()
            ))
            .collect(Collectors.toList());

        return new PageImpl<>(groups, pageable, total);
    }

    // =====================================================
    // Children API (Faculties by university)
    // =====================================================

    /**
     * Get faculties by university (Lazy loading - Level 2)
     *
     * @param universityCode University code (PK)
     * @param q Search query (faculty name/code)
     * @param status Filter by status
     * @param pageable Pagination parameters
     * @return Page of faculties
     */
    public Page<FacultyRowDto> getFacultiesByUniversity(
        String universityCode, 
        String q, 
        Boolean status, 
        Pageable pageable
    ) {
        log.debug("Getting faculties for university: code={}, q={}, status={}", universityCode, q, status);

        String countSql = 
            "SELECT COUNT(*) FROM hemishe_r_university_department d " +
            "INNER JOIN hemishe_r_university_department parent ON parent.id = d.parent_id " +
            "WHERE d._department_type = '11' AND parent.code = ?";
        
        Query countQuery = entityManager.createNativeQuery(countSql);
        countQuery.setParameter(1, universityCode);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        if (total == 0) {
            return Page.empty(pageable);
        }

        String dataSql = 
            "SELECT d.code, d.name_uz, d.name_ru, parent.code, parent.name_uz, true " +
            "FROM hemishe_r_university_department d " +
            "INNER JOIN hemishe_r_university_department parent ON parent.id = d.parent_id " +
            "WHERE d._department_type = '11' AND parent.code = ? " +
            "ORDER BY d.name_uz ASC " +
            "LIMIT " + pageable.getPageSize() + " OFFSET " + pageable.getOffset();
        
        Query dataQuery = entityManager.createNativeQuery(dataSql);
        dataQuery.setParameter(1, universityCode);

        @SuppressWarnings("unchecked")
        List<Object[]> results = dataQuery.getResultList();

        List<FacultyRowDto> faculties = results.stream()
            .map(row -> new FacultyRowDto(
                (String) row[0],
                (String) row[1],
                (String) row[2],
                (String) row[3],
                (String) row[4],
                (Boolean) row[5]
            ))
            .collect(Collectors.toList());

        return new PageImpl<>(faculties, pageable, total);
    }

    // =====================================================
    // Detail API (Single faculty by code)
    // =====================================================

    /**
     * Get faculty detail by code
     *
     * @param code Faculty code (PK)
     * @return Faculty detail DTO
     */
    public Optional<FacultyDetailDto> getFacultyDetail(String code) {
        log.debug("Getting faculty detail: code={}", code);

        String sql = "SELECT d.code, d.name_uz, d.name_ru, parent.code, parent.name_uz, " +
                    "true, d._department_type, dt.name, CAST(d.parent_id AS VARCHAR), d.keys_, " +
                    "null, null, null, null, 0 " +
                    "FROM hemishe_r_university_department d " +
                    "LEFT JOIN hemishe_r_university_department parent ON parent.id = d.parent_id " +
                    "LEFT JOIN hemishe_h_university_department_type dt ON d._department_type = dt.code " +
                    "WHERE d.code = ?";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, code);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        if (results.isEmpty()) {
            return Optional.empty();
        }

        Object[] row = results.get(0);
        FacultyDetailDto dto = FacultyDetailDto.builder()
            .code((String) row[0])
            .nameUz((String) row[1])
            .nameRu((String) row[2])
            .universityCode((String) row[3])
            .universityName((String) row[4])
            .status((Boolean) row[5])
            .departmentType((String) row[6])
            .departmentTypeName((String) row[7])
            .parentCode((String) row[8])
            .path((String) row[9])
            .createdAt(row[10] != null ? ((java.sql.Timestamp) row[10]).toLocalDateTime() : null)
            .createdBy((String) row[11])
            .updatedAt(row[12] != null ? ((java.sql.Timestamp) row[12]).toLocalDateTime() : null)
            .updatedBy((String) row[13])
            .version((Integer) row[14])
            .build();

        return Optional.of(dto);
    }

    // =====================================================
    // Dictionaries API (Reference data for filters)
    // =====================================================

    /**
     * Get dictionaries for faculty filters (Cached)
     *
     * @return Dictionary data
     */
    @Cacheable(value = "facultyDictionaries", key = "'all'")
    public FacultyDictionariesDto getDictionaries() {
        log.debug("Loading faculty dictionaries");

        List<FacultyDictionariesDto.DictionaryItem> statuses = Arrays.asList(
            FacultyDictionariesDto.DictionaryItem.builder()
                .code("true")
                .label("Active")
                .description("Active faculties")
                .build(),
            FacultyDictionariesDto.DictionaryItem.builder()
                .code("false")
                .label("Inactive")
                .description("Inactive faculties")
                .build()
        );

        String deptTypeSql = "SELECT code, name FROM hemishe_h_university_department_type " +
                            "WHERE delete_ts IS NULL AND active = true ORDER BY name";
        Query deptTypeQuery = entityManager.createNativeQuery(deptTypeSql);

        @SuppressWarnings("unchecked")
        List<Object[]> deptTypeResults = deptTypeQuery.getResultList();

        List<FacultyDictionariesDto.DictionaryItem> departmentTypes = deptTypeResults.stream()
            .map(row -> FacultyDictionariesDto.DictionaryItem.builder()
                .code((String) row[0])
                .label((String) row[1])
                .build())
            .collect(Collectors.toList());

        return FacultyDictionariesDto.builder()
            .statuses(statuses)
            .departmentTypes(departmentTypes)
            .build();
    }
}
