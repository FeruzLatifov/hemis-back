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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Group Registry Service - Business logic for the Study Groups (Guruhlar) registry API.
 *
 * <p><strong>Purpose:</strong> READ-ONLY, lazy-loading tree structure for study groups
 * (OTM-owned academic data that Univer pushes/syncs to the center). Mirrors the
 * {@link FacultyRegistryService}/{@link DepartmentRegistryService} read-only pattern.</p>
 * <ul>
 *   <li>Level 1 (Groups): Universities with study-group counts</li>
 *   <li>Level 2 (Children): Study groups under each university</li>
 * </ul>
 *
 * <p><strong>Source table:</strong> {@code hemishe_e_university_group} — has NO
 * {@code delete_ts} and NO audit columns, so (unlike Faculty/Department) NO
 * soft-delete filter is applied on the group table. University soft-delete is still
 * enforced via {@code hemishe_e_university.delete_ts}.</p>
 *
 * <p><strong>Classifier resolution:</strong> LEFT JOIN {@code hemishe_h_education_type}
 * and {@code hemishe_h_education_year} to resolve human labels; falls back to the raw
 * code when a classifier row is missing.</p>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GroupRegistryService {

    private final EntityManager entityManager;

    // =====================================================
    // Groups API (Universities with study-group counts)
    // =====================================================

    /**
     * Get university groups with study-group counts (Lazy loading - Level 1).
     *
     * @param q Search query (university name/code)
     * @param status Filter by group status (accepted for API symmetry; not applied to root counts)
     * @param pageable Pagination parameters
     * @return Page of university groups
     */
    public Page<GroupGroupRowDto> getGroupGroups(String q, Boolean status, Pageable pageable) {
        log.debug("Getting group groups: q={}, status={}, page={}", q, status, pageable.getPageNumber());

        String searchLike = (q != null && !q.isBlank()) ? "%" + q.trim().toLowerCase() + "%" : null;

        // Uses hemishe_e_university (has delete_ts) for proper soft-delete filtering of universities.
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

        // Data query — study-group counts from hemishe_e_university_group (NO delete_ts on this table).
        String dataSql =
            "SELECT u.code, u.name, " +
            "  (SELECT COUNT(*) FROM hemishe_e_university_group g " +
            "   WHERE g._university = u.code) as group_count, " +
            "  (SELECT COUNT(*) FROM hemishe_e_university_group g " +
            "   WHERE g._university = u.code AND g.active = true) as active_count " +
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

        List<GroupGroupRowDto> groups = results.stream()
            .map(row -> new GroupGroupRowDto(
                (String) row[0],
                (String) row[1],
                ((Number) row[2]).longValue(),
                ((Number) row[3]).longValue()
            ))
            .collect(Collectors.toList());

        return new PageImpl<>(groups, pageable, total);
    }

    // =====================================================
    // Children API (Study groups by university)
    // =====================================================

    /**
     * Get study groups by university (Lazy loading - Level 2).
     *
     * @param universityCode University code
     * @param q Search query (group name / external group id)
     * @param educationType Filter by education-type classifier code
     * @param educationYear Filter by education-year classifier code
     * @param status Filter by active status
     * @param pageable Pagination parameters
     * @return Page of study groups
     */
    public Page<GroupRegistryRowDto> getGroupsByUniversity(
        String universityCode,
        String q,
        String educationType,
        String educationYear,
        Boolean status,
        Pageable pageable
    ) {
        log.debug("Getting groups for university: code={}, q={}, eduType={}, eduYear={}, status={}",
                  universityCode, q, educationType, educationYear, status);

        String searchLike = (q != null && !q.isBlank()) ? "%" + q.trim().toLowerCase() + "%" : null;

        // NO delete_ts filter on the group table (it has none); university soft-delete via u.delete_ts.
        StringBuilder where = new StringBuilder("WHERE u.delete_ts IS NULL AND u.code = ? ");
        List<Object> params = new ArrayList<>();
        params.add(universityCode);
        int paramIdx = 2;

        if (searchLike != null) {
            where.append("AND (LOWER(g.group_name) LIKE ? OR LOWER(g.group_id) LIKE ?) ");
            params.add(searchLike);
            params.add(searchLike);
            paramIdx += 2;
        }

        if (educationType != null && !educationType.isBlank()) {
            where.append("AND g._education_type = ? ");
            params.add(educationType.trim());
            paramIdx++;
        }

        if (educationYear != null && !educationYear.isBlank()) {
            where.append("AND g._education_year = ? ");
            params.add(educationYear.trim());
            paramIdx++;
        }

        if (status != null) {
            where.append("AND g.active = ? ");
            params.add(status);
            paramIdx++;
        }

        // Count query
        String countSql =
            "SELECT COUNT(*) FROM hemishe_e_university_group g " +
            "INNER JOIN hemishe_e_university u ON u.code = g._university " +
            where;

        Query countQuery = entityManager.createNativeQuery(countSql);
        for (int i = 0; i < params.size(); i++) {
            countQuery.setParameter(i + 1, params.get(i));
        }
        long total = ((Number) countQuery.getSingleResult()).longValue();

        if (total == 0) {
            return Page.empty(pageable);
        }

        // Data query — LEFT JOIN classifier tables to resolve education-type/year labels.
        String dataSql =
            "SELECT g.id, g.group_id, g.group_name, u.code, u.name, " +
            "  g._education_type, et.name, g._education_year, ey.name, g.active " +
            "FROM hemishe_e_university_group g " +
            "INNER JOIN hemishe_e_university u ON u.code = g._university " +
            "LEFT JOIN hemishe_h_education_type et ON et.code = g._education_type AND et.delete_ts IS NULL " +
            "LEFT JOIN hemishe_h_education_year ey ON ey.code = g._education_year AND ey.delete_ts IS NULL " +
            where +
            "ORDER BY g.group_name ASC " +
            "LIMIT ? OFFSET ?";

        Query dataQuery = entityManager.createNativeQuery(dataSql);
        for (int i = 0; i < params.size(); i++) {
            dataQuery.setParameter(i + 1, params.get(i));
        }
        dataQuery.setParameter(paramIdx, pageable.getPageSize());
        dataQuery.setParameter(paramIdx + 1, pageable.getOffset());

        @SuppressWarnings("unchecked")
        List<Object[]> results = dataQuery.getResultList();

        List<GroupRegistryRowDto> groups = results.stream()
            .map(this::mapRow)
            .collect(Collectors.toList());

        return new PageImpl<>(groups, pageable, total);
    }

    // =====================================================
    // Detail API (Single study group by id)
    // =====================================================

    /**
     * Get study-group detail by id (UUID).
     *
     * @param id Group id (UUID string)
     * @return Group detail DTO (empty if not found or id not a valid UUID)
     */
    public Optional<GroupDetailDto> getGroupDetail(String id) {
        log.debug("Getting group detail: id={}", id);

        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }

        String sql =
            "SELECT g.id, g.group_id, g.group_name, u.code, u.name, " +
            "  g._education_type, et.name, g._education_year, ey.name, g.active " +
            "FROM hemishe_e_university_group g " +
            "LEFT JOIN hemishe_e_university u ON u.code = g._university AND u.delete_ts IS NULL " +
            "LEFT JOIN hemishe_h_education_type et ON et.code = g._education_type AND et.delete_ts IS NULL " +
            "LEFT JOIN hemishe_h_education_year ey ON ey.code = g._education_year AND ey.delete_ts IS NULL " +
            "WHERE g.id = ?";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, uuid);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        if (results.isEmpty()) {
            return Optional.empty();
        }

        GroupRegistryRowDto row = mapRow(results.get(0));
        return Optional.of(new GroupDetailDto(
            row.id(), row.groupId(), row.groupName(),
            row.universityCode(), row.universityName(),
            row.educationTypeCode(), row.educationTypeName(),
            row.educationYearCode(), row.educationYearName(),
            row.active()
        ));
    }

    /**
     * Map a native-query row to a {@link GroupRegistryRowDto}, resolving classifier
     * labels with a fallback to the raw code when the classifier row is missing.
     */
    private GroupRegistryRowDto mapRow(Object[] row) {
        String id = row[0] != null ? row[0].toString() : null;
        String eduTypeCode = (String) row[5];
        String eduTypeName = (String) row[6];
        String eduYearCode = (String) row[7];
        String eduYearName = (String) row[8];

        return new GroupRegistryRowDto(
            id,
            (String) row[1],
            (String) row[2],
            (String) row[3],
            (String) row[4],
            eduTypeCode,
            eduTypeName != null ? eduTypeName : eduTypeCode,
            eduYearCode,
            eduYearName != null ? eduYearName : eduYearCode,
            (Boolean) row[9]
        );
    }

    // =====================================================
    // Dictionaries API (Reference data for filters)
    // =====================================================

    /**
     * Get dictionaries for study-group filters (Cached).
     *
     * @return Dictionary data (education types, education years, statuses)
     */
    @Cacheable(value = "groupDictionaries", key = "'all'")
    public GroupDictionariesDto getDictionaries() {
        log.debug("Loading group dictionaries");

        List<GroupDictionariesDto.DictionaryItem> statuses = Arrays.asList(
            new GroupDictionariesDto.DictionaryItem("true", "Active"),
            new GroupDictionariesDto.DictionaryItem("false", "Inactive")
        );

        List<GroupDictionariesDto.DictionaryItem> educationTypes = loadClassifier("hemishe_h_education_type");
        List<GroupDictionariesDto.DictionaryItem> educationYears = loadClassifier("hemishe_h_education_year");

        return new GroupDictionariesDto(educationTypes, educationYears, statuses);
    }

    private List<GroupDictionariesDto.DictionaryItem> loadClassifier(String table) {
        // Table name is a fixed literal (not user input) — safe to inline.
        String sql = "SELECT code, name FROM " + table +
                     " WHERE delete_ts IS NULL AND active = true ORDER BY name";
        Query query = entityManager.createNativeQuery(sql);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();

        return rows.stream()
            .map(row -> new GroupDictionariesDto.DictionaryItem((String) row[0], (String) row[1]))
            .collect(Collectors.toList());
    }
}
