package uz.hemis.service.student;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import uz.hemis.domain.entity.finance.Contract;
import uz.hemis.domain.entity.student.Student;
import uz.hemis.domain.repository.ContractRepository;
import uz.hemis.domain.repository.GradeRepository;
import uz.hemis.domain.repository.StudentRepository;
import uz.hemis.service.base.CubaResponseHelper;

import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Student CUBA Service - OLD-HEMIS CUBA Platform Compatibility
 *
 * <p><strong>CRITICAL - OLD-HEMIS Compatibility:</strong></p>
 * <ul>
 *   <li>Implements 24 CUBA service methods from rest-services.xml</li>
 *   <li>Different from CRUD StudentService - these are CUBA-specific methods</li>
 *   <li>Used by universities calling old-HEMIS API patterns</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StudentCubaService {

    private final StudentRepository studentRepository;
    private final ContractRepository contractRepository;
    private final GradeRepository gradeRepository;

    // Tashkent SOATO code prefix
    private static final String TASHKENT_SOATO_PREFIX = "1726";

    // Student status codes (from hemishe_h_student_status_type)
    private static final Set<String> ACTIVE_STATUSES = Set.of(
            "10", // Boshqa (Other)
            "11", // O'qimoqda (Active)
            "12", // Ta'tilda (On leave)
            "13", // Akademik ta'til
            "14", // Harbiy xizmat
            "15"  // Tibbiy ta'til
    );
    private static final Set<String> GRADUATED_STATUSES = Set.of(
            "16"  // Bitirgan (Graduated)
    );
    private static final Set<String> EXPELLED_STATUSES = Set.of(
            "31", // Talabaning iltimosiga ko'ra
            "32", // O'quv intizomini buzganlik
            "33", // Akademik qarzdorlik
            "34", // Shartnoma shartlarini bajarmaganlik
            "35", // Sog'lig'i sababli
            "36"  // Jinoyat sodir etganligi
    );

    // Doctoral education types
    private static final Set<String> DOCTORAL_EDUCATION_TYPES = Set.of(
            "40", // PhD
            "50"  // DSc
    );

    /**
     * Verify student exists by PINFL
     */
    public Map<String, Object> verify(String pinfl) {
        log.info("Verifying student - PINFL: {}", pinfl);

        // ✅ SECURITY FIX: PINFL format validatsiyasi
        if (pinfl == null || !pinfl.matches("\\d{14}")) {
            return CubaResponseHelper.errorResponse("invalid_parameter", "PINFL must be exactly 14 digits");
        }

        Optional<Student> student = studentRepository.findMasterByPinfl(pinfl);

        Map<String, Object> result = new HashMap<>();

        if (student.isPresent()) {
            Student s = student.get();
            result.put("exists", true);
            result.put("pinfl", pinfl);
            result.put("id", s.getId());
            result.put("code", s.getCode());
            result.put("university", s.getUniversity());
            result.put("status", s.getStudentStatus());
        } else {
            // ✅ SECURITY FIX: Enumeration hujumini oldini olish —
            // Topilmasa ham topilgandek javob qaytarish o'rniga 404
            return CubaResponseHelper.errorResponse("not_found", "Student not found");
        }

        return result;
    }

    /**
     * Get student by PINFL
     */
    public Map<String, Object> get(String pinfl) {
        log.info("Getting student by PINFL - PINFL: {}", pinfl);

        Optional<Student> student = studentRepository.findMasterByPinfl(pinfl);

        if (student.isEmpty()) {
            return CubaResponseHelper.errorResponse("not_found", "Student not found");
        }

        return studentToMap(student.get());
    }

    /**
     * Get student with enrollment status
     */
    public Map<String, Object> getWithStatus(String pinfl) {
        log.info("Getting student with status - PINFL: {}", pinfl);

        Optional<Student> student = studentRepository.findMasterByPinfl(pinfl);

        if (student.isEmpty()) {
            return CubaResponseHelper.errorResponse("not_found", "Student not found");
        }

        Map<String, Object> data = studentToMap(student.get());

        Student s = student.get();
        data.put("status_code", s.getStudentStatus());
        data.put("is_active", isActiveStatus(s.getStudentStatus()));
        data.put("is_graduated", isGraduatedStatus(s.getStudentStatus()));
        data.put("is_expelled", isExpelledStatus(s.getStudentStatus()));

        return data;
    }

    /**
     * Get student by ID
     */
    public Map<String, Object> getById(String id) {
        log.info("Getting student by ID - ID: {}", id);

        try {
            UUID uuid = UUID.fromString(id);
            Optional<Student> student = studentRepository.findById(uuid);

            if (student.isEmpty()) {
                return CubaResponseHelper.errorResponse("not_found", "Student not found");
            }

            return studentToMap(student.get());

        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID: {}", id);
            return CubaResponseHelper.errorResponse("invalid_id", "Invalid student ID format");
        }
    }

    /**
     * Get doctoral student by PINFL
     *
     * Searches for students with education type 40 (PhD) or 50 (DSc)
     */
    public Map<String, Object> getDoctoral(String pinfl) {
        log.info("Getting doctoral student - PINFL: {}", pinfl);

        // Search all students with this PINFL, find doctoral one
        List<Student> allStudents = studentRepository.findAllByPinfl(pinfl);

        Optional<Student> doctoral = allStudents.stream()
                .filter(s -> s.getEducationType() != null && DOCTORAL_EDUCATION_TYPES.contains(s.getEducationType()))
                .findFirst();

        if (doctoral.isEmpty()) {
            return CubaResponseHelper.errorResponse("not_found", "Doctoral student not found");
        }

        Map<String, Object> data = studentToMap(doctoral.get());
        data.put("is_doctoral", true);
        data.put("education_type", doctoral.get().getEducationType());

        return data;
    }

    /**
     * Check if students are expelled (batch check)
     */
    public Map<String, Object> isExpel(String[] pinfls) {
        log.info("Checking expelled status for {} students", pinfls != null ? pinfls.length : 0);

        Map<String, Object> result = new HashMap<>();

        if (pinfls == null || pinfls.length == 0) {
            result.put("count", 0);
            result.put("results", new HashMap<>());
            return result;
        }

        Map<String, Boolean> expelled = new HashMap<>();

        for (String pinfl : pinfls) {
            Optional<Student> student = studentRepository.findMasterByPinfl(pinfl);
            if (student.isPresent()) {
                expelled.put(pinfl, isExpelledStatus(student.get().getStudentStatus()));
            } else {
                expelled.put(pinfl, null);
            }
        }

        result.put("count", pinfls.length);
        result.put("results", expelled);

        return result;
    }

    /**
     * Get contract information for student
     */
    public Map<String, Object> contractInfo(String pinfl) {
        log.info("Getting contract info - PINFL: {}", pinfl);

        Optional<Student> student = studentRepository.findMasterByPinfl(pinfl);

        if (student.isEmpty()) {
            return CubaResponseHelper.errorResponse("not_found", "Student not found");
        }

        Student s = student.get();

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("pinfl", pinfl);
        result.put("student_id", s.getId());
        result.put("payment_form", s.getPaymentForm());

        // Fetch actual contract data
        List<Contract> contracts = contractRepository.findByStudent(s.getId());
        boolean hasContract = !contracts.isEmpty();
        result.put("has_contract", hasContract);

        if (hasContract) {
            // Return the most recent active contract
            Contract contract = contracts.stream()
                    .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
                    .findFirst()
                    .orElse(contracts.get(0));

            result.put("contract_number", contract.getContractNumber());
            result.put("contract_date", contract.getContractDate());
            result.put("contract_amount", contract.getContractSum());
            result.put("paid_amount", contract.getPaidSum());
            result.put("remaining_amount", contract.getRemainingSum());
            result.put("is_fully_paid", contract.isFullyPaid());
        } else {
            result.put("contract_number", null);
            result.put("contract_date", null);
            result.put("contract_amount", null);
        }

        return result;
    }

    /**
     * Health check / validation
     */
    public Map<String, Object> check() {
        log.debug("Student service health check");

        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        result.put("service", "hemishe_StudentService");
        result.put("timestamp", new Date());

        try {
            long count = studentRepository.count();
            result.put("student_count", count);
            result.put("database", "connected");
        } catch (Exception e) {
            log.error("Database check failed", e);
            result.put("database", "error");
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * List students by university (with pagination)
     */
    public Map<String, Object> students(String university, Integer limit, Integer offset) {
        log.info("Listing students - University: {}, Limit: {}, Offset: {}", university, limit, offset);

        int pageSize = (limit != null && limit > 0) ? Math.min(limit, 1000) : 100;
        int pageOffset = (offset != null && offset >= 0) ? offset : 0;

        List<Map<String, Object>> studentData;
        long totalCount;

        if (university != null && !university.isEmpty()) {
            int pageNum = pageOffset / Math.max(pageSize, 1);
            Page<Student> page = studentRepository.findByUniversity(university, PageRequest.of(pageNum, pageSize));
            studentData = page.getContent().stream()
                    .map(this::studentToMap)
                    .collect(Collectors.toList());
            totalCount = page.getTotalElements();
        } else {
            Page<Student> page = studentRepository.findAll(PageRequest.of(pageOffset / Math.max(pageSize, 1), pageSize));
            studentData = page.getContent().stream()
                    .map(this::studentToMap)
                    .collect(Collectors.toList());
            totalCount = page.getTotalElements();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("university", university);
        result.put("limit", pageSize);
        result.put("offset", pageOffset);
        result.put("count", studentData.size());
        result.put("total", totalCount);
        result.put("students", studentData);

        return result;
    }

    /**
     * Calculate student GPA
     */
    public Map<String, Object> gpa(String pinfl) {
        log.info("Calculating GPA - PINFL: {}", pinfl);

        Optional<Student> student = studentRepository.findMasterByPinfl(pinfl);

        if (student.isEmpty()) {
            return CubaResponseHelper.errorResponse("not_found", "Student not found");
        }

        Student s = student.get();

        // Calculate GPA from grade records
        Double calculatedGpa = gradeRepository.calculateGPA(s.getId());
        long totalGrades = gradeRepository.countByStudent(s.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("pinfl", pinfl);
        result.put("student_id", s.getId());
        result.put("gpa", calculatedGpa != null ? calculatedGpa : 0.0);
        result.put("total_grades", totalGrades);

        return result;
    }

    /**
     * Update student data
     */
    @SuppressWarnings("unchecked")
    @Transactional
    public Map<String, Object> update(Map<String, Object> studentData) {
        log.info("Updating student - Data keys: {}", studentData != null ? studentData.keySet() : "null");

        if (studentData == null || !studentData.containsKey("pinfl")) {
            return CubaResponseHelper.errorResponse("invalid_parameter", "PINFL required");
        }

        String pinfl = (String) studentData.get("pinfl");
        Optional<Student> studentOpt = studentRepository.findMasterByPinfl(pinfl);

        if (studentOpt.isEmpty()) {
            return CubaResponseHelper.errorResponse("not_found", "Student not found");
        }

        Student student = studentOpt.get();

        // Update fields from data map
        if (studentData.containsKey("first_name")) {
            student.setFirstname(str(studentData.get("first_name")));
        }
        if (studentData.containsKey("second_name")) {
            student.setLastname(str(studentData.get("second_name")));
        }
        if (studentData.containsKey("third_name")) {
            student.setFathername(str(studentData.get("third_name")));
        }
        if (studentData.containsKey("phone")) {
            student.setPhone(str(studentData.get("phone")));
        }
        if (studentData.containsKey("email")) {
            student.setEmail(str(studentData.get("email")));
        }
        if (studentData.containsKey("address")) {
            student.setAddress(str(studentData.get("address")));
        }
        if (studentData.containsKey("current_address")) {
            student.setCurrentAddress(str(studentData.get("current_address")));
        }
        if (studentData.containsKey("parent_phone")) {
            student.setParentPhone(str(studentData.get("parent_phone")));
        }
        if (studentData.containsKey("responsible_person_phone")) {
            student.setResponsiblePersonPhone(str(studentData.get("responsible_person_phone")));
        }
        if (studentData.containsKey("geo_address")) {
            student.setGeoAddress(str(studentData.get("geo_address")));
        }
        if (studentData.containsKey("serial_number")) {
            student.setSerialNumber(str(studentData.get("serial_number")));
        }

        studentRepository.save(student);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Student updated successfully");
        result.put("pinfl", pinfl);
        result.put("id", student.getId());

        return result;
    }

    /**
     * Check scholarship eligibility
     */
    public Map<String, Object> checkScholarship(String pinfl) {
        log.info("Checking scholarship eligibility - PINFL: {}", pinfl);

        Optional<Student> student = studentRepository.findMasterByPinfl(pinfl);

        if (student.isEmpty()) {
            return CubaResponseHelper.errorResponse("not_found", "Student not found");
        }

        Student s = student.get();

        // Check: payment_form = '11' (grant/budget), active status, no debts
        boolean isGrant = "11".equals(s.getPaymentForm()) || "10".equals(s.getPaymentForm());
        boolean isActive = isActiveStatus(s.getStudentStatus());

        // Check GPA
        Double gpa = gradeRepository.calculateGPA(s.getId());
        boolean gpaOk = gpa != null && gpa >= 3.5;

        // Check contract debts
        List<Contract> contracts = contractRepository.findByStudent(s.getId());
        boolean hasDebt = contracts.stream().anyMatch(c -> !c.isFullyPaid());

        boolean eligible = isGrant && isActive && !hasDebt;

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("pinfl", pinfl);
        result.put("eligible", eligible);
        result.put("payment_form", s.getPaymentForm());
        result.put("is_grant", isGrant);
        result.put("is_active", isActive);
        result.put("gpa", gpa);
        result.put("gpa_ok", gpaOk);
        result.put("has_debt", hasDebt);

        if (!eligible) {
            List<String> reasons = new ArrayList<>();
            if (!isGrant) reasons.add("Contract students not eligible");
            if (!isActive) reasons.add("Student not in active status");
            if (hasDebt) reasons.add("Student has unpaid debts");
            result.put("reasons", reasons);
        }

        return result;
    }

    /**
     * Check scholarship eligibility (alternative method with semester)
     */
    public Map<String, Object> checkScholarship2(String pinfl, String semester) {
        log.info("Checking scholarship eligibility (v2) - PINFL: {}, Semester: {}", pinfl, semester);

        Optional<Student> student = studentRepository.findMasterByPinfl(pinfl);

        if (student.isEmpty()) {
            return CubaResponseHelper.errorResponse("not_found", "Student not found");
        }

        Student s = student.get();

        // GPA check
        Double gpa = gradeRepository.calculateGPA(s.getId());
        boolean gpaCheck = gpa != null && gpa >= 3.5;

        // Debt check
        List<Contract> contracts = contractRepository.findByStudent(s.getId());
        boolean hasDebt = contracts.stream().anyMatch(c -> !c.isFullyPaid());

        // Payment form check
        boolean isGrant = "11".equals(s.getPaymentForm()) || "10".equals(s.getPaymentForm());

        // Overall eligibility
        boolean eligible = isGrant && gpaCheck && !hasDebt;

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("pinfl", pinfl);
        result.put("semester", semester);
        result.put("eligible", eligible);
        result.put("gpa_check", gpaCheck);
        result.put("gpa", gpa != null ? gpa : 0.0);
        result.put("debt_check", !hasDebt);
        result.put("has_debt", hasDebt);
        result.put("payment_form_check", isGrant);
        result.put("payment_form", s.getPaymentForm());

        return result;
    }

    /**
     * Get student ID by various criteria
     *
     * Data can be: PINFL, passport serial, or student code
     */
    public Map<String, Object> id(String data) {
        log.info("Getting student ID - Data: {}", data);

        if (data == null || data.isEmpty()) {
            return CubaResponseHelper.errorResponse("invalid_parameter", "Data parameter is required");
        }

        // Try to parse as JSON first
        try {
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = mapper.readValue(data, Map.class);

            // Search by specific field from JSON
            if (parsed.containsKey("pinfl")) {
                return findStudentId(studentRepository.findMasterByPinfl(str(parsed.get("pinfl"))));
            }
            if (parsed.containsKey("code")) {
                return findStudentId(studentRepository.findByCode(str(parsed.get("code"))));
            }
            if (parsed.containsKey("passport") || parsed.containsKey("serial_number")) {
                String serial = str(parsed.getOrDefault("passport", parsed.get("serial_number")));
                List<Student> found = studentRepository.findBySerialNumber(serial);
                if (!found.isEmpty()) {
                    return findStudentId(Optional.of(found.get(0)));
                }
                return CubaResponseHelper.errorResponse("not_found", "Student not found");
            }
        } catch (Exception e) {
            // Not JSON - treat as plain search value
            log.debug("Data is not JSON, trying direct search: {}", data);
        }

        // Try multi-criteria search: PINFL or serial number or code
        List<Student> found = studentRepository.findByPinflOrSerialNumber(data);
        if (!found.isEmpty()) {
            return findStudentId(Optional.of(found.get(0)));
        }

        // Try by code
        Optional<Student> byCode = studentRepository.findByCode(data);
        if (byCode.isPresent()) {
            return findStudentId(byCode);
        }

        return CubaResponseHelper.errorResponse("not_found", "Student not found");
    }

    /**
     * Validate student data
     */
    public Map<String, Object> validate(Map<String, Object> studentData) {
        log.info("Validating student data");

        List<String> errors = new ArrayList<>();

        String pinfl = (String) studentData.get("pinfl");
        if (pinfl == null || pinfl.length() != 14) {
            errors.add("PINFL must be 14 digits");
        }

        if (studentData.get("first_name") == null) {
            errors.add("First name is required");
        }
        if (studentData.get("second_name") == null) {
            errors.add("Second name is required");
        }

        if (studentData.get("birth_date") == null) {
            errors.add("Birth date is required");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("valid", errors.isEmpty());
        result.put("errors", errors);

        return result;
    }

    /**
     * Get Tashkent students list
     *
     * ✅ PERFORMANCE FIX: Filters at database level instead of loading all records into memory
     */
    public Map<String, Object> tashkentStudents(String university) {
        log.info("Getting Tashkent students - University: {}", university);

        String universityParam = (university != null && !university.isEmpty()) ? university : null;
        Page<Student> page = studentRepository.findActiveBySoatoPrefix(
                universityParam, TASHKENT_SOATO_PREFIX, PageRequest.of(0, 100));

        List<Map<String, Object>> studentData = page.getContent().stream()
                .map(this::studentToMap)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("university", university);
        result.put("region", "Tashkent");
        result.put("count", studentData.size());
        result.put("total", page.getTotalElements());
        result.put("students", studentData);

        return result;
    }

    /**
     * Get students by Tashkent and payment form
     *
     * ✅ PERFORMANCE FIX: Filters at database level instead of loading all records into memory
     */
    public Map<String, Object> byTashkentAndPaymentForm(String university, String paymentForm) {
        log.info("Getting students - University: {}, PaymentForm: {}", university, paymentForm);

        String universityParam = (university != null && !university.isEmpty()) ? university : null;
        String paymentFormParam = (paymentForm != null && !paymentForm.isEmpty()) ? paymentForm : null;
        Page<Student> page = studentRepository.findActiveBySoatoPrefixAndPaymentForm(
                universityParam, TASHKENT_SOATO_PREFIX, paymentFormParam, PageRequest.of(0, 100));

        List<Map<String, Object>> studentData = page.getContent().stream()
                .map(this::studentToMap)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("university", university);
        result.put("payment_form", paymentForm);
        result.put("region", "Tashkent");
        result.put("count", studentData.size());
        result.put("total", page.getTotalElements());
        result.put("students", studentData);

        return result;
    }

    /**
     * Get contract payment statistics
     */
    public Map<String, Object> contractStatistics(String university) {
        log.info("Getting contract statistics - University: {}", university);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("university", university);

        // Get current education year (approximate)
        String currentYear = String.valueOf(java.time.LocalDate.now().getYear());

        Map<String, Object> stats = new HashMap<>();

        try {
            long activeContracts = contractRepository.countActiveByUniversityAndYear(university, currentYear);
            BigDecimal totalExpected = contractRepository.sumContractByUniversityAndYear(university, currentYear);
            BigDecimal totalPaid = contractRepository.sumPaidByUniversityAndYear(university, currentYear);

            stats.put("total_contract_students", activeContracts);

            if (totalExpected != null) {
                stats.put("total_amount_expected", totalExpected);
            } else {
                stats.put("total_amount_expected", BigDecimal.ZERO);
            }

            if (totalPaid != null) {
                stats.put("total_amount_received", totalPaid);
            } else {
                stats.put("total_amount_received", BigDecimal.ZERO);
            }

            if (totalExpected != null && totalExpected.compareTo(BigDecimal.ZERO) > 0 && totalPaid != null) {
                double rate = totalPaid.doubleValue() / totalExpected.doubleValue() * 100;
                stats.put("payment_rate", Math.round(rate * 10.0) / 10.0);
            } else {
                stats.put("payment_rate", 0.0);
            }
        } catch (Exception e) {
            log.warn("Error fetching contract statistics for university {}: {}", university, e.getMessage());
            stats.put("total_contract_students", 0);
            stats.put("total_amount_expected", BigDecimal.ZERO);
            stats.put("total_amount_received", BigDecimal.ZERO);
            stats.put("payment_rate", 0.0);
        }

        result.put("statistics", stats);

        return result;
    }

    // =====================================================
    // Helper Methods
    // =====================================================

    private Map<String, Object> studentToMap(Student student) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", student.getId());
        map.put("code", student.getCode());
        map.put("pinfl", student.getPinfl());
        map.put("first_name", student.getFirstName());
        map.put("second_name", student.getSecondName());
        map.put("third_name", student.getThirdName());
        map.put("first_name_latin", student.getFirstNameLatin());
        map.put("second_name_latin", student.getSecondNameLatin());
        map.put("third_name_latin", student.getThirdNameLatin());
        map.put("birth_date", student.getBirthDate());
        map.put("university", student.getUniversity());
        map.put("student_status", student.getStudentStatus());
        map.put("payment_form", student.getPaymentForm());
        map.put("gender", student.getGender());
        map.put("citizenship", student.getCitizenship());
        map.put("is_duplicate", student.getIsDuplicate());
        map.put("success", true);
        return map;
    }

    private boolean isActiveStatus(String statusCode) {
        return statusCode != null && ACTIVE_STATUSES.contains(statusCode);
    }

    private boolean isGraduatedStatus(String statusCode) {
        return statusCode != null && GRADUATED_STATUSES.contains(statusCode);
    }

    private boolean isExpelledStatus(String statusCode) {
        return statusCode != null && EXPELLED_STATUSES.contains(statusCode);
    }

    /**
     * Check if student is from Tashkent by SOATO code
     */
    private boolean isTashkentStudent(Student s) {
        String soato = s.getSoato();
        if (soato != null && soato.startsWith(TASHKENT_SOATO_PREFIX)) {
            return true;
        }
        String currentSoato = s.getCurrentSoato();
        return currentSoato != null && currentSoato.startsWith(TASHKENT_SOATO_PREFIX);
    }

    private Map<String, Object> findStudentId(Optional<Student> student) {
        if (student.isEmpty()) {
            return CubaResponseHelper.errorResponse("not_found", "Student not found");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("id", student.get().getId());
        result.put("code", student.get().getCode());
        result.put("pinfl", student.get().getPinfl());
        return result;
    }

    private String str(Object obj) {
        return obj != null ? String.valueOf(obj) : null;
    }
}
