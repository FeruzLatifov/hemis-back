package uz.hemis.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.service.audit.AuditService;

import java.util.Map;

/**
 * Audit Log Controller — Tizim loglarini ko'rish API
 *
 * <p>4 turdagi loglar: Activity, Request, Error, Login</p>
 * <p>Filtrlash, sahifalash, statistika va eksport imkoniyatlari</p>
 */
@RestController
@RequestMapping("/api/v1/web/audit")
@Tag(name = "Audit Logs", description = "Tizim audit loglarini ko'rish va tahlil qilish")
@SecurityRequirement(name = "bearerAuth")
// UNIVERSITY_ADMIN cross-OTM audit log ko'rmasligi shart — vazirlik darajasidagi audit
// faqat ministry rollar uchun. Per-OTM audit feature alohida endpoint (kelajakda).
// Permission only. The role half of this gate was unreachable: a USER token's authorities are the
// permission codes loaded from the Redis cache (JwtGrantedAuthoritiesConverter), and no ROLE_* is
// ever granted — so hasRole('ADMIN') was false for every real caller and the whole audit API
// answered 403 to everyone, including SUPER_ADMIN. The audience is now expressed where it belongs,
// in the role→permission mapping: audit.view is held by SUPER_ADMIN and ADMIN only (seed S038).
@PreAuthorize("hasAuthority('audit.view')")
@RequiredArgsConstructor
@Slf4j
@Validated
// Same gate as AuditService/AuditDataSourceConfig — no audit datasource under the test profile.
@Profile("!test")
@ConditionalOnProperty(name = "hemis.audit.enabled", havingValue = "true", matchIfMissing = false)
public class AuditLogController {

    private final AuditService auditService;

    // =====================================================
    // Activity Logs
    // =====================================================


    /** Hard ceiling on a page. The audit log only grows; an unbounded `size` is a self-service DoS. */
    private static final int MAX_PAGE_SIZE = 200;

    private static int clampSize(int size) {
        return Math.max(1, Math.min(size, MAX_PAGE_SIZE));
    }

    private static int clampPage(int page) {
        return Math.max(0, page);
    }

    /** 30 days back, ISO-8601 — the window /stats falls back to when none is given. */
    private static String defaultStatsFrom() {
        return java.time.LocalDate.now().minusDays(30).atStartOfDay().toString();
    }

    @GetMapping("/activities")
    @Operation(summary = "Activity loglar ro'yxati", description = "CRUD operatsiyalar audit logi")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab etiladi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    public ResponseEntity<ResponseWrapper<PageResponse<Map<String, Object>>>> getActivities(
            @Parameter(description = "Sahifa raqami (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Sahifa hajmi") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Foydalanuvchi ID") @RequestParam(required = false) String userId,
            @Parameter(description = "Foydalanuvchi nomi") @RequestParam(required = false) String username,
            @Parameter(description = "Harakat turi (CREATE, UPDATE, DELETE, VIEW, EXPORT, IMPORT)") @RequestParam(required = false) String action,
            @Parameter(description = "Entity turi — vergul bilan ro'yxat ham mumkin (masalan: HSpeciality,ClassifierItem)")
            @RequestParam(required = false) String entityType,
            @Parameter(description = "Entity ID — bitta yozuv tarixini ko'rish uchun (masalan mutaxassislik id'si)")
            @RequestParam(required = false) String entityId,
            @Parameter(description = "Qamrov kaliti — egasi bo'yicha tarix (masalan OTM kodi \"301\"); o'chirilgan qatorlar ham chiqadi")
            @RequestParam(required = false) String scopeKey,
            @Parameter(description = "IP manzil") @RequestParam(required = false) String ip,
            @Parameter(description = "Boshlanish sanasi (ISO 8601)") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "Tugash sanasi (ISO 8601)") @RequestParam(required = false) String dateTo,
            @Parameter(description = "Qidiruv (entity nomi, tavsif)") @RequestParam(required = false) String search) {

        Map<String, String> filters = buildFilters(userId, username, ip, dateFrom, dateTo, search);
        if (action != null) filters.put("action", action);
        if (entityType != null) filters.put("entityType", entityType);
        if (entityId != null) filters.put("entityId", entityId);
        if (scopeKey != null) filters.put("scopeKey", scopeKey);

        return ResponseEntity.ok(ResponseWrapper.success(
                auditService.getActivities(filters, clampPage(page), clampSize(size))));
    }

    @GetMapping("/activities/{id}")
    @Operation(summary = "Activity log tafsiloti", description = "Old/new value diff bilan")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli"),
            @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    @PreAuthorize("hasAnyAuthority('audit.view', 'audit.history.view')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getActivityDetail(
            @PathVariable @NotBlank String id) {
        Map<String, Object> detail = auditService.getActivityDetail(id);
        if (detail == null) {
            return ResponseEntity.status(404).body(ResponseWrapper.error("Activity log not found"));
        }
        // The owner-scoped history lists entries without their before/after images and fetches them
        // one at a time, so a reader who may see the list must be able to open an entry of it —
        // otherwise the dialog renders an empty diff, which reads as "every field was cleared".
        // The journal itself stays closed: the narrow permission may read only the record types it
        // curates, checked here against the row that was actually found.
        assertEntityTypeReadable(String.valueOf(detail.get("entityType")));
        return ResponseEntity.ok(ResponseWrapper.success(detail));
    }

    // =====================================================
    // Entity History
    // =====================================================

    /**
     * One record's history — the narrow read.
     *
     * <p>Separate from {@code audit.view} on purpose: reading the whole journal (every user, every
     * IP, every before/after snapshot including personal data) and reading what happened to the row
     * in front of you are different capabilities. An operator needs the second and has no business
     * with the first, so {@code audit.history.view} unlocks this endpoint alone, and only for the
     * entity types an operator actually curates ({@link #OPERATOR_ENTITY_TYPES}). A holder of the
     * full {@code audit.view} is not restricted.</p>
     */
    @GetMapping("/entities/{entityType}/{entityId}/history")
    @PreAuthorize("hasAnyAuthority('audit.view', 'audit.history.view')")
    @Operation(summary = "Entity o'zgarishlar tarixi",
               description = "Bitta entity uchun barcha CREATE/UPDATE/DELETE tarix")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab etiladi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    public ResponseEntity<ResponseWrapper<PageResponse<Map<String, Object>>>> getEntityHistory(
            @Parameter(description = "Entity turi (masalan: University, Student)")
            @PathVariable @NotBlank String entityType,
            @Parameter(description = "Entity ID yoki kodi")
            @PathVariable @NotBlank String entityId,
            @Parameter(description = "Sahifa raqami (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Sahifa hajmi")
            @RequestParam(defaultValue = "50") int size) {
        assertEntityTypeReadable(entityType);
        return ResponseEntity.ok(ResponseWrapper.success(
                auditService.getEntityHistory(entityType, entityId, clampPage(page), clampSize(size))));
    }

    /**
     * Owner-scoped history — "everything that happened to OTM 301's attachments".
     *
     * <p>Its own endpoint rather than a filter on {@code /activities}: the list endpoint is the whole
     * journal and stays {@code audit.view}, while this one answers a bounded question an operator is
     * entitled to ask. A hard-deleted link row has no other way to be accounted for.</p>
     */
    @GetMapping("/scopes/{entityType}/{scopeKey}/history")
    @PreAuthorize("hasAnyAuthority('audit.view', 'audit.history.view')")
    @Operation(summary = "Egasi bo'yicha o'zgarishlar tarixi",
               description = "Bitta egaga (masalan OTM kodi) tegishli barcha yozuvlar — o'chirilganlari ham")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab etiladi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    public ResponseEntity<ResponseWrapper<PageResponse<Map<String, Object>>>> getScopeHistory(
            @Parameter(description = "Entity turi (masalan: UniversitySpecialityAttachment)")
            @PathVariable @NotBlank String entityType,
            @Parameter(description = "Qamrov kaliti — masalan OTM kodi")
            @PathVariable @NotBlank String scopeKey,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        assertEntityTypeReadable(entityType);
        Map<String, String> filters = new java.util.HashMap<>();
        filters.put("entityType", entityType);
        filters.put("scopeKey", scopeKey);
        return ResponseEntity.ok(ResponseWrapper.success(
                auditService.getActivities(filters, clampPage(page), clampSize(size))));
    }

    /**
     * What an {@code audit.history.view} holder may look at: the registries they curate.
     *
     * <p>Without this the narrow permission would still reach {@code User} or {@code OAuthClient}
     * history — the snapshots the wide permission exists to protect.</p>
     */
    private static final java.util.Set<String> OPERATOR_ENTITY_TYPES = java.util.Set.of(
            "HSpeciality", "ClassifierItem",
            "UniversitySpecialityAttachment", "UniversityAttachedSpeciality");

    private void assertEntityTypeReadable(String entityType) {
        var auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        boolean fullAccess = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "audit.view".equals(a.getAuthority()));
        if (!fullAccess && !OPERATOR_ENTITY_TYPES.contains(entityType)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "audit.history.view covers curated registries only; " + entityType
                            + " needs the full audit.view");
        }
    }

    // =====================================================
    // Error Logs
    // =====================================================

    @GetMapping("/errors")
    @Operation(summary = "Error loglar ro'yxati", description = "Xatolar audit logi")
    public ResponseEntity<ResponseWrapper<PageResponse<Map<String, Object>>>> getErrors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String errorType,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String search) {

        Map<String, String> filters = buildFilters(userId, username, ip, dateFrom, dateTo, search);
        if (errorType != null) filters.put("errorType", errorType);

        return ResponseEntity.ok(ResponseWrapper.success(
                auditService.getErrors(filters, clampPage(page), clampSize(size))));
    }

    @GetMapping("/errors/{id}")
    @Operation(summary = "Error log tafsiloti", description = "Stack trace bilan")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getErrorDetail(
            @PathVariable @NotBlank String id) {
        Map<String, Object> detail = auditService.getErrorDetail(id);
        if (detail == null) {
            return ResponseEntity.status(404).body(ResponseWrapper.error("Error log not found"));
        }
        return ResponseEntity.ok(ResponseWrapper.success(detail));
    }

    // =====================================================
    // Login Logs
    // =====================================================

    @GetMapping("/logins")
    @Operation(summary = "Login loglar ro'yxati", description = "Autentifikatsiya hodisalari logi")
    public ResponseEntity<ResponseWrapper<PageResponse<Map<String, Object>>>> getLogins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {

        Map<String, String> filters = buildFilters(userId, username, ip, dateFrom, dateTo, null);
        if (eventType != null) filters.put("eventType", eventType);

        return ResponseEntity.ok(ResponseWrapper.success(
                auditService.getLogins(filters, clampPage(page), clampSize(size))));
    }

    // =====================================================
    // Statistics
    // =====================================================

    @GetMapping("/stats")
    @Operation(summary = "Audit statistikasi", description = "Top users, endpoints, error rates")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getStats(
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {
        // An unbounded aggregation over a table that only grows is the first thing to fall over, so
        // an unfiltered call defaults to the last 30 days. Only an UNFILTERED one: adding the
        // default to a caller who named just an end date built "last 30 days AND before <older
        // date>" — an empty range that showed 0 everywhere while the table below listed the rows.
        String from = (dateFrom == null && dateTo == null) ? defaultStatsFrom() : dateFrom;
        return ResponseEntity.ok(ResponseWrapper.success(auditService.getStats(from, dateTo)));
    }

    // =====================================================
    // Private Helpers
    // =====================================================

    private Map<String, String> buildFilters(String userId, String username, String ip,
                                              String dateFrom, String dateTo, String search) {
        Map<String, String> filters = new java.util.HashMap<>();
        if (userId != null) filters.put("userId", userId);
        if (username != null) filters.put("username", username);
        if (ip != null) filters.put("ip", ip);
        if (dateFrom != null) filters.put("dateFrom", dateFrom);
        if (dateTo != null) filters.put("dateTo", dateTo);
        if (search != null) filters.put("search", search);
        return filters;
    }
}
