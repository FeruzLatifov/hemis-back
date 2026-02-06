package uz.hemis.service.legacy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import uz.hemis.common.dto.system.TranslationFilterRequest;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TranslationLegacyService}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>loadTranslations - table exists with data, table not found, null request,
 *       request with category filter, exception handling, empty result set,
 *       partial columns available, various column presence scenarios</li>
 * </ul>
 *
 * @since 1.5.4
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TranslationLegacyService Unit Tests")
class TranslationLegacyServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private TranslationLegacyService translationLegacyService;

    // =====================================================
    // loadTranslations
    // =====================================================

    @Nested
    @DisplayName("loadTranslations")
    class LoadTranslations {

        @Test
        @DisplayName("returns translations when table exists and has data")
        void returnsTranslations_whenTableExistsAndHasData() {
            // Table exists
            when(jdbcTemplate.queryForObject(
                    argThat(sql -> sql != null && sql.contains("information_schema.tables")),
                    eq(Boolean.class)))
                    .thenReturn(true);

            // Available columns
            when(jdbcTemplate.queryForList(
                    argThat(sql -> sql != null && sql.contains("information_schema.columns")),
                    eq(String.class)))
                    .thenReturn(List.of("id", "message", "uz_uz", "ru_ru", "oz_uz", "en_us", "kk_uz", "category", "version", "delete_ts"));

            // Translation rows
            UUID translationId = UUID.randomUUID();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", translationId);
            row.put("message", "hello.world");
            row.put("uz_uz", "Salom dunyo");
            row.put("ru_ru", "Privet mir");
            row.put("oz_uz", "Salom dunyo (oz)");
            row.put("en_us", "Hello world");
            row.put("kk_uz", "Salom dunyo (kk)");
            row.put("category", "common");
            row.put("version", 3);

            when(jdbcTemplate.queryForList(
                    argThat(sql -> sql != null && sql.startsWith("SELECT ")),
                    any(Object[].class)))
                    .thenReturn(List.of(row));

            List<Map<String, Object>> result = translationLegacyService.loadTranslations(null);

            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);

            Map<String, Object> item = result.get(0);
            assertThat(item.get("_entityName")).isEqualTo("hemishe_ETranslation");
            assertThat(item.get("id")).isEqualTo(translationId.toString());
            assertThat(item.get("message")).isEqualTo("hello.world");
            assertThat(item.get("uz_Uz")).isEqualTo("Salom dunyo");
            assertThat(item.get("ru_Ru")).isEqualTo("Privet mir");
            assertThat(item.get("en_Us")).isEqualTo("Hello world");
            assertThat(item.get("category")).isEqualTo("common");
            assertThat(item.get("version")).isEqualTo(3);
        }

        @Test
        @DisplayName("returns empty list when table does not exist")
        void returnsEmptyList_whenTableDoesNotExist() {
            when(jdbcTemplate.queryForObject(
                    argThat(sql -> sql != null && sql.contains("information_schema.tables")),
                    eq(Boolean.class)))
                    .thenReturn(false);

            List<Map<String, Object>> result = translationLegacyService.loadTranslations(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("filters by category when request has category set")
        void filtersByCategory_whenRequestHasCategory() {
            when(jdbcTemplate.queryForObject(
                    argThat(sql -> sql != null && sql.contains("information_schema.tables")),
                    eq(Boolean.class)))
                    .thenReturn(true);

            when(jdbcTemplate.queryForList(
                    argThat(sql -> sql != null && sql.contains("information_schema.columns")),
                    eq(String.class)))
                    .thenReturn(List.of("id", "message", "category", "delete_ts"));

            when(jdbcTemplate.queryForList(
                    argThat(sql -> sql != null && sql.contains("category = ?")),
                    any(Object[].class)))
                    .thenReturn(Collections.emptyList());

            TranslationFilterRequest request = new TranslationFilterRequest();
            request.setCategory("dashboard");

            List<Map<String, Object>> result = translationLegacyService.loadTranslations(request);

            assertThat(result).isEmpty();
            // Verify the SQL was called with category parameter
            verify(jdbcTemplate).queryForList(
                    argThat(sql -> sql != null && sql.contains("category = ?")),
                    any(Object[].class));
        }

        @Test
        @DisplayName("returns empty list when exception occurs during loading")
        void returnsEmptyList_whenExceptionOccurs() {
            when(jdbcTemplate.queryForObject(anyString(), eq(Boolean.class)))
                    .thenThrow(new RuntimeException("DB error"));

            List<Map<String, Object>> result = translationLegacyService.loadTranslations(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty list when table exists but no rows found")
        void returnsEmptyList_whenTableExistsButNoRows() {
            when(jdbcTemplate.queryForObject(
                    argThat(sql -> sql != null && sql.contains("information_schema.tables")),
                    eq(Boolean.class)))
                    .thenReturn(true);

            when(jdbcTemplate.queryForList(
                    argThat(sql -> sql != null && sql.contains("information_schema.columns")),
                    eq(String.class)))
                    .thenReturn(List.of("id", "message", "delete_ts"));

            when(jdbcTemplate.queryForList(
                    argThat(sql -> sql != null && sql.startsWith("SELECT ")),
                    any(Object[].class)))
                    .thenReturn(Collections.emptyList());

            List<Map<String, Object>> result = translationLegacyService.loadTranslations(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("handles partial columns gracefully - only id and message")
        void handlesPartialColumns_onlyIdAndMessage() {
            when(jdbcTemplate.queryForObject(
                    argThat(sql -> sql != null && sql.contains("information_schema.tables")),
                    eq(Boolean.class)))
                    .thenReturn(true);

            // Only id and message columns available (no language columns)
            when(jdbcTemplate.queryForList(
                    argThat(sql -> sql != null && sql.contains("information_schema.columns")),
                    eq(String.class)))
                    .thenReturn(List.of("id", "message"));

            UUID translationId = UUID.randomUUID();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", translationId);
            row.put("message", "test.key");

            when(jdbcTemplate.queryForList(
                    argThat(sql -> sql != null && sql.startsWith("SELECT ")),
                    any(Object[].class)))
                    .thenReturn(List.of(row));

            List<Map<String, Object>> result = translationLegacyService.loadTranslations(null);

            assertThat(result).hasSize(1);
            Map<String, Object> item = result.get(0);
            assertThat(item.get("_entityName")).isEqualTo("hemishe_ETranslation");
            assertThat(item.get("message")).isEqualTo("test.key");
            // Language columns should not be present
            assertThat(item).doesNotContainKey("uz_Uz");
            assertThat(item).doesNotContainKey("ru_Ru");
            assertThat(item).doesNotContainKey("en_Us");
            assertThat(item).doesNotContainKey("category");
            assertThat(item).doesNotContainKey("version");
        }

        @Test
        @DisplayName("handles null id in translation row gracefully")
        void handlesNullIdInRow() {
            when(jdbcTemplate.queryForObject(
                    argThat(sql -> sql != null && sql.contains("information_schema.tables")),
                    eq(Boolean.class)))
                    .thenReturn(true);

            when(jdbcTemplate.queryForList(
                    argThat(sql -> sql != null && sql.contains("information_schema.columns")),
                    eq(String.class)))
                    .thenReturn(List.of("id", "message"));

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", null);
            row.put("message", "test.null.id");

            when(jdbcTemplate.queryForList(
                    argThat(sql -> sql != null && sql.startsWith("SELECT ")),
                    any(Object[].class)))
                    .thenReturn(List.of(row));

            List<Map<String, Object>> result = translationLegacyService.loadTranslations(null);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).get("id")).isEqualTo("");
        }

        @Test
        @DisplayName("handles version null value in row by defaulting to 1")
        void handlesNullVersion_defaultsToOne() {
            when(jdbcTemplate.queryForObject(
                    argThat(sql -> sql != null && sql.contains("information_schema.tables")),
                    eq(Boolean.class)))
                    .thenReturn(true);

            when(jdbcTemplate.queryForList(
                    argThat(sql -> sql != null && sql.contains("information_schema.columns")),
                    eq(String.class)))
                    .thenReturn(List.of("id", "version"));

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", UUID.randomUUID());
            row.put("version", null);

            when(jdbcTemplate.queryForList(
                    argThat(sql -> sql != null && sql.startsWith("SELECT ")),
                    any(Object[].class)))
                    .thenReturn(List.of(row));

            List<Map<String, Object>> result = translationLegacyService.loadTranslations(null);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).get("version")).isEqualTo(1);
        }
    }
}
