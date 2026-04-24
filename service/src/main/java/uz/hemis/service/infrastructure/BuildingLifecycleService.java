package uz.hemis.service.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.building.BuildingLifecycleDto;
import uz.hemis.domain.repository.BuildingLifecycleRepository;
import uz.hemis.service.infrastructure.mapper.BuildingMapper;

import java.util.List;
import java.util.UUID;

/**
 * Bino tarixi — immutable read-only endpoint.
 * Event yaratish UniversityBuildingService'dan avtomatik (CONSTRUCTED, RENOVATED).
 */
@Service
@RequiredArgsConstructor
public class BuildingLifecycleService {

    private final BuildingLifecycleRepository repo;
    private final BuildingMapper mapper;

    @Transactional(readOnly = true)
    public List<BuildingLifecycleDto> getHistory(UUID buildingId) {
        return repo.findByBuildingIdOrderByEventDateDesc(buildingId)
                .stream()
                .map(mapper::toLifecycleDto)
                .toList();
    }
}
