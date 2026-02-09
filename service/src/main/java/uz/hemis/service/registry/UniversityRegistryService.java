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
import uz.hemis.domain.entity.University;
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

    // =====================================================
    // READ Operations (REPLICA Database)
    // =====================================================

    @Cacheable(
            value = "universitiesSearch",
            key = "#q + ':' + #searchField + ':' + #regionId + ':' + #ownershipId + ':' + #typeId + ':' + #activityStatusId + ':' + #belongsToId + ':' + #contractCategoryId + ':' + #versionTypeId + ':' + #districtId + ':' + #active + ':' + #gpaEdit + ':' + #accreditationEdit + ':' + #addStudent + ':' + #allowGrouping + ':' + #allowTransferOutside + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()",
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
            Pageable pageable
    ) {
        log.debug("Searching universities: q={}, searchField={}, regionId={}, ownershipId={}, typeId={}",
                  q, searchField, regionId, ownershipId, typeId);

        Specification<University> spec = buildFilterSpecification(q, searchField, regionId, ownershipId, typeId,
                activityStatusId, belongsToId, contractCategoryId, versionTypeId,
                districtId, active, gpaEdit, accreditationEdit, addStudent, allowGrouping, allowTransferOutside);
        Page<University> universities = universityRepository.findAll(spec, pageable);
        return universities.map(universityMapper::toDto);
    }

    @Cacheable(value = "universitiesSearch", key = "'detail:' + #id", unless = "#result == null")
    public UniversityDto getUniversityById(String id) {
        log.debug("Getting university by id: {}", id);
        return universityRepository.findById(id)
                .map(universityMapper::toDto)
                .orElse(null);
    }

    @Cacheable(value = "universityDictionaries", key = "'all'", unless = "#result == null")
    public UniversityDictionariesDto getDictionaries() {
        log.debug("Loading university dictionaries from database");

        List<DictionaryItem> ownerships = loadClassifierItems(
                "SELECT code, name FROM hemishe_h_ownership WHERE delete_ts IS NULL AND active = true ORDER BY name");

        List<DictionaryItem> types = loadClassifierItems(
                "SELECT code, name FROM hemishe_h_university_type WHERE delete_ts IS NULL AND active = true ORDER BY name");

        List<DictionaryItem> regions = loadRegionItems();

        List<DictionaryItem> activityStatuses = loadClassifierItems(
                "SELECT code, name FROM hemishe_h_university_activity_status WHERE delete_ts IS NULL ORDER BY name");

        List<DictionaryItem> belongsToOptions = loadClassifierItems(
                "SELECT code, name FROM hemishe_h_university_belongs_to WHERE delete_ts IS NULL ORDER BY name");

        List<DictionaryItem> contractCategories = loadClassifierItems(
                "SELECT code, name FROM hemishe_h_university_contract_category WHERE delete_ts IS NULL ORDER BY name");

        List<DictionaryItem> versionTypes = loadClassifierItems(
                "SELECT code, name FROM hemishe_h_hemis_version_type WHERE delete_ts IS NULL ORDER BY name");

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
            String allowTransferOutside
    ) {
        log.debug("Exporting universities: q={}, searchField={}, regionId={}, ownershipId={}, typeId={}",
                  q, searchField, regionId, ownershipId, typeId);

        Specification<University> spec = buildFilterSpecification(q, searchField, regionId, ownershipId, typeId,
                activityStatusId, belongsToId, contractCategoryId, versionTypeId,
                districtId, active, gpaEdit, accreditationEdit, addStudent, allowGrouping, allowTransferOutside);
        List<University> universities = universityRepository.findAll(spec);
        return universityMapper.toDtoList(universities);
    }

    /**
     * Build name lookup maps for CSV export (code → name resolution)
     */
    public Map<String, String> getRegionNameMap() {
        return loadNameMap("SELECT code, name_uz FROM hemishe_h_soato WHERE delete_ts IS NULL AND active = true AND LENGTH(code) = 4");
    }

    public Map<String, String> getOwnershipNameMap() {
        return loadNameMap("SELECT code, name FROM hemishe_h_ownership WHERE delete_ts IS NULL AND active = true");
    }

    public Map<String, String> getUniversityTypeNameMap() {
        return loadNameMap("SELECT code, name FROM hemishe_h_university_type WHERE delete_ts IS NULL AND active = true");
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
            String addStudent, String allowGrouping, String allowTransferOutside
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
     * Load region items from hemishe_h_soato (top-level regions only: code length = 4)
     */
    @SuppressWarnings("unchecked")
    private List<DictionaryItem> loadRegionItems() {
        Query query = entityManager.createNativeQuery(
                "SELECT code, name_uz FROM hemishe_h_soato " +
                "WHERE delete_ts IS NULL AND active = true AND LENGTH(code) = 4 " +
                "ORDER BY name_uz");

        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(row -> DictionaryItem.builder()
                        .code((String) row[0])
                        .name((String) row[1])
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Load district items from hemishe_h_soato (7-digit SOATO codes — tuman/shahar)
     */
    @SuppressWarnings("unchecked")
    private List<DictionaryItem> loadDistrictItems() {
        Query query = entityManager.createNativeQuery(
                "SELECT code, name_uz FROM hemishe_h_soato " +
                "WHERE delete_ts IS NULL AND active = true AND LENGTH(code) = 7 " +
                "ORDER BY name_uz");

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
        entity.setCode(request.getCode());
        entity.setTin(request.getTin());
        entity.setName(request.getName());
        entity.setOwnership(request.getOwnership());
        entity.setSoato(request.getSoato());
        entity.setSoatoRegion(request.getSoatoRegion());
        entity.setUniversityType(request.getUniversityType());
        entity.setUniversityVersion(request.getUniversityVersion());
        entity.setUniversityActivityStatus(request.getActivityStatus());
        entity.setUniversityBelongsTo(request.getBelongsTo());
        entity.setUniversityContractCategory(request.getContractCategory());
        entity.setParentUniversity(request.getParentUniversity());
        entity.setTerrain(request.getTerrain());
        entity.setAddress(request.getAddress());
        entity.setCadastre(request.getCadastre());
        entity.setUniversityUrl(request.getUniversityUrl());
        entity.setStudentUrl(request.getStudentUrl());
        entity.setTeacherUrl(request.getTeacherUrl());
        entity.setUzbmbUrl(request.getUzbmbUrl());
        entity.setMailAddress(request.getMailAddress());
        entity.setAccreditationInfo(request.getAccreditationInfo());
        entity.setBankInfo(request.getBankInfo());
        entity.setActive(request.getActive());
        entity.setGpaEdit(request.getGpaEdit());
        entity.setAccreditationEdit(request.getAccreditationEdit());
        entity.setAddStudent(request.getAddStudent());
        entity.setAllowGrouping(request.getAllowGrouping());
        entity.setAllowTransferOutside(request.getAllowTransferOutside());
    }
}
