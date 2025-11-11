package uz.hemis.web.controller;

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
import uz.hemis.service.GroupService;
import uz.hemis.common.dto.GroupDto;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.common.dto.ResponseWrapper;

import java.util.List;
import java.util.UUID;

@Tag(name = "Groups")
@RestController
@RequestMapping("/app/rest/v2/groups")
@RequiredArgsConstructor
@Slf4j
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    public ResponseEntity<ResponseWrapper<PageResponse<GroupDto>>> getAllGroups(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<GroupDto> groups = groupService.findAll(pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(groups)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<GroupDto>> getGroupById(@PathVariable UUID id) {
        GroupDto group = groupService.findById(id);
        return ResponseEntity.ok(ResponseWrapper.success(group));
    }

    @GetMapping(params = "university")
    public ResponseEntity<ResponseWrapper<PageResponse<GroupDto>>> getGroupsByUniversity(
            @RequestParam("university") String universityCode,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<GroupDto> groups = groupService.findByUniversity(universityCode, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(groups)));
    }

    @GetMapping(params = "specialty")
    public ResponseEntity<ResponseWrapper<PageResponse<GroupDto>>> getGroupsBySpecialty(
            @RequestParam("specialty") UUID specialtyId,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<GroupDto> groups = groupService.findBySpecialty(specialtyId, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(groups)));
    }

    @GetMapping(params = "countByUniversity")
    public ResponseEntity<ResponseWrapper<Long>> countGroupsByUniversity(
            @RequestParam("countByUniversity") String universityCode
    ) {
        long count = groupService.countByUniversity(universityCode);
        return ResponseEntity.ok(ResponseWrapper.success(count));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<GroupDto>> createGroup(@Valid @RequestBody GroupDto groupDto) {
        GroupDto created = groupService.create(groupDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<GroupDto>> updateGroup(
            @PathVariable UUID id,
            @Valid @RequestBody GroupDto groupDto
    ) {
        GroupDto updated = groupService.update(id, groupDto);
        return ResponseEntity.ok(ResponseWrapper.success(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<Void>> deleteGroup(@PathVariable UUID id) {
        groupService.softDelete(id);
        return ResponseEntity.ok(ResponseWrapper.success(null));
    }
}
