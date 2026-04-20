package uz.hemis.service.student;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import uz.hemis.domain.entity.student.StudentGpa;
import uz.hemis.domain.repository.StudentGpaRepository;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link StudentGpaService}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>findById - found and null scenarios</li>
 *   <li>findAll - paginated results</li>
 *   <li>findByUniversity - paginated results</li>
 *   <li>countByUniversity - delegation to repository</li>
 *   <li>countAll - delegation to repository</li>
 *   <li>create - new and upsert scenarios</li>
 * </ul>
 *
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("StudentGpaService Unit Tests")
class StudentGpaServiceTest {

    @Mock
    private StudentGpaRepository studentGpaRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private StudentGpaService studentGpaService;

    private UUID gpaId;
    private UUID studentId;
    private StudentGpa sampleGpa;

    @BeforeEach
    void setUp() {
        gpaId = UUID.randomUUID();
        studentId = UUID.randomUUID();

        sampleGpa = new StudentGpa();
        sampleGpa.setId(gpaId);
        sampleGpa.setStudentId(studentId);
        sampleGpa.setEducationYearCode("2024");
        sampleGpa.setGpa("3.85");
        sampleGpa.setMethod("one_year");
        sampleGpa.setLevelCode("12");
        sampleGpa.setCreditSum("47.0");
        sampleGpa.setSubjects(11);
        sampleGpa.setDebtSubjects(0);
        sampleGpa.setVersion(1);
    }

    // =====================================================
    // findById tests
    // =====================================================

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("returns legacy map when GPA record found")
        void returnsLegacyMap_whenFound() {
            when(studentGpaRepository.findById(gpaId)).thenReturn(Optional.of(sampleGpa));

            // Mock JDBC calls for toLegacyMap reference loading
            Map<String, Object> courseRow = new LinkedHashMap<>();
            courseRow.put("code", "12");
            courseRow.put("name", "2-kurs");
            when(jdbcTemplate.queryForMap(anyString(), eq("12"))).thenReturn(courseRow);

            Map<String, Object> yearRow = new LinkedHashMap<>();
            yearRow.put("code", "2024");
            yearRow.put("name", "2024-2025");
            when(jdbcTemplate.queryForMap(anyString(), eq("2024"))).thenReturn(yearRow);

            Map<String, Object> studentRow = new LinkedHashMap<>();
            studentRow.put("id", studentId);
            studentRow.put("lastname", "Karimov");
            studentRow.put("firstname", "Jasur");
            studentRow.put("fathername", "Alievich");
            when(jdbcTemplate.queryForMap(anyString(), eq(studentId))).thenReturn(studentRow);

            Map<String, Object> result = studentGpaService.findById(gpaId);

            assertThat(result).isNotNull();
            assertThat(result.get("_entityName")).isEqualTo("hemishe_EStudentGpa");
            assertThat(result.get("id")).isEqualTo(gpaId.toString());
            assertThat(result.get("gpa")).isEqualTo("3.85");
            assertThat(result.get("method")).isEqualTo("one_year");

            verify(studentGpaRepository).findById(gpaId);
        }

        @Test
        @DisplayName("returns null when GPA record not found")
        void returnsNull_whenNotFound() {
            UUID missingId = UUID.randomUUID();
            when(studentGpaRepository.findById(missingId)).thenReturn(Optional.empty());

            Map<String, Object> result = studentGpaService.findById(missingId);

            assertThat(result).isNull();
            verify(studentGpaRepository).findById(missingId);
        }
    }

    // =====================================================
    // findAll tests
    // =====================================================

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("returns list of legacy maps with pagination")
        void returnsLegacyMaps_withPagination() {
            Page<StudentGpa> page = new PageImpl<>(List.of(sampleGpa), PageRequest.of(0, 10), 1);
            when(studentGpaRepository.findAll(any(PageRequest.class))).thenReturn(page);

            // Mock JDBC for reference loading (may throw on missing references - that's OK)
            when(jdbcTemplate.queryForMap(anyString(), any())).thenThrow(
                    new org.springframework.dao.EmptyResultDataAccessException(1));

            List<Map<String, Object>> result = studentGpaService.findAll(10, 0);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).get("_entityName")).isEqualTo("hemishe_EStudentGpa");
            assertThat(result.get(0).get("gpa")).isEqualTo("3.85");

            verify(studentGpaRepository).findAll(any(PageRequest.class));
        }

        @Test
        @DisplayName("returns empty list when no GPA records exist")
        void returnsEmptyList_whenNoRecords() {
            Page<StudentGpa> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
            when(studentGpaRepository.findAll(any(PageRequest.class))).thenReturn(emptyPage);

            List<Map<String, Object>> result = studentGpaService.findAll(10, 0);

            assertThat(result).isEmpty();
        }
    }

    // =====================================================
    // findByUniversity tests
    // =====================================================

    @Nested
    @DisplayName("findByUniversity")
    class FindByUniversity {

        @Test
        @DisplayName("returns GPA records for university")
        void returnsRecords_forUniversity() {
            Page<StudentGpa> page = new PageImpl<>(List.of(sampleGpa), PageRequest.of(0, 10), 1);
            when(studentGpaRepository.findByUniversityCode(eq("401"), any(PageRequest.class))).thenReturn(page);

            // Mock JDBC for reference loading (may throw - that's OK)
            when(jdbcTemplate.queryForMap(anyString(), any())).thenThrow(
                    new org.springframework.dao.EmptyResultDataAccessException(1));

            List<Map<String, Object>> result = studentGpaService.findByUniversity("401", 10, 0);

            assertThat(result).hasSize(1);
            verify(studentGpaRepository).findByUniversityCode(eq("401"), any(PageRequest.class));
        }
    }

    // =====================================================
    // countByUniversity tests
    // =====================================================

    @Nested
    @DisplayName("countByUniversity")
    class CountByUniversity {

        @Test
        @DisplayName("delegates to repository and returns count")
        void delegatesToRepository() {
            when(studentGpaRepository.countByUniversityCode("401")).thenReturn(250L);

            long count = studentGpaService.countByUniversity("401");

            assertThat(count).isEqualTo(250L);
            verify(studentGpaRepository).countByUniversityCode("401");
        }
    }

    // =====================================================
    // countAll tests
    // =====================================================

    @Nested
    @DisplayName("countAll")
    class CountAll {

        @Test
        @DisplayName("delegates to repository count")
        void delegatesToRepositoryCount() {
            when(studentGpaRepository.count()).thenReturn(50000L);

            long count = studentGpaService.countAll();

            assertThat(count).isEqualTo(50000L);
            verify(studentGpaRepository).count();
        }
    }

    // =====================================================
    // create tests
    // =====================================================

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("creates new GPA record when no existing found")
        void createsNewRecord_whenNoExisting() {
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("studentId", studentId.toString());
            requestBody.put("educationYear", "2024");
            requestBody.put("level", "12");
            requestBody.put("gpa", "4.0");
            requestBody.put("method", "one_year");
            requestBody.put("creditSum", "47.0");
            requestBody.put("subjects", 11);
            requestBody.put("debtSubjects", 0);

            when(studentGpaRepository.findByStudentIdAndEducationYearCode(studentId, "2024"))
                    .thenReturn(Optional.empty());
            when(studentGpaRepository.save(any(StudentGpa.class))).thenAnswer(invocation -> {
                StudentGpa saved = invocation.getArgument(0);
                if (saved.getId() == null) saved.setId(UUID.randomUUID());
                return saved;
            });

            // Mock JDBC for toLegacyMap
            when(jdbcTemplate.queryForMap(anyString(), any())).thenThrow(
                    new org.springframework.dao.EmptyResultDataAccessException(1));

            Map<String, Object> result = studentGpaService.create(requestBody);

            assertThat(result).isNotNull();
            assertThat(result.get("_entityName")).isEqualTo("hemishe_EStudentGpa");
            assertThat(result.get("gpa")).isEqualTo("4.0");

            verify(studentGpaRepository).save(any(StudentGpa.class));
        }

        @Test
        @DisplayName("updates existing GPA record when found by studentId and educationYear")
        void updatesExisting_whenFoundByCompositeKey() {
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("studentId", studentId.toString());
            requestBody.put("educationYear", "2024");
            requestBody.put("gpa", "3.90");

            when(studentGpaRepository.findByStudentIdAndEducationYearCode(studentId, "2024"))
                    .thenReturn(Optional.of(sampleGpa));
            when(studentGpaRepository.save(any(StudentGpa.class))).thenReturn(sampleGpa);

            // Mock JDBC for toLegacyMap
            when(jdbcTemplate.queryForMap(anyString(), any())).thenThrow(
                    new org.springframework.dao.EmptyResultDataAccessException(1));

            Map<String, Object> result = studentGpaService.create(requestBody);

            assertThat(result).isNotNull();
            // Verify existing GPA was updated, not a new one created
            verify(studentGpaRepository).save(sampleGpa);
        }

        @Test
        @DisplayName("handles nested CUBA entity format for studentId")
        void handlesNestedCubaFormat() {
            Map<String, Object> studentIdNested = new LinkedHashMap<>();
            studentIdNested.put("id", studentId.toString());

            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("studentId", studentIdNested);
            requestBody.put("educationYear", Map.of("code", "2024"));
            requestBody.put("level", Map.of("code", "12"));
            requestBody.put("gpa", "3.50");
            requestBody.put("subjects", 10);
            requestBody.put("debtSubjects", 1);

            when(studentGpaRepository.findByStudentIdAndEducationYearCode(studentId, "2024"))
                    .thenReturn(Optional.empty());
            when(studentGpaRepository.save(any(StudentGpa.class))).thenAnswer(invocation -> {
                StudentGpa saved = invocation.getArgument(0);
                return saved;
            });

            // Mock JDBC for toLegacyMap
            when(jdbcTemplate.queryForMap(anyString(), any())).thenThrow(
                    new org.springframework.dao.EmptyResultDataAccessException(1));

            Map<String, Object> result = studentGpaService.create(requestBody);

            assertThat(result).isNotNull();
            verify(studentGpaRepository).save(any(StudentGpa.class));
        }
    }
}
