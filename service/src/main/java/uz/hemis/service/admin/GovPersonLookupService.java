package uz.hemis.service.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uz.hemis.common.exception.BadRequestException;
import uz.hemis.common.vo.Pinfl;
import uz.hemis.service.admin.dto.GovPersonDto;
import uz.hemis.service.integration.ApiMspdTokenService;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Person-registry lookup for the "Shaxs" (person) user-create autofill flow.
 *
 * <p>Reuses the ALREADY-WIRED api_mspd/GUVD gateway (172.18.9.171) via
 * {@link ApiMspdTokenService} — the SAME transport used by
 * {@code PassportServiceController} (/app/rest/v2/services/passport-data/*) and
 * {@code UniversityOfficialService}. NO new integration is introduced.</p>
 *
 * <p>Endpoints (Bearer token):</p>
 * <ul>
 *   <li>{@code POST /person/pinpp-and-document/} — PINFL + passport document</li>
 *   <li>{@code POST /person/pinpp-and-birth-date/} — PINFL + birth date (fallback)</li>
 *   <li>{@code POST /person/person-address/} — registered address</li>
 * </ul>
 *
 * <p><strong>Read-only:</strong> unlike {@code UniversityOfficialService.lookupByPinfl}, this
 * does NOT create/persist any entity — it only fills the create form. Persistence happens on
 * user save.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GovPersonLookupService {

    private final ApiMspdTokenService tokenService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Resolve a person by PINFL + (passport document OR birth date) from the gateway.
     *
     * @param pinfl    14-digit PINFL (required)
     * @param document passport series+number, e.g. AA0000000 (preferred)
     * @param birthDate ISO yyyy-MM-dd (used when document is absent)
     * @return the resolved person, or {@code null} if not found / gateway unavailable
     */
    public GovPersonDto lookup(String pinfl, String document, String birthDate) {
        if (pinfl == null || !pinfl.matches("^\\d{14}$")) {
            throw new BadRequestException("PINFL must be 14 digits");
        }
        boolean hasDoc = document != null && !document.isBlank();
        boolean hasBirth = birthDate != null && !birthDate.isBlank();
        if (!hasDoc && !hasBirth) {
            throw new BadRequestException("Passport document or birth date is required");
        }

        String token = tokenService.getAccessToken();
        if (token == null) {
            log.warn("Person lookup skipped — api_mspd token unavailable (pinfl={})", Pinfl.maskOrEmpty(pinfl));
            return null;
        }
        String baseUrl = tokenService.getBaseUrl();

        Map<String, String> body = new LinkedHashMap<>();
        body.put("pinfl", pinfl);
        String path;
        if (hasDoc) {
            body.put("document", document.trim());
            path = "/person/pinpp-and-document/";
        } else {
            body.put("birth_date", birthDate.trim());
            path = "/person/pinpp-and-birth-date/";
        }

        try {
            Map<String, Object> resp = postJson(baseUrl + path, body, token);
            if (resp == null || !"1".equals(str(resp.get("result")))) {
                log.info("Person not found in gateway: pinfl={}, msg={}",
                        Pinfl.maskOrEmpty(pinfl), resp != null ? resp.get("message") : null);
                return null;
            }
            Object dataObj = resp.get("data");
            if (!(dataObj instanceof Map<?, ?> person)) {
                return null;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> p = (Map<String, Object>) person;

            // api-mspd gateway key names: Latin = surnamelat/namelat/patronymlat, Cyrillic = *cyr.
            // Latin first, Cyrillic fallback (absent keys yield null → harmless). The earlier code
            // read sur_name_latin/birth_place/document/issued_date, which THIS gateway never
            // returns, so F.I.Sh and most passport fields came back empty.
            String lastName = firstNonBlank(str(p.get("surnamelat")), str(p.get("surnamecyr")));
            String firstName = firstNonBlank(str(p.get("namelat")), str(p.get("namecyr")));
            String middleName = firstNonBlank(str(p.get("patronymlat")), str(p.get("patronymcyr")));

            // Passport: current_document is the active series+number; documents[] carries the
            // give-place and begin/end dates for that document.
            String currentDoc = str(p.get("current_document"));
            Map<String, Object> doc = activeDocument(p.get("documents"), currentDoc);
            String docNumber = (doc != null) ? str(doc.get("document")) : null;
            String passport = firstNonBlank(currentDoc,
                    firstNonBlank(docNumber, hasDoc ? document.trim() : null));

            return GovPersonDto.builder()
                    .pinfl(firstNonBlank(str(p.get("current_pinpp")), pinfl))
                    .lastName(lastName)
                    .firstName(firstName)
                    .middleName(middleName)
                    .fullName(composeFullName(lastName, firstName, middleName))
                    .birthDate(str(p.get("birth_date")))
                    .birthPlace(str(p.get("birthplace")))
                    .gender(str(p.get("sex")))
                    .nationality(str(p.get("nationality")))
                    .passport(passport)
                    .passportGivePlace(doc != null ? str(doc.get("docgiveplace")) : null)
                    .passportIssuedDate(doc != null ? str(doc.get("datebegin")) : null)
                    .passportExpiryDate(doc != null ? str(doc.get("dateend")) : null)
                    .photo(str(p.get("photo")))
                    .address(fetchAddress(baseUrl, pinfl, token))
                    .build();

        } catch (Exception e) {
            log.warn("Person lookup gateway error: pinfl={}, err={}", Pinfl.maskOrEmpty(pinfl), e.getMessage());
            return null;
        }
    }

    /** Registered address from {@code /person/person-address/}. Null on any failure. */
    private String fetchAddress(String baseUrl, String pinfl, String token) {
        try {
            Map<String, String> body = new LinkedHashMap<>();
            body.put("pinfl", pinfl);
            Map<String, Object> resp = postJson(baseUrl + "/person/person-address/", body, token);
            if (resp == null) return null;

            // Shape 1 (passport-data): data.permanent_registration.{region,district,address}
            Object data = resp.get("data");
            if (data instanceof Map<?, ?> dm) {
                Object reg = ((Map<?, ?>) dm).get("permanent_registration");
                if (reg instanceof Map<?, ?> rm) {
                    return joinAddress(str(rm.get("region")), str(rm.get("district")), str(rm.get("address")));
                }
            }
            // Shape 2 (person-data): flat regionName/districtName/address
            return joinAddress(str(resp.get("regionName")), str(resp.get("districtName")), str(resp.get("address")));
        } catch (Exception e) {
            log.debug("Address lookup failed: {}", e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> postJson(String url, Map<String, String> body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        ResponseEntity<Map> resp = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), Map.class);
        return resp.getBody();
    }

    private static String str(Object v) {
        if (v == null) return null;
        String s = v.toString().trim();
        return s.isEmpty() ? null : s;
    }

    private static String firstNonBlank(String a, String b) {
        return (a != null && !a.isBlank()) ? a : b;
    }

    /** Active document from the gateway's {@code documents[]}: the entry whose {@code document}
     *  equals {@code current_document}, else the first. Null when the array is absent/empty. */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> activeDocument(Object documentsObj, String currentDoc) {
        if (!(documentsObj instanceof java.util.List<?> docs) || docs.isEmpty()) return null;
        Map<String, Object> first = null;
        for (Object o : docs) {
            if (o instanceof Map<?, ?> dm) {
                Map<String, Object> m = (Map<String, Object>) dm;
                if (first == null) first = m;
                if (currentDoc != null && currentDoc.equals(str(m.get("document")))) return m;
            }
        }
        return first;
    }

    private static String composeFullName(String last, String first, String middle) {
        StringBuilder sb = new StringBuilder();
        for (String part : new String[]{last, first, middle}) {
            if (part != null && !part.isBlank()) {
                if (!sb.isEmpty()) sb.append(' ');
                sb.append(part.trim());
            }
        }
        return sb.isEmpty() ? null : sb.toString();
    }

    private static String joinAddress(String region, String district, String address) {
        StringBuilder sb = new StringBuilder();
        for (String part : new String[]{region, district, address}) {
            if (part != null && !part.isBlank()) {
                if (!sb.isEmpty()) sb.append(", ");
                sb.append(part.trim());
            }
        }
        return sb.isEmpty() ? null : sb.toString();
    }
}
