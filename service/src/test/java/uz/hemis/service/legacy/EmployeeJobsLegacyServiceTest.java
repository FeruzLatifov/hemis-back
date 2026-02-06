package uz.hemis.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeJobsLegacyServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private CubaNestedObjectLoader nestedObjectLoader;

    private EmployeeJobsLegacyService service;

    @BeforeEach
    void setUp() {
        service = new EmployeeJobsLegacyService(jdbcTemplate, nestedObjectLoader);
    }

    // =====================================================
    // getEmployeeFullName tests
    // =====================================================

    @Test
    void getEmployeeFullName_found_returnsUpperCase() {
        UUID empId = UUID.randomUUID();
        when(jdbcTemplate.queryForMap(
                contains("hemishe_e_teacher"),
                eq(empId)
        )).thenReturn(Map.of(
                "first_name", "Ali",
                "second_name", "Valiyev",
                "third_name", "Karimovich"
        ));

        String result = service.getEmployeeFullName(empId);

        assertThat(result).isEqualTo("VALIYEV ALI KARIMOVICH");
    }

    @Test
    void getEmployeeFullName_notFound_returnsFallback() {
        UUID empId = UUID.randomUUID();
        when(jdbcTemplate.queryForMap(
                contains("hemishe_e_teacher"),
                eq(empId)
        )).thenThrow(new EmptyResultDataAccessException(1));

        String result = service.getEmployeeFullName(empId);

        assertThat(result).startsWith("Employee-");
    }

    @Test
    void getEmployeeFullName_null_returnsUnknown() {
        String result = service.getEmployeeFullName(null);
        assertThat(result).isEqualTo("Unknown Employee");
    }

    // =====================================================
    // putNestedEmployee tests
    // =====================================================

    @Test
    void putNestedEmployee_found_addsToMap() {
        UUID empId = UUID.randomUUID();
        when(jdbcTemplate.queryForMap(
                contains("SELECT id, first_name"),
                eq(empId)
        )).thenReturn(Map.of(
                "id", empId,
                "first_name", "Ali",
                "second_name", "Valiyev",
                "third_name", "Karimovich",
                "version", 1
        ));

        Map<String, Object> map = new java.util.LinkedHashMap<>();
        service.putNestedEmployee(map, empId, false);

        assertThat(map).containsKey("employee");
        @SuppressWarnings("unchecked")
        Map<String, Object> nested = (Map<String, Object>) map.get("employee");
        assertThat(nested.get("_entityName")).isEqualTo("hemishe_ETeacher");
        assertThat(nested.get("fullname")).isEqualTo("Valiyev Ali Karimovich");
    }

    @Test
    void putNestedEmployee_null_returnNullsTrue_setsNull() {
        Map<String, Object> map = new java.util.LinkedHashMap<>();
        service.putNestedEmployee(map, null, true);

        assertThat(map).containsKey("employee");
        assertThat(map.get("employee")).isNull();
    }

    @Test
    void putNestedEmployee_null_returnNullsFalse_skips() {
        Map<String, Object> map = new java.util.LinkedHashMap<>();
        service.putNestedEmployee(map, null, false);

        assertThat(map).doesNotContainKey("employee");
    }

    // =====================================================
    // toMap tests
    // =====================================================

    @Test
    void toMap_minimalFields_returnsEntityName() {
        UUID entityId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();

        // Mock employee name lookup for instanceName
        when(jdbcTemplate.queryForMap(
                contains("hemishe_e_teacher"),
                eq(employeeId)
        )).thenThrow(new EmptyResultDataAccessException(1));

        Map<String, Object> result = service.toMap(
                entityId, employeeId, null, null,
                null, null, null, null, null,
                1, null,
                null, null, null, null, null, null,
                false, null
        );

        assertThat(result).containsKey("_entityName");
        assertThat(result.get("_entityName")).isEqualTo("hemishe_EEmployeeJob");
        assertThat(result.get("id")).isEqualTo(entityId);
        assertThat(result.get("version")).isEqualTo(1);
    }

    // =====================================================
    // buildInstanceName tests
    // =====================================================

    @Test
    void buildInstanceName_nullEmployee_startsWithUnknown() {
        String result = service.buildInstanceName(null, null, null);
        assertThat(result).isEqualTo("Unknown Employee");
    }
}
