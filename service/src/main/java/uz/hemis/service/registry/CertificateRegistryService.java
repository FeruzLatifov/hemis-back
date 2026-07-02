package uz.hemis.service.registry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.service.registry.dto.CertificateDetailDto;
import uz.hemis.service.registry.dto.CertificateDictionariesDto;
import uz.hemis.service.registry.dto.CertificateRowDto;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Certificate Registry Service - READ-ONLY business logic for the Certificates
 * (Sertifikatlar) registry. Source table {@code hemishe_e_student_certificate} read via
 * {@link EntityManager} native queries.
 *
 * <p>Classifier labels are resolved via LEFT JOIN (each with its own {@code delete_ts IS NULL}
 * guard), falling back to the raw code when a label is missing. Confirmed classifier tables
 * (from the JPA classifier entities): {@code hemishe_h_certificate_type},
 * {@code hemishe_h_certificate_names}, {@code hemishe_h_certificate_grades},
 * {@code hemishe_h_certificate_subjects} — all with {@code code}/{@code name} columns.</p>
 *
 * <p>Soft-delete enforced with explicit {@code AND c.delete_ts IS NULL}. University and
 * student LEFT-JOINed with their own guard. PINFL never exposed.</p>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CertificateRegistryService {

    private final EntityManager entityManager;

    private static final String FULL_NAME = "CONCAT_WS(' ', s.lastname, s.firstname, s.fathername)";

    private static final String BASE_FROM =
        "FROM hemishe_e_student_certificate c " +
        "LEFT JOIN hemishe_e_university u ON u.code = c._university AND u.delete_ts IS NULL " +
        "LEFT JOIN hemishe_e_student s ON s.id = c._student AND s.delete_ts IS NULL " +
        "LEFT JOIN hemishe_h_certificate_type ct ON ct.code = c._certificate_type AND ct.delete_ts IS NULL " +
        "LEFT JOIN hemishe_h_certificate_names cn ON cn.code = c._certificate_name AND cn.delete_ts IS NULL " +
        "LEFT JOIN hemishe_h_certificate_grades cg ON cg.code = c._certificate_grade AND cg.delete_ts IS NULL ";

    private static final String SUBJECT_JOIN =
        "LEFT JOIN hemishe_h_certificate_subjects cs ON cs.code = c._certificate_subject AND cs.delete_ts IS NULL ";

    /**
     * Paged certificate list.
     *
     * @param q               search (serial_number / student full name)
     * @param universityCode  filter by university code
     * @param certificateType filter by certificate-type classifier code
     * @param pageable        pagination
     */
    public Page<CertificateRowDto> getCertificates(String q, String universityCode,
                                                   String certificateType, Pageable pageable) {
        log.debug("Getting certificates: q={}, universityCode={}, certificateType={}, page={}",
                  q, universityCode, certificateType, pageable.getPageNumber());

        List<Object> params = new ArrayList<>();
        String where = buildWhere(q, universityCode, certificateType, params);

        String countSql = "SELECT COUNT(*) " + BASE_FROM + where;
        Query countQuery = entityManager.createNativeQuery(countSql);
        bind(countQuery, params, 1);
        long total = ((Number) countQuery.getSingleResult()).longValue();
        if (total == 0) {
            return Page.empty(pageable);
        }

        String dataSql =
            "SELECT c.id, c._student, " + FULL_NAME + ", c._university, u.name, " +
            "  c._certificate_type, ct.name, c._certificate_name, cn.name, " +
            "  c._certificate_grade, cg.name, c.serial_number, c.issue_date, c.valid_date, c.active " +
            BASE_FROM + where +
            "ORDER BY c.issue_date DESC NULLS LAST, c.id " +
            "LIMIT ? OFFSET ?";

        Query dataQuery = entityManager.createNativeQuery(dataSql);
        int idx = bind(dataQuery, params, 1);
        dataQuery.setParameter(idx, pageable.getPageSize());
        dataQuery.setParameter(idx + 1, pageable.getOffset());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();
        List<CertificateRowDto> content = rows.stream().map(this::mapRow).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Single certificate detail by id (UUID). Returns empty if not found or id is not a valid UUID.
     */
    public Optional<CertificateDetailDto> getCertificateDetail(String id) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }

        String sql =
            "SELECT c.id, c._student, " + FULL_NAME + ", c._university, u.name, " +
            "  c._certificate_type, ct.name, c._certificate_name, cn.name, " +
            "  c._certificate_grade, cg.name, c.serial_number, c.issue_date, c.valid_date, c.active, " +
            "  c._certificate_subject, cs.name " +
            BASE_FROM + SUBJECT_JOIN +
            "WHERE c.delete_ts IS NULL AND c.id = ?";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, uuid);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Object[] r = rows.get(0);
        String subjectCode = (String) r[15];
        String subjectName = (String) r[16];
        return Optional.of(new CertificateDetailDto(
            str(r[0]), str(r[1]), (String) r[2], (String) r[3], (String) r[4],
            (String) r[5], label(r[6], r[5]), (String) r[7], label(r[8], r[7]),
            (String) r[9], label(r[10], r[9]), (String) r[11], toLocalDate(r[12]), toLocalDate(r[13]), (Boolean) r[14],
            subjectCode, subjectName != null ? subjectName : subjectCode
        ));
    }

    /** Filter dictionaries (cached). */
    @Cacheable(value = "certificatesDictionaries", key = "'all'")
    public CertificateDictionariesDto getDictionaries() {
        log.debug("Loading certificate dictionaries");
        return new CertificateDictionariesDto(loadUniversities(), loadCertificateTypes());
    }

    // ---- helpers ----

    private String buildWhere(String q, String universityCode, String certificateType, List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE c.delete_ts IS NULL ");
        String searchLike = (q != null && !q.isBlank()) ? "%" + q.trim().toLowerCase() + "%" : null;
        if (searchLike != null) {
            where.append("AND (LOWER(c.serial_number) LIKE ? OR LOWER(" + FULL_NAME + ") LIKE ?) ");
            params.add(searchLike);
            params.add(searchLike);
        }
        if (universityCode != null && !universityCode.isBlank()) {
            where.append("AND c._university = ? ");
            params.add(universityCode.trim());
        }
        if (certificateType != null && !certificateType.isBlank()) {
            where.append("AND c._certificate_type = ? ");
            params.add(certificateType.trim());
        }
        return where.toString();
    }

    private CertificateRowDto mapRow(Object[] r) {
        return new CertificateRowDto(
            str(r[0]), str(r[1]), (String) r[2], (String) r[3], (String) r[4],
            (String) r[5], label(r[6], r[5]), (String) r[7], label(r[8], r[7]),
            (String) r[9], label(r[10], r[9]), (String) r[11], toLocalDate(r[12]), toLocalDate(r[13]), (Boolean) r[14]
        );
    }

    private List<CertificateDictionariesDto.DictionaryItem> loadUniversities() {
        Query query = entityManager.createNativeQuery(
            "SELECT code, name FROM hemishe_e_university WHERE delete_ts IS NULL ORDER BY name");
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
            .map(row -> new CertificateDictionariesDto.DictionaryItem((String) row[0], (String) row[1]))
            .collect(Collectors.toList());
    }

    private List<CertificateDictionariesDto.DictionaryItem> loadCertificateTypes() {
        Query query = entityManager.createNativeQuery(
            "SELECT code, name FROM hemishe_h_certificate_type WHERE delete_ts IS NULL AND active = true ORDER BY name");
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
            .map(row -> new CertificateDictionariesDto.DictionaryItem((String) row[0], (String) row[1]))
            .collect(Collectors.toList());
    }

    private static int bind(Query query, List<Object> params, int start) {
        int i = start;
        for (Object p : params) {
            query.setParameter(i++, p);
        }
        return i;
    }

    /** Resolved classifier label with raw-code fallback. */
    private static String label(Object name, Object code) {
        if (name != null) return (String) name;
        return code != null ? (String) code : null;
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
