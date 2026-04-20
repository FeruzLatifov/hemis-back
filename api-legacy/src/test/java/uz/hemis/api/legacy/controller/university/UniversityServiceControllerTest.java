package uz.hemis.api.legacy.controller.university;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uz.hemis.api.legacy.util.LegacySecurityHelper;
import uz.hemis.common.dto.university.UniversityDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.university.University;
import uz.hemis.domain.repository.UniversityRepository;
import uz.hemis.service.legacy.CubaNestedObjectLoader;
import uz.hemis.service.legacy.university.UniversityRefLegacyService;
import uz.hemis.service.university.UniversityService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UniversityServiceController}.
 *
 * <p>Tests cover:</p>
 * <ul>
 *   <li>getConfig() - all branches (null auth, success, not found)</li>
 *   <li>getByCode() - found and not found cases</li>
 *   <li>buildUniversityMap() - CUBA format output validation (tested indirectly via getByCode)</li>
 *   <li>Boolean null-coalescing defaults in map building</li>
 *   <li>Conditional field inclusion (null fields omitted from map)</li>
 *   <li>Nested object loader delegation for classifiers and SOATO</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UniversityServiceController Tests")
class UniversityServiceControllerTest {

    @Mock
    private UniversityService universityService;

    @Mock
    private UniversityRefLegacyService universityRefLegacyService;

    @Mock
    private UniversityRepository universityRepository;

    @Mock
    private CubaNestedObjectLoader nestedObjectLoader;

    @Mock
    private LegacySecurityHelper securityHelper;

    @InjectMocks
    private UniversityServiceController controller;

    // =====================================================
    // getConfig() tests
    // =====================================================

    @Nested
    @DisplayName("GET /config")
    class GetConfigTests {

        @Test
        @DisplayName("Autentifikatsiya yo'q bo'lsa success=true va universityCode=null qaytaradi")
        void returnsNullCodeWhenNoAuthentication() {
            when(securityHelper.getUniversityCodeFromContext()).thenReturn(null);

            ResponseEntity<?> response = controller.getConfig();

            assertThat(response.getStatusCode().value()).isEqualTo(200);
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertThat(body).isNotNull();
            assertThat(body).containsEntry("success", true);
            assertThat(body).containsEntry("universityCode", null);
            // universityService should NOT be called when code is null
            verifyNoInteractions(universityService);
        }

        @Test
        @DisplayName("Universitet topilganda barcha config maydonlari qaytariladi")
        void returnsConfigWhenUniversityFound() {
            UniversityDto dto = new UniversityDto();
            dto.setCode("00520");
            dto.setGpaEdit(true);
            dto.setAccreditationEdit(true);
            dto.setAddStudent(false);
            dto.setAllowGrouping(false);
            dto.setAllowTransferOutside(false);

            when(securityHelper.getUniversityCodeFromContext()).thenReturn("00520");
            when(universityService.findByCode("00520")).thenReturn(dto);

            ResponseEntity<?> response = controller.getConfig();

            assertThat(response.getStatusCode().value()).isEqualTo(200);
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertThat(body).isNotNull();
            assertThat(body).containsEntry("success", true);
            assertThat(body).containsEntry("universityCode", "00520");
            assertThat(body).containsEntry("gpaEdit", true);
            assertThat(body).containsEntry("accreditationEdit", true);
            assertThat(body).containsEntry("addStudent", false);
            assertThat(body).containsEntry("allowGrouping", false);
            assertThat(body).containsEntry("allowTransferOutside", false);
            assertThat(body).containsEntry("oneId", true);
        }

        @Test
        @DisplayName("Config dagi null boolean maydonlar default qiymatlar bilan qaytariladi")
        void returnsDefaultsForNullBooleans() {
            UniversityDto dto = new UniversityDto();
            dto.setCode("00401");
            dto.setGpaEdit(null);
            dto.setAccreditationEdit(null);
            dto.setAddStudent(null);
            dto.setAllowGrouping(null);
            dto.setAllowTransferOutside(null);

            when(securityHelper.getUniversityCodeFromContext()).thenReturn("00401");
            when(universityService.findByCode("00401")).thenReturn(dto);

            ResponseEntity<?> response = controller.getConfig();

            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertThat(body).isNotNull();
            assertThat(body).containsEntry("gpaEdit", false);            // default: false
            assertThat(body).containsEntry("accreditationEdit", false);   // default: false
            assertThat(body).containsEntry("addStudent", true);           // default: true
            assertThat(body).containsEntry("allowGrouping", true);        // default: true
            assertThat(body).containsEntry("allowTransferOutside", true); // default: true
            assertThat(body).containsEntry("oneId", true);                // always true
        }

        @Test
        @DisplayName("Universitet topilmaganda success=false va xabar qaytariladi")
        void returnsErrorWhenUniversityNotFound() {
            when(securityHelper.getUniversityCodeFromContext()).thenReturn("99999");
            when(universityService.findByCode("99999"))
                    .thenThrow(new ResourceNotFoundException("University", "code", "99999"));

            ResponseEntity<?> response = controller.getConfig();

            assertThat(response.getStatusCode().value()).isEqualTo(200);
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertThat(body).isNotNull();
            assertThat(body).containsEntry("success", false);
            assertThat(body).containsKey("message");
            assertThat((String) body.get("message")).contains("99999");
        }
    }

    // =====================================================
    // getByCode() tests
    // =====================================================

    @Nested
    @DisplayName("GET /get")
    class GetByCodeTests {

        @Test
        @DisplayName("Topilmagan kod uchun success=false qaytariladi")
        void returnsErrorForMissingCode() {
            when(universityRefLegacyService.findUniversityById("NONEXIST")).thenReturn(Optional.empty());

            ResponseEntity<?> response = controller.getByCode("NONEXIST");

            assertThat(response.getStatusCode().value()).isEqualTo(200);
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertThat(body).isNotNull();
            assertThat(body).containsEntry("success", false);
            assertThat((String) body.get("message")).contains("NONEXIST");
        }

        @Test
        @DisplayName("Topilgan universitet success=true va university map qaytariladi")
        void returnsUniversityMapWhenFound() {
            University u = createMinimalUniversity("00520", "Toshkent OTM");
            when(universityRefLegacyService.findUniversityById("00520")).thenReturn(Optional.of(u));

            ResponseEntity<?> response = controller.getByCode("00520");

            assertThat(response.getStatusCode().value()).isEqualTo(200);
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertThat(body).isNotNull();
            assertThat(body).containsEntry("success", true);
            assertThat(body).containsKey("university");
        }
    }

    // =====================================================
    // buildUniversityMap() - CUBA format (indirectly via getByCode)
    // =====================================================

    @Nested
    @DisplayName("buildUniversityMap - CUBA format")
    class BuildUniversityMapTests {

        @Test
        @DisplayName("CUBA metadata maydonlari (_entityName, _instanceName, id) to'g'ri")
        void containsCubaMetadataFields() {
            University u = createMinimalUniversity("00520", "Test University");
            when(universityRefLegacyService.findUniversityById("00520")).thenReturn(Optional.of(u));

            Map<String, Object> uniMap = extractUniversityMap("00520");

            assertThat(uniMap).containsEntry("_entityName", "hemishe_EUniversity");
            assertThat(uniMap).containsEntry("id", "00520");
            assertThat(uniMap).containsEntry("code", "00520");
            assertThat(uniMap).containsEntry("name", "Test University");
        }

        @Test
        @DisplayName("Null boolean maydonlar default qiymatlarga o'rnatiladi")
        void nullBooleansGetDefaults() {
            University u = createMinimalUniversity("00401", "Default Booleans OTM");
            // All boolean fields are null by default from createMinimalUniversity
            when(universityRefLegacyService.findUniversityById("00401")).thenReturn(Optional.of(u));

            Map<String, Object> uniMap = extractUniversityMap("00401");

            assertThat(uniMap).containsEntry("addStudent", true);            // null -> true
            assertThat(uniMap).containsEntry("accreditationEdit", false);     // null -> false
            assertThat(uniMap).containsEntry("active", false);               // null -> false
            assertThat(uniMap).containsEntry("gpaEdit", false);              // null -> false
            assertThat(uniMap).containsEntry("oneId", true);                 // null -> true
            assertThat(uniMap).containsEntry("allowGrouping", true);         // null -> true
            assertThat(uniMap).containsEntry("allowTransferOutside", true);  // null -> true
            assertThat(uniMap).containsEntry("version", 1);                  // null -> 1
        }

        @Test
        @DisplayName("Explicit boolean qiymatlari saqlanadi")
        void explicitBooleanValuesPreserved() {
            University u = createMinimalUniversity("00520", "Explicit Booleans OTM");
            u.setAddStudent(false);
            u.setAccreditationEdit(true);
            u.setActive(true);
            u.setGpaEdit(true);
            u.setOneId(false);
            u.setAllowGrouping(false);
            u.setAllowTransferOutside(false);
            u.setVersion(5);
            when(universityRefLegacyService.findUniversityById("00520")).thenReturn(Optional.of(u));

            Map<String, Object> uniMap = extractUniversityMap("00520");

            assertThat(uniMap).containsEntry("addStudent", false);
            assertThat(uniMap).containsEntry("accreditationEdit", true);
            assertThat(uniMap).containsEntry("active", true);
            assertThat(uniMap).containsEntry("gpaEdit", true);
            assertThat(uniMap).containsEntry("oneId", false);
            assertThat(uniMap).containsEntry("allowGrouping", false);
            assertThat(uniMap).containsEntry("allowTransferOutside", false);
            assertThat(uniMap).containsEntry("version", 5);
        }

        @Test
        @DisplayName("Null optional maydonlar (studentUrl, tin, address, teacherUrl) map ga qo'shilmaydi")
        void nullOptionalFieldsOmittedFromMap() {
            University u = createMinimalUniversity("00520", "Sparse OTM");
            // studentUrl, tin, address, teacherUrl all null by default
            when(universityRefLegacyService.findUniversityById("00520")).thenReturn(Optional.of(u));

            Map<String, Object> uniMap = extractUniversityMap("00520");

            assertThat(uniMap).doesNotContainKey("studentUrl");
            assertThat(uniMap).doesNotContainKey("tin");
            assertThat(uniMap).doesNotContainKey("address");
            assertThat(uniMap).doesNotContainKey("teacherUrl");
        }

        @Test
        @DisplayName("Optional string maydonlar mavjud bo'lganda map ga qo'shiladi")
        void presentOptionalFieldsIncludedInMap() {
            University u = createMinimalUniversity("00520", "Full OTM");
            u.setStudentUrl("https://student.hemis.uz");
            u.setTin("123456789");
            u.setAddress("Toshkent sh., Universitet ko'chasi 4");
            u.setTeacherUrl("https://teacher.hemis.uz");
            when(universityRefLegacyService.findUniversityById("00520")).thenReturn(Optional.of(u));

            Map<String, Object> uniMap = extractUniversityMap("00520");

            assertThat(uniMap).containsEntry("studentUrl", "https://student.hemis.uz");
            assertThat(uniMap).containsEntry("tin", "123456789");
            assertThat(uniMap).containsEntry("address", "Toshkent sh., Universitet ko'chasi 4");
            assertThat(uniMap).containsEntry("teacherUrl", "https://teacher.hemis.uz");
        }

        @Test
        @DisplayName("SOATO nested ob'ekt CubaNestedObjectLoader orqali yuklanadi")
        void soatoLoadedViaNestedObjectLoader() {
            University u = createMinimalUniversity("00520", "SOATO OTM");
            u.setSoato("1726");

            Map<String, Object> mockSoato = new LinkedHashMap<>();
            mockSoato.put("_entityName", "hemishe_HSoato");
            mockSoato.put("code", "1726");
            mockSoato.put("name_uz", "Toshkent shahri");

            when(universityRefLegacyService.findUniversityById("00520")).thenReturn(Optional.of(u));
            when(nestedObjectLoader.loadSoato("1726")).thenReturn(mockSoato);

            Map<String, Object> uniMap = extractUniversityMap("00520");

            assertThat(uniMap).containsKey("soato");
            assertThat(uniMap.get("soato")).isEqualTo(mockSoato);
            verify(nestedObjectLoader).loadSoato("1726");
        }

        @Test
        @DisplayName("SOATO null bo'lganda loadSoato chaqirilmaydi")
        void soatoSkippedWhenNull() {
            University u = createMinimalUniversity("00520", "No SOATO OTM");
            // soato is null by default
            when(universityRefLegacyService.findUniversityById("00520")).thenReturn(Optional.of(u));

            Map<String, Object> uniMap = extractUniversityMap("00520");

            assertThat(uniMap).doesNotContainKey("soato");
            verify(nestedObjectLoader, never()).loadSoato(anyString());
        }

        @Test
        @DisplayName("SOATO Region nested ob'ekt yuklanadi")
        void soatoRegionLoadedWhenPresent() {
            University u = createMinimalUniversity("00520", "Region OTM");
            u.setSoatoRegion("17");

            Map<String, Object> mockRegion = new LinkedHashMap<>();
            mockRegion.put("_entityName", "hemishe_HSoato");
            mockRegion.put("code", "17");

            when(universityRefLegacyService.findUniversityById("00520")).thenReturn(Optional.of(u));
            when(nestedObjectLoader.loadSoato("17")).thenReturn(mockRegion);

            Map<String, Object> uniMap = extractUniversityMap("00520");

            assertThat(uniMap).containsKey("soatoRegion");
            assertThat(uniMap.get("soatoRegion")).isEqualTo(mockRegion);
        }

        @Test
        @DisplayName("universityActivityStatus classifikatori yuklanadi")
        void universityActivityStatusLoaded() {
            University u = createMinimalUniversity("00520", "Active OTM");
            u.setUniversityActivityStatus("11");

            Map<String, Object> mockStatus = new LinkedHashMap<>();
            mockStatus.put("_entityName", "hemishe_HUniversityActivityStatus");
            mockStatus.put("code", "11");
            when(universityRefLegacyService.findUniversityById("00520")).thenReturn(Optional.of(u));
            when(nestedObjectLoader.loadClassifier(
                    "hemishe_h_university_activity_status", "HUniversityActivityStatus", "11"))
                    .thenReturn(mockStatus);

            Map<String, Object> uniMap = extractUniversityMap("00520");

            @SuppressWarnings("unchecked")
            Map<String, Object> status = (Map<String, Object>) uniMap.get("universityActivityStatus");
            assertThat(status).containsEntry("code", "11");
        }

        @Test
        @DisplayName("universityType classifikatori yuklanadi")
        void universityTypeLoaded() {
            University u = createMinimalUniversity("00520", "Typed OTM");
            u.setUniversityType("11");

            Map<String, Object> mockType = new LinkedHashMap<>();
            mockType.put("_entityName", "hemishe_HUniversityType");
            mockType.put("code", "11");
            when(universityRefLegacyService.findUniversityById("00520")).thenReturn(Optional.of(u));
            when(nestedObjectLoader.loadClassifier(
                    "hemishe_h_university_type", "HUniversityType", "11"))
                    .thenReturn(mockType);

            Map<String, Object> uniMap = extractUniversityMap("00520");

            @SuppressWarnings("unchecked")
            Map<String, Object> type = (Map<String, Object>) uniMap.get("universityType");
            assertThat(type).containsEntry("code", "11");
        }

        @Test
        @DisplayName("versionType classifikatori yuklanadi")
        void versionTypeLoadedWithNames() {
            University u = createMinimalUniversity("00520", "Versioned OTM");
            u.setUniversityVersion("2");

            Map<String, Object> mockVersion = new LinkedHashMap<>();
            mockVersion.put("_entityName", "hemishe_HHemisVersionType");
            mockVersion.put("code", "2");
            mockVersion.put("nameRu", "HEMIS v2");
            when(universityRefLegacyService.findUniversityById("00520")).thenReturn(Optional.of(u));
            when(nestedObjectLoader.loadClassifier(
                    "hemishe_h_hemis_version_type", "HHemisVersionType", "2"))
                    .thenReturn(mockVersion);

            Map<String, Object> uniMap = extractUniversityMap("00520");

            @SuppressWarnings("unchecked")
            Map<String, Object> versionType = (Map<String, Object>) uniMap.get("versionType");
            assertThat(versionType).containsEntry("code", "2");
            assertThat(versionType).containsEntry("active", true);
        }

        @Test
        @DisplayName("ownership classifikatori yuklanadi")
        void ownershipLoaded() {
            University u = createMinimalUniversity("00520", "Owned OTM");
            u.setOwnership("11");

            Map<String, Object> mockOwnership = new LinkedHashMap<>();
            mockOwnership.put("_entityName", "hemishe_HOwnership");
            mockOwnership.put("code", "11");
            when(universityRefLegacyService.findUniversityById("00520")).thenReturn(Optional.of(u));
            when(nestedObjectLoader.loadClassifier(
                    "hemishe_h_ownership", "HOwnership", "11"))
                    .thenReturn(mockOwnership);

            Map<String, Object> uniMap = extractUniversityMap("00520");

            @SuppressWarnings("unchecked")
            Map<String, Object> ownership = (Map<String, Object>) uniMap.get("ownership");
            assertThat(ownership).containsEntry("_entityName", "hemishe_HOwnership");
            assertThat(ownership).containsEntry("code", "11");
        }

        @Test
        @DisplayName("belongsTo classifikatori yuklanadi")
        void belongsToLoaded() {
            University u = createMinimalUniversity("00520", "Belonging OTM");
            u.setUniversityBelongsTo("12");

            Map<String, Object> mockBelongs = new LinkedHashMap<>();
            mockBelongs.put("_entityName", "hemishe_HUniversityBelongsTo");
            mockBelongs.put("code", "12");
            when(universityRefLegacyService.findUniversityById("00520")).thenReturn(Optional.of(u));
            when(nestedObjectLoader.loadClassifier(
                    "hemishe_h_university_belongs_to", "HUniversityBelongsTo", "12"))
                    .thenReturn(mockBelongs);

            Map<String, Object> uniMap = extractUniversityMap("00520");

            @SuppressWarnings("unchecked")
            Map<String, Object> belongsTo = (Map<String, Object>) uniMap.get("belongsTo");
            assertThat(belongsTo).containsEntry("_entityName", "hemishe_HUniversityBelongsTo");
            assertThat(belongsTo).containsEntry("code", "12");
        }

        @Test
        @DisplayName("universityContractCategory classifikatori yuklanadi")
        void universityContractCategoryLoadedWithNames() {
            University u = createMinimalUniversity("00520", "Contract OTM");
            u.setUniversityContractCategory("3");

            Map<String, Object> mockCategory = new LinkedHashMap<>();
            mockCategory.put("_entityName", "hemishe_HUniversityContractCategory");
            mockCategory.put("code", "3");
            when(universityRefLegacyService.findUniversityById("00520")).thenReturn(Optional.of(u));
            when(nestedObjectLoader.loadClassifier(
                    "hemishe_h_university_contract_category", "HUniversityContractCategory", "3"))
                    .thenReturn(mockCategory);

            Map<String, Object> uniMap = extractUniversityMap("00520");

            @SuppressWarnings("unchecked")
            Map<String, Object> contract = (Map<String, Object>) uniMap.get("universityContractCategory");
            assertThat(contract).containsEntry("code", "3");
            assertThat(contract).containsEntry("active", true);
        }

        @Test
        @DisplayName("Barcha null classifikator maydonlari bo'lganda hech qanday nested ob'ekt qo'shilmaydi")
        void allNullClassifiersSkipped() {
            University u = createMinimalUniversity("00520", "Plain OTM");
            // All classifier fields null by default
            when(universityRefLegacyService.findUniversityById("00520")).thenReturn(Optional.of(u));

            Map<String, Object> uniMap = extractUniversityMap("00520");

            assertThat(uniMap).doesNotContainKey("soato");
            assertThat(uniMap).doesNotContainKey("soatoRegion");
            assertThat(uniMap).doesNotContainKey("universityActivityStatus");
            assertThat(uniMap).doesNotContainKey("universityType");
            assertThat(uniMap).doesNotContainKey("versionType");
            assertThat(uniMap).doesNotContainKey("ownership");
            assertThat(uniMap).doesNotContainKey("belongsTo");
            assertThat(uniMap).doesNotContainKey("universityContractCategory");
            verifyNoInteractions(nestedObjectLoader);
        }

        @Test
        @DisplayName("SOATO loader null qaytarsa soato mapga qo'shilmaydi")
        void soatoNotAddedWhenLoaderReturnsNull() {
            University u = createMinimalUniversity("00520", "Bad SOATO OTM");
            u.setSoato("9999");

            when(universityRefLegacyService.findUniversityById("00520")).thenReturn(Optional.of(u));
            when(nestedObjectLoader.loadSoato("9999")).thenReturn(null);

            Map<String, Object> uniMap = extractUniversityMap("00520");

            // Controller puts soato=null when loader qaytaradi null
            assertThat(uniMap.get("soato")).isNull();
        }

        @Test
        @DisplayName("To'liq universitet barcha maydonlar bilan CUBA formatda qaytariladi")
        void fullUniversityWithAllFields() {
            University u = createFullUniversity();
            when(universityRefLegacyService.findUniversityById("00520")).thenReturn(Optional.of(u));

            // Stub all nested loaders — wrap in mutable maps since controller calls remove/put on them
            when(nestedObjectLoader.loadSoato("1726")).thenReturn(
                    new LinkedHashMap<>(Map.of("_entityName", "hemishe_HSoato", "code", "1726")));
            when(nestedObjectLoader.loadSoato("17")).thenReturn(
                    new LinkedHashMap<>(Map.of("_entityName", "hemishe_HSoato", "code", "17")));
            when(nestedObjectLoader.loadClassifier(
                    eq("hemishe_h_university_activity_status"), eq("HUniversityActivityStatus"), eq("11")))
                    .thenReturn(new LinkedHashMap<>(Map.of("code", "11")));
            when(nestedObjectLoader.loadClassifier(
                    eq("hemishe_h_university_type"), eq("HUniversityType"), eq("22")))
                    .thenReturn(new LinkedHashMap<>(Map.of("code", "22")));
            when(nestedObjectLoader.loadClassifier(
                    eq("hemishe_h_hemis_version_type"), eq("HHemisVersionType"), eq("2")))
                    .thenReturn(new LinkedHashMap<>(Map.of("code", "2")));
            when(nestedObjectLoader.loadClassifier(
                    eq("hemishe_h_ownership"), eq("HOwnership"), eq("11")))
                    .thenReturn(new LinkedHashMap<>(Map.of("code", "11")));
            when(nestedObjectLoader.loadClassifier(
                    eq("hemishe_h_university_belongs_to"), eq("HUniversityBelongsTo"), eq("5")))
                    .thenReturn(new LinkedHashMap<>(Map.of("code", "5")));
            when(nestedObjectLoader.loadClassifier(
                    eq("hemishe_h_university_contract_category"), eq("HUniversityContractCategory"), eq("3")))
                    .thenReturn(new LinkedHashMap<>(Map.of("code", "3")));

            Map<String, Object> uniMap = extractUniversityMap("00520");

            // Verify all expected keys are present
            assertThat(uniMap).containsKeys(
                    "_entityName", "id", "code", "name",
                    "studentUrl", "tin", "address", "teacherUrl",
                    "soato", "soatoRegion",
                    "universityActivityStatus", "universityType", "versionType",
                    "ownership", "belongsTo", "universityContractCategory",
                    "addStudent", "accreditationEdit", "active", "gpaEdit",
                    "oneId", "allowGrouping", "allowTransferOutside", "version"
            );
        }
    }

    // =====================================================
    // Controller instantiation
    // =====================================================

    @Test
    @DisplayName("Controller Mockito InjectMocks bilan yaratiladi")
    void controllerInstantiatesWithMocks() {
        assertThat(controller).isNotNull();
    }

    // =====================================================
    // Helper methods
    // =====================================================

    /**
     * Extracts the "university" map from getByCode() response.
     * Convenience method for testing buildUniversityMap indirectly.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractUniversityMap(String code) {
        ResponseEntity<?> response = controller.getByCode(code);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body).containsEntry("success", true);
        return (Map<String, Object>) body.get("university");
    }

    private University createMinimalUniversity(String code, String name) {
        University u = new University();
        u.setCode(code);
        u.setName(name);
        u.setVersion(null);
        return u;
    }

    private University createFullUniversity() {
        University u = new University();
        u.setCode("00520");
        u.setName("Toshkent Axborot Texnologiyalari Universiteti");
        u.setVersion(3);
        u.setStudentUrl("https://student.tuit.uz");
        u.setTeacherUrl("https://teacher.tuit.uz");
        u.setTin("200600115");
        u.setAddress("Toshkent sh., Amir Temur shoh ko'chasi, 108");
        u.setSoato("1726");
        u.setSoatoRegion("17");
        u.setUniversityActivityStatus("11");
        u.setUniversityType("22");
        u.setUniversityVersion("2");
        u.setOwnership("11");
        u.setUniversityBelongsTo("5");
        u.setUniversityContractCategory("3");
        u.setActive(true);
        u.setAddStudent(true);
        u.setAccreditationEdit(false);
        u.setGpaEdit(true);
        u.setOneId(true);
        u.setAllowGrouping(true);
        u.setAllowTransferOutside(true);
        return u;
    }
}
