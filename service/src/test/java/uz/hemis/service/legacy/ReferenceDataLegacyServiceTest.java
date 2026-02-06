package uz.hemis.service.legacy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ReferenceDataLegacyService}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>getUniversityData - found in DB, not found (fallback), cached result</li>
 *   <li>getEducationYearData - found in DB, not found (fallback)</li>
 *   <li>getCourseData - found in DB, not found (fallback)</li>
 *   <li>getSpecialityDoctoralData - found, not found, exception handling</li>
 *   <li>isValidClassifier - valid, not valid, invalid table name, exception handling</li>
 * </ul>
 *
 * @since 1.5.4
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReferenceDataLegacyService Unit Tests")
class ReferenceDataLegacyServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ReferenceDataLegacyService referenceDataLegacyService;

    // =====================================================
    // getUniversityData
    // =====================================================

    @Nested
    @DisplayName("getUniversityData")
    class GetUniversityData {

        @Test
        @DisplayName("returns data from DB when university exists")
        @SuppressWarnings("unchecked")
        void returnsDataFromDb_whenUniversityExists() {
            Map<String, String> dbData = Map.of("code", "TATU01", "name", "TATU");
            when(jdbcTemplate.queryForObject(
                    anyString(),
                    any(RowMapper.class),
                    eq("TATU01")))
                    .thenReturn(dbData);

            Map<String, String> result = referenceDataLegacyService.getUniversityData("TATU01");

            assertThat(result).isNotNull();
            assertThat(result.get("code")).isEqualTo("TATU01");
            assertThat(result.get("name")).isEqualTo("TATU");
        }

        @Test
        @DisplayName("returns fallback data when university not found in DB")
        @SuppressWarnings("unchecked")
        void returnsFallback_whenUniversityNotFound() {
            when(jdbcTemplate.queryForObject(
                    anyString(),
                    any(RowMapper.class),
                    eq("UNKNOWN")))
                    .thenThrow(new EmptyResultDataAccessException(1));

            Map<String, String> result = referenceDataLegacyService.getUniversityData("UNKNOWN");

            assertThat(result).isNotNull();
            assertThat(result.get("code")).isEqualTo("UNKNOWN");
            assertThat(result.get("name")).isEqualTo("University UNKNOWN");
        }

        @Test
        @DisplayName("returns cached data on second call for same code")
        @SuppressWarnings("unchecked")
        void returnsCachedData_onSecondCall() {
            Map<String, String> dbData = Map.of("code", "TATU01", "name", "TATU");
            when(jdbcTemplate.queryForObject(
                    anyString(),
                    any(RowMapper.class),
                    eq("TATU01")))
                    .thenReturn(dbData);

            // First call - fetches from DB
            Map<String, String> result1 = referenceDataLegacyService.getUniversityData("TATU01");
            // Second call - should use cache
            Map<String, String> result2 = referenceDataLegacyService.getUniversityData("TATU01");

            assertThat(result1).isEqualTo(result2);
            // DB should be queried only once
            verify(jdbcTemplate, times(1)).queryForObject(anyString(), any(RowMapper.class), eq("TATU01"));
        }
    }

    // =====================================================
    // getEducationYearData
    // =====================================================

    @Nested
    @DisplayName("getEducationYearData")
    class GetEducationYearData {

        @Test
        @DisplayName("returns data from DB when education year exists")
        @SuppressWarnings("unchecked")
        void returnsDataFromDb_whenEducationYearExists() {
            Map<String, String> dbData = Map.of("code", "2024", "name", "2024-2025");
            when(jdbcTemplate.queryForObject(
                    anyString(),
                    any(RowMapper.class),
                    eq("2024")))
                    .thenReturn(dbData);

            Map<String, String> result = referenceDataLegacyService.getEducationYearData("2024");

            assertThat(result).isNotNull();
            assertThat(result.get("code")).isEqualTo("2024");
            assertThat(result.get("name")).isEqualTo("2024-2025");
        }

        @Test
        @DisplayName("returns fallback data when education year not found in DB")
        @SuppressWarnings("unchecked")
        void returnsFallback_whenNotFound() {
            when(jdbcTemplate.queryForObject(
                    anyString(),
                    any(RowMapper.class),
                    eq("9999")))
                    .thenThrow(new EmptyResultDataAccessException(1));

            Map<String, String> result = referenceDataLegacyService.getEducationYearData("9999");

            assertThat(result).isNotNull();
            assertThat(result.get("code")).isEqualTo("9999");
            assertThat(result.get("name")).isEqualTo("9999");
        }
    }

    // =====================================================
    // getCourseData
    // =====================================================

    @Nested
    @DisplayName("getCourseData")
    class GetCourseData {

        @Test
        @DisplayName("returns data from DB when course exists")
        @SuppressWarnings("unchecked")
        void returnsDataFromDb_whenCourseExists() {
            Map<String, String> dbData = new HashMap<>();
            dbData.put("code", "1");
            dbData.put("name", "1-kurs");
            when(jdbcTemplate.queryForObject(
                    anyString(),
                    any(RowMapper.class),
                    eq("1")))
                    .thenReturn(dbData);

            Map<String, String> result = referenceDataLegacyService.getCourseData("1");

            assertThat(result).isNotNull();
            assertThat(result.get("code")).isEqualTo("1");
            assertThat(result.get("name")).isEqualTo("1-kurs");
        }

        @Test
        @DisplayName("returns fallback data when course not found in DB")
        @SuppressWarnings("unchecked")
        void returnsFallback_whenCourseNotFound() {
            when(jdbcTemplate.queryForObject(
                    anyString(),
                    any(RowMapper.class),
                    eq("99")))
                    .thenThrow(new RuntimeException("Not found"));

            Map<String, String> result = referenceDataLegacyService.getCourseData("99");

            assertThat(result).isNotNull();
            assertThat(result.get("code")).isEqualTo("99");
            assertThat(result.get("name")).isEqualTo("99-kurs");
        }
    }

    // =====================================================
    // getSpecialityDoctoralData
    // =====================================================

    @Nested
    @DisplayName("getSpecialityDoctoralData")
    class GetSpecialityDoctoralData {

        @Test
        @DisplayName("returns data when speciality is found")
        void returnsData_whenSpecialityFound() {
            UUID specialityId = UUID.randomUUID();
            Map<String, Object> dbRow = Map.of("id", specialityId, "name", "Computer Science");
            when(jdbcTemplate.queryForList(anyString(), eq(specialityId)))
                    .thenReturn(List.of(dbRow));

            Map<String, Object> result = referenceDataLegacyService.getSpecialityDoctoralData(specialityId);

            assertThat(result).isNotNull();
            assertThat(result.get("id")).isEqualTo(specialityId);
            assertThat(result.get("name")).isEqualTo("Computer Science");
        }

        @Test
        @DisplayName("returns null when speciality is not found")
        void returnsNull_whenSpecialityNotFound() {
            UUID specialityId = UUID.randomUUID();
            when(jdbcTemplate.queryForList(anyString(), eq(specialityId)))
                    .thenReturn(Collections.emptyList());

            Map<String, Object> result = referenceDataLegacyService.getSpecialityDoctoralData(specialityId);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("returns null when exception occurs")
        void returnsNull_whenExceptionOccurs() {
            UUID specialityId = UUID.randomUUID();
            when(jdbcTemplate.queryForList(anyString(), eq(specialityId)))
                    .thenThrow(new RuntimeException("DB error"));

            Map<String, Object> result = referenceDataLegacyService.getSpecialityDoctoralData(specialityId);

            assertThat(result).isNull();
        }
    }

    // =====================================================
    // isValidClassifier
    // =====================================================

    @Nested
    @DisplayName("isValidClassifier")
    class IsValidClassifier {

        @Test
        @DisplayName("returns true when classifier code exists in table")
        void returnsTrue_whenClassifierExists() {
            when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("11")))
                    .thenReturn(1);

            boolean result = referenceDataLegacyService.isValidClassifier("hemishe_h_gender", "11");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("returns false when classifier code does not exist in table")
        void returnsFalse_whenClassifierDoesNotExist() {
            when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("99")))
                    .thenReturn(0);

            boolean result = referenceDataLegacyService.isValidClassifier("hemishe_h_gender", "99");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when table name contains invalid characters")
        void returnsFalse_whenTableNameIsInvalid() {
            boolean result = referenceDataLegacyService.isValidClassifier("DROP TABLE;--", "11");

            assertThat(result).isFalse();
            verifyNoInteractions(jdbcTemplate);
        }

        @Test
        @DisplayName("returns false when exception occurs during query")
        void returnsFalse_whenExceptionOccurs() {
            when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("11")))
                    .thenThrow(new RuntimeException("Table not found"));

            boolean result = referenceDataLegacyService.isValidClassifier("hemishe_h_nonexistent", "11");

            assertThat(result).isFalse();
        }
    }
}
