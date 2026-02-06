package uz.hemis.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CubaNestedObjectLoaderTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private CubaNestedObjectLoader loader;

    @BeforeEach
    void setUp() {
        loader = new CubaNestedObjectLoader(jdbcTemplate);
    }

    // =====================================================
    // loadClassifier tests
    // =====================================================

    @Test
    void loadClassifier_found_returnsMapWithCodeAndName() {
        when(jdbcTemplate.queryForMap(
                contains("SELECT code, name"),
                eq("11")
        )).thenReturn(Map.of("code", "11", "name", "Bakalavr", "version", 1));

        Map<String, Object> result = loader.loadClassifier(
                "hemishe_h_education_type", "HEducationType", "11");

        assertThat(result).isNotNull();
        assertThat(result.get("_entityName")).isEqualTo("hemishe_HEducationType");
        assertThat(result.get("id")).isEqualTo("11");
        assertThat(result.get("code")).isEqualTo("11");
        assertThat(result.get("name")).isEqualTo("Bakalavr");
    }

    @Test
    void loadClassifier_notFound_returnsFallback() {
        when(jdbcTemplate.queryForMap(
                contains("SELECT code, name"),
                eq("999")
        )).thenThrow(new EmptyResultDataAccessException(1));

        Map<String, Object> result = loader.loadClassifier(
                "hemishe_h_education_type", "HEducationType", "999");

        assertThat(result).isNotNull();
        assertThat(result.get("_entityName")).isEqualTo("hemishe_HEducationType");
        assertThat(result.get("id")).isEqualTo("999");
    }

    @Test
    void loadClassifier_nullCode_returnsNull() {
        Map<String, Object> result = loader.loadClassifier(
                "hemishe_h_education_type", "HEducationType", null);

        assertThat(result).isNull();
    }

    @Test
    void loadClassifier_emptyCode_returnsNull() {
        Map<String, Object> result = loader.loadClassifier(
                "hemishe_h_education_type", "HEducationType", "");

        assertThat(result).isNull();
    }

    // =====================================================
    // loadUniversity tests
    // =====================================================

    @Test
    void loadUniversity_found_returnsMapWithName() {
        when(jdbcTemplate.queryForMap(
                contains("hemishe_e_university"),
                eq("999")
        )).thenReturn(Map.of("code", "999", "name", "Test University", "version", 1));

        Map<String, Object> result = loader.loadUniversity("999");

        assertThat(result).isNotNull();
        assertThat(result.get("_entityName")).isEqualTo("hemishe_EUniversity");
        assertThat(result.get("name")).isEqualTo("Test University");
    }

    @Test
    void loadUniversity_notFound_returnsFallback() {
        when(jdbcTemplate.queryForMap(
                contains("hemishe_e_university"),
                eq("000")
        )).thenThrow(new EmptyResultDataAccessException(1));

        Map<String, Object> result = loader.loadUniversity("000");

        assertThat(result).isNotNull();
        assertThat(result.get("_entityName")).isEqualTo("hemishe_EUniversity");
        assertThat(result.get("id")).isEqualTo("000");
    }

    @Test
    void loadUniversity_nullCode_returnsNull() {
        Map<String, Object> result = loader.loadUniversity(null);
        assertThat(result).isNull();
    }

    // =====================================================
    // loadDepartment tests
    // =====================================================

    @Test
    void loadDepartment_found_returnsMapWithNameUz() {
        when(jdbcTemplate.queryForMap(
                contains("hemishe_e_university_department"),
                eq("999-101")
        )).thenReturn(Map.of("code", "999-101", "name_uz", "Informatika", "version", 1));

        Map<String, Object> result = loader.loadDepartment("999-101");

        assertThat(result).isNotNull();
        assertThat(result.get("_entityName")).isEqualTo("hemishe_EUniversityDepartment");
        assertThat(result.get("_instanceName")).isEqualTo("Informatika");
    }

    @Test
    void loadDepartment_nullCode_returnsNull() {
        Map<String, Object> result = loader.loadDepartment(null);
        assertThat(result).isNull();
    }

    // =====================================================
    // loadSoato tests
    // =====================================================

    @Test
    void loadSoato_found_returnsMap() {
        when(jdbcTemplate.queryForMap(
                contains("hemishe_h_soato"),
                eq("1726")
        )).thenReturn(Map.of("code", "1726", "name_uz", "Toshkent", "name_ru", "Ташкент", "version", 1, "active", true));

        Map<String, Object> result = loader.loadSoato("1726");

        assertThat(result).isNotNull();
        assertThat(result.get("_entityName")).isEqualTo("hemishe_HSoato");
        assertThat(result.get("name_uz")).isEqualTo("Toshkent");
        assertThat(result.get("name_ru")).isEqualTo("Ташкент");
        assertThat(result.get("code")).isEqualTo("1726");
    }

    @Test
    void loadSoato_nullCode_returnsNull() {
        Map<String, Object> result = loader.loadSoato(null);
        assertThat(result).isNull();
    }

    // =====================================================
    // sanitizeTableName tests (via loadClassifier)
    // =====================================================

    @Test
    void loadClassifier_invalidTableName_returnsNull() {
        // sanitizeTableName throws IllegalArgumentException, but loadClassifier catches all exceptions and returns null
        Map<String, Object> result = loader.loadClassifier("table; DROP TABLE", "HTest", "11");
        assertThat(result).isNull();
    }

    @Test
    void loadClassifier_nullTableName_returnsNull() {
        // sanitizeTableName throws IllegalArgumentException, but loadClassifier catches all exceptions and returns null
        Map<String, Object> result = loader.loadClassifier(null, "HTest", "11");
        assertThat(result).isNull();
    }
}
