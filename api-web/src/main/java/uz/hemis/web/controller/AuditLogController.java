package uz.hemis.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@PreAuthorize("hasAuthority('audit.view')")
@RequiredArgsConstructor
@Slf4j
public class AuditLogController {

    private final AuditService auditService;

    // =====================================================
    // Activity Logs
    // =====================================================

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
            @Parameter(description = "Entity turi") @RequestParam(required = false) String entityType,
            @Parameter(description = "IP manzil") @RequestParam(required = false) String ip,
            @Parameter(description = "Boshlanish sanasi (ISO 8601)") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "Tugash sanasi (ISO 8601)") @RequestParam(required = false) String dateTo,
            @Parameter(description = "Qidiruv (entity nomi, tavsif)") @RequestParam(required = false) String search) {

        Map<String, String> filters = buildFilters(userId, username, ip, dateFrom, dateTo, search);
        if (action != null) filters.put("action", action);
        if (entityType != null) filters.put("entityType", entityType);

        return ResponseEntity.ok(ResponseWrapper.success(
                auditService.getActivities(filters, page, size)));
    }

    @GetMapping("/activities/{id}")
    @Operation(summary = "Activity log tafsiloti", description = "Old/new value diff bilan")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli"),
            @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getActivityDetail(
            @PathVariable String id) {
        Map<String, Object> detail = auditService.getActivityDetail(id);
        if (detail == null) {
            return ResponseEntity.status(404).body(ResponseWrapper.error("Activity log not found"));
        }
        return ResponseEntity.ok(ResponseWrapper.success(detail));
    }

    // =====================================================
    // Entity History
    // =====================================================

    @GetMapping("/entities/{entityType}/{entityId}/history")
    @Operation(summary = "Entity o'zgarishlar tarixi",
               description = "Bitta entity uchun barcha CREATE/UPDATE/DELETE tarix")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab etiladi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    public ResponseEntity<ResponseWrapper<PageResponse<Map<String, Object>>>> getEntityHistory(
            @Parameter(description = "Entity turi (masalan: University, Student)")
            @PathVariable String entityType,
            @Parameter(description = "Entity ID yoki kodi")
            @PathVariable String entityId,
            @Parameter(description = "Sahifa raqami (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Sahifa hajmi")
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ResponseWrapper.success(
                auditService.getEntityHistory(entityType, entityId, page, size)));
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
                auditService.getErrors(filters, page, size)));
    }

    @GetMapping("/errors/{id}")
    @Operation(summary = "Error log tafsiloti", description = "Stack trace bilan")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getErrorDetail(
            @PathVariable String id) {
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
                auditService.getLogins(filters, page, size)));
    }

    // =====================================================
    // Statistics
    // =====================================================

    @GetMapping("/stats")
    @Operation(summary = "Audit statistikasi", description = "Top users, endpoints, error rates")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getStats(
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {
        return ResponseEntity.ok(ResponseWrapper.success(
                auditService.getStats(dateFrom, dateTo)));
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
