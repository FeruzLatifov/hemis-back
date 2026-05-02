package uz.hemis.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.app.HemisApplication;
import uz.hemis.domain.entity.system.SystemMessage;
import uz.hemis.domain.repository.SystemMessageRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests for TranslationAdminController
 *
 * <p><strong>Test Coverage:</strong></p>
 * <ul>
 *   <li>List translations with pagination and filtering</li>
 *   <li>Get single translation by ID</li>
 *   <li>Create new translation</li>
 *   <li>Update existing translation</li>
 *   <li>Delete translation (soft delete)</li>
 *   <li>Toggle active status</li>
 *   <li>Get statistics</li>
 *   <li>Clear cache</li>
 *   <li>Export to properties</li>
 *   <li>Regenerate properties files</li>
 * </ul>
 */
@EnabledIfEnvironmentVariable(named = "TESTS_ENABLED", matches = "(?i)true")
@SpringBootTest(
    classes = HemisApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.datasource.master.jdbc-url=jdbc:postgresql://${DB_MASTER_HOST}:${DB_MASTER_PORT}/${DB_MASTER_NAME}",
        "spring.datasource.replica.jdbc-url=jdbc:postgresql://${DB_REPLICA_HOST:${DB_MASTER_HOST}}:${DB_REPLICA_PORT:${DB_MASTER_PORT}}/${DB_REPLICA_NAME:${DB_MASTER_NAME}}"
    }
)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("Tests written against a different API surface. Real controller is at "
        + "/api/v1/web/system/translation and exposes only GET/PUT/PATCH — "
        + "no POST create or DELETE. Rewrite needed in a separate ticket.")
class TranslationAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SystemMessageRepository systemMessageRepository;

    private static UUID testTranslationId;
    private static final String BASE_URL = "/api/v1/admin/translations";

    private static RequestPostProcessor adminAuth() {
        return jwt().authorities(
                new SimpleGrantedAuthority("system.translation.view"),
                new SimpleGrantedAuthority("system.translation.edit")
        );
    }

    @Test
    @Order(1)
    @DisplayName("Test 1: Should list all translations with pagination")
    void testListTranslations() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .with(adminAuth())
                .param("page", "0")
                .param("size", "20")
                .param("sortBy", "category")
                .param("sortDir", "ASC"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.currentPage").value(0))
            .andExpect(jsonPath("$.totalItems").exists())
            .andExpect(jsonPath("$.totalPages").exists())
            .andExpect(jsonPath("$.pageSize").value(20));
    }

    @Test
    @Order(2)
    @DisplayName("Test 2: Should filter translations by category")
    void testFilterByCategory() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .with(adminAuth())
                .param("category", "menu")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[*].category", everyItem(is("menu"))));
    }

    @Test
    @Order(3)
    @DisplayName("Test 3: Should search translations by key or message")
    void testSearchTranslations() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .with(adminAuth())
                .param("search", "dashboard")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @Order(4)
    @DisplayName("Test 4: Should filter by active status")
    void testFilterByActive() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .with(adminAuth())
                .param("active", "true")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[*].isActive", everyItem(is(true))));
    }

    @Test
    @Order(5)
    @Transactional
    @DisplayName("Test 5: Should create new translation")
    void testCreateTranslation() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("category", "test");
        request.put("messageKey", "test.integration.key");
        request.put("messageUz", "Test matn");
        request.put("messageOz", "Тест матн");
        request.put("messageRu", "Тестовый текст");
        request.put("messageEn", "Test text");
        request.put("active", true);

        String response = mockMvc.perform(post(BASE_URL)
                .with(adminAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.messageKey").value("test.integration.key"))
            .andExpect(jsonPath("$.category").value("test"))
            .andExpect(jsonPath("$.message").value("Test matn"))
            .andExpect(jsonPath("$.isActive").value(true))
            .andReturn().getResponse().getContentAsString();

        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        testTranslationId = UUID.fromString((String) responseMap.get("id"));
    }

    @Test
    @Order(6)
    @Transactional
    @DisplayName("Test 6: Should fail to create duplicate translation key")
    void testCreateDuplicateTranslation() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("category", "test");
        request.put("messageKey", "test.duplicate.key");
        request.put("messageUz", "Test");
        request.put("active", true);

        mockMvc.perform(post(BASE_URL)
                .with(adminAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        mockMvc.perform(post(BASE_URL)
                .with(adminAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().is4xxClientError());
    }

    @Test
    @Order(7)
    @DisplayName("Test 7: Should get translation by ID")
    void testGetTranslationById() throws Exception {
        if (testTranslationId == null) {
            testTranslationId = systemMessageRepository.findAll().get(0).getId();
        }

        mockMvc.perform(get(BASE_URL + "/" + testTranslationId).with(adminAuth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(testTranslationId.toString()))
            .andExpect(jsonPath("$.messageKey").exists())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Order(8)
    @DisplayName("Test 8: Should return 404 for non-existent translation")
    void testGetNonExistentTranslation() throws Exception {
        UUID randomId = UUID.randomUUID();
        mockMvc.perform(get(BASE_URL + "/" + randomId).with(adminAuth()))
            .andExpect(status().isNotFound());
    }

    @Test
    @Order(9)
    @Transactional
    @DisplayName("Test 9: Should update existing translation")
    void testUpdateTranslation() throws Exception {
        if (testTranslationId == null) {
            testTranslationId = systemMessageRepository.findAll().get(0).getId();
        }

        Map<String, Object> request = new HashMap<>();
        request.put("category", "test");
        request.put("messageKey", "test.integration.key.updated");
        request.put("messageUz", "Updated test matn");
        request.put("messageOz", "Янгиланган тест матн");
        request.put("messageRu", "Обновленный тестовый текст");
        request.put("messageEn", "Updated test text");
        request.put("active", true);

        mockMvc.perform(put(BASE_URL + "/" + testTranslationId)
                .with(adminAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(testTranslationId.toString()))
            .andExpect(jsonPath("$.message").value("Updated test matn"));
    }

    @Test
    @Order(10)
    @Transactional
    @DisplayName("Test 10: Should toggle translation active status")
    void testToggleActive() throws Exception {
        if (testTranslationId == null) {
            testTranslationId = systemMessageRepository.findAll().get(0).getId();
        }

        SystemMessage before = systemMessageRepository.findById(testTranslationId).orElseThrow();
        boolean initialStatus = before.getIsActive();

        mockMvc.perform(post(BASE_URL + "/" + testTranslationId + "/toggle-active").with(adminAuth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isActive").value(!initialStatus));

        SystemMessage after = systemMessageRepository.findById(testTranslationId).orElseThrow();
        Assertions.assertEquals(!initialStatus, after.getIsActive());
    }

    @Test
    @Order(11)
    @DisplayName("Test 11: Should get translation statistics")
    void testGetStatistics() throws Exception {
        mockMvc.perform(get(BASE_URL + "/statistics").with(adminAuth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalMessages").isNumber())
            .andExpect(jsonPath("$.activeMessages").isNumber())
            .andExpect(jsonPath("$.inactiveMessages").isNumber())
            .andExpect(jsonPath("$.totalTranslations").isNumber())
            .andExpect(jsonPath("$.categoryBreakdown").isMap())
            .andExpect(jsonPath("$.languages").isArray())
            .andExpect(jsonPath("$.languages", hasSize(4)));
    }

    @Test
    @Order(12)
    @DisplayName("Test 12: Should clear translation cache")
    void testClearCache() throws Exception {
        mockMvc.perform(post(BASE_URL + "/cache/clear").with(adminAuth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Translation cache cleared successfully"));
    }

    @Test
    @Order(13)
    @DisplayName("Test 13: Should export translations to properties format")
    void testExportToProperties() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("language", "uz-UZ");

        mockMvc.perform(post(BASE_URL + "/export")
                .with(adminAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isMap());
    }

    @Test
    @Order(14)
    @DisplayName("Test 14: Should export translations for all languages")
    void testExportAllLanguages() throws Exception {
        String[] languages = {"uz-UZ", "oz-UZ", "ru-RU", "en-US"};

        for (String language : languages) {
            Map<String, String> request = new HashMap<>();
            request.put("language", language);

            mockMvc.perform(post(BASE_URL + "/export")
                    .with(adminAuth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap());
        }
    }

    @Test
    @Order(15)
    @DisplayName("Test 15: Should regenerate properties files")
    void testRegeneratePropertiesFiles() throws Exception {
        mockMvc.perform(post(BASE_URL + "/properties/regenerate").with(adminAuth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.generatedFiles").isArray())
            .andExpect(jsonPath("$.generatedFiles", hasSize(4)))
            .andExpect(jsonPath("$.totalFiles").value(4))
            .andExpect(jsonPath("$.totalTranslations").isNumber())
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @Order(16)
    @Transactional
    @DisplayName("Test 16: Should soft delete translation")
    void testDeleteTranslation() throws Exception {
        if (testTranslationId == null) {
            Map<String, Object> request = new HashMap<>();
            request.put("category", "test");
            request.put("messageKey", "test.to.delete");
            request.put("messageUz", "To be deleted");
            request.put("active", true);

            String response = mockMvc.perform(post(BASE_URL)
                    .with(adminAuth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            testTranslationId = UUID.fromString((String) responseMap.get("id"));
        }

        mockMvc.perform(delete(BASE_URL + "/" + testTranslationId).with(adminAuth()))
            .andExpect(status().isOk());

        SystemMessage deleted = systemMessageRepository.findById(testTranslationId).orElse(null);
        Assertions.assertNotNull(deleted);
        Assertions.assertNotNull(deleted.getDeletedAt());
    }

    @Test
    @Order(17)
    @DisplayName("Test 17: Should return 404 when deleting non-existent translation")
    void testDeleteNonExistentTranslation() throws Exception {
        UUID randomId = UUID.randomUUID();
        mockMvc.perform(delete(BASE_URL + "/" + randomId).with(adminAuth()))
            .andExpect(status().isNotFound());
    }

    @Test
    @Order(18)
    @DisplayName("Test 18: Should deny access without proper permissions")
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get(BASE_URL).with(jwt().authorities(new SimpleGrantedAuthority("basic.view"))))
            .andExpect(status().isForbidden());
    }

    @Test
    @Order(19)
    @DisplayName("Test 19: Should validate required fields on create")
    void testCreateWithMissingFields() throws Exception {
        Map<String, Object> request = new HashMap<>();

        mockMvc.perform(post(BASE_URL)
                .with(adminAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().is4xxClientError());
    }

    @Test
    @Order(20)
    @DisplayName("Test 20: Should handle large page size")
    void testLargePageSize() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .with(adminAuth())
                .param("page", "0")
                .param("size", "1000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @Order(21)
    @DisplayName("Test 21: Should handle page beyond total pages")
    void testPageBeyondTotal() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .with(adminAuth())
                .param("page", "9999")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isEmpty());
    }
}
