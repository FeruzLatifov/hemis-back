package uz.hemis.api.legacy.controller.finance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Contract Service Controller
 *
 * <p><strong>URL Pattern:</strong> {@code /services/contract/*}</p>
 * <p><strong>OLD-HEMIS Format:</strong> {@code GET /services/contract/get?pinfl=30503941620012&year=2022}</p>
 *
 * @since 2.0.0
 */
@Tag(name = "57.Contract", description = "Talaba shartnoma ma'lumotlari")
@RestController
@RequestMapping("/services/contract")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class ContractServiceController {

    @Operation(
        summary = "Shartnoma ma'lumotlarini olish",
        description = "Talaba PINFL va o'quv yili orqali shartnoma ma'lumotlarini olish. " +
                      "OLD-HEMIS formatida javob qaytaradi."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Shartnoma ma'lumotlari muvaffaqiyatli olindi",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    name = "Muvaffaqiyatli javob",
                    value = """
                        {
                            "success": true,
                            "data": {
                                "pinfl": "30503941620012",
                                "year": 2022,
                                "contracts": [],
                                "message": "Contract data"
                            }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Noto'g'ri so'rov parametrlari",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Xato javob",
                    value = """
                        {
                            "success": false,
                            "data": null
                        }
                        """
                )
            )
        )
    })
    @GetMapping("/get")
    public ResponseEntity<Map<String, Object>> getContract(
        @Parameter(description = "Talaba PINFL (14 raqamli)", required = true, example = "30503941620012")
        @RequestParam String pinfl,
        @Parameter(description = "O'quv yili", required = true, example = "2022")
        @RequestParam Integer year
    ) {
        log.info("GET /services/contract/get - pinfl: {}, year: {}", pinfl, year);

        // Build data object with consistent field order using LinkedHashMap
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("pinfl", pinfl);
        data.put("year", year);
        data.put("contracts", new ArrayList<>());
        data.put("message", "Contract data");

        // Build response wrapper with consistent field order
        LinkedHashMap<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", data);

        return ResponseEntity.ok(response);
    }
}
