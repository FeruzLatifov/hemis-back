package uz.hemis.university.minor.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.university.service.MinorServicesCubaService;

import java.util.Map;

/**
 * Minor Services API Controller
 *
 * <p><strong>Feature:</strong> Minor/Supporting Services</p>
 * <p><strong>OLD-HEMIS Equivalent:</strong></p>
 * <ul>
 *   <li>UzasboServiceBean.java - Uzasbo financial integration</li>
 *   <li>BillingServiceBean.java - Billing and invoicing</li>
 *   <li>OakServiceBean.java - Academic council (Ilmiy kengash)</li>
 *   <li>LegalEntityServiceBean.java - Legal entity verification</li>
 *   <li>MandatServiceBean.java - Mandate management</li>
 *   <li>HokimiyatServiceBean.java - Local government integration</li>
 *   <li>TestServiceBean.java - System testing endpoints</li>
 * </ul>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Uzasbo financial system integration</li>
 *   <li>Billing and payment tracking</li>
 *   <li>Academic council records</li>
 *   <li>Legal entity validation (TIN verification)</li>
 *   <li>Mandate document management</li>
 *   <li>Hokimiyat student reports</li>
 *   <li>System health checks and testing</li>
 * </ul>
 *
 * <p><strong>Endpoints:</strong> 10 minor service endpoints</p>
 * <p><strong>Users:</strong> Universities, Ministry, Financial systems</p>
 *
 * @since 1.0.0
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class MinorServicesApiController {

    private final MinorServicesCubaService minorServicesCubaService;

    // =====================================================
    // UZASBO SERVICE (1 endpoint)
    // =====================================================

    /**
     * Submit scholarship payment to Uzasbo system
     *
     * <p><strong>OLD-HEMIS URL:</strong></p>
     * <pre>
     * POST /app/rest/v2/services/hemishe_UzasboService/scholarship
     * </pre>
     *
     * <p><strong>Use Case:</strong> University submits scholarship payment info to Uzasbo</p>
     *
     * <p><strong>Request Body Example:</strong></p>
     * <pre>
     * {
     *   "student_pinfl": "12345678901234",
     *   "scholarship_type": "11",
     *   "amount": 1000000,
     *   "period": "2024-09",
     *   "bank_account": "00000000000000000000",
     *   "mfo": "00000"
     * }
     * </pre>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "uzasbo_id": "UZASBO-2024-123456",
     *   "status": "pending",
     *   "message": "Payment submitted to Uzasbo"
     * }
     * </pre>
     *
     * @param paymentData Scholarship payment information
     * @return Uzasbo submission result with tracking ID
     */
    @PostMapping("/app/rest/v2/services/hemishe_UzasboService/scholarship")
    public ResponseEntity<Map<String, Object>> uzasboScholarship(@RequestBody Map<String, Object> paymentData) {
        log.info("💰 Uzasbo scholarship - Submitting payment data");
        Map<String, Object> result = minorServicesCubaService.uzasboScholarship(paymentData);
        log.info("✅ Uzasbo scholarship completed - Success: {}, ID: {}",
                result.get("success"), result.get("uzasbo_id"));
        return ResponseEntity.ok(result);
    }

    // =====================================================
    // BILLING SERVICE (2 endpoints)
    // =====================================================

    /**
     * Get billing invoice for student
     *
     * <p><strong>OLD-HEMIS URL:</strong></p>
     * <pre>
     * GET /app/rest/v2/services/hemishe_BillingService/invoice?pinfl={pinfl}
     * </pre>
     *
     * <p><strong>Use Case:</strong> Student checks tuition payment invoice</p>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "invoice": {
     *     "student_pinfl": "12345678901234",
     *     "student_name": "Javohir Ergashev",
     *     "total_amount": 12000000,
     *     "paid_amount": 8000000,
     *     "remaining": 4000000,
     *     "due_date": "2024-12-31",
     *     "payments": [
     *       {
     *         "date": "2024-09-01",
     *         "amount": 4000000,
     *         "method": "bank_transfer"
     *       },
     *       ...
     *     ]
     *   }
     * }
     * </pre>
     *
     * @param pinfl Student PINFL (14 digits)
     * @return billing invoice with payment history
     */
    @GetMapping("/app/rest/v2/services/hemishe_BillingService/invoice")
    public ResponseEntity<Map<String, Object>> billingInvoice(@RequestParam("pinfl") String pinfl) {
        log.info("📄 Billing invoice - PINFL: {}", pinfl);
        Map<String, Object> result = minorServicesCubaService.billingInvoice(pinfl);
        log.info("✅ Billing invoice completed - Total: {}, Remaining: {}",
                result.get("total_amount"), result.get("remaining"));
        return ResponseEntity.ok(result);
    }

    /**
     * Get scholarship billing information
     *
     * <p><strong>Use Case:</strong> Student checks scholarship payment status</p>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "scholarship": {
     *     "student_pinfl": "12345678901234",
     *     "type": "Academic Scholarship",
     *     "monthly_amount": 500000,
     *     "payments": [
     *       {
     *         "period": "2024-09",
     *         "amount": 500000,
     *         "status": "paid",
     *         "paid_date": "2024-09-10"
     *       },
     *       {
     *         "period": "2024-10",
     *         "amount": 500000,
     *         "status": "pending"
     *       }
     *     ]
     *   }
     * }
     * </pre>
     *
     * @param pinfl Student PINFL (14 digits)
     * @return scholarship payment history
     */
    @GetMapping("/app/rest/v2/services/hemishe_BillingService/scholarship")
    public ResponseEntity<Map<String, Object>> billingScholarship(@RequestParam("pinfl") String pinfl) {
        log.info("💰 Billing scholarship - PINFL: {}", pinfl);
        return ResponseEntity.ok(minorServicesCubaService.billingScholarship(pinfl));
    }

    // =====================================================
    // OAK SERVICE (1 endpoint)
    // =====================================================

    /**
     * Get academic council (OAK) information by PINFL
     *
     * <p><strong>OLD-HEMIS URL:</strong></p>
     * <pre>
     * GET /app/rest/v2/services/hemishe_OakService/byPin?pinfl={pinfl}
     * </pre>
     *
     * <p><strong>Use Case:</strong> Check if person is doctoral candidate/academic council member</p>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "person": {
     *     "pinfl": "12345678901234",
     *     "full_name": "Dr. Alisher Karimov",
     *     "councils": [
     *       {
     *         "council_code": "DSC.01.2024.123",
     *         "university": "Toshkent davlat universiteti",
     *         "speciality": "01.02.04 - Mexanika",
     *         "role": "member",
     *         "start_date": "2020-01-01"
     *       }
     *     ]
     *   }
     * }
     * </pre>
     *
     * @param pinfl Person PINFL (14 digits)
     * @return academic council membership information
     */
    @GetMapping("/app/rest/v2/services/hemishe_OakService/byPin")
    public ResponseEntity<Map<String, Object>> oakByPin(@RequestParam("pinfl") String pinfl) {
        log.info("🎓 OAK byPin - PINFL: {}", pinfl);
        return ResponseEntity.ok(minorServicesCubaService.oakByPin(pinfl));
    }

    // =====================================================
    // LEGAL ENTITY SERVICE (1 endpoint)
    // =====================================================

    /**
     * Get legal entity information by TIN (STIR)
     *
     * <p><strong>OLD-HEMIS URL:</strong></p>
     * <pre>
     * GET /app/rest/v2/services/hemishe_LegalEntityService/get?tin={tin}
     * </pre>
     *
     * <p><strong>Use Case:</strong> Verify organization/company during contract creation</p>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "entity": {
     *     "tin": "123456789",
     *     "name": "IT Company LLC",
     *     "name_latin": "IT Company MChJ",
     *     "legal_form": "Limited Liability Company",
     *     "registration_date": "2020-05-15",
     *     "address": "Toshkent, Chilonzor tumani",
     *     "director": "Alisher Karimov",
     *     "status": "active"
     *   }
     * }
     * </pre>
     *
     * @param tin Organization TIN/STIR (9 digits)
     * @return legal entity details from state registry
     */
    @GetMapping("/app/rest/v2/services/hemishe_LegalEntityService/get")
    public ResponseEntity<Map<String, Object>> legalEntityGet(@RequestParam("tin") String tin) {
        log.info("🏢 LegalEntity get - TIN: {}", tin);
        return ResponseEntity.ok(minorServicesCubaService.legalEntityGet(tin));
    }

    // =====================================================
    // MANDAT SERVICE (1 endpoint)
    // =====================================================

    /**
     * Get mandate document information
     *
     * <p><strong>OLD-HEMIS URL:</strong></p>
     * <pre>
     * GET /app/rest/v2/services/hemishe_MandatService/get?id={id}
     * </pre>
     *
     * <p><strong>Use Case:</strong> Verify power of attorney/mandate document</p>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "mandat": {
     *     "id": "uuid-1234",
     *     "number": "M-2024-123",
     *     "date": "2024-01-15",
     *     "grantor": {
     *       "name": "Alisher Karimov",
     *       "pinfl": "11111111111111"
     *     },
     *     "grantee": {
     *       "name": "Javohir Ergashev",
     *       "pinfl": "22222222222222"
     *     },
     *     "powers": ["sign_documents", "represent_interests"],
     *     "valid_until": "2025-01-15",
     *     "status": "active"
     *   }
     * }
     * </pre>
     *
     * @param id Mandate ID (UUID)
     * @return mandate document details
     */
    @GetMapping("/app/rest/v2/services/hemishe_MandatService/get")
    public ResponseEntity<Map<String, Object>> mandatGet(@RequestParam("id") String id) {
        log.info("📜 Mandat get - ID: {}", id);
        return ResponseEntity.ok(minorServicesCubaService.mandatGet(id));
    }

    // =====================================================
    // HOKIMIYAT SERVICE (1 endpoint)
    // =====================================================

    /**
     * Get students by hokimiyat (local government) region
     *
     * <p><strong>OLD-HEMIS URL:</strong></p>
     * <pre>
     * GET /app/rest/v2/services/hemishe_HokimiyatService/students?region={region}&district={district}
     * </pre>
     *
     * <p><strong>Use Case:</strong> Local government requests list of students from their region</p>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "region": "Toshkent viloyati",
     *   "district": "Chirchiq tumani",
     *   "students": [
     *     {
     *       "pinfl": "12345678901234",
     *       "full_name": "Javohir Ergashev",
     *       "university": "TATU",
     *       "faculty": "Informatika",
     *       "course": 2,
     *       "status": "active"
     *     },
     *     ...
     *   ],
     *   "total": 150
     * }
     * </pre>
     *
     * @param region Region code
     * @param district District code (optional)
     * @return list of students from specified region/district
     */
    @GetMapping("/app/rest/v2/services/hemishe_HokimiyatService/students")
    public ResponseEntity<Map<String, Object>> hokimiyatStudents(
            @RequestParam("region") String region,
            @RequestParam(value = "district", required = false) String district) {
        log.info("🏛️ Hokimiyat students - Region: {}, District: {}", region, district);
        Map<String, Object> result = minorServicesCubaService.hokimiyatStudents(region, district);
        log.info("✅ Hokimiyat students completed - Total: {}", result.get("total"));
        return ResponseEntity.ok(result);
    }

    // =====================================================
    // TEST SERVICE (3 endpoints)
    // =====================================================

    /**
     * Test service - type test
     *
     * <p><strong>OLD-HEMIS URL:</strong></p>
     * <pre>
     * GET /app/rest/v2/services/hemishe_TestService/typetest
     * </pre>
     *
     * <p><strong>Use Case:</strong> System testing and debugging</p>
     *
     * @return test result
     */
    @GetMapping("/app/rest/v2/services/hemishe_TestService/typetest")
    public ResponseEntity<Map<String, Object>> testTypetest() {
        log.debug("🧪 Test typetest");
        return ResponseEntity.ok(minorServicesCubaService.testTypetest());
    }

    /**
     * Test service - students test
     *
     * <p><strong>Use Case:</strong> Test student service functionality</p>
     *
     * @return test result with sample student data
     */
    @GetMapping("/app/rest/v2/services/hemishe_TestService/students")
    public ResponseEntity<Map<String, Object>> testStudents() {
        log.debug("🧪 Test students");
        return ResponseEntity.ok(minorServicesCubaService.testStudents());
    }

    /**
     * Test service - health check
     *
     * <p><strong>Use Case:</strong> System health monitoring</p>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "status": "healthy",
     *   "timestamp": "2024-10-27T10:30:00Z",
     *   "services": {
     *     "database": "up",
     *     "redis": "up",
     *     "elasticsearch": "up"
     *   }
     * }
     * </pre>
     *
     * @return system health status
     */
    @GetMapping("/app/rest/v2/services/hemishe_TestService/healthcheck")
    public ResponseEntity<Map<String, Object>> testHealthcheck() {
        log.debug("🧪 Test healthcheck");
        return ResponseEntity.ok(minorServicesCubaService.testHealthcheck());
    }

    // =====================================================
    // ✅ 10 MINOR SERVICE ENDPOINTS IMPLEMENTED
    // =====================================================
    // Following OLD-HEMIS pattern: Grouped minor services
    // Equivalent to: UzasboServiceBean, BillingServiceBean,
    //                OakServiceBean, LegalEntityServiceBean,
    //                MandatServiceBean, HokimiyatServiceBean,
    //                TestServiceBean
    // =====================================================
}
