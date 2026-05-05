package uz.hemis.api.legacy.controller.university;

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
import uz.hemis.service.university.GroupService;
import uz.hemis.common.dto.university.GroupDto;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.service.util.PageResponses;
import uz.hemis.common.dto.ResponseWrapper;

import java.util.UUID;

@Tag(name = "51.Guruhlar", description = "Guruhlar boshqaruvi")
@RestController
@RequestMapping("/app/rest/v2/groups")
@RequiredArgsConstructor
@Slf4j
public class GroupController {

    private final GroupService groupService;

    @Operation(summary = "get all groups")
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<ResponseWrapper<PageResponse<GroupDto>>> getAllGroups(
            @PageableDefault(size = 20, sort = "groupName", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<GroupDto> groups = groupService.findAll(pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(groups)));
    }

    @Operation(summary = "get group by id")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<GroupDto>> getGroupById(@PathVariable UUID id) {
        GroupDto group = groupService.findById(id);
        return ResponseEntity.ok(ResponseWrapper.success(group));
    }

    @Operation(summary = "Yangi guruh yaratish")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OTM_API')")
    public ResponseEntity<ResponseWrapper<GroupDto>> createGroup(@Valid @RequestBody GroupDto groupDto) {
        GroupDto created = groupService.create(groupDto);
        return ResponseEntity.ok(ResponseWrapper.success(created));
    }

    @Operation(summary = "update group")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OTM_API')")
    public ResponseEntity<ResponseWrapper<GroupDto>> updateGroup(
            @PathVariable UUID id,
            @Valid @RequestBody GroupDto groupDto
    ) {
        GroupDto updated = groupService.update(id, groupDto);
        return ResponseEntity.ok(ResponseWrapper.success(updated));
    }

    @Operation(summary = "delete group")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<Void>> deleteGroup(@PathVariable UUID id) {
        groupService.delete(id);
        return ResponseEntity.ok(ResponseWrapper.success(null));
    }
}
