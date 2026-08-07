package uz.hemis.service.registry;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.service.registry.dto.DiplomaBlankDetailDto;
import uz.hemis.service.registry.dto.DiplomaBlankDictionariesDto;
import uz.hemis.service.registry.dto.DiplomaBlankRowDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Diploma Blank Registry Service — READ-ONLY registry over {@code hemishe_e_diploma_blank}.
 *
 * <p>CENTRAL read-only card. The ministry manages blank distribution centrally
 * (see {@code DiplomaBlankDistributionService}); this card is the read-only inventory
 * of the printed blank forms themselves.</p>
 *
 * <p>Uses {@link EntityManager} native queries (mirroring {@code DiplomaRegistryService}).
 * Native SQL bypasses {@code @SQLRestriction}, so {@code b.delete_ts IS NULL} is added
 * manually; the university is LEFT-JOINed with its own {@code delete_ts IS NULL} guard.</p>
 *
 * <p><strong>NOTE:</strong> the source table {@code hemishe_e_diploma_blank} is currently
 * NOT provisioned centrally (no migration and absent from the dev DB). Until it exists the
 * card renders empty; the endpoint contract is nonetheless complete and correct.</p>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DiplomaBlankRegistryService {

    private final EntityManager entityManager;

    private static final String BASE_FROM =
        "FROM hemishe_e_diploma_blank b " +
        "LEFT JOIN hemishe_e_university u ON u.code = b._university AND u.delete_ts IS NULL ";

    /**
     * Paged diploma-blank list.
     *
     * @param q              search (blank_code / series / number)
     * @param universityCode filter by university code
     * @param status         filter by status code
     * @param pageable       pagination
     */
    public Page<DiplomaBlankRowDto> getBlanks(String q, String universityCode, String status, Pageable pageable) {
        log.debug("Getting diploma blanks: q={}, universityCode={}, status={}, page={}",
                  q, universityCode, status, pageable.getPageNumber());

        List<Object> params = new ArrayList<>();
        String where = buildWhere(q, universityCode, status, params);

        String countSql = "SELECT COUNT(*) " + BASE_FROM + where;
        Query countQuery = entityManager.createNativeQuery(countSql);
        bind(countQuery, params, 1);
        long total = ((Number) countQuery.getSingleResult()).longValue();
        if (total == 0) {
            return Page.empty(pageable);
        }

        String dataSql =
            "SELECT b.id, b.blank_code, b.series, b.\"number\", b._university, u.name, " +
            "  b._blank_type, b._status, b.received_date, b.issued_date, b.academic_year, (b.delete_ts IS NULL) " +
            BASE_FROM + where +
            "ORDER BY b.received_date DESC NULLS LAST, b.id " +
            "LIMIT ? OFFSET ?";

        Query dataQuery = entityManager.createNativeQuery(dataSql);
        int idx = bind(dataQuery, params, 1);
        dataQuery.setParameter(idx, pageable.getPageSize());
        dataQuery.setParameter(idx + 1, pageable.getOffset());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();
        List<DiplomaBlankRowDto> content = rows.stream().map(this::mapRow).toList();
        return new PageImpl<>(content, pageable, total);
    }

    /** Single diploma-blank detail by id (UUID). Empty if not found or id is not a valid UUID. */
    public Optional<DiplomaBlankDetailDto> getBlankDetail(String id) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }

        String sql =
            "SELECT b.id, b.blank_code, b.series, b.\"number\", b._university, u.name, " +
            "  b._blank_type, b._status, b.received_date, b.issued_date, b.academic_year, (b.delete_ts IS NULL), " +
            "  b.supplier, b.batch_number, b.status_reason " +
            BASE_FROM +
            "WHERE b.delete_ts IS NULL AND b.id = ?";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, uuid);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Object[] r = rows.get(0);
        return Optional.of(new DiplomaBlankDetailDto(
            str(r[0]), (String) r[1], (String) r[2], (String) r[3], (String) r[4], (String) r[5],
            (String) r[6], (String) r[7], toLocalDate(r[8]), toLocalDate(r[9]), toInt(r[10]), (Boolean) r[11],
            (String) r[12], (String) r[13], (String) r[14]
        ));
    }

    /** Filter dictionaries (cached). */
    @Cacheable(value = "diplomaBlanksDictionaries", key = "'all'", unless = "#result == null")
    public DiplomaBlankDictionariesDto getDictionaries() {
        log.debug("Loading diploma-blank dictionaries");
        return new DiplomaBlankDictionariesDto(loadUniversities(), loadStatuses());
    }

    // ---- helpers ----

    private String buildWhere(String q, String universityCode, String status, List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE b.delete_ts IS NULL ");
        String searchLike = (q != null && !q.isBlank()) ? "%" + q.trim().toLowerCase() + "%" : null;
        if (searchLike != null) {
            where.append("AND (LOWER(b.blank_code) LIKE ? OR LOWER(b.series) LIKE ? OR LOWER(b.\"number\") LIKE ?) ");
            params.add(searchLike);
            params.add(searchLike);
            params.add(searchLike);
        }
        if (universityCode != null && !universityCode.isBlank()) {
            where.append("AND b._university = ? ");
            params.add(universityCode.trim());
        }
        if (status != null && !status.isBlank()) {
            where.append("AND b._status = ? ");
            params.add(status.trim());
        }
        return where.toString();
    }

    private DiplomaBlankRowDto mapRow(Object[] r) {
        return new DiplomaBlankRowDto(
            str(r[0]), (String) r[1], (String) r[2], (String) r[3], (String) r[4], (String) r[5],
            (String) r[6], (String) r[7], toLocalDate(r[8]), toLocalDate(r[9]), toInt(r[10]), (Boolean) r[11]
        );
    }

    private List<DiplomaBlankDictionariesDto.DictionaryItem> loadUniversities() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery(
            "SELECT code, name FROM hemishe_e_university WHERE delete_ts IS NULL ORDER BY name").getResultList();
        return rows.stream()
            .map(row -> new DiplomaBlankDictionariesDto.DictionaryItem((String) row[0], (String) row[1]))
            .toList();
    }

    /** Distinct status codes present in the blank inventory (code == name — no status classifier table). */
    private List<DiplomaBlankDictionariesDto.DictionaryItem> loadStatuses() {
        @SuppressWarnings("unchecked")
        List<Object> rows = entityManager.createNativeQuery(
            "SELECT DISTINCT _status FROM hemishe_e_diploma_blank " +
            "WHERE delete_ts IS NULL AND _status IS NOT NULL ORDER BY _status")
            .getResultList();
        return rows.stream()
            .map(o -> new DiplomaBlankDictionariesDto.DictionaryItem((String) o, (String) o))
            .toList();
    }

    private static int bind(Query query, List<Object> params, int start) {
        int i = start;
        for (Object p : params) {
            query.setParameter(i++, p);
        }
        return i;
    }

    private static String str(Object o) {
        return o != null ? o.toString() : null;
    }

    private static Integer toInt(Object o) {
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
