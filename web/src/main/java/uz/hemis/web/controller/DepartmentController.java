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
import uz.hemis.service.DepartmentService;
import uz.hemis.common.dto.DepartmentDto;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.common.dto.ResponseWrapper;

import java.util.List;
import java.util.UUID;

/**
 * Department REST Controller - API Layer
 *
 * <p><strong>CRITICAL - Legacy URL Preservation:</strong></p>
 * <ul>
 *   <li>Base URL: /app/rest/v2/departments (unchanged from CUBA)</li>
 *   <li>Legacy URL: /app/rest/cathedra/* (preserved for compatibility)</li>
 *   <li>200+ universities depend on this API contract</li>
 *   <li>Response format must match legacy (ResponseWrapper + PageResponse)</li>
 * </ul>
 *
 * <p><strong>Endpoints:</strong></p>
 * <ul>
 *   <li>GET /app/rest/v2/departments - List all departments (paginated)</li>
 *   <li>GET /app/rest/v2/departments/{id} - Find by ID</li>
 *   <li>GET /app/rest/v2/departments?university={code} - Filter by university</li>
 *   <li>GET /app/rest/v2/departments?faculty={id} - Filter by faculty</li>
 *   <li>POST /app/rest/v2/departments - Create new department</li>
 *   <li>PUT /app/rest/v2/departments/{id} - Update existing department</li>
 *   <li>❌ NO DELETE endpoint (NDG - Non-Deletion Guarantee)</li>
 * </ul>
 *
 * <p><strong>Error Handling:</strong></p>
 * <p>All exceptions handled by {@link uz.hemis.app.exception.GlobalExceptionHandler}</p>
 *
 * @since 1.0.0
 */
@Tag(name = "Departments")
@RestController
@RequestMapping("/app/rest/v2/departments")
@RequiredArgsConstructor
@Slf4j
public class DepartmentController {

    private final DepartmentService departmentService;

    // =====================================================
    // Read Operations
    // =====================================================

    /**
     * Get all departments (paginated)
     *
     * <p><strong>Legacy URL:</strong> GET /app/rest/v2/departments</p>
     *
     * @param pageable pagination parameters
     * @return paginated list of departments
     */
    @GetMapping
    public ResponseEntity<ResponseWrapper<PageResponse<DepartmentDto>>> getAllDepartments(
            @PageableDefault(size = 20, sort = "departmentName", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        log.debug("GET /app/rest/v2/departments - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<DepartmentDto> departments = departmentService.findAll(pageable);
        PageResponse<DepartmentDto> pageResponse = PageResponse.of(departments);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get department by ID
     *
     * <p><strong>Legacy URL:</strong> GET /app/rest/v2/departments/{id}</p>
     *
     * @param id department ID (UUID)
     * @return department details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<DepartmentDto>> getDepartmentById(@PathVariable UUID id) {
        log.debug("GET /app/rest/v2/departments/{}", id);

        DepartmentDto department = departmentService.findById(id);

        return ResponseEntity.ok(ResponseWrapper.success(department));
    }

    /**
     * Get department by code
     *
     * <p><strong>Legacy URL:</strong> GET /app/rest/v2/departments/code/{code}</p>
     *
     * @param code department code
     * @return department details
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ResponseWrapper<DepartmentDto>> getDepartmentByCode(@PathVariable String code) {
        log.debug("GET /app/rest/v2/departments/code/{}", code);

        DepartmentDto department = departmentService.findByCode(code);

        return ResponseEntity.ok(ResponseWrapper.success(department));
    }

    /**
     * Get departments by university code
     *
     * <p><strong>Legacy URL:</strong> GET /app/rest/v2/departments?university={code}</p>
     *
     * @param universityCode university code
     * @param pageable pagination parameters
     * @return paginated list of departments for the university
     */
    @GetMapping(params = "university")
    public ResponseEntity<ResponseWrapper<PageResponse<DepartmentDto>>> getDepartmentsByUniversity(
            @RequestParam("university") String universityCode,
            @PageableDefault(size = 20, sort = "departmentName", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        log.debug("GET /app/rest/v2/departments?university={}", universityCode);

        Page<DepartmentDto> departments = departmentService.findByUniversity(universityCode, pageable);
        PageResponse<DepartmentDto> pageResponse = PageResponse.of(departments);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get active departments by university
     *
     * <p><strong>Legacy URL:</strong> GET /app/rest/v2/departments/active?university={code}</p>
     *
     * @param universityCode university code
     * @return list of active departments
     */
    @GetMapping(value = "/active", params = "university")
    public ResponseEntity<ResponseWrapper<List<DepartmentDto>>> getActiveDepartmentsByUniversity(
            @RequestParam("university") String universityCode
    ) {
        log.debug("GET /app/rest/v2/departments/active?university={}", universityCode);

        List<DepartmentDto> departments = departmentService.findActiveByUniversity(universityCode);

        return ResponseEntity.ok(ResponseWrapper.success(departments));
    }

    /**
     * Get departments by faculty
     *
     * <p><strong>Legacy URL:</strong> GET /app/rest/v2/departments?faculty={id}</p>
     *
     * @param facultyId faculty UUID
     * @param pageable pagination parameters
     * @return paginated list of departments for the faculty
     */
    @GetMapping(params = "faculty")
    public ResponseEntity<ResponseWrapper<PageResponse<DepartmentDto>>> getDepartmentsByFaculty(
            @RequestParam("faculty") UUID facultyId,
            @PageableDefault(size = 20, sort = "departmentName", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        log.debug("GET /app/rest/v2/departments?faculty={}", facultyId);

        Page<DepartmentDto> departments = departmentService.findByFaculty(facultyId, pageable);
        PageResponse<DepartmentDto> pageResponse = PageResponse.of(departments);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get active departments by faculty
     *
     * <p><strong>Legacy URL:</strong> GET /app/rest/v2/departments/active?faculty={id}</p>
     *
     * @param facultyId faculty UUID
     * @return list of active departments
     */
    @GetMapping(value = "/active", params = "faculty")
    public ResponseEntity<ResponseWrapper<List<DepartmentDto>>> getActiveDepartmentsByFaculty(
            @RequestParam("faculty") UUID facultyId
    ) {
        log.debug("GET /app/rest/v2/departments/active?faculty={}", facultyId);

        List<DepartmentDto> departments = departmentService.findActiveByFaculty(facultyId);

        return ResponseEntity.ok(ResponseWrapper.success(departments));
    }

    /**
     * Get department by head (department head teacher)
     *
     * <p><strong>Legacy URL:</strong> GET /app/rest/v2/departments/head/{headId}</p>
     *
     * @param headId teacher UUID
     * @return department details
     */
    @GetMapping("/head/{headId}")
    public ResponseEntity<ResponseWrapper<DepartmentDto>> getDepartmentByHead(@PathVariable UUID headId) {
        log.debug("GET /app/rest/v2/departments/head/{}", headId);

        DepartmentDto department = departmentService.findByHead(headId);

        return ResponseEntity.ok(ResponseWrapper.success(department));
    }

    // =====================================================
    // Write Operations
    // =====================================================

    /**
     * Create new department
     *
     * <p><strong>Legacy URL:</strong> POST /app/rest/v2/departments</p>
     *
     * <p><strong>Security:</strong> Requires ROLE_ADMIN or ROLE_UNIVERSITY_ADMIN</p>
     *
     * @param departmentDto department data
     * @return created department (HTTP 201 CREATED)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<DepartmentDto>> createDepartment(
            @Valid @RequestBody DepartmentDto departmentDto
    ) {
        log.info("POST /app/rest/v2/departments - creating department: {}", departmentDto.getDepartmentCode());

        DepartmentDto created = departmentService.create(departmentDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseWrapper.success(created));
    }

    /**
     * Update existing department (full update)
     *
     * <p><strong>Legacy URL:</strong> PUT /app/rest/v2/departments/{id}</p>
     *
     * <p><strong>Security:</strong> Requires ROLE_ADMIN or ROLE_UNIVERSITY_ADMIN</p>
     *
     * @param id department ID (UUID)
     * @param departmentDto department data
     * @return updated department
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<DepartmentDto>> updateDepartment(
            @PathVariable UUID id,
            @Valid @RequestBody DepartmentDto departmentDto
    ) {
        log.info("PUT /app/rest/v2/departments/{} - updating department", id);

        DepartmentDto updated = departmentService.update(id, departmentDto);

        return ResponseEntity.ok(ResponseWrapper.success(updated));
    }

    // =====================================================
    // NOTE: NO DELETE ENDPOINT
    // =====================================================
    // Physical DELETE is prohibited (NDG - Non-Deletion Guarantee)
    // Soft delete is handled internally by admin-api module
    // =====================================================
}
