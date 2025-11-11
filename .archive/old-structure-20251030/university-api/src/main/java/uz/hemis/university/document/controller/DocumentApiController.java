package uz.hemis.university.document.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.university.service.DocumentCubaService;

import java.util.Map;

/**
 * Document API Controller
 *
 * <p><strong>Feature:</strong> Academic Document Management</p>
 * <p><strong>OLD-HEMIS Equivalent:</strong></p>
 * <ul>
 *   <li>DiplomaServiceBean.java - Diploma management</li>
 *   <li>TranscriptServiceBean.java - Academic transcript</li>
 *   <li>ContractServiceBean.java - Education contracts</li>
 *   <li>DiplomBlankServiceBean.java - Diploma blank tracking</li>
 * </ul>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Diploma verification and retrieval</li>
 *   <li>Academic transcripts</li>
 *   <li>Education contracts</li>
 *   <li>Diploma blank inventory management</li>
 * </ul>
 *
 * <p><strong>CUBA Pattern (Backward Compatible):</strong></p>
 * <pre>
 * GET /app/rest/v2/services/hemishe_DiplomaService/byhash
 * GET /app/rest/v2/services/hemishe_TranscriptService/get
 * GET /app/rest/v2/services/hemishe_ContractService/get
 * GET /app/rest/v2/services/hemishe_DiplomBlankService/get
 * </pre>
 *
 * <p><strong>Endpoints:</strong> 6 document management endpoints</p>
 * <p><strong>Users:</strong> 200+ universities, graduates, employers</p>
 *
 * @since 1.0.0
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class DocumentApiController {

    private final DocumentCubaService documentCubaService;

    /**
     * Get diploma by hash (verification)
     *
     * <p><strong>OLD-HEMIS URL:</strong></p>
     * <pre>
     * GET /app/rest/v2/services/hemishe_DiplomaService/byhash?hash={hash}
     * </pre>
     *
     * <p><strong>Use Case:</strong> Employers verify diploma authenticity using QR code hash</p>
     *
     * <p><strong>Example:</strong></p>
     * <pre>
     * curl 'https://ministry.hemis.uz/app/rest/v2/services/hemishe_DiplomaService/byhash?hash=abc123...' \
     *   -H 'Authorization: Bearer {token}'
     * </pre>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "valid": true,
     *   "diploma": {
     *     "series": "AA",
     *     "number": "1234567",
     *     "issue_date": "2024-06-15",
     *     "student_name": "Javohir Ergashev",
     *     "speciality": "Dasturiy injiniring",
     *     "university": "Toshkent axborot texnologiyalari universiteti",
     *     "graduation_date": "2024-06-01"
     *   }
     * }
     * </pre>
     *
     * @param hash Diploma verification hash (from QR code)
     * @return diploma information if hash is valid
     */
    @GetMapping("/app/rest/v2/services/hemishe_DiplomaService/byhash")
    public ResponseEntity<Map<String, Object>> diplomaByHash(@RequestParam("hash") String hash) {
        log.info("🎓 Diploma byhash - Hash: {}", hash.substring(0, Math.min(8, hash.length())) + "...");
        Map<String, Object> result = documentCubaService.diplomaByHash(hash);
        log.info("✅ Diploma byhash completed - Valid: {}", result.get("valid"));
        return ResponseEntity.ok(result);
    }

    /**
     * Get diploma info by PINFL
     *
     * <p>Returns diploma information for a graduate by their PINFL</p>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "diplomas": [
     *     {
     *       "series": "AA",
     *       "number": "1234567",
     *       "issue_date": "2024-06-15",
     *       "education_type": "11",
     *       "speciality": "Dasturiy injiniring",
     *       "university": "TATU"
     *     }
     *   ]
     * }
     * </pre>
     *
     * @param pinfl PINFL (14 digits)
     * @return list of diplomas issued to the person
     */
    @GetMapping("/app/rest/v2/services/hemishe_DiplomaService/info")
    public ResponseEntity<Map<String, Object>> diplomaInfo(@RequestParam("pinfl") String pinfl) {
        log.info("🎓 Diploma info - PINFL: {}", pinfl);
        return ResponseEntity.ok(documentCubaService.diplomaInfo(pinfl));
    }

    /**
     * Get transcript by PINFL
     *
     * <p>Returns academic transcript with all courses and grades</p>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "transcript": {
     *     "student_name": "Javohir Ergashev",
     *     "pinfl": "12345678901234",
     *     "speciality": "Dasturiy injiniring",
     *     "courses": [
     *       {
     *         "code": "CS101",
     *         "name": "Dasturlash asoslari",
     *         "credits": 4,
     *         "grade": 5,
     *         "semester": "2020/2021 - 1"
     *       },
     *       ...
     *     ],
     *     "total_credits": 240,
     *     "gpa": 4.5
     *   }
     * }
     * </pre>
     *
     * @param pinfl PINFL (14 digits)
     * @return academic transcript with grades
     */
    @GetMapping("/app/rest/v2/services/hemishe_TranscriptService/get")
    public ResponseEntity<Map<String, Object>> transcriptGet(@RequestParam("pinfl") String pinfl) {
        log.info("📝 Transcript get - PINFL: {}", pinfl);
        return ResponseEntity.ok(documentCubaService.transcriptGet(pinfl));
    }

    /**
     * Get education contract
     *
     * <p>Returns contract information for a specific academic year</p>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "contract": {
     *     "number": "2024/123",
     *     "date": "2024-08-15",
     *     "student_name": "Javohir Ergashev",
     *     "amount": 12000000,
     *     "payment_schedule": [
     *       {"month": 9, "amount": 1000000, "paid": true},
     *       {"month": 10, "amount": 1000000, "paid": false},
     *       ...
     *     ]
     *   }
     * }
     * </pre>
     *
     * @param pinfl PINFL (14 digits)
     * @param year Academic year (e.g., "2024")
     * @return contract information with payment schedule
     */
    @GetMapping("/app/rest/v2/services/hemishe_ContractService/get")
    public ResponseEntity<Map<String, Object>> contractGet(
            @RequestParam("pinfl") String pinfl,
            @RequestParam("year") String year) {
        log.info("📄 Contract get - PINFL: {}, Year: {}", pinfl, year);
        return ResponseEntity.ok(documentCubaService.contractGet(pinfl, year));
    }

    /**
     * Get diploma blanks inventory
     *
     * <p>Returns list of diploma blank forms assigned to university for a specific year</p>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "blanks": [
     *     {
     *       "series": "AA",
     *       "number_from": "1234500",
     *       "number_to": "1234600",
     *       "total": 101,
     *       "used": 45,
     *       "available": 56,
     *       "status": "active"
     *     },
     *     ...
     *   ]
     * }
     * </pre>
     *
     * @param university University code (5 digits)
     * @param year Academic year (e.g., "2024")
     * @return diploma blank inventory
     */
    @GetMapping("/app/rest/v2/services/hemishe_DiplomBlankService/get")
    public ResponseEntity<Map<String, Object>> diplomBlankGet(
            @RequestParam("university") String university,
            @RequestParam("year") String year) {
        log.info("📋 DiplomBlank get - University: {}, Year: {}", university, year);
        return ResponseEntity.ok(documentCubaService.diplomBlankGet(university, year));
    }

    /**
     * Set diploma blank status
     *
     * <p>Updates status of a diploma blank (used, damaged, lost, etc.)</p>
     *
     * <p><strong>Request Body Example:</strong></p>
     * <pre>
     * {
     *   "blankCode": "AA1234567",
     *   "statusCode": "used",
     *   "reason": "Issued to graduate" (optional)
     * }
     * </pre>
     *
     * <p><strong>Status Codes:</strong></p>
     * <ul>
     *   <li>available - Available for use</li>
     *   <li>used - Already issued</li>
     *   <li>damaged - Damaged, cannot use</li>
     *   <li>lost - Lost, reported</li>
     *   <li>cancelled - Cancelled by ministry</li>
     * </ul>
     *
     * @param request Status update request with blank code, status, and optional reason
     * @return update result
     */
    @PostMapping("/app/rest/v2/services/hemishe_DiplomBlankService/setStatus")
    public ResponseEntity<Map<String, Object>> diplomBlankSetStatus(@RequestBody Map<String, String> request) {
        String blankCode = request.get("blankCode");
        String statusCode = request.get("statusCode");
        String reason = request.get("reason");

        log.info("📝 DiplomBlank setStatus - Code: {}, Status: {}", blankCode, statusCode);

        Map<String, Object> result;
        if (reason != null && !reason.isEmpty()) {
            result = documentCubaService.diplomBlankSetStatusWithReason(blankCode, statusCode, reason);
        } else {
            result = documentCubaService.diplomBlankSetStatus(blankCode, statusCode);
        }

        log.info("✅ DiplomBlank setStatus completed - Success: {}", result.get("success"));
        return ResponseEntity.ok(result);
    }

    // =====================================================
    // ✅ 6 DOCUMENT ENDPOINTS IMPLEMENTED
    // =====================================================
    // Following OLD-HEMIS pattern: Grouped document services
    // Equivalent to: DiplomaServiceBean, TranscriptServiceBean,
    //                ContractServiceBean, DiplomBlankServiceBean
    // =====================================================
}
