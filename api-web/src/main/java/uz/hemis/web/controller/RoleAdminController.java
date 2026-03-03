package uz.hemis.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.service.admin.RoleAdminService;
import uz.hemis.service.admin.dto.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/web/admin/roles")
@Tag(name = "Role Management", description = "Admin CRUD operations for role management")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class RoleAdminController {

    private final RoleAdminService roleAdminService;

    @GetMapping
    @PreAuthorize("hasAuthority('roles.manage')")
    @Operation(summary = "List roles", description = "Get paginated list of roles with optional search")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Roles list retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ResponseWrapper<Page<RoleResponse>>> getRoles(
            @Parameter(description = "Search by code or name")
            @RequestParam(required = false) String search,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field and direction")
            @RequestParam(defaultValue = "name,asc") String sort
    ) {
        Pageable pageable = buildPageable(page, size, sort);
        Page<RoleResponse> roles = roleAdminService.getAll(search, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(roles));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('roles.manage')")
    @Operation(summary = "Get role by ID", description = "Get role details including permissions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role found"),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    public ResponseEntity<ResponseWrapper<RoleResponse>> getRoleById(
            @PathVariable UUID id
    ) {
        RoleResponse role = roleAdminService.getById(id);
        return ResponseEntity.ok(ResponseWrapper.success(role));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('roles.manage')")
    @Operation(summary = "Create role", description = "Create a new custom role")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Role created"),
            @ApiResponse(responseCode = "400", description = "Invalid input or code already exists")
    })
    public ResponseEntity<ResponseWrapper<RoleResponse>> createRole(
            @Valid @RequestBody RoleCreateRequest request
    ) {
        RoleResponse role = roleAdminService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(role));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('roles.manage')")
    @Operation(summary = "Update role", description = "Update role name, description, and permissions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role updated"),
            @ApiResponse(responseCode = "400", description = "Cannot modify system role"),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    public ResponseEntity<ResponseWrapper<RoleResponse>> updateRole(
            @PathVariable UUID id,
            @Valid @RequestBody RoleUpdateRequest request
    ) {
        RoleResponse role = roleAdminService.update(id, request);
        return ResponseEntity.ok(ResponseWrapper.success(role));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('roles.manage')")
    @Operation(summary = "Delete role", description = "Soft delete a custom role")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Role deleted"),
            @ApiResponse(responseCode = "400", description = "Cannot delete system role or role with users"),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    public ResponseEntity<Void> deleteRole(
            @PathVariable UUID id
    ) {
        roleAdminService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('roles.manage')")
    @Operation(summary = "List all permissions", description = "Get all available permissions grouped by category")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permissions list retrieved")
    })
    public ResponseEntity<ResponseWrapper<List<RoleResponse.PermissionResponse>>> getAllPermissions() {
        List<RoleResponse.PermissionResponse> permissions = roleAdminService.getAllPermissions();
        return ResponseEntity.ok(ResponseWrapper.success(permissions));
    }

    // ── Helpers ──

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("code", "name", "roleType", "createdAt");

    private Pageable buildPageable(int page, int size, String sort) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        if (sort != null && sort.contains(",")) {
            String[] parts = sort.split(",");
            String field = parts[0].trim();
            if (!ALLOWED_SORT_FIELDS.contains(field)) {
                field = "name";
            }
            Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            return PageRequest.of(safePage, safeSize, Sort.by(direction, field));
        }

        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, "name"));
    }
}
