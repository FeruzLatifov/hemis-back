package uz.hemis.service.legacy;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link HokimiyatClassifierService}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>getHokimiyatClassifiers - success and exception handling</li>
 * </ul>
 *
 * @since 2.2.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HokimiyatClassifierService Unit Tests")
class HokimiyatClassifierServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private HokimiyatClassifierService hokimiyatClassifierService;

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

            Map<String, Object> result = hokimiyatClassifierService.getHokimiyatClassifiers();

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

            Map<String, Object> result = hokimiyatClassifierService.getHokimiyatClassifiers();

            assertThat(result).isNotNull();
            assertThat(result.get("success")).isEqualTo(true);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> classifiers = (List<Map<String, Object>>) result.get("classifiers");
            assertThat(classifiers).isEmpty();
        }
    }
}
