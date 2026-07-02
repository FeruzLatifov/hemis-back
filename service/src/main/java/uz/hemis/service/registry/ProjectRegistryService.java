package uz.hemis.service.registry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.service.registry.dto.ProjectDetailDto;
import uz.hemis.service.registry.dto.ProjectDictionariesDto;
import uz.hemis.service.registry.dto.ProjectRowDto;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Scientific Project Registry Service - READ-ONLY business logic.
 *
 * <p>Source table {@code hemishe_e_project}. Read via {@link EntityManager} native queries.
 * Soft-delete enforced with explicit {@code AND t.delete_ts IS NULL}. University and
 * classifier tables LEFT-JOINed with their own {@code delete_ts IS NULL} guard and a
 * raw-code fallback when the classifier row is missing. {@code _translations} is never
 * selected.</p>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProjectRegistryService {

    private final EntityManager entityManager;

    private static final String BASE_FROM =
        "FROM hemishe_e_project t " +
        "LEFT JOIN hemishe_e_university u ON u.code = t._university AND u.delete_ts IS NULL " +
        "LEFT JOIN hemishe_h_project_type pt ON pt.code = t._project_type AND pt.delete_ts IS NULL " +
        "LEFT JOIN hemishe_h_project_locality pl ON pl.code = t._locality AND pl.delete_ts IS NULL " +
        "LEFT JOIN hemishe_h_currency cu ON cu.code = t._project_currency AND cu.delete_ts IS NULL ";

    private static final String ROW_COLS =
        "t.id, t.name, t.project_number, t._university, u.name, " +
        "t._project_type, pt.name, t.contract_number, t.contract_date, t.start_date, t.end_date, t.active ";

    public Page<ProjectRowDto> getProjects(String q, String universityCode, String projectType,
                                           Boolean active, Pageable pageable) {
        log.debug("Getting projects: q={}, universityCode={}, projectType={}, active={}, page={}",
                  q, universityCode, projectType, active, pageable.getPageNumber());

        List<Object> params = new ArrayList<>();
        String where = buildWhere(q, universityCode, projectType, active, params);

        String countSql = "SELECT COUNT(*) " + BASE_FROM + where;
        Query countQuery = entityManager.createNativeQuery(countSql);
        bind(countQuery, params, 1);
        long total = ((Number) countQuery.getSingleResult()).longValue();
        if (total == 0) {
            return Page.empty(pageable);
        }

        String dataSql = "SELECT " + ROW_COLS + BASE_FROM + where +
            "ORDER BY t.contract_date DESC NULLS LAST, t.id LIMIT ? OFFSET ?";
        Query dataQuery = entityManager.createNativeQuery(dataSql);
        int idx = bind(dataQuery, params, 1);
        dataQuery.setParameter(idx, pageable.getPageSize());
        dataQuery.setParameter(idx + 1, pageable.getOffset());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();
        List<ProjectRowDto> content = rows.stream().map(this::mapRow).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, total);
    }

    public Optional<ProjectDetailDto> getProjectDetail(String id) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }

        String sql = "SELECT " + ROW_COLS +
            ", t._department, t._locality, pl.name, t._project_currency, cu.name " +
            BASE_FROM + "WHERE t.delete_ts IS NULL AND t.id = ?";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, uuid);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Object[] r = rows.get(0);
        ProjectRowDto row = mapRow(r);
        return Optional.of(new ProjectDetailDto(
            row.id(), row.name(), row.projectNumber(), row.universityCode(), row.universityName(),
            row.projectTypeCode(), row.projectTypeName(), row.contractNumber(), row.contractDate(),
            row.startDate(), row.endDate(), row.active(),
            (String) r[12], (String) r[13], label(r[14], r[13]), (String) r[15], label(r[16], r[15])
        ));
    }

    @Cacheable(value = "scientificProjectsDictionaries", key = "'all'")
    public ProjectDictionariesDto getDictionaries() {
        log.debug("Loading scientific project dictionaries");
        return new ProjectDictionariesDto(
            loadUniversities(),
            loadClassifier("hemishe_h_project_type")
        );
    }

    // ---- helpers ----

    private String buildWhere(String q, String universityCode, String projectType,
                              Boolean active, List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE t.delete_ts IS NULL ");
        String like = (q != null && !q.isBlank()) ? "%" + q.trim().toLowerCase() + "%" : null;
        if (like != null) {
            where.append("AND (LOWER(t.name) LIKE ? OR LOWER(t.project_number) LIKE ? " +
                         "OR LOWER(t.contract_number) LIKE ?) ");
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (universityCode != null && !universityCode.isBlank()) {
            where.append("AND t._university = ? ");
            params.add(universityCode.trim());
        }
        if (projectType != null && !projectType.isBlank()) {
            where.append("AND t._project_type = ? ");
            params.add(projectType.trim());
        }
        if (active != null) {
            where.append("AND t.active = ? ");
            params.add(active);
        }
        return where.toString();
    }

    private ProjectRowDto mapRow(Object[] r) {
        return new ProjectRowDto(
            str(r[0]), (String) r[1], (String) r[2], (String) r[3], (String) r[4],
            (String) r[5], label(r[6], r[5]), (String) r[7], toLocalDate(r[8]),
            toLocalDate(r[9]), toLocalDate(r[10]), (Boolean) r[11]
        );
    }

    private List<ProjectDictionariesDto.DictionaryItem> loadUniversities() {
        Query query = entityManager.createNativeQuery(
            "SELECT code, name FROM hemishe_e_university WHERE delete_ts IS NULL ORDER BY name");
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
            .map(row -> new ProjectDictionariesDto.DictionaryItem((String) row[0], (String) row[1]))
            .collect(Collectors.toList());
    }

    private List<ProjectDictionariesDto.DictionaryItem> loadClassifier(String table) {
        // Table name is a fixed literal (not user input) — safe to inline.
        String sql = "SELECT code, name FROM " + table +
                     " WHERE delete_ts IS NULL AND active = true ORDER BY name";
        Query query = entityManager.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
            .map(row -> new ProjectDictionariesDto.DictionaryItem((String) row[0], (String) row[1]))
            .collect(Collectors.toList());
    }

    private static int bind(Query query, List<Object> params, int start) {
        int i = start;
        for (Object p : params) {
            query.setParameter(i++, p);
        }
        return i;
    }

    private static String label(Object name, Object code) {
        if (name != null) return (String) name;
        return code != null ? code.toString() : null;
    }

    private static String str(Object o) {
        return o != null ? o.toString() : null;
    }

    private static LocalDate toLocalDate(Object o) {
        if (o == null) return null;
        if (o instanceof LocalDate ld) return ld;
        if (o instanceof java.sql.Date d) return d.toLocalDate();
        if (o instanceof java.sql.Timestamp ts) return ts.toLocalDateTime().toLocalDate();
        return null;
    }
}
