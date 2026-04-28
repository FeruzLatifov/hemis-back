package uz.hemis.service.classifier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.audit.AuditAction;
import uz.hemis.common.audit.Audited;
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
 * <p>Schema moslashuvchanligi (Bosqich 5 refactor):
 * <ul>
 *   <li>Eski CUBA jadvallar: {@code active, delete_ts, create_ts, update_ts, create_by, update_by}</li>
 *   <li>Yangi jadvallar (V009-V013): {@code is_active, (no delete_ts), created_at, updated_at, created_by, updated_by}</li>
 * </ul>
 * Aliasing orqali DTO va API format o'zgarmaydi.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClassifierWebService {

    private final JdbcTemplate jdbcTemplate;

    // ==================== READ Operations ====================

    @Transactional(readOnly = true)
    public List<ClassifierCategoryDto> getCategories() {
        return ClassifierMetadataRegistry.getAllCategories();
    }

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

    @Transactional(readOnly = true)
    public Page<ClassifierItemDto> getClassifierItems(String apiKey, String search, Pageable pageable) {
        ClassifierMeta meta = resolveAndValidate(apiKey);
        String tableName = meta.getTableName();

        if (!tableExists(tableName)) {
            return Page.empty(pageable);
        }

        SchemaInfo schema = detectSchema(tableName);

        // Count
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM ").append(tableName);
        List<Object> params = new ArrayList<>();
        String whereClause = buildWhereClause(schema, search, params);
        countSql.append(whereClause);

        Long total = jdbcTemplate.queryForObject(countSql.toString(), Long.class, params.toArray());
        if (total == null || total == 0) {
            return Page.empty(pageable);
        }

        // Items (with aliasing so DTO field names remain stable)
        StringBuilder sql = new StringBuilder("SELECT code");
        if (schema.hasName) sql.append(", name");
        if (schema.hasNameRu) sql.append(", name_ru");
        if (schema.hasNameEn) sql.append(", name_en");
        if (schema.activeCol != null) sql.append(", ").append(schema.activeCol).append(" as active");
        if (schema.hasVersion) sql.append(", version");
        if (schema.hasParentCode) sql.append(", parent_code");
        if (schema.createTsCol != null) sql.append(", ").append(schema.createTsCol).append(" as create_ts");
        if (schema.updateTsCol != null) sql.append(", ").append(schema.updateTsCol).append(" as update_ts");
        sql.append(" FROM ").append(tableName);

        List<Object> itemParams = new ArrayList<>();
        sql.append(buildWhereClause(schema, search, itemParams));
        sql.append(" ORDER BY code");
        sql.append(" LIMIT ? OFFSET ?");
        itemParams.add(pageable.getPageSize());
        itemParams.add(pageable.getOffset());

        List<ClassifierItemDto> items = jdbcTemplate.query(sql.toString(),
                (rs, rowNum) -> mapToDto(rs, schema), itemParams.toArray());

        return new PageImpl<>(items, pageable, total);
    }

    @Transactional(readOnly = true)
    public ClassifierItemDto getClassifierItem(String apiKey, String code) {
        ClassifierMeta meta = resolveAndValidate(apiKey);
        String tableName = meta.getTableName();

        if (!tableExists(tableName)) {
            return null;
        }

        SchemaInfo schema = detectSchema(tableName);

        StringBuilder sql = new StringBuilder("SELECT code");
        if (schema.hasName) sql.append(", name");
        if (schema.hasNameRu) sql.append(", name_ru");
        if (schema.hasNameEn) sql.append(", name_en");
        if (schema.activeCol != null) sql.append(", ").append(schema.activeCol).append(" as active");
        if (schema.hasVersion) sql.append(", version");
        if (schema.hasParentCode) sql.append(", parent_code");
        if (schema.createTsCol != null) sql.append(", ").append(schema.createTsCol).append(" as create_ts");
        if (schema.updateTsCol != null) sql.append(", ").append(schema.updateTsCol).append(" as update_ts");
        sql.append(" FROM ").append(tableName).append(" WHERE code = ?");
        if (schema.hasDeleteTs) sql.append(" AND delete_ts IS NULL");

        List<ClassifierItemDto> results = jdbcTemplate.query(sql.toString(),
                (rs, rowNum) -> mapToDto(rs, schema), code);

        return results.isEmpty() ? null : results.getFirst();
    }

    // ==================== WRITE Operations ====================

    @Transactional
    @Audited(action = AuditAction.CREATE, entity = "ClassifierItem", keyArg = "apiKey")
    public ClassifierItemDto createClassifierItem(String apiKey, ClassifierItemCreateDto dto) {
        ClassifierMeta meta = resolveAndValidate(apiKey);
        if (!meta.isEditable()) {
            throw new BadRequestException("Classifier '" + apiKey + "' faqat o'qish uchun (read-only)");
        }

        String tableName = meta.getTableName();
        if (!tableExists(tableName)) {
            throw new ResourceNotFoundException("Classifier", "table", tableName);
        }

        SchemaInfo schema = detectSchema(tableName);

        // Check existing
        String checkSql = "SELECT COUNT(*) FROM " + tableName + " WHERE code = ?";
        Integer existingCount = jdbcTemplate.queryForObject(checkSql, Integer.class, dto.getCode());
        if (existingCount != null && existingCount > 0) {
            if (schema.hasDeleteTs) {
                String checkDeletedSql = "SELECT COUNT(*) FROM " + tableName + " WHERE code = ? AND delete_ts IS NOT NULL";
                Integer deletedCount = jdbcTemplate.queryForObject(checkDeletedSql, Integer.class, dto.getCode());
                if (deletedCount != null && deletedCount > 0) {
                    return restoreItem(tableName, dto, schema);
                }
            }
            throw new IllegalArgumentException("Bu kodli element allaqachon mavjud: " + dto.getCode());
        }

        // Build INSERT
        List<String> columns = new ArrayList<>(List.of("code"));
        List<Object> values = new ArrayList<>(List.of(dto.getCode()));
        if (schema.hasName && dto.getName() != null) {
            columns.add("name");
            values.add(dto.getName());
        }
        if (schema.hasNameRu && dto.getNameRu() != null) {
            columns.add("name_ru");
            values.add(dto.getNameRu());
        }
        if (schema.hasNameEn && dto.getNameEn() != null) {
            columns.add("name_en");
            values.add(dto.getNameEn());
        }
        if (schema.activeCol != null) {
            columns.add(schema.activeCol);
            values.add(dto.getActive() != null ? dto.getActive() : true);
        }
        if (schema.hasVersion) {
            columns.add("version");
            values.add(1);
        }
        LocalDateTime now = LocalDateTime.now();
        String currentUser = getCurrentUsername();
        if (schema.createTsCol != null) {
            columns.add(schema.createTsCol);
            values.add(now);
        }
        if (schema.updateTsCol != null) {
            columns.add(schema.updateTsCol);
            values.add(now);
        }
        // Audit: JdbcTemplate JPA Auditing listener'ni chetlab o'tadi → qo'lda o'rnatamiz
        if (schema.createdByCol != null) {
            columns.add(schema.createdByCol);
            values.add(currentUser);
        }
        if (schema.updatedByCol != null) {
            columns.add(schema.updatedByCol);
            values.add(currentUser);
        }

        String placeholders = columns.stream().map(c -> "?").collect(Collectors.joining(", "));
        String insertSql = "INSERT INTO " + tableName + " (" + String.join(", ", columns) + ") VALUES (" + placeholders + ")";

        jdbcTemplate.update(insertSql, values.toArray());
        log.info("Classifier item created: {}.{} by {}", apiKey, dto.getCode(), currentUser);

        return getClassifierItem(apiKey, dto.getCode());
    }

    @Transactional
    @Audited(action = AuditAction.UPDATE, entity = "ClassifierItem", keyArg = "code")
    public ClassifierItemDto updateClassifierItem(String apiKey, String code, ClassifierItemUpdateDto dto) {
        ClassifierMeta meta = resolveAndValidate(apiKey);
        if (!meta.isEditable()) {
            throw new BadRequestException("Classifier '" + apiKey + "' faqat o'qish uchun (read-only)");
        }

        String tableName = meta.getTableName();
        if (!tableExists(tableName)) {
            throw new ResourceNotFoundException("Classifier", "table", tableName);
        }

        ClassifierItemDto existing = getClassifierItem(apiKey, code);
        if (existing == null) {
            return null;
        }

        SchemaInfo schema = detectSchema(tableName);

        List<String> setClauses = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (dto.getName() != null && schema.hasName) {
            setClauses.add("name = ?");
            params.add(dto.getName());
        }
        if (dto.getNameRu() != null && schema.hasNameRu) {
            setClauses.add("name_ru = ?");
            params.add(dto.getNameRu());
        }
        if (dto.getNameEn() != null && schema.hasNameEn) {
            setClauses.add("name_en = ?");
            params.add(dto.getNameEn());
        }
        if (dto.getActive() != null && schema.activeCol != null) {
            setClauses.add(schema.activeCol + " = ?");
            params.add(dto.getActive());
        }
        if (schema.hasVersion) {
            setClauses.add("version = COALESCE(version, 0) + 1");
        }
        if (schema.updateTsCol != null) {
            setClauses.add(schema.updateTsCol + " = ?");
            params.add(LocalDateTime.now());
        }
        if (schema.updatedByCol != null) {
            setClauses.add(schema.updatedByCol + " = ?");
            params.add(getCurrentUsername());
        }

        if (setClauses.isEmpty()) {
            return existing;
        }

        params.add(code);
        String updateSql = "UPDATE " + tableName + " SET " + String.join(", ", setClauses) + " WHERE code = ?";
        if (schema.hasDeleteTs) {
            updateSql += " AND delete_ts IS NULL";
        }

        jdbcTemplate.update(updateSql, params.toArray());
        log.info("Classifier item updated: {}.{}", apiKey, code);

        return getClassifierItem(apiKey, code);
    }

    /**
     * Delete classifier item.
     * Old CUBA tables → soft delete (set delete_ts).
     * New tables (no delete_ts) → set is_active = false (soft disable).
     */
    @Transactional
    @Audited(action = AuditAction.DELETE, entity = "ClassifierItem", keyArg = "code")
    public void deleteClassifierItem(String apiKey, String code) {
        ClassifierMeta meta = resolveAndValidate(apiKey);
        if (!meta.isEditable()) {
            throw new BadRequestException("Classifier '" + apiKey + "' faqat o'qish uchun (read-only)");
        }

        String tableName = meta.getTableName();
        if (!tableExists(tableName)) {
            throw new ResourceNotFoundException("Classifier", "table", tableName);
        }

        SchemaInfo schema = detectSchema(tableName);

        if (schema.hasDeleteTs) {
            String sql = "UPDATE " + tableName + " SET delete_ts = ? WHERE code = ? AND delete_ts IS NULL";
            int updated = jdbcTemplate.update(sql, LocalDateTime.now(), code);
            if (updated == 0) {
                throw new IllegalArgumentException("Element topilmadi yoki allaqachon o'chirilgan: " + code);
            }
        } else if (schema.activeCol != null) {
            // Yangi jadvallar: soft-disable (is_active = false)
            StringBuilder sql = new StringBuilder("UPDATE ").append(tableName)
                    .append(" SET ").append(schema.activeCol).append(" = false");
            if (schema.updateTsCol != null) {
                sql.append(", ").append(schema.updateTsCol).append(" = ?");
            }
            sql.append(" WHERE code = ?");
            int updated;
            if (schema.updateTsCol != null) {
                updated = jdbcTemplate.update(sql.toString(), LocalDateTime.now(), code);
            } else {
                updated = jdbcTemplate.update(sql.toString(), code);
            }
            if (updated == 0) {
                throw new IllegalArgumentException("Element topilmadi: " + code);
            }
        } else {
            // Hard delete (no audit columns)
            String sql = "DELETE FROM " + tableName + " WHERE code = ?";
            int deleted = jdbcTemplate.update(sql, code);
            if (deleted == 0) {
                throw new IllegalArgumentException("Element topilmadi: " + code);
            }
        }

        log.info("Classifier item deleted: {}.{}", apiKey, code);
    }

    // ==================== Helpers ====================

    private ClassifierMeta resolveAndValidate(String apiKey) {
        ClassifierMeta meta = ClassifierMetadataRegistry.getByApiKey(apiKey);
        if (meta == null) {
            throw new IllegalArgumentException("Noma'lum klasifikator: " + apiKey);
        }
        return meta;
    }

    private String buildWhereClause(SchemaInfo schema, String search, List<Object> params) {
        List<String> conditions = new ArrayList<>();

        if (schema.hasDeleteTs) {
            conditions.add("delete_ts IS NULL");
        }

        if (search != null && !search.isBlank()) {
            String searchPattern = "%" + search.trim().toLowerCase() + "%";
            StringBuilder searchCondition = new StringBuilder("(LOWER(code) LIKE ?");
            params.add(searchPattern);
            if (schema.hasName) {
                searchCondition.append(" OR LOWER(name) LIKE ?");
                params.add(searchPattern);
            }
            if (schema.hasNameRu) {
                searchCondition.append(" OR LOWER(name_ru) LIKE ?");
                params.add(searchPattern);
            }
            if (schema.hasNameEn) {
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

    private ClassifierItemDto mapToDto(java.sql.ResultSet rs, SchemaInfo schema) throws java.sql.SQLException {
        ClassifierItemDto.ClassifierItemDtoBuilder builder = ClassifierItemDto.builder()
                .code(rs.getString("code"));
        if (schema.hasName) builder.name(rs.getString("name"));
        if (schema.hasNameRu) builder.nameRu(rs.getString("name_ru"));
        if (schema.hasNameEn) builder.nameEn(rs.getString("name_en"));
        if (schema.activeCol != null) {
            Object activeVal = rs.getObject("active");
            builder.active(activeVal != null ? rs.getBoolean("active") : null);
        }
        if (schema.hasVersion) {
            Object versionVal = rs.getObject("version");
            builder.version(versionVal != null ? rs.getInt("version") : null);
        }
        if (schema.hasParentCode) builder.parentCode(rs.getString("parent_code"));
        if (schema.createTsCol != null) {
            java.sql.Timestamp ts = rs.getTimestamp("create_ts");
            builder.createTs(ts != null ? ts.toLocalDateTime() : null);
        }
        if (schema.updateTsCol != null) {
            java.sql.Timestamp ts = rs.getTimestamp("update_ts");
            builder.updateTs(ts != null ? ts.toLocalDateTime() : null);
        }
        return builder.build();
    }

    private ClassifierItemDto restoreItem(String tableName, ClassifierItemCreateDto dto, SchemaInfo schema) {
        List<String> setClauses = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (schema.hasName && dto.getName() != null) {
            setClauses.add("name = ?");
            params.add(dto.getName());
        }
        if (schema.hasDeleteTs) {
            setClauses.add("delete_ts = NULL");
        }
        if (schema.hasNameRu && dto.getNameRu() != null) {
            setClauses.add("name_ru = ?");
            params.add(dto.getNameRu());
        }
        if (schema.hasNameEn && dto.getNameEn() != null) {
            setClauses.add("name_en = ?");
            params.add(dto.getNameEn());
        }
        if (schema.activeCol != null) {
            setClauses.add(schema.activeCol + " = ?");
            params.add(dto.getActive() != null ? dto.getActive() : true);
        }
        if (schema.updateTsCol != null) {
            setClauses.add(schema.updateTsCol + " = ?");
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

    /**
     * Jadvalning schema formatini aniqlash — eski CUBA yoki yangi ReferenceEntity.
     * Aliasing orqali DTO maydonlari bir xil qoladi.
     */
    private SchemaInfo detectSchema(String tableName) {
        SchemaInfo info = new SchemaInfo();
        info.hasName = columnExists(tableName, "name");
        info.hasNameRu = columnExists(tableName, "name_ru");
        info.hasNameEn = columnExists(tableName, "name_en");
        info.hasVersion = columnExists(tableName, "version");
        info.hasParentCode = columnExists(tableName, "parent_code");
        info.hasDeleteTs = columnExists(tableName, "delete_ts");

        // active vs is_active
        if (columnExists(tableName, "active")) info.activeCol = "active";
        else if (columnExists(tableName, "is_active")) info.activeCol = "is_active";
        else info.activeCol = null;

        // create_ts vs created_at
        if (columnExists(tableName, "create_ts")) info.createTsCol = "create_ts";
        else if (columnExists(tableName, "created_at")) info.createTsCol = "created_at";
        else info.createTsCol = null;

        // update_ts vs updated_at
        if (columnExists(tableName, "update_ts")) info.updateTsCol = "update_ts";
        else if (columnExists(tableName, "updated_at")) info.updateTsCol = "updated_at";
        else info.updateTsCol = null;

        // created_by — eski CUBA 'create_by' ham bor (legacy edge-case), yangi 'created_by'
        if (columnExists(tableName, "created_by")) info.createdByCol = "created_by";
        else if (columnExists(tableName, "create_by")) info.createdByCol = "create_by";
        else info.createdByCol = null;

        // updated_by
        if (columnExists(tableName, "updated_by")) info.updatedByCol = "updated_by";
        else if (columnExists(tableName, "update_by")) info.updatedByCol = "update_by";
        else info.updatedByCol = null;

        return info;
    }

    /**
     * Joriy foydalanuvchi username'ni olish — SecurityContextHolder orqali.
     * Fallback: 'system:web' (anonim yoki non-HTTP thread).
     */
    private String getCurrentUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                return auth.getName();
            }
        } catch (Exception e) {
            log.debug("Auth context'dan username olishda xato: {}", e.getMessage());
        }
        return "system:web";
    }

    /** Schema flag'lar — eski CUBA va yangi ReferenceEntity pattern'lar uchun. */
    private static final class SchemaInfo {
        boolean hasName;
        boolean hasNameRu;
        boolean hasNameEn;
        boolean hasVersion;
        boolean hasParentCode;
        boolean hasDeleteTs;
        String activeCol;      // "active" | "is_active" | null
        String createTsCol;    // "create_ts" | "created_at" | null
        String updateTsCol;    // "update_ts" | "updated_at" | null
        String createdByCol;   // "created_by" | "create_by" | null
        String updatedByCol;   // "updated_by" | "update_by" | null
    }
}
