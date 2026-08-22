package uz.hemis.service.registry;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.util.JdbcTemporal;
import uz.hemis.common.exception.ConflictException;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.university.UniversityAttachedSpeciality;
import uz.hemis.domain.repository.SpecialityBachelorRepository;
import uz.hemis.domain.repository.SpecialityDoctoralRepository;
import uz.hemis.domain.repository.SpecialityMasterRepository;
import uz.hemis.domain.repository.SpecialityOrdinaturaRepository;
import uz.hemis.domain.repository.UniversityAttachedSpecialityRepository;
import uz.hemis.service.registry.dto.AttachedSpecialityCreateDto;
import uz.hemis.service.registry.dto.AttachedSpecialityDetailDto;
import uz.hemis.service.registry.dto.AttachedSpecialityDictionariesDto;
import uz.hemis.service.registry.dto.AttachedSpecialityDictionariesDto.CodeName;
import uz.hemis.service.registry.dto.AttachedSpecialityDictionariesDto.IdName;
import uz.hemis.service.registry.dto.AttachedSpecialityRowDto;
import uz.hemis.service.registry.dto.AttachedSpecialityUpdateDto;
import uz.hemis.service.registry.dto.SpecialityLevel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Attached-speciality registry service (University specialities card).
 *
 * <p>CENTRAL-MINISTRY CRUD over {@code hemishe_e_university_attached_speciality}:
 * the ministry attaches classifier specialities to universities. Reads run on the
 * REPLICA (class-level {@code readOnly}); mutations override to MASTER.</p>
 *
 * <p><strong>Name resolution</strong> mirrors {@code FacultyRegistryService}: native
 * queries {@code LEFT JOIN} the classifier tables so every {@code code}/{@code id} is
 * paired with its display name in a single round-trip (no N+1). Because native SQL
 * bypasses {@code @SQLRestriction}, {@code delete_ts IS NULL} is added manually.</p>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AttachedSpecialityRegistryService {

    private final EntityManager entityManager;
    private final UniversityAttachedSpecialityRepository repository;
    private final SpecialityBachelorRepository bachelorRepository;
    private final SpecialityMasterRepository masterRepository;
    private final SpecialityOrdinaturaRepository ordinaturaRepository;
    private final SpecialityDoctoralRepository doctoralRepository;

    /**
     * Shared SELECT + JOIN skeleton. {@code sb/sm/so/sd} resolve the four possible
     * speciality columns; {@code COALESCE} collapses the single populated one.
     */
    private static final String BASE_SELECT = """
            SELECT a.id,
                   a._university,
                   u.name AS university_name,
                   a._education_type,
                   et.name AS education_type_name,
                   a._education_form,
                   ef.name AS education_form_name,
                   CASE WHEN a._speciality_bachelor IS NOT NULL THEN 'BACHELOR'
                        WHEN a._speciality_master IS NOT NULL THEN 'MASTER'
                        WHEN a._speciality_ordinatura IS NOT NULL THEN 'ORDINATURA'
                        WHEN a._speciality_doctoral IS NOT NULL THEN 'DOCTORAL' END AS speciality_level,
                   COALESCE(a._speciality_bachelor, a._speciality_master, a._speciality_ordinatura, a._speciality_doctoral) AS speciality_id,
                   COALESCE(sb.name, sm.name, so.name, sd.name) AS speciality_name,
                   a.active
            """;

    private static final String BASE_FROM = """
             FROM hemishe_e_university_attached_speciality a
             LEFT JOIN hemishe_e_university u ON u.code = a._university AND u.delete_ts IS NULL
             LEFT JOIN hemishe_h_education_type et ON et.code = a._education_type AND et.delete_ts IS NULL
             LEFT JOIN hemishe_h_education_form ef ON ef.code = a._education_form AND ef.delete_ts IS NULL
             LEFT JOIN hemishe_h_speciality_bachelor sb ON sb.id = a._speciality_bachelor AND sb.delete_ts IS NULL
             LEFT JOIN hemishe_h_speciality_master sm ON sm.id = a._speciality_master AND sm.delete_ts IS NULL
             LEFT JOIN hemishe_h_speciality_ordinatura so ON so.id = a._speciality_ordinatura AND so.delete_ts IS NULL
             LEFT JOIN hemishe_h_speciality_doctoral sd ON sd.id = a._speciality_doctoral AND sd.delete_ts IS NULL
            """;

    /** Whitelist: sort property → safe SQL ORDER BY expression (blocks column-abuse). */
    private static final Map<String, String> SORT_WHITELIST = Map.of(
            "universityName", "university_name",
            "universityCode", "a._university",
            "educationType", "a._education_type",
            "educationForm", "a._education_form",
            "specialityLevel", "speciality_level",
            "specialityName", "speciality_name",
            "active", "a.active"
    );
    // a.id is appended as a total-order tiebreaker: the streaming .xlsx export pages through the
    // result with separate LIMIT/OFFSET statements, so tied rows must have a stable order or they
    // would be duplicated/skipped across page boundaries.
    private static final String DEFAULT_ORDER_BY = "university_name ASC, speciality_name ASC, a.id ASC";

    // =====================================================
    // READ (REPLICA)
    // =====================================================

    /**
     * Paginated, filtered list of attachments with resolved names.
     */
    public Page<AttachedSpecialityRowDto> list(String q, String universityCode, String educationType,
                                               String educationForm, Boolean active, Pageable pageable) {
        log.debug("Listing attached specialities: q={}, universityCode={}, educationType={}, educationForm={}, active={}",
                q, universityCode, educationType, educationForm, active);

        List<Object> params = new ArrayList<>();
        String where = buildWhere(q, universityCode, educationType, educationForm, active, params);

        String countSql = "SELECT COUNT(*)" + BASE_FROM + where;
        Query countQuery = entityManager.createNativeQuery(countSql);
        bindParams(countQuery, params);
        long total = ((Number) countQuery.getSingleResult()).longValue();
        if (total == 0) {
            return Page.empty(pageable);
        }

        String dataSql = BASE_SELECT + BASE_FROM + where
                + " ORDER BY " + resolveOrderBy(pageable.getSort())
                + " LIMIT ? OFFSET ?";
        Query dataQuery = entityManager.createNativeQuery(dataSql);
        bindParams(dataQuery, params);
        dataQuery.setParameter(params.size() + 1, pageable.getPageSize());
        dataQuery.setParameter(params.size() + 2, pageable.getOffset());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();
        List<AttachedSpecialityRowDto> content = rows.stream().map(this::toRow).toList();
        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Single attachment detail (with audit fields). 404 if missing/deleted.
     */
    public AttachedSpecialityDetailDto getDetail(UUID id) {
        log.debug("Getting attached speciality detail: id={}", id);
        return findDetail(id)
                .orElseThrow(() -> new ResourceNotFoundException("AttachedSpeciality", "id", id));
    }

    @Cacheable(value = "attachedSpecialityDictionaries", key = "'all'", unless = "#result == null")
    public AttachedSpecialityDictionariesDto getDictionaries() {
        log.debug("Loading attached-speciality dictionaries");

        List<CodeName> universities = loadCodeNames(
                "SELECT code, name FROM hemishe_e_university WHERE delete_ts IS NULL ORDER BY name");
        List<CodeName> educationTypes = loadCodeNames(
                "SELECT code, name FROM hemishe_h_education_type WHERE delete_ts IS NULL AND active = true ORDER BY name");
        List<CodeName> educationForms = loadCodeNames(
                "SELECT code, name FROM hemishe_h_education_form WHERE delete_ts IS NULL AND active = true ORDER BY name");

        Map<String, List<IdName>> specialities = new java.util.LinkedHashMap<>();
        specialities.put(SpecialityLevel.BACHELOR.name(), bachelorRepository.findByActiveTrueOrderByNameAsc()
                .stream().map(s -> new IdName(s.getId().toString(), s.getName())).toList());
        specialities.put(SpecialityLevel.MASTER.name(), masterRepository.findByActiveTrueOrderByNameAsc()
                .stream().map(s -> new IdName(s.getId().toString(), s.getName())).toList());
        specialities.put(SpecialityLevel.ORDINATURA.name(), ordinaturaRepository.findByActiveTrueOrderByNameAsc()
                .stream().map(s -> new IdName(s.getId().toString(), s.getName())).toList());
        specialities.put(SpecialityLevel.DOCTORAL.name(), doctoralRepository.findByActiveTrueOrderByNameAsc()
                .stream().map(s -> new IdName(s.getId().toString(), s.getName())).toList());

        return new AttachedSpecialityDictionariesDto(universities, educationTypes, educationForms, specialities);
    }


    // =====================================================
    // WRITE (MASTER)
    // =====================================================

    @Transactional
    @CacheEvict(value = "attachedSpecialityDictionaries", allEntries = true)
    public AttachedSpecialityDetailDto create(AttachedSpecialityCreateDto dto) {
        log.info("Creating attached speciality: university={}, level={}, specialityId={}",
                dto.universityCode(), dto.specialityLevel(), dto.specialityId());

        checkDuplicate(dto.universityCode(), dto.educationType(), dto.educationForm(),
                dto.specialityLevel(), dto.specialityId(), null);

        UniversityAttachedSpeciality entity = new UniversityAttachedSpeciality();
        entity.setUniversity(dto.universityCode());
        entity.setEducationType(dto.educationType());
        entity.setEducationForm(dto.educationForm());
        entity.setActive(dto.active() == null ? Boolean.TRUE : dto.active());
        applySpecialityLevel(entity, dto.specialityLevel(), dto.specialityId());

        UniversityAttachedSpeciality saved = repository.save(entity);
        log.info("Attached speciality created: id={}", saved.getId());
        return getDetail(saved.getId());
    }

    @Transactional
    @CacheEvict(value = "attachedSpecialityDictionaries", allEntries = true)
    public AttachedSpecialityDetailDto update(UUID id, AttachedSpecialityUpdateDto dto) {
        log.info("Updating attached speciality: id={}", id);

        UniversityAttachedSpeciality entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AttachedSpeciality", "id", id));

        checkDuplicate(dto.universityCode(), dto.educationType(), dto.educationForm(),
                dto.specialityLevel(), dto.specialityId(), id);

        entity.setUniversity(dto.universityCode());
        entity.setEducationType(dto.educationType());
        entity.setEducationForm(dto.educationForm());
        if (dto.active() != null) {
            entity.setActive(dto.active());
        }
        applySpecialityLevel(entity, dto.specialityLevel(), dto.specialityId());

        repository.save(entity);
        log.info("Attached speciality updated: id={}", id);
        return getDetail(id);
    }

    @Transactional
    @CacheEvict(value = "attachedSpecialityDictionaries", allEntries = true)
    public void delete(UUID id) {
        log.info("Deleting attached speciality (soft): id={}", id);
        UniversityAttachedSpeciality entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AttachedSpeciality", "id", id));
        entity.setDeleteTs(LocalDateTime.now());
        repository.save(entity);
        log.info("Attached speciality deleted (soft): id={}", id);
    }

    // =====================================================
    // Helpers
    // =====================================================

    /** Set the {@code _speciality_<level>} column and NULL the other three. */
    private void applySpecialityLevel(UniversityAttachedSpeciality entity, SpecialityLevel level, UUID specialityId) {
        entity.setSpecialityBachelor(null);
        entity.setSpecialityMaster(null);
        entity.setSpecialityOrdinatura(null);
        entity.setSpecialityDoctoral(null);
        switch (level) {
            case BACHELOR -> entity.setSpecialityBachelor(specialityId);
            case MASTER -> entity.setSpecialityMaster(specialityId);
            case ORDINATURA -> entity.setSpecialityOrdinatura(specialityId);
            case DOCTORAL -> entity.setSpecialityDoctoral(specialityId);
        }
    }

    /** Duplicate guard (no DB unique constraint) — throws 409 if an active twin exists. */
    private void checkDuplicate(String university, String educationType, String educationForm,
                                SpecialityLevel level, UUID specialityId, UUID excludeId) {
        boolean exists = switch (level) {
            case BACHELOR -> repository.existsBachelorDuplicate(university, educationType, educationForm, specialityId, excludeId);
            case MASTER -> repository.existsMasterDuplicate(university, educationType, educationForm, specialityId, excludeId);
            case ORDINATURA -> repository.existsOrdinaturaDuplicate(university, educationType, educationForm, specialityId, excludeId);
            case DOCTORAL -> repository.existsDoctoralDuplicate(university, educationType, educationForm, specialityId, excludeId);
        };
        if (exists) {
            throw new ConflictException("This speciality is already attached to the university for the given "
                    + "education type/form (" + level + ")");
        }
    }

    private String buildWhere(String q, String universityCode, String educationType,
                              String educationForm, Boolean active, List<Object> params) {
        StringBuilder where = new StringBuilder(" WHERE a.delete_ts IS NULL");
        if (q != null && !q.isBlank()) {
            String like = "%" + q.trim().toLowerCase() + "%";
            where.append(" AND (LOWER(u.name) LIKE ?"
                    + " OR LOWER(COALESCE(sb.name, sm.name, so.name, sd.name)) LIKE ?)");
            params.add(like);
            params.add(like);
        }
        if (universityCode != null && !universityCode.isBlank()) {
            where.append(" AND a._university = ?");
            params.add(universityCode);
        }
        if (educationType != null && !educationType.isBlank()) {
            where.append(" AND a._education_type = ?");
            params.add(educationType);
        }
        if (educationForm != null && !educationForm.isBlank()) {
            where.append(" AND a._education_form = ?");
            params.add(educationForm);
        }
        if (active != null) {
            where.append(" AND a.active = ?");
            params.add(active);
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
        // Append the PK as a final tiebreaker so any custom sort is still a total order (stable paging).
        return orders.isEmpty() ? DEFAULT_ORDER_BY : String.join(", ", orders) + ", a.id ASC";
    }

    private static void bindParams(Query query, List<Object> params) {
        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }
    }

    @SuppressWarnings("unchecked")
    private java.util.Optional<AttachedSpecialityDetailDto> findDetail(UUID id) {
        String sql = BASE_SELECT
                + ", a.create_ts, a.created_by, a.update_ts, a.updated_by, a.version"
                + BASE_FROM
                + " WHERE a.delete_ts IS NULL AND a.id = ?";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, id);
        List<Object[]> rows = query.getResultList();
        if (rows.isEmpty()) {
            return java.util.Optional.empty();
        }
        Object[] r = rows.get(0);
        AttachedSpecialityDetailDto dto = new AttachedSpecialityDetailDto(
                asString(r[0]), asString(r[1]), asString(r[2]), asString(r[3]), asString(r[4]),
                asString(r[5]), asString(r[6]), asString(r[7]), asString(r[8]), asString(r[9]),
                (Boolean) r[10],
                toLdt(r[11]), asString(r[12]), toLdt(r[13]), asString(r[14]),
                r[15] != null ? ((Number) r[15]).intValue() : null
        );
        return java.util.Optional.of(dto);
    }

    private AttachedSpecialityRowDto toRow(Object[] r) {
        return new AttachedSpecialityRowDto(
                asString(r[0]), asString(r[1]), asString(r[2]), asString(r[3]), asString(r[4]),
                asString(r[5]), asString(r[6]), asString(r[7]), asString(r[8]), asString(r[9]),
                (Boolean) r[10]
        );
    }

    @SuppressWarnings("unchecked")
    private List<CodeName> loadCodeNames(String sql) {
        List<Object[]> rows = entityManager.createNativeQuery(sql).getResultList();
        return rows.stream().map(r -> new CodeName(asString(r[0]), asString(r[1]))).toList();
    }

    private static String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private static LocalDateTime toLdt(Object value) {
        return JdbcTemporal.toLocalDateTime(value);
    }
}
