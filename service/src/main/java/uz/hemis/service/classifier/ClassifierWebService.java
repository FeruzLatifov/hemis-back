package uz.hemis.service.classifier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.classifier.*;
import uz.hemis.common.exception.BadRequestException;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.service.classifier.ClassifierMetadataRegistry.Category;
import uz.hemis.service.classifier.ClassifierMetadataRegistry.ClassifierMeta;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Universal Classifier Web Service — JdbcTemplate-based CRUD for all classifier tables.
 *
 * <p>Barcha 90+ klasifikator jadvali uchun yagona xizmat.
 * {@link ClassifierMetadataRegistry} orqali whitelist tekshiruvi amalga oshiriladi.</p>
 *
 * <p>Mavjud {@code ClassifierLegacyService} dan {@code buildCountSql()}, {@code buildItemsSql()},
 * {@code columnExists()}, {@code tableExists()} pattern'lari qayta ishlatilgan.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClassifierWebService {

    private final JdbcTemplate jdbcTemplate;

    // ==================== READ Operations ====================

    /**
     * Get all categories with classifier counts.
     */
    @Transactional(readOnly = true)
    public List<ClassifierCategoryDto> getCategories() {
        return ClassifierMetadataRegistry.getAllCategories();
    }

    /**
     * Get classifiers by category with item counts from DB.
     */
    @Transactional(readOnly = true)
    public List<ClassifierMetadataDto> getClassifiersByCategory(String categoryKey) {
        Category category = ClassifierMetadataRegistry.resolveCategory(categoryKey);
        if (category == null) {
            return Collections.emptyList();
        }

        List<ClassifierMeta> metas = ClassifierMetadataRegistry.getByCategory(category);
        List<ClassifierMetadataDto> result = new ArrayList<>();

        for (ClassifierMeta meta : metas) {
            long itemCount = 0;
            if (tableExists(meta.getTableName())) {
                itemCount = countItems(meta.getTableName());
            }

            result.add(ClassifierMetadataDto.builder()
                    .apiKey(meta.getApiKey())
                    .tableName(meta.getTableName())
                    .titleUz(meta.getTitleUz())
                    .titleRu(meta.getTitleRu())
                    .titleEn(meta.getTitleEn())
                    .category(meta.getCategory().name())
                    .itemCount(itemCount)
                    .editable(meta.isEditable())
                    .hierarchical(meta.isHierarchical())
                    .build());
        }

        return result;
    }

    /**
     * Get classifier items with pagination and search.
     */
    @Transactional(readOnly = true)
    public Page<ClassifierItemDto> getClassifierItems(String apiKey, String search, Pageable pageable) {
        ClassifierMeta meta = resolveAndValidate(apiKey);
        String tableName = meta.getTableName();

        if (!tableExists(tableName)) {
            return Page.empty(pageable);
        }

        boolean hasName = columnExists(tableName, "name");
        boolean hasDeleteTs = columnExists(tableName, "delete_ts");
        boolean hasActive = columnExists(tableName, "active");
        boolean hasNameRu = columnExists(tableName, "name_ru");
        boolean hasNameEn = columnExists(tableName, "name_en");
        boolean hasParentCode = columnExists(tableName, "parent_code");
        boolean hasVersion = columnExists(tableName, "version");
        boolean hasCreateTs = columnExists(tableName, "create_ts");
        boolean hasUpdateTs = columnExists(tableName, "update_ts");

        // Count query
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM ").append(tableName);
        List<Object> params = new ArrayList<>();
        String whereClause = buildWhereClause(hasDeleteTs, search, hasName, hasNameRu, hasNameEn, params);
        countSql.append(whereClause);

        Long total = jdbcTemplate.queryForObject(countSql.toString(), Long.class, params.toArray());
        if (total == null || total == 0) {
            return Page.empty(pageable);
        }

        // Items query
        StringBuilder sql = new StringBuilder("SELECT code");
        if (hasName) sql.append(", name");
        if (hasNameRu) sql.append(", name_ru");
        if (hasNameEn) sql.append(", name_en");
        if (hasActive) sql.append(", active");
        if (hasVersion) sql.append(", version");
        if (hasParentCode) sql.append(", parent_code");
        if (hasCreateTs) sql.append(", create_ts");
        if (hasUpdateTs) sql.append(", update_ts");
        sql.append(" FROM ").append(tableName);

        List<Object> itemParams = new ArrayList<>();
        sql.append(buildWhereClause(hasDeleteTs, search, hasName, hasNameRu, hasNameEn, itemParams));
        sql.append(" ORDER BY code");
        sql.append(" LIMIT ? OFFSET ?");
        itemParams.add(pageable.getPageSize());
        itemParams.add(pageable.getOffset());

        List<ClassifierItemDto> items = jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            ClassifierItemDto.ClassifierItemDtoBuilder builder = ClassifierItemDto.builder()
                    .code(rs.getString("code"));
            if (hasName) builder.name(rs.getString("name"));
            if (hasNameRu) builder.nameRu(rs.getString("name_ru"));
            if (hasNameEn) builder.nameEn(rs.getString("name_en"));
            if (hasActive) {
                Object activeVal = rs.getObject("active");
                builder.active(activeVal != null ? rs.getBoolean("active") : null);
            }
            if (hasVersion) {
                Object versionVal = rs.getObject("version");
                builder.version(versionVal != null ? rs.getInt("version") : null);
            }
            if (hasParentCode) builder.parentCode(rs.getString("parent_code"));
            if (hasCreateTs) {
                java.sql.Timestamp ts = rs.getTimestamp("create_ts");
                builder.createTs(ts != null ? ts.toLocalDateTime() : null);
            }
            if (hasUpdateTs) {
                java.sql.Timestamp ts = rs.getTimestamp("update_ts");
                builder.updateTs(ts != null ? ts.toLocalDateTime() : null);
            }
            return builder.build();
        }, itemParams.toArray());

        return new PageImpl<>(items, pageable, total);
    }

    /**
     * Get single classifier item by code.
     */
    @Transactional(readOnly = true)
    public ClassifierItemDto getClassifierItem(String apiKey, String code) {
        ClassifierMeta meta = resolveAndValidate(apiKey);
        String tableName = meta.getTableName();

        if (!tableExists(tableName)) {
            return null;
        }

        boolean hasName = columnExists(tableName, "name");
        boolean hasDeleteTs = columnExists(tableName, "delete_ts");
        boolean hasActive = columnExists(tableName, "active");
        boolean hasNameRu = columnExists(tableName, "name_ru");
        boolean hasNameEn = columnExists(tableName, "name_en");
        boolean hasParentCode = columnExists(tableName, "parent_code");
        boolean hasVersion = columnExists(tableName, "version");
        boolean hasCreateTs = columnExists(tableName, "create_ts");
        boolean hasUpdateTs = columnExists(tableName, "update_ts");

        StringBuilder sql = new StringBuilder("SELECT code");
        if (hasName) sql.append(", name");
        if (hasNameRu) sql.append(", name_ru");
        if (hasNameEn) sql.append(", name_en");
        if (hasActive) sql.append(", active");
        if (hasVersion) sql.append(", version");
        if (hasParentCode) sql.append(", parent_code");
        if (hasCreateTs) sql.append(", create_ts");
        if (hasUpdateTs) sql.append(", update_ts");
        sql.append(" FROM ").append(tableName);
        sql.append(" WHERE code = ?");
        if (hasDeleteTs) sql.append(" AND delete_ts IS NULL");

        List<ClassifierItemDto> results = jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            ClassifierItemDto.ClassifierItemDtoBuilder builder = ClassifierItemDto.builder()
                    .code(rs.getString("code"));
            if (hasName) builder.name(rs.getString("name"));
            if (hasNameRu) builder.nameRu(rs.getString("name_ru"));
            if (hasNameEn) builder.nameEn(rs.getString("name_en"));
            if (hasActive) {
                Object activeVal = rs.getObject("active");
                builder.active(activeVal != null ? rs.getBoolean("active") : null);
            }
            if (hasVersion) {
                Object versionVal = rs.getObject("version");
                builder.version(versionVal != null ? rs.getInt("version") : null);
            }
            if (hasParentCode) builder.parentCode(rs.getString("parent_code"));
            if (hasCreateTs) {
                java.sql.Timestamp ts = rs.getTimestamp("create_ts");
                builder.createTs(ts != null ? ts.toLocalDateTime() : null);
            }
            if (hasUpdateTs) {
                java.sql.Timestamp ts = rs.getTimestamp("update_ts");
                builder.updateTs(ts != null ? ts.toLocalDateTime() : null);
            }
            return builder.build();
        }, code);

        return results.isEmpty() ? null : results.getFirst();
    }

    // ==================== WRITE Operations ====================

    /**
     * Create a new classifier item.
     */
    @Transactional
    public ClassifierItemDto createClassifierItem(String apiKey, ClassifierItemCreateDto dto) {
        ClassifierMeta meta = resolveAndValidate(apiKey);
        if (!meta.isEditable()) {
            throw new BadRequestException("Classifier '" + apiKey + "' faqat o'qish uchun (read-only)");
        }

        String tableName = meta.getTableName();
        if (!tableExists(tableName)) {
            throw new ResourceNotFoundException("Classifier", "table", tableName);
        }

        // Check for existing code (including soft-deleted)
        String checkSql = "SELECT COUNT(*) FROM " + tableName + " WHERE code = ?";
        Integer existingCount = jdbcTemplate.queryForObject(checkSql, Integer.class, dto.getCode());
        if (existingCount != null && existingCount > 0) {
            // Restore if soft-deleted, otherwise throw
            boolean hasDeleteTs = columnExists(tableName, "delete_ts");
            if (hasDeleteTs) {
                String checkDeletedSql = "SELECT COUNT(*) FROM " + tableName + " WHERE code = ? AND delete_ts IS NOT NULL";
                Integer deletedCount = jdbcTemplate.queryForObject(checkDeletedSql, Integer.class, dto.getCode());
                if (deletedCount != null && deletedCount > 0) {
                    // Restore soft-deleted item
                    return restoreItem(tableName, dto);
                }
            }
            throw new IllegalArgumentException("Bu kodli element allaqachon mavjud: " + dto.getCode());
        }

        // Build INSERT
        boolean hasName = columnExists(tableName, "name");
        boolean hasNameRu = columnExists(tableName, "name_ru");
        boolean hasNameEn = columnExists(tableName, "name_en");
        boolean hasActive = columnExists(tableName, "active");
        boolean hasVersion = columnExists(tableName, "version");
        boolean hasCreateTs = columnExists(tableName, "create_ts");
        boolean hasUpdateTs = columnExists(tableName, "update_ts");

        List<String> columns = new ArrayList<>(List.of("code"));
        List<Object> values = new ArrayList<>(List.of(dto.getCode()));
        if (hasName && dto.getName() != null) {
            columns.add("name");
            values.add(dto.getName());
        }

        if (hasNameRu && dto.getNameRu() != null) {
            columns.add("name_ru");
            values.add(dto.getNameRu());
        }
        if (hasNameEn && dto.getNameEn() != null) {
            columns.add("name_en");
            values.add(dto.getNameEn());
        }
        if (hasActive) {
            columns.add("active");
            values.add(dto.getActive() != null ? dto.getActive() : true);
        }
        if (hasVersion) {
            columns.add("version");
            values.add(1);
        }
        LocalDateTime now = LocalDateTime.now();
        if (hasCreateTs) {
            columns.add("create_ts");
            values.add(now);
        }
        if (hasUpdateTs) {
            columns.add("update_ts");
            values.add(now);
        }

        String placeholders = columns.stream().map(c -> "?").collect(Collectors.joining(", "));
        String insertSql = "INSERT INTO " + tableName + " (" + String.join(", ", columns) + ") VALUES (" + placeholders + ")";

        jdbcTemplate.update(insertSql, values.toArray());
        log.info("Classifier item created: {}.{}", apiKey, dto.getCode());

        return getClassifierItem(apiKey, dto.getCode());
    }

    /**
     * Update an existing classifier item.
     */
    @Transactional
    public ClassifierItemDto updateClassifierItem(String apiKey, String code, ClassifierItemUpdateDto dto) {
        ClassifierMeta meta = resolveAndValidate(apiKey);
        if (!meta.isEditable()) {
            throw new BadRequestException("Classifier '" + apiKey + "' faqat o'qish uchun (read-only)");
        }

        String tableName = meta.getTableName();
        if (!tableExists(tableName)) {
            throw new ResourceNotFoundException("Classifier", "table", tableName);
        }

        // Check item exists
        ClassifierItemDto existing = getClassifierItem(apiKey, code);
        if (existing == null) {
            return null;
        }

        boolean hasName = columnExists(tableName, "name");
        boolean hasNameRu = columnExists(tableName, "name_ru");
        boolean hasNameEn = columnExists(tableName, "name_en");
        boolean hasActive = columnExists(tableName, "active");
        boolean hasVersion = columnExists(tableName, "version");
        boolean hasUpdateTs = columnExists(tableName, "update_ts");

        List<String> setClauses = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (dto.getName() != null && hasName) {
            setClauses.add("name = ?");
            params.add(dto.getName());
        }
        if (dto.getNameRu() != null && hasNameRu) {
            setClauses.add("name_ru = ?");
            params.add(dto.getNameRu());
        }
        if (dto.getNameEn() != null && hasNameEn) {
            setClauses.add("name_en = ?");
            params.add(dto.getNameEn());
        }
        if (dto.getActive() != null && hasActive) {
            setClauses.add("active = ?");
            params.add(dto.getActive());
        }
        if (hasVersion) {
            setClauses.add("version = COALESCE(version, 0) + 1");
        }
        if (hasUpdateTs) {
            setClauses.add("update_ts = ?");
            params.add(LocalDateTime.now());
        }

        if (setClauses.isEmpty()) {
            return existing;
        }

        params.add(code);
        String updateSql = "UPDATE " + tableName + " SET " + String.join(", ", setClauses) + " WHERE code = ?";
        boolean hasDeleteTs = columnExists(tableName, "delete_ts");
        if (hasDeleteTs) {
            updateSql += " AND delete_ts IS NULL";
        }

        jdbcTemplate.update(updateSql, params.toArray());
        log.info("Classifier item updated: {}.{}", apiKey, code);

        return getClassifierItem(apiKey, code);
    }

    /**
     * Soft delete a classifier item.
     */
    @Transactional
    public void deleteClassifierItem(String apiKey, String code) {
        ClassifierMeta meta = resolveAndValidate(apiKey);
        if (!meta.isEditable()) {
            throw new BadRequestException("Classifier '" + apiKey + "' faqat o'qish uchun (read-only)");
        }

        String tableName = meta.getTableName();
        if (!tableExists(tableName)) {
            throw new ResourceNotFoundException("Classifier", "table", tableName);
        }

        boolean hasDeleteTs = columnExists(tableName, "delete_ts");
        if (hasDeleteTs) {
            // Soft delete
            String sql = "UPDATE " + tableName + " SET delete_ts = ? WHERE code = ? AND delete_ts IS NULL";
            int updated = jdbcTemplate.update(sql, LocalDateTime.now(), code);
            if (updated == 0) {
                throw new IllegalArgumentException("Element topilmadi yoki allaqachon o'chirilgan: " + code);
            }
        } else {
            // Hard delete (for tables without delete_ts)
            String sql = "DELETE FROM " + tableName + " WHERE code = ?";
            int deleted = jdbcTemplate.update(sql, code);
            if (deleted == 0) {
                throw new IllegalArgumentException("Element topilmadi: " + code);
            }
        }

        log.info("Classifier item deleted: {}.{}", apiKey, code);
    }

    // ==================== Helper Methods ====================

    private ClassifierMeta resolveAndValidate(String apiKey) {
        ClassifierMeta meta = ClassifierMetadataRegistry.getByApiKey(apiKey);
        if (meta == null) {
            throw new IllegalArgumentException("Noma'lum klasifikator: " + apiKey);
        }
        return meta;
    }

    private String buildWhereClause(boolean hasDeleteTs, String search,
                                    boolean hasName, boolean hasNameRu, boolean hasNameEn,
                                    List<Object> params) {
        List<String> conditions = new ArrayList<>();

        if (hasDeleteTs) {
            conditions.add("delete_ts IS NULL");
        }

        if (search != null && !search.isBlank()) {
            String searchPattern = "%" + search.trim().toLowerCase() + "%";
            StringBuilder searchCondition = new StringBuilder("(LOWER(code) LIKE ?");
            params.add(searchPattern);
            if (hasName) {
                searchCondition.append(" OR LOWER(name) LIKE ?");
                params.add(searchPattern);
            }
            if (hasNameRu) {
                searchCondition.append(" OR LOWER(name_ru) LIKE ?");
                params.add(searchPattern);
            }
            if (hasNameEn) {
                searchCondition.append(" OR LOWER(name_en) LIKE ?");
                params.add(searchPattern);
            }
            searchCondition.append(")");
            conditions.add(searchCondition.toString());
        }

        if (conditions.isEmpty()) {
            return "";
        }
        return " WHERE " + String.join(" AND ", conditions);
    }

    private ClassifierItemDto restoreItem(String tableName, ClassifierItemCreateDto dto) {
        List<String> setClauses = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (columnExists(tableName, "name") && dto.getName() != null) {
            setClauses.add("name = ?");
            params.add(dto.getName());
        }
        setClauses.add("delete_ts = NULL");

        if (columnExists(tableName, "name_ru") && dto.getNameRu() != null) {
            setClauses.add("name_ru = ?");
            params.add(dto.getNameRu());
        }
        if (columnExists(tableName, "name_en") && dto.getNameEn() != null) {
            setClauses.add("name_en = ?");
            params.add(dto.getNameEn());
        }
        if (columnExists(tableName, "active")) {
            setClauses.add("active = ?");
            params.add(dto.getActive() != null ? dto.getActive() : true);
        }
        if (columnExists(tableName, "update_ts")) {
            setClauses.add("update_ts = ?");
            params.add(LocalDateTime.now());
        }

        params.add(dto.getCode());
        String updateSql = "UPDATE " + tableName + " SET " + String.join(", ", setClauses) + " WHERE code = ?";
        jdbcTemplate.update(updateSql, params.toArray());

        String apiKey = ClassifierMetadataRegistry.tableNameToApiKey(tableName);
        log.info("Classifier item restored: {}.{}", apiKey, dto.getCode());
        return getClassifierItem(apiKey, dto.getCode());
    }

    private boolean tableExists(String tableName) {
        try {
            String sql = "SELECT EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = ?)";
            Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, tableName);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean columnExists(String tableName, String columnName) {
        try {
            String sql = "SELECT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = ? AND column_name = ?)";
            Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, tableName, columnName);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            return false;
        }
    }

    private long countItems(String tableName) {
        try {
            boolean hasDeleteTs = columnExists(tableName, "delete_ts");
            String sql = "SELECT COUNT(*) FROM " + tableName;
            if (hasDeleteTs) {
                sql += " WHERE delete_ts IS NULL";
            }
            Long count = jdbcTemplate.queryForObject(sql, Long.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.debug("Error counting {}: {}", tableName, e.getMessage());
            return 0;
        }
    }
}
