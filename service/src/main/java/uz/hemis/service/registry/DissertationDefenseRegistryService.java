package uz.hemis.service.registry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.service.registry.dto.DissertationDefenseDetailDto;
import uz.hemis.service.registry.dto.DissertationDefenseDictionariesDto;
import uz.hemis.service.registry.dto.DissertationDefenseRowDto;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Dissertation Defense Registry Service - READ-ONLY business logic.
 *
 * <p>Source table {@code hemishe_e_dissertation_defense} read via {@link EntityManager} native
 * queries. Soft-delete enforced with explicit {@code AND t.delete_ts IS NULL}. The owner and
 * university are resolved INDIRECTLY: {@code _doctorate_student} (UUID) →
 * {@code hemishe_e_doctorate_student.id} → its {@code _university} → {@code hemishe_e_university}.
 * The student is shown by full name only (PII-safe); {@code _translations}, passport columns
 * are never selected.</p>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DissertationDefenseRegistryService {

    private final EntityManager entityManager;

    /** Student full name (PII-free). CONCAT_WS skips nulls. */
    private static final String STUDENT_NAME = "CONCAT_WS(' ', ds.second_name, ds.first_name, ds.third_name)";

    private static final String BASE_FROM =
        "FROM hemishe_e_dissertation_defense t " +
        "LEFT JOIN hemishe_e_doctorate_student ds ON ds.id = t._doctorate_student AND ds.delete_ts IS NULL " +
        "LEFT JOIN hemishe_e_university u ON u.code = ds._university AND u.delete_ts IS NULL ";

    private static final String ROW_COLS =
        "t.id, t._doctorate_student, " + STUDENT_NAME + ", ds._university, u.name, " +
        "t._speciality, t.defense_date, t.diploma_number, t.register_number, t.approved_date, t.active ";

    public Page<DissertationDefenseRowDto> getDefenses(String q, String universityCode,
                                                       Boolean active, Pageable pageable) {
        log.debug("Getting dissertation defenses: q={}, universityCode={}, active={}, page={}",
                  q, universityCode, active, pageable.getPageNumber());

        List<Object> params = new ArrayList<>();
        String where = buildWhere(q, universityCode, active, params);

        String countSql = "SELECT COUNT(*) " + BASE_FROM + where;
        Query countQuery = entityManager.createNativeQuery(countSql);
        bind(countQuery, params, 1);
        long total = ((Number) countQuery.getSingleResult()).longValue();
        if (total == 0) {
            return Page.empty(pageable);
        }

        String dataSql = "SELECT " + ROW_COLS + BASE_FROM + where +
            "ORDER BY t.defense_date DESC NULLS LAST, t.id LIMIT ? OFFSET ?";
        Query dataQuery = entityManager.createNativeQuery(dataSql);
        int idx = bind(dataQuery, params, 1);
        dataQuery.setParameter(idx, pageable.getPageSize());
        dataQuery.setParameter(idx + 1, pageable.getOffset());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();
        List<DissertationDefenseRowDto> content = rows.stream().map(this::mapRow).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, total);
    }

    public Optional<DissertationDefenseDetailDto> getDefenseDetail(String id) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }

        String sql = "SELECT " + ROW_COLS +
            ", t.defense_place, t.diploma_given_date, t.diploma_given_by_whom " +
            BASE_FROM + "WHERE t.delete_ts IS NULL AND t.id = ?";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, uuid);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Object[] r = rows.get(0);
        DissertationDefenseRowDto row = mapRow(r);
        return Optional.of(new DissertationDefenseDetailDto(
            row.id(), row.doctorateStudentId(), row.studentName(), row.universityCode(), row.universityName(),
            row.specialityCode(), row.defenseDate(), row.diplomaNumber(), row.registerNumber(),
            row.approvedDate(), row.active(),
            (String) r[11], toLocalDate(r[12]), (String) r[13]
        ));
    }

    @Cacheable(value = "dissertationDefenseDictionaries", key = "'all'")
    public DissertationDefenseDictionariesDto getDictionaries() {
        log.debug("Loading dissertation-defense dictionaries");
        return new DissertationDefenseDictionariesDto(loadUniversities());
    }

    // ---- helpers ----

    private String buildWhere(String q, String universityCode, Boolean active, List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE t.delete_ts IS NULL ");
        String like = (q != null && !q.isBlank()) ? "%" + q.trim().toLowerCase() + "%" : null;
        if (like != null) {
            where.append("AND (LOWER(t.diploma_number) LIKE ? OR LOWER(t.register_number) LIKE ? " +
                         "OR LOWER(" + STUDENT_NAME + ") LIKE ?) ");
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (universityCode != null && !universityCode.isBlank()) {
            where.append("AND ds._university = ? ");
            params.add(universityCode.trim());
        }
        if (active != null) {
            where.append("AND t.active = ? ");
            params.add(active);
        }
        return where.toString();
    }

    private DissertationDefenseRowDto mapRow(Object[] r) {
        return new DissertationDefenseRowDto(
            str(r[0]), str(r[1]), (String) r[2], (String) r[3], (String) r[4],
            str(r[5]), toLocalDate(r[6]), (String) r[7], (String) r[8], toLocalDate(r[9]), (Boolean) r[10]
        );
    }

    private List<DissertationDefenseDictionariesDto.DictionaryItem> loadUniversities() {
        Query query = entityManager.createNativeQuery(
            "SELECT code, name FROM hemishe_e_university WHERE delete_ts IS NULL ORDER BY name");
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
            .map(row -> new DissertationDefenseDictionariesDto.DictionaryItem((String) row[0], (String) row[1]))
            .collect(Collectors.toList());
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

    private static LocalDate toLocalDate(Object o) {
        if (o == null) return null;
        if (o instanceof LocalDate ld) return ld;
        if (o instanceof java.sql.Date d) return d.toLocalDate();
        if (o instanceof java.sql.Timestamp ts) return ts.toLocalDateTime().toLocalDate();
        return null;
    }
}
