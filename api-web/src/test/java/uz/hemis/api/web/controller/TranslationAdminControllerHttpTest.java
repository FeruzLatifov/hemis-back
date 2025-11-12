package uz.hemis.api.web.controller.api;

import org.junit.jupiter.api.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP Integration Tests for TranslationAdminController
 *
 * <p><strong>Test Strategy:</strong></p>
 * <ul>
 *   <li>Tests against running backend (localhost:8081)</li>
 *   <li>Uses Java 11+ HttpClient (no external dependencies)</li>
 *   <li>Real database and cache testing</li>
 *   <li>JWT authentication flow</li>
 * </ul>
 *
 * <p><strong>Prerequisites:</strong></p>
 * <ul>
 *   <li>Backend must be running: ./gradlew bootRun</li>
 *   <li>Database must be accessible: test_hemis</li>
 *   <li>Redis must be running</li>
 *   <li>Admin user exists: username=admin, password=admin</li>
 * </ul>
 *
 * <p><strong>Best Practice:</strong> HTTP tests are simpler and test the real system</p>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TranslationAdminControllerHttpTest {

    private static final String BASE_URL = "http://localhost:8081";
    private static final String API_URL = BASE_URL + "/api/v1/admin/translations";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static String jwtToken;
    private static String testTranslationId;

    // ==========================================================================
    // Setup: Get JWT Token
    // ==========================================================================

    @BeforeAll
    static void setup() throws Exception {
        System.out.println("\n==========================================================================");
        System.out.println("Starting Translation Admin Controller HTTP Tests");
        System.out.println("==========================================================================\n");

        // Login to get JWT token
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "admin");
        loginRequest.put("password", "admin");

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/v1/web/auth/login"))
            .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(loginRequest)))
            .header("Content-Type", "application/json")
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonNode json = mapper.readTree(response.body());
            jwtToken = json.get("accessToken").asText();
            System.out.println("✅ JWT Token obtained successfully");
        } else {
            throw new RuntimeException("Failed to login: " + response.statusCode());
        }
    }

    // ==========================================================================
    // Test 1: List Translations
    // ==========================================================================

    @Test
    @Order(1)
    @DisplayName("Test 1: Should list all translations with pagination")
    void testListTranslations() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_URL + "?page=0&size=20"))
            .GET()
            .header("Authorization", "Bearer " + jwtToken)
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Test 1 - List Translations: " + response.statusCode());
        Assertions.assertEquals(200, response.statusCode(), "Should return 200 OK");

        JsonNode json = mapper.readTree(response.body());
        Assertions.assertTrue(json.has("content"), "Response should have content field");
        Assertions.assertTrue(json.has("totalItems"), "Response should have totalItems");
        Assertions.assertTrue(json.get("content").isArray(), "Content should be an array");

        System.out.println("✅ Test 1 PASSED - Found " + json.get("totalItems").asInt() + " translations");
    }

    // ==========================================================================
    // Test 2: Filter by Category
    // ==========================================================================

    @Test
    @Order(2)
    @DisplayName("Test 2: Should filter translations by category")
    void testFilterByCategory() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_URL + "?category=menu&page=0&size=10"))
            .GET()
            .header("Authorization", "Bearer " + jwtToken)
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Test 2 - Filter by Category: " + response.statusCode());
        Assertions.assertEquals(200, response.statusCode());

        JsonNode json = mapper.readTree(response.body());
        int totalItems = json.get("totalItems").asInt();
        System.out.println("✅ Test 2 PASSED - Found " + totalItems + " menu translations");
    }

    // ==========================================================================
    // Test 3: Create Translation
    // ==========================================================================

    @Test
    @Order(3)
    @DisplayName("Test 3: Should create new translation")
    void testCreateTranslation() throws Exception {
        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("category", "test");
        createRequest.put("messageKey", "test.http.integration." + System.currentTimeMillis());
        createRequest.put("messageUz", "HTTP Test");
        createRequest.put("messageOz", "ХТТП Тест");
        createRequest.put("messageRu", "ХТТП Тест");
        createRequest.put("messageEn", "HTTP Test");
        createRequest.put("active", true);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_URL))
            .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(createRequest)))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + jwtToken)
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Test 3 - Create Translation: " + response.statusCode());
        Assertions.assertEquals(200, response.statusCode(), "Should create successfully");

        JsonNode json = mapper.readTree(response.body());
        testTranslationId = json.get("id").asText();
        Assertions.assertNotNull(testTranslationId, "Should return translation ID");
        Assertions.assertEquals("test", json.get("category").asText());

        System.out.println("✅ Test 3 PASSED - Created translation with ID: " + testTranslationId);
    }

    // ==========================================================================
    // Test 4: Get Translation By ID
    // ==========================================================================

    @Test
    @Order(4)
    @DisplayName("Test 4: Should get translation by ID")
    void testGetTranslationById() throws Exception {
        Assumptions.assumeTrue(testTranslationId != null, "Test 3 must pass first");

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_URL + "/" + testTranslationId))
            .GET()
            .header("Authorization", "Bearer " + jwtToken)
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Test 4 - Get by ID: " + response.statusCode());
        Assertions.assertEquals(200, response.statusCode());

        JsonNode json = mapper.readTree(response.body());
        Assertions.assertEquals(testTranslationId, json.get("id").asText());

        System.out.println("✅ Test 4 PASSED - Retrieved translation successfully");
    }

    // ==========================================================================
    // Test 5: Update Translation
    // ==========================================================================

    @Test
    @Order(5)
    @DisplayName("Test 5: Should update existing translation")
    void testUpdateTranslation() throws Exception {
        Assumptions.assumeTrue(testTranslationId != null, "Test 3 must pass first");

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("category", "test");
        updateRequest.put("messageKey", "test.http.updated");
        updateRequest.put("messageUz", "HTTP Test UPDATED");
        updateRequest.put("active", true);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_URL + "/" + testTranslationId))
            .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(updateRequest)))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + jwtToken)
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Test 5 - Update Translation: " + response.statusCode());
        Assertions.assertEquals(200, response.statusCode());

        JsonNode json = mapper.readTree(response.body());
        Assertions.assertEquals("HTTP Test UPDATED", json.get("message").asText());

        System.out.println("✅ Test 5 PASSED - Updated translation successfully");
    }

    // ==========================================================================
    // Test 6: Get Statistics
    // ==========================================================================

    @Test
    @Order(6)
    @DisplayName("Test 6: Should get translation statistics")
    void testGetStatistics() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_URL + "/statistics"))
            .GET()
            .header("Authorization", "Bearer " + jwtToken)
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Test 6 - Get Statistics: " + response.statusCode());
        Assertions.assertEquals(200, response.statusCode());

        JsonNode json = mapper.readTree(response.body());
        Assertions.assertTrue(json.has("totalMessages"));
        Assertions.assertTrue(json.has("activeMessages"));
        Assertions.assertTrue(json.has("categoryBreakdown"));

        System.out.println("✅ Test 6 PASSED - Statistics:");
        System.out.println("   Total Messages: " + json.get("totalMessages").asInt());
        System.out.println("   Active Messages: " + json.get("activeMessages").asInt());
    }

    // ==========================================================================
    // Test 7: Clear Cache
    // ==========================================================================

    @Test
    @Order(7)
    @DisplayName("Test 7: Should clear translation cache")
    void testClearCache() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_URL + "/cache/clear"))
            .POST(HttpRequest.BodyPublishers.noBody())
            .header("Authorization", "Bearer " + jwtToken)
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Test 7 - Clear Cache: " + response.statusCode());
        Assertions.assertEquals(200, response.statusCode());

        System.out.println("✅ Test 7 PASSED - Cache cleared successfully");
    }

    // ==========================================================================
    // Test 8: Export to Properties
    // ==========================================================================

    @Test
    @Order(8)
    @DisplayName("Test 8: Should export translations to properties format")
    void testExportToProperties() throws Exception {
        Map<String, String> exportRequest = new HashMap<>();
        exportRequest.put("language", "uz-UZ");

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_URL + "/export"))
            .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(exportRequest)))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + jwtToken)
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Test 8 - Export Properties: " + response.statusCode());
        Assertions.assertEquals(200, response.statusCode());

        JsonNode json = mapper.readTree(response.body());
        Assertions.assertTrue(json.size() > 0, "Should have properties");

        System.out.println("✅ Test 8 PASSED - Exported " + json.size() + " properties");
    }

    // ==========================================================================
    // Test 9: Toggle Active Status
    // ==========================================================================

    @Test
    @Order(9)
    @DisplayName("Test 9: Should toggle translation active status")
    void testToggleActive() throws Exception {
        Assumptions.assumeTrue(testTranslationId != null, "Test 3 must pass first");

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_URL + "/" + testTranslationId + "/toggle-active"))
            .POST(HttpRequest.BodyPublishers.noBody())
            .header("Authorization", "Bearer " + jwtToken)
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Test 9 - Toggle Active: " + response.statusCode());
        Assertions.assertEquals(200, response.statusCode());

        JsonNode json = mapper.readTree(response.body());
        boolean isActive = json.get("isActive").asBoolean();
        System.out.println("✅ Test 9 PASSED - Active status toggled to: " + isActive);
    }

    // ==========================================================================
    // Test 10: Delete Translation
    // ==========================================================================

    @Test
    @Order(10)
    @DisplayName("Test 10: Should delete translation (soft delete)")
    void testDeleteTranslation() throws Exception {
        Assumptions.assumeTrue(testTranslationId != null, "Test 3 must pass first");

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_URL + "/" + testTranslationId))
            .DELETE()
            .header("Authorization", "Bearer " + jwtToken)
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Test 10 - Delete Translation: " + response.statusCode());
        Assertions.assertEquals(200, response.statusCode());

        System.out.println("✅ Test 10 PASSED - Translation deleted successfully");
    }

    // ==========================================================================
    // Cleanup
    // ==========================================================================

    @AfterAll
    static void cleanup() {
        System.out.println("\n==========================================================================");
        System.out.println("All Translation Admin Controller HTTP Tests Completed!");
        System.out.println("==========================================================================\n");
    }
}
