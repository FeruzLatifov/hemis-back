package uz.hemis.service.university;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.university.University;
import uz.hemis.domain.entity.university.UniversityCadastre;
import uz.hemis.domain.entity.university.UniversityFounder;
import uz.hemis.domain.entity.university.UniversityLegal;
import uz.hemis.domain.repository.UniversityCadastreRepository;
import uz.hemis.domain.entity.employee.Employee;
import uz.hemis.domain.entity.university.Organization;
import uz.hemis.domain.repository.UniversityFounderRepository;
import uz.hemis.domain.repository.UniversityLegalRepository;
import uz.hemis.domain.repository.UniversityRepository;
import uz.hemis.service.integration.ApiMspdTokenService;

import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * University External Data Service
 *
 * <p>Fetches legal entity and cadastre data from external API (172.18.9.171)
 * and saves to university_legal, university_founder, university_cadastre tables.</p>
 *
 * <p>API endpoints:</p>
 * <ul>
 *   <li>POST /legalentity/legalentity-info/ — company info, director, accountant, founders</li>
 *   <li>POST /kadastr/by-inn — list of cadastre numbers by TIN</li>
 *   <li>POST /kadastr/by-cadnum — detailed cadastre info by number</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UniversityExternalDataService {

    private final ApiMspdTokenService tokenService;
    private final UniversityRepository universityRepository;
    private final UniversityLegalRepository legalRepository;
    private final UniversityFounderRepository founderRepository;
    private final UniversityCadastreRepository cadastreRepository;
    private final uz.hemis.domain.repository.EmployeeRepository employeeRepository;
    private final uz.hemis.domain.repository.OrganizationRepository organizationRepository;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    // =====================================================
    // LEGAL ENTITY
    // =====================================================

    /**
     * Fetch legal entity data from API and save to university_legal + university_founder
     */
    @Transactional
    public UniversityLegal syncLegalEntity(String universityCode, String tin) {
        log.info("Syncing legal entity for university={}, tin={}", universityCode, tin);

        JsonNode response = callApi("/legalentity/legalentity-info/", "{\"tin\": \"" + tin + "\"}");
        if (response == null) return null;

        String newResponseJson = response.toString();

        // O'zgarish tekshiruvi — API javobi bir xil bo'lsa hech narsa qilmaymiz
        UniversityLegal existing = legalRepository.findByUniversityCode(universityCode).orElse(null);
        if (existing != null && newResponseJson.equals(existing.getApiRawResponse())) {
            log.info("Legal entity unchanged for university={}, skipping", universityCode);
            return existing;
        }

        // O'zgarish bor — yangilaymiz
        UniversityLegal legal = existing != null ? existing : new UniversityLegal();
        mapLegalEntity(legal, response, universityCode);
        legal.setApiRawResponse(newResponseJson);
        legal.setSyncedAt(LocalDateTime.now());
        legal = legalRepository.save(legal);
        log.info("Legal entity saved for university={}", universityCode);

        // Update hemishe_e_university with authoritative data from API
        updateUniversityFromApi(universityCode, response);

        // Founders — faqat o'zgargan bo'lsa
        syncFounders(universityCode, response.path("founders"));

        return legal;
    }

    /**
     * Update hemishe_e_university with data from external API.
     * ONLY bank_info is updated — address/soato are NOT touched because:
     *   - hemishe_e_university.address = university CAMPUS location (entered by university)
     *   - API billingAddress = LEGAL ENTITY registered address (can be different)
     *   - Legal address is stored in university_legal.billing_street
     */
    private void updateUniversityFromApi(String universityCode, JsonNode resp) {
        universityRepository.findById(universityCode).ifPresent(uni -> {
            boolean changed = false;

            // Bank accounts — API field names: mfo, paymentAccount, status
            JsonNode banks = resp.path("companyBanks");
            if (banks.isArray() && !banks.isEmpty()) {
                StringBuilder bankInfo = new StringBuilder();
                for (JsonNode bank : banks) {
                    if (!bankInfo.isEmpty()) bankInfo.append("\n");
                    String mfo = textOrNull(bank, "mfo");
                    String account = textOrNull(bank, "paymentAccount");
                    if (mfo != null) bankInfo.append("MFO: ").append(mfo);
                    if (account != null) bankInfo.append(" h/r: ").append(account);
                }
                if (!bankInfo.isEmpty()) {
                    uni.setBankInfo(bankInfo.toString());
                    changed = true;
                }
            }

            if (changed) {
                universityRepository.save(uni);
                log.info("hemishe_e_university updated from API for code={}", universityCode);
            }
        });
    }

    private void mapLegalEntity(UniversityLegal legal, JsonNode resp, String universityCode) {
        legal.setUniversityCode(universityCode);

        JsonNode company = resp.path("company");
        if (!company.isMissingNode()) {
            legal.setShortName(textOrNull(company, "shortName"));
            legal.setOpf(intOrNull(company, "opf"));
            legal.setKfs(intOrNull(company, "kfs"));
            legal.setTin(textOrNull(company, "tin"));
            legal.setOked(textOrNull(company, "oked"));
            legal.setSoogu(textOrNull(company, "soogu"));
            legal.setSooguRegistrator(textOrNull(company, "sooguRegistrator"));
            legal.setRegistrationDate(dateOrNull(company, "registrationDate"));
            legal.setRegistrationNumber(textOrNull(company, "registrationNumber"));
            legal.setReregistrationDate(dateOrNull(company, "reregistrationDate"));
            legal.setStatus(intOrNull(company, "status"));
            legal.setStatusUpdated(dateOrNull(company, "statusUpdated"));
            legal.setTaxMode(intOrNull(company, "taxMode"));
            legal.setVatNumber(longOrNull(company, "vatNumber"));
            legal.setTaxpayerType(intOrNull(company, "taxpayerType"));
            legal.setBusinessType(intOrNull(company, "businessType"));
            legal.setBusinessFund(longOrNull(company, "businessFund"));
            legal.setBusinessStructure(intOrNull(company, "businessStructure"));
        }

        JsonNode extraInfo = resp.path("companyExtraInfo");
        if (!extraInfo.isMissingNode()) {
            legal.setAvgEmployees(intOrNull(extraInfo, "avgNumberEmployees"));
        }

        JsonNode billing = resp.path("companyBillingAddress");
        if (!billing.isMissingNode()) {
            legal.setBillingCountryCode(intOrNull(billing, "countryCode"));
            legal.setBillingSoato(textOrNull(billing, "soato"));
            legal.setBillingStreet(textOrNull(billing, "streetName"));
            legal.setBillingPostcode(textOrNull(billing, "postcode"));
            legal.setBillingCadastre(textOrNull(billing, "cadastreNumber"));
        }

        JsonNode shipping = resp.path("companyShippingAddresses");
        if (shipping.isArray() && !shipping.isEmpty()) {
            legal.setShippingAddresses(shipping.toString());
        }

        // Director — find or create employee, link by PINFL
        JsonNode director = resp.path("director");
        JsonNode dirContact = resp.path("directorContact");
        if (!director.isMissingNode()) {
            String dirPinfl = textOrNull(director, "pinfl");
            if (dirPinfl != null && !dirPinfl.isBlank()) {
                Employee dirEmployee = findOrCreateEmployee(dirPinfl, director, dirContact, universityCode);
                legal.setDirectorEmployee(dirEmployee);
            }
        }

        // Accountant — find or create employee, link by PINFL
        JsonNode accountant = resp.path("accountant");
        JsonNode accContact = resp.path("accountantContact");
        if (!accountant.isMissingNode()) {
            String accPinfl = textOrNull(accountant, "pinfl");
            if (accPinfl != null && !accPinfl.isBlank()) {
                Employee accEmployee = findOrCreateEmployee(accPinfl, accountant, accContact, universityCode);
                legal.setAccountantEmployee(accEmployee);
            }
        }

        // Bank accounts
        JsonNode banks = resp.path("companyBanks");
        if (banks.isArray() && !banks.isEmpty()) {
            legal.setBankAccounts(banks.toString());
        }
    }

    private void syncFounders(String universityCode, JsonNode foundersNode) {
        if (!foundersNode.isArray()) return;

        // Delete ALL existing founders for this university (idempotent sync)
        List<UniversityFounder> existing = founderRepository.findByUniversityCode(universityCode);
        if (!existing.isEmpty()) {
            founderRepository.deleteAll(existing);
            log.info("Deleted {} old founders for university={}", existing.size(), universityCode);
        }

        // Create fresh from API
        List<UniversityFounder> newFounders = new ArrayList<>();
        for (JsonNode founderNode : foundersNode) {
            UniversityFounder founder = new UniversityFounder();
            founder.setUniversityCode(universityCode);
            founder.setIsCurrent(true);

            JsonNode individual = founderNode.path("founderIndividual");
            JsonNode legal = founderNode.path("founderLegal");

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
                continue;
            }

            newFounders.add(founder);
        }

        founderRepository.saveAll(newFounders);
        log.info("Saved {} founders for university={}", newFounders.size(), universityCode);
    }

    // =====================================================
    // CADASTRE
    // =====================================================

    /**
     * Fetch cadastre data from API and save to university_cadastre
     */
    @Transactional
    public List<UniversityCadastre> syncCadastre(String universityCode, String tin) {
        log.info("Syncing cadastre for university={}, tin={}", universityCode, tin);

        // Step 1: Get cadastre numbers by TIN
        JsonNode innResponse = callApi("/kadastr/by-inn", "{\"tin\": \"" + tin + "\"}");
        if (innResponse == null) return List.of();

        int code = innResponse.path("code").asInt(0);
        if (code != 1) {
            log.info("No cadastre data for tin={}: {}", tin, innResponse.path("message").asText());
            return List.of();
        }

        JsonNode cadastrList = innResponse.path("cadastr_list");
        if (!cadastrList.isArray() || cadastrList.isEmpty()) {
            log.info("Empty cadastre list for tin={}", tin);
            return List.of();
        }

        // Step 2: Fetch details for each cadastre number
        List<UniversityCadastre> result = new ArrayList<>();
        for (JsonNode cadNode : cadastrList) {
            String cadNumber = cadNode.asText();
            try {
                UniversityCadastre cadastre = syncSingleCadastre(universityCode, cadNumber);
                if (cadastre != null) result.add(cadastre);
            } catch (Exception e) {
                log.error("Failed to sync cadastre {}: {}", cadNumber, e.getMessage());
            }
        }

        log.info("Synced {} cadastre objects for university={}", result.size(), universityCode);
        return result;
    }

    private UniversityCadastre syncSingleCadastre(String universityCode, String cadNumber) {
        JsonNode resp = callApi("/kadastr/by-cadnum", "{\"cad_num\": \"" + cadNumber + "\"}");
        if (resp == null || resp.path("code").asInt(0) != 1) return null;

        UniversityCadastre c = cadastreRepository.findByCadNumber(cadNumber).orElse(new UniversityCadastre());
        c.setUniversityCode(universityCode);
        c.setCadNumber(cadNumber);
        c.setCadNumberOld(textOrNull(resp, "cad_number_old"));

        // Location
        c.setRegionId(intOrNull(resp, "region_id"));
        c.setRegion(textOrNull(resp, "region"));
        c.setDistrictId(intOrNull(resp, "district_id"));
        c.setDistrict(textOrNull(resp, "district"));
        c.setAddress(textOrNull(resp, "address"));
        c.setShortAddress(textOrNull(resp, "short_address"));
        c.setStreet(textOrNull(resp, "street"));
        c.setStreetCode(textOrNull(resp, "street_code"));
        c.setDomNum(textOrNull(resp, "dom_num"));
        c.setNeighborhood(textOrNull(resp, "neighborhood"));
        c.setNeighborhoodId(textOrNull(resp, "neighborhood_id"));

        // Classification — kadastr API raw field names (tip/vid) mapped to type/kind
        c.setTypeCode(textOrNull(resp, "tip"));
        c.setTypeName(textOrNull(resp, "tipText"));
        c.setKindCode(textOrNull(resp, "vid"));
        c.setKindName(textOrNull(resp, "vidText"));

        // Land area
        c.setLandArea(decimalOrNull(resp, "land_area"));
        c.setLandAreaI(decimalOrNull(resp, "land_area_i"));
        c.setLandAreaB(decimalOrNull(resp, "land_area_b"));
        c.setLandAreaF(decimalOrNull(resp, "land_area_f"));
        c.setLandAreaZ(decimalOrNull(resp, "land_area_z"));
        c.setLandAreaD(decimalOrNull(resp, "land_area_d"));
        c.setLandAreaU(decimalOrNull(resp, "land_area_u"));

        // Object area
        c.setObjectArea(decimalOrNull(resp, "object_area"));
        c.setObjectAreaL(decimalOrNull(resp, "object_area_l"));
        c.setObjectAreaU(decimalOrNull(resp, "object_area_u"));

        // Value
        c.setCost(longOrNull(resp, "cost"));

        // Legal
        c.setEcoZone(textOrNull(resp, "eco_zone"));
        c.setBanIs("1".equals(textOrNull(resp, "ban_is")));
        c.setLandFundType(textOrNull(resp, "land_fund_type"));
        c.setLandUseType(textOrNull(resp, "land_use_type"));
        c.setLandFundCategory(textOrNull(resp, "land_fund_category"));

        // JSONB
        JsonNode subjects = resp.path("subjects");
        if (subjects.isArray()) c.setSubjects(subjects.toString());
        JsonNode docs = resp.path("documents");
        if (docs.isArray()) c.setDocuments(docs.toString());
        JsonNode docsL = resp.path("documents_l");
        if (docsL.isArray()) c.setDocumentsL(docsL.toString());
        JsonNode bans = resp.path("bans");
        if (bans.isArray()) c.setBans(bans.toString());

        // Meta
        c.setDataSource(textOrNull(resp, "data_source"));
        c.setApiRawResponse(resp.toString());
        c.setSyncedAt(LocalDateTime.now());

        return cadastreRepository.save(c);
    }

    // =====================================================
    // SYNC ALL for a university
    // =====================================================

    /**
     * Sync all external data (legal + cadastre) for a university.
     * Resolves TIN from university record automatically.
     */
    @Transactional
    public void syncAll(String universityCode) {
        String tin = resolveTin(universityCode);
        syncAll(universityCode, tin);
    }

    /**
     * Sync all external data (legal + cadastre) for a university
     */
    @Transactional
    public void syncAll(String universityCode, String tin) {
        log.info("Full external data sync for university={}, tin={}", universityCode, tin);
        syncLegalEntity(universityCode, tin);
        syncCadastre(universityCode, tin);
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
                        throw new uz.hemis.common.exception.BadRequestException(
                                "University TIN is empty for code: " + universityCode);
                    }
                    return tin;
                })
                .orElseThrow(() -> new uz.hemis.common.exception.BadRequestException(
                        "University not found: " + universityCode));
    }

    // =====================================================
    // HTTP HELPER
    // =====================================================

    private JsonNode callApi(String path, String jsonBody) {
        String token = tokenService.getAccessToken();
        if (token == null) {
            log.error("No API-MSPD token available — cannot call {}", path);
            return null;
        }

        String url = tokenService.getBaseUrl() + path;
        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);

            try (var os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            if (status == 200) {
                String body = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                conn.disconnect();
                return objectMapper.readTree(body);
            } else {
                String err = "";
                try { err = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8); } catch (Exception ignored) {}
                conn.disconnect();
                log.error("API call {} failed: status={}, body={}", path, status, err);
                return null;
            }
        } catch (Exception e) {
            log.error("API call {} error: {}", path, e.getMessage());
            return null;
        }
    }

    // =====================================================
    // JSON HELPERS
    // =====================================================

    private String textOrNull(JsonNode node, String field) {
        JsonNode v = node.path(field);
        return v.isNull() || v.isMissingNode() ? null : v.asText();
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
            org.setSource("api_legal");
            log.info("Created organization from API: tin={}, name={}", tin, org.getName());
            return organizationRepository.save(org);
        });
    }

    /**
     * Find employee by PINFL or create new one from API data.
     * Ensures no duplicate — PINFL UNIQUE in employee table.
     */
    private Employee findOrCreateEmployee(String pinfl, JsonNode personNode, JsonNode contactNode, String universityCode) {
        Employee existing = employeeRepository.findByPinfl(pinfl).orElse(null);

        String newLastName = textOrNull(personNode, "lastName");
        String newFirstName = textOrNull(personNode, "firstName");
        String newTin = textOrNull(personNode, "tin");

        // Mavjud bo'lsa va asosiy ma'lumotlar bir xil bo'lsa — yozmaymiz
        if (existing != null) {
            boolean same = java.util.Objects.equals(existing.getLastName(), newLastName)
                    && java.util.Objects.equals(existing.getFirstName(), newFirstName)
                    && java.util.Objects.equals(existing.getTin(), newTin);
            if (same) {
                log.debug("Employee unchanged: pinfl={}, skipping", pinfl);
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
            if (series != null) emp.setPassportSeries(series);
            if (number != null) emp.setPassportNumber(number);
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
        log.info("Employee {}: pinfl={}, name={} {}",
                existing == null ? "created" : "updated", pinfl, saved.getLastName(), saved.getFirstName());
        return saved;
    }
}
