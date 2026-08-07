package uz.hemis.service.student;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.auth.AccessScope;
import uz.hemis.common.auth.ScopeResolver;
import uz.hemis.common.dto.classifier.ClassifierItemDto;
import uz.hemis.common.dto.student.StudentDictionariesDto;
import uz.hemis.common.dto.student.StudentDictionariesDto.DictionaryItem;
import uz.hemis.common.dto.student.DuplicateGroupDetailDto;
import uz.hemis.common.dto.student.DuplicateGroupDto;
import uz.hemis.common.dto.student.DuplicateStatsDto;
import uz.hemis.common.dto.student.DuplicateStudentCard;
import uz.hemis.common.dto.student.StudentDto;
import uz.hemis.common.dto.student.StudentListDto;
import uz.hemis.common.dto.student.SpecialityStatsDto;
import uz.hemis.common.dto.student.SpecialitySummaryDto;
import uz.hemis.common.dto.student.StudentStatsDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.student.Student;
import uz.hemis.domain.repository.StudentRepository;
import uz.hemis.service.classifier.ClassifierWebService;
import uz.hemis.service.student.mapper.StudentMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Student Web Service - Frontend UI API
 *
 * <p><strong>Purpose:</strong> Separate service for hemis-front web UI.
 * Does NOT modify the existing {@link StudentService} (legacy endpoints stay intact).</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Universal multi-criteria search + filter (JPA Specification)</li>
 *   <li>Student statistics for dashboard cards</li>
 *   <li>Read-only operations (no CRUD)</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StudentWebService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final ClassifierWebService classifierWebService;
    private final CacheManager cacheManager;

    @Qualifier("dashboardJdbcTemplate")
    private final JdbcTemplate jdbcTemplate;

    /** Server-derived tenant data-scope (OTM confinement) — api-web only. */
    private final ScopeResolver scopeResolver;

    // =====================================================
    // Native SQL: 18 columns instead of 70+ (SELECT *)
    // =====================================================

    private static final String SELECT_COLUMNS = """
            t.id, t.code, t.firstname, t.lastname, t.fathername, t.pinfl,
            t."_university", t."_faculty", t."_speciality", t."_student_status",
            t."_payment_form", t."_education_type", t."_education_form",
            t."_course", t."_education_year", t."_gender", t.group_name, t.active
            """;

    private static final RowMapper<StudentListDto> STUDENT_LIST_ROW_MAPPER = (rs, rowNum) ->
            new StudentListDto(
                    rs.getObject("id", UUID.class),
                    rs.getString("code"),
                    rs.getString("firstname"),
                    rs.getString("lastname"),
                    rs.getString("fathername"),
                    rs.getString("pinfl"),
                    rs.getString("_university"),
                    rs.getString("_faculty"),
                    rs.getString("_speciality"),
                    rs.getString("_student_status"),
                    rs.getString("_payment_form"),
                    rs.getString("_education_type"),
                    rs.getString("_education_form"),
                    rs.getString("_course"),
                    rs.getString("_education_year"),
                    rs.getString("_gender"),
                    rs.getString("group_name"),
                    rs.getObject("active", Boolean.class)
            );

    /** Row from duplicate analysis (MV or live CTE). */
    record DuplicatePinflRow(String pinfl, int cnt, int univCount, int activeCount, int specCount, String reason) {}

    private static final RowMapper<DuplicatePinflRow> DUPLICATE_PINFL_ROW_MAPPER = (rs, rowNum) ->
            new DuplicatePinflRow(
                    rs.getString("pinfl"),
                    rs.getInt("cnt"),
                    rs.getInt("univ_count"),
                    rs.getInt("active_count"),
                    rs.getInt("spec_count"),
                    rs.getString("reason")
            );

    /**
     * Search students with multi-criteria filtering (Native SQL).
     *
     * <p><strong>Why native SQL instead of JPA Specification?</strong></p>
     * <ul>
     *   <li>SELECT only 18 columns vs 70+ (SELECT *) — ~4x less data</li>
     *   <li>ILIKE for case-insensitive search — works with pg_trgm GIN indexes</li>
     *   <li>Estimated COUNT for unfiltered queries — 1000x faster</li>
     *   <li>Returns lightweight {@link StudentListDto} instead of full {@link StudentDto}</li>
     * </ul>
     *
     * @param q              search query (name, code, PINFL)
     * @param university     university code filter
     * @param educationType  education type filter
     * @param paymentForm    payment form filter
     * @param studentStatus  student status filter
     * @param course         course filter
     * @param faculty        faculty code filter
     * @param educationForm  education form filter
     * @param educationYear  education year filter
     * @param gender         gender filter
     * @param pageable       pagination parameters
     * @return page of lightweight student DTOs
     */
    @Cacheable(
            value = "studentsListSearch",
            key = "#q + ':' + #searchField + ':' + #university + ':' + #educationType + ':' + #paymentForm + ':' + #studentStatus + ':' + #course + ':' + #faculty + ':' + #educationForm + ':' + #educationYear + ':' + #gender + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + '|' + @scopeResolver.currentScopeKey()",
            unless = "#result == null || #pageable.pageNumber > 10"
    )
    public Page<StudentListDto> searchStudents(
            String q,
            String searchField,
            String university,
            String educationType,
            String paymentForm,
            String studentStatus,
            String course,
            String faculty,
            String educationForm,
            String educationYear,
            String gender,
            Pageable pageable
    ) {
        log.debug("Searching students - q={}, searchField={}, university={}, page={}",
                q, searchField, university, pageable.getPageNumber());

        // Clamp the university filter to the caller's tenant scope (403 if out of scope).
        String scopedUniversity = resolveUniversityScope(university);

        // 1. Build dynamic WHERE clause
        StringBuilder where = new StringBuilder("WHERE delete_ts IS NULL");
        List<Object> params = new ArrayList<>();

        if (q != null && !q.isBlank()) {
            // Prefix match (value%) — uses B-tree index range scan (instant).
            // Single column search: searchField determines which column to query.
            String pattern = q.trim() + "%";
            if ("pinfl".equalsIgnoreCase(searchField)) {
                where.append(" AND pinfl LIKE ?");
                params.add(pattern);
            } else if ("code".equalsIgnoreCase(searchField)) {
                where.append(" AND code LIKE ?");
                params.add(pattern);
            } else {
                // Default: search both (backward compatibility)
                where.append(" AND (code LIKE ? OR pinfl LIKE ?)");
                params.addAll(List.of(pattern, pattern));
            }
        }

        addFilter(where, params, "\"_university\"", scopedUniversity);
        addFilter(where, params, "\"_education_type\"", educationType);
        addFilter(where, params, "\"_payment_form\"", paymentForm);
        addFilter(where, params, "\"_student_status\"", studentStatus);
        addFilter(where, params, "\"_course\"", course);
        addFilter(where, params, "\"_faculty\"", faculty);
        addFilter(where, params, "\"_education_form\"", educationForm);
        addFilter(where, params, "\"_education_year\"", educationYear);
        addFilter(where, params, "\"_gender\"", gender);

        String whereClause = where.toString();

        // 2. Total count — strategy depends on whether filters are active
        boolean hasFilters = Stream.of(q, scopedUniversity, educationType, paymentForm, studentStatus,
                        course, faculty, educationForm, educationYear, gender)
                .anyMatch(s -> s != null && !s.isBlank());

        long total;
        if (!hasFilters) {
            // No filters → approximate count from partial index stats (INSTANT, ~0ms)
            // idx_student_active_code_desc already filters delete_ts IS NULL
            total = getApproximateTotal();
        } else {
            // Filters active → exact COUNT (fast because filters narrow dataset), cached
            total = getCachedCount(whereClause, params,
                    q, scopedUniversity, educationType, paymentForm, studentStatus,
                    course, faculty, educationForm, educationYear, gender);
        }

        if (total == 0) {
            return Page.empty(pageable);
        }

        // 3. Deferred join with REVERSE optimization for deep offsets.
        //    Problem: OFFSET 1.6M = PostgreSQL scans 1.6M index entries (slow).
        //    Solution: For deep offsets (last pages), reverse the sort order
        //    so OFFSET is near 0, then reverse the results in Java.
        //
        //    Example (total=1.6M, pageSize=20):
        //      Last page normal:  ORDER BY code DESC OFFSET 1,599,980 → scans 1.6M entries (SLOW)
        //      Last page reverse: ORDER BY code ASC  OFFSET 0          → scans 20 entries (INSTANT)
        long offset = pageable.getOffset();
        boolean reverseQuery = offset > total / 2;

        String orderDir = reverseQuery ? "ASC" : "DESC";
        long effectiveOffset = reverseQuery
                ? Math.max(0, total - offset - pageable.getPageSize())
                : offset;

        String dataSql = "SELECT " + SELECT_COLUMNS
                + " FROM hemishe_e_student t"
                + " JOIN (SELECT id FROM hemishe_e_student " + whereClause
                + " ORDER BY code " + orderDir + " LIMIT ? OFFSET ?) sub ON t.id = sub.id"
                + " ORDER BY t.code " + orderDir;

        List<Object> dataParams = new ArrayList<>(params);
        dataParams.add(pageable.getPageSize());
        dataParams.add((int) effectiveOffset);

        List<StudentListDto> content = jdbcTemplate.query(dataSql, STUDENT_LIST_ROW_MAPPER, dataParams.toArray());

        if (reverseQuery && !content.isEmpty()) {
            // Reverse back to DESC order (natural display order)
            content = new ArrayList<>(content);
            java.util.Collections.reverse(content);
        }

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Get count from cache or compute it. Cached by filter params only (NOT page/size),
     * so all pages with the same filters share one COUNT query.
     *
     * <p>This prevents the #1 performance problem: COUNT(*) on 3.4M rows
     * being re-executed for every page navigation.</p>
     */
    private long getCachedCount(String whereClause, List<Object> params,
                                String q, String university, String educationType,
                                String paymentForm, String studentStatus, String course,
                                String faculty, String educationForm, String educationYear,
                                String gender) {
        String countKey = Stream.of(q, university, educationType, paymentForm, studentStatus,
                        course, faculty, educationForm, educationYear, gender)
                .map(s -> s != null ? s : "")
                .collect(Collectors.joining(":"));

        Cache cache = cacheManager.getCache("studentsListCount");
        if (cache != null) {
            // Use Number.class — Jackson deserializes small numbers as Integer, not Long
            Number cached = cache.get(countKey, Number.class);
            if (cached != null) {
                log.debug("Count cache hit: {}", cached);
                return cached.longValue();
            }
        }

        log.debug("Count cache miss — running COUNT(*)");
        String countSql = "SELECT COUNT(*) FROM hemishe_e_student " + whereClause;
        Long count = params.isEmpty()
                ? jdbcTemplate.queryForObject(countSql, Long.class)
                : jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());
        long total = count != null ? count : 0L;

        if (cache != null) {
            cache.put(countKey, total);
        }
        return total;
    }

    /**
     * Get approximate total count from partial index statistics (INSTANT).
     *
     * <p>Uses PostgreSQL catalog: {@code pg_class.reltuples} for the partial index
     * {@code idx_student_active_code_desc (code DESC) WHERE delete_ts IS NULL}.</p>
     *
     * <p>This is ~0ms vs 5-10 seconds for {@code COUNT(*)} on 3.4M rows.</p>
     *
     * <p>Accuracy: within ~1-5% of actual count (updated by VACUUM/ANALYZE).
     * For pagination display purposes, this is acceptable.</p>
     *
     * @return approximate count of active (non-deleted) students
     */
    private long getApproximateTotal() {
        try {
            Long approx = jdbcTemplate.queryForObject(
                    "SELECT reltuples::bigint FROM pg_class WHERE relname = 'idx_student_active_code_desc'",
                    Long.class);
            if (approx != null && approx > 0) {
                log.debug("Approximate total from index stats: {}", approx);
                return approx;
            }
        } catch (Exception e) {
            log.warn("Failed to get approximate count from index stats, falling back to COUNT(*)", e);
        }

        // Fallback: exact COUNT (index hasn't been analyzed yet)
        log.debug("Index stats unavailable, running exact COUNT(*)");
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hemishe_e_student WHERE delete_ts IS NULL",
                Long.class);
        return count != null ? count : 0L;
    }

    /**
     * Add a filter to WHERE clause. Supports comma-separated multi-values (IN clause).
     */
    private static void addFilter(StringBuilder where, List<Object> params, String column, String value) {
        if (value == null || value.isBlank()) return;

        if (value.contains(",")) {
            List<String> values = Arrays.stream(value.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            if (values.isEmpty()) return;

            where.append(" AND ").append(column).append(" IN (");
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) where.append(",");
                where.append("?");
                params.add(values.get(i));
            }
            where.append(")");
        } else {
            where.append(" AND ").append(column).append(" = ?");
            params.add(value.trim());
        }
    }

    // =====================================================
    // Tenant scope (api-web) — mirrors ReportSupport.Filter.scoped for the raw-JDBC path
    // =====================================================

    /**
     * Server-derived clamp for the {@code "_university"} filter.
     * <ul>
     *   <li>Ministry/system (unrestricted) → the requested value verbatim ({@code null} = national view).</li>
     *   <li>OTM (restricted) → an out-of-scope requested code = 403; no code = clamp to the caller's own code(s).</li>
     *   <li>Organization/unresolved (deny-all) → 403.</li>
     * </ul>
     * The returned string feeds {@link #addFilter} (single code → {@code = ?}, comma list → {@code IN (...)}).
     */
    private String resolveUniversityScope(String requested) {
        AccessScope scope = scopeResolver.currentScope();
        if (scope.unrestricted()) {
            return (requested != null && !requested.isBlank()) ? requested : null;
        }
        if (scope.isDenyAll()) {
            throw new AccessDeniedException("No university data scope for the current principal");
        }
        if (requested != null && !requested.isBlank()) {
            for (String part : requested.split(",")) {
                String code = part.trim();
                if (!code.isEmpty() && !scope.allows(code)) {
                    throw new AccessDeniedException("University out of scope: " + code);
                }
            }
            return requested;
        }
        return String.join(",", scope.universityCodes());
    }

    /** Single-record guard: 403 if the loaded row's university is outside the caller's scope. */
    private void assertUniversityInScope(String universityCode) {
        if (!scopeResolver.currentScope().allows(universityCode)) {
            throw new AccessDeniedException("Student is outside your university scope");
        }
    }

    // =====================================================
    // Duplicate Detection
    // =====================================================

    /**
     * Get duplicate PINFL statistics — categorized by reason.
     *
     * <p><strong>Performance:</strong> Reads from {@code mv_student_duplicates} materialized view
     * (pre-computed). Query time: ~1ms vs ~5-30 seconds with live CTE.</p>
     *
     * @param university optional university code filter (falls back to live CTE)
     * @return duplicate statistics with reason breakdown
     */
    @Cacheable(value = "studentDuplicateStats", key = "(#university != null ? #university : 'all') + '|' + @scopeResolver.currentScopeKey()", unless = "#result == null")
    public DuplicateStatsDto getDuplicateStats(String university) {
        log.debug("Getting duplicate stats - university={}", university);

        // Clamp to tenant scope; a restricted (OTM) caller always resolves to its own code.
        String scoped = resolveUniversityScope(university);

        // University filter requires live CTE (MV doesn't filter by university)
        if (scoped != null && !scoped.isBlank()) {
            return getDuplicateStatsLive(scoped);
        }

        // Fast path: read from materialized view
        String sql = """
            SELECT
              COUNT(*) AS total_duplicate_pinfls,
              COALESCE(SUM(cnt), 0) AS total_affected_students,
              COUNT(CASE WHEN reason = 'NORMAL' THEN 1 END) AS normal_count,
              COUNT(CASE WHEN reason = 'CROSS_UNIVERSITY' THEN 1 END) AS cross_university_count,
              COUNT(CASE WHEN reason = 'SAME_UNIVERSITY' THEN 1 END) AS same_university_count,
              COUNT(CASE WHEN reason = 'MULTI_LEVEL' THEN 1 END) AS multi_level_count,
              COUNT(CASE WHEN reason = 'INTERNAL_TRANSFER' THEN 1 END) AS internal_transfer_count,
              COUNT(CASE WHEN reason = 'EXTERNAL_TRANSFER' THEN 1 END) AS external_transfer_count,
              COALESCE(MAX(cnt), 0) AS max_duplicate_count
            FROM mv_student_duplicates
            """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new DuplicateStatsDto(
                rs.getLong("total_duplicate_pinfls"),
                rs.getLong("total_affected_students"),
                rs.getLong("normal_count"),
                rs.getLong("cross_university_count"),
                rs.getLong("same_university_count"),
                rs.getLong("multi_level_count"),
                rs.getLong("internal_transfer_count"),
                rs.getLong("external_transfer_count"),
                rs.getInt("max_duplicate_count")
        ));
    }

    /**
     * Live CTE fallback for university-filtered stats.
     */
    private DuplicateStatsDto getDuplicateStatsLive(String university) {
        String sql = """
            SELECT
              COUNT(*) AS total_duplicate_pinfls,
              COALESCE(SUM(cnt), 0) AS total_affected_students,
              COUNT(CASE WHEN reason = 'NORMAL' THEN 1 END) AS normal_count,
              COUNT(CASE WHEN reason = 'CROSS_UNIVERSITY' THEN 1 END) AS cross_university_count,
              COUNT(CASE WHEN reason = 'SAME_UNIVERSITY' THEN 1 END) AS same_university_count,
              COUNT(CASE WHEN reason = 'MULTI_LEVEL' THEN 1 END) AS multi_level_count,
              COUNT(CASE WHEN reason = 'INTERNAL_TRANSFER' THEN 1 END) AS internal_transfer_count,
              COUNT(CASE WHEN reason = 'EXTERNAL_TRANSFER' THEN 1 END) AS external_transfer_count,
              COALESCE(MAX(cnt), 0) AS max_duplicate_count
            FROM (
              SELECT pinfl, COUNT(*) AS cnt,
                CASE
                  WHEN COUNT(CASE WHEN "_student_status" = '11' THEN 1 END) > 1
                    AND COUNT(DISTINCT CASE WHEN "_student_status" = '11' THEN "_education_type" END) > 1 THEN 'MULTI_LEVEL'
                  WHEN COUNT(CASE WHEN "_student_status" = '11' THEN 1 END) > 1
                    AND COUNT(DISTINCT CASE WHEN "_student_status" = '11' THEN "_university" END) > 1 THEN 'CROSS_UNIVERSITY'
                  WHEN COUNT(CASE WHEN "_student_status" = '11' THEN 1 END) > 1 THEN 'SAME_UNIVERSITY'
                  WHEN COUNT(DISTINCT "_university") = 1
                    AND COUNT(DISTINCT COALESCE("_speciality_bachelor"::text, "_speciality_master"::text, "_speciality_ordinatura"::text)) > 1 THEN 'INTERNAL_TRANSFER'
                  WHEN COUNT(DISTINCT "_university") > 1 THEN 'EXTERNAL_TRANSFER'
                  ELSE 'NORMAL'
                END AS reason
              FROM hemishe_e_student
              WHERE delete_ts IS NULL AND pinfl IS NOT NULL AND pinfl != '' AND "_university" = ?
              GROUP BY pinfl HAVING COUNT(*) > 1
            ) categorized
            """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new DuplicateStatsDto(
                rs.getLong("total_duplicate_pinfls"),
                rs.getLong("total_affected_students"),
                rs.getLong("normal_count"),
                rs.getLong("cross_university_count"),
                rs.getLong("same_university_count"),
                rs.getLong("multi_level_count"),
                rs.getLong("internal_transfer_count"),
                rs.getLong("external_transfer_count"),
                rs.getInt("max_duplicate_count")
        ), university);
    }

    /**
     * Get paginated list of duplicate PINFL groups with reason analysis.
     *
     * <p><strong>Performance:</strong> Reads from {@code mv_student_duplicates} materialized view.
     * Query time: ~1-5ms vs ~5-30 seconds with live CTE.</p>
     *
     * @param university optional university code filter (falls back to live CTE)
     * @param reason     optional reason filter: NORMAL, CROSS_UNIVERSITY, SAME_UNIVERSITY, MULTI_LEVEL
     * @param pageable   pagination
     * @return page of duplicate groups with reason
     */
    @Cacheable(
            value = "studentDuplicates",
            key = "#university + ':' + #reason + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + '|' + @scopeResolver.currentScopeKey()",
            unless = "#result == null || #pageable.pageNumber > 10"
    )
    public Page<DuplicateGroupDto> getDuplicates(String university, String reason, Pageable pageable) {
        log.debug("Getting duplicates - university={}, reason={}, page={}", university, reason, pageable.getPageNumber());

        // Clamp to tenant scope; a restricted (OTM) caller always resolves to its own code.
        String scoped = resolveUniversityScope(university);

        // 1. Get total count from cached stats
        DuplicateStatsDto stats = getDuplicateStats(scoped);
        long total = getTotalForReason(stats, reason);

        if (total == 0) {
            return Page.empty(pageable);
        }
        if (pageable.getOffset() >= total) {
            return Page.empty(pageable);
        }

        // 2. Get one page of duplicate PINFLs from materialized view (or live CTE)
        List<DuplicatePinflRow> pinflRows;

        if (scoped == null || scoped.isBlank()) {
            pinflRows = queryDuplicatesFromView(reason, pageable);
        } else {
            pinflRows = queryDuplicatesLive(scoped, reason, pageable);
        }

        if (pinflRows.isEmpty()) {
            return Page.empty(pageable);
        }

        // 3. Fetch representative names for these PINFLs (one name per PINFL)
        List<String> pinfls = pinflRows.stream().map(DuplicatePinflRow::pinfl).toList();
        java.util.Map<String, String> nameMap = fetchRepresentativeNames(pinfls);

        // 4. Build lightweight DTOs (no student details)
        List<DuplicateGroupDto> content = pinflRows.stream()
                .map(row -> new DuplicateGroupDto(
                        row.pinfl(),
                        row.cnt(),
                        row.univCount(),
                        row.activeCount(),
                        row.reason(),
                        nameMap.getOrDefault(row.pinfl(), "")
                ))
                .toList();

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Get full detail for a single duplicate PINFL group.
     * Resolves all classifier codes to names via JOINs.
     *
     * @param pinfl PINFL to analyze
     * @return group detail with analysis and resolved student cards
     */
    public DuplicateGroupDetailDto getDuplicateGroupDetail(String pinfl) {
        log.debug("Getting duplicate group detail for PINFL: {}****", pinfl.substring(0, Math.min(4, pinfl.length())));

        // 1. Fetch students with resolved classifier names
        String sql = """
            SELECT
                s.id, s.code, s.firstname, s.lastname, s.fathername, s.pinfl,
                s."_university" AS university_code,
                COALESCE(u.name, s."_university") AS university_name,
                s."_student_status" AS student_status_code,
                COALESCE(ss.name, s."_student_status") AS student_status_name,
                s."_education_type" AS education_type_code,
                COALESCE(et.name, s."_education_type") AS education_type_name,
                s."_education_form" AS education_form_code,
                COALESCE(ef.name, s."_education_form") AS education_form_name,
                s."_payment_form" AS payment_form_code,
                COALESCE(pf.name, s."_payment_form") AS payment_form_name,
                s."_course" AS course_code,
                COALESCE(c.name, s."_course") AS course_name,
                s.group_name,
                s."_education_year" AS education_year,
                s.active,
                COALESCE(sb.name, sm.name, so.name) AS speciality_name
            FROM hemishe_e_student s
            LEFT JOIN hemishe_e_university u ON u.code = s."_university" AND u.delete_ts IS NULL
            LEFT JOIN hemishe_h_student_status_type ss ON ss.code = s."_student_status" AND ss.delete_ts IS NULL
            LEFT JOIN hemishe_h_education_type et ON et.code = s."_education_type" AND et.delete_ts IS NULL
            LEFT JOIN hemishe_h_education_form ef ON ef.code = s."_education_form" AND ef.delete_ts IS NULL
            LEFT JOIN hemishe_h_payment_form pf ON pf.code = s."_payment_form" AND pf.delete_ts IS NULL
            LEFT JOIN hemishe_h_course c ON c.code = s."_course" AND c.delete_ts IS NULL
            LEFT JOIN hemishe_h_speciality_bachelor sb ON sb.id = s."_speciality_bachelor"
            LEFT JOIN hemishe_h_speciality_master sm ON sm.id = s."_speciality_master"
            LEFT JOIN hemishe_h_speciality_ordinatura so ON so.id = s."_speciality_ordinatura"
            WHERE s.delete_ts IS NULL AND s.pinfl = ?
            ORDER BY
                CASE WHEN s."_student_status" = '11' THEN 0 ELSE 1 END,
                s.code
            """;

        List<DuplicateStudentCard> students = jdbcTemplate.query(sql, (rs, rowNum) ->
                new DuplicateStudentCard(
                        rs.getObject("id", UUID.class),
                        rs.getString("code"),
                        Stream.of(rs.getString("lastname"), rs.getString("firstname"), rs.getString("fathername"))
                                .filter(s -> s != null && !s.isBlank())
                                .collect(Collectors.joining(" ")),
                        rs.getString("university_code"),
                        rs.getString("university_name"),
                        rs.getString("student_status_code"),
                        rs.getString("student_status_name"),
                        "11".equals(rs.getString("student_status_code")),
                        rs.getString("education_type_code"),
                        rs.getString("education_type_name"),
                        rs.getString("education_form_code"),
                        rs.getString("education_form_name"),
                        rs.getString("payment_form_code"),
                        rs.getString("payment_form_name"),
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getString("group_name"),
                        rs.getString("education_year"),
                        rs.getString("speciality_name")
                ), pinfl);

        if (students.isEmpty()) {
            throw new ResourceNotFoundException("No students found for PINFL");
        }

        // Tenant-scope guard: a restricted (OTM) caller may open the cross-OTM duplicate card
        // only for a PINFL that actually appears in its own scope — the feature stays cross-OTM
        // for legitimate duplicate resolution, but arbitrary-PINFL fishing is blocked.
        AccessScope scope = scopeResolver.currentScope();
        if (!scope.unrestricted()
                && students.stream().noneMatch(s -> scope.allows(s.universityCode()))) {
            throw new AccessDeniedException("PINFL is outside your university scope");
        }

        // 2. Compute analysis
        int activeCount = (int) students.stream().filter(DuplicateStudentCard::active).count();
        int univCount = (int) students.stream().map(DuplicateStudentCard::universityCode).distinct().count();
        long activeUnivCount = students.stream()
                .filter(DuplicateStudentCard::active)
                .map(DuplicateStudentCard::universityCode)
                .distinct().count();
        long activeEduTypeCount = students.stream()
                .filter(DuplicateStudentCard::active)
                .map(DuplicateStudentCard::educationTypeCode)
                .distinct().count();
        long specCount = students.stream()
                .map(DuplicateStudentCard::specialityName)
                .filter(s -> s != null && !s.isBlank())
                .distinct().count();

        // Check if any single education type is active at >1 university (true cross-uni same-level)
        boolean hasCrossUniSameType = students.stream()
                .filter(DuplicateStudentCard::active)
                .collect(Collectors.groupingBy(DuplicateStudentCard::educationTypeCode))
                .values().stream()
                .anyMatch(group -> group.stream()
                        .map(DuplicateStudentCard::universityCode)
                        .distinct().count() > 1);

        String reason;
        String reasonDescription;
        String recommendation;

        if (activeCount <= 1 && univCount == 1 && specCount > 1) {
            reason = "INTERNAL_TRANSFER";
            reasonDescription = "Talaba bir universitetda mutaxassislik almashtirilgan. " +
                    students.size() + " ta yozuv mavjud, " + specCount + " xil mutaxassislik.";
            recommendation = "Muammo yo'q. Talaba ichki ko'chirish orqali mutaxassislik o'zgartirgan.";
        } else if (activeCount <= 1 && univCount > 1) {
            reason = "EXTERNAL_TRANSFER";
            reasonDescription = "Talaba boshqa universitetga o'tgan. " +
                    students.size() + " ta yozuv mavjud, " + univCount + " ta universitet.";
            recommendation = "Muammo yo'q. Talaba tashqi ko'chirish orqali boshqa OTMga o'tgan.";
        } else if (activeCount <= 1) {
            reason = "NORMAL";
            reasonDescription = students.size() + " ta yozuv mavjud, " +
                    (activeCount == 1 ? "faqat 1 tasi faol" : "faol yozuv yo'q") +
                    ". Qolganlari bitirgan yoki chetlashgan.";
            recommendation = "Muammo yo'q. Talaba oldin o'qigan va qaytadan kirgan bo'lishi mumkin.";
        } else if (hasCrossUniSameType) {
            // Same education type active at >1 university — serious problem
            String sameTypeUnis = students.stream()
                    .filter(DuplicateStudentCard::active)
                    .collect(Collectors.groupingBy(DuplicateStudentCard::educationTypeName))
                    .entrySet().stream()
                    .filter(e -> e.getValue().stream()
                            .map(DuplicateStudentCard::universityCode).distinct().count() > 1)
                    .map(e -> e.getKey() + " (" + e.getValue().stream()
                            .map(DuplicateStudentCard::universityName).distinct()
                            .collect(Collectors.joining(", ")) + ")")
                    .collect(Collectors.joining("; "));
            reason = "CROSS_UNIVERSITY";
            reasonDescription = "Talaba BIR XIL ta'lim darajasida bir nechta universitetda FAOL: " +
                    sameTypeUnis + ". Bu jiddiy muammo.";
            recommendation = "Bir universitetdan chetlashtirish yoki ma'lumotni tuzatish kerak.";
        } else if (activeEduTypeCount > 1) {
            // Different education types active — potentially legitimate (e.g. finishing Bakalavr, starting Magistr)
            String activeEduTypes = students.stream()
                    .filter(DuplicateStudentCard::active)
                    .map(s -> s.educationTypeName() + " — " + s.universityName())
                    .collect(Collectors.joining(", "));
            reason = "MULTI_LEVEL";
            reasonDescription = "Talaba turli ta'lim darajalarida bir vaqtda faol: " + activeEduTypes + ".";
            if (activeUnivCount > 1) {
                recommendation = "Tekshiring: agar talaba bir darajani bitirish arafasida bo'lsa va " +
                        "keyingi darajaga kirgan bo'lsa (masalan, bakalavr → magistr), bu qonuniy holat. " +
                        "Aks holda, ma'lumotni tuzatish kerak.";
            } else {
                recommendation = "Bir universitetda turli darajalar: magistrga o'tgan bo'lsa, " +
                        "bakalavrni bitirgan qilish lozim.";
            }
        } else if (activeUnivCount > 1) {
            // Same type at >1 university (shouldn't reach here due to hasCrossUniSameType, but safety)
            reason = "CROSS_UNIVERSITY";
            reasonDescription = "Talaba " + activeUnivCount +
                    " ta universitetda bir vaqtda FAOL. Bu jiddiy muammo.";
            recommendation = "Bir universitetdan chetlashtirish yoki ma'lumotni tuzatish kerak.";
        } else {
            reason = "SAME_UNIVERSITY";
            reasonDescription = "Talaba bir universitetda " + activeCount +
                    " marta faol yozuvga ega. Bu ma'lumot xatosi.";
            recommendation = "Ortiqcha yozuvlarni o'chirish yoki birlashtirish kerak.";
        }

        String fullName = students.getFirst().fullName();

        return new DuplicateGroupDetailDto(
                pinfl, fullName, students.size(), univCount, activeCount,
                reason, reasonDescription, recommendation, students
        );
    }

    /**
     * Query duplicate PINFLs from materialized view (instant).
     */
    private List<DuplicatePinflRow> queryDuplicatesFromView(String reason, Pageable pageable) {
        List<Object> params = new ArrayList<>();
        String where = "WHERE 1=1";
        if (reason != null && !reason.isBlank()) {
            where += " AND reason = ?";
            params.add(reason);
        }
        params.add(pageable.getPageSize());
        params.add((int) pageable.getOffset());

        String sql = "SELECT pinfl, cnt, univ_count, active_count, spec_count, reason FROM mv_student_duplicates "
                + where + " ORDER BY cnt DESC, pinfl LIMIT ? OFFSET ?";

        return jdbcTemplate.query(sql, DUPLICATE_PINFL_ROW_MAPPER, params.toArray());
    }

    /**
     * Query duplicate PINFLs via live CTE (for university filter).
     */
    private List<DuplicatePinflRow> queryDuplicatesLive(String university, String reason, Pageable pageable) {
        List<Object> params = new ArrayList<>();
        String universityFilter = "";
        if (university != null && !university.isBlank()) {
            universityFilter = " AND \"_university\" = ?";
            params.add(university);
        }

        String cte = """
            WITH categorized AS (
              SELECT pinfl, COUNT(*) AS cnt, COUNT(DISTINCT "_university") AS univ_count,
                COUNT(CASE WHEN "_student_status" = '11' THEN 1 END) AS active_count,
                COUNT(DISTINCT COALESCE("_speciality_bachelor"::text, "_speciality_master"::text, "_speciality_ordinatura"::text)) AS spec_count,
                CASE
                  WHEN COUNT(CASE WHEN "_student_status" = '11' THEN 1 END) > 1
                    AND COUNT(DISTINCT CASE WHEN "_student_status" = '11' THEN "_education_type" END) > 1 THEN 'MULTI_LEVEL'
                  WHEN COUNT(CASE WHEN "_student_status" = '11' THEN 1 END) > 1
                    AND COUNT(DISTINCT CASE WHEN "_student_status" = '11' THEN "_university" END) > 1 THEN 'CROSS_UNIVERSITY'
                  WHEN COUNT(CASE WHEN "_student_status" = '11' THEN 1 END) > 1 THEN 'SAME_UNIVERSITY'
                  WHEN COUNT(DISTINCT "_university") = 1
                    AND COUNT(DISTINCT COALESCE("_speciality_bachelor"::text, "_speciality_master"::text, "_speciality_ordinatura"::text)) > 1 THEN 'INTERNAL_TRANSFER'
                  WHEN COUNT(DISTINCT "_university") > 1 THEN 'EXTERNAL_TRANSFER'
                  ELSE 'NORMAL'
                END AS reason
              FROM hemishe_e_student
              WHERE delete_ts IS NULL AND pinfl IS NOT NULL AND pinfl != ''""" + universityFilter + """

              GROUP BY pinfl HAVING COUNT(*) > 1
            )
            """;

        String reasonFilter = "";
        if (reason != null && !reason.isBlank()) {
            reasonFilter = " AND reason = ?";
            params.add(reason);
        }
        params.add(pageable.getPageSize());
        params.add((int) pageable.getOffset());

        String sql = cte + " SELECT pinfl, cnt, univ_count, active_count, spec_count, reason FROM categorized WHERE 1=1"
                + reasonFilter + " ORDER BY cnt DESC, pinfl LIMIT ? OFFSET ?";

        return jdbcTemplate.query(sql, DUPLICATE_PINFL_ROW_MAPPER, params.toArray());
    }

    /**
     * Refresh the duplicate analysis materialized view.
     * Called by scheduled job. Uses CONCURRENTLY for non-blocking refresh.
     */
    @Transactional(readOnly = false)
    public void refreshDuplicateMaterializedView() {
        log.info("Refreshing mv_student_duplicates materialized view...");
        jdbcTemplate.execute("REFRESH MATERIALIZED VIEW CONCURRENTLY mv_student_duplicates");
        log.info("mv_student_duplicates refresh completed");
    }

    /**
     * Fetch one representative name per PINFL (prefer active student).
     */
    private java.util.Map<String, String> fetchRepresentativeNames(List<String> pinfls) {
        if (pinfls.isEmpty()) return java.util.Map.of();

        StringBuilder inClause = new StringBuilder("(");
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < pinfls.size(); i++) {
            if (i > 0) inClause.append(",");
            inClause.append("?");
            params.add(pinfls.get(i));
        }
        inClause.append(")");

        String sql = """
            SELECT DISTINCT ON (pinfl) pinfl,
                CONCAT_WS(' ', lastname, firstname, fathername) AS full_name
            FROM hemishe_e_student
            WHERE delete_ts IS NULL AND pinfl IN """ + inClause + """
             ORDER BY pinfl,
                CASE WHEN "_student_status" = '11' THEN 0 ELSE 1 END,
                code
            """;

        java.util.Map<String, String> result = new java.util.HashMap<>();
        jdbcTemplate.query(sql, (rs) -> {
            result.put(rs.getString("pinfl"), rs.getString("full_name"));
        }, params.toArray());
        return result;
    }

    /**
     * Extract total count for a given reason from cached stats.
     */
    private long getTotalForReason(DuplicateStatsDto stats, String reason) {
        if (reason == null || reason.isBlank()) {
            return stats.totalDuplicatePinfls();
        }
        return switch (reason) {
            case "CROSS_UNIVERSITY" -> stats.crossUniversityCount();
            case "SAME_UNIVERSITY" -> stats.sameUniversityCount();
            case "MULTI_LEVEL" -> stats.multiLevelCount();
            case "INTERNAL_TRANSFER" -> stats.internalTransferCount();
            case "EXTERNAL_TRANSFER" -> stats.externalTransferCount();
            case "NORMAL" -> stats.normalCount();
            default -> stats.totalDuplicatePinfls();
        };
    }

    /**
     * Get student by UUID
     *
     * @param id student UUID
     * @return student DTO
     * @throws ResourceNotFoundException if not found
     */
    public StudentDto getStudentById(UUID id) {
        log.debug("Getting student by ID: {}", id);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        assertUniversityInScope(student.getUniversity());
        return studentMapper.toDto(student);
    }

    /**
     * Get student by code (business key)
     *
     * @param code student code
     * @return student DTO
     * @throws ResourceNotFoundException if not found
     */
    public StudentDto getStudentByCode(String code) {
        log.debug("Getting student by code: {}", code);

        Student student = studentRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "code", code));

        assertUniversityInScope(student.getUniversity());
        return studentMapper.toDto(student);
    }

    /**
     * Get student statistics for dashboard cards.
     *
     * <p>Uses native SQL against {@code hemishe_r_student_full} denormalized view
     * (same approach as {@link uz.hemis.service.dashboard.DashboardService}).</p>
     *
     * <p><strong>Why native SQL instead of JPA Specification?</strong></p>
     * <ul>
     *   <li>Entity has {@code @SQLRestriction("delete_ts IS NULL")} which hides graduated/expelled students</li>
     *   <li>The view uses {@code is_expel} flag (different from {@code delete_ts})</li>
     *   <li>Grant/Contract must only count ACTIVE students (status_code = '11')</li>
     *   <li>Reads from REPLICA database (zero master load)</li>
     * </ul>
     *
     * @param university optional university code filter
     * @return stats DTO with total, grant, contract, graduate counts
     */
    @Cacheable(value = "studentStats", key = "(#university != null ? #university : 'all') + '|' + @scopeResolver.currentScopeKey()", unless = "#result == null")
    public StudentStatsDto getStats(String university) {
        log.debug("Getting student stats - university={}", university);

        // Clamp to tenant scope (403 if out of scope); null = national (ministry only).
        String scopedUniversity = resolveUniversityScope(university);

        // Reads directly from hemishe_e_student (same as old-hemis JPQL queries)
        // Old-hemis status codes (hemishe_h_student_status_type):
        //   '11' = Aktiv    '12' = Chetlashgan    '13' = Akad.ta'til
        //   '14' = Bitirgan  '15' = Ko'chirilgan
        // Old-hemis dashboard: COUNT WHERE studentStatus.code = '11'
        // Old-hemis graduates: studentStatus.code = '11' AND isGraduate = '1'

        StringBuilder where = new StringBuilder("WHERE delete_ts IS NULL");
        List<Object> params = new ArrayList<>();
        addFilter(where, params, "\"_university\"", scopedUniversity);

        String sql = """
            SELECT
              COUNT(CASE WHEN "_student_status" = '11' THEN 1 END) as total,
              COUNT(CASE WHEN "_student_status" = '11' AND "_payment_form" = '11' THEN 1 END) as grant_count,
              COUNT(CASE WHEN "_student_status" = '11' AND "_payment_form" = '12' THEN 1 END) as contract_count,
              COUNT(CASE WHEN "_student_status" = '14'
                           OR ("_student_status" = '11' AND is_graduate = '1') THEN 1 END) as graduate_count
            FROM hemishe_e_student
            """ + where;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new StudentStatsDto(
                rs.getLong("total"),
                rs.getLong("grant_count"),
                rs.getLong("contract_count"),
                rs.getLong("graduate_count")
        ), params.toArray());
    }

    /**
     * Get filter dictionaries for student filter dropdowns.
     *
     * <p>Delegates to {@link ClassifierWebService} which handles dynamic column detection
     * (not all classifier tables have a {@code name} column — some use {@code name_uz}).</p>
     *
     * @return DTO with lists for courses, statuses, payment forms, education types/forms, genders
     */
    @Cacheable(value = "studentDictionaries", key = "'all'", unless = "#result == null")
    public StudentDictionariesDto getDictionaries() {
        log.debug("Loading student dictionaries via ClassifierWebService");

        return StudentDictionariesDto.builder()
                .courses(loadDictionary("course"))
                .studentStatuses(loadDictionary("student-status-type"))
                .paymentForms(loadDictionary("payment-form"))
                .educationTypes(loadDictionary("education-type"))
                .educationForms(loadDictionary("education-form"))
                .genders(loadDictionary("gender"))
                .build();
    }

    private List<DictionaryItem> loadDictionary(String apiKey) {
        Page<ClassifierItemDto> page = classifierWebService.getClassifierItems(
                apiKey, null, PageRequest.of(0, 500));

        return page.getContent().stream()
                .map(item -> DictionaryItem.builder()
                        .code(item.getCode())
                        .name(item.getName() != null ? item.getName() : item.getCode())
                        .build())
                .toList();
    }

    // =====================================================
    // Speciality Directions
    // =====================================================

    /**
     * Get paginated speciality statistics for directions page.
     *
     * <p><strong>Performance optimization:</strong></p>
     * <ul>
     *   <li>Each education type aggregates students via its own FK column
     *       (uses FK index instead of COALESCE full-scan)</li>
     *   <li>educationType filter → only 1 branch instead of 3 (3x faster)</li>
     *   <li>COUNT(*) OVER() eliminates separate count query (1 query instead of 2)</li>
     * </ul>
     *
     * @param search        search query (ILIKE on code + name)
     * @param educationType filter: BACHELOR, MASTER, ORDINATURA
     * @param hasStudents   filter: true = only with students, false = only without
     * @param pageable      pagination
     * @return page of speciality stats
     */
    @Cacheable(
            value = "specialityStats",
            key = "#search + ':' + #educationType + ':' + #hasStudents + ':' + #pageable.pageNumber + ':' + #pageable.pageSize",
            unless = "#result == null || #pageable.pageNumber > 10"
    )
    public Page<SpecialityStatsDto> getSpecialityStats(
            String search, String educationType, Boolean hasStudents, Pageable pageable) {
        log.debug("Getting speciality stats - search={}, educationType={}, hasStudents={}, page={}",
                search, educationType, hasStudents, pageable.getPageNumber());

        // Build UNION ALL branches — only include matching education types
        List<Object> params = new ArrayList<>();
        List<String> branches = new ArrayList<>();

        boolean includeAll = educationType == null || educationType.isBlank();

        if (includeAll || "BACHELOR".equals(educationType)) {
            branches.add(buildSpecialityBranch(
                    "hemishe_h_speciality_bachelor", "\"_speciality_bachelor\"",
                    "BACHELOR", search, hasStudents, params));
        }
        if (includeAll || "MASTER".equals(educationType)) {
            branches.add(buildSpecialityBranch(
                    "hemishe_h_speciality_master", "\"_speciality_master\"",
                    "MASTER", search, hasStudents, params));
        }
        if (includeAll || "ORDINATURA".equals(educationType)) {
            branches.add(buildSpecialityBranch(
                    "hemishe_h_speciality_ordinatura", "\"_speciality_ordinatura\"",
                    "ORDINATURA", search, hasStudents, params));
        }

        // Single query: data + total count via window function
        String sql = "SELECT *, COUNT(*) OVER() AS total_count FROM ("
                + String.join(" UNION ALL ", branches)
                + ") combined ORDER BY total_students DESC, code LIMIT ? OFFSET ?";

        params.add(pageable.getPageSize());
        params.add((int) pageable.getOffset());

        final long[] totalCount = {0};
        List<SpecialityStatsDto> content = jdbcTemplate.query(sql, (rs, rowNum) -> {
            totalCount[0] = rs.getLong("total_count");
            return new SpecialityStatsDto(
                    rs.getString("id"),
                    rs.getString("code"),
                    rs.getString("name"),
                    rs.getString("education_type"),
                    rs.getLong("total_students"),
                    rs.getLong("active_students"),
                    rs.getLong("graduated_students"),
                    rs.getLong("expelled_students")
            );
        }, params.toArray());

        long total = content.isEmpty() ? 0 : totalCount[0];
        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Build one UNION ALL branch for a specific education type.
     * LEFT JOINs student counts aggregated by the direct FK column (index-friendly).
     */
    private String buildSpecialityBranch(
            String specTable, String fkColumn, String eduType,
            String search, Boolean hasStudents, List<Object> params) {

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT sp.id::text AS id, sp.code, sp.name, '")
                .append(eduType).append("' AS education_type,")
                .append(" COALESCE(sc.total_students, 0) AS total_students,")
                .append(" COALESCE(sc.active_students, 0) AS active_students,")
                .append(" COALESCE(sc.graduated_students, 0) AS graduated_students,")
                .append(" COALESCE(sc.expelled_students, 0) AS expelled_students")
                .append(" FROM ").append(specTable).append(" sp")
                .append(" LEFT JOIN (")
                .append("  SELECT ").append(fkColumn).append(" AS spec_id,")
                .append("  COUNT(*) AS total_students,")
                .append("  COUNT(CASE WHEN \"_student_status\" = '11' THEN 1 END) AS active_students,")
                .append("  COUNT(CASE WHEN \"_student_status\" = '14' THEN 1 END) AS graduated_students,")
                .append("  COUNT(CASE WHEN \"_student_status\" = '12' THEN 1 END) AS expelled_students")
                .append("  FROM hemishe_e_student")
                .append("  WHERE delete_ts IS NULL AND ").append(fkColumn).append(" IS NOT NULL")
                .append("  GROUP BY ").append(fkColumn)
                .append(" ) sc ON sc.spec_id = sp.id");

        // Filters applied per-branch (pushed down into each branch for optimizer)
        List<String> conditions = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            conditions.add("(sp.code ILIKE ? OR sp.name ILIKE ?)");
            String pattern = "%" + search.trim() + "%";
            params.add(pattern);
            params.add(pattern);
        }

        if (hasStudents != null) {
            conditions.add(hasStudents
                    ? "COALESCE(sc.total_students, 0) > 0"
                    : "COALESCE(sc.total_students, 0) = 0");
        }

        if (!conditions.isEmpty()) {
            sb.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        return sb.toString();
    }

    /**
     * Get summary statistics for speciality directions (header cards).
     *
     * <p><strong>Performance:</strong> Uses scalar subqueries with direct FK columns.
     * Each COUNT(DISTINCT fk) can use the FK index. No COALESCE full-scan.</p>
     *
     * @return summary with total, with/without students counts
     */
    @Cacheable(value = "specialitySummary", key = "'all'", unless = "#result == null")
    public SpecialitySummaryDto getSpecialitySummary() {
        log.debug("Getting speciality summary");

        String sql = """
                SELECT
                  (SELECT COUNT(*) FROM hemishe_h_speciality_bachelor)
                    + (SELECT COUNT(*) FROM hemishe_h_speciality_master)
                    + (SELECT COUNT(*) FROM hemishe_h_speciality_ordinatura) AS total_specialities,
                  (SELECT COUNT(DISTINCT "_speciality_bachelor")
                     FROM hemishe_e_student WHERE delete_ts IS NULL AND "_speciality_bachelor" IS NOT NULL)
                    + (SELECT COUNT(DISTINCT "_speciality_master")
                         FROM hemishe_e_student WHERE delete_ts IS NULL AND "_speciality_master" IS NOT NULL)
                    + (SELECT COUNT(DISTINCT "_speciality_ordinatura")
                         FROM hemishe_e_student WHERE delete_ts IS NULL AND "_speciality_ordinatura" IS NOT NULL) AS with_students,
                  (SELECT COUNT(*) FROM hemishe_e_student
                     WHERE delete_ts IS NULL AND "_speciality_bachelor" IS NOT NULL)
                    + (SELECT COUNT(*) FROM hemishe_e_student
                         WHERE delete_ts IS NULL AND "_speciality_master" IS NOT NULL)
                    + (SELECT COUNT(*) FROM hemishe_e_student
                         WHERE delete_ts IS NULL AND "_speciality_ordinatura" IS NOT NULL) AS total_students_in_specialities
                """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            long total = rs.getLong("total_specialities");
            long withStudents = rs.getLong("with_students");
            return new SpecialitySummaryDto(
                    total,
                    withStudents,
                    total - withStudents,
                    rs.getLong("total_students_in_specialities")
            );
        });
    }
}
