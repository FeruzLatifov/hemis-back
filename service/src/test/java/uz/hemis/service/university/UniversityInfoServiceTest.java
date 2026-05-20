package uz.hemis.service.university;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import uz.hemis.domain.entity.university.UniversityFounder;
import uz.hemis.domain.entity.university.UniversityLifecycle;
import uz.hemis.domain.repository.UniversityBuildingRepository;
import uz.hemis.domain.repository.UniversityFounderRepository;
import uz.hemis.domain.repository.UniversityLifecycleRepository;
import uz.hemis.service.infrastructure.mapper.BuildingMapper;
import uz.hemis.service.registry.ClassifierLookupService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UniversityInfoService — founders/lifecycle/dashboard/rector")
class UniversityInfoServiceTest {

    @Mock private UniversityFounderRepository founderRepository;
    @Mock private UniversityLifecycleRepository lifecycleRepository;
    @Mock private UniversityBuildingRepository buildingRepository;
    @Mock private BuildingMapper buildingMapper;
    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private ClassifierLookupService classifiers;

    @InjectMocks
    private UniversityInfoService service;

    @BeforeEach
    void setUp() {
        lenient().when(buildingRepository.findByUniversityCodeOrderByNameAsc(anyString()))
                .thenReturn(List.of());
        lenient().when(jdbcTemplate.queryForList(anyString(), anyString()))
                .thenReturn(List.of());
    }

    @Test
    void getFounders_delegatesToRepository() {
        UniversityFounder f = new UniversityFounder();
        when(founderRepository.findByUniversityCode("337")).thenReturn(List.of(f));

        assertThat(service.getFounders("337")).containsExactly(f);
    }

    @Test
    void getLifecycle_orderByEventDateDesc() {
        UniversityLifecycle l = new UniversityLifecycle();
        when(lifecycleRepository.findByUniversityCodeOrderByEventDateDesc("337"))
                .thenReturn(List.of(l));

        assertThat(service.getLifecycle("337")).containsExactly(l);
    }

    @Test
    void addLifecycleEvent_savesToRepository() {
        UniversityLifecycle event = new UniversityLifecycle();
        event.setUniversityCode("337");
        when(lifecycleRepository.save(event)).thenReturn(event);

        assertThat(service.addLifecycleEvent(event)).isEqualTo(event);
        verify(lifecycleRepository).save(event);
    }

    @Test
    @DisplayName("getRector — NEW jadval (employee_jobs) topilsa, undan qaytariladi")
    void getRector_fromNewTable() {
        Map<String, Object> row = new java.util.HashMap<>();
        row.put("first_name", "Akmal");
        row.put("last_name", "Karimov");
        row.put("middle_name", "Akmal o'g'li");
        row.put("pinfl", "12345678901234");
        row.put("phone", "+998901234567");
        row.put("position_code", "20");
        row.put("position_name", "Rektor");

        when(jdbcTemplate.queryForList(anyString(), eq("337")))
                .thenReturn(List.of(row));

        var rector = service.getRector("337");

        assertThat(rector).isNotNull();
        assertThat(rector.getFirstname()).isEqualTo("Akmal");
        assertThat(rector.getLastname()).isEqualTo("Karimov");
        assertThat(rector.getPositionName()).isEqualTo("Rektor");
    }

    @Test
    @DisplayName("getRector — NEW jadval bo'sh, OLD jadval'ga fallback")
    void getRector_fallbackToOldTable() {
        Map<String, Object> oldRow = new java.util.HashMap<>();
        oldRow.put("firstname", "Bekzod");
        oldRow.put("lastname", "Aliyev");
        oldRow.put("fathername", "Aliyevich");
        oldRow.put("pinfl", "PIN");
        oldRow.put("phone", "+998991111111");
        oldRow.put("position_code", "20");
        oldRow.put("position_name", "Rektor");

        // First call (NEW jadval): empty; Second call (OLD fallback): populated
        when(jdbcTemplate.queryForList(anyString(), eq("337")))
                .thenReturn(List.of())  // NEW
                .thenReturn(List.of(oldRow));  // OLD

        var rector = service.getRector("337");

        assertThat(rector).isNotNull();
        assertThat(rector.getFirstname()).isEqualTo("Bekzod");
        assertThat(rector.getLastname()).isEqualTo("Aliyev");
    }

    @Test
    @DisplayName("getRector — ikkala jadval bo'sh → null")
    void getRector_notFound_null() {
        when(jdbcTemplate.queryForList(anyString(), eq("999"))).thenReturn(List.of());

        assertThat(service.getRector("999")).isNull();
    }

    @Test
    @DisplayName("getUniversityDashboard — barcha qism aggregate")
    void getUniversityDashboard_aggregates() {
        UniversityFounder founder = new UniversityFounder();
        UniversityLifecycle lifecycle = new UniversityLifecycle();
        when(founderRepository.findByUniversityCode("337")).thenReturn(List.of(founder));
        when(lifecycleRepository.findByUniversityCodeOrderByEventDateDesc("337"))
                .thenReturn(List.of(lifecycle));
        when(buildingRepository.findByUniversityCodeOrderByNameAsc("337")).thenReturn(List.of());

        var dashboard = service.getUniversityDashboard("337");

        assertThat(dashboard).isNotNull();
        assertThat(dashboard.getFounders()).hasSize(1);
        assertThat(dashboard.getLifecycle()).hasSize(1);
        assertThat(dashboard.getBuildings()).isEmpty();
        // rector — null (jdbcTemplate empty default)
    }
}
