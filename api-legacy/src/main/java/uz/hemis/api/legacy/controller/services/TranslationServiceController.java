package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.TranslationFilterRequest;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Translation/Transcript Service Controller
 *
 * <p><strong>URL Pattern:</strong> {@code /app/rest/v2/services/translate/*} and {@code /app/rest/v2/services/transcript/*}</p>
 *
 * <p><strong>Legacy Compatibility:</strong></p>
 * <ul>
 *   <li>Base URL: /app/rest/v2/services/translate</li>
 *   <li>Matches OLD-HEMIS CUBA service pattern</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Tag(name = "14.Tarjima", description = "Tizim tarjimalari - UI va xabarlar tarjimasi")
@RestController
@RequestMapping("/app/rest/v2/services")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class TranslationServiceController {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Barcha tarjimalarni olish
     *
     * <p><strong>Legacy Endpoint:</strong> GET /app/rest/v2/services/translate/get</p>
     *
     * <p>OLD-HEMIS formatida barcha tarjimalarni qaytaradi:</p>
     * <pre>
     * {
     *   "success": true,
     *   "translations": [
     *     {
     *       "_entityName": "hemishe_ETranslation",
     *       "id": "uuid",
     *       "message": "English text",
     *       "uz_Uz": "O'zbekcha",
     *       "ru_Ru": "Русский",
     *       "oz_Uz": "Ўзбекча",
     *       "en_Us": "",
     *       "kk_Uz": "",
     *       "category": "app",
     *       "version": 1
     *     }
     *   ]
     * }
     * </pre>
     *
     * @return barcha tarjimalar OLD-HEMIS formatida
     */
    @GetMapping("/translate/get")
    @Operation(
        summary = "Barcha tarjimalar",
        description = "Tizimda mavjud barcha tarjimalarni OLD-HEMIS formatida qaytaradi"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli",
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"success\":true,\"translations\":[{\"_entityName\":\"hemishe_ETranslation\",\"id\":\"uuid\",\"message\":\"...\",\"uz_Uz\":\"...\"}]}")))
    })
    public ResponseEntity<Map<String, Object>> getAllTranslations() {
        log.info("GET /services/translate/get - barcha tarjimalar");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);

        List<Map<String, Object>> translations = loadAllTranslations();
        response.put("translations", translations);

        log.info("Jami {} ta tarjima topildi", translations.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Tarjimalarni filtrlab olish
     *
     * <p><strong>Legacy Endpoint:</strong> POST /app/rest/v2/services/translate/get</p>
     */
    @PostMapping("/translate/get")
    @Operation(
        summary = "Tarjimalarni filtrlab olish",
        description = "Filter parametrlari bilan tarjimalarni olish"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli")
    })
    public ResponseEntity<Map<String, Object>> getTranslationsFiltered(
        @RequestBody(description = "Filter parametrlari")
        @org.springframework.web.bind.annotation.RequestBody(required = false) TranslationFilterRequest request
    ) {
        log.info("POST /services/translate/get - filter: {}", request);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);

        List<Map<String, Object>> translations = loadTranslationsFiltered(request);
        response.put("translations", translations);

        log.info("Filterdan keyin {} ta tarjima topildi", translations.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Barcha tarjimalarni OLD-HEMIS jadvalidan yuklash
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> loadAllTranslations() {
        try {
            // Avval jadval mavjudligini tekshirish
            String checkSql = "SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'hemishe_e_translation')";
            Boolean exists = (Boolean) entityManager.createNativeQuery(checkSql).getSingleResult();

            if (!exists) {
                log.warn("hemishe_e_translation jadvali topilmadi");
                return Collections.emptyList();
            }

            // Mavjud ustunlarni tekshirish
            String columnsSql = "SELECT column_name FROM information_schema.columns WHERE table_name = 'hemishe_e_translation'";
            List<String> columns = entityManager.createNativeQuery(columnsSql).getResultList();

            boolean hasMessage = columns.contains("message");
            boolean hasUzUz = columns.contains("uz_uz");
            boolean hasRuRu = columns.contains("ru_ru");
            boolean hasOzUz = columns.contains("oz_uz");
            boolean hasEnUs = columns.contains("en_us");
            boolean hasKkUz = columns.contains("kk_uz");
            boolean hasCategory = columns.contains("category");
            boolean hasVersion = columns.contains("version");
            boolean hasDeleteTs = columns.contains("delete_ts");

            // Dinamik SQL yaratish
            StringBuilder selectColumns = new StringBuilder("id");
            if (hasMessage) selectColumns.append(", message");
            if (hasUzUz) selectColumns.append(", uz_uz");
            if (hasRuRu) selectColumns.append(", ru_ru");
            if (hasOzUz) selectColumns.append(", oz_uz");
            if (hasEnUs) selectColumns.append(", en_us");
            if (hasKkUz) selectColumns.append(", kk_uz");
            if (hasCategory) selectColumns.append(", category");
            if (hasVersion) selectColumns.append(", version");

            String whereClause = hasDeleteTs ? " WHERE delete_ts IS NULL" : "";
            String sql = "SELECT " + selectColumns + " FROM hemishe_e_translation" + whereClause;

            List<Object[]> results = entityManager.createNativeQuery(sql).getResultList();

            return results.stream()
                    .map(row -> {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("_entityName", "hemishe_ETranslation");

                        int idx = 0;
                        item.put("id", row[idx++] != null ? row[idx - 1].toString() : "");

                        if (hasMessage) item.put("message", row[idx++] != null ? row[idx - 1].toString() : "");
                        if (hasUzUz) item.put("uz_Uz", row[idx++] != null ? row[idx - 1].toString() : "");
                        if (hasRuRu) item.put("ru_Ru", row[idx++] != null ? row[idx - 1].toString() : "");
                        if (hasOzUz) item.put("oz_Uz", row[idx++] != null ? row[idx - 1].toString() : "");
                        if (hasEnUs) item.put("en_Us", row[idx++] != null ? row[idx - 1].toString() : "");
                        if (hasKkUz) item.put("kk_Uz", row[idx++] != null ? row[idx - 1].toString() : "");
                        if (hasCategory) item.put("category", row[idx++] != null ? row[idx - 1].toString() : "");
                        if (hasVersion) item.put("version", row[idx] != null ? ((Number) row[idx]).intValue() : 1);

                        return item;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Tarjimalarni yuklashda xatolik: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Tarjimalarni filter bilan yuklash
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> loadTranslationsFiltered(TranslationFilterRequest request) {
        if (request == null) {
            return loadAllTranslations();
        }

        try {
            // Avval jadval mavjudligini tekshirish
            String checkSql = "SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'hemishe_e_translation')";
            Boolean exists = (Boolean) entityManager.createNativeQuery(checkSql).getSingleResult();

            if (!exists) {
                log.warn("hemishe_e_translation jadvali topilmadi");
                return Collections.emptyList();
            }

            // Mavjud ustunlarni tekshirish
            String columnsSql = "SELECT column_name FROM information_schema.columns WHERE table_name = 'hemishe_e_translation'";
            List<String> columns = entityManager.createNativeQuery(columnsSql).getResultList();

            boolean hasMessage = columns.contains("message");
            boolean hasUzUz = columns.contains("uz_uz");
            boolean hasRuRu = columns.contains("ru_ru");
            boolean hasOzUz = columns.contains("oz_uz");
            boolean hasEnUs = columns.contains("en_us");
            boolean hasKkUz = columns.contains("kk_uz");
            boolean hasCategory = columns.contains("category");
            boolean hasVersion = columns.contains("version");
            boolean hasDeleteTs = columns.contains("delete_ts");

            // Dinamik SQL yaratish
            StringBuilder selectColumns = new StringBuilder("id");
            if (hasMessage) selectColumns.append(", message");
            if (hasUzUz) selectColumns.append(", uz_uz");
            if (hasRuRu) selectColumns.append(", ru_ru");
            if (hasOzUz) selectColumns.append(", oz_uz");
            if (hasEnUs) selectColumns.append(", en_us");
            if (hasKkUz) selectColumns.append(", kk_uz");
            if (hasCategory) selectColumns.append(", category");
            if (hasVersion) selectColumns.append(", version");

            StringBuilder whereClause = new StringBuilder();
            if (hasDeleteTs) {
                whereClause.append(" WHERE delete_ts IS NULL");
            }

            // Filter by category
            if (request.getCategory() != null && !request.getCategory().isEmpty() && hasCategory) {
                if (whereClause.length() == 0) {
                    whereClause.append(" WHERE ");
                } else {
                    whereClause.append(" AND ");
                }
                whereClause.append("category = '").append(request.getCategory().replace("'", "''")).append("'");
            }

            String sql = "SELECT " + selectColumns + " FROM hemishe_e_translation" + whereClause;

            List<Object[]> results = entityManager.createNativeQuery(sql).getResultList();

            return results.stream()
                    .map(row -> {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("_entityName", "hemishe_ETranslation");

                        int idx = 0;
                        item.put("id", row[idx++] != null ? row[idx - 1].toString() : "");

                        if (hasMessage) item.put("message", row[idx++] != null ? row[idx - 1].toString() : "");
                        if (hasUzUz) item.put("uz_Uz", row[idx++] != null ? row[idx - 1].toString() : "");
                        if (hasRuRu) item.put("ru_Ru", row[idx++] != null ? row[idx - 1].toString() : "");
                        if (hasOzUz) item.put("oz_Uz", row[idx++] != null ? row[idx - 1].toString() : "");
                        if (hasEnUs) item.put("en_Us", row[idx++] != null ? row[idx - 1].toString() : "");
                        if (hasKkUz) item.put("kk_Uz", row[idx++] != null ? row[idx - 1].toString() : "");
                        if (hasCategory) item.put("category", row[idx++] != null ? row[idx - 1].toString() : "");
                        if (hasVersion) item.put("version", row[idx] != null ? ((Number) row[idx]).intValue() : 1);

                        return item;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Tarjimalarni filter bilan yuklashda xatolik: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Transkript ariza
     */
    @Tag(name = "54.Transkript", description = "Transkript va o'quv natijalari")
    @GetMapping("/transcript/get")
    @Operation(
        summary = "Transkript ariza",
        description = "Talaba transkript ariza ma'lumotlarini olish"
    )
    public ResponseEntity<Map<String, Object>> getTranscript(
        @Parameter(description = "Ariza ID", required = true) @RequestParam UUID applicationId
    ) {
        log.info("GET /services/transcript/get - applicationId: {}", applicationId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("transcript", Map.of("id", applicationId));

        return ResponseEntity.ok(response);
    }
}
