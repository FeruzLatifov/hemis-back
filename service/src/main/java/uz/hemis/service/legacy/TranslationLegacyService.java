package uz.hemis.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uz.hemis.common.dto.system.TranslationFilterRequest;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for loading translations from the hemishe_e_translation table.
 *
 * Extracted from TranslationServiceController to move JdbcTemplate usage
 * out of the controller layer.
 *
 * @since 1.5.4
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TranslationLegacyService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Load translations with optional filter.
     *
     * @param request optional filter request (category filter)
     * @return list of translation maps in OLD-HEMIS format
     */
    public List<Map<String, Object>> loadTranslations(TranslationFilterRequest request) {
        try {
            // Check if table exists
            Boolean exists = jdbcTemplate.queryForObject(
                    "SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'hemishe_e_translation')",
                    Boolean.class);

            if (!Boolean.TRUE.equals(exists)) {
                log.warn("hemishe_e_translation jadvali topilmadi");
                return Collections.emptyList();
            }

            // Check available columns
            List<String> columns = jdbcTemplate.queryForList(
                    "SELECT column_name FROM information_schema.columns WHERE table_name = 'hemishe_e_translation'",
                    String.class);

            boolean hasMessage = columns.contains("message");
            boolean hasUzUz = columns.contains("uz_uz");
            boolean hasRuRu = columns.contains("ru_ru");
            boolean hasOzUz = columns.contains("oz_uz");
            boolean hasEnUs = columns.contains("en_us");
            boolean hasKkUz = columns.contains("kk_uz");
            boolean hasCategory = columns.contains("category");
            boolean hasVersion = columns.contains("version");
            boolean hasDeleteTs = columns.contains("delete_ts");

            // Build dynamic SELECT
            StringBuilder selectColumns = new StringBuilder("id");
            if (hasMessage) selectColumns.append(", message");
            if (hasUzUz) selectColumns.append(", uz_uz");
            if (hasRuRu) selectColumns.append(", ru_ru");
            if (hasOzUz) selectColumns.append(", oz_uz");
            if (hasEnUs) selectColumns.append(", en_us");
            if (hasKkUz) selectColumns.append(", kk_uz");
            if (hasCategory) selectColumns.append(", category");
            if (hasVersion) selectColumns.append(", version");

            // WHERE clause - parameterized query
            StringBuilder whereClause = new StringBuilder();
            List<Object> params = new ArrayList<>();

            if (hasDeleteTs) {
                whereClause.append(" WHERE delete_ts IS NULL");
            }

            if (request != null && request.getCategory() != null && !request.getCategory().isEmpty() && hasCategory) {
                whereClause.append(whereClause.isEmpty() ? " WHERE " : " AND ");
                whereClause.append("category = ?");
                params.add(request.getCategory());
            }

            String sql = "SELECT " + selectColumns + " FROM hemishe_e_translation" + whereClause;

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params.toArray());

            return rows.stream()
                    .map(row -> {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("_entityName", "hemishe_ETranslation");
                        item.put("id", row.get("id") != null ? row.get("id").toString() : "");

                        if (hasMessage) item.put("message", str(row.get("message")));
                        if (hasUzUz) item.put("uz_Uz", str(row.get("uz_uz")));
                        if (hasRuRu) item.put("ru_Ru", str(row.get("ru_ru")));
                        if (hasOzUz) item.put("oz_Uz", str(row.get("oz_uz")));
                        if (hasEnUs) item.put("en_Us", str(row.get("en_us")));
                        if (hasKkUz) item.put("kk_Uz", str(row.get("kk_uz")));
                        if (hasCategory) item.put("category", str(row.get("category")));
                        if (hasVersion) item.put("version", row.get("version") != null ? ((Number) row.get("version")).intValue() : 1);

                        return item;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Tarjimalarni yuklashda xatolik: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private String str(Object obj) {
        return obj != null ? obj.toString() : "";
    }
}
