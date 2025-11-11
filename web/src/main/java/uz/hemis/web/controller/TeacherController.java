package uz.hemis.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
import uz.hemis.service.TeacherService;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.common.dto.TeacherDto;

import java.util.List;
import java.util.UUID;

/**
 * Teacher REST Controller - API Layer
 *
 * <p><strong>CRITICAL - Legacy URL Preservation:</strong></p>
 * <ul>
 *   <li>Base URL: /app/rest/v2/teachers (unchanged from CUBA)</li>
 *   <li>200+ universities depend on this API contract</li>
 *   <li>Response format must match legacy (ResponseWrapper + PageResponse)</li>
 * </ul>
 *
 * <p><strong>Endpoints:</strong></p>
 * <ul>
 *   <li>GET /app/rest/v2/teachers - List all teachers (paginated)</li>
 *   <li>GET /app/rest/v2/teachers/{id} - Find by ID</li>
 *   <li>GET /app/rest/v2/teachers/university/{code} - Filter by university</li>
 *   <li>GET /app/rest/v2/teachers/search - Search by name</li>
 *   <li>POST /app/rest/v2/teachers - Create new teacher</li>
 *   <li>PUT /app/rest/v2/teachers/{id} - Update existing teacher</li>
 *   <li>PATCH /app/rest/v2/teachers/{id} - Partial update</li>
 *   <li>❌ NO DELETE endpoint (NDG - Non-Deletion Guarantee)</li>
 * </ul>
 *
 * <p><strong>Error Handling:</strong></p>
 * <p>All exceptions handled by {@link uz.hemis.app.exception.GlobalExceptionHandler}</p>
 *
 * @since 1.0.0
 */
@Tag(name = "Teachers")
@RestController
@RequestMapping("/app/rest/v2/teachers")
@RequiredArgsConstructor
@Slf4j
public class TeacherController {

    private final TeacherService teacherService;

    // =====================================================
    // Read Operations
    // =====================================================

    /**
     * Get all teachers (paginated)
     *
     * <p><strong>Legacy URL:</strong> GET /app/rest/v2/teachers</p>
     *
     * <p><strong>Query Parameters:</strong></p>
     * <ul>
     *   <li>page - page number (default: 0)</li>
     *   <li>size - page size (default: 20)</li>
     *   <li>sort - sorting (e.g., lastname,asc)</li>
     * </ul>
     *
     * @param pageable pagination parameters
     * @return ResponseWrapper with PageResponse<TeacherDto>
     */
    @Operation(
            summary = "Get all teachers (paginated)",
            description = """
                Barcha o'qituvchilarni ro'yxatini olish (sahifalangan).

                **Query Parameters:**
                - `page` - Sahifa raqami (default: 0)
                - `size` - Sahifadagi elementlar soni (default: 20, max: 100)
                - `sort` - Tartiblash (default: `lastname,asc`)

                **Filterlash:**
                - Universitet bo'yicha: `/university/{code}`
                - Akademik daraja bo'yicha: `/degree/{degreeCode}`
                - Akademik unvon bo'yicha: `/rank/{rankCode}`
                - Qidirish: `/search?name={name}`

                **Misol so'rovlar:**
                - Birinchi sahifa: `/app/rest/v2/teachers?page=0&size=20`
                - Familiya bo'yicha tartiblangan: `/app/rest/v2/teachers?sort=lastname,asc`
                """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "O'qituvchilar muvaffaqiyatli qaytarildi",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapper.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    summary = "Muvaffaqiyatli javob",
                                    value = """
                                    {
                                      "success": true,
                                      "message": "Teachers retrieved successfully",
                                      "data": {
                                        "content": [
                                          {
                                            "id": "660e8400-e29b-41d4-a716-446655440000",
                                            "code": "TCH2024001",
                                            "_university": "UNI001",
                                            "firstname": "Alisher",
                                            "lastname": "Navoiy",
                                            "fathername": "Akbarovich",
                                            "pinfl": "22345678901234",
                                            "_academic_degree": "DSc",
                                            "_academic_rank": "Professor",
                                            "_employee_status": "WORKING",
                                            "createTs": "2024-01-15T10:30:00Z",
                                            "updateTs": "2024-01-15T10:30:00Z"
                                          }
                                        ],
                                        "page": 0,
                                        "size": 20,
                                        "totalElements": 85,
                                        "totalPages": 5,
                                        "first": true,
                                        "last": false
                                      },
                                      "timestamp": "2025-11-06T10:30:00Z"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Avtorizatsiya xatosi",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Unauthorized Error",
                                    value = """
                                    {
                                      "success": false,
                                      "status": 401,
                                      "error": "UNAUTHORIZED",
                                      "message": "Invalid or expired JWT token",
                                      "timestamp": "2025-11-06T10:30:00Z"
                                    }
                                    """
                            )
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<ResponseWrapper<PageResponse<TeacherDto>>> getAllTeachers(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            @PageableDefault(size = 20, sort = "lastname", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        log.info("GET /app/rest/v2/teachers - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<TeacherDto> teachers = teacherService.findAll(pageable);
        PageResponse<TeacherDto> pageResponse = PageResponse.of(teachers);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get teacher by ID
     *
     * <p><strong>Legacy URL:</strong> GET /app/rest/v2/teachers/{id}</p>
     *
     * @param id teacher ID (UUID)
     * @return ResponseWrapper with TeacherDto
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<TeacherDto>> getTeacherById(@PathVariable UUID id) {
        log.info("GET /app/rest/v2/teachers/{}", id);

        TeacherDto teacher = teacherService.findById(id);
        return ResponseEntity.ok(ResponseWrapper.success(teacher));
    }

    /**
     * Get teachers by university code (paginated)
     *
     * <p><strong>Legacy URL:</strong> GET /app/rest/v2/teachers/university/{code}</p>
     *
     * @param code university code
     * @param pageable pagination parameters
     * @return ResponseWrapper with PageResponse<TeacherDto>
     */
    @GetMapping("/university/{code}")
    public ResponseEntity<ResponseWrapper<PageResponse<TeacherDto>>> getTeachersByUniversity(
            @PathVariable String code,
            @PageableDefault(size = 20, sort = "lastname") Pageable pageable
    ) {
        log.info("GET /app/rest/v2/teachers/university/{}", code);

        Page<TeacherDto> teachers = teacherService.findByUniversity(code, pageable);
        PageResponse<TeacherDto> pageResponse = PageResponse.of(teachers);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get all teachers by university (no pagination)
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/teachers/university/{code}/all</p>
     *
     * @param code university code
     * @return ResponseWrapper with List<TeacherDto>
     */
    @GetMapping("/university/{code}/all")
    public ResponseEntity<ResponseWrapper<List<TeacherDto>>> getAllTeachersByUniversity(@PathVariable String code) {
        log.info("GET /app/rest/v2/teachers/university/{}/all", code);

        List<TeacherDto> teachers = teacherService.findAllByUniversity(code);
        return ResponseEntity.ok(ResponseWrapper.success(teachers));
    }

    /**
     * Search teachers by lastname (partial match, case-insensitive)
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/teachers/search/lastname?q={lastname}</p>
     *
     * @param q lastname search term (partial)
     * @param pageable pagination parameters
     * @return ResponseWrapper with PageResponse<TeacherDto>
     */
    @GetMapping("/search/lastname")
    public ResponseEntity<ResponseWrapper<PageResponse<TeacherDto>>> searchByLastname(
            @RequestParam String q,
            @PageableDefault(size = 20, sort = "lastname") Pageable pageable
    ) {
        log.info("GET /app/rest/v2/teachers/search/lastname?q={}", q);

        Page<TeacherDto> teachers = teacherService.findByLastname(q, pageable);
        PageResponse<TeacherDto> pageResponse = PageResponse.of(teachers);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Search teachers by name (partial match on any name field, case-insensitive)
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/teachers/search?name={name}</p>
     *
     * @param name search term (applied to firstname, lastname, fathername)
     * @param pageable pagination parameters
     * @return ResponseWrapper with PageResponse<TeacherDto>
     */
    @GetMapping("/search")
    public ResponseEntity<ResponseWrapper<PageResponse<TeacherDto>>> searchTeachers(
            @RequestParam String name,
            @PageableDefault(size = 20, sort = "lastname") Pageable pageable
    ) {
        log.info("GET /app/rest/v2/teachers/search?name={}", name);

        Page<TeacherDto> teachers = teacherService.findByName(name, pageable);
        PageResponse<TeacherDto> pageResponse = PageResponse.of(teachers);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get teachers by academic degree
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/teachers/degree/{degreeCode}</p>
     *
     * @param degreeCode academic degree code
     * @param pageable pagination parameters
     * @return ResponseWrapper with PageResponse<TeacherDto>
     */
    @GetMapping("/degree/{degreeCode}")
    public ResponseEntity<ResponseWrapper<PageResponse<TeacherDto>>> getTeachersByDegree(
            @PathVariable String degreeCode,
            @PageableDefault(size = 20, sort = "lastname") Pageable pageable
    ) {
        log.info("GET /app/rest/v2/teachers/degree/{}", degreeCode);

        Page<TeacherDto> teachers = teacherService.findByAcademicDegree(degreeCode, pageable);
        PageResponse<TeacherDto> pageResponse = PageResponse.of(teachers);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get teachers by academic rank
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/teachers/rank/{rankCode}</p>
     *
     * @param rankCode academic rank code
     * @param pageable pagination parameters
     * @return ResponseWrapper with PageResponse<TeacherDto>
     */
    @GetMapping("/rank/{rankCode}")
    public ResponseEntity<ResponseWrapper<PageResponse<TeacherDto>>> getTeachersByRank(
            @PathVariable String rankCode,
            @PageableDefault(size = 20, sort = "lastname") Pageable pageable
    ) {
        log.info("GET /app/rest/v2/teachers/rank/{}", rankCode);

        Page<TeacherDto> teachers = teacherService.findByAcademicRank(rankCode, pageable);
        PageResponse<TeacherDto> pageResponse = PageResponse.of(teachers);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get professors by university
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/teachers/university/{code}/professors</p>
     *
     * @param code university code
     * @param pageable pagination parameters
     * @return ResponseWrapper with PageResponse<TeacherDto>
     */
    @GetMapping("/university/{code}/professors")
    public ResponseEntity<ResponseWrapper<PageResponse<TeacherDto>>> getProfessorsByUniversity(
            @PathVariable String code,
            @PageableDefault(size = 20, sort = "lastname") Pageable pageable
    ) {
        log.info("GET /app/rest/v2/teachers/university/{}/professors", code);

        Page<TeacherDto> professors = teacherService.findProfessorsByUniversity(code, pageable);
        PageResponse<TeacherDto> pageResponse = PageResponse.of(professors);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Count teachers by university
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/teachers/university/{code}/count</p>
     *
     * @param code university code
     * @return ResponseWrapper with count
     */
    @GetMapping("/university/{code}/count")
    public ResponseEntity<ResponseWrapper<Long>> countTeachersByUniversity(@PathVariable String code) {
        log.info("GET /app/rest/v2/teachers/university/{}/count", code);

        long count = teacherService.countByUniversity(code);
        return ResponseEntity.ok(ResponseWrapper.success(count));
    }

    /**
     * Count professors by university
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/teachers/university/{code}/professors/count</p>
     *
     * @param code university code
     * @return ResponseWrapper with count
     */
    @GetMapping("/university/{code}/professors/count")
    public ResponseEntity<ResponseWrapper<Long>> countProfessorsByUniversity(@PathVariable String code) {
        log.info("GET /app/rest/v2/teachers/university/{}/professors/count", code);

        long count = teacherService.countProfessorsByUniversity(code);
        return ResponseEntity.ok(ResponseWrapper.success(count));
    }

    // =====================================================
    // Write Operations (ADMIN/UNIVERSITY_ADMIN only)
    // =====================================================

    /**
     * Create new teacher
     *
     * <p><strong>Legacy URL:</strong> POST /app/rest/v2/teachers</p>
     *
     * <p><strong>Authorization:</strong> ADMIN or UNIVERSITY_ADMIN</p>
     *
     * @param dto teacher DTO
     * @return ResponseWrapper with created TeacherDto
     */
    @Operation(
            summary = "Create new teacher",
            description = """
                Yangi o'qituvchi yaratish.

                **Majburiy maydonlar:**
                - `code` - O'qituvchi kodi (unique)
                - `_university` - Universitet kodi
                - `firstname` - Ism
                - `lastname` - Familiya
                - `pinfl` - PINFL (14 raqam, unique)
                - `_employee_status` - Xodim statusi (WORKING, FIRED, RETIRED, etc.)

                **Ixtiyoriy maydonlar:**
                - `fathername` - Otasining ismi
                - `_academic_degree` - Akademik daraja (DSc, PhD, etc.)
                - `_academic_rank` - Akademik unvon (Professor, Docent, etc.)
                - `_department` - Kafedra kodi
                - `_position` - Lavozim kodi

                **Xavfsizlik:**
                - Faqat ADMIN yoki UNIVERSITY_ADMIN ro'yxatdagi foydalanuvchilar o'qituvchi qo'sha oladi
                """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "O'qituvchi muvaffaqiyatli yaratildi",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapper.class),
                            examples = @ExampleObject(
                                    name = "Created Teacher",
                                    summary = "Yaratilgan o'qituvchi",
                                    value = """
                                    {
                                      "success": true,
                                      "message": "Teacher created successfully",
                                      "data": {
                                        "id": "660e8400-e29b-41d4-a716-446655440020",
                                        "code": "TCH2025001",
                                        "_university": "UNI001",
                                        "firstname": "Bobur",
                                        "lastname": "Tursunov",
                                        "fathername": "Rahmanov",
                                        "pinfl": "42109876543210",
                                        "_academic_degree": "PhD",
                                        "_academic_rank": "Docent",
                                        "_employee_status": "WORKING",
                                        "_department": "DEP001",
                                        "_position": "POS001",
                                        "createTs": "2025-11-06T10:30:00Z",
                                        "updateTs": "2025-11-06T10:30:00Z",
                                        "deleteTs": null
                                      },
                                      "timestamp": "2025-11-06T10:30:00Z"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validatsiya xatosi",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Validation Error",
                                    value = """
                                    {
                                      "success": false,
                                      "status": 400,
                                      "error": "VALIDATION_ERROR",
                                      "message": "Validation failed",
                                      "errors": [
                                        {
                                          "field": "pinfl",
                                          "message": "PINFL must be exactly 14 digits",
                                          "rejectedValue": "12345"
                                        }
                                      ],
                                      "timestamp": "2025-11-06T10:30:00Z"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict - PINFL yoki Code allaqachon mavjud",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Duplicate Error",
                                    value = """
                                    {
                                      "success": false,
                                      "status": 409,
                                      "error": "DUPLICATE_RESOURCE",
                                      "message": "Teacher with PINFL '22345678901234' already exists",
                                      "timestamp": "2025-11-06T10:30:00Z"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Ruxsat yo'q",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Forbidden Error",
                                    value = """
                                    {
                                      "success": false,
                                      "status": 403,
                                      "error": "FORBIDDEN",
                                      "message": "Access denied. Required role: ADMIN or UNIVERSITY_ADMIN",
                                      "timestamp": "2025-11-06T10:30:00Z"
                                    }
                                    """
                            )
                    )
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<TeacherDto>> createTeacher(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Yangi o'qituvchi ma'lumotlari",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TeacherDto.class),
                            examples = @ExampleObject(
                                    name = "New Teacher Request",
                                    summary = "Yangi o'qituvchi yaratish so'rovi",
                                    value = """
                                    {
                                      "code": "TCH2025001",
                                      "_university": "UNI001",
                                      "firstname": "Bobur",
                                      "lastname": "Tursunov",
                                      "fathername": "Rahmanov",
                                      "pinfl": "42109876543210",
                                      "_academic_degree": "PhD",
                                      "_academic_rank": "Docent",
                                      "_employee_status": "WORKING",
                                      "_department": "DEP001",
                                      "_position": "POS001"
                                    }
                                    """
                            )
                    )
            )
            @Valid @RequestBody TeacherDto dto) {
        log.info("POST /app/rest/v2/teachers - {} {}", dto.getFirstname(), dto.getLastname());

        TeacherDto created = teacherService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(created));
    }

    /**
     * Update existing teacher
     *
     * <p><strong>Legacy URL:</strong> PUT /app/rest/v2/teachers/{id}</p>
     *
     * <p><strong>Authorization:</strong> ADMIN or UNIVERSITY_ADMIN</p>
     *
     * @param id teacher ID
     * @param dto teacher DTO
     * @return ResponseWrapper with updated TeacherDto
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<TeacherDto>> updateTeacher(
            @PathVariable UUID id,
            @Valid @RequestBody TeacherDto dto
    ) {
        log.info("PUT /app/rest/v2/teachers/{}", id);

        TeacherDto updated = teacherService.update(id, dto);
        return ResponseEntity.ok(ResponseWrapper.success(updated));
    }

    /**
     * Partial update (PATCH) - only update non-null fields
     *
     * <p><strong>Legacy URL:</strong> PATCH /app/rest/v2/teachers/{id}</p>
     *
     * <p><strong>Authorization:</strong> ADMIN or UNIVERSITY_ADMIN</p>
     *
     * @param id teacher ID
     * @param dto teacher DTO with fields to update
     * @return ResponseWrapper with updated TeacherDto
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<TeacherDto>> partialUpdateTeacher(
            @PathVariable UUID id,
            @RequestBody TeacherDto dto
    ) {
        log.info("PATCH /app/rest/v2/teachers/{}", id);

        TeacherDto updated = teacherService.partialUpdate(id, dto);
        return ResponseEntity.ok(ResponseWrapper.success(updated));
    }

    // =====================================================
    // NO DELETE ENDPOINT
    // =====================================================
    // Physical DELETE operations are PROHIBITED (NDG).
    // Soft delete is handled via service layer if needed.
    // Any DELETE request will return 405 Method Not Allowed.
    // =====================================================
}
