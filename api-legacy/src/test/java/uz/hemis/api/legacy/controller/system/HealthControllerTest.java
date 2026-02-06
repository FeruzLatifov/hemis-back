package uz.hemis.api.legacy.controller.system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uz.hemis.common.dto.ResponseWrapper;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link HealthController}.
 *
 * <p>This is a simple controller with no injected dependencies,
 * so direct instantiation is used (no mocking required).</p>
 *
 * <p>Tests cover:</p>
 * <ul>
 *   <li>getHealth - returns status UP with application info</li>
 *   <li>ping - returns running status with timestamp</li>
 *   <li>getVersion - returns version info</li>
 * </ul>
 *
 * @since 1.0.0
 */
@DisplayName("HealthController Unit Tests")
class HealthControllerTest {

    private HealthController controller;

    @BeforeEach
    void setUp() {
        controller = new HealthController();
    }

    // =====================================================
    // getHealth tests
    // =====================================================

    @Nested
    @DisplayName("GET /app/rest/v2/health")
    class GetHealthTests {

        @Test
        @DisplayName("Returns HTTP 200 with success wrapper")
        void returnsOkStatus() {
            ResponseEntity<ResponseWrapper<Map<String, Object>>> response = controller.getHealth();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getSuccess()).isTrue();
        }

        @Test
        @DisplayName("Response data contains status UP")
        void responseContainsStatusUp() {
            ResponseEntity<ResponseWrapper<Map<String, Object>>> response = controller.getHealth();

            Map<String, Object> data = response.getBody().getData();
            assertThat(data).isNotNull();
            assertThat(data).containsEntry("status", "UP");
        }

        @Test
        @DisplayName("Response data contains application name HEMIS")
        void responseContainsApplicationName() {
            ResponseEntity<ResponseWrapper<Map<String, Object>>> response = controller.getHealth();

            Map<String, Object> data = response.getBody().getData();
            assertThat(data).containsEntry("application", "HEMIS");
        }

        @Test
        @DisplayName("Response data contains version 2.0.0")
        void responseContainsVersion() {
            ResponseEntity<ResponseWrapper<Map<String, Object>>> response = controller.getHealth();

            Map<String, Object> data = response.getBody().getData();
            assertThat(data).containsEntry("version", "2.0.0");
        }

        @Test
        @DisplayName("Response data contains a timestamp close to now")
        void responseContainsTimestamp() {
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            ResponseEntity<ResponseWrapper<Map<String, Object>>> response = controller.getHealth();

            LocalDateTime after = LocalDateTime.now().plusSeconds(1);
            Map<String, Object> data = response.getBody().getData();
            assertThat(data).containsKey("timestamp");
            LocalDateTime timestamp = (LocalDateTime) data.get("timestamp");
            assertThat(timestamp).isAfterOrEqualTo(before);
            assertThat(timestamp).isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("Response data contains exactly 4 keys")
        void responseContainsExpectedKeyCount() {
            ResponseEntity<ResponseWrapper<Map<String, Object>>> response = controller.getHealth();

            Map<String, Object> data = response.getBody().getData();
            assertThat(data).hasSize(4);
            assertThat(data).containsKeys("status", "timestamp", "application", "version");
        }
    }

    // =====================================================
    // ping tests
    // =====================================================

    @Nested
    @DisplayName("GET /app/rest/v2/health/ping")
    class PingTests {

        @Test
        @DisplayName("Returns HTTP 200 with success wrapper")
        void returnsOkStatus() {
            ResponseEntity<ResponseWrapper<Map<String, Object>>> response = controller.ping();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getSuccess()).isTrue();
        }

        @Test
        @DisplayName("Response data contains status UP")
        void responseContainsStatusUp() {
            ResponseEntity<ResponseWrapper<Map<String, Object>>> response = controller.ping();

            Map<String, Object> data = response.getBody().getData();
            assertThat(data).isNotNull();
            assertThat(data).containsEntry("status", "UP");
        }

        @Test
        @DisplayName("Response data contains 'Service is running' message")
        void responseContainsRunningMessage() {
            ResponseEntity<ResponseWrapper<Map<String, Object>>> response = controller.ping();

            Map<String, Object> data = response.getBody().getData();
            assertThat(data).containsEntry("message", "Service is running");
        }

        @Test
        @DisplayName("Response data contains a timestamp close to now")
        void responseContainsTimestamp() {
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            ResponseEntity<ResponseWrapper<Map<String, Object>>> response = controller.ping();

            LocalDateTime after = LocalDateTime.now().plusSeconds(1);
            Map<String, Object> data = response.getBody().getData();
            assertThat(data).containsKey("timestamp");
            LocalDateTime timestamp = (LocalDateTime) data.get("timestamp");
            assertThat(timestamp).isAfterOrEqualTo(before);
            assertThat(timestamp).isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("Response data contains exactly 3 keys")
        void responseContainsExpectedKeyCount() {
            ResponseEntity<ResponseWrapper<Map<String, Object>>> response = controller.ping();

            Map<String, Object> data = response.getBody().getData();
            assertThat(data).hasSize(3);
            assertThat(data).containsKeys("status", "timestamp", "message");
        }

        @Test
        @DisplayName("Ping response does not contain version or application fields")
        void pingDoesNotContainVersionOrApplication() {
            ResponseEntity<ResponseWrapper<Map<String, Object>>> response = controller.ping();

            Map<String, Object> data = response.getBody().getData();
            assertThat(data).doesNotContainKey("version");
            assertThat(data).doesNotContainKey("application");
        }
    }

    // =====================================================
    // getVersion tests
    // =====================================================

    @Nested
    @DisplayName("GET /app/rest/v2/health/version")
    class GetVersionTests {

        @Test
        @DisplayName("Returns HTTP 200 with success wrapper")
        void returnsOkStatus() {
            ResponseEntity<ResponseWrapper<Map<String, String>>> response = controller.getVersion();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getSuccess()).isTrue();
        }

        @Test
        @DisplayName("Response data contains application name HEMIS")
        void responseContainsApplicationName() {
            ResponseEntity<ResponseWrapper<Map<String, String>>> response = controller.getVersion();

            Map<String, String> data = response.getBody().getData();
            assertThat(data).isNotNull();
            assertThat(data).containsEntry("application", "HEMIS");
        }

        @Test
        @DisplayName("Response data contains version 2.0.0")
        void responseContainsVersion() {
            ResponseEntity<ResponseWrapper<Map<String, String>>> response = controller.getVersion();

            Map<String, String> data = response.getBody().getData();
            assertThat(data).containsEntry("version", "2.0.0");
        }

        @Test
        @DisplayName("Response data contains build timestamp as a non-empty string")
        void responseContainsBuildTimestamp() {
            ResponseEntity<ResponseWrapper<Map<String, String>>> response = controller.getVersion();

            Map<String, String> data = response.getBody().getData();
            assertThat(data).containsKey("build");
            assertThat(data.get("build")).isNotBlank();
        }

        @Test
        @DisplayName("Build timestamp is a valid LocalDateTime string")
        void buildTimestampIsValidLocalDateTime() {
            ResponseEntity<ResponseWrapper<Map<String, String>>> response = controller.getVersion();

            Map<String, String> data = response.getBody().getData();
            String buildStr = data.get("build");
            // Should parse without exception
            LocalDateTime buildTime = LocalDateTime.parse(buildStr);
            assertThat(buildTime).isNotNull();
            // Should be close to now
            assertThat(buildTime).isAfterOrEqualTo(LocalDateTime.now().minusSeconds(5));
            assertThat(buildTime).isBeforeOrEqualTo(LocalDateTime.now().plusSeconds(5));
        }

        @Test
        @DisplayName("Response data contains exactly 3 keys")
        void responseContainsExpectedKeyCount() {
            ResponseEntity<ResponseWrapper<Map<String, String>>> response = controller.getVersion();

            Map<String, String> data = response.getBody().getData();
            assertThat(data).hasSize(3);
            assertThat(data).containsKeys("application", "version", "build");
        }

        @Test
        @DisplayName("Version response uses String values (not Object map)")
        void versionResponseUsesStringValues() {
            ResponseEntity<ResponseWrapper<Map<String, String>>> response = controller.getVersion();

            Map<String, String> data = response.getBody().getData();
            // All values should be String instances
            data.values().forEach(value ->
                    assertThat(value).isInstanceOf(String.class)
            );
        }
    }

    // =====================================================
    // Cross-endpoint consistency tests
    // =====================================================

    @Nested
    @DisplayName("Cross-endpoint consistency")
    class CrossEndpointTests {

        @Test
        @DisplayName("Health and version endpoints report the same version number")
        void healthAndVersionReportSameVersion() {
            ResponseEntity<ResponseWrapper<Map<String, Object>>> healthResponse = controller.getHealth();
            ResponseEntity<ResponseWrapper<Map<String, String>>> versionResponse = controller.getVersion();

            String healthVersion = (String) healthResponse.getBody().getData().get("version");
            String versionVersion = versionResponse.getBody().getData().get("version");

            assertThat(healthVersion).isEqualTo(versionVersion);
        }

        @Test
        @DisplayName("Health and version endpoints report the same application name")
        void healthAndVersionReportSameApplication() {
            ResponseEntity<ResponseWrapper<Map<String, Object>>> healthResponse = controller.getHealth();
            ResponseEntity<ResponseWrapper<Map<String, String>>> versionResponse = controller.getVersion();

            String healthApp = (String) healthResponse.getBody().getData().get("application");
            String versionApp = versionResponse.getBody().getData().get("application");

            assertThat(healthApp).isEqualTo(versionApp);
        }

        @Test
        @DisplayName("Health and ping endpoints both report status UP")
        void healthAndPingBothReportStatusUp() {
            ResponseEntity<ResponseWrapper<Map<String, Object>>> healthResponse = controller.getHealth();
            ResponseEntity<ResponseWrapper<Map<String, Object>>> pingResponse = controller.ping();

            assertThat(healthResponse.getBody().getData().get("status")).isEqualTo("UP");
            assertThat(pingResponse.getBody().getData().get("status")).isEqualTo("UP");
        }

        @Test
        @DisplayName("All endpoints return success=true in wrapper")
        void allEndpointsReturnSuccess() {
            assertThat(controller.getHealth().getBody().getSuccess()).isTrue();
            assertThat(controller.ping().getBody().getSuccess()).isTrue();
            assertThat(controller.getVersion().getBody().getSuccess()).isTrue();
        }
    }
}
