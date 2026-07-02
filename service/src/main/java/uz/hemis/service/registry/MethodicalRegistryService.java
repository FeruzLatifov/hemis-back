package uz.hemis.service.registry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.service.registry.dto.MethodicalDetailDto;
import uz.hemis.service.registry.dto.MethodicalDictionariesDto;
import uz.hemis.service.registry.dto.MethodicalRowDto;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Methodical Publication Registry Service - READ-ONLY business logic.
 *
 * <p>Source table {@code hemishe_e_publication_methodical}. Read via {@link EntityManager}
 * native queries. Soft-delete enforced with explicit {@code AND t.delete_ts IS NULL}.
 * University and classifier tables LEFT-JOINed with their own {@code delete_ts IS NULL}
 * guard and a raw-code fallback when the classifier row is missing. {@code _translations}
 * is never selected.</p>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MethodicalRegistryService {

    private final EntityManager entityManager;

    private static final String BASE_FROM =
        "FROM hemishe_e_publication_methodical t " +
        "LEFT JOIN hemishe_e_university u ON u.code = t._university AND u.delete_ts IS NULL " +
        "LEFT JOIN hemishe_h_methodical_publication_type mt ON mt.code = t._methodical_publication_type AND mt.delete_ts IS NULL " +
        "LEFT JOIN hemishe_h_publication_database pd ON pd.code = t._publication_database AND pd.delete_ts IS NULL ";

    private static final String ROW_COLS =
        "t.id, t.name, t.authors, t.author_counts, t.publisher, t.issue_year, t.source_name, " +
        "t._university, u.name, t._methodical_publication_type, mt.name, t.active ";

    public Page<MethodicalRowDto> getMethodicals(String q, String universityCode, String methodicalType,
                                                 Integer issueYear, Boolean active, Pageable pageable) {
        log.debug("Getting methodicals: q={}, universityCode={}, methodicalType={}, issueYear={}, active={}, page={}",
                  q, universityCode, methodicalType, issueYear, active, pageable.getPageNumber());

        List<Object> params = new ArrayList<>();
        String where = buildWhere(q, universityCode, methodicalType, issueYear, active, params);

        String countSql = "SELECT COUNT(*) " + BASE_FROM + where;
        Query countQuery = entityManager.createNativeQuery(countSql);
        bind(countQuery, params, 1);
        long total = ((Number) countQuery.getSingleResult()).longValue();
        if (total == 0) {
            return Page.empty(pageable);
        }

        String dataSql = "SELECT " + ROW_COLS + BASE_FROM + where +
            "ORDER BY t.issue_year DESC NULLS LAST, t.id LIMIT ? OFFSET ?";
        Query dataQuery = entityManager.createNativeQuery(dataSql);
        int idx = bind(dataQuery, params, 1);
        dataQuery.setParameter(idx, pageable.getPageSize());
        dataQuery.setParameter(idx + 1, pageable.getOffset());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();
        List<MethodicalRowDto> content = rows.stream().map(this::mapRow).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, total);
    }

    public Optional<MethodicalDetailDto> getMethodicalDetail(String id) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }

        String sql = "SELECT " + ROW_COLS +
            ", t.parameter, t._publication_database, pd.name, t._education_year, t.is_checked " +
            BASE_FROM + "WHERE t.delete_ts IS NULL AND t.id = ?";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, uuid);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Object[] r = rows.get(0);
        MethodicalRowDto row = mapRow(r);
        return Optional.of(new MethodicalDetailDto(
            row.id(), row.name(), row.authors(), row.authorCounts(), row.publisher(), row.issueYear(),
            row.sourceName(), row.universityCode(), row.universityName(), row.methodicalTypeCode(),
            row.methodicalTypeName(), row.active(),
            (String) r[12], (String) r[13], label(r[14], r[13]), (String) r[15], (Boolean) r[16]
        ));
    }

    @Cacheable(value = "methodicalDictionaries", key = "'all'")
    public MethodicalDictionariesDto getDictionaries() {
        log.debug("Loading methodical publication dictionaries");
        return new MethodicalDictionariesDto(
            loadUniversities(),
            loadClassifier("hemishe_h_methodical_publication_type")
        );
    }

    // ---- helpers ----

    private String buildWhere(String q, String universityCode, String methodicalType,
                              Integer issueYear, Boolean active, List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE t.delete_ts IS NULL ");
        String like = (q != null && !q.isBlank()) ? "%" + q.trim().toLowerCase() + "%" : null;
        if (like != null) {
            where.append("AND (LOWER(t.name) LIKE ? OR LOWER(t.authors) LIKE ? " +
                         "OR LOWER(t.publisher) LIKE ?) ");
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (universityCode != null && !universityCode.isBlank()) {
            where.append("AND t._university = ? ");
            params.add(universityCode.trim());
        }
        if (methodicalType != null && !methodicalType.isBlank()) {
            where.append("AND t._methodical_publication_type = ? ");
            params.add(methodicalType.trim());
        }
        if (issueYear != null) {
            where.append("AND t.issue_year = ? ");
            params.add(issueYear);
        }
        if (active != null) {
            where.append("AND t.active = ? ");
            params.add(active);
        }
        return where.toString();
    }

    private MethodicalRowDto mapRow(Object[] r) {
        return new MethodicalRowDto(
            str(r[0]), (String) r[1], (String) r[2], toInteger(r[3]), (String) r[4], toInteger(r[5]),
            (String) r[6], (String) r[7], (String) r[8], (String) r[9], label(r[10], r[9]), (Boolean) r[11]
        );
    }

    private List<MethodicalDictionariesDto.DictionaryItem> loadUniversities() {
        Query query = entityManager.createNativeQuery(
            "SELECT code, name FROM hemishe_e_university WHERE delete_ts IS NULL ORDER BY name");
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
            .map(row -> new MethodicalDictionariesDto.DictionaryItem((String) row[0], (String) row[1]))
            .collect(Collectors.toList());
    }

    private List<MethodicalDictionariesDto.DictionaryItem> loadClassifier(String table) {
        // Table name is a fixed literal (not user input) — safe to inline.
        String sql = "SELECT code, name FROM " + table +
                     " WHERE delete_ts IS NULL AND active = true ORDER BY name";
        Query query = entityManager.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
            .map(row -> new MethodicalDictionariesDto.DictionaryItem((String) row[0], (String) row[1]))
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

    private static Integer toInteger(Object o) {
        return o != null ? ((Number) o).intValue() : null;
    }
}
