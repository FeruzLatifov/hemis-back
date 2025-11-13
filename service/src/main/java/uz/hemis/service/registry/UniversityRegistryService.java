package uz.hemis.service.registry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.UniversityDto;
import uz.hemis.domain.entity.University;
import uz.hemis.service.mapper.UniversityMapper;
import uz.hemis.domain.repository.UniversityRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * University Registry Service
 * 
 * READS FROM: READ REPLICA Database
 * - @Transactional(readOnly=true) ensures REPLICA routing
 * - All queries optimized for read-only operations
 * - Zero load on MASTER database
 * 
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UniversityRegistryService {

    private final UniversityRepository universityRepository;
    private final UniversityMapper universityMapper;

    @Cacheable(
            value = "universitiesSearch",
            key = "#q + ':' + #regionId + ':' + #ownershipId + ':' + #typeId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()",
            unless = "#result == null || #pageable.pageNumber > 10" // Limit caching of very deep pages
    )
    public Page<UniversityDto> searchUniversities(
            String q,
            String regionId,
            String ownershipId,
            String typeId,
            Pageable pageable
    ) {
        log.debug("Searching universities: q={}, regionId={}, ownershipId={}, typeId={}", 
                  q, regionId, ownershipId, typeId);

        Specification<University> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (q != null && !q.isBlank()) {
                String searchPattern = "%" + q.toLowerCase() + "%";
                Predicate nameLike = cb.like(cb.lower(root.get("name")), searchPattern);
                Predicate codeLike = cb.like(cb.lower(root.get("code")), searchPattern);
                Predicate tinLike = cb.like(cb.lower(root.get("tin")), searchPattern);
                predicates.add(cb.or(nameLike, codeLike, tinLike));
            }

            if (regionId != null && !regionId.isBlank()) {
                predicates.add(cb.equal(root.get("soatoRegion"), regionId));
            }

            if (ownershipId != null && !ownershipId.isBlank()) {
                predicates.add(cb.equal(root.get("ownership"), ownershipId));
            }

            if (typeId != null && !typeId.isBlank()) {
                predicates.add(cb.equal(root.get("universityType"), typeId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

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
    public Map<String, Object> getDictionaries() {
        log.debug("Getting dictionaries for university filters");
        Map<String, Object> dictionaries = new HashMap<>();
        
        List<Map<String, String>> ownerships = new ArrayList<>();
        ownerships.add(Map.of("code", "11", "name", "Davlat"));
        ownerships.add(Map.of("code", "12", "name", "Xususiy"));
        
        List<Map<String, String>> types = new ArrayList<>();
        types.add(Map.of("code", "11", "name", "Universitet"));
        types.add(Map.of("code", "12", "name", "Institut"));
        types.add(Map.of("code", "13", "name", "Akademiya"));
        
        List<Map<String, String>> regions = new ArrayList<>();
        regions.add(Map.of("code", "26", "name", "Toshkent shahar"));
        regions.add(Map.of("code", "27", "name", "Toshkent viloyati"));
        regions.add(Map.of("code", "03", "name", "Andijon viloyati"));
        regions.add(Map.of("code", "06", "name", "Buxoro viloyati"));
        regions.add(Map.of("code", "09", "name", "Jizzax viloyati"));
        regions.add(Map.of("code", "10", "name", "Qashqadaryo viloyati"));
        regions.add(Map.of("code", "12", "name", "Navoiy viloyati"));
        regions.add(Map.of("code", "13", "name", "Namangan viloyati"));
        regions.add(Map.of("code", "14", "name", "Samarqand viloyati"));
        regions.add(Map.of("code", "15", "name", "Surxondaryo viloyati"));
        regions.add(Map.of("code", "17", "name", "Sirdaryo viloyati"));
        regions.add(Map.of("code", "18", "name", "Farg'ona viloyati"));
        regions.add(Map.of("code", "19", "name", "Xorazm viloyati"));
        regions.add(Map.of("code", "23", "name", "Qoraqalpog'iston Respublikasi"));
        
        dictionaries.put("ownerships", ownerships);
        dictionaries.put("types", types);
        dictionaries.put("regions", regions);
        
        return dictionaries;
    }

    public List<UniversityDto> exportUniversities(
            String q,
            String regionId,
            String ownershipId,
            String typeId
    ) {
        log.debug("Exporting universities: q={}, regionId={}, ownershipId={}, typeId={}", 
                  q, regionId, ownershipId, typeId);

        Specification<University> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (q != null && !q.isBlank()) {
                String searchPattern = "%" + q.toLowerCase() + "%";
                Predicate nameLike = cb.like(cb.lower(root.get("name")), searchPattern);
                Predicate codeLike = cb.like(cb.lower(root.get("code")), searchPattern);
                Predicate tinLike = cb.like(cb.lower(root.get("tin")), searchPattern);
                predicates.add(cb.or(nameLike, codeLike, tinLike));
            }

            if (regionId != null && !regionId.isBlank()) {
                predicates.add(cb.equal(root.get("soatoRegion"), regionId));
            }

            if (ownershipId != null && !ownershipId.isBlank()) {
                predicates.add(cb.equal(root.get("ownership"), ownershipId));
            }

            if (typeId != null && !typeId.isBlank()) {
                predicates.add(cb.equal(root.get("universityType"), typeId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<University> universities = universityRepository.findAll(spec);
        return universityMapper.toDtoList(universities);
    }
    
    /**
     * Create new university (WRITES TO MASTER)
     */
    @Transactional
    @CacheEvict(value = {"universitiesSearch","universityDictionaries"}, allEntries = true)
    public UniversityDto createUniversity(uz.hemis.service.registry.dto.UniversityRequestDto request) {
        log.info("Creating university: code={}, name={}", request.getCode(), request.getName());
        
        // Check if code already exists
        if (universityRepository.existsById(request.getCode())) {
            throw new IllegalArgumentException("University with code " + request.getCode() + " already exists");
        }
        
        University university = new University();
        mapRequestToEntity(request, university);
        university.setVersion(1);
        university.setCreateTs(java.time.LocalDateTime.now());
        
        University saved = universityRepository.save(university);
        log.info("University created: {}", saved.getCode());
        
        return universityMapper.toDto(saved);
    }
    
    /**
     * Update existing university (WRITES TO MASTER)
     */
    @Transactional
    @Caching(evict = {
        @CacheEvict(value="universitiesSearch", allEntries=true),
        @CacheEvict(value="universityDictionaries", allEntries=true),
        @CacheEvict(value="universitiesSearch", key="'detail:' + #code")
    })
    public UniversityDto updateUniversity(String code, uz.hemis.service.registry.dto.UniversityRequestDto request) {
        log.info("Updating university: {}", code);
        
        University university = universityRepository.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("University not found: " + code));
        
        mapRequestToEntity(request, university);
        university.setVersion(university.getVersion() + 1);
        university.setUpdateTs(java.time.LocalDateTime.now());
        
        University saved = universityRepository.save(university);
        log.info("University updated: {}", saved.getCode());
        
        return universityMapper.toDto(saved);
    }
    
    /**
     * Delete university (soft delete - WRITES TO MASTER)
     */
    @Transactional
    @Caching(evict = {
        @CacheEvict(value="universitiesSearch", allEntries=true),
        @CacheEvict(value="universityDictionaries", allEntries=true),
        @CacheEvict(value="universitiesSearch", key="'detail:' + #code")
    })
    public void deleteUniversity(String code) {
        log.info("Deleting university: {}", code);
        
        University university = universityRepository.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("University not found: " + code));
        
        university.setDeleteTs(java.time.LocalDateTime.now());
        university.setVersion(university.getVersion() + 1);
        universityRepository.save(university);
        
        log.info("University deleted (soft): {}", code);
    }
    
    /**
     * Map request DTO to entity
     */
    private void mapRequestToEntity(uz.hemis.service.registry.dto.UniversityRequestDto request, University entity) {
        entity.setCode(request.getCode());
        entity.setTin(request.getTin());
        entity.setName(request.getName());
        entity.setOwnership(request.getOwnership());
        entity.setSoato(request.getSoato());
        entity.setSoatoRegion(request.getSoatoRegion());
        entity.setUniversityType(request.getUniversityType());
        entity.setUniversityVersion(request.getUniversityVersion());
        entity.setUniversityActivityStatus(request.getActivityStatus());  // Fixed
        entity.setUniversityBelongsTo(request.getBelongsTo());            // Fixed
        entity.setUniversityContractCategory(request.getContractCategory()); // Fixed
        entity.setParentUniversity(request.getParentUniversity());
        entity.setTerrain(request.getTerrain());
        entity.setVersionType(request.getVersionType());
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

