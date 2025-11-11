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
import uz.hemis.domain.mapper.UniversityMapper;
import uz.hemis.domain.repository.UniversityRepository;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UniversityRegistryService {

    private final UniversityRepository universityRepository;
    private final UniversityMapper universityMapper;

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

    public UniversityDto getUniversityById(String id) {
        log.debug("Getting university by id: {}", id);
        return universityRepository.findById(id)
                .map(universityMapper::toDto)
                .orElse(null);
    }

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
}

