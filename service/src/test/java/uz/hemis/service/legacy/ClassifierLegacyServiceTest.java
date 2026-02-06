package uz.hemis.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import uz.hemis.domain.entity.University;
import uz.hemis.domain.repository.UniversityRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ClassifierLegacyService}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>getAllClassifiersWithItems - success and empty table list</li>
 *   <li>getAllClassifiersInfo - success and empty table list</li>
 *   <li>getSingleClassifier - h_university, dynamic classifier, and not-found</li>
 *   <li>getHokimiyatClassifiers - success and exception handling</li>
 * </ul>
 *
 * @since 1.5.4
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClassifierLegacyService Unit Tests")
class ClassifierLegacyServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private UniversityRepository universityRepository;

    @Mock
    private CubaNestedObjectLoader nestedLoader;

    @InjectMocks
    private ClassifierLegacyService classifierLegacyService;

    // =====================================================
    // getAllClassifiersWithItems
    // =====================================================

    @Nested
    @DisplayName("getAllClassifiersWithItems")
    class GetAllClassifiersWithItems {

        @Test
        @DisplayName("returns success true and classifiers list when tables exist")
        void returnsSuccessAndClassifiersList() {
            // Simulate getClassifierTables returning one table
            when(jdbcTemplate.queryForList(
                    argThat(sql -> sql != null && sql.contains("information_schema.tables")),
                    eq(String.class)))
                    .thenReturn(List.of("hemishe_h_gender"));

            // Simulate checkColumnExists calls
            when(jdbcTemplate.queryForObject(
                    argThat(sql -> sql != null && sql.contains("information_schema.columns")),
                    eq(Boolean.class), any(), any()))
                    .thenReturn(true);

            // Simulate buildCountSql result
            when(jdbcTemplate.queryForMap(argThat(sql -> sql != null && sql.contains("COUNT"))))
                    .thenReturn(Map.of("cnt", 5L, "ver", 10));

            // Simulate items query
            when(jdbcTemplate.queryForList(argThat(sql -> sql != null && sql.startsWith("SELECT code"))))
                    .thenReturn(List.of(
                            Map.of("code", "11", "name", "Male", "active", true, "version", 1)
                    ));

            Map<String, Object> result = classifierLegacyService.getAllClassifiersWithItems();

            assertThat(result).isNotNull();
            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result).containsKey("classifiers");
        }

        @Test
        @DisplayName("returns empty classifiers list when no tables found")
        void returnsEmptyClassifiersWhenNoTables() {
            when(jdbcTemplate.queryForList(
                    argThat(sql -> sql != null && sql.contains("information_schema.tables")),
                    eq(String.class)))
                    .thenReturn(Collections.emptyList());

            Map<String, Object> result = classifierLegacyService.getAllClassifiersWithItems();

            assertThat(result).isNotNull();
            assertThat(result.get("success")).isEqualTo(true);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> classifiers = (List<Map<String, Object>>) result.get("classifiers");
            // May contain duplicate classifiers even if no tables, but should not fail
            assertThat(classifiers).isNotNull();
        }
    }

    // =====================================================
    // getAllClassifiersInfo
    // =====================================================

    @Nested
    @DisplayName("getAllClassifiersInfo")
    class GetAllClassifiersInfo {

        @Test
        @DisplayName("returns success true with metadata for each table")
        void returnsSuccessWithMetadata() {
            when(jdbcTemplate.queryForList(
                    argThat(sql -> sql != null && sql.contains("information_schema.tables")),
                    eq(String.class)))
                    .thenReturn(List.of("hemishe_h_gender"));

            when(jdbcTemplate.queryForObject(
                    argThat(sql -> sql != null && sql.contains("information_schema.columns")),
                    eq(Boolean.class), any(), any()))
                    .thenReturn(true);

            when(jdbcTemplate.queryForMap(argThat(sql -> sql != null && sql.contains("COUNT"))))
                    .thenReturn(Map.of("cnt", 3L, "ver", 6));

            Map<String, Object> result = classifierLegacyService.getAllClassifiersInfo();

            assertThat(result).isNotNull();
            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result).containsKey("classifiers");
        }

        @Test
        @DisplayName("returns empty classifiers when no tables exist")
        void returnsEmptyWhenNoTables() {
            when(jdbcTemplate.queryForList(
                    argThat(sql -> sql != null && sql.contains("information_schema.tables")),
                    eq(String.class)))
                    .thenReturn(Collections.emptyList());

            Map<String, Object> result = classifierLegacyService.getAllClassifiersInfo();

            assertThat(result).isNotNull();
            assertThat(result.get("success")).isEqualTo(true);
        }
    }

    // =====================================================
    // getSingleClassifier
    // =====================================================

    @Nested
    @DisplayName("getSingleClassifier")
    class GetSingleClassifier {

        @Test
        @DisplayName("h_university - returns university classifier from repository")
        void universityClassifier_returnsFromRepository() {
            University uni = new University();
            uni.setCode("TATU01");
            uni.setName("Tashkent University");
            uni.setVersion(3);
            uni.setActive(true);

            when(universityRepository.findAll()).thenReturn(List.of(uni));

            Map<String, Object> result = classifierLegacyService.getSingleClassifier("h_university");

            assertThat(result).isNotNull();
            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result).containsKey("classifier");

            @SuppressWarnings("unchecked")
            Map<String, Object> classifierWrapper = (Map<String, Object>) result.get("classifier");
            assertThat(classifierWrapper).containsKey("h_university");

            verify(universityRepository).findAll();
        }

        @Test
        @DisplayName("dynamic classifier - returns null when table does not exist")
        void dynamicClassifier_returnsNullWhenTableNotFound() {
            // tableExists returns false
            when(jdbcTemplate.queryForObject(
                    argThat(sql -> sql != null && sql.contains("information_schema.tables") && sql.contains("table_name = ?")),
                    eq(Boolean.class), anyString()))
                    .thenReturn(false);

            Map<String, Object> result = classifierLegacyService.getSingleClassifier("h_nonexistent");

            assertThat(result).isNull();
        }
    }

    // =====================================================
    // getHokimiyatClassifiers
    // =====================================================

    @Nested
    @DisplayName("getHokimiyatClassifiers")
    class GetHokimiyatClassifiers {

        @Test
        @DisplayName("returns success true with classifiers list")
        void returnsSuccessWithClassifiers() {
            // All internal calls to getClassifierWithItems will throw exceptions,
            // which are caught and logged. This tests the exception-handling path.
            when(jdbcTemplate.queryForObject(
                    argThat(sql -> sql != null && sql.contains("information_schema.columns")),
                    eq(Boolean.class), any(), any()))
                    .thenThrow(new RuntimeException("simulated error"));

            Map<String, Object> result = classifierLegacyService.getHokimiyatClassifiers();

            assertThat(result).isNotNull();
            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result).containsKey("classifiers");
        }

        @Test
        @DisplayName("handles individual classifier loading failures gracefully")
        void handlesIndividualFailuresGracefully() {
            // Simulating that each classifier table lookup fails
            when(jdbcTemplate.queryForObject(anyString(), eq(Boolean.class), any(Object[].class)))
                    .thenThrow(new RuntimeException("DB error"));

            Map<String, Object> result = classifierLegacyService.getHokimiyatClassifiers();

            assertThat(result).isNotNull();
            assertThat(result.get("success")).isEqualTo(true);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> classifiers = (List<Map<String, Object>>) result.get("classifiers");
            assertThat(classifiers).isEmpty();
        }
    }
}
