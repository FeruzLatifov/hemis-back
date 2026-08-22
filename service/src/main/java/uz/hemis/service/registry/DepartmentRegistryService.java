package uz.hemis.service.registry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.util.JdbcTemporal;
import uz.hemis.service.registry.dto.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Department Registry Service - Business logic for Department Registry API
 *
 * <p><strong>Purpose:</strong> Lazy-loading tree structure for Departments (Kafedralar)</p>
 * <ul>
 *   <li>Level 1 (Groups): Universities with department counts</li>
 *   <li>Level 2 (Children): Departments under each university</li>
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
public class DepartmentRegistryService {

    private final EntityManager entityManager;

    private static final String DEPARTMENT_DEPT_TYPE = "12"; // Department (kafedra) department type code

    // =====================================================
    // Groups API (Universities with department counts)
    // =====================================================

    /**
     * Get university groups with department counts (Lazy loading - Level 1)
     *
     * @param q Search query (university name/code)
     * @param status Filter by department status
     * @param pageable Pagination parameters
     * @return Page of university groups
     */
    public Page<DepartmentGroupRowDto> getDepartmentGroups(String q, Boolean status, Pageable pageable) {
        log.debug("Getting department groups: q={}, status={}, page={}", q, status, pageable.getPageNumber());

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

        // `status` OTM emas, bo'linma atributi — shuning uchun EXISTS orqali qo'llanadi:
        // filtrga mos kafedrai yo'q OTM ro'yxatda ko'rinmasin (aks holda foydalanuvchi
        // qatorni ochadi-yu ichi bo'sh chiqadi). Ichki ro'yxat getDepartmentsByUniversity'da allaqachon filtrlanadi.
        if (status != null) {
            where.append("AND EXISTS (SELECT 1 FROM hemishe_e_university_department d "
                    + "WHERE d.university_code = u.code AND d._deparment_type = '12' "
                    + "AND d.delete_ts IS NULL AND d.status = ?) ");
            params.add(status);
            paramIdx++;
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

        // Data query — department counts from hemishe_e_university_department (has delete_ts, status)
        // Note: legacy typo _deparment_type in entity table
        String dataSql =
            "SELECT u.code, u.name, " +
            "  (SELECT COUNT(*) FROM hemishe_e_university_department d " +
            "   WHERE d.university_code = u.code AND d._deparment_type = '12' AND d.delete_ts IS NULL) as department_count, " +
            "  (SELECT COUNT(*) FROM hemishe_e_university_department d " +
            "   WHERE d.university_code = u.code AND d._deparment_type = '12' AND d.delete_ts IS NULL AND d.status = true) as active_count " +
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

        List<DepartmentGroupRowDto> groups = results.stream()
            .map(row -> new DepartmentGroupRowDto(
                (String) row[0],
                (String) row[1],
                ((Number) row[2]).longValue(),
                ((Number) row[3]).longValue()
            ))
            .collect(Collectors.toList());

        return new PageImpl<>(groups, pageable, total);
    }

    // =====================================================
    // Children API (Departments by university)
    // =====================================================

    /**
     * Get departments by university (Lazy loading - Level 2)
     *
     * @param universityCode University code (PK)
     * @param q Search query (department name/code)
     * @param status Filter by status
     * @param pageable Pagination parameters
     * @return Page of departments
     */
    public Page<DepartmentRowDto> getDepartmentsByUniversity(
        String universityCode,
        String q,
        Boolean status,
        Pageable pageable
    ) {
        log.debug("Getting departments for university: code={}, q={}, status={}", universityCode, q, status);

        String searchLike = (q != null && !q.isBlank()) ? "%" + q.trim().toLowerCase() + "%" : null;

        // Uses hemishe_e_university_department (has delete_ts, status) with hemishe_e_university
        // Note: legacy typo _deparment_type in entity table
        StringBuilder where = new StringBuilder(
            "WHERE d._deparment_type = '12' AND d.delete_ts IS NULL AND u.code = ? ");
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

        List<DepartmentRowDto> departments = results.stream()
            .map(row -> new DepartmentRowDto(
                (String) row[0],
                (String) row[1],
                (String) row[2],
                (String) row[3],
                (String) row[4],
                (Boolean) row[5]
            ))
            .collect(Collectors.toList());

        return new PageImpl<>(departments, pageable, total);
    }

    // =====================================================
    // Detail API (Single department by code)
    // =====================================================

    /**
     * Get department detail by code
     *
     * @param code Department code (PK)
     * @return Department detail DTO
     */
    public Optional<DepartmentDetailDto> getDepartmentDetail(String code) {
        log.debug("Getting department detail: code={}", code);

        // Uses entity tables with proper delete_ts/status columns
        // Note: legacy typo _deparment_type in entity table
        String sql = "SELECT d.code, d.name_uz, d.name_ru, u.code, u.name, " +
                    "d.status, d._deparment_type, dt.name, d.parent_code, d.path, " +
                    "d.create_ts, d.created_by, d.update_ts, d.updated_by, d.version " +
                    "FROM hemishe_e_university_department d " +
                    "LEFT JOIN hemishe_e_university u ON u.code = d.university_code " +
                    "LEFT JOIN hemishe_h_university_department_type dt ON d._deparment_type = dt.code " +
                    "WHERE d.code = ? AND d.delete_ts IS NULL";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, code);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        if (results.isEmpty()) {
            return Optional.empty();
        }

        Object[] row = results.get(0);
        DepartmentDetailDto dto = DepartmentDetailDto.builder()
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
            .createdAt(JdbcTemporal.toLocalDateTime(row[10]))
            .createdBy((String) row[11])
            .updatedAt(JdbcTemporal.toLocalDateTime(row[12]))
            .updatedBy((String) row[13])
            .version((Integer) row[14])
            .build();

        return Optional.of(dto);
    }

    // =====================================================
    // Dictionaries API (Reference data for filters)
    // =====================================================

    /**
     * Get dictionaries for department filters (Cached)
     *
     * @return Dictionary data
     */
    @Cacheable(value = "departmentDictionaries", key = "'all'")
    public DepartmentDictionariesDto getDictionaries() {
        log.debug("Loading department dictionaries");

        List<DepartmentDictionariesDto.DictionaryItem> statuses = Arrays.asList(
            DepartmentDictionariesDto.DictionaryItem.builder()
                .code("true")
                .label("Active")
                .description("Active departments")
                .build(),
            DepartmentDictionariesDto.DictionaryItem.builder()
                .code("false")
                .label("Inactive")
                .description("Inactive departments")
                .build()
        );

        String deptTypeSql = "SELECT code, name FROM hemishe_h_university_department_type " +
                            "WHERE delete_ts IS NULL AND active = true ORDER BY name";
        Query deptTypeQuery = entityManager.createNativeQuery(deptTypeSql);

        @SuppressWarnings("unchecked")
        List<Object[]> deptTypeResults = deptTypeQuery.getResultList();

        List<DepartmentDictionariesDto.DictionaryItem> departmentTypes = deptTypeResults.stream()
            .map(row -> DepartmentDictionariesDto.DictionaryItem.builder()
                .code((String) row[0])
                .label((String) row[1])
                .build())
            .collect(Collectors.toList());

        return DepartmentDictionariesDto.builder()
            .statuses(statuses)
            .departmentTypes(departmentTypes)
            .build();
    }
}
