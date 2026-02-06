package uz.hemis.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for checking and restoring soft-deleted entities via native SQL.
 *
 * Used by CUBA-compatible entity controllers that need to bypass @SQLRestriction
 * when checking for soft-deleted records during upsert operations.
 *
 * @since 1.5.4
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SoftDeleteRestoreLegacyService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Check if a soft-deleted record exists in the given table with the given code.
     *
     * @param tableName the DB table name (must be alphanumeric + underscore)
     * @param code the entity code/id
     * @return true if a soft-deleted record exists
     */
    public boolean hasSoftDeletedRecord(String tableName, String code) {
        validateTableName(tableName);
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT COUNT(*) AS cnt FROM " + tableName + " WHERE code = ? AND delete_ts IS NOT NULL",
                code
            );
            long count = rows.isEmpty() ? 0L : ((Number) rows.get(0).get("cnt")).longValue();
            return count > 0;
        } catch (Exception e) {
            log.warn("Error checking soft-deleted record in {}: {}", tableName, e.getMessage());
            return false;
        }
    }

    /**
     * Restore a soft-deleted record by clearing delete_ts and deleted_by.
     *
     * @param tableName the DB table name (must be alphanumeric + underscore)
     * @param code the entity code/id
     */
    public void restoreSoftDeletedRecord(String tableName, String code) {
        validateTableName(tableName);
        jdbcTemplate.update(
            "UPDATE " + tableName + " SET delete_ts = NULL, deleted_by = NULL, update_ts = NOW() WHERE code = ?",
            code
        );
        log.info("Restored soft-deleted record in {} with code: {}", tableName, code);
    }

    private void validateTableName(String tableName) {
        if (tableName == null || !tableName.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name: " + tableName);
        }
    }
}
