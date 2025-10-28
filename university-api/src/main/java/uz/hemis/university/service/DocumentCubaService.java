package uz.hemis.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.hemis.app.service.base.AbstractInternalCubaService;
import uz.hemis.domain.entity.Contract;
import uz.hemis.domain.entity.Diploma;
import uz.hemis.domain.entity.Student;
import uz.hemis.domain.repository.ContractRepository;
import uz.hemis.domain.repository.DiplomaRepository;
import uz.hemis.domain.repository.StudentRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Document CUBA Services - OLD-HEMIS Compatibility
 *
 * <p><strong>CRITICAL - OLD-HEMIS Compatibility:</strong></p>
 * <ul>
 *   <li>Implements document-related CUBA services from rest-services.xml</li>
 *   <li>Diploma, Transcript, Contract, DiplomBlank services</li>
 *   <li>Used for student document verification and management</li>
 * </ul>
 *
 * <p><strong>OPTIMIZATION:</strong></p>
 * <ul>
 *   <li>Extends AbstractInternalCubaService</li>
 *   <li>Uses REAL database for Diploma and Contract with PINFL lookup</li>
 *   <li>All 4 document services in ONE class - no code duplication</li>
 * </ul>
 *
 * <p><strong>Services (4 services, 7 methods):</strong></p>
 * <ul>
 *   <li>Diploma Service - 2 methods (100% REAL database)</li>
 *   <li>Transcript Service - 1 method (mock - entity doesn't exist)</li>
 *   <li>Contract Service - 1 method (100% REAL database)</li>
 *   <li>DiplomBlank Service - 3 methods (mock - entity doesn't exist)</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentCubaService extends AbstractInternalCubaService {

    private final DiplomaRepository diplomaRepository;
    private final ContractRepository contractRepository;
    private final StudentRepository studentRepository;

    // TODO: Inject when Transcript and DiplomBlank entities are created
    // private final TranscriptRepository transcriptRepository;
    // private final DiplomBlankRepository diplomBlankRepository;

    // =====================================================
    // DIPLOMA SERVICE (2 methods)
    // =====================================================

    /**
     * Get diploma by hash
     *
     * <p><strong>Method:</strong> byhash</p>
     * <p><strong>URL:</strong> /app/rest/v2/services/hemishe_DiplomaService/byhash?hash={hash}</p>
     *
     * <p>Diploma verification by hash - used for diploma authenticity check</p>
     *
     * @param hash Diploma hash (unique identifier)
     * @return Diploma data or error
     */
    public Map<String, Object> diplomaByHash(String hash) {
        log.info("Getting diploma by hash - Hash: {}", hash);

        Map<String, Object> validationError = validateRequired("hash", hash);
        if (validationError != null) {
            return validationError;
        }

        // Query from REAL database
        Optional<Diploma> diplomaOpt = diplomaRepository.findByDiplomaHash(hash);

        if (diplomaOpt.isEmpty()) {
            return errorResponse("not_found", "Diploma not found with hash: " + hash);
        }

        Diploma diploma = diplomaOpt.get();
        Map<String, Object> result = mapDiplomaToResponse(diploma);
        result.put("verified", true); // Found in database = verified

        log.info("Found diploma: {}", diploma.getDiplomaNumber());
        return successResponse(result);
    }

    /**
     * Get diploma info by PINFL
     *
     * <p><strong>Method:</strong> info</p>
     * <p><strong>URL:</strong> /app/rest/v2/services/hemishe_DiplomaService/info?pinfl={pinfl}</p>
     *
     * @param pinfl Student PINFL
     * @return Diploma info or error
     */
    public Map<String, Object> diplomaInfo(String pinfl) {
        log.info("Getting diploma info - PINFL: {}", pinfl);

        Map<String, Object> validationError = validateRequired("pinfl", pinfl);
        if (validationError != null) {
            return validationError;
        }

        // Step 1: Find student by PINFL (REAL database)
        Optional<Student> studentOpt = studentRepository.findMasterByPinfl(pinfl);

        if (studentOpt.isEmpty()) {
            return errorResponse("not_found", "Student not found with PINFL: " + pinfl);
        }

        Student student = studentOpt.get();

        // Step 2: Query all diplomas for this student (REAL database)
        List<Diploma> diplomas = diplomaRepository.findByStudentOrderByIssueDateDesc(student.getId());

        if (diplomas.isEmpty()) {
            log.info("No diplomas found for student: {}", pinfl);
            return successListResponse(new ArrayList<>()); // Empty list
        }

        // Step 3: Map to response format
        List<Map<String, Object>> diplomaList = diplomas.stream()
                .map(this::mapDiplomaToResponse)
                .collect(Collectors.toList());

        log.info("Found {} diplomas for student: {}", diplomaList.size(), pinfl);
        return successListResponse(diplomaList);
    }

    // =====================================================
    // TRANSCRIPT SERVICE (1 method)
    // =====================================================

    /**
     * Get transcript (academic record) by PINFL
     *
     * <p><strong>Method:</strong> get</p>
     * <p><strong>URL:</strong> /app/rest/v2/services/hemishe_TranscriptService/get?pinfl={pinfl}</p>
     *
     * <p>Transcript = Student's complete academic record (all subjects, grades)</p>
     *
     * @param pinfl Student PINFL
     * @return Transcript data or error
     */
    public Map<String, Object> transcriptGet(String pinfl) {
        log.info("Getting transcript - PINFL: {}", pinfl);

        Map<String, Object> validationError = validateRequired("pinfl", pinfl);
        if (validationError != null) {
            return validationError;
        }

        // TODO: Query from database when Transcript/AcademicRecord entity exists
        // Optional<Transcript> transcript = transcriptRepository.findByPinfl(pinfl);

        // Mock data
        Map<String, Object> transcript = new HashMap<>();
        transcript.put("student_pinfl", pinfl);
        transcript.put("student_name", "Alimov Vali Akbarovich");
        transcript.put("university", "Toshkent Davlat Universiteti");
        transcript.put("education_type", "Bakalavr");
        transcript.put("total_credits", 240);
        transcript.put("gpa", 4.5);

        // Subjects list
        List<Map<String, Object>> subjects = new ArrayList<>();
        subjects.add(Map.of("subject", "Oliy matematika I", "credits", 6, "grade", 5));
        subjects.add(Map.of("subject", "Fizika", "credits", 5, "grade", 4));
        subjects.add(Map.of("subject", "Ingliz tili", "credits", 4, "grade", 5));

        transcript.put("subjects", subjects);

        return successResponse(transcript);
    }

    // =====================================================
    // CONTRACT SERVICE (1 method)
    // =====================================================

    /**
     * Get contract by PINFL and year
     *
     * <p><strong>Method:</strong> get</p>
     * <p><strong>URL:</strong> /app/rest/v2/services/hemishe_ContractService/get?pinfl={pinfl}&year={year}</p>
     *
     * <p>Student payment contract (for contract students)</p>
     *
     * @param pinfl Student PINFL
     * @param year Academic year
     * @return Contract data or error
     */
    public Map<String, Object> contractGet(String pinfl, String year) {
        log.info("Getting contract - PINFL: {}, Year: {}", pinfl, year);

        Map<String, Object> validationError = validateRequired("pinfl", pinfl);
        if (validationError != null) {
            return validationError;
        }

        validationError = validateRequired("year", year);
        if (validationError != null) {
            return validationError;
        }

        // Step 1: Find student by PINFL (REAL database)
        Optional<Student> studentOpt = studentRepository.findMasterByPinfl(pinfl);

        if (studentOpt.isEmpty()) {
            return errorResponse("not_found", "Student not found with PINFL: " + pinfl);
        }

        Student student = studentOpt.get();

        // Step 2: Query contracts for this student (REAL database)
        List<Contract> contracts = contractRepository.findByStudent(student.getId());

        // Step 3: Filter by education year if contracts found
        Optional<Contract> contractOpt = contracts.stream()
                .filter(c -> year.equals(c.getEducationYear()))
                .findFirst();

        if (contractOpt.isEmpty()) {
            return errorResponse("not_found",
                    "Contract not found for student " + pinfl + " in year " + year);
        }

        Contract contract = contractOpt.get();
        Map<String, Object> result = mapContractToResponse(contract);

        log.info("Found contract: {} for student: {}", contract.getContractNumber(), pinfl);
        return successResponse(result);
    }

    // =====================================================
    // DIPLOM BLANK SERVICE (3 methods)
    // =====================================================

    /**
     * Get diploma blanks by university and year
     *
     * <p><strong>Method:</strong> get</p>
     * <p><strong>URL:</strong> /app/rest/v2/services/hemishe_DiplomBlankService/get?university={code}&year={year}</p>
     *
     * <p>Diploma blanks = Physical diploma forms allocated to university</p>
     *
     * @param university University code
     * @param year Year
     * @return List of diploma blanks
     */
    public Map<String, Object> diplomBlankGet(String university, String year) {
        log.info("Getting diploma blanks - University: {}, Year: {}", university, year);

        Map<String, Object> validationError = validateRequired("university", university);
        if (validationError != null) {
            return validationError;
        }

        validationError = validateRequired("year", year);
        if (validationError != null) {
            return validationError;
        }

        // TODO: Query from database when DiplomBlank entity exists
        // List<DiplomBlank> blanks = diplomBlankRepository.findByUniversityAndYear(university, year);

        // Mock data
        List<Map<String, Object>> blanks = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            Map<String, Object> blank = new HashMap<>();
            blank.put("blank_code", "MB " + (1234560 + i));
            blank.put("blank_series", "MB");
            blank.put("year", year);
            blank.put("status", "Available"); // Available, Used, Damaged, Lost
            blank.put("university", university);
            blanks.add(blank);
        }

        return successListResponse(blanks);
    }

    /**
     * Set diploma blank status
     *
     * <p><strong>Method:</strong> setStatus</p>
     * <p><strong>URL:</strong> POST /app/rest/v2/services/hemishe_DiplomBlankService/setStatus</p>
     *
     * <p><strong>Request Body:</strong></p>
     * <pre>
     * {
     *   "blankCode": "MB 1234567",
     *   "statusCode": "Used"  // Available, Used, Damaged, Lost
     * }
     * </pre>
     *
     * @param blankCode Blank code
     * @param statusCode New status code
     * @return Success or error
     */
    public Map<String, Object> diplomBlankSetStatus(String blankCode, String statusCode) {
        log.info("Setting diploma blank status - Blank: {}, Status: {}", blankCode, statusCode);

        Map<String, Object> validationError = validateRequired("blankCode", blankCode);
        if (validationError != null) {
            return validationError;
        }

        validationError = validateRequired("statusCode", statusCode);
        if (validationError != null) {
            return validationError;
        }

        // TODO: Update database when DiplomBlank entity exists
        // DiplomBlank blank = diplomBlankRepository.findByCode(blankCode);
        // blank.setStatus(statusCode);
        // diplomBlankRepository.save(blank);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Diploma blank status updated");
        result.put("blank_code", blankCode);
        result.put("new_status", statusCode);

        return result;
    }

    /**
     * Set diploma blank status with reason
     *
     * <p><strong>Method:</strong> setStatus (overloaded)</p>
     * <p><strong>URL:</strong> POST /app/rest/v2/services/hemishe_DiplomBlankService/setStatus</p>
     *
     * <p><strong>Request Body:</strong></p>
     * <pre>
     * {
     *   "blankCode": "MB 1234567",
     *   "statusCode": "Damaged",
     *   "reason": "Water damage during storage"
     * }
     * </pre>
     *
     * @param blankCode Blank code
     * @param statusCode New status code
     * @param reason Reason for status change
     * @return Success or error
     */
    public Map<String, Object> diplomBlankSetStatusWithReason(String blankCode, String statusCode, String reason) {
        log.info("Setting diploma blank status with reason - Blank: {}, Status: {}, Reason: {}",
                blankCode, statusCode, reason);

        Map<String, Object> validationError = validateRequired("blankCode", blankCode);
        if (validationError != null) {
            return validationError;
        }

        validationError = validateRequired("statusCode", statusCode);
        if (validationError != null) {
            return validationError;
        }

        // Reason is optional but logged
        if (isNotEmpty(reason)) {
            log.info("Status change reason: {}", reason);
        }

        // TODO: Update database with reason
        // DiplomBlank blank = diplomBlankRepository.findByCode(blankCode);
        // blank.setStatus(statusCode);
        // blank.setStatusChangeReason(reason);
        // diplomBlankRepository.save(blank);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Diploma blank status updated with reason");
        result.put("blank_code", blankCode);
        result.put("new_status", statusCode);
        result.put("reason", reason);

        return result;
    }

    // =====================================================
    // Helper Methods
    // =====================================================

    /**
     * Map Diploma entity to response format
     */
    private Map<String, Object> mapDiplomaToResponse(Diploma diploma) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", diploma.getId());
        map.put("diploma_number", diploma.getDiplomaNumber());
        map.put("serial_number", diploma.getSerialNumber());
        map.put("diploma_hash", diploma.getDiplomaHash());
        map.put("diploma_type", diploma.getDiplomaType());
        map.put("student", diploma.getStudent());
        map.put("university", diploma.getUniversity());
        map.put("specialty", diploma.getSpecialty());
        map.put("graduation_year", diploma.getGraduationYear());
        map.put("issue_date", diploma.getIssueDate());
        map.put("status", diploma.getStatus());
        return map;
    }

    /**
     * Map Contract entity to response format
     */
    private Map<String, Object> mapContractToResponse(Contract contract) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", contract.getId());
        map.put("contract_number", contract.getContractNumber());
        map.put("student", contract.getStudent());
        map.put("university", contract.getUniversity());
        map.put("education_year", contract.getEducationYear());
        map.put("contract_type", contract.getContractType());
        map.put("contract_sum", contract.getContractSum());
        map.put("paid_sum", contract.getPaidSum());
        map.put("status", contract.getStatus());
        map.put("is_active", contract.getIsActive());
        return map;
    }
}
