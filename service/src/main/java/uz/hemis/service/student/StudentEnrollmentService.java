package uz.hemis.service.student;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.student.StudentIdRequest;
import uz.hemis.common.exception.BadRequestException;
import uz.hemis.domain.entity.student.Student;
import uz.hemis.service.student.mapper.StudentLegacyMapper;
import uz.hemis.domain.repository.StudentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Student Enrollment Service - update, validate, check, ID generation
 *
 * <p>Extracted from StudentService as part of service decomposition.</p>
 *
 * @since 2.1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StudentEnrollmentService {

    private final StudentRepository studentRepository;
    private final StudentLegacyMapper studentLegacyMapper;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Update student information (university transfer)
     */
    @Transactional
    @SuppressWarnings("unchecked")
    public Object updateStudent(Map<String, Object> request) {
        log.info("Updating student: {}", request);

        Object studentObj = request.get("student");
        Map<String, Object> studentData;
        if (studentObj instanceof Map) {
            studentData = (Map<String, Object>) studentObj;
        } else if (studentObj == null) {
            studentData = request;
        } else {
            throw new IllegalArgumentException("'student' field must be a JSON object, got: " + studentObj.getClass().getSimpleName());
        }

        String studentIdStr = (String) studentData.get("id");
        if (studentIdStr == null) {
            return Map.of("success", false, "error", "Student ID required");
        }

        UUID studentId;
        try {
            studentId = UUID.fromString(studentIdStr);
        } catch (IllegalArgumentException e) {
            return Map.of("success", false, "error", "Invalid student ID format");
        }

        String targetUniversityCode = null;
        Object universityObj = studentData.get("university");
        if (universityObj instanceof Map) {
            targetUniversityCode = (String) ((Map<String, Object>) universityObj).get("code");
        } else if (universityObj instanceof String) {
            targetUniversityCode = (String) universityObj;
        }

        String statusCode = null;
        Object statusObj = studentData.get("studentStatus");
        if (statusObj instanceof Map) {
            statusCode = (String) ((Map<String, Object>) statusObj).get("code");
        } else if (statusObj instanceof String) {
            statusCode = (String) statusObj;
        }

        log.debug("Update request - studentId: {}, targetUniversity: {}, status: {}",
                studentId, targetUniversityCode, statusCode);

        if ("12".equals(statusCode) && targetUniversityCode != null) {
            Optional<Student> transferCandidate = studentRepository.findStudentForTransfer(
                    studentId, targetUniversityCode);

            if (transferCandidate.isPresent()) {
                Student originalStudent = transferCandidate.get();
                log.info("Student transfer detected - {} from {} to {}",
                        originalStudent.getPinfl(), originalStudent.getUniversity(), targetUniversityCode);

                Student transferredStudent = copyStudentForTransfer(originalStudent, targetUniversityCode, statusCode);

                Student saved = studentRepository.save(transferredStudent);
                log.info("Student transferred successfully - new ID: {}, new code: {}",
                        saved.getId(), saved.getCode());

                return null;
            } else {
                log.debug("No valid transfer candidate found for student ID: {}", studentId);
            }
        }

        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            log.warn("Student not found for update: {}", studentId);
            return Map.of("success", false, "error", "Student not found");
        }

        Student student = studentOpt.get();
        updateStudentFields(student, studentData);
        student.setUpdateTs(LocalDateTime.now());

        Student saved = studentRepository.save(student);
        log.info("Student updated successfully - ID: {}, code: {}", saved.getId(), saved.getCode());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", saved.getId().toString());
        response.put("code", saved.getCode());
        response.put("verified", saved.getVerified());
        response.put("points", saved.getPoints());
        return response;
    }

    /**
     * Validate student status (OLD-HEMIS Compatible).
     */
    public Map<String, Object> validateStudent(String data) {
        log.info("Validating student by PINFL or Serial: {}", data);
        Map<String, Object> result = new LinkedHashMap<>();

        try {
            List<Student> students = studentRepository.findByPinflOrSerialNumber(data);

            if (students.isEmpty()) {
                log.info("Student not found for data: {} - can create new", data);
                result.put("success", true);
                result.put("code", "not_active");
                result.put("message", "Student not found. You can create it!");
                return result;
            }

            Student student = students.get(0);
            String statusCode = student.getStudentStatus();
            log.info("Found student: {} with status: {}", student.getCode(), statusCode);

            String code;
            String message;

            if ("14".equals(statusCode)) {
                code = "graduated";
                message = "Student is graduated!";
            } else if ("12".equals(statusCode) || "16".equals(statusCode) ||
                       "17".equals(statusCode) || "18".equals(statusCode)) {
                code = "not_active";
                message = "Student is not active. You can create it again!";
            } else {
                code = "active";
                message = "Student is active! You can not create it again!";
            }

            result.put("success", true);
            result.put("code", code);
            result.put("message", message);
            result.put("data", studentLegacyMapper.toLegacyMapForService(student));

            return result;

        } catch (Exception e) {
            log.error("Error validating student: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("code", "error");
            result.put("message", "Error validating student: " + e.getMessage());
            return result;
        }
    }

    /**
     * OLD-HEMIS: StudentServiceBean.check()
     */
    public Map<String, Object> checkStudents() {
        log.info("CUBA API: check students against MVD");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("all_count", 0);
        result.put("incorrect_count", 0);
        result.put("no_data_count", 0);
        result.put("incorrect", List.of());
        result.put("no_personal_data", List.of());
        return result;
    }

    /**
     * Generate or retrieve student unique ID (OLD-HEMIS compatible)
     */
    @Transactional
    public Map<String, Object> generateStudentId(StudentIdRequest data, String universityCode) {
        Map<String, Object> result = new LinkedHashMap<>();
        log.info("Generating student ID - PINFL: {}, Serial: {}, University: {}",
                data.getPinfl(), data.getSerial(), universityCode);

        try {
            data.validate();
        } catch (IllegalArgumentException ex) {
            log.warn("Validation failed: {}", ex.getMessage());
            result.put("success", false);
            result.put("message", ex.getMessage());
            result.put("data", data);
            return result;
        }

        try {
            jdbcTemplate.queryForMap(
                    "SELECT code FROM citizenship WHERE code = ? AND is_active = true",
                    data.getCitizenship());
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            log.warn("Citizenship code not found: {}", data.getCitizenship());
            result.put("success", false);
            result.put("message", "Citizenship value not available!");
            result.put("data", data);
            return result;
        }

        String idData;
        if ("11".equals(data.getCitizenship())) {
            idData = data.getPinfl();
        } else {
            idData = data.getSerial();
        }

        Optional<Student> activeStudent = findActiveStudent(idData, data.getCitizenship());
        if (activeStudent.isPresent()) {
            log.warn("Student is already active: {}", activeStudent.get().getCode());
            result.put("success", false);
            result.put("message", "Student is active!");
            result.put("is_active", true);
            result.put("student", studentLegacyMapper.toLegacyMapForService(activeStudent.get()));
            return result;
        }

        Optional<Student> existingStudent = findExistingStudent(data);
        if (existingStudent.isPresent()) {
            Student student = existingStudent.get();
            log.info("Found existing student: {}", student.getCode());
            result.put("success", true);
            result.put("is_new", false);
            result.put("unique_id", student.getCode());
            result.put("student", studentLegacyMapper.toLegacyMapForService(student));
            return result;
        }

        try {
            String uniqueCode = generateUniqueCode(universityCode, data.getYear(), data.getEducationType());

            Student newStudent = new Student();
            newStudent.setPinfl(data.getPinfl());
            newStudent.setSerialNumber(data.getSerial());
            newStudent.setCode(uniqueCode);
            newStudent.setUniversity(universityCode);
            newStudent.setEducationYear(data.getYear());
            newStudent.setEducationType(data.getEducationType());
            newStudent.setEducationForm(data.getEducationForm());
            newStudent.setCitizenship(data.getCitizenship());
            newStudent.setStudentStatus("10");
            newStudent.setIsDuplicate(false);

            Student saved = studentRepository.save(newStudent);
            log.info("New student created with code: {}", uniqueCode);

            if (data.getPinfl() != null && !data.getPinfl().isEmpty()) {
                int updatedCount = studentRepository.markPreviousMastersAsDuplicates(data.getPinfl());
                if (updatedCount > 0) {
                    log.info("Reset {} isDuplicate=TRUE record(s) to FALSE for PINFL: {}", updatedCount, data.getPinfl());
                }
            }

            result.put("success", true);
            result.put("is_new", true);
            result.put("unique_id", uniqueCode);
            result.put("university", universityCode);
            result.put("student", studentLegacyMapper.toLegacyMapForService(saved));
            return result;

        } catch (Exception ex) {
            log.error("Error creating student: {}", ex.getMessage(), ex);
            result.put("success", false);
            result.put("message", "No results");
            result.put("data", Map.of(
                    "citizenship", data.getCitizenship(),
                    "pinfl", data.getPinfl(),
                    "serial", data.getSerial(),
                    "year", data.getYear(),
                    "education_type", data.getEducationType(),
                    "education_form", data.getEducationForm()
            ));
            return result;
        }
    }

    // =====================================================
    // Private helper methods
    // =====================================================

    private Optional<Student> findActiveStudent(String idData, String citizenship) {
        List<Student> masterStudents = studentRepository.findActiveByPinflAndDuplicate(idData, true);
        if (!masterStudents.isEmpty()) {
            log.debug("Master record (isDuplicate=true) found for PINFL: {} - NOT active", idData);
            return Optional.empty();
        }

        if ("11".equals(citizenship)) {
            List<Student> students = studentRepository.findActiveByPinfl(idData);
            if (!students.isEmpty()) {
                return Optional.of(students.get(0));
            }
        }

        List<Student> bySerial = studentRepository.findActiveBySerialNumber(idData);
        return bySerial.isEmpty() ? Optional.empty() : Optional.of(bySerial.get(0));
    }

    private Optional<Student> findExistingStudent(StudentIdRequest data) {
        List<Student> students;
        if ("11".equals(data.getCitizenship())) {
            students = studentRepository.findExistingStudent(
                    data.getPinfl(),
                    data.getEducationType(),
                    data.getYear()
            );
        } else {
            students = studentRepository.findExistingForeignStudent(
                    data.getSerial(),
                    data.getCitizenship(),
                    data.getEducationType(),
                    data.getYear()
            );
        }
        return students.isEmpty() ? Optional.empty() : Optional.of(students.get(0));
    }

    private String generateUniqueCode(String universityCode, String year, String educationType) {
        String yearSuffix = year.length() >= 2 ? year.substring(year.length() - 2) : year;

        long count = studentRepository.countForIdGeneration(universityCode, educationType, year) + 1;

        String uniqueCode;
        int iterations = 0;
        final int MAX_ITERATIONS = 1000;

        do {
            String sequence = String.format("%05d", count);
            uniqueCode = universityCode + yearSuffix + educationType + sequence;
            count++;
            iterations++;

            if (iterations > MAX_ITERATIONS) {
                throw new BadRequestException("Unable to generate unique student code after " + MAX_ITERATIONS + " attempts (sequence exhausted)");
            }
        } while (studentRepository.existsByCode(uniqueCode));

        return uniqueCode;
    }

    private Student copyStudentForTransfer(Student original, String targetUniversityCode, String statusCode) {
        Student copy = new Student();

        copy.setId(UUID.randomUUID());

        String baseCode = original.getCode();
        String newCode = baseCode + "0";
        int suffix = 0;
        while (studentRepository.existsByCode(newCode)) {
            suffix++;
            newCode = baseCode + "0".repeat(suffix + 1);
            if (suffix > 10) {
                newCode = baseCode + "_" + System.currentTimeMillis();
                break;
            }
        }
        copy.setCode(newCode);

        copy.setUniversity(targetUniversityCode);
        copy.setStudentStatus(statusCode);

        copy.setFirstname(original.getFirstName());
        copy.setLastname(original.getLastname());
        copy.setFathername(original.getFathername());
        copy.setPinfl(original.getPinfl());
        copy.setSerialNumber(original.getSerialNumber());
        copy.setBirthday(original.getBirthday());
        copy.setPhone(original.getPhone());
        copy.setAddress(original.getAddress());
        copy.setCurrentAddress(original.getCurrentAddress());
        copy.setSoato(original.getSoato());
        copy.setCurrentSoato(original.getCurrentSoato());
        copy.setFaculty(original.getFaculty());
        copy.setSpeciality(original.getSpeciality());
        copy.setPaymentForm(original.getPaymentForm());
        copy.setEducationType(original.getEducationType());
        copy.setEducationForm(original.getEducationForm());
        copy.setEducationYear(original.getEducationYear());
        copy.setCourse(original.getCourse());
        copy.setGroupId(original.getGroupId());
        copy.setGroupName(original.getGroupName());
        copy.setGender(original.getGender());
        copy.setCitizenship(original.getCitizenship());
        copy.setCountry(original.getCountry());
        copy.setNationality(original.getNationality());
        copy.setSocialCategory(original.getSocialCategory());
        copy.setAccomodation(original.getAccomodation());
        copy.setActive(original.getActive());
        copy.setIsDuplicate(original.getIsDuplicate());

        copy.setCreateTs(LocalDateTime.now());
        copy.setCreatedBy(original.getCreatedBy());

        return copy;
    }

    @SuppressWarnings("unchecked")
    private void updateStudentFields(Student student, Map<String, Object> data) {
        if (data.containsKey("firstname"))    student.setFirstname(safeString(data.get("firstname")));
        if (data.containsKey("lastname"))     student.setLastname(safeString(data.get("lastname")));
        if (data.containsKey("fathername"))   student.setFathername(safeString(data.get("fathername")));
        if (data.containsKey("pinfl"))        student.setPinfl(safeString(data.get("pinfl")));
        if (data.containsKey("serialNumber")) student.setSerialNumber(safeString(data.get("serialNumber")));
        if (data.containsKey("phone"))        student.setPhone(safeString(data.get("phone")));
        if (data.containsKey("email"))        student.setEmail(safeString(data.get("email")));
        if (data.containsKey("address"))      student.setAddress(safeString(data.get("address")));
        if (data.containsKey("currentAddress")) student.setCurrentAddress(safeString(data.get("currentAddress")));
        if (data.containsKey("responsiblePersonPhone")) student.setResponsiblePersonPhone(safeString(data.get("responsiblePersonPhone")));
        if (data.containsKey("parentPhone")) student.setParentPhone(safeString(data.get("parentPhone")));
        if (data.containsKey("geoAddress"))  student.setGeoAddress(safeString(data.get("geoAddress")));
        if (data.containsKey("groupId"))     student.setGroupId(safeString(data.get("groupId")));
        if (data.containsKey("groupName"))   student.setGroupName(safeString(data.get("groupName")));
        if (data.containsKey("isGraduate"))  student.setIsGraduate(safeString(data.get("isGraduate")));
        if (data.containsKey("tag"))         student.setTag(safeString(data.get("tag")));
        if (data.containsKey("enrollOrderNumber"))   student.setEnrollOrderNumber(safeString(data.get("enrollOrderNumber")));
        if (data.containsKey("enrollOrderName"))     student.setEnrollOrderName(safeString(data.get("enrollOrderName")));
        if (data.containsKey("enrollOrderCategory")) student.setEnrollOrderCategory(safeString(data.get("enrollOrderCategory")));
        if (data.containsKey("statusOrderNumber"))   student.setStatusOrderNumber(safeString(data.get("statusOrderNumber")));
        if (data.containsKey("statusOrderName"))     student.setStatusOrderName(safeString(data.get("statusOrderName")));
        if (data.containsKey("statusOrderCategory")) student.setStatusOrderCategory(safeString(data.get("statusOrderCategory")));

        if (data.containsKey("gender"))              student.setGender(extractCode(data.get("gender")));
        if (data.containsKey("university"))          student.setUniversity(extractCode(data.get("university")));
        if (data.containsKey("educationYear"))       student.setEducationYear(extractCode(data.get("educationYear")));
        if (data.containsKey("country"))             student.setCountry(extractCode(data.get("country")));
        if (data.containsKey("citizenship"))         student.setCitizenship(extractCode(data.get("citizenship")));
        if (data.containsKey("nationality"))         student.setNationality(extractCode(data.get("nationality")));
        if (data.containsKey("accomodation"))        student.setAccomodation(extractCode(data.get("accomodation")));
        if (data.containsKey("soato"))               student.setSoato(extractCode(data.get("soato")));
        if (data.containsKey("currentSoato"))        student.setCurrentSoato(extractCode(data.get("currentSoato")));
        if (data.containsKey("paymentForm"))         student.setPaymentForm(extractCode(data.get("paymentForm")));
        if (data.containsKey("educationForm"))       student.setEducationForm(extractCode(data.get("educationForm")));
        if (data.containsKey("educationType"))       student.setEducationType(extractCode(data.get("educationType")));
        if (data.containsKey("course"))              student.setCourse(extractCode(data.get("course")));
        if (data.containsKey("language"))            student.setLanguage(extractCode(data.get("language")));
        if (data.containsKey("faculty"))             student.setFaculty(extractCode(data.get("faculty")));
        if (data.containsKey("studentStatus"))       student.setStudentStatus(extractCode(data.get("studentStatus")));
        if (data.containsKey("socialCategory"))      student.setSocialCategory(extractCode(data.get("socialCategory")));
        if (data.containsKey("expelReason"))         student.setExpelReason(extractCode(data.get("expelReason")));
        if (data.containsKey("livingStatus"))        student.setLivingStatus(extractCode(data.get("livingStatus")));
        if (data.containsKey("roommateType"))        student.setRoommateType(extractCode(data.get("roommateType")));
        if (data.containsKey("statusEducationYear")) student.setStatusEducationYearCode(extractCode(data.get("statusEducationYear")));
        if (data.containsKey("currentEducationYear")) student.setCurrentEducationYearCode(extractCode(data.get("currentEducationYear")));
        if (data.containsKey("studentType"))         student.setStudentType(extractCode(data.get("studentType")));
        if (data.containsKey("academicMobileType"))  student.setAcademicMobileType(extractCode(data.get("academicMobileType")));
        if (data.containsKey("academicReason"))      student.setAcademicReason(extractCode(data.get("academicReason")));
        if (data.containsKey("terrain"))             student.setTerrain(extractCode(data.get("terrain")));
        if (data.containsKey("currentTerrain"))      student.setCurrentTerrainCode(extractCode(data.get("currentTerrain")));
        if (data.containsKey("admissionType"))       student.setAdmissionType(extractCode(data.get("admissionType")));
        if (data.containsKey("transferCountry"))     student.setTransferCountry(extractCode(data.get("transferCountry")));
        if (data.containsKey("transferType"))        student.setTransferType(extractCode(data.get("transferType")));
        if (data.containsKey("povertyLevel"))        student.setPovertyLevel(extractCode(data.get("povertyLevel")));
        if (data.containsKey("grantType"))           student.setGrantType(extractCode(data.get("grantType")));
        if (data.containsKey("stipendRate"))         student.setStipendRate(extractCode(data.get("stipendRate")));
        if (data.containsKey("doctoralStudentType")) student.setDoctoralStudentType(extractCode(data.get("doctoralStudentType")));
        if (data.containsKey("graduationYear"))      student.setGraduationYear(extractCode(data.get("graduationYear")));

        if (data.containsKey("decreeInfoName"))      student.setDecreeInfoName(safeString(data.get("decreeInfoName")));
        if (data.containsKey("decreeInfoNumber"))    student.setDecreeInfoNumber(safeString(data.get("decreeInfoNumber")));
        if (data.containsKey("transferUniversity"))  student.setTransferUniversity(safeString(data.get("transferUniversity")));
        if (data.containsKey("studyDuration"))       student.setStudyDuration(safeString(data.get("studyDuration")));

        if (data.containsKey("specialityBachelor"))   student.setSpecialityBachelor(extractUuid(data.get("specialityBachelor")));
        if (data.containsKey("specialityMaster"))     student.setSpecialityMaster(extractUuid(data.get("specialityMaster")));
        if (data.containsKey("specialityOrdinatura")) student.setSpecialityOrdinatura(extractUuid(data.get("specialityOrdinatura")));
        if (data.containsKey("specialityDoctoral"))   student.setSpecialityDoctoral(extractUuid(data.get("specialityDoctoral")));

        if (data.containsKey("birthday"))          student.setBirthday(parseDate(data.get("birthday")));
        if (data.containsKey("passportGivenDate")) student.setPassportGivenDate(parseDate(data.get("passportGivenDate")));
        if (data.containsKey("enrollOrderDate"))   student.setEnrollOrderDate(parseDate(data.get("enrollOrderDate")));
        if (data.containsKey("statusOrderDate"))   student.setStatusOrderDate(parseDate(data.get("statusOrderDate")));
        if (data.containsKey("decreeInfoDate"))    student.setDecreeInfoDate(parseDate(data.get("decreeInfoDate")));
        if (data.containsKey("eduStartDate"))      student.setEduStartDate(parseDate(data.get("eduStartDate")));
        if (data.containsKey("graduationDate"))    student.setGraduationDate(parseDate(data.get("graduationDate")));

        if (data.containsKey("roommateCount")) {
            Object val = data.get("roommateCount");
            if (val instanceof Number) {
                student.setRoommateCount(((Number) val).intValue());
            } else if (val instanceof String) {
                try {
                    student.setRoommateCount(Integer.parseInt((String) val));
                } catch (NumberFormatException ignored) { }
            }
        }
    }

    private String safeString(Object obj) {
        if (obj == null) return null;
        String str = String.valueOf(obj);
        return "null".equals(str) ? null : str;
    }

    @SuppressWarnings("unchecked")
    private String extractCode(Object obj) {
        if (obj instanceof Map) {
            Object code = ((Map<String, Object>) obj).get("code");
            return code != null ? String.valueOf(code) : null;
        } else if (obj != null) {
            return String.valueOf(obj);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private UUID extractUuid(Object obj) {
        String idStr = null;
        if (obj instanceof Map) {
            Object id = ((Map<String, Object>) obj).get("id");
            idStr = id != null ? String.valueOf(id) : null;
        } else if (obj != null) {
            idStr = String.valueOf(obj);
        }
        if (idStr == null || idStr.equals("null")) return null;
        try {
            return UUID.fromString(idStr);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID in student update: {}", idStr);
            return null;
        }
    }

    private LocalDate parseDate(Object obj) {
        if (obj == null) return null;
        String dateStr = obj.toString();
        if (dateStr.isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            log.warn("Invalid date in student update: {}", dateStr);
            return null;
        }
    }
}
