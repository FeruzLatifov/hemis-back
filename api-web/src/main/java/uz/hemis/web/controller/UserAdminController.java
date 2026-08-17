package uz.hemis.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.service.util.PageResponses;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.service.admin.UserAdminService;
import uz.hemis.service.admin.GovPersonLookupService;
import uz.hemis.service.admin.dto.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * User Administration Controller
 *
 * <p>RESTful API for user CRUD operations with hierarchical scope validation.</p>
 *
 * <p><strong>Base Path:</strong> {@code /api/v1/web/admin/users}</p>
 *
 * <p><strong>Security:</strong></p>
 * <ul>
 *   <li>All endpoints require authentication (JWT Bearer token)</li>
 *   <li>GET endpoints require {@code users.view} or {@code users.manage} permission</li>
 *   <li>CUD endpoints require {@code users.manage} permission</li>
 *   <li>Scope validation at service layer (SUPER_ADMIN > MINISTRY_ADMIN > OTM_API)</li>
 * </ul>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/web/admin/users")
@Tag(name = "User Management", description = "Admin CRUD operations for user management")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserAdminController {

    private final UserAdminService userAdminService;
    private final GovPersonLookupService govPersonLookupService;

    // =====================================================
    // READ ENDPOINTS
    // =====================================================

    // POST (not GET) — PINFL + passport are PII and must travel in the body, never the URL
    // query-string (which nginx/proxies write to access logs). Read-only despite the POST verb.
    @PostMapping("/person-lookup")
    @PreAuthorize("hasAnyAuthority('users.create', 'users.edit', 'users.manage')")
    @Operation(summary = "Lookup person by PINFL + passport",
            description = "Resolve person data (name, birth date, passport, address, ...) from the "
                    + "GUVD/api_mspd gateway to autofill the person create form. Read-only — no DB side-effect. "
                    + "PINFL/passport travel in the POST body (never the URL) to keep them out of access logs.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Person data (data is null if not found)"),
            @ApiResponse(responseCode = "400", description = "Invalid PINFL, or neither document nor birth date supplied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public ResponseEntity<ResponseWrapper<GovPersonDto>> personLookup(
            @Valid @RequestBody PersonLookupRequest request
    ) {
        GovPersonDto person = govPersonLookupService.lookup(request.pinfl(), request.document(), request.birthDate());
        return ResponseEntity.ok(ResponseWrapper.success(person));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('users.view', 'users.manage')")
    @Operation(summary = "List users", description = "Get paginated list of users with optional filters")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users list retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public ResponseEntity<ResponseWrapper<PageResponse<UserAdminResponse>>> getUsers(
            @Parameter(description = "Search by username or full name")
            @RequestParam(required = false) String search,
            @Parameter(description = "Filter by role code", example = "OTM_API")
            @RequestParam(required = false) String role,
            @Parameter(description = "Filter by university entity code", example = "TATU")
            @RequestParam(required = false) String university,
            @Parameter(description = "Filter by enabled status")
            @RequestParam(required = false) Boolean enabled,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field and direction", example = "username,asc")
            @RequestParam(defaultValue = "username,asc") String sort,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID callerId = UUID.fromString(jwt.getSubject());
        Pageable pageable = buildPageable(page, size, sort);

        Page<UserAdminResponse> users = userAdminService.getUsers(
                search, role, university, enabled, pageable, callerId);

        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(users)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('users.view', 'users.manage')")
    @Operation(summary = "Get user by ID", description = "Get full user details including roles and university")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions or scope"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ResponseWrapper<UserAdminResponse>> getUserById(
            @Parameter(description = "User ID (UUID)", required = true)
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID callerId = UUID.fromString(jwt.getSubject());
        UserAdminResponse user = userAdminService.getUserById(id, callerId);
        return ResponseEntity.ok(ResponseWrapper.success(user));
    }

    // =====================================================
    // WRITE ENDPOINTS
    // =====================================================

    @PostMapping
    @PreAuthorize("hasAnyAuthority('users.create', 'users.manage')")
    @Operation(summary = "Create user", description = "Create a new user with roles")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(responseCode = "400", description = "Invalid input or username already exists"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions or scope")
    })
    public ResponseEntity<ResponseWrapper<UserAdminResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID callerId = UUID.fromString(jwt.getSubject());
        UserAdminResponse user = userAdminService.createUser(request, callerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('users.edit', 'users.manage')")
    @Operation(summary = "Update user", description = "Update user profile and roles (username is immutable)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ResponseWrapper<UserAdminResponse>> updateUser(
            @Parameter(description = "User ID (UUID)", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID callerId = UUID.fromString(jwt.getSubject());
        UserAdminResponse user = userAdminService.updateUser(id, request, callerId);
        return ResponseEntity.ok(ResponseWrapper.success(user));
    }

    @PatchMapping("/{id}/password")
    @PreAuthorize("hasAnyAuthority('users.edit', 'users.manage')")
    @Operation(summary = "Change password", description = "Change user password (BCrypt hashed)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password changed"),
            @ApiResponse(responseCode = "400", description = "Invalid password"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "User ID (UUID)", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID callerId = UUID.fromString(jwt.getSubject());
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new uz.hemis.common.exception.BadRequestException("Passwords do not match");
        }
        userAdminService.changePassword(id, request.getNewPassword(), callerId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('users.edit', 'users.manage')")
    @Operation(summary = "Toggle user status", description = "Enable or disable a user account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status toggled"),
            @ApiResponse(responseCode = "400", description = "Cannot disable own account"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ResponseWrapper<UserAdminResponse>> toggleStatus(
            @Parameter(description = "User ID (UUID)", required = true)
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID callerId = UUID.fromString(jwt.getSubject());
        UserAdminResponse user = userAdminService.toggleStatus(id, callerId);
        return ResponseEntity.ok(ResponseWrapper.success(user));
    }

    @PatchMapping("/{id}/unlock")
    @PreAuthorize("hasAnyAuthority('users.edit', 'users.manage')")
    @Operation(summary = "Unlock account", description = "Unlock a locked user account and reset failed attempts")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account unlocked"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ResponseWrapper<UserAdminResponse>> unlockAccount(
            @Parameter(description = "User ID (UUID)", required = true)
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID callerId = UUID.fromString(jwt.getSubject());
        UserAdminResponse user = userAdminService.unlockAccount(id, callerId);
        return ResponseEntity.ok(ResponseWrapper.success(user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('users.delete', 'users.manage')")
    @Operation(summary = "Delete user", description = "Soft delete a user (sets deleted_at timestamp)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted"),
            @ApiResponse(responseCode = "400", description = "Cannot delete own account"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID (UUID)", required = true)
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID callerId = UUID.fromString(jwt.getSubject());
        userAdminService.softDelete(id, callerId);
        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // DICTIONARY ENDPOINTS
    // =====================================================

    @GetMapping("/roles")
    @PreAuthorize("hasAnyAuthority('users.view', 'users.manage')")
    @Operation(summary = "List active roles", description = "Get all active roles for dropdown selection")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Roles list retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ResponseWrapper<List<RoleSummary>>> getActiveRoles(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID callerId = UUID.fromString(jwt.getSubject());
        List<RoleSummary> roles = userAdminService.getActiveRoles(callerId);
        return ResponseEntity.ok(ResponseWrapper.success(roles));
    }

    @GetMapping("/roles/{id}/permissions")
    @PreAuthorize("hasAnyAuthority('users.view', 'users.manage')")
    @Operation(summary = "Get role permissions", description = "Get permissions assigned to a specific role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permissions list retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    public ResponseEntity<ResponseWrapper<List<String>>> getRolePermissions(
            @Parameter(description = "Role ID (UUID)", required = true)
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        List<String> permissions = userAdminService.getRolePermissions(id);
        return ResponseEntity.ok(ResponseWrapper.success(permissions));
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "username", "fullName", "email", "university.code", "enabled", "createdAt", "updatedAt");

    private Pageable buildPageable(int page, int size, String sort) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        if (sort != null && sort.contains(",")) {
            String[] parts = sort.split(",");
            String field = parts[0].trim();
            if (!ALLOWED_SORT_FIELDS.contains(field)) {
                field = "username";
            }
            Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            // Append the PK as a tiebreaker so the sort is a total order — otherwise rows with an
            // equal/null sort key (e.g. duplicate username) can jump between pages during paging.
            return PageRequest.of(safePage, safeSize,
                    Sort.by(direction, field).and(Sort.by(Sort.Direction.ASC, "id")));
        }

        return PageRequest.of(safePage, safeSize,
                Sort.by(Sort.Direction.ASC, "username").and(Sort.by(Sort.Direction.ASC, "id")));
    }
}
