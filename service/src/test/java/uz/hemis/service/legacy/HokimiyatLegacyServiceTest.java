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
@DisplayName("HokimiyatLegacyService — chetlashgan talabalar response")
class HokimiyatLegacyServiceTest {

    @Mock private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private HokimiyatLegacyService service;

    @Test
    @DisplayName("students found — OK + count + items")
    void studentsFound_okStatus() {
        Map<String, Object> row1 = Map.of("id", "u1", "pinfl", "11111111111111");
        Map<String, Object> row2 = Map.of("id", "u2", "pinfl", "22222222222222");
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(row1, row2));

        Map<String, Object> result = service.getStudentsUpdatedYesterday();

        assertThat(result).containsEntry("status", "OK");
        assertThat(result).containsEntry("count", 2);
        assertThat((List<?>) result.get("items")).hasSize(2);
    }

    @Test
    @DisplayName("empty — OK + count=0")
    void noStudents_emptyOk() {
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of());

        Map<String, Object> result = service.getStudentsUpdatedYesterday();

        assertThat(result).containsEntry("status", "OK");
        assertThat(result).containsEntry("count", 0);
    }

    @Test
    @DisplayName("DB xato — ERROR + title + message")
    void dbException_errorResponse() {
        when(jdbcTemplate.queryForList(anyString()))
                .thenThrow(new RuntimeException("Connection refused"));

        Map<String, Object> result = service.getStudentsUpdatedYesterday();

        assertThat(result).containsEntry("status", "ERROR");
        assertThat(result).containsEntry("title", "Chetlashgan talabalar");
        assertThat(result).containsKey("message");
    }
}
