package uz.hemis.service.registry;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.exception.BadRequestException;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.finance.DiplomaBlankDistribution;
import uz.hemis.domain.repository.DiplomaBlankDistributionRepository;
import uz.hemis.service.registry.dto.DiplomaBlankDistributionDictionariesDto;
import uz.hemis.service.registry.dto.DiplomaBlankDistributionDictionariesDto.DictionaryItem;
import uz.hemis.service.registry.dto.DiplomaBlankDistributionRequestDto;
import uz.hemis.service.registry.dto.DiplomaBlankDistributionRowDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Diploma Blank Distribution Service — CENTRAL-MINISTRY CRUD over
 * {@code diploma_blank_distribution} (NEW table, V016).
 *
 * <p>The ministry manages serial-range allocations to universities centrally.
 * NO fanout — OTMs read via existing legacy endpoints. Reads run on the REPLICA
 * (class-level {@code readOnly}); mutations override to MASTER.</p>
 *
 * <p><strong>Name resolution</strong> mirrors {@code AttachedSpecialityRegistryService}:
 * native queries {@code LEFT JOIN} the classifier tables (education year/type, blank
 * category, generate status) so every code is paired with a display name (raw-code
 * fallback via {@code COALESCE}). Native SQL bypasses {@code @SQLRestriction}, so
 * {@code deleted_at IS NULL} is added manually.</p>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DiplomaBlankDistributionService {

    private final EntityManager entityManager;
    private final DiplomaBlankDistributionRepository repository;

    /** Hard cap for CSV export — protects memory when the filter is broad. */
    private static final int EXPORT_HARD_LIMIT = 5000;

    private static final String BASE_SELECT = """
            SELECT d.id,
                   d.university_code,
                   u.name AS university_name,
                   d.education_year,
                   ey.name AS education_year_name,
                   d.education_type,
                   et.name AS education_type_name,
                   d.blank_category,
                   bc.name AS blank_category_name,
                   d.blank_seria,
                   d.blank_start_number,
                   d.blank_end_number,
                   d.generate_status_code,
                   gs.name AS generate_status_name,
                   d.distribution_date,
                   d.note
            """;

    private static final String BASE_FROM = """
             FROM diploma_blank_distribution d
             LEFT JOIN hemishe_e_university u ON u.code = d.university_code AND u.delete_ts IS NULL
             LEFT JOIN hemishe_h_education_year ey ON ey.code = d.education_year AND ey.delete_ts IS NULL
             LEFT JOIN hemishe_h_education_type et ON et.code = d.education_type AND et.delete_ts IS NULL
             LEFT JOIN hemishe_h_diplom_blank_category bc ON bc.code = d.blank_category AND bc.delete_ts IS NULL
             LEFT JOIN hemishe_h_diplom_blank_generate_status gs ON gs.code = d.generate_status_code AND gs.delete_ts IS NULL
            """;

    /** Whitelist: sort property → safe SQL ORDER BY expression (blocks column-abuse). */
    private static final Map<String, String> SORT_WHITELIST = Map.of(
            "universityName", "university_name",
            "universityCode", "d.university_code",
            "educationYear", "d.education_year",
            "blankCategory", "d.blank_category",
            "blankSeria", "d.blank_seria",
            "distributionDate", "d.distribution_date"
    );
    private static final String DEFAULT_ORDER_BY = "d.distribution_date DESC NULLS LAST, d.id";

    // =====================================================
    // READ (REPLICA)
    // =====================================================

    public Page<DiplomaBlankDistributionRowDto> list(String q, String universityCode, String educationYear,
                                                     String blankCategory, Pageable pageable) {
        log.debug("Listing diploma-blank distributions: q={}, universityCode={}, educationYear={}, blankCategory={}",
                q, universityCode, educationYear, blankCategory);

        List<Object> params = new ArrayList<>();
        String where = buildWhere(q, universityCode, educationYear, blankCategory, params);

        String countSql = "SELECT COUNT(*)" + BASE_FROM + where;
        Query countQuery = entityManager.createNativeQuery(countSql);
        bind(countQuery, params);
        long total = ((Number) countQuery.getSingleResult()).longValue();
        if (total == 0) {
            return Page.empty(pageable);
        }

        String dataSql = BASE_SELECT + BASE_FROM + where
                + " ORDER BY " + resolveOrderBy(pageable.getSort())
                + " LIMIT ? OFFSET ?";
        Query dataQuery = entityManager.createNativeQuery(dataSql);
        bind(dataQuery, params);
        dataQuery.setParameter(params.size() + 1, pageable.getPageSize());
        dataQuery.setParameter(params.size() + 2, pageable.getOffset());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();
        List<DiplomaBlankDistributionRowDto> content = rows.stream().map(this::toRow).toList();
        return new PageImpl<>(content, pageable, total);
    }

    public DiplomaBlankDistributionRowDto getDetail(UUID id) {
        log.debug("Getting diploma-blank distribution detail: id={}", id);
        String sql = BASE_SELECT + BASE_FROM + " WHERE d.deleted_at IS NULL AND d.id = ?";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, id);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        if (rows.isEmpty()) {
            throw new ResourceNotFoundException("DiplomaBlankDistribution", "id", id);
        }
        return toRow(rows.get(0));
    }

    @Cacheable(value = "diplomaBlankDistributionDictionaries", key = "'all'", unless = "#result == null")
    public DiplomaBlankDistributionDictionariesDto getDictionaries() {
        log.debug("Loading diploma-blank distribution dictionaries");
        List<DictionaryItem> universities = loadItems(
                "SELECT code, name FROM hemishe_e_university WHERE delete_ts IS NULL ORDER BY name");
        List<DictionaryItem> educationYears = loadItems(
                "SELECT code, name FROM hemishe_h_education_year WHERE delete_ts IS NULL AND active = true ORDER BY name");
        List<DictionaryItem> educationTypes = loadItems(
                "SELECT code, name FROM hemishe_h_education_type WHERE delete_ts IS NULL AND active = true ORDER BY name");
        List<DictionaryItem> blankCategories = loadItems(
                "SELECT code, name FROM hemishe_h_diplom_blank_category WHERE delete_ts IS NULL AND active = true ORDER BY name");
        List<DictionaryItem> generateStatuses = loadItems(
                "SELECT code, name FROM hemishe_h_diplom_blank_generate_status WHERE delete_ts IS NULL AND active = true ORDER BY name");
        return new DiplomaBlankDistributionDictionariesDto(
                universities, educationYears, educationTypes, blankCategories, generateStatuses);
    }

    public List<DiplomaBlankDistributionRowDto> export(String q, String universityCode, String educationYear,
                                                       String blankCategory) {
        Pageable exportPage = PageRequest.of(0, EXPORT_HARD_LIMIT);
        Page<DiplomaBlankDistributionRowDto> page = list(q, universityCode, educationYear, blankCategory, exportPage);
        if (page.getTotalElements() > EXPORT_HARD_LIMIT) {
            log.warn("Diploma-blank distribution export truncated: matched {} rows, returning first {}",
                    page.getTotalElements(), EXPORT_HARD_LIMIT);
        }
        return page.getContent();
    }

    // =====================================================
    // WRITE (MASTER)
    // =====================================================

    @Transactional
    @CacheEvict(value = "diplomaBlankDistributionDictionaries", allEntries = true)
    public DiplomaBlankDistributionRowDto create(DiplomaBlankDistributionRequestDto dto) {
        log.info("Creating diploma-blank distribution: university={}, seria={}, range={}-{}",
                dto.universityCode(), dto.blankSeria(), dto.blankStartNumber(), dto.blankEndNumber());

        validateRange(dto);

        DiplomaBlankDistribution entity = new DiplomaBlankDistribution();
        apply(entity, dto);

        DiplomaBlankDistribution saved = repository.save(entity);
        log.info("Diploma-blank distribution created: id={}", saved.getId());
        return getDetail(saved.getId());
    }

    @Transactional
    @CacheEvict(value = "diplomaBlankDistributionDictionaries", allEntries = true)
    public DiplomaBlankDistributionRowDto update(UUID id, DiplomaBlankDistributionRequestDto dto) {
        log.info("Updating diploma-blank distribution: id={}", id);

        validateRange(dto);

        DiplomaBlankDistribution entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DiplomaBlankDistribution", "id", id));
        apply(entity, dto);

        repository.save(entity);
        log.info("Diploma-blank distribution updated: id={}", id);
        return getDetail(id);
    }

    @Transactional
    @CacheEvict(value = "diplomaBlankDistributionDictionaries", allEntries = true)
    public void delete(UUID id) {
        log.info("Deleting diploma-blank distribution (soft): id={}", id);
        DiplomaBlankDistribution entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DiplomaBlankDistribution", "id", id));
        entity.softDelete();
        repository.save(entity);
        log.info("Diploma-blank distribution deleted (soft): id={}", id);
    }

    // =====================================================
    // Helpers
    // =====================================================

    private void validateRange(DiplomaBlankDistributionRequestDto dto) {
        if (dto.blankStartNumber() != null && dto.blankEndNumber() != null
                && dto.blankEndNumber() < dto.blankStartNumber()) {
            throw new BadRequestException("blankEndNumber must be >= blankStartNumber");
        }
    }

    private void apply(DiplomaBlankDistribution entity, DiplomaBlankDistributionRequestDto dto) {
        entity.setUniversityCode(dto.universityCode());
        entity.setEducationYear(dto.educationYear());
        entity.setEducationType(dto.educationType());
        entity.setBlankCategory(dto.blankCategory());
        entity.setBlankSeria(dto.blankSeria());
        entity.setBlankStartNumber(dto.blankStartNumber());
        entity.setBlankEndNumber(dto.blankEndNumber());
        entity.setGenerateStatusCode(dto.generateStatusCode());
        entity.setDistributionDate(dto.distributionDate());
        entity.setNote(dto.note());
    }

    private String buildWhere(String q, String universityCode, String educationYear,
                              String blankCategory, List<Object> params) {
        StringBuilder where = new StringBuilder(" WHERE d.deleted_at IS NULL");
        if (q != null && !q.isBlank()) {
            String like = "%" + q.trim().toLowerCase() + "%";
            where.append(" AND (LOWER(d.blank_seria) LIKE ? OR LOWER(u.name) LIKE ? OR LOWER(COALESCE(d.note, '')) LIKE ?)");
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (universityCode != null && !universityCode.isBlank()) {
            where.append(" AND d.university_code = ?");
            params.add(universityCode.trim());
        }
        if (educationYear != null && !educationYear.isBlank()) {
            where.append(" AND d.education_year = ?");
            params.add(educationYear.trim());
        }
        if (blankCategory != null && !blankCategory.isBlank()) {
            where.append(" AND d.blank_category = ?");
            params.add(blankCategory.trim());
        }
        return where.toString();
    }

    private String resolveOrderBy(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return DEFAULT_ORDER_BY;
        }
        List<String> orders = new ArrayList<>();
        for (Sort.Order o : sort) {
            String column = SORT_WHITELIST.get(o.getProperty());
            if (column != null) {
                orders.add(column + (o.isAscending() ? " ASC" : " DESC"));
            }
        }
        return orders.isEmpty() ? DEFAULT_ORDER_BY : String.join(", ", orders);
    }

    private DiplomaBlankDistributionRowDto toRow(Object[] r) {
        Integer start = toInt(r[10]);
        Integer end = toInt(r[11]);
        Integer quantity = (start != null && end != null) ? (end - start + 1) : null;
        return new DiplomaBlankDistributionRowDto(
                str(r[0]), (String) r[1], (String) r[2], (String) r[3], (String) r[4],
                (String) r[5], (String) r[6], (String) r[7], (String) r[8], (String) r[9],
                start, end, quantity, (String) r[12], (String) r[13], toLocalDate(r[14]), (String) r[15]
        );
    }

    @SuppressWarnings("unchecked")
    private List<DictionaryItem> loadItems(String sql) {
        List<Object[]> rows = entityManager.createNativeQuery(sql).getResultList();
        return rows.stream().map(r -> new DictionaryItem((String) r[0], (String) r[1])).toList();
    }

    private static void bind(Query query, List<Object> params) {
        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }
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
