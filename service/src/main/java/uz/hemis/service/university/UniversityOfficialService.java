package uz.hemis.service.university;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import uz.hemis.domain.entity.Employee;
import uz.hemis.domain.entity.EmployeeJobs;
import uz.hemis.domain.repository.EmployeeJobsRepository;
import uz.hemis.domain.repository.EmployeeRepository;
import uz.hemis.service.integration.ApiMspdTokenService;
import uz.hemis.service.university.dto.OfficialDto;
import uz.hemis.service.university.dto.OfficialRequest;

import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * University Official Service — manages rector, prorektors, director appointments.
 *
 * <p>Ministry assigns officials via hemis-back admin panel.
 * Universities read via API (sync).
 * Source = 'ministry' (vs 'hemis_sync' from universities).</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UniversityOfficialService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeJobsRepository employeeJobsRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ApiMspdTokenService tokenService;
    private final ObjectMapper objectMapper;

    // Position codes for university leadership
    private static final List<String> LEADERSHIP_POSITIONS = List.of(
            "20",   // Rektor
            "31",   // Direktor
            "46",   // Birinchi prorektor
            "21",   // Ilmiy ishlar prorektor
            "22",   // O'quv ishlari prorektor
            "23",   // Yoshlar prorektor
            "24",   // Moliya prorektor
            "28",   // AT prorektor
            "32",   // Xalqaro hamkorlik prorektor
            "90",   // Filial direktori
            "241"   // Ijrochi direktor
    );

    /**
     * Get all current officials (leadership) for a university.
     * Reads from employee + employee_jobs WHERE source='ministry'.
     */
    @Transactional(readOnly = true)
    public List<OfficialDto> getOfficials(String universityCode) {
        return getOfficials(universityCode, true);
    }

    /**
     * Get officials — current or all (including history).
     */
    public List<OfficialDto> getOfficials(String universityCode, boolean currentOnly) {
        List<EmployeeJobs> jobs = currentOnly
                ? employeeJobsRepository.findByUniversityCodeAndIsCurrentAndEmployeeType(universityCode, true, "15")
                : employeeJobsRepository.findByUniversityCodeAndEmployeeType(universityCode, "15");

        return jobs.stream().map(meta -> {
            Employee emp = meta.getEmployee();
            String positionName = resolvePositionName(meta.getPositionCode());
            return OfficialDto.builder()
                    .employeeId(emp.getId())
                    .metaId(meta.getId())
                    .pinfl(emp.getPinfl())
                    .firstName(emp.getFirstName())
                    .lastName(emp.getLastName())
                    .middleName(emp.getMiddleName())
                    .phone(emp.getPhone())
                    .positionCode(meta.getPositionCode())
                    .positionName(positionName)
                    .decreeNumber(meta.getDecreeNumber())
                    .decreeDate(meta.getDecreeDate() != null ? meta.getDecreeDate().toString() : null)
                    .startDate(meta.getStartDate() != null ? meta.getStartDate().toString() : null)
                    .endDate(meta.getEndDate() != null ? meta.getEndDate().toString() : null)
                    .current(meta.getIsCurrent())
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * Appoint a university official (rector, prorektor, etc.)
     * Creates employee if not exists, creates employee_jobs with source='ministry'.
     */
    @Transactional
    public OfficialDto appointOfficial(String universityCode, OfficialRequest request) {
        log.info("Appointing official: university={}, pinfl={}, position={}",
                universityCode, request.getPinfl(), request.getPositionCode());

        // Find or create employee
        Employee employee = employeeRepository.findByPinfl(request.getPinfl())
                .orElseGet(() -> {
                    Employee emp = new Employee();
                    emp.setPinfl(request.getPinfl());
                    emp.setFirstName(request.getFirstName());
                    emp.setLastName(request.getLastName());
                    emp.setMiddleName(request.getMiddleName());
                    emp.setPhone(request.getPhone());
                    return employeeRepository.save(emp);
                });

        // Update employee info if needed
        if (request.getPhone() != null) employee.setPhone(request.getPhone());
        employeeRepository.save(employee);

        // Deactivate previous holder of this position at this university
        List<EmployeeJobs> previousHolders = employeeJobsRepository
                .findByUniversityCodeAndPositionCodeAndIsCurrent(universityCode, request.getPositionCode(), true);
        for (EmployeeJobs prev : previousHolders) {
            prev.setIsCurrent(false);
            prev.setEndDate(LocalDate.now());
        }
        employeeJobsRepository.saveAll(previousHolders);

        // Create new appointment
        EmployeeJobs meta = new EmployeeJobs();
        meta.setEmployee(employee);
        meta.setUniversityCode(universityCode);
        meta.setPositionCode(request.getPositionCode());
        meta.setEmployeeType("15"); // Rahbariyat — leadership type
        meta.setIsCurrent(true);
        meta.setStartDate(LocalDate.now());
        // source tracking removed — audit via created_by
        if (request.getDecreeNumber() != null) meta.setDecreeNumber(request.getDecreeNumber());
        if (request.getDecreeDate() != null) {
            try { meta.setDecreeDate(LocalDate.parse(request.getDecreeDate())); } catch (Exception ignored) {}
        }
        meta = employeeJobsRepository.save(meta);

        String positionName = resolvePositionName(request.getPositionCode());
        log.info("Official appointed: {} {} as {} at {}",
                employee.getLastName(), employee.getFirstName(), positionName, universityCode);

        return OfficialDto.builder()
                .employeeId(employee.getId())
                .metaId(meta.getId())
                .pinfl(employee.getPinfl())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .middleName(employee.getMiddleName())
                .phone(employee.getPhone())
                .positionCode(meta.getPositionCode())
                .positionName(positionName)
                .decreeNumber(meta.getDecreeNumber())
                .decreeDate(meta.getDecreeDate() != null ? meta.getDecreeDate().toString() : null)
                .startDate(meta.getStartDate().toString())
                .current(true)
                .build();
    }

    /**
     * Dismiss (deactivate) an official appointment with decree.
     */
    @Transactional
    public void removeOfficial(UUID metaId, String dismissalDecree) {
        employeeJobsRepository.findById(metaId).ifPresent(meta -> {
            meta.setIsCurrent(false);
            meta.setEndDate(LocalDate.now());
            if (dismissalDecree != null && !dismissalDecree.isBlank()) {
                meta.setContractNumber(dismissalDecree); // dismissal decree stored in contract_number
            }
            employeeJobsRepository.save(meta);
            log.info("Official dismissed: metaId={}, decree={}", metaId, dismissalDecree);
        });
    }

    /**
     * Lookup person by PINFL — chain: employee → hemishe_e_teacher → external API.
     * If found in old table or API → saves to employee table.
     */
    @Transactional
    public Map<String, Object> lookupByPinfl(String pinfl, String document, String birthDate) {
        // 1. Check new employee table
        var fromEmployee = employeeRepository.findByPinfl(pinfl);
        if (fromEmployee.isPresent()) {
            Employee emp = fromEmployee.get();
            return buildResult("employee", emp);
        }

        // 2. Check old hemishe_e_teacher → topilsa employee ga saqlash
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                    SELECT firstname, lastname, fathername, phone, pinfl, birthday,
                           _gender, _citizenship, _nationality, serial_number, address,
                           _academic_degree, _academic_rank
                    FROM hemishe_e_teacher WHERE pinfl = ? AND delete_ts IS NULL LIMIT 1
                    """, pinfl);
            if (!rows.isEmpty()) {
                Map<String, Object> row = rows.get(0);
                Employee emp = new Employee();
                emp.setPinfl(pinfl);
                emp.setFirstName(str(row, "firstname"));
                emp.setLastName(str(row, "lastname"));
                emp.setMiddleName(str(row, "fathername"));
                emp.setPhone(str(row, "phone"));
                emp.setAddress(str(row, "address"));
                emp.setGender(str(row, "_gender"));
                emp.setCitizenship(str(row, "_citizenship"));
                emp.setNationality(str(row, "_nationality"));
                // serial_number = "AD1234567" → series="AD", number="1234567"
                String serial = str(row, "serial_number");
                if (serial != null && serial.length() > 2) {
                    emp.setPassportSeries(serial.substring(0, 2));
                    emp.setPassportNumber(serial.substring(2));
                }
                Object bd = row.get("birthday");
                if (bd instanceof java.sql.Date) emp.setBirthDate(((java.sql.Date) bd).toLocalDate());
                emp.setAcademicDegree(str(row, "_academic_degree"));
                emp.setAcademicRank(str(row, "_academic_rank"));
                // source removed — audit via created_by
                emp = employeeRepository.save(emp);
                log.info("Employee created from hemishe_e_teacher: pinfl={}", pinfl);
                return buildResult("hemishe_e_teacher", emp);
            }
        } catch (Exception e) {
            log.debug("Error looking up teacher by PINFL: {}", e.getMessage());
        }

        // 3. External API → topilsa employee ga saqlash + address olish
        if ((document != null && !document.isBlank()) || (birthDate != null && !birthDate.isBlank())) {
            return lookupFromExternalApi(pinfl, document, birthDate);
        }

        return null;
    }

    private Map<String, Object> buildResult(String source, Employee emp) {
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("source", source);
        result.put("firstName", emp.getFirstName());
        result.put("lastName", emp.getLastName());
        result.put("middleName", emp.getMiddleName());
        result.put("phone", emp.getPhone());
        result.put("pinfl", emp.getPinfl());
        result.put("address", emp.getAddress());
        return result;
    }

    private String str(Map<String, Object> row, String key) {
        Object v = row.get(key);
        return v != null ? v.toString() : null;
    }

    /**
     * Fetch person data from external API (172.18.9.171/person/person-data/)
     * API requires at least 2 of: pinfl, document, birth_date
     */
    public Map<String, Object> lookupFromExternalApi(String pinfl, String document, String birthDate) {
        String token = tokenService.getAccessToken();
        if (token == null) return null;

        try {
            String url = tokenService.getBaseUrl() + "/person/person-data/";

            // Build request with available fields
            StringBuilder bodyBuilder = new StringBuilder("{");
            bodyBuilder.append("\"pinfl\": \"").append(pinfl).append("\"");
            if (document != null && !document.isBlank()) {
                bodyBuilder.append(", \"document\": \"").append(document).append("\"");
            }
            if (birthDate != null && !birthDate.isBlank()) {
                bodyBuilder.append(", \"birth_date\": \"").append(birthDate).append("\"");
            }
            bodyBuilder.append("}");
            String body = bodyBuilder.toString();

            HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            try (var os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            if (status == 200) {
                String responseStr = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                conn.disconnect();
                JsonNode json = objectMapper.readTree(responseStr);

                String firstName = textOrNull(json, "name");
                String lastName = textOrNull(json, "surname");
                String middleName = textOrNull(json, "patronym");

                if (lastName != null || firstName != null) {
                    // Save to employee table
                    Employee emp = new Employee();
                    emp.setPinfl(pinfl);
                    emp.setFirstName(firstName);
                    emp.setLastName(lastName);
                    emp.setMiddleName(middleName);
                    emp.setGender(textOrNull(json, "sex"));
                    emp.setCitizenship(textOrNull(json, "citizenship"));
                    emp.setNationality(textOrNull(json, "nationality"));
                    emp.setPassportSeries(textOrNull(json, "doc_serial"));
                    emp.setPassportNumber(textOrNull(json, "doc_number"));
                    String bd = textOrNull(json, "birth_date");
                    if (bd != null) {
                        try { emp.setBirthDate(java.time.LocalDate.parse(bd)); } catch (Exception ignored) {}
                    }
                    // source removed — audit via created_by

                    // Fetch address from /person/person-address/
                    String address = fetchPersonAddress(pinfl, token);
                    if (address != null) emp.setAddress(address);

                    emp = employeeRepository.save(emp);
                    log.info("Employee created from external API: pinfl={}", pinfl);
                    return buildResult("external_api", emp);
                }
            }
            conn.disconnect();
        } catch (Exception e) {
            log.debug("External person API error for PINFL {}: {}", pinfl, e.getMessage());
        }
        return null;
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode v = node.path(field);
        return v.isNull() || v.isMissingNode() ? null : v.asText();
    }

    /**
     * Fetch person address from /person/person-address/ API.
     */
    private String fetchPersonAddress(String pinfl, String token) {
        try {
            String url = tokenService.getBaseUrl() + "/person/person-address/";
            String body = "{\"pinfl\": \"" + pinfl + "\"}";

            HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            try (var os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            if (status == 200) {
                String resp = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                conn.disconnect();
                JsonNode json = objectMapper.readTree(resp);
                // Build address from response fields
                String region = textOrNull(json, "regionName");
                String district = textOrNull(json, "districtName");
                String address = textOrNull(json, "address");
                StringBuilder sb = new StringBuilder();
                if (region != null) sb.append(region);
                if (district != null) { if (!sb.isEmpty()) sb.append(", "); sb.append(district); }
                if (address != null) { if (!sb.isEmpty()) sb.append(", "); sb.append(address); }
                return sb.isEmpty() ? null : sb.toString();
            }
            conn.disconnect();
        } catch (Exception e) {
            log.debug("Person address API error for PINFL {}: {}", pinfl, e.getMessage());
        }
        return null;
    }

    /**
     * Get leadership positions from NEW position table (type_code = '15').
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getLeadershipPositions() {
        return jdbcTemplate.queryForList(
                "SELECT code, name FROM position WHERE type_code = '15' AND is_active = true ORDER BY name");
    }

    private String resolvePositionName(String positionCode) {
        if (positionCode == null) return null;
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT name FROM position WHERE code = ?", positionCode);
            return rows.isEmpty() ? positionCode : rows.get(0).get("name").toString();
        } catch (Exception e) {
            return positionCode;
        }
    }
}
