package uz.hemis.service.infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.hemis.common.dto.building.BuildingLifecycleDto;
import uz.hemis.domain.entity.infrastructure.BuildingLifecycle;
import uz.hemis.domain.repository.BuildingLifecycleRepository;
import uz.hemis.service.infrastructure.mapper.BuildingMapper;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BuildingLifecycleService")
class BuildingLifecycleServiceTest {

    @Mock private BuildingLifecycleRepository repo;
    @Mock private BuildingMapper mapper;

    @InjectMocks
    private BuildingLifecycleService service;

    @Test
    void getHistory_orderedByEventDateDesc() {
        UUID buildingId = UUID.randomUUID();
        BuildingLifecycle e1 = new BuildingLifecycle();
        BuildingLifecycle e2 = new BuildingLifecycle();
        BuildingLifecycleDto d1 = BuildingLifecycleDto.builder().build();
        BuildingLifecycleDto d2 = BuildingLifecycleDto.builder().build();

        when(repo.findByBuildingIdOrderByEventDateDesc(buildingId)).thenReturn(List.of(e1, e2));
        when(mapper.toLifecycleDto(e1)).thenReturn(d1);
        when(mapper.toLifecycleDto(e2)).thenReturn(d2);

        List<BuildingLifecycleDto> result = service.getHistory(buildingId);

        assertThat(result).containsExactly(d1, d2);
    }

    @Test
    void getHistory_empty_returnsEmptyList() {
        UUID buildingId = UUID.randomUUID();
        when(repo.findByBuildingIdOrderByEventDateDesc(buildingId)).thenReturn(List.of());

        assertThat(service.getHistory(buildingId)).isEmpty();
    }
}
