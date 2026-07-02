package uz.hemis.service.registry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.service.registry.dto.PublicationPropertyDetailDto;
import uz.hemis.service.registry.dto.PublicationPropertyDictionariesDto;
import uz.hemis.service.registry.dto.PublicationPropertyRowDto;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Intellectual Property Registry Service - READ-ONLY business logic.
 *
 * <p>Source table {@code hemishe_e_publication_property} read via {@link EntityManager} native
 * queries. Soft-delete enforced with explicit {@code AND t.delete_ts IS NULL}. University and
 * classifier tables LEFT-JOINed with their own {@code delete_ts IS NULL} guard and a raw-code
 * fallback. The {@code _patent_type} classifier has NO reference table — the raw code is used
 * as the label, and the patent-types dictionary is built from distinct codes present in the
 * table. {@code _translations} is never selected.</p>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PublicationPropertyRegistryService {

    private final EntityManager entityManager;

    private static final String BASE_FROM =
        "FROM hemishe_e_publication_property t " +
        "LEFT JOIN hemishe_e_university u ON u.code = t._university AND u.delete_ts IS NULL " +
        "LEFT JOIN hemishe_h_publication_database pd ON pd.code = t._publication_database AND pd.delete_ts IS NULL ";

    private static final String ROW_COLS =
        "t.id, t.name, t.authors, t.author_counts, t._university, u.name, " +
        "t._patent_type, t.numbers, t.property_date, t._country, t.active ";

    public Page<PublicationPropertyRowDto> getProperties(String q, String universityCode, String patentType,
                                                         Boolean active, Pageable pageable) {
        log.debug("Getting intellectual property: q={}, universityCode={}, patentType={}, active={}, page={}",
                  q, universityCode, patentType, active, pageable.getPageNumber());

        List<Object> params = new ArrayList<>();
        String where = buildWhere(q, universityCode, patentType, active, params);

        String countSql = "SELECT COUNT(*) " + BASE_FROM + where;
        Query countQuery = entityManager.createNativeQuery(countSql);
        bind(countQuery, params, 1);
        long total = ((Number) countQuery.getSingleResult()).longValue();
        if (total == 0) {
            return Page.empty(pageable);
        }

        String dataSql = "SELECT " + ROW_COLS + BASE_FROM + where +
            "ORDER BY t.property_date DESC NULLS LAST, t.id LIMIT ? OFFSET ?";
        Query dataQuery = entityManager.createNativeQuery(dataSql);
        int idx = bind(dataQuery, params, 1);
        dataQuery.setParameter(idx, pageable.getPageSize());
        dataQuery.setParameter(idx + 1, pageable.getOffset());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();
        List<PublicationPropertyRowDto> content = rows.stream().map(this::mapRow).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, total);
    }

    public Optional<PublicationPropertyDetailDto> getPropertyDetail(String id) {
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
        PublicationPropertyRowDto row = mapRow(r);
        return Optional.of(new PublicationPropertyDetailDto(
            row.id(), row.name(), row.authors(), row.authorCounts(), row.universityCode(), row.universityName(),
            row.patentTypeCode(), row.patentTypeName(), row.numbers(), row.propertyDate(), row.countryCode(), row.active(),
            (String) r[11], (String) r[12], label(r[13], r[12]), (String) r[14], (Boolean) r[15]
        ));
    }

    @Cacheable(value = "intellectualDictionaries", key = "'all'")
    public PublicationPropertyDictionariesDto getDictionaries() {
        log.debug("Loading intellectual-property dictionaries");
        return new PublicationPropertyDictionariesDto(
            loadUniversities(),
            loadPatentTypes()
        );
    }

    // ---- helpers ----

    private String buildWhere(String q, String universityCode, String patentType,
                              Boolean active, List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE t.delete_ts IS NULL ");
        String like = (q != null && !q.isBlank()) ? "%" + q.trim().toLowerCase() + "%" : null;
        if (like != null) {
            where.append("AND (LOWER(t.name) LIKE ? OR LOWER(t.authors) LIKE ? OR LOWER(t.numbers) LIKE ?) ");
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (universityCode != null && !universityCode.isBlank()) {
            where.append("AND t._university = ? ");
            params.add(universityCode.trim());
        }
        if (patentType != null && !patentType.isBlank()) {
            where.append("AND t._patent_type = ? ");
            params.add(patentType.trim());
        }
        if (active != null) {
            where.append("AND t.active = ? ");
            params.add(active);
        }
        return where.toString();
    }

    private PublicationPropertyRowDto mapRow(Object[] r) {
        return new PublicationPropertyRowDto(
            str(r[0]), (String) r[1], (String) r[2], toInteger(r[3]), (String) r[4], (String) r[5],
            (String) r[6], (String) r[6], (String) r[7], toLocalDate(r[8]), (String) r[9], (Boolean) r[10]
        );
    }

    private List<PublicationPropertyDictionariesDto.DictionaryItem> loadUniversities() {
        Query query = entityManager.createNativeQuery(
            "SELECT code, name FROM hemishe_e_university WHERE delete_ts IS NULL ORDER BY name");
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
            .map(row -> new PublicationPropertyDictionariesDto.DictionaryItem((String) row[0], (String) row[1]))
            .collect(Collectors.toList());
    }

    /** No patent-type classifier reference table exists — build from distinct raw codes. */
    private List<PublicationPropertyDictionariesDto.DictionaryItem> loadPatentTypes() {
        Query query = entityManager.createNativeQuery(
            "SELECT DISTINCT _patent_type FROM hemishe_e_publication_property " +
            "WHERE delete_ts IS NULL AND _patent_type IS NOT NULL AND _patent_type <> '' ORDER BY _patent_type");
        @SuppressWarnings("unchecked")
        List<Object> rows = query.getResultList();
        return rows.stream()
            .map(code -> new PublicationPropertyDictionariesDto.DictionaryItem((String) code, (String) code))
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

    private static LocalDate toLocalDate(Object o) {
        if (o == null) return null;
        if (o instanceof LocalDate ld) return ld;
        if (o instanceof java.sql.Date d) return d.toLocalDate();
        if (o instanceof java.sql.Timestamp ts) return ts.toLocalDateTime().toLocalDate();
        return null;
    }
}
