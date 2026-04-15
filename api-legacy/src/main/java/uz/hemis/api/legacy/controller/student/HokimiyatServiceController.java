package uz.hemis.api.legacy.controller.student;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.legacy.HokimiyatLegacyService;

import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Hokimiyat Service Controller - CUBA REST API Compatible
 *
 * <p>OLD-HEMIS: HokimiyatServiceBean.students()</p>
 * <p>Delegates to HokimiyatLegacyService for business logic.</p>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/app/rest/v2/services/hokimiyat")
@Tag(name = "62.Hokimiyat", description = "Hokimiyat uchun ma'lumotlar")
@RequiredArgsConstructor
@Slf4j
public class HokimiyatServiceController {

    private final HokimiyatLegacyService hokimiyatLegacyService;

    /**
     * Get students updated yesterday
     *
     * <p>OLD-HEMIS: HokimiyatServiceBean.students()</p>
     * <p>Native SQL: joins hemishe_e_student with hemishe_e_university and hemishe_e_university_department</p>
     * <p>Filter: update_ts between yesterday and today</p>
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/students")
    @Operation(summary = "Kechagi yangilangan talabalar (hokimiyat)", description = "Kecha yangilangan talabalar - tuman hokimiyatlari uchun")
    public ResponseEntity<?> students() {
        log.info("[CUBA Service] hokimiyat/students");

        Map<String, Object> result = hokimiyatLegacyService.getStudentsUpdatedYesterday();

        return ResponseEntity.ok(result);
    }
}
