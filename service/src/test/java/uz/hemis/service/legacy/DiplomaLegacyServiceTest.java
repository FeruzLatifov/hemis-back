package uz.hemis.service.legacy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DiplomBlankLegacyService}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>getDiplomBlanks - success with nested objects, empty result</li>
 *   <li>findDiplomBlankId - found, not found</li>
 *   <li>updateDiplomBlankStatus - successful update, with and without reason</li>
 * </ul>
 *
 * @since 1.5.4
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DiplomBlankLegacyService Unit Tests")
class DiplomaLegacyServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private CubaNestedObjectLoader nestedObjectLoader;

    @InjectMocks
    private DiplomBlankLegacyService diplomBlankLegacyService;

    private static final String UNIVERSITY_CODE = "TATU01";
    private static final Integer YEAR = 2025;

    // =====================================================
    // getDiplomBlanks
    // =====================================================

    @Nested
    @DisplayName("getDiplomBlanks")
    class GetDiplomBlanks {

        @Test
        @DisplayName("returns populated blanks list with nested objects when rows exist")
        void returnsPopulatedBlanksList_whenRowsExist() {
            UUID blankId = UUID.randomUUID();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", blankId);
            row.put("version", 1);
            row.put("blank_number", "DN-001");
            row.put("blank_seria", "AB");
            row.put("cancel_reason", null);
            row.put("_blank_status", "10");
            row.put("_blank_year", "2025");
            row.put("_education_type", "11");
            row.put("_university", UNIVERSITY_CODE);
            row.put("_blank_category", "20");
            row.put("blank_generate_status_code", "30");

            when(jdbcTemplate.queryForList(anyString(), eq(UNIVERSITY_CODE), eq(String.valueOf(YEAR))))
                    .thenReturn(List.of(row));

            // Nested loaders
            Map<String, Object> statusObj = Map.of("_entityName", "hemishe_HDiplomBlankGenerateStatus", "code", "30");
            when(nestedObjectLoader.loadClassifier(eq("hemishe_h_diplom_blank_generate_status"), anyString(), eq("30")))
                    .thenReturn(statusObj);

            Map<String, Object> yearObj = Map.of("_entityName", "hemishe_HEducationYear", "code", "2025");
            when(nestedObjectLoader.loadClassifier(eq("hemishe_h_education_year"), anyString(), eq("2025")))
                    .thenReturn(yearObj);

            Map<String, Object> blankStatusObj = Map.of("_entityName", "hemishe_HDiplomBlankStatus", "code", "10");
            when(nestedObjectLoader.loadClassifier(eq("hemishe_h_diplom_blank_status"), anyString(), eq("10")))
                    .thenReturn(blankStatusObj);

            Map<String, Object> eduTypeObj = Map.of("_entityName", "hemishe_HEducationType", "code", "11");
            when(nestedObjectLoader.loadClassifierWithNames(eq("hemishe_h_education_type"), anyString(), eq("11")))
                    .thenReturn(eduTypeObj);

            Map<String, Object> uniObj = Map.of("_entityName", "hemishe_EUniversity", "code", UNIVERSITY_CODE);
            when(nestedObjectLoader.loadUniversity(UNIVERSITY_CODE)).thenReturn(uniObj);

            Map<String, Object> catObj = Map.of("_entityName", "hemishe_HDiplomBlankCategory", "code", "20");
            when(nestedObjectLoader.loadClassifier(eq("hemishe_h_diplom_blank_category"), anyString(), eq("20")))
                    .thenReturn(catObj);

            List<Map<String, Object>> result = diplomBlankLegacyService.getDiplomBlanks(UNIVERSITY_CODE, YEAR);

            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);

            Map<String, Object> blank = result.get(0);
            assertThat(blank.get("_entityName")).isEqualTo("hemishe_EDiplomBlank");
            assertThat(blank.get("id")).isEqualTo(blankId.toString());
            assertThat(blank.get("blankNumber")).isEqualTo("DN-001");
            assertThat(blank.get("blankSeria")).isEqualTo("AB");
            assertThat(blank.get("version")).isEqualTo(1);
            assertThat(blank).containsKey("blankGenerateStatus");
            assertThat(blank).containsKey("blankYear");
            assertThat(blank).containsKey("blankStatus");
            assertThat(blank).containsKey("educationType");
            assertThat(blank).containsKey("university");
            assertThat(blank).containsKey("blankCategory");
        }

        @Test
        @DisplayName("returns empty list when no rows found")
        void returnsEmptyList_whenNoRowsFound() {
            when(jdbcTemplate.queryForList(anyString(), eq(UNIVERSITY_CODE), eq(String.valueOf(YEAR))))
                    .thenReturn(Collections.emptyList());

            List<Map<String, Object>> result = diplomBlankLegacyService.getDiplomBlanks(UNIVERSITY_CODE, YEAR);

            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("skips nested objects when their codes are null")
        void skipsNestedObjects_whenCodesAreNull() {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", UUID.randomUUID());
            row.put("version", 2);
            row.put("blank_number", "DN-002");
            row.put("blank_seria", null);
            row.put("cancel_reason", null);
            row.put("_blank_status", null);
            row.put("_blank_year", null);
            row.put("_education_type", null);
            row.put("_university", null);
            row.put("_blank_category", null);
            row.put("blank_generate_status_code", null);

            when(jdbcTemplate.queryForList(anyString(), eq(UNIVERSITY_CODE), eq(String.valueOf(YEAR))))
                    .thenReturn(List.of(row));

            List<Map<String, Object>> result = diplomBlankLegacyService.getDiplomBlanks(UNIVERSITY_CODE, YEAR);

            assertThat(result).hasSize(1);
            Map<String, Object> blank = result.get(0);
            assertThat(blank.get("_entityName")).isEqualTo("hemishe_EDiplomBlank");
            assertThat(blank).doesNotContainKey("blankGenerateStatus");
            assertThat(blank).doesNotContainKey("blankYear");
            assertThat(blank).doesNotContainKey("blankStatus");
            assertThat(blank).doesNotContainKey("educationType");
            assertThat(blank).doesNotContainKey("university");
            assertThat(blank).doesNotContainKey("blankCategory");
            verifyNoInteractions(nestedObjectLoader);
        }
    }

    // =====================================================
    // findDiplomBlankId
    // =====================================================

    @Nested
    @DisplayName("findDiplomBlankId")
    class FindDiplomBlankId {

        @Test
        @DisplayName("returns UUID string when blank is found by blankCode")
        void returnsUuidString_whenBlankFound() {
            UUID expectedId = UUID.randomUUID();
            when(jdbcTemplate.queryForList(anyString(), eq("DN-001"), eq("DN-001")))
                    .thenReturn(List.of(Map.of("id", expectedId)));

            String result = diplomBlankLegacyService.findDiplomBlankId("DN-001");

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(expectedId.toString());
        }

        @Test
        @DisplayName("returns null when blank is not found")
        void returnsNull_whenBlankNotFound() {
            when(jdbcTemplate.queryForList(anyString(), eq("NONEXISTENT"), eq("NONEXISTENT")))
                    .thenReturn(Collections.emptyList());

            String result = diplomBlankLegacyService.findDiplomBlankId("NONEXISTENT");

            assertThat(result).isNull();
        }
    }

    // =====================================================
    // updateDiplomBlankStatus
    // =====================================================

    @Nested
    @DisplayName("updateDiplomBlankStatus")
    class UpdateDiplomBlankStatus {

        @Test
        @DisplayName("executes update SQL with status code and reason")
        void executesUpdate_withStatusAndReason() {
            String id = UUID.randomUUID().toString();

            diplomBlankLegacyService.updateDiplomBlankStatus(id, "20", "Damaged blank");

            verify(jdbcTemplate).update(anyString(), eq("20"), eq("Damaged blank"), eq(id));
        }

        @Test
        @DisplayName("executes update SQL with null reason")
        void executesUpdate_withNullReason() {
            String id = UUID.randomUUID().toString();

            diplomBlankLegacyService.updateDiplomBlankStatus(id, "10", null);

            verify(jdbcTemplate).update(anyString(), eq("10"), eq(null), eq(id));
        }
    }
}
