package uz.hemis.service.registry;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.Predicate;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import uz.hemis.service.security.TenantGuard;
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

    /**
     * Sort whitelist — frontend ixtiyoriy property nomi yuborolmaydi (security + cache hygiene).
     * Whitelisted bo'lmagan property'lar olib tashlanadi; bo'sh sort default {@code code ASC} ga
     * tushadi. 21 filter parametr × Sort permutation cache key ko'payishini chegaralaydi
     * (har "name,asc" / "name,ASC" / "name" → bitta normalized variant).
     */
    private static final Set<String> SORT_WHITELIST = Set.of(
            "code", "name", "tin", "createTs", "updateTs"
    );
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Order.asc("code"));

    private final UniversityRepository universityRepository;
    private final UniversityDtoConverter universityMapper;
    private final EntityManager entityManager;
    private final ClassifierLookupService classifiers;
    private final TenantGuard tenantGuard;

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

        // Sort'ni whitelist orqali normalize qilamiz — cache key permutation va SQL injection
        // (Sort property name SpEL/JPQL'ga oqib chiqishi mumkin) himoyasi.
        Pageable normalized = normalizePageable(pageable);

        Specification<University> spec = buildFilterSpecification(q, searchField, regionId, ownershipId, typeId,
                activityStatusId, belongsToId, contractCategoryId, versionTypeId,
                districtId, active, gpaEdit, accreditationEdit, addStudent, allowGrouping, allowTransferOutside,
                oneId, gradingSystem, addForeignStudent, addTransferStudent, addAcademicMobileStudent);
        Page<University> universities = universityRepository.findAll(spec, normalized);
        return universities.map(u -> enrich(universityMapper.toDto(u)));
    }

    /**
     * Apply Sort whitelist; preserves direction but drops unknown property names.
     * Empty result → {@link #DEFAULT_SORT} ({@code code ASC}).
     */
    private Pageable normalizePageable(Pageable pageable) {
        if (pageable == null || pageable.getSort().isUnsorted()) {
            return pageable == null
                    ? PageRequest.of(0, 20, DEFAULT_SORT)
                    : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), DEFAULT_SORT);
        }
        LinkedHashSet<Sort.Order> orders = new LinkedHashSet<>();
        for (Sort.Order o : pageable.getSort()) {
            if (SORT_WHITELIST.contains(o.getProperty())) {
                orders.add(o);
            }
        }
        Sort safeSort = orders.isEmpty() ? DEFAULT_SORT : Sort.by(orders.toArray(new Sort.Order[0]));
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), safeSort);
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

        // M8 — UNION ALL: 6 ta classifier query → bitta DB roundtrip
        // (cache MISS holatida — har deploy/restart). Region/district alohida
        // chunki ular `hemishe_h_soato` da LENGTH(code) filter bilan.
        Map<String, List<DictionaryItem>> classifiers = loadClassifierItemsBatch();

        return UniversityDictionariesDto.builder()
                .ownerships(classifiers.getOrDefault("ownership", List.of()))
                .types(classifiers.getOrDefault("type", List.of()))
                .regions(loadRegionItems())
                .activityStatuses(classifiers.getOrDefault("activity_status", List.of()))
                .belongsToOptions(classifiers.getOrDefault("belongs_to", List.of()))
                .contractCategories(classifiers.getOrDefault("contract_category", List.of()))
                .versionTypes(classifiers.getOrDefault("version_type", List.of()))
                .districts(loadDistrictItems())
                .build();
    }

    /**
     * Single UNION ALL query — fetches 6 classifier datasets in one DB roundtrip.
     * Ordering: classifier tag (asc), then name (asc) for deterministic dropdown order.
     */
    @SuppressWarnings("unchecked")
    private Map<String, List<DictionaryItem>> loadClassifierItemsBatch() {
        String sql =
                "SELECT 'ownership' AS tag, code, name FROM hemishe_h_ownership "
                        + "  WHERE delete_ts IS NULL AND active = true "
                        + "UNION ALL "
                        + "SELECT 'type' AS tag, code, name FROM hemishe_h_university_type "
                        + "  WHERE delete_ts IS NULL AND active = true "
                        + "UNION ALL "
                        + "SELECT 'activity_status' AS tag, code, name FROM hemishe_h_university_activity_status "
                        + "  WHERE delete_ts IS NULL "
                        + "UNION ALL "
                        + "SELECT 'belongs_to' AS tag, code, name FROM hemishe_h_university_belongs_to "
                        + "  WHERE delete_ts IS NULL "
                        + "UNION ALL "
                        + "SELECT 'contract_category' AS tag, code, name FROM hemishe_h_university_contract_category "
                        + "  WHERE delete_ts IS NULL "
                        + "UNION ALL "
                        + "SELECT 'version_type' AS tag, code, name FROM hemishe_h_hemis_version_type "
                        + "  WHERE delete_ts IS NULL "
                        + "ORDER BY tag, name";
        List<Object[]> rows = entityManager.createNativeQuery(sql).getResultList();
        Map<String, List<DictionaryItem>> grouped = new java.util.HashMap<>();
        for (Object[] row : rows) {
            String tag = (String) row[0];
            grouped.computeIfAbsent(tag, k -> new ArrayList<>())
                    .add(DictionaryItem.builder()
                            .code((String) row[1])
                            .name((String) row[2])
                            .build());
        }
        return grouped;
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<DictionaryItem> getTerrainsBySoato(String soato) {
        Query query = entityManager.createNativeQuery(
                "SELECT code, name FROM hemishe_h_terrain WHERE _soato = :soato AND delete_ts IS NULL AND active = true ORDER BY name");
        query.setParameter("soato", soato);
        List<Object[]> results = query.getResultList();
        return results.stream()
                .map(row -> DictionaryItem.builder()
                        .code((String) row[0])
                        .name((String) row[1])
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Hard limit for export — prevents memory exhaustion when filter is too broad.
     * 224 OTM ekosistemi uchun butun ro'yxat 230 row, lekin filter notilik bilan
     * yondashilsa boshqa kelajakdagi joinlar 1M+ row qaytarishi mumkin.
     */
    private static final int EXPORT_HARD_LIMIT = 5000;

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
        // Hard limit (5000) — memory exhaustion oldini olish.
        Pageable exportPage = PageRequest.of(0, EXPORT_HARD_LIMIT, DEFAULT_SORT);
        Page<University> page = universityRepository.findAll(spec, exportPage);
        if (page.getTotalElements() > EXPORT_HARD_LIMIT) {
            log.warn("Export truncated: filter matched {} rows, returning first {} (hard limit)",
                    page.getTotalElements(), EXPORT_HARD_LIMIT);
        }
        return universityMapper.toDtoList(page.getContent()).stream().map(this::enrich).toList();
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
    @Caching(evict = {
        @CacheEvict(value = "universitiesSearch", allEntries = true),
        @CacheEvict(value = "universityDictionaries", allEntries = true),
        // Cross-service desync prevention — UniversityService cache'lar ham yangilanishi kerak
        @CacheEvict(value = "universityList", allEntries = true),
        @CacheEvict(value = "universityActive", allEntries = true),
        @CacheEvict(value = "universityChildren", allEntries = true)
    })
    public UniversityDto createUniversity(UniversityRequestDto request) {
        log.info("Creating university: code={}, name={}", request.getCode(), request.getName());

        if (universityRepository.existsByCode(request.getCode())) {
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
        @CacheEvict(value = "universitiesSearch", key = "'detail:' + #code"),
        // Cross-service desync prevention — UniversityService cache namespacelar.
        // universityNullable cache producer codebase'da yo'q (dead evict) — olib tashlandi.
        @CacheEvict(value = "university", key = "#code"),
        @CacheEvict(value = "universityList", allEntries = true),
        @CacheEvict(value = "universityActive", allEntries = true),
        @CacheEvict(value = "universityChildren", allEntries = true),
        @CacheEvict(value = "universityDashboard", key = "#code")
    })
    public UniversityDto updateUniversity(String code, UniversityRequestDto request) {
        log.info("Updating university: {}", code);
        tenantGuard.verifyOwnershipOrAdmin(code);

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
        @CacheEvict(value = "universitiesSearch", key = "'detail:' + #code"),
        // Cross-service desync prevention — universityNullable olib tashlandi (producer yo'q).
        @CacheEvict(value = "university", key = "#code"),
        @CacheEvict(value = "universityList", allEntries = true),
        @CacheEvict(value = "universityActive", allEntries = true),
        @CacheEvict(value = "universityChildren", allEntries = true),
        @CacheEvict(value = "universityDashboard", key = "#code")
    })
    public void deleteUniversity(String code) {
        log.info("Deleting university: {}", code);
        tenantGuard.verifyOwnershipOrAdmin(code);

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
     * Load district items from hemishe_h_soato (7-digit SOATO codes — tuman/shahar).
     * Includes inactive entries so historical universities still resolve to a name on display.
     */
    @SuppressWarnings("unchecked")
    private List<DictionaryItem> loadDistrictItems() {
        Query query = entityManager.createNativeQuery(
                "SELECT code, name_uz FROM hemishe_h_soato " +
                        "WHERE delete_ts IS NULL AND LENGTH(code) = 7 " +
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
