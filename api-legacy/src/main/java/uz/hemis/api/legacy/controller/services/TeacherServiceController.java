package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.TeacherIdRequest;
import uz.hemis.common.dto.TeacherJobRequest;
import uz.hemis.service.integration.TeacherIntegrationService;

import java.util.Map;

/**
 * Teacher Service Controller
 *
 * <p><strong>URL Pattern:</strong> {@code /services/teacher/*}</p>
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Teacher ID lookup</li>
 *   <li>Add teacher job positions</li>
 *   <li>Teacher data management</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Tag(name = "O'qituvchilar", description = "O'qituvchi ma'lumotlari xizmatlari")
@RestController
@RequestMapping("/services/teacher")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class TeacherServiceController {

    private final TeacherIntegrationService teacherIntegrationService;

    /**
     * Get teacher ID by PINFL or other criteria
     *
     * <p><strong>Endpoint:</strong> POST /services/teacher/id</p>
     *
     * @param request Teacher ID request
     * @return Teacher UUID and basic info
     */
    @Operation(
        summary = "O'qituvchi ID sini olish",
        description = "PINFL yoki boshqa ma'lumotlar orqali o'qituvchi UUID raqamini olish"
    )
    @PostMapping("/id")
    public ResponseEntity<Map<String, Object>> getTeacherId(
        @RequestBody(description = "O'qituvchi qidiruv ma'lumotlari", required = true)
        @org.springframework.web.bind.annotation.RequestBody TeacherIdRequest request
    ) {
        log.info("POST /services/teacher/id - pinfl: {}", request.getPinfl());
        return ResponseEntity.ok(teacherIntegrationService.getTeacherId(request));
    }

    /**
     * Add teacher job position
     *
     * <p><strong>Endpoint:</strong> POST /services/teacher/addJob</p>
     *
     * @param request Teacher job request (position, department, etc.)
     * @return Success status
     */
    @Operation(
        summary = "Xodim lavozimini qo'shish",
        description = "O'qituvchiga yangi lavozim yoki ish joyini qo'shish"
    )
    @PostMapping("/addJob")
    public ResponseEntity<Map<String, Object>> addJob(
        @RequestBody(description = "Lavozim ma'lumotlari", required = true)
        @org.springframework.web.bind.annotation.RequestBody TeacherJobRequest request
    ) {
        log.info("POST /services/teacher/addJob - teacherId: {}, position: {}", 
            request.getTeacherId(), request.getPosition());
        return ResponseEntity.ok(teacherIntegrationService.addJob(request));
    }
}
