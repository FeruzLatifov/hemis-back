package uz.hemis.api.university.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.hemis.common.Jackson2Response;
import uz.hemis.service.integration.GatewayService;
import uz.hemis.service.integration.model.GatewayResult;

/**
 * Gateway controller — tashqi tizimlardan ma'lumotni passthrough qilib
 * universitet tarafga chiqaradi. Tashqi API'ning HTTP status va body
 * o'zgarmasdan klient'ga uzatiladi.
 */
@RestController
@RequestMapping("/api/v1/university/gateway")
@Tag(name = "University Gateway", description = "Tashqi tizimlardan ma'lumot olish (passthrough)")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
@Validated
@Jackson2Response
public class GatewayController {

    private final GatewayService gatewayService;

    @GetMapping("/kadastr/by-cadnum")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Kadastr raqami bo'yicha ma'lumot",
            description = "Tashqi api-mspd dan kadastr raqami bo'yicha ma'lumotni olib qaytaradi. " +
                    "Tashqi API javobining HTTP status code va body o'zgarmasdan passthrough qilinadi.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tashqi API muvaffaqiyatli javob qaytardi"),
            @ApiResponse(responseCode = "400", description = "cadNumber bo'sh yoki tashqi API'ning xato javobi"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab qilinadi")
    })
    public ResponseEntity<JsonNode> getCadastreByCadnum(
            @Parameter(description = "Kadastr raqami", example = "00:00:00:00:00:0000")
            @RequestParam @NotBlank String cadNumber
    ) {
        log.info("GET /university/gateway/kadastr/by-cadnum cadNumber={}", cadNumber);
        GatewayResult result = gatewayService.getCadastreByCadnum(cadNumber);
        return ResponseEntity.status(result.statusCode()).body(result.body());
    }
}
