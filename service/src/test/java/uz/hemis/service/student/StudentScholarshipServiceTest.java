package uz.hemis.service.student;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link StudentScholarshipService}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>checkScholarshipNative - bad TIN validation, successful check</li>
 *   <li>isExpel - successful query</li>
 * </ul>
 *
 * @since 2.1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StudentScholarshipService Unit Tests")
class StudentScholarshipServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private StudentScholarshipService studentScholarshipService;

    // =====================================================
    // checkScholarshipNative tests
    // =====================================================

    @Nested
    @DisplayName("checkScholarshipNative")
    class CheckScholarshipNative {

        @Test
        @DisplayName("returns bad request when TIN is null")
        void returnsBadRequest_whenTinNull() {
            Map<String, Object> result = studentScholarshipService.checkScholarshipNative(null, new String[]{"12345678901234"});

            assertThat(result.get("success")).isEqualTo(false);
            assertThat(result.get("data")).isEqualTo("Bad request");
        }

        @Test
        @DisplayName("returns bad request when TIN contains non-digit characters")
        void returnsBadRequest_whenTinNonDigit() {
            Map<String, Object> result = studentScholarshipService.checkScholarshipNative("abc123", new String[]{"12345678901234"});

            assertThat(result.get("success")).isEqualTo(false);
            assertThat(result.get("data")).isEqualTo("Bad request");
        }

        @Test
        @DisplayName("returns bad request when pinfls array is empty")
        void returnsBadRequest_whenPinflsEmpty() {
            Map<String, Object> result = studentScholarshipService.checkScholarshipNative("123456789", new String[]{});

            assertThat(result.get("success")).isEqualTo(false);
            assertThat(result.get("data")).isEqualTo("Bad request");
        }

        @Test
        @DisplayName("returns bad request when pinfl contains non-digit characters")
        void returnsBadRequest_whenPinflNonDigit() {
            Map<String, Object> result = studentScholarshipService.checkScholarshipNative("123456789", new String[]{"abc"});

            assertThat(result.get("success")).isEqualTo(false);
            assertThat(result.get("data")).isEqualTo("Bad request");
        }

        @Test
        @DisplayName("returns success with scholarship data when valid parameters")
        @SuppressWarnings("unchecked")
        void returnsSuccess_whenValidParams() throws SQLException {
            DataSource dataSource = mock(DataSource.class);
            Connection connection = mock(Connection.class);
            Array sqlArray = mock(Array.class);

            when(jdbcTemplate.getDataSource()).thenReturn(dataSource);
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.createArrayOf(eq("text"), any(Object[].class))).thenReturn(sqlArray);

            List<Map<String, Object>> items = List.of(
                    Map.of("pinfl", "12345678901234", "fullname", "Karimov Jasur")
            );
            when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(items);

            Map<String, Object> result = studentScholarshipService.checkScholarshipNative(
                    "123456789", new String[]{"12345678901234"});

            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result.get("data")).isInstanceOf(List.class);
            assertThat((List<Map<String, Object>>) result.get("data")).hasSize(1);

            verify(connection).close();
        }
    }

    // =====================================================
    // isExpel tests
    // =====================================================

    @Nested
    @DisplayName("isExpel")
    class IsExpel {

        @Test
        @DisplayName("returns bad request when pinfls is null")
        void returnsBadRequest_whenPinflsNull() {
            Map<String, Object> result = studentScholarshipService.isExpel(null);

            assertThat(result.get("success")).isEqualTo(false);
            assertThat(result.get("data")).isEqualTo("Bad request");
        }

        @Test
        @DisplayName("returns bad request when pinfls is empty")
        void returnsBadRequest_whenPinflsEmpty() {
            Map<String, Object> result = studentScholarshipService.isExpel(new String[]{});

            assertThat(result.get("success")).isEqualTo(false);
            assertThat(result.get("data")).isEqualTo("Bad request");
        }

        @Test
        @DisplayName("returns success with expel data when valid pinfls")
        @SuppressWarnings("unchecked")
        void returnsSuccess_whenValidPinfls() {
            List<Map<String, Object>> items = List.of(
                    Map.of("pinfl", "12345678901234", "fullname", "Karimov Jasur",
                            "universityCode", "401", "expelReasonCode", "01",
                            "expelReasonName", "O'z xohishi bilan")
            );
            when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(items);

            Map<String, Object> result = studentScholarshipService.isExpel(new String[]{"12345678901234"});

            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result.get("data")).isInstanceOf(List.class);
            assertThat((List<Map<String, Object>>) result.get("data")).hasSize(1);
        }
    }
}
