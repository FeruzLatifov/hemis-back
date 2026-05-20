package uz.hemis.service.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LegacyOtmIntegrationService — OTM student lookup")
class LegacyOtmIntegrationServiceTest {

    @Mock private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private LegacyOtmIntegrationService service;

    @Test
    @DisplayName("getStudentInfoById — topilgan: id+pinfl+name format")
    void byId_found() {
        Map<String, Object> row = Map.of(
                "code", "STU-001", "pinfl", "12345678901234",
                "lastname", "Karimov", "firstname", "Akmal", "fathername", "Bekzod o'g'li");
        when(jdbcTemplate.queryForMap(anyString(), anyString())).thenReturn(row);

        Map<String, Object> result = service.getStudentInfoById("STU-001");

        assertThat(result).containsEntry("id", "STU-001");
        assertThat(result).containsEntry("pinfl", "12345678901234");
        assertThat(result).containsEntry("name", "Karimov Akmal Bekzod o'g'li");
    }

    @Test
    @DisplayName("getStudentInfoById — not found → null")
    void byId_notFound() {
        when(jdbcTemplate.queryForMap(anyString(), anyString()))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThat(service.getStudentInfoById("X")).isNull();
    }

    @Test
    @DisplayName("getStudentInfoByPinfl — topilgan + university")
    void byPinfl_found() {
        Map<String, Object> row = new java.util.HashMap<>();
        row.put("code", "STU-001");
        row.put("pinfl", "11111111111111");
        row.put("lastname", "Aliyev");
        row.put("firstname", "Bekzod");
        row.put("fathername", null);
        row.put("_university", "337");

        when(jdbcTemplate.queryForMap(anyString(), anyString(), anyString())).thenReturn(row);

        Map<String, Object> result = service.getStudentInfoByPinfl("11111111111111", "337");

        assertThat(result).containsEntry("pinfl", "11111111111111");
        assertThat(result).containsEntry("name", "Aliyev Bekzod");  // fathername null → skip
        assertThat(result).containsEntry("university", "337");
    }

    @Test
    @DisplayName("getStudentInfoByPinfl — not found → null")
    void byPinfl_notFound() {
        when(jdbcTemplate.queryForMap(anyString(), anyString(), anyString()))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThat(service.getStudentInfoByPinfl("X", "999")).isNull();
    }

    @Test
    @DisplayName("getStudentListByTutor — stub returns empty list")
    void byTutor_emptyStub() {
        assertThat(service.getStudentListByTutor("337", "11111111111111")).isEmpty();
    }

    @Test
    @DisplayName("buildFullName — all nulls → empty string")
    void allNullNames_emptyString() {
        Map<String, Object> row = new java.util.HashMap<>();
        row.put("code", "X");
        row.put("pinfl", "X");
        row.put("lastname", null);
        row.put("firstname", null);
        row.put("fathername", null);

        when(jdbcTemplate.queryForMap(anyString(), anyString())).thenReturn(row);

        Map<String, Object> result = service.getStudentInfoById("X");

        assertThat(result).containsEntry("name", "");
    }
}
