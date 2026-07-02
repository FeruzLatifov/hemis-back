package uz.hemis.service.registry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.service.registry.dto.SpecialityDetailDto;
import uz.hemis.service.registry.dto.SpecialityDictionariesDto;
import uz.hemis.service.registry.dto.SpecialityRowDto;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * University Speciality Registry Service - READ-ONLY business logic.
 *
 * <p>Source table {@code hemishe_e_university_speciality} (entity {@code Specialty}) read via
 * {@link EntityManager} native queries. This table has NO {@code delete_ts} column, so NO
 * soft-delete filter is applied on the base table (distinct from the attached-specialities
 * card). University and classifier tables LEFT-JOINed with their own {@code delete_ts IS NULL}
 * guard and a raw-code fallback when the classifier row is missing.</p>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UniversitySpecialityRegistryService {

    private final EntityManager entityManager;

    private static final String BASE_FROM =
        "FROM hemishe_e_university_speciality t " +
        "LEFT JOIN hemishe_e_university u ON u.code = t._university AND u.delete_ts IS NULL " +
        "LEFT JOIN hemishe_h_education_type et ON et.code = t._education_type AND et.delete_ts IS NULL ";

    private static final String ROW_COLS =
        "t.id, t._university, u.name, t.speciality_code, t.speciality_name, " +
        "t._education_type, et.name, t._education_year, t._faculty, t.active ";

    public Page<SpecialityRowDto> getSpecialities(String q, String universityCode, String educationType,
                                                  Boolean active, Pageable pageable) {
        log.debug("Getting university specialities: q={}, universityCode={}, educationType={}, active={}, page={}",
                  q, universityCode, educationType, active, pageable.getPageNumber());

        List<Object> params = new ArrayList<>();
        String where = buildWhere(q, universityCode, educationType, active, params);

        String countSql = "SELECT COUNT(*) " + BASE_FROM + where;
        Query countQuery = entityManager.createNativeQuery(countSql);
        bind(countQuery, params, 1);
        long total = ((Number) countQuery.getSingleResult()).longValue();
        if (total == 0) {
            return Page.empty(pageable);
        }

        String dataSql = "SELECT " + ROW_COLS + BASE_FROM + where +
            "ORDER BY t.speciality_name NULLS LAST, t.id LIMIT ? OFFSET ?";
        Query dataQuery = entityManager.createNativeQuery(dataSql);
        int idx = bind(dataQuery, params, 1);
        dataQuery.setParameter(idx, pageable.getPageSize());
        dataQuery.setParameter(idx + 1, pageable.getOffset());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();
        List<SpecialityRowDto> content = rows.stream().map(this::mapRow).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, total);
    }

    public Optional<SpecialityDetailDto> getSpecialityDetail(String id) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }

        String sql = "SELECT " + ROW_COLS + BASE_FROM + "WHERE t.id = ?";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, uuid);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Object[] r = rows.get(0);
        return Optional.of(new SpecialityDetailDto(
            str(r[0]), (String) r[1], (String) r[2], (String) r[3], (String) r[4],
            (String) r[5], label(r[6], r[5]), (String) r[7], (String) r[8], (Boolean) r[9]
        ));
    }

    @Cacheable(value = "universitySpecialitiesDictionaries", key = "'all'")
    public SpecialityDictionariesDto getDictionaries() {
        log.debug("Loading university-speciality dictionaries");
        return new SpecialityDictionariesDto(
            loadUniversities(),
            loadClassifier("hemishe_h_education_type")
        );
    }

    // ---- helpers ----

    private String buildWhere(String q, String universityCode, String educationType,
                              Boolean active, List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE 1=1 ");
        String like = (q != null && !q.isBlank()) ? "%" + q.trim().toLowerCase() + "%" : null;
        if (like != null) {
            where.append("AND (LOWER(t.speciality_code) LIKE ? OR LOWER(t.speciality_name) LIKE ?) ");
            params.add(like);
            params.add(like);
        }
        if (universityCode != null && !universityCode.isBlank()) {
            where.append("AND t._university = ? ");
            params.add(universityCode.trim());
        }
        if (educationType != null && !educationType.isBlank()) {
            where.append("AND t._education_type = ? ");
            params.add(educationType.trim());
        }
        if (active != null) {
            where.append("AND t.active = ? ");
            params.add(active);
        }
        return where.toString();
    }

    private SpecialityRowDto mapRow(Object[] r) {
        return new SpecialityRowDto(
            str(r[0]), (String) r[1], (String) r[2], (String) r[3], (String) r[4],
            (String) r[5], label(r[6], r[5]), (String) r[7], (String) r[8], (Boolean) r[9]
        );
    }

    private List<SpecialityDictionariesDto.DictionaryItem> loadUniversities() {
        Query query = entityManager.createNativeQuery(
            "SELECT code, name FROM hemishe_e_university WHERE delete_ts IS NULL ORDER BY name");
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
            .map(row -> new SpecialityDictionariesDto.DictionaryItem((String) row[0], (String) row[1]))
            .collect(Collectors.toList());
    }

    private List<SpecialityDictionariesDto.DictionaryItem> loadClassifier(String table) {
        // Table name is a fixed literal (not user input) — safe to inline.
        String sql = "SELECT code, name FROM " + table +
                     " WHERE delete_ts IS NULL AND active = true ORDER BY name";
        Query query = entityManager.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
            .map(row -> new SpecialityDictionariesDto.DictionaryItem((String) row[0], (String) row[1]))
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
