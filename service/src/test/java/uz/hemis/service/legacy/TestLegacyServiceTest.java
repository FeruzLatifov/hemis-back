package uz.hemis.service.legacy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TestLegacyService — diagnostic endpoint")
class TestLegacyServiceTest {

    @Mock private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private TestLegacyService service;

    @Test
    @DisplayName("getStudentsUpdatedYesterday — yozuv bor, list qaytariladi")
    void studentsFound_returnsList() {
        Map<String, Object> row = Map.of("id", "uuid-1", "pinfl", "1234");
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(row));

        List<Map<String, Object>> result = service.getStudentsUpdatedYesterday();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsEntry("pinfl", "1234");
    }

    @Test
    @DisplayName("getStudentsUpdatedYesterday — bo'sh natija → null")
    void empty_returnsNull() {
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of());

        assertThat(service.getStudentsUpdatedYesterday()).isNull();
    }
}
