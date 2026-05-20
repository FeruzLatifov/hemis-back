package uz.hemis.service.legacy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GuvdLegacyService — OLD-HEMIS GuvdServiceBean compat")
class GuvdLegacyServiceTest {

    @Mock private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private GuvdLegacyService service;

    @Test
    @DisplayName("getObjects — university list CUBA format")
    void objects_universityList() {
        Map<String, Object> row1 = new HashMap<>();
        row1.put("code", "337");
        row1.put("name", "Andijon DU");
        row1.put("tin", "300000337");

        Map<String, Object> row2 = new HashMap<>();
        row2.put("code", "401");
        row2.put("name", "TUIT");
        row2.put("tin", null);  // null tin — removed from item

        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(row1, row2));

        Map<String, Object> result = service.getObjects();

        assertThat(result).containsEntry("success", true);
        assertThat(result).containsEntry("count", 2);
        List<?> data = (List<?>) result.get("data");
        assertThat(data).hasSize(2);

        @SuppressWarnings("unchecked")
        Map<String, Object> item1 = (Map<String, Object>) data.get(0);
        assertThat(item1).containsEntry("_entityName", "hemishe_EUniversity");
        assertThat(item1).containsEntry("code", "337");
        assertThat(item1).containsEntry("name", "Andijon DU");
        assertThat(item1).containsEntry("tin", "300000337");

        @SuppressWarnings("unchecked")
        Map<String, Object> item2 = (Map<String, Object>) data.get(1);
        // null tin removed (Objects::isNull filter)
        assertThat(item2).doesNotContainKey("tin");
        assertThat(item2).containsEntry("code", "401");
    }

    @Test
    @DisplayName("getObjects — empty list")
    void objects_emptyList() {
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of());

        Map<String, Object> result = service.getObjects();

        assertThat(result).containsEntry("success", true);
        assertThat(result).containsEntry("count", 0);
        assertThat((List<?>) result.get("data")).isEmpty();
    }

    @Test
    @DisplayName("getClassifiers — exception → success=false + message yashirin")
    void classifiers_error_silentMessage() {
        lenient().when(jdbcTemplate.queryForList(anyString()))
                .thenThrow(new RuntimeException("DB unreachable"));

        Map<String, Object> result = service.getClassifiers();

        assertThat(result).containsEntry("success", false);
        assertThat(result).containsEntry("message", "Server error");
    }
}
