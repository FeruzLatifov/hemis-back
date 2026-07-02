package uz.hemis.service.registry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.service.registry.dto.ResearchActivityDetailDto;
import uz.hemis.service.registry.dto.ResearchActivityDictionariesDto;
import uz.hemis.service.registry.dto.ResearchActivityRowDto;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Research Activity Registry Service - READ-ONLY business logic.
 *
 * <p>Source table {@code hemishe_e_research_activity} read via {@link EntityManager} native
 * queries. Soft-delete enforced with explicit {@code AND t.delete_ts IS NULL}. University and
 * classifier tables LEFT-JOINed with their own {@code delete_ts IS NULL} guard and a raw-code
 * fallback when the classifier row is missing.</p>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ResearchActivityRegistryService {

    private final EntityManager entityManager;

    private static final String BASE_FROM =
        "FROM hemishe_e_research_activity t " +
        "LEFT JOIN hemishe_e_university u ON u.code = t._university AND u.delete_ts IS NULL " +
        "LEFT JOIN hemishe_h_scholar_database sd ON sd.code = t._scholar_database AND sd.delete_ts IS NULL ";

    private static final String ROW_COLS =
        "t.id, t._university, u.name, t._education_year, t._scholar_database, sd.name, " +
        "t.h_index, t.scientific_work_count, t.reference_count, t.link ";

    public Page<ResearchActivityRowDto> getActivities(String q, String universityCode, String educationYear,
                                                      Pageable pageable) {
        log.debug("Getting research activities: q={}, universityCode={}, educationYear={}, page={}",
                  q, universityCode, educationYear, pageable.getPageNumber());

        List<Object> params = new ArrayList<>();
        String where = buildWhere(q, universityCode, educationYear, params);

        String countSql = "SELECT COUNT(*) " + BASE_FROM + where;
        Query countQuery = entityManager.createNativeQuery(countSql);
        bind(countQuery, params, 1);
        long total = ((Number) countQuery.getSingleResult()).longValue();
        if (total == 0) {
            return Page.empty(pageable);
        }

        String dataSql = "SELECT " + ROW_COLS + BASE_FROM + where +
            "ORDER BY t._education_year DESC NULLS LAST, t.id LIMIT ? OFFSET ?";
        Query dataQuery = entityManager.createNativeQuery(dataSql);
        int idx = bind(dataQuery, params, 1);
        dataQuery.setParameter(idx, pageable.getPageSize());
        dataQuery.setParameter(idx + 1, pageable.getOffset());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();
        List<ResearchActivityRowDto> content = rows.stream().map(this::mapRow).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, total);
    }

    public Optional<ResearchActivityDetailDto> getActivityDetail(String id) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }

        String sql = "SELECT " + ROW_COLS + BASE_FROM + "WHERE t.delete_ts IS NULL AND t.id = ?";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, uuid);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Object[] r = rows.get(0);
        return Optional.of(new ResearchActivityDetailDto(
            str(r[0]), (String) r[1], (String) r[2], (String) r[3], (String) r[4], label(r[5], r[4]),
            (String) r[6], (String) r[7], (String) r[8], (String) r[9]
        ));
    }

    @Cacheable(value = "researchActivityDictionaries", key = "'all'")
    public ResearchActivityDictionariesDto getDictionaries() {
        log.debug("Loading research-activity dictionaries");
        return new ResearchActivityDictionariesDto(
            loadUniversities(),
            loadClassifier("hemishe_h_scholar_database")
        );
    }

    // ---- helpers ----

    private String buildWhere(String q, String universityCode, String educationYear, List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE t.delete_ts IS NULL ");
        String like = (q != null && !q.isBlank()) ? "%" + q.trim().toLowerCase() + "%" : null;
        if (like != null) {
            where.append("AND (LOWER(t.link) LIKE ? OR LOWER(t.h_index) LIKE ?) ");
            params.add(like);
            params.add(like);
        }
        if (universityCode != null && !universityCode.isBlank()) {
            where.append("AND t._university = ? ");
            params.add(universityCode.trim());
        }
        if (educationYear != null && !educationYear.isBlank()) {
            where.append("AND t._education_year = ? ");
            params.add(educationYear.trim());
        }
        return where.toString();
    }

    private ResearchActivityRowDto mapRow(Object[] r) {
        return new ResearchActivityRowDto(
            str(r[0]), (String) r[1], (String) r[2], (String) r[3], (String) r[4], label(r[5], r[4]),
            (String) r[6], (String) r[7], (String) r[8], (String) r[9]
        );
    }

    private List<ResearchActivityDictionariesDto.DictionaryItem> loadUniversities() {
        Query query = entityManager.createNativeQuery(
            "SELECT code, name FROM hemishe_e_university WHERE delete_ts IS NULL ORDER BY name");
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
            .map(row -> new ResearchActivityDictionariesDto.DictionaryItem((String) row[0], (String) row[1]))
            .collect(Collectors.toList());
    }

    private List<ResearchActivityDictionariesDto.DictionaryItem> loadClassifier(String table) {
        // Table name is a fixed literal (not user input) — safe to inline.
        String sql = "SELECT code, name FROM " + table +
                     " WHERE delete_ts IS NULL AND active = true ORDER BY name";
        Query query = entityManager.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
            .map(row -> new ResearchActivityDictionariesDto.DictionaryItem((String) row[0], (String) row[1]))
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
}
