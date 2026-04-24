package uz.hemis.service.registry;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.Predicate;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.audit.AuditAction;
import uz.hemis.common.audit.Audited;
import uz.hemis.common.dto.university.UniversityDto;
import uz.hemis.domain.entity.university.University;
import uz.hemis.domain.repository.UniversityRepository;
import uz.hemis.service.registry.dto.UniversityDictionariesDto;
import uz.hemis.service.registry.dto.UniversityDictionariesDto.DictionaryItem;
import uz.hemis.service.registry.dto.UniversityRequestDto;
import uz.hemis.service.shared.mapper.UniversityDtoConverter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * University Registry Service
 *
 * <p>READS FROM: READ REPLICA Database</p>
 * <ul>
 *   <li>Class-level @Transactional(readOnly=true) ensures REPLICA routing for all read methods</li>
 *   <li>Write methods override with @Transactional to route to MASTER</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UniversityRegistryService {

    private final UniversityRepository universityRepository;
    private final UniversityDtoConverter universityMapper;
    private final EntityManager entityManager;
    private final ClassifierLookupService classifiers;

    /** Populate human-readable `*Name` fields from cached classifier maps (in-memory, O(1)). */
    private UniversityDto enrich(UniversityDto dto) {
        if (dto == null) return null;
        dto.setOwnershipName(classifiers.resolveOwnership(dto.getOwnership()));
        dto.setUniversityTypeName(classifiers.resolveType(dto.getUniversityType()));
        dto.setUniversityActivityStatusName(classifiers.resolveActivityStatus(dto.getUniversityActivityStatus()));
        dto.setUniversityBelongsToName(classifiers.resolveBelongsTo(dto.getUniversityBelongsTo()));
        dto.setUniversityContractCategoryName(classifiers.resolveContractCategory(dto.getUniversityContractCategory()));
        dto.setUniversityVersionName(classifiers.resolveVersionType(dto.getUniversityVersion()));
        dto.setSoatoName(classifiers.resolveSoato(dto.getSoato()));
        dto.setSoatoRegionName(classifiers.resolveSoato(dto.getSoatoRegion()));
        dto.setTerrainName(classifiers.resolveSoato(dto.getTerrain()));
        return dto;
    }

    // =====================================================
    // READ Operations (REPLICA Database)
    // =====================================================

    @Cacheable(
            value = "universitiesSearch",
            key = "#q + ':' + #searchField + ':' + #regionId + ':' + #ownershipId + ':' + #typeId + ':' + #activityStatusId + ':' + #belongsToId + ':' + #contractCategoryId + ':' + #versionTypeId + ':' + #districtId + ':' + #active + ':' + #gpaEdit + ':' + #accreditationEdit + ':' + #addStudent + ':' + #allowGrouping + ':' + #allowTransferOutside + ':' + #oneId + ':' + #gradingSystem + ':' + #addForeignStudent + ':' + #addTransferStudent + ':' + #addAcademicMobileStudent + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()",
            unless = "#result == null || #pageable.pageNumber > 10"
    )
    public Page<UniversityDto> searchUniversities(
            String q,
            String searchField,
            String regionId,
            String ownershipId,
            String typeId,
            String activityStatusId,
            String belongsToId,
            String contractCategoryId,
            String versionTypeId,
            String districtId,
            String active,
            String gpaEdit,
            String accreditationEdit,
            String addStudent,
            String allowGrouping,
            String allowTransferOutside,
            String oneId,
            String gradingSystem,
            String addForeignStudent,
            String addTransferStudent,
            String addAcademicMobileStudent,
            Pageable pageable
    ) {
        log.debug("Searching universities: q={}, searchField={}, regionId={}, ownershipId={}, typeId={}",
                  q, searchField, regionId, ownershipId, typeId);

        Specification<University> spec = buildFilterSpecification(q, searchField, regionId, ownershipId, typeId,
                activityStatusId, belongsToId, contractCategoryId, versionTypeId,
                districtId, active, gpaEdit, accreditationEdit, addStudent, allowGrouping, allowTransferOutside,
                oneId, gradingSystem, addForeignStudent, addTransferStudent, addAcademicMobileStudent);
        Page<University> universities = universityRepository.findAll(spec, pageable);
        return universities.map(u -> enrich(universityMapper.toDto(u)));
    }

    @Cacheable(value = "universitiesSearch", key = "'detail:' + #id", unless = "#result == null")
    public UniversityDto getUniversityById(String id) {
        log.debug("Getting university by id: {}", id);
        return universityRepository.findById(id)
                .map(universityMapper::toDto)
                .map(this::enrich)
                .orElse(null);
    }

    @Cacheable(value = "universityDictionaries", key = "'all'", unless = "#result == null")
    public UniversityDictionariesDto getDictionaries() {
        log.debug("Loading university dictionaries from database");

        // Yangi jadvallarga yo'naltirilgan — Bosqich 4.5 (ReferenceEntity: is_active, delete_ts yo'q)
        List<DictionaryItem> ownerships = loadClassifierItems(
                "SELECT code, name FROM ownership WHERE is_active = true ORDER BY name");

        List<DictionaryItem> types = loadClassifierItems(
                "SELECT code, name FROM university_type WHERE is_active = true ORDER BY name");

        List<DictionaryItem> regions = loadRegionItems();

        List<DictionaryItem> activityStatuses = loadClassifierItems(
                "SELECT code, name FROM university_activity_status WHERE is_active = true ORDER BY name");

        List<DictionaryItem> belongsToOptions = loadClassifierItems(
                "SELECT code, name FROM university_belongs_to WHERE is_active = true ORDER BY name");

        List<DictionaryItem> contractCategories = loadClassifierItems(
                "SELECT code, name FROM contract_category WHERE is_active = true ORDER BY name");

        List<DictionaryItem> versionTypes = loadClassifierItems(
                "SELECT code, name FROM hemis_version WHERE is_active = true ORDER BY name");

        List<DictionaryItem> districts = loadDistrictItems();

        return UniversityDictionariesDto.builder()
                .ownerships(ownerships)
                .types(types)
                .regions(regions)
                .activityStatuses(activityStatuses)
                .belongsToOptions(belongsToOptions)
                .contractCategories(contractCategories)
                .versionTypes(versionTypes)
                .districts(districts)
                .build();
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<DictionaryItem> getTerrainsBySoato(String soato) {
        Query query = entityManager.createNativeQuery(
                "SELECT code, name FROM terrain WHERE soato_code = :soato AND is_active = true ORDER BY name");
        query.setParameter("soato", soato);
        List<Object[]> results = query.getResultList();
        return results.stream()
                .map(row -> DictionaryItem.builder()
                        .code((String) row[0])
                        .name((String) row[1])
                        .build())
                .collect(Collectors.toList());
    }

    public List<UniversityDto> exportUniversities(
            String q,
            String searchField,
            String regionId,
            String ownershipId,
            String typeId,
            String activityStatusId,
            String belongsToId,
            String contractCategoryId,
            String versionTypeId,
            String districtId,
            String active,
            String gpaEdit,
            String accreditationEdit,
            String addStudent,
            String allowGrouping,
            String allowTransferOutside,
            String oneId,
            String gradingSystem,
            String addForeignStudent,
            String addTransferStudent,
            String addAcademicMobileStudent
    ) {
        log.debug("Exporting universities: q={}, searchField={}, regionId={}, ownershipId={}, typeId={}",
                  q, searchField, regionId, ownershipId, typeId);

        Specification<University> spec = buildFilterSpecification(q, searchField, regionId, ownershipId, typeId,
                activityStatusId, belongsToId, contractCategoryId, versionTypeId,
                districtId, active, gpaEdit, accreditationEdit, addStudent, allowGrouping, allowTransferOutside,
                oneId, gradingSystem, addForeignStudent, addTransferStudent, addAcademicMobileStudent);
        List<University> universities = universityRepository.findAll(spec);
        return universityMapper.toDtoList(universities).stream().map(this::enrich).toList();
    }

    /**
     * Build name lookup maps for CSV export (code → name resolution)
     */
    public Map<String, String> getRegionNameMap() {
        // Yangi soato jadvali: `name` (eski name_uz), `is_active` (eski active + delete_ts)
        return loadNameMap("SELECT code, name FROM soato WHERE is_active = true AND LENGTH(code) = 4");
    }

    public Map<String, String> getOwnershipNameMap() {
        return loadNameMap("SELECT code, name FROM ownership WHERE is_active = true");
    }

    public Map<String, String> getUniversityTypeNameMap() {
        return loadNameMap("SELECT code, name FROM university_type WHERE is_active = true");
    }

    // =====================================================
    // WRITE Operations (MASTER Database)
    // =====================================================

    /**
     * Create new university (WRITES TO MASTER)
     */
    @Audited(action = AuditAction.CREATE, entity = "University", entityClass = University.class)
    @Transactional
    @CacheEvict(value = {"universitiesSearch", "universityDictionaries"}, allEntries = true)
    public UniversityDto createUniversity(UniversityRequestDto request) {
        log.info("Creating university: code={}, name={}", request.getCode(), request.getName());

        if (universityRepository.existsById(request.getCode())) {
            throw new IllegalArgumentException("University with code " + request.getCode() + " already exists");
        }

        University university = new University();
        mapRequestToEntity(request, university);
        // version and createTs are set by @PrePersist and @CreatedDate

        University saved = universityRepository.save(university);
        log.info("University created: {}", saved.getCode());

        return universityMapper.toDto(saved);
    }

    /**
     * Update existing university (WRITES TO MASTER)
     */
    @Audited(action = AuditAction.UPDATE, entity = "University", entityClass = University.class, keyArg = "code")
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "universitiesSearch", allEntries = true),
        @CacheEvict(value = "universityDictionaries", allEntries = true),
        @CacheEvict(value = "universitiesSearch", key = "'detail:' + #code")
    })
    public UniversityDto updateUniversity(String code, UniversityRequestDto request) {
        log.info("Updating university: {}", code);

        University university = universityRepository.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("University not found: " + code));

        mapRequestToEntity(request, university);
        // version is incremented by @PreUpdate; updateTs is set by @LastModifiedDate

        University saved = universityRepository.save(university);
        log.info("University updated: {}", saved.getCode());

        return universityMapper.toDto(saved);
    }

    /**
     * Delete university (soft delete - WRITES TO MASTER)
     */
    @Audited(action = AuditAction.DELETE, entity = "University", entityClass = University.class, keyArg = "code")
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "universitiesSearch", allEntries = true),
        @CacheEvict(value = "universityDictionaries", allEntries = true),
        @CacheEvict(value = "universitiesSearch", key = "'detail:' + #code")
    })
    public void deleteUniversity(String code) {
        log.info("Deleting university: {}", code);

        University university = universityRepository.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("University not found: " + code));

        university.setDeleteTs(LocalDateTime.now());
        // version is incremented by @PreUpdate
        universityRepository.save(university);

        log.info("University deleted (soft): {}", code);
    }

    // =====================================================
    // Private Helper Methods
    // =====================================================

    /**
     * Build JPA Specification for university filtering
     * Shared between search and export operations (DRY)
     */
    private Specification<University> buildFilterSpecification(
            String q, String searchField, String regionId, String ownershipId, String typeId,
            String activityStatusId, String belongsToId, String contractCategoryId, String versionTypeId,
            String districtId, String active, String gpaEdit, String accreditationEdit,
            String addStudent, String allowGrouping, String allowTransferOutside,
            String oneId, String gradingSystem, String addForeignStudent,
            String addTransferStudent, String addAcademicMobileStudent
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (q != null && !q.isBlank()) {
                String searchPattern = "%" + q.toLowerCase() + "%";
                if (searchField != null && !searchField.isBlank() && !"all".equals(searchField)) {
                    // Search only the specified field
                    predicates.add(cb.like(cb.lower(root.get(searchField)), searchPattern));
                } else {
                    // Search across all text fields (default)
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("name")), searchPattern),
                            cb.like(cb.lower(root.get("code")), searchPattern),
                            cb.like(cb.lower(root.get("tin")), searchPattern),
                            cb.like(cb.lower(cb.coalesce(root.get("address"), cb.literal(""))), searchPattern),
                            cb.like(cb.lower(cb.coalesce(root.get("mailAddress"), cb.literal(""))), searchPattern)
                    ));
                }
            }

            if (regionId != null && !regionId.isBlank()) {
                List<String> codes = Arrays.stream(regionId.split(",")).map(String::trim).toList();
                if (codes.size() == 1) {
                    predicates.add(cb.equal(root.get("soato"), codes.get(0)));
                } else {
                    predicates.add(root.get("soato").in(codes));
                }
            }

            if (ownershipId != null && !ownershipId.isBlank()) {
                List<String> codes = Arrays.stream(ownershipId.split(",")).map(String::trim).toList();
                if (codes.size() == 1) {
                    predicates.add(cb.equal(root.get("ownership"), codes.get(0)));
                } else {
                    predicates.add(root.get("ownership").in(codes));
                }
            }

            if (typeId != null && !typeId.isBlank()) {
                List<String> codes = Arrays.stream(typeId.split(",")).map(String::trim).toList();
                if (codes.size() == 1) {
                    predicates.add(cb.equal(root.get("universityType"), codes.get(0)));
                } else {
                    predicates.add(root.get("universityType").in(codes));
                }
            }

            if (activityStatusId != null && !activityStatusId.isBlank()) {
                List<String> codes = Arrays.stream(activityStatusId.split(",")).map(String::trim).toList();
                if (codes.size() == 1) {
                    predicates.add(cb.equal(root.get("universityActivityStatus"), codes.get(0)));
                } else {
                    predicates.add(root.get("universityActivityStatus").in(codes));
                }
            }

            if (belongsToId != null && !belongsToId.isBlank()) {
                List<String> codes = Arrays.stream(belongsToId.split(",")).map(String::trim).toList();
                if (codes.size() == 1) {
                    predicates.add(cb.equal(root.get("universityBelongsTo"), codes.get(0)));
                } else {
                    predicates.add(root.get("universityBelongsTo").in(codes));
                }
            }

            if (contractCategoryId != null && !contractCategoryId.isBlank()) {
                List<String> codes = Arrays.stream(contractCategoryId.split(",")).map(String::trim).toList();
                if (codes.size() == 1) {
                    predicates.add(cb.equal(root.get("universityContractCategory"), codes.get(0)));
                } else {
                    predicates.add(root.get("universityContractCategory").in(codes));
                }
            }

            if (versionTypeId != null && !versionTypeId.isBlank()) {
                List<String> codes = Arrays.stream(versionTypeId.split(",")).map(String::trim).toList();
                if (codes.size() == 1) {
                    predicates.add(cb.equal(root.get("universityVersion"), codes.get(0)));
                } else {
                    predicates.add(root.get("universityVersion").in(codes));
                }
            }

            if (districtId != null && !districtId.isBlank()) {
                List<String> codes = Arrays.stream(districtId.split(",")).map(String::trim).toList();
                if (codes.size() == 1) {
                    predicates.add(cb.equal(root.get("soatoRegion"), codes.get(0)));
                } else {
                    predicates.add(root.get("soatoRegion").in(codes));
                }
            }

            // Boolean filters
            if (active != null && !active.isBlank()) {
                predicates.add(cb.equal(root.get("active"), Boolean.valueOf(active)));
            }
            if (gpaEdit != null && !gpaEdit.isBlank()) {
                predicates.add(cb.equal(root.get("gpaEdit"), Boolean.valueOf(gpaEdit)));
            }
            if (accreditationEdit != null && !accreditationEdit.isBlank()) {
                predicates.add(cb.equal(root.get("accreditationEdit"), Boolean.valueOf(accreditationEdit)));
            }
            if (addStudent != null && !addStudent.isBlank()) {
                predicates.add(cb.equal(root.get("addStudent"), Boolean.valueOf(addStudent)));
            }
            if (allowGrouping != null && !allowGrouping.isBlank()) {
                predicates.add(cb.equal(root.get("allowGrouping"), Boolean.valueOf(allowGrouping)));
            }
            if (allowTransferOutside != null && !allowTransferOutside.isBlank()) {
                predicates.add(cb.equal(root.get("allowTransferOutside"), Boolean.valueOf(allowTransferOutside)));
            }
            if (oneId != null && !oneId.isBlank()) {
                predicates.add(cb.equal(root.get("oneId"), Boolean.valueOf(oneId)));
            }
            if (gradingSystem != null && !gradingSystem.isBlank()) {
                predicates.add(cb.equal(root.get("gradingSystem"), Boolean.valueOf(gradingSystem)));
            }
            if (addForeignStudent != null && !addForeignStudent.isBlank()) {
                predicates.add(cb.equal(root.get("addForeignStudent"), Boolean.valueOf(addForeignStudent)));
            }
            if (addTransferStudent != null && !addTransferStudent.isBlank()) {
                predicates.add(cb.equal(root.get("addTransferStudent"), Boolean.valueOf(addTransferStudent)));
            }
            if (addAcademicMobileStudent != null && !addAcademicMobileStudent.isBlank()) {
                predicates.add(cb.equal(root.get("addAcademicMobileStudent"), Boolean.valueOf(addAcademicMobileStudent)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Load classifier items from a legacy hemishe_h_* table
     */
    @SuppressWarnings("unchecked")
    private List<DictionaryItem> loadClassifierItems(String sql) {
        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(row -> DictionaryItem.builder()
                        .code((String) row[0])
                        .name((String) row[1])
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Load region items from soato (top-level regions only: code length = 4).
     * Yangi jadval — Bosqich 4.5.
     */
    @SuppressWarnings("unchecked")
    private List<DictionaryItem> loadRegionItems() {
        Query query = entityManager.createNativeQuery(
                "SELECT code, name FROM soato " +
                "WHERE is_active = true AND LENGTH(code) = 4 " +
                "ORDER BY name");

        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(row -> DictionaryItem.builder()
                        .code((String) row[0])
                        .name((String) row[1])
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Load district items from soato (7-digit SOATO codes — tuman/shahar).
     * Includes inactive entries so historical universities still resolve to a name on display.
     * Yangi jadval — Bosqich 4.5.
     */
    @SuppressWarnings("unchecked")
    private List<DictionaryItem> loadDistrictItems() {
        Query query = entityManager.createNativeQuery(
                "SELECT code, name FROM soato " +
                "WHERE LENGTH(code) = 7 " +
                "ORDER BY name");

        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(row -> DictionaryItem.builder()
                        .code((String) row[0])
                        .name((String) row[1])
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Load a code→name map from a classifier table (for CSV export)
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> loadNameMap(String sql) {
        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (String) row[1],
                        (a, b) -> a
                ));
    }

    /**
     * Map request DTO to entity
     */
    private void mapRequestToEntity(UniversityRequestDto request, University entity) {
        // Required fields — always set
        entity.setCode(request.getCode());
        entity.setName(request.getName());

        // Optional fields — only update if provided (null = don't change)
        if (request.getTin() != null) entity.setTin(request.getTin());
        if (request.getOwnership() != null) entity.setOwnership(request.getOwnership());
        if (request.getSoato() != null) entity.setSoato(request.getSoato());
        if (request.getSoatoRegion() != null) entity.setSoatoRegion(request.getSoatoRegion());
        if (request.getUniversityType() != null) entity.setUniversityType(request.getUniversityType());
        if (request.getUniversityVersion() != null) entity.setUniversityVersion(request.getUniversityVersion());
        if (request.getActivityStatus() != null) entity.setUniversityActivityStatus(request.getActivityStatus());
        if (request.getBelongsTo() != null) entity.setUniversityBelongsTo(request.getBelongsTo());
        if (request.getContractCategory() != null) entity.setUniversityContractCategory(request.getContractCategory());
        if (request.getParentUniversity() != null) entity.setParentUniversity(request.getParentUniversity());
        if (request.getTerrain() != null) entity.setTerrain(request.getTerrain());
        if (request.getAddress() != null) entity.setAddress(request.getAddress());
        if (request.getCadastre() != null) entity.setCadastre(request.getCadastre());
        if (request.getUniversityUrl() != null) entity.setUniversityUrl(request.getUniversityUrl());
        if (request.getStudentUrl() != null) entity.setStudentUrl(request.getStudentUrl());
        if (request.getTeacherUrl() != null) entity.setTeacherUrl(request.getTeacherUrl());
        if (request.getUzbmbUrl() != null) entity.setUzbmbUrl(request.getUzbmbUrl());
        if (request.getMailAddress() != null) entity.setMailAddress(request.getMailAddress());
        if (request.getAccreditationInfo() != null) entity.setAccreditationInfo(request.getAccreditationInfo());
        if (request.getBankInfo() != null) entity.setBankInfo(request.getBankInfo());

        // Boolean fields — always have defaults
        if (request.getActive() != null) entity.setActive(request.getActive());
        if (request.getGpaEdit() != null) entity.setGpaEdit(request.getGpaEdit());
        if (request.getAccreditationEdit() != null) entity.setAccreditationEdit(request.getAccreditationEdit());
        if (request.getAddStudent() != null) entity.setAddStudent(request.getAddStudent());
        if (request.getAllowGrouping() != null) entity.setAllowGrouping(request.getAllowGrouping());
        if (request.getAllowTransferOutside() != null) entity.setAllowTransferOutside(request.getAllowTransferOutside());
        if (request.getOneId() != null) entity.setOneId(request.getOneId());
        if (request.getGradingSystem() != null) entity.setGradingSystem(request.getGradingSystem());
        if (request.getAddForeignStudent() != null) entity.setAddForeignStudent(request.getAddForeignStudent());
        if (request.getAddTransferStudent() != null) entity.setAddTransferStudent(request.getAddTransferStudent());
        if (request.getAddAcademicMobileStudent() != null) entity.setAddAcademicMobileStudent(request.getAddAcademicMobileStudent());
    }
}
