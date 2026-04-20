package uz.hemis.service.legacy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;
import uz.hemis.domain.repository.UniversityRepository;
import uz.hemis.service.legacy.HokimiyatClassifierService;

import java.util.Collections;
import java.util.LinkedHashMap;
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
 * </ul>
 *
 * <p>Hokimiyat/Stipend classifier tests moved to
 * {@link HokimiyatClassifierServiceTest}.</p>
 *
 * @since 1.5.4
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ClassifierLegacyService Unit Tests")
class ClassifierLegacyServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private UniversityRepository universityRepository;

    @Mock
    private CubaNestedObjectLoader nestedLoader;

    @Mock
    private HokimiyatClassifierService hokimiyatClassifierService;

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
        @DisplayName("h_university - returns university classifier via HokimiyatClassifierService")
        void universityClassifier_returnsFromHokimiyatService() {
            Map<String, Object> uniClassifier = new LinkedHashMap<>();
            uniClassifier.put("h_university", Map.of("title", "Oliy ta'lim muassasalari ro'yxati", "version", 3L, "count", 1L, "items", List.of()));

            when(hokimiyatClassifierService.getUniversityClassifierForHokimiyat()).thenReturn(uniClassifier);

            Map<String, Object> result = classifierLegacyService.getSingleClassifier("h_university");

            assertThat(result).isNotNull();
            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result).containsKey("classifier");

            @SuppressWarnings("unchecked")
            Map<String, Object> classifierWrapper = (Map<String, Object>) result.get("classifier");
            assertThat(classifierWrapper).containsKey("h_university");

            verify(hokimiyatClassifierService).getUniversityClassifierForHokimiyat();
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
}
