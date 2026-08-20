package uz.hemis.service.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link UniversityBuildingService} unit testlar — Mockito.
 *
 * <p>Qamrab olingan business rules:
 * <ul>
 *   <li>findById — topilmasa ResourceNotFoundException</li>
 *   <li>create — cadastre auto-fill chaqiriladi, yearBuilt bo'lsa CONSTRUCTED event</li>
 *   <li>update — last_renovation_date o'zgarsa RENOVATED event avtomatik</li>
 *   <li>update — renovation o'zgarmasa lifecycle event YO'Q</li>
 *   <li>softDelete — deleted_at belgilanadi</li>
 * </ul></p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UniversityBuildingService Unit Tests")
class UniversityBuildingServiceTest {

    @Mock
    private UniversityBuildingRepository repo;

    @Mock
    private BuildingLifecycleRepository lifecycleRepo;

    @Mock
    private BuildingMapper mapper;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache dashboardCache;

    @InjectMocks
    private UniversityBuildingService service;

    private UUID buildingId;
    private UniversityBuilding building;
    private BuildingDto dto;

    @BeforeEach
    void setUp() {
        buildingId = UUID.randomUUID();
        building = new UniversityBuilding();
        building.setId(buildingId);
        building.setUniversityCode("401");
        building.setName("Bosh bino");
        dto = BuildingDto.builder().id(buildingId).name("Bosh bino").build();
        // Cache eviction stub — only used by mutation paths, lenient to avoid strict-stubbing fail.
        lenient().when(cacheManager.getCache(anyString())).thenReturn(dashboardCache);
    }

    @Test
    @DisplayName("findById — topilmaganda ResourceNotFoundException tashlaydi")
    void findById_whenNotFound_throwsException() {
        when(repo.findById(buildingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(buildingId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("create — cadastre auto-fill chaqiriladi, yearBuilt bo'lsa CONSTRUCTED event")
    void create_withYearBuilt_recordsConstructionEvent() {
        BuildingCreateUpdateDto createDto = BuildingCreateUpdateDto.builder()
                .name("Yangi bino")                .yearBuilt(2020)
                .build();
        UniversityBuilding entity = new UniversityBuilding();
        entity.setYearBuilt(2020);
        when(mapper.toEntity(createDto)).thenReturn(entity);
        when(repo.save(any(UniversityBuilding.class))).thenAnswer(inv -> {
            UniversityBuilding b = inv.getArgument(0);
            b.setId(UUID.randomUUID());
            return b;
        });
        when(mapper.toDto(any())).thenReturn(dto);

        service.create("401", createDto);

        // 2026-05-06: BuildingCadastreAutoFiller olib tashlandi (university_cadastre drop).
        // Endi faqat lifecycle event tekshiriladi.
        ArgumentCaptor<BuildingLifecycle> captor = ArgumentCaptor.forClass(BuildingLifecycle.class);
        verify(lifecycleRepo).save(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo(EventType.CONSTRUCTED);
        assertThat(captor.getValue().getEventDate()).isEqualTo(LocalDate.of(2020, 1, 1));
    }

    @Test
    @DisplayName("create — yearBuilt NULL bo'lsa CONSTRUCTED event yaratilmaydi")
    void create_withoutYearBuilt_noLifecycleEvent() {
        BuildingCreateUpdateDto createDto = BuildingCreateUpdateDto.builder()
                .name("Bino")                .build();
        UniversityBuilding entity = new UniversityBuilding();
        when(mapper.toEntity(createDto)).thenReturn(entity);
        when(repo.save(any())).thenReturn(entity);
        when(mapper.toDto(any())).thenReturn(dto);

        service.create("401", createDto);

        verify(lifecycleRepo, never()).save(any());
    }

    @Test
    @DisplayName("update — renovation sana o'zgarsa RENOVATED event avtomatik yoziladi")
    void update_whenRenovationDateChanged_recordsRenovatedEvent() {
        LocalDate oldDate = LocalDate.of(2015, 5, 1);
        LocalDate newDate = LocalDate.of(2024, 8, 15);
        building.setLastRenovationDate(oldDate);

        BuildingCreateUpdateDto updateDto = BuildingCreateUpdateDto.builder()
                .name(building.getName())                .lastRenovationDate(newDate)
                .build();

        when(repo.findById(buildingId)).thenReturn(Optional.of(building));
        when(mapper.toDto(building)).thenReturn(dto);
        // updateEntity simulates MapStruct behavior — update lastRenovationDate
        org.mockito.Mockito.doAnswer(inv -> {
            building.setLastRenovationDate(newDate);
            return null;
        }).when(mapper).updateEntity(updateDto, building);

        service.update(buildingId, updateDto);

        ArgumentCaptor<BuildingLifecycle> captor = ArgumentCaptor.forClass(BuildingLifecycle.class);
        verify(lifecycleRepo).save(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo(EventType.RENOVATED);
        assertThat(captor.getValue().getEventDate()).isEqualTo(newDate);
    }

    @Test
    @DisplayName("update — renovation sana o'zgarmasa lifecycle event yo'q")
    void update_whenRenovationDateUnchanged_noLifecycleEvent() {
        LocalDate date = LocalDate.of(2020, 1, 1);
        building.setLastRenovationDate(date);
        BuildingCreateUpdateDto updateDto = BuildingCreateUpdateDto.builder()
                .name("Yangi nom")                .lastRenovationDate(date)
                .build();
        when(repo.findById(buildingId)).thenReturn(Optional.of(building));
        when(mapper.toDto(building)).thenReturn(dto);

        service.update(buildingId, updateDto);

        verify(lifecycleRepo, never()).save(any());
    }

    @Test
    @DisplayName("softDelete — deleted_at belgilanadi, topilmasa exception")
    void softDelete_setsDeletedAtTimestamp() {
        when(repo.findById(buildingId)).thenReturn(Optional.of(building));

        service.softDelete(buildingId);

        assertThat(building.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("softDelete — topilmaganda ResourceNotFoundException")
    void softDelete_whenNotFound_throwsException() {
        when(repo.findById(buildingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.softDelete(buildingId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
