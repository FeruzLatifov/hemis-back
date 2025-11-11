package uz.hemis.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.GroupDto;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.domain.service.GroupService;

import java.util.UUID;

@RestController
@RequestMapping("/app/rest/v2/groups")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Group", description = "Group management API")
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    @Operation(summary = "Get all groups")
    public ResponseEntity<ResponseWrapper<PageResponse<GroupDto>>> getAllGroups(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(groupService.findAll(pageable))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get group by ID")
    public ResponseEntity<ResponseWrapper<GroupDto>> getGroupById(@PathVariable UUID id) {
        return ResponseEntity.ok(ResponseWrapper.success(groupService.findById(id)));
    }

    @GetMapping("/university/{code}")
    @Operation(summary = "Get groups by university")
    public ResponseEntity<ResponseWrapper<PageResponse<GroupDto>>> getGroupsByUniversity(
            @PathVariable String code,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(groupService.findByUniversity(code, pageable))));
    }

    @GetMapping("/specialty/{id}")
    @Operation(summary = "Get groups by specialty")
    public ResponseEntity<ResponseWrapper<PageResponse<GroupDto>>> getGroupsBySpecialty(
            @PathVariable UUID id,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(groupService.findBySpecialty(id, pageable))));
    }

    @GetMapping("/university/{code}/count")
    @Operation(summary = "Count groups by university")
    public ResponseEntity<ResponseWrapper<Long>> countGroupsByUniversity(@PathVariable String code) {
        return ResponseEntity.ok(ResponseWrapper.success(groupService.countByUniversity(code)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    @Operation(summary = "Create group")
    public ResponseEntity<ResponseWrapper<GroupDto>> createGroup(@Valid @RequestBody GroupDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(groupService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    @Operation(summary = "Update group")
    public ResponseEntity<ResponseWrapper<GroupDto>> updateGroup(@PathVariable UUID id, @Valid @RequestBody GroupDto dto) {
        return ResponseEntity.ok(ResponseWrapper.success(groupService.update(id, dto)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft delete group")
    public ResponseEntity<ResponseWrapper<Void>> deactivateGroup(@PathVariable UUID id) {
        groupService.softDelete(id);
        return ResponseEntity.ok(ResponseWrapper.success(null, "Group deactivated successfully"));
    }
}
