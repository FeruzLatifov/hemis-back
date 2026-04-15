package uz.hemis.service.student;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link StudentStatisticsService}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>byTashkentAndPaymentForm - returns statistics map</li>
 *   <li>executeTashkentStats - success and error handling</li>
 * </ul>
 *
 * @since 2.1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StudentStatisticsService Unit Tests")
class StudentStatisticsServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private StudentStatisticsService studentStatisticsService;

    // =====================================================
    // byTashkentAndPaymentForm tests
    // =====================================================

    @Nested
    @DisplayName("byTashkentAndPaymentForm")
    class ByTashkentAndPaymentForm {

        @Test
        @DisplayName("returns map with status OK and items when query succeeds")
        @SuppressWarnings("unchecked")
        void returnsMap_withStatusOk() {
            List<Map<String, Object>> items = List.of(
                    Map.of("university_code", "401", "university_name", "Test University",
                            "budget", 100, "contract", 200, "total", 300)
            );

            when(jdbcTemplate.queryForList(anyString())).thenReturn(items);
            when(jdbcTemplate.queryForObject(anyString(), eq(Long.class))).thenReturn(5000L);

            Map<String, Object> result = studentStatisticsService.byTashkentAndPaymentForm();

            assertThat(result.get("status")).isEqualTo("OK");
            assertThat(result.get("title")).isNotNull();
            assertThat(result.get("total_student_count")).isEqualTo(5000L);
            assertThat(result.get("columns")).isNotNull();
            assertThat(result.get("columns")).isInstanceOf(Map.class);
            assertThat(result.get("items")).isInstanceOf(List.class);
            assertThat((List<Map<String, Object>>) result.get("items")).hasSize(1);

            verify(jdbcTemplate).queryForList(anyString());
            verify(jdbcTemplate).queryForObject(anyString(), eq(Long.class));
        }

        @Test
        @DisplayName("returns map with status ERROR when query fails")
        void returnsMap_withStatusError() {
            when(jdbcTemplate.queryForList(anyString()))
                    .thenThrow(new RuntimeException("Database error"));

            Map<String, Object> result = studentStatisticsService.byTashkentAndPaymentForm();

            assertThat(result.get("status")).isEqualTo("ERROR");
            assertThat(result.get("title")).isNotNull();
            assertThat(result.get("message")).isEqualTo("Database error");
        }
    }

    // =====================================================
    // executeTashkentStats tests (via byTashkentAndRegionDistrict)
    // =====================================================

    @Nested
    @DisplayName("executeTashkentStats")
    class ExecuteTashkentStats {

        @Test
        @DisplayName("returns complete response structure on success")
        @SuppressWarnings("unchecked")
        void returnsCompleteResponse_onSuccess() {
            List<Map<String, Object>> items = List.of(
                    Map.of("university_code", "401", "university_name", "Test University",
                            "region_code", "1726", "region_name", "Toshkent",
                            "district_code", "01", "district_name", "Chilonzor",
                            "payment_form_code", "11", "payment_form_name", "Davlat granti",
                            "student_count", 150)
            );

            when(jdbcTemplate.queryForList(anyString())).thenReturn(items);
            when(jdbcTemplate.queryForObject(anyString(), eq(Long.class))).thenReturn(10000L);

            Map<String, Object> result = studentStatisticsService.byTashkentAndRegionDistrict();

            assertThat(result.get("status")).isEqualTo("OK");
            assertThat(result.get("total_student_count")).isEqualTo(10000L);
            assertThat(result.get("columns")).isNotNull();
            assertThat(result.get("items")).isNotNull();
            assertThat(result.get("message")).isNull();

            List<Map<String, Object>> resultItems = (List<Map<String, Object>>) result.get("items");
            assertThat(resultItems).hasSize(1);
            assertThat(resultItems.get(0).get("university_code")).isEqualTo("401");
        }
    }
}
