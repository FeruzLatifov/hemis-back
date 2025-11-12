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

        // Build count query
        String countSql = buildGroupCountQuery(q, status);
        Query countQuery = entityManager.createNativeQuery(countSql);
        setGroupQueryParameters(countQuery, q, status);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        if (total == 0) {
            return Page.empty(pageable);
        }

        // Build data query
        String dataSql = buildGroupDataQuery(q, status, pageable);
        Query dataQuery = entityManager.createNativeQuery(dataSql);
        setGroupQueryParameters(dataQuery, q, status);

        @SuppressWarnings("unchecked")
        List<Object[]> results = dataQuery.getResultList();

        List<FacultyGroupRowDto> groups = results.stream()
            .map(row -> new FacultyGroupRowDto(
                (String) row[0],              // universityCode
                (String) row[1],              // universityName
                ((Number) row[2]).longValue(), // facultyCount
                ((Number) row[3]).longValue()  // activeFacultyCount
            ))
            .collect(Collectors.toList());

        return new PageImpl<>(groups, pageable, total);
    }

    private String buildGroupCountQuery(String q, Boolean status) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(DISTINCT u.code) ");
        sql.append("FROM hemishe_e_university u ");
        sql.append("INNER JOIN hemishe_e_university_department d ON d.university_code = u.code ");
        sql.append("WHERE u.delete_ts IS NULL AND d.delete_ts IS NULL ");
        sql.append("AND d._deparment_type = '").append(FACULTY_DEPT_TYPE).append("' ");
        
        if (q != null && !q.trim().isEmpty()) {
            sql.append("AND (LOWER(u.name) LIKE LOWER(:q) OR LOWER(u.code) LIKE LOWER(:q)) ");
        }
        
        if (status != null) {
            sql.append("AND d.status = :status ");
        }
        
        return sql.toString();
    }

    private String buildGroupDataQuery(String q, Boolean status, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT u.code, u.name, ");
        sql.append("COUNT(d.code) as faculty_count, ");
        sql.append("SUM(CASE WHEN d.status = true THEN 1 ELSE 0 END) as active_count ");
        sql.append("FROM hemishe_e_university u ");
        sql.append("INNER JOIN hemishe_e_university_department d ON d.university_code = u.code ");
        sql.append("WHERE u.delete_ts IS NULL AND d.delete_ts IS NULL ");
        sql.append("AND d._deparment_type = '").append(FACULTY_DEPT_TYPE).append("' ");
        
        if (q != null && !q.trim().isEmpty()) {
            sql.append("AND (LOWER(u.name) LIKE LOWER(:q) OR LOWER(u.code) LIKE LOWER(:q)) ");
        }
        
        if (status != null) {
            sql.append("AND d.status = :status ");
        }
        
        sql.append("GROUP BY u.code, u.name ");
        sql.append("ORDER BY u.name ASC ");
        sql.append("LIMIT ").append(pageable.getPageSize());
        sql.append(" OFFSET ").append(pageable.getOffset());
        
        return sql.toString();
    }

    private void setGroupQueryParameters(Query query, String q, Boolean status) {
        if (q != null && !q.trim().isEmpty()) {
            query.setParameter("q", "%" + q.trim() + "%");
        }
        if (status != null) {
            query.setParameter("status", status);
        }
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

        // Build count query
        String countSql = buildFacultyCountQuery(universityCode, q, status);
        Query countQuery = entityManager.createNativeQuery(countSql);
        setFacultyQueryParameters(countQuery, universityCode, q, status);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        if (total == 0) {
            return Page.empty(pageable);
        }

        // Build data query
        String dataSql = buildFacultyDataQuery(universityCode, q, status, pageable);
        Query dataQuery = entityManager.createNativeQuery(dataSql);
        setFacultyQueryParameters(dataQuery, universityCode, q, status);

        @SuppressWarnings("unchecked")
        List<Object[]> results = dataQuery.getResultList();

        List<FacultyRowDto> faculties = results.stream()
            .map(row -> new FacultyRowDto(
                (String) row[0],       // code
                (String) row[1],       // nameUz
                (String) row[2],       // nameRu
                (String) row[3],       // universityCode
                (String) row[4],       // universityName
                (Boolean) row[5]       // status
            ))
            .collect(Collectors.toList());

        return new PageImpl<>(faculties, pageable, total);
    }

    private String buildFacultyCountQuery(String universityCode, String q, Boolean status) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) ");
        sql.append("FROM hemishe_e_university_department d ");
        sql.append("WHERE d.delete_ts IS NULL ");
        sql.append("AND d._deparment_type = '").append(FACULTY_DEPT_TYPE).append("' ");
        sql.append("AND d.university_code = :universityCode ");
        
        if (q != null && !q.trim().isEmpty()) {
            sql.append("AND (LOWER(d.name_uz) LIKE LOWER(:q) OR LOWER(d.code) LIKE LOWER(:q)) ");
        }
        
        if (status != null) {
            sql.append("AND d.status = :status ");
        }
        
        return sql.toString();
    }

    private String buildFacultyDataQuery(String universityCode, String q, Boolean status, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT d.code, d.name_uz, d.name_ru, d.university_code, u.name, d.status ");
        sql.append("FROM hemishe_e_university_department d ");
        sql.append("INNER JOIN hemishe_e_university u ON d.university_code = u.code ");
        sql.append("WHERE d.delete_ts IS NULL AND u.delete_ts IS NULL ");
        sql.append("AND d._deparment_type = '").append(FACULTY_DEPT_TYPE).append("' ");
        sql.append("AND d.university_code = :universityCode ");
        
        if (q != null && !q.trim().isEmpty()) {
            sql.append("AND (LOWER(d.name_uz) LIKE LOWER(:q) OR LOWER(d.code) LIKE LOWER(:q)) ");
        }
        
        if (status != null) {
            sql.append("AND d.status = :status ");
        }
        
        sql.append("ORDER BY d.name_uz ASC ");
        sql.append("LIMIT ").append(pageable.getPageSize());
        sql.append(" OFFSET ").append(pageable.getOffset());
        
        return sql.toString();
    }

    private void setFacultyQueryParameters(Query query, String universityCode, String q, Boolean status) {
        query.setParameter("universityCode", universityCode);
        if (q != null && !q.trim().isEmpty()) {
            query.setParameter("q", "%" + q.trim() + "%");
        }
        if (status != null) {
            query.setParameter("status", status);
        }
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

        String sql = "SELECT d.code, d.name_uz, d.name_ru, d.university_code, u.name, " +
                    "d.status, d._deparment_type, dt.name, d.parent_code, d.path, " +
                    "d.create_ts, d.created_by, d.update_ts, d.updated_by, d.version " +
                    "FROM hemishe_e_university_department d " +
                    "INNER JOIN hemishe_e_university u ON d.university_code = u.code " +
                    "LEFT JOIN hemishe_h_university_department_type dt ON d._deparment_type = dt.code " +
                    "WHERE d.delete_ts IS NULL AND u.delete_ts IS NULL " +
                    "AND d.code = :code";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("code", code);

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
