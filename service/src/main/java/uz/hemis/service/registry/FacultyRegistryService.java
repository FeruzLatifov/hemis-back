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

        String searchLike = (q != null && !q.isBlank()) ? "%" + q.trim().toLowerCase() + "%" : null;

        // Uses hemishe_e_university (has delete_ts) for proper soft-delete filtering
        StringBuilder where = new StringBuilder("WHERE u.delete_ts IS NULL ");
        List<Object> params = new ArrayList<>();
        int paramIdx = 1;

        if (searchLike != null) {
            where.append("AND (LOWER(u.name) LIKE ? OR LOWER(u.code) LIKE ?) ");
            params.add(searchLike);
            params.add(searchLike);
            paramIdx += 2;
        }

        // Count query
        String countSql = "SELECT COUNT(*) FROM hemishe_e_university u " + where;
        Query countQuery = entityManager.createNativeQuery(countSql);
        for (int i = 0; i < params.size(); i++) {
            countQuery.setParameter(i + 1, params.get(i));
        }
        long total = ((Number) countQuery.getSingleResult()).longValue();

        if (total == 0) {
            return Page.empty(pageable);
        }

        // Data query — faculty counts from hemishe_e_university_department (has delete_ts, status)
        // Note: legacy typo _deparment_type in entity table
        String dataSql =
            "SELECT u.code, u.name, " +
            "  (SELECT COUNT(*) FROM hemishe_e_university_department d " +
            "   WHERE d.university_code = u.code AND d._deparment_type = '11' AND d.delete_ts IS NULL) as faculty_count, " +
            "  (SELECT COUNT(*) FROM hemishe_e_university_department d " +
            "   WHERE d.university_code = u.code AND d._deparment_type = '11' AND d.delete_ts IS NULL AND d.status = true) as active_count " +
            "FROM hemishe_e_university u " +
            where +
            "ORDER BY u.name ASC " +
            "LIMIT ? OFFSET ?";

        Query dataQuery = entityManager.createNativeQuery(dataSql);
        for (int i = 0; i < params.size(); i++) {
            dataQuery.setParameter(i + 1, params.get(i));
        }
        dataQuery.setParameter(paramIdx, pageable.getPageSize());
        dataQuery.setParameter(paramIdx + 1, pageable.getOffset());

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

        String searchLike = (q != null && !q.isBlank()) ? "%" + q.trim().toLowerCase() + "%" : null;

        // Uses hemishe_e_university_department (has delete_ts, status) with hemishe_e_university
        // Note: legacy typo _deparment_type in entity table
        StringBuilder where = new StringBuilder(
            "WHERE d._deparment_type = '11' AND d.delete_ts IS NULL AND u.code = ? ");
        List<Object> params = new ArrayList<>();
        params.add(universityCode);
        int paramIdx = 2;

        if (searchLike != null) {
            where.append("AND (LOWER(d.name_uz) LIKE ? OR LOWER(d.code) LIKE ?) ");
            params.add(searchLike);
            params.add(searchLike);
            paramIdx += 2;
        }

        if (status != null) {
            where.append("AND d.status = ? ");
            params.add(status);
            paramIdx++;
        }

        // Count query
        String countSql =
            "SELECT COUNT(*) FROM hemishe_e_university_department d " +
            "INNER JOIN hemishe_e_university u ON u.code = d.university_code " +
            where;

        Query countQuery = entityManager.createNativeQuery(countSql);
        for (int i = 0; i < params.size(); i++) {
            countQuery.setParameter(i + 1, params.get(i));
        }
        long total = ((Number) countQuery.getSingleResult()).longValue();

        if (total == 0) {
            return Page.empty(pageable);
        }

        // Data query
        String dataSql =
            "SELECT d.code, d.name_uz, d.name_ru, u.code, u.name, d.status " +
            "FROM hemishe_e_university_department d " +
            "INNER JOIN hemishe_e_university u ON u.code = d.university_code " +
            where +
            "ORDER BY d.name_uz ASC " +
            "LIMIT ? OFFSET ?";

        Query dataQuery = entityManager.createNativeQuery(dataSql);
        for (int i = 0; i < params.size(); i++) {
            dataQuery.setParameter(i + 1, params.get(i));
        }
        dataQuery.setParameter(paramIdx, pageable.getPageSize());
        dataQuery.setParameter(paramIdx + 1, pageable.getOffset());

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

        // Uses entity tables with proper delete_ts/status columns
        // Note: legacy typo _deparment_type in entity table
        String sql = "SELECT d.code, d.name_uz, d.name_ru, u.code, u.name, " +
                    "d.status, d._deparment_type, dt.name, d.parent_code, d.path, " +
                    "d.create_ts, d.created_by, d.update_ts, d.updated_by, d.version " +
                    "FROM hemishe_e_university_department d " +
                    "LEFT JOIN hemishe_e_university u ON u.code = d.university_code " +
                    "LEFT JOIN university_department_type dt ON d._deparment_type = dt.code " +
                    "WHERE d.code = ? AND d.delete_ts IS NULL";

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

        String deptTypeSql = "SELECT code, name FROM university_department_type " +
                            "WHERE is_active = true ORDER BY name";
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
