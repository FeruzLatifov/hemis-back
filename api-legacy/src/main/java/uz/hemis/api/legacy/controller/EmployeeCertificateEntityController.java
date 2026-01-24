package uz.hemis.api.legacy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Employee Certificate Entity Controller for OLD-HEMIS compatibility.
 * <p>
 * Note: The endpoint path contains intentional typo "EEmpoyeeCertificate" to match OLD-HEMIS exactly.
 * </p>
 */
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EEmpoyeeCertificate")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "68.Sertifikat", description = "Xodim sertifikatlari")
public class EmployeeCertificateEntityController {

    /**
     * Create employee certificates (batch operation).
     * <p>
     * Accepts an array of employee certificate data and returns created entities.
     * This is a stub implementation that generates UUIDs without persisting to database.
     * </p>
     *
     * @param certificateDataList List of employee certificate data maps
     * @return List of created certificate responses with generated IDs
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Xodim sertifikatlarini yaratish",
            description = """
                    Xodim sertifikatlarini yaratish uchun OLD-HEMIS formatidagi endpoint.

                    **Eslatma:** Endpoint yo'lida "EEmpoyeeCertificate" yozuvi OLD-HEMIS tizimiga moslik uchun ataylab saqlab qolindi.

                    **So'rov formati:** Array ko'rinishida sertifikat ma'lumotlari qabul qilinadi.

                    **Javob:** Yaratilgan sertifikatlar ro'yxati qaytariladi.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Sertifikatlar muvaffaqiyatli yaratildi",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = EmployeeCertificateResponse.class)),
                            examples = @ExampleObject(
                                    name = "Muvaffaqiyatli javob",
                                    value = """
                                            [
                                                {
                                                    "_entityName": "hemishe_EEmpoyeeCertificate",
                                                    "_instanceName": "AA1112244",
                                                    "id": "550e8400-e29b-41d4-a716-446655440000"
                                                }
                                            ]
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Noto'g'ri so'rov formati",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autentifikatsiya talab qilinadi",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Ruxsat berilmagan",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Xodim sertifikatlari ma'lumotlari (array formatda)",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = EmployeeCertificateRequest.class)),
                    examples = @ExampleObject(
                            name = "So'rov namunasi",
                            value = """
                                    [
                                        {
                                            "university": {"code": "999"},
                                            "employee": {"id": "f41a4336-a4f5-154d-cc28-eff76884fa99"},
                                            "certificateType": {"code": "2"},
                                            "certificateName": {"code": "30"},
                                            "certificateGrade": {"code": "2"},
                                            "certificateSubject": {"code": "1"},
                                            "issueDate": "2025-03-05",
                                            "validDate": "2026-03-11",
                                            "serialNumber": "AA1112244",
                                            "active": true
                                        }
                                    ]
                                    """
                    )
            )
    )
    public ResponseEntity<List<Map<String, Object>>> createEmployeeCertificates(
            @RequestBody List<Map<String, Object>> certificateDataList) {

        log.info("Creating {} employee certificate(s)", certificateDataList.size());

        List<Map<String, Object>> responseList = new ArrayList<>();

        for (Map<String, Object> certificateData : certificateDataList) {
            // Extract serial number for _instanceName
            String serialNumber = extractSerialNumber(certificateData);

            // Generate UUID for the new certificate
            UUID generatedId = UUID.randomUUID();

            // Build response in OLD-HEMIS format using LinkedHashMap for consistent field order
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("_entityName", "hemishe_EEmpoyeeCertificate");
            response.put("_instanceName", serialNumber);
            response.put("id", generatedId.toString());

            responseList.add(response);

            log.debug("Created employee certificate stub with id: {}, serialNumber: {}", generatedId, serialNumber);
        }

        log.info("Successfully created {} employee certificate stub(s)", responseList.size());

        return ResponseEntity.status(HttpStatus.CREATED).body(responseList);
    }

    /**
     * Extracts serial number from certificate data map.
     *
     * @param certificateData Certificate data map
     * @return Serial number or empty string if not found
     */
    private String extractSerialNumber(Map<String, Object> certificateData) {
        Object serialNumberObj = certificateData.get("serialNumber");
        return serialNumberObj != null ? serialNumberObj.toString() : "";
    }

    /**
     * Schema class for Swagger documentation - Request body.
     */
    @Schema(description = "Xodim sertifikati yaratish uchun so'rov")
    private static class EmployeeCertificateRequest {

        @Schema(description = "Universitet", example = "{\"code\": \"999\"}")
        public Map<String, String> university;

        @Schema(description = "Xodim", example = "{\"id\": \"f41a4336-a4f5-154d-cc28-eff76884fa99\"}")
        public Map<String, String> employee;

        @Schema(description = "Sertifikat turi", example = "{\"code\": \"2\"}")
        public Map<String, String> certificateType;

        @Schema(description = "Sertifikat nomi", example = "{\"code\": \"30\"}")
        public Map<String, String> certificateName;

        @Schema(description = "Sertifikat darajasi", example = "{\"code\": \"2\"}")
        public Map<String, String> certificateGrade;

        @Schema(description = "Sertifikat fani", example = "{\"code\": \"1\"}")
        public Map<String, String> certificateSubject;

        @Schema(description = "Berilgan sana", example = "2025-03-05")
        public String issueDate;

        @Schema(description = "Amal qilish sanasi", example = "2026-03-11")
        public String validDate;

        @Schema(description = "Seriya raqami", example = "AA1112244")
        public String serialNumber;

        @Schema(description = "Faol holati", example = "true")
        public Boolean active;
    }

    /**
     * Schema class for Swagger documentation - Response body.
     */
    @Schema(description = "Yaratilgan xodim sertifikati javobi")
    private static class EmployeeCertificateResponse {

        @Schema(description = "Entity nomi (OLD-HEMIS formati)", example = "hemishe_EEmpoyeeCertificate")
        public String _entityName;

        @Schema(description = "Instance nomi (seriya raqami)", example = "AA1112244")
        public String _instanceName;

        @Schema(description = "Yaratilgan sertifikat ID", example = "550e8400-e29b-41d4-a716-446655440000")
        public String id;
    }
}
