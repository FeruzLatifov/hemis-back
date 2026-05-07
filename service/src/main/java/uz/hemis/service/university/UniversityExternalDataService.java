package uz.hemis.service.university;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import uz.hemis.common.audit.AuditAction;
import uz.hemis.common.audit.Audited;
import uz.hemis.common.exception.BadRequestException;
import uz.hemis.common.vo.Pinfl;
import uz.hemis.domain.entity.employee.Employee;
import uz.hemis.domain.entity.university.Organization;
import uz.hemis.domain.entity.university.University;
import uz.hemis.domain.entity.university.UniversityFounder;
import uz.hemis.domain.repository.UniversityFounderRepository;
import uz.hemis.domain.repository.UniversityRepository;
import uz.hemis.service.integration.ApiMspdClient;
import uz.hemis.service.integration.model.GatewayResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * University External Data Service
 *
 * <p>Fetches founders data from external API (172.18.9.171) and persists it to
 * {@code university_founder}. The legal-entity snapshot (TIN, registration trail,
 * officials) is NOT cached locally — fetched on-demand by callers, since all
 * useful columns either duplicate {@code hemishe_e_university} or have no consumer.
 * Cadastre data is stored as a JSONB snapshot on {@code university_building}
 * (caller-provided), not via this service.</p>
 *
 * <p>API endpoints:</p>
 * <ul>
 *   <li>POST /legalentity/legalentity-info/ — founders (rest of payload ignored)</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UniversityExternalDataService {

    private final ApiMspdClient apiMspdClient;
    private final UniversityRepository universityRepository;
    private final UniversityFounderRepository founderRepository;
    private final uz.hemis.domain.repository.EmployeeRepository employeeRepository;
    private final uz.hemis.domain.repository.OrganizationRepository organizationRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    // =====================================================
    // FOUNDERS (extracted from /legalentity/legalentity-info/)
    // =====================================================

    /**
     * Fetch founders from legal-entity API and persist them.
     * Idempotent: rewrites the founders for {@code universityCode} every call.
     *
     * <p>API javobining shakli kutilgan: root'da {@code "founders"} array.
     * Agar gateway response'ni wrap qilib qaytarsa ({@code data.founders}), bu yerda
     * {@code BadRequestException} otiladi — silent skip emas.</p>
     */
    @Transactional
    public void syncFoundersFromApi(String universityCode, String tin) {
        log.info("Syncing founders for university={}, tin={}", universityCode, tin);
        JsonNode response = callApi("/legalentity/legalentity-info/", Map.of("tin", tin));
        JsonNode foundersNode = response.path("founders");
        if (foundersNode.isMissingNode()) {
            log.error("API javobida 'founders' yo'q. Response shakli: {}",
                    response.isObject() ? response.fieldNames() : response.getNodeType());
            throw new BadRequestException(
                    "Tashqi API javobida 'founders' ma'lumoti yo'q (response shape o'zgargan bo'lishi mumkin)");
        }
        syncFounders(universityCode, foundersNode);
    }

    private void syncFounders(String universityCode, JsonNode foundersNode) {
        // Delete ALL existing founders for this university (idempotent sync).
        // deleteAllInBatch + flush forces the DELETE to hit the DB before the
        // subsequent INSERT, otherwise Hibernate's action queue runs INSERTs
        // first and idx_ufounder_unique_current_legal trips on the same key.
        List<UniversityFounder> existing = founderRepository.findByUniversityCode(universityCode);
        if (!existing.isEmpty()) {
            founderRepository.deleteAllInBatch(existing);
            founderRepository.flush();
            log.info("Deleted {} old founders for university={}", existing.size(), universityCode);
        }

        if (!foundersNode.isArray()) {
            // Tashqi API'da founders yo'q — bu yangi universitet uchun normal hol
            // (asoschilar hali rasmiylanmagan). Mavjudlari tozalandi, yangisi yo'q.
            log.warn("Founders array bo'sh yoki noto'g'ri shaklda university={} — mavjudlari tozalandi", universityCode);
            return;
        }

        // Create fresh from API
        List<UniversityFounder> newFounders = new ArrayList<>();
        int skipped = 0;
        for (JsonNode founderNode : foundersNode) {
            UniversityFounder founder = new UniversityFounder();
            founder.setUniversityCode(universityCode);

            JsonNode individual = founderNode.path("founderIndividual");
            JsonNode legal = founderNode.path("founderLegal");

            try {
                if (!individual.isMissingNode() && !individual.isNull()) {
                    founder.setFounderType(uz.hemis.domain.entity.enums.FounderType.INDIVIDUAL);
                    String founderPinfl = textOrNull(individual, "pinfl");
                    if (founderPinfl != null && !founderPinfl.isBlank()) {
                        Employee emp = findOrCreateEmployee(founderPinfl, individual, null, universityCode);
                        founder.setEmployee(emp);
                    }
                    BigDecimal percent = decimalOrNull(individual, "founderSharePercent");
                    founder.setSharePercent(percent);
                    founder.setShareSum(longOrNull(individual, "founderShareSum"));
                } else if (!legal.isMissingNode() && !legal.isNull()) {
                    founder.setFounderType(uz.hemis.domain.entity.enums.FounderType.LEGAL);
                    String legalTin = textOrNull(legal, "tin");
                    if (legalTin != null && !legalTin.isBlank()) {
                        Organization org = findOrCreateOrganization(legalTin, legal);
                        founder.setOrganization(org);
                    }
                    BigDecimal percent = decimalOrNull(legal, "founderSharePercent");
                    founder.setSharePercent(percent);
                    founder.setShareSum(longOrNull(legal, "founderShareSum"));
                } else {
                    // Founder ichida na individual, na legal — record buzilgan
                    skipped++;
                    continue;
                }

                newFounders.add(founder);
            } catch (IllegalArgumentException e) {
                // Pinfl/Tin VO validation (14 raqam emas, format buzuq) — alohida record skip qilinadi,
                // butun batch fail bo'lmasin. Validation loyiha doirasidan tashqarida (tashqi API ma'lumoti).
                log.warn("Founder skip qilindi (validation): {}", e.getMessage());
                skipped++;
            }
        }

        founderRepository.saveAll(newFounders);
        log.info("Saved {} founders for university={} (skipped={})", newFounders.size(), universityCode, skipped);
    }

    // =====================================================
    // SYNC ALL for a university
    // =====================================================

    /**
     * Sync all external data (founders) for a university.
     * Resolves TIN from university record automatically.
     *
     * <p><strong>Cache evict:</strong> {@code universityDashboard} va
     * {@code universityFounders} — sync DB'ga yozadi, lekin
     * {@code UniversityInfoService.getUniversityDashboard}/{@code getFounders}
     * {@code @Cacheable} bo'lgani uchun evict qilinmasa, controller darhol
     * eski cached qiymatni qaytaradi (foydalanuvchi "saqlanmadi" deb ko'radi).</p>
     */
    @Audited(action = AuditAction.UPDATE, entity = "University",
            entityClass = University.class, keyArg = "universityCode")
    @Caching(evict = {
            @CacheEvict(value = "universityDashboard", key = "#universityCode"),
            @CacheEvict(value = "universityFounders", key = "#universityCode")
    })
    @Transactional
    public void syncAll(String universityCode) {
        String tin = resolveTin(universityCode);
        syncAll(universityCode, tin);
    }

    /**
     * Sync all external data (founders) for a university
     */
    @Transactional
    public void syncAll(String universityCode, String tin) {
        log.info("Full external data sync for university={}, tin={}", universityCode, tin);
        syncFoundersFromApi(universityCode, tin);
        log.info("Full sync completed for university={}", universityCode);
    }

    /**
     * Resolve university TIN from university code. Throws BadRequestException if not found or empty.
     */
    public String resolveTin(String universityCode) {
        return universityRepository.findById(universityCode)
                .map(u -> {
                    String tin = u.getTin();
                    if (tin == null || tin.isBlank()) {
                        throw new BadRequestException(
                                "University TIN is empty for code: " + universityCode);
                    }
                    return tin;
                })
                .orElseThrow(() -> new BadRequestException(
                        "University not found: " + universityCode));
    }

    // =====================================================
    // HTTP HELPER
    // =====================================================

    /**
     * api-mspd gateway'ga so'rov yuboradi. 200 bo'lmasa — exception otiladi
     * (silent fail YO'Q, foydalanuvchi xatolikni ko'radi).
     *
     * @return JSON response body (200 OK)
     * @throws BadRequestException tashqi API xatolik qaytardi yoki tarmoq xatosi
     */
    private JsonNode callApi(String path, Object body) {
        GatewayResult result;
        try {
            result = apiMspdClient.post(path, body);
        } catch (BadRequestException e) {
            // ApiMspdClient.post token yo'q yoki tarmoq xatosida BadRequestException tashlaydi —
            // o'zgartirmasdan ko'taramiz, GlobalExceptionHandler 400 javob qaytaradi.
            log.error("Tashqi API ga ulanib bo'lmadi: path={}, sabab={}", path, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Tashqi API chaqiruvi xatosi: path={}, sabab={}", path, e.getMessage(), e);
            throw new BadRequestException("Tashqi API chaqiruvi xatosi: " + e.getMessage(), e);
        }

        if (result.statusCode() != 200) {
            log.error("Tashqi API muvaffaqiyatsiz status: path={}, status={}, body={}",
                    path, result.statusCode(), result.body());
            throw new BadRequestException(
                    "Tashqi API xatolik bilan javob qaytardi: status=" + result.statusCode());
        }
        return result.body();
    }

    // =====================================================
    // JSON HELPERS
    // =====================================================

    private String textOrNull(JsonNode node, String field) {
        JsonNode v = node.path(field);
        return v.isNull() || v.isMissingNode() ? null : v.asText();
    }

    /**
     * Concatenate passport series + number into a single identifier
     * matching legacy {@code hemishe_e_employee.passport} and per-OTM databases.
     * Returns {@code null} when both inputs are null/blank.
     */
    private String concatPassport(String series, String number) {
        String s = series == null ? "" : series.trim();
        String n = number == null ? "" : number.trim();
        String combined = s + n;
        return combined.isEmpty() ? null : combined;
    }

    private Integer intOrNull(JsonNode node, String field) {
        JsonNode v = node.path(field);
        return v.isNull() || v.isMissingNode() ? null : v.asInt();
    }

    private Long longOrNull(JsonNode node, String field) {
        JsonNode v = node.path(field);
        if (v.isNull() || v.isMissingNode()) return null;
        if (v.isTextual()) {
            try { return Long.parseLong(v.asText()); } catch (NumberFormatException e) { return null; }
        }
        return v.asLong();
    }

    private BigDecimal decimalOrNull(JsonNode node, String field) {
        JsonNode v = node.path(field);
        if (v.isNull() || v.isMissingNode()) return null;
        if (v.isNumber()) return BigDecimal.valueOf(v.asDouble());
        if (v.isTextual()) {
            try { return new BigDecimal(v.asText()); } catch (NumberFormatException e) { return null; }
        }
        return null;
    }

    private LocalDate dateOrNull(JsonNode node, String field) {
        String text = textOrNull(node, field);
        if (text == null || text.isBlank()) return null;
        try {
            // Handle both "dd.MM.yyyy" and "dd.MM.yyyy HH:mm:ss" formats
            if (text.contains(" ")) text = text.split(" ")[0];
            return LocalDate.parse(text, DATE_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    private String joinName(JsonNode person) {
        String last = textOrNull(person, "lastName");
        String first = textOrNull(person, "firstName");
        String middle = textOrNull(person, "middleName");
        StringBuilder sb = new StringBuilder();
        if (last != null) sb.append(last);
        if (first != null) { if (!sb.isEmpty()) sb.append(" "); sb.append(first); }
        if (middle != null) { if (!sb.isEmpty()) sb.append(" "); sb.append(middle); }
        return sb.isEmpty() ? null : sb.toString();
    }

    /**
     * Find organization by TIN or create new one from API data.
     * Ensures no duplicate — TIN UNIQUE in organization table.
     */
    private Organization findOrCreateOrganization(String tin, JsonNode legalNode) {
        return organizationRepository.findByTin(tin).orElseGet(() -> {
            Organization org = new Organization();
            org.setTin(tin);
            org.setName(textOrNull(legalNode, "name"));
            log.info("Created organization from API: tin={}, name={}", tin, org.getName());
            return organizationRepository.save(org);
        });
    }

    /**
     * Find employee by PINFL or create new one from API data.
     * Ensures no duplicate — PINFL UNIQUE in employee table.
     */
    private Employee findOrCreateEmployee(String pinfl, JsonNode personNode, JsonNode contactNode, String universityCode) {
        Employee existing = employeeRepository.findByPinfl(Pinfl.of(pinfl)).orElse(null);

        String newLastName = textOrNull(personNode, "lastName");
        String newFirstName = textOrNull(personNode, "firstName");
        String newTin = textOrNull(personNode, "tin");

        // Mavjud bo'lsa va asosiy ma'lumotlar bir xil bo'lsa — yozmaymiz
        if (existing != null) {
            boolean same = java.util.Objects.equals(existing.getLastName(), newLastName)
                    && java.util.Objects.equals(existing.getFirstName(), newFirstName)
                    && java.util.Objects.equals(existing.getTin(), newTin);
            if (same) {
                log.debug("Employee unchanged: pinfl={}, skipping", Pinfl.maskOrEmpty(pinfl));
                return existing;
            }
        }

        Employee emp = existing != null ? existing : new Employee();
        if (existing == null) {
            emp.setPinfl(uz.hemis.common.vo.Pinfl.of(pinfl));
            // source tracking removed — audit via created_by
        }

        if (personNode != null && !personNode.isMissingNode()) {
            emp.setLastName(newLastName);
            emp.setFirstName(newFirstName);
            emp.setMiddleName(textOrNull(personNode, "middleName"));
            if (newTin != null && !newTin.isBlank()) {
                emp.setTin(uz.hemis.common.vo.Tin.of(newTin));
            }
            String series = textOrNull(personNode, "passportSeries");
            String number = textOrNull(personNode, "passportNumber");
            String passport = concatPassport(series, number);
            if (passport != null) emp.setPassport(passport);
            String address = textOrNull(personNode, "address");
            if (address != null) emp.setAddress(address);
        }

        if (contactNode != null && !contactNode.isMissingNode()) {
            String phone = textOrNull(contactNode, "phone");
            String email = textOrNull(contactNode, "email");
            if (phone != null && !phone.isBlank()) {
                try {
                    emp.setPhone(uz.hemis.common.vo.PhoneNumber.parse(phone));
                } catch (IllegalArgumentException ignored) { /* external data — normalize bo'lmasa, skip */ }
            }
            if (email != null) emp.setEmail(email);
        }

        Employee saved = employeeRepository.save(emp);
        log.info("Employee {}: pinfl={}, name={}",
                existing == null ? "created" : "updated",
                uz.hemis.common.vo.Pinfl.maskOrEmpty(pinfl),
                uz.hemis.common.util.PiiMask.name(saved.getLastName() + " " + saved.getFirstName()));
        return saved;
    }
}
