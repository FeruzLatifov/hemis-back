package uz.hemis.api.legacy.controller.finance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Legal Entity Service Controller - OLD-HEMIS Compatible
 *
 * <p>Yuridik shaxs bank rekvizitlarini olish</p>
 *
 * <p>Old-hemis: LegalEntityServiceBean.bankRequisites()</p>
 *
 * @since 2.0.0
 */
@Tag(name = "08.Yuridik shaxs", description = "Yuridik shaxs ma'lumotlari")
@RestController
@RequestMapping("/app/rest/v2/services/legalentity")
@RequiredArgsConstructor
@Slf4j
public class LegalEntityServiceController {

    /**
     * Bank rekvizitlarini INN bo'yicha olish
     *
     * <p>Old-hemis: LegalEntityServiceBean.bankRequisites(String inn)</p>
     * <p>Tashqi API: https://api-mspd.edu.uz/legalentity/legalentity-bankrequisites/</p>
     */
    @GetMapping("/bankRequisites")
    @Operation(
            summary = "Bank rekvizitlarini olish",
            description = """
                Yuridik shaxsning bank rekvizitlarini INN bo'yicha olish.

                **Endpoint:** GET /app/rest/v2/services/legalentity/bankRequisites?inn={inn}
                **Auth:** Bearer token (required)

                **Tashqi API:** api-mspd.edu.uz orqali ma'lumot olinadi
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yoki xato xabari"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi")
    })
    public ResponseEntity<?> bankRequisites(
            @Parameter(description = "INN (soliq to'lovchi raqami)", required = true)
            @RequestParam String inn
    ) {
        log.info("GET /app/rest/v2/services/legalentity/bankRequisites - inn={}", inn);

        // Old-hemis returns empty object {} when external API is not available or no data found
        // DEFERRED: Requires external API (api-mspd.edu.uz) integration specification
        return ResponseEntity.ok(new LinkedHashMap<>());
    }
}
