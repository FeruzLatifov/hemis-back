package uz.hemis.service.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.audit.AuditAction;
import uz.hemis.common.audit.Audited;
import uz.hemis.common.dto.building.BuildingCreateUpdateDto;
import uz.hemis.common.dto.building.BuildingDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.infrastructure.BuildingLifecycle;
import uz.hemis.domain.entity.infrastructure.BuildingLifecycle.EventType;
import uz.hemis.domain.entity.infrastructure.UniversityBuilding;
import uz.hemis.domain.repository.BuildingLifecycleRepository;
import uz.hemis.domain.repository.UniversityBuildingRepository;
import uz.hemis.service.infrastructure.mapper.BuildingMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Bino CRUD — ministry admin web API uchun.
 *
 * <p>Business rules:
 * <ul>
 *   <li>Create: cad_number bo'lsa cadastre'dan auto-fill; CONSTRUCTED lifecycle event</li>
 *   <li>Update: last_renovation_date o'zgarsa RENOVATED lifecycle event avtomatik</li>
 *   <li>Delete: soft-delete (deleted_at) — ma'lumot saqlanadi</li>
 * </ul></p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UniversityBuildingService {

    private static final String DASHBOARD_CACHE = "universityDashboard";

    private final UniversityBuildingRepository repo;
    private final BuildingLifecycleRepository lifecycleRepo;
    private final BuildingMapper mapper;
    private final CacheManager cacheManager;
    private final CadastreIngestService cadastreIngestService;

    @Transactional(readOnly = true)
    public BuildingDto findById(UUID id) {
        return repo.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Building", "id", id));
    }

    @Transactional(readOnly = true)
    public Page<BuildingDto> findByUniversity(String universityCode, Pageable pageable) {
        return repo.findByUniversityCode(universityCode, pageable).map(mapper::toDto);
    }

    /** OTM serve — universitetning o'z binolari (token-scoped, paginatsiyasiz). */
    @Transactional(readOnly = true)
    public java.util.List<BuildingDto> findAllByUniversity(String universityCode) {
        return repo.findByUniversityCodeOrderByNameAsc(universityCode)
                .stream().map(mapper::toDto).toList();
    }

    @Transactional
    @Audited(action = AuditAction.CREATE, entity = "Building", entityClass = UniversityBuilding.class)
    public BuildingDto create(String universityCode, BuildingCreateUpdateDto dto) {
        UniversityBuilding building = mapper.toEntity(dto);
        building.setUniversityCode(universityCode);
        // FK: cad_number bo'lsa university_cadastre qatorini ta'minlaymiz (yo'q → fetch/PENDING; noto'g'ri → 422)
        cadastreIngestService.ensureCadastreExists(building.getCadNumber(), true);
        UniversityBuilding saved = repo.save(building);
        recordConstructionEvent(saved);
        evictDashboard(universityCode);
        log.info("Building created: {} / {}", universityCode, saved.getId());
        return mapper.toDto(saved);
    }

    @Transactional
    @Audited(action = AuditAction.UPDATE, entity = "Building", entityClass = UniversityBuilding.class, keyArg = "id")
    public BuildingDto update(UUID id, BuildingCreateUpdateDto dto) {
        UniversityBuilding existing = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building", "id", id));
        LocalDate oldRenovationDate = existing.getLastRenovationDate();
        mapper.updateEntity(dto, existing);
        // FK: cad_number o'zgargan bo'lishi mumkin — yangi qiymatga kadastr qatorini ta'minlaymiz
        cadastreIngestService.ensureCadastreExists(existing.getCadNumber(), true);
        recordRenovationIfChanged(existing, oldRenovationDate);
        evictDashboard(existing.getUniversityCode());
        log.info("Building updated: {}", id);
        return mapper.toDto(existing); // dirty-check saves
    }

    @Transactional
    @Audited(action = AuditAction.DELETE, entity = "Building", entityClass = UniversityBuilding.class, keyArg = "id")
    public void softDelete(UUID id) {
        UniversityBuilding building = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building", "id", id));
        building.setDeletedAt(LocalDateTime.now());
        evictDashboard(building.getUniversityCode());
        log.info("Building soft-deleted: {}", id);
    }

    /** Dashboard cache (universityDashboard) selective evict — building mutation tetiklaydi. */
    private void evictDashboard(String universityCode) {
        if (universityCode == null) return;
        Cache cache = cacheManager.getCache(DASHBOARD_CACHE);
        if (cache != null) cache.evict(universityCode);
    }

    // =====================================================
    // Lifecycle event helpers (data irreversibility principle)
    // =====================================================

    /** Yangi bino yaratilganda — CONSTRUCTED event (agar qurilish yili ma'lum bo'lsa). */
    private void recordConstructionEvent(UniversityBuilding b) {
        if (b.getYearBuilt() == null) {
            return;
        }
        lifecycleRepo.save(BuildingLifecycle.builder()
                .buildingId(b.getId())
                .eventType(EventType.CONSTRUCTED)
                .eventDate(LocalDate.of(b.getYearBuilt(), 1, 1))
                .note("Initial record from building creation")
                .build());
    }

    /** last_renovation_date o'zgarsa — RENOVATED event yoziladi (tarix saqlash). */
    private void recordRenovationIfChanged(UniversityBuilding b, LocalDate oldDate) {
        LocalDate newDate = b.getLastRenovationDate();
        if (Objects.equals(oldDate, newDate) || newDate == null) {
            return;
        }
        lifecycleRepo.save(BuildingLifecycle.builder()
                .buildingId(b.getId())
                .eventType(EventType.RENOVATED)
                .eventDate(newDate)
                .note("Auto-recorded from lastRenovationDate update")
                .build());
    }
}
