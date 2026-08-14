package uz.hemis.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.service.admin.OAuthClientAdminService;
import uz.hemis.service.admin.dto.OAuthClientCreateRequest;
import uz.hemis.service.admin.dto.OAuthClientResponse;
import uz.hemis.service.util.PageResponses;

import java.util.Set;
import java.util.UUID;

/**
 * OTM API-client (oauth_client) admin — machine accounts for the Univer client_credentials API.
 *
 * <p>Separate from {@code /admin/users}: an OTM integration credential lives ONLY in oauth_client,
 * not in the human user table. Ministry-only: {@code oauth-clients.view} / {@code oauth-clients.manage}.</p>
 */
@RestController
@RequestMapping("/api/v1/web/admin/oauth-clients")
@RequiredArgsConstructor
@Tag(name = "OTM API clients", description = "OTM/organization API machine accounts (OAuth2 client_credentials)")
public class OAuthClientController {

    private final OAuthClientAdminService service;

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("clientId", "clientName", "clientType", "isActive", "createdAt", "lastUsedAt");

    @GetMapping
    @PreAuthorize("hasAnyAuthority('oauth-clients.view', 'oauth-clients.manage')")
    @Operation(summary = "List OTM API clients", description = "Filtered + paged oauth_client list")
    public ResponseEntity<ResponseWrapper<PageResponse<OAuthClientResponse>>> list(
            @Parameter(description = "Search by client_id or name") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by client type") @RequestParam(required = false) String clientType,
            @Parameter(description = "Filter by university code") @RequestParam(required = false) String university,
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "clientId,asc") String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        Page<OAuthClientResponse> result = service.getClients(search, clientType, university, active, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(result)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('oauth-clients.view', 'oauth-clients.manage')")
    @Operation(summary = "Get OTM API client by id")
    public ResponseEntity<ResponseWrapper<OAuthClientResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ResponseWrapper.success(service.getClient(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('oauth-clients.manage')")
    @Operation(summary = "Create OTM API client",
            description = "Creates an oauth_client for the Univer client_credentials API. client_id = login, "
                    + "client_secret = password (BCrypt-hashed). Bound to the OTM_API role.")
    public ResponseEntity<ResponseWrapper<OAuthClientResponse>> create(
            @Valid @RequestBody OAuthClientCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseWrapper.success(service.createClient(request)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('oauth-clients.manage')")
    @Operation(summary = "Toggle OTM API client active status")
    public ResponseEntity<ResponseWrapper<OAuthClientResponse>> toggleStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(ResponseWrapper.success(service.toggleStatus(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('oauth-clients.manage')")
    @Operation(summary = "Soft-delete OTM API client")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    private Pageable buildPageable(int page, int size, String sort) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        String field = "clientId";
        Sort.Direction dir = Sort.Direction.ASC;
        if (sort != null && sort.contains(",")) {
            String[] parts = sort.split(",");
            String f = parts[0].trim();
            if (ALLOWED_SORT_FIELDS.contains(f)) {
                field = f;
            }
            if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())) {
                dir = Sort.Direction.DESC;
            }
        }
        // PK tiebreaker → total order across pages
        return PageRequest.of(safePage, safeSize, Sort.by(dir, field).and(Sort.by(Sort.Direction.ASC, "id")));
    }
}
