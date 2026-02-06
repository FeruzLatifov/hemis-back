package uz.hemis.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TeacherLegacyService}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>loadJobs - success, empty result, null teacherCode, exception handling</li>
 *   <li>putClassifier - found, not found, null code, returnNulls flag</li>
 *   <li>putUniversity - found, not found, null code</li>
 *   <li>putDepartment - found, not found, null code</li>
 *   <li>putSoato - found, null code</li>
 * </ul>
 *
 * @since 1.5.4
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TeacherLegacyService Unit Tests")
class TeacherLegacyServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private CubaNestedObjectLoader nestedLoader;

    @InjectMocks
    private TeacherLegacyService teacherLegacyService;

    private static final String TEACHER_CODE = "T001";
    private static final String UNIVERSITY_CODE = "TATU01";

    // =====================================================
    // loadJobs
    // =====================================================

    @Nested
    @DisplayName("loadJobs")
    class LoadJobs {

        @Test
        @DisplayName("returns jobs list when teacher has jobs")
        void returnsJobsList_whenTeacherHasJobs() {
            Map<String, Object> jobRow = new LinkedHashMap<>();
            jobRow.put("id", UUID.randomUUID());
            jobRow.put("version", 1);
            jobRow.put("code", TEACHER_CODE);
            jobRow.put("active", true);

            when(jdbcTemplate.queryForList(anyString(), eq(TEACHER_CODE)))
                    .thenReturn(List.of(jobRow));

            List<Map<String, Object>> result = teacherLegacyService.loadJobs(TEACHER_CODE, UNIVERSITY_CODE, false);

            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).get("_entityName")).isEqualTo("hemishe_EEmployeeJobs");
            assertThat(result.get(0).get("code")).isEqualTo(TEACHER_CODE);
            assertThat(result.get(0).get("active")).isEqualTo(true);
        }

        @Test
        @DisplayName("returns empty list when no jobs found")
        void returnsEmptyList_whenNoJobsFound() {
            when(jdbcTemplate.queryForList(anyString(), eq(TEACHER_CODE)))
                    .thenReturn(Collections.emptyList());

            List<Map<String, Object>> result = teacherLegacyService.loadJobs(TEACHER_CODE, UNIVERSITY_CODE, false);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty list when teacherCode is null")
        void returnsEmptyList_whenTeacherCodeIsNull() {
            List<Map<String, Object>> result = teacherLegacyService.loadJobs(null, UNIVERSITY_CODE, false);

            assertThat(result).isEmpty();
            verifyNoInteractions(jdbcTemplate);
        }

        @Test
        @DisplayName("returns empty list when exception occurs")
        void returnsEmptyList_whenExceptionOccurs() {
            when(jdbcTemplate.queryForList(anyString(), eq(TEACHER_CODE)))
                    .thenThrow(new RuntimeException("DB connection failed"));

            List<Map<String, Object>> result = teacherLegacyService.loadJobs(TEACHER_CODE, UNIVERSITY_CODE, true);

            assertThat(result).isEmpty();
        }
    }

    // =====================================================
    // putClassifier
    // =====================================================

    @Nested
    @DisplayName("putClassifier")
    class PutClassifier {

        @Test
        @DisplayName("puts classifier object when code is valid and found in DB")
        void putsClassifierObject_whenCodeIsValid() {
            Map<String, Object> map = new LinkedHashMap<>();
            Map<String, Object> dbRow = Map.of(
                    "code", "11",
                    "name", "Male",
                    "active", true,
                    "version", 1
            );
            when(jdbcTemplate.queryForMap(anyString(), eq("11")))
                    .thenReturn(dbRow);

            teacherLegacyService.putClassifier(map, "gender", "11",
                    "hemishe_HGender", "hemishe_h_gender", false);

            assertThat(map).containsKey("gender");
            @SuppressWarnings("unchecked")
            Map<String, Object> genderObj = (Map<String, Object>) map.get("gender");
            assertThat(genderObj.get("_entityName")).isEqualTo("hemishe_HGender");
            assertThat(genderObj.get("code")).isEqualTo("11");
            assertThat(genderObj.get("name")).isEqualTo("Male");
        }

        @Test
        @DisplayName("does not put key when code is null and returnNulls is false")
        void doesNotPutKey_whenCodeIsNullAndReturnNullsFalse() {
            Map<String, Object> map = new LinkedHashMap<>();

            teacherLegacyService.putClassifier(map, "gender", null,
                    "hemishe_HGender", "hemishe_h_gender", false);

            assertThat(map).doesNotContainKey("gender");
        }

        @Test
        @DisplayName("puts null value when code is null and returnNulls is true")
        void putsNullValue_whenCodeIsNullAndReturnNullsTrue() {
            Map<String, Object> map = new LinkedHashMap<>();

            teacherLegacyService.putClassifier(map, "gender", null,
                    "hemishe_HGender", "hemishe_h_gender", true);

            assertThat(map).containsKey("gender");
            assertThat(map.get("gender")).isNull();
        }

        @Test
        @DisplayName("puts null when EmptyResultDataAccessException and returnNulls is true")
        void putsNull_whenNotFoundAndReturnNullsTrue() {
            Map<String, Object> map = new LinkedHashMap<>();
            when(jdbcTemplate.queryForMap(anyString(), eq("INVALID")))
                    .thenThrow(new EmptyResultDataAccessException(1));

            teacherLegacyService.putClassifier(map, "gender", "INVALID",
                    "hemishe_HGender", "hemishe_h_gender", true);

            assertThat(map).containsKey("gender");
            assertThat(map.get("gender")).isNull();
        }
    }

    // =====================================================
    // putUniversity
    // =====================================================

    @Nested
    @DisplayName("putUniversity")
    class PutUniversity {

        @Test
        @DisplayName("puts university object when found by nestedLoader")
        void putsUniversityObject_whenFound() {
            Map<String, Object> map = new LinkedHashMap<>();
            Map<String, Object> uniObj = Map.of(
                    "_entityName", "hemishe_EUniversity",
                    "code", UNIVERSITY_CODE,
                    "name", "Test University"
            );
            when(nestedLoader.loadUniversity(UNIVERSITY_CODE)).thenReturn(uniObj);

            teacherLegacyService.putUniversity(map, UNIVERSITY_CODE, false);

            assertThat(map).containsKey("university");
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) map.get("university");
            assertThat(result.get("code")).isEqualTo(UNIVERSITY_CODE);
            verify(nestedLoader).loadUniversity(UNIVERSITY_CODE);
        }

        @Test
        @DisplayName("does not put key when code is null and returnNulls is false")
        void doesNotPutKey_whenCodeIsNullAndReturnNullsFalse() {
            Map<String, Object> map = new LinkedHashMap<>();

            teacherLegacyService.putUniversity(map, null, false);

            assertThat(map).doesNotContainKey("university");
            verifyNoInteractions(nestedLoader);
        }

        @Test
        @DisplayName("puts null when nestedLoader returns null and returnNulls is true")
        void putsNull_whenNotFoundAndReturnNullsTrue() {
            Map<String, Object> map = new LinkedHashMap<>();
            when(nestedLoader.loadUniversity("NONEXISTENT")).thenReturn(null);

            teacherLegacyService.putUniversity(map, "NONEXISTENT", true);

            assertThat(map).containsKey("university");
            assertThat(map.get("university")).isNull();
        }
    }

    // =====================================================
    // putDepartment
    // =====================================================

    @Nested
    @DisplayName("putDepartment")
    class PutDepartment {

        @Test
        @DisplayName("puts department object when found by nestedLoader")
        void putsDepartmentObject_whenFound() {
            Map<String, Object> map = new LinkedHashMap<>();
            String deptCode = "DEPT01";
            Map<String, Object> deptObj = Map.of(
                    "_entityName", "hemishe_EUniversityDepartment",
                    "code", deptCode,
                    "name", "CS Department"
            );
            when(nestedLoader.loadDepartment(deptCode)).thenReturn(deptObj);

            teacherLegacyService.putDepartment(map, deptCode, false);

            assertThat(map).containsKey("department");
            verify(nestedLoader).loadDepartment(deptCode);
        }

        @Test
        @DisplayName("puts null when code is empty and returnNulls is true")
        void putsNull_whenCodeIsEmptyAndReturnNullsTrue() {
            Map<String, Object> map = new LinkedHashMap<>();

            teacherLegacyService.putDepartment(map, "", true);

            assertThat(map).containsKey("department");
            assertThat(map.get("department")).isNull();
            verifyNoInteractions(nestedLoader);
        }
    }

    // =====================================================
    // putSoato
    // =====================================================

    @Nested
    @DisplayName("putSoato")
    class PutSoato {

        @Test
        @DisplayName("puts soato object when found by nestedLoader")
        void putsSoatoObject_whenFound() {
            Map<String, Object> map = new LinkedHashMap<>();
            String soatoCode = "1726";
            Map<String, Object> soatoObj = Map.of(
                    "_entityName", "hemishe_HSoato",
                    "code", soatoCode
            );
            when(nestedLoader.loadSoato(soatoCode)).thenReturn(soatoObj);

            teacherLegacyService.putSoato(map, "soato", soatoCode, false);

            assertThat(map).containsKey("soato");
            verify(nestedLoader).loadSoato(soatoCode);
        }

        @Test
        @DisplayName("puts null when code is null and returnNulls is true")
        void putsNull_whenCodeIsNullAndReturnNullsTrue() {
            Map<String, Object> map = new LinkedHashMap<>();

            teacherLegacyService.putSoato(map, "birthPlace", null, true);

            assertThat(map).containsKey("birthPlace");
            assertThat(map.get("birthPlace")).isNull();
            verifyNoInteractions(nestedLoader);
        }
    }
}
