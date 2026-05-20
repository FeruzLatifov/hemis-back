package uz.hemis.service.legacy;

import org.junit.jupiter.api.BeforeEach;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DiplomBlankLegacyService — CUBA format + nested classifier loaders")
class DiplomBlankLegacyServiceTest {

    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private CubaNestedObjectLoader nestedObjectLoader;

    @InjectMocks
    private DiplomBlankLegacyService service;

    @BeforeEach
    void setUp() {
        // Default — empty classifier lookup
        lenient().when(nestedObjectLoader.loadClassifier(anyString(), anyString(), anyString(), eq(true)))
                .thenReturn(new HashMap<>(Map.of("_entityName", "X", "code", "x", "_instanceName", "i")));
        lenient().when(nestedObjectLoader.loadUniversity(anyString(), eq(true)))
                .thenReturn(new HashMap<>(Map.of("_entityName", "hemishe_EUniversity", "code", "337",
                        "_instanceName", "Andijon")));
    }

    @Test
    @DisplayName("getDiplomBlanks — yozuv yo'q, empty list")
    void getDiplomBlanks_empty() {
        when(jdbcTemplate.queryForList(anyString(), eq("337"), eq("2026")))
                .thenReturn(List.of());

        List<Map<String, Object>> result = service.getDiplomBlanks("337", 2026);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getDiplomBlanks — CUBA format + nested loaders")
    void getDiplomBlanks_legacyFormat() {
        Map<String, Object> row = new HashMap<>();
        UUID id = UUID.randomUUID();
        row.put("id", id);
        row.put("version", 1);
        row.put("blank_number", "B-001");
        row.put("blank_seria", "AB");
        row.put("_blank_status", "STATUS-1");
        row.put("_blank_year", "2026");
        row.put("_education_type", "11");
        row.put("_university", "337");
        row.put("_blank_category", "CAT-1");
        row.put("blank_generate_status_code", "BGS-1");

        when(jdbcTemplate.queryForList(anyString(), eq("337"), eq("2026")))
                .thenReturn(List.of(row));
        when(nestedObjectLoader.loadClassifierWithNames(eq("hemishe_h_education_type"),
                anyString(), eq("11"), eq(true)))
                .thenReturn(new HashMap<>(Map.of("code", "11", "name", "Bakalavr",
                        "_instanceName", "i", "nameRu", "Бакалавр", "active", true)));

        List<Map<String, Object>> result = service.getDiplomBlanks("337", 2026);

        assertThat(result).hasSize(1);
        Map<String, Object> blank = result.get(0);
        assertThat(blank).containsEntry("_entityName", "hemishe_EDiplomBlank");
        assertThat(blank).containsEntry("blankNumber", "B-001");
        assertThat(blank).containsEntry("blankSeria", "AB");
        assertThat(blank).containsKey("blankYear");
        assertThat(blank).containsKey("blankStatus");
        assertThat(blank).containsKey("educationType");
        assertThat(blank).containsKey("university");
        assertThat(blank).containsKey("blankCategory");
        assertThat(blank).containsKey("blankGenerateStatus");

        // educationType - nameRu/active olib tashlanadi (OLD-HEMIS xulqi)
        @SuppressWarnings("unchecked")
        Map<String, Object> eduType = (Map<String, Object>) blank.get("educationType");
        assertThat(eduType).doesNotContainKey("nameRu");
        assertThat(eduType).doesNotContainKey("active");
        assertThat(eduType).doesNotContainKey("_instanceName");
    }

    @Test
    @DisplayName("findDiplomBlankId — topilmaganda null")
    void findById_notFound() {
        when(jdbcTemplate.queryForList(anyString(), eq("X-blank"), eq("X-blank")))
                .thenReturn(List.of());

        assertThat(service.findDiplomBlankId("X-blank")).isNull();
    }

    @Test
    @DisplayName("findDiplomBlankId — topilgan UUID string")
    void findById_found() {
        UUID id = UUID.randomUUID();
        when(jdbcTemplate.queryForList(anyString(), eq("B-001"), eq("B-001")))
                .thenReturn(List.of(Map.of("id", id)));

        assertThat(service.findDiplomBlankId("B-001")).isEqualTo(id.toString());
    }

    @Test
    @DisplayName("updateDiplomBlankStatus — JDBC UPDATE chaqiriladi")
    void updateStatus_jdbcUpdate() {
        service.updateDiplomBlankStatus("uuid-str", "STATUS-1", "Reason");

        verify(jdbcTemplate).update(anyString(), eq("STATUS-1"), eq("Reason"), eq("uuid-str"));
    }

    @Test
    @DisplayName("getDiplomBlanks — nested code null'lar field qo'shilmaydi")
    void nullCodes_fieldsOmitted() {
        Map<String, Object> row = new HashMap<>();
        row.put("id", UUID.randomUUID());
        row.put("version", 1);
        row.put("blank_number", "B-002");
        row.put("blank_seria", "AB");
        // Barcha nested code null
        row.put("_blank_status", null);
        row.put("_blank_year", null);
        row.put("_education_type", null);
        row.put("_university", null);
        row.put("_blank_category", null);
        row.put("blank_generate_status_code", null);

        when(jdbcTemplate.queryForList(anyString(), anyString(), anyString()))
                .thenReturn(List.of(row));

        List<Map<String, Object>> result = service.getDiplomBlanks("337", 2026);

        assertThat(result).hasSize(1);
        Map<String, Object> blank = result.get(0);
        // null codes — fieldlar qo'shilmaydi
        assertThat(blank).doesNotContainKey("blankYear");
        assertThat(blank).doesNotContainKey("blankStatus");
        assertThat(blank).doesNotContainKey("educationType");
        assertThat(blank).doesNotContainKey("university");
        assertThat(blank).doesNotContainKey("blankCategory");
    }
}
