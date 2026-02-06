package uz.hemis.service.legacy.student;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.hemis.domain.entity.*;
import uz.hemis.domain.repository.*;
import uz.hemis.service.legacy.ReferenceDataLegacyService;
import uz.hemis.service.legacy.SoftDeleteRestoreLegacyService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentEntityLegacyService Tests")
class StudentEntityLegacyServiceTest {

    @Mock
    private StudentCertificateRepository studentCertificateRepository;
    @Mock
    private AdministrativeStudent2Repository administrativeStudent2Repository;
    @Mock
    private AdministrativeStudent3Repository administrativeStudent3Repository;
    @Mock
    private AdministrativeStudent4Repository administrativeStudent4Repository;
    @Mock
    private AdministrativeStudentSportRepository administrativeStudentSportRepository;
    @Mock
    private AdministrativeSportFacilitiesRepository administrativeSportFacilitiesRepository;
    @Mock
    private CitizenshipRepository citizenshipRepository;
    @Mock
    private ExpelRepository expelRepository;
    @Mock
    private StudentStatusTypeRepository studentStatusTypeRepository;
    @Mock
    private ReferenceDataLegacyService referenceDataService;
    @Mock
    private SoftDeleteRestoreLegacyService softDeleteRestoreService;

    @InjectMocks
    private StudentEntityLegacyService service;

    // =====================================================
    // StudentCertificate Tests
    // =====================================================

    @Nested
    @DisplayName("StudentCertificate")
    class StudentCertificateTests {

        private StudentCertificate entity;
        private UUID testId;

        @BeforeEach
        void setUp() {
            testId = UUID.randomUUID();
            entity = new StudentCertificate();
            entity.setId(testId);
            entity.setUniversity("301");
            entity.setStudent(UUID.randomUUID());
            entity.setCertificateType("IELTS");
            entity.setCertificateName("IELTS Academic");
            entity.setCertificateGrade("7.5");
            entity.setSerialNumber("SN-12345");
            entity.setActive(true);
            entity.setIssueDate(LocalDate.of(2024, 1, 15));
        }

        @Test
        @DisplayName("findById - mavjud entity qaytaradi")
        void findById_returnsEntity() {
            when(studentCertificateRepository.findById(testId)).thenReturn(Optional.of(entity));

            Optional<StudentCertificate> result = service.findStudentCertificateById(testId);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(testId);
        }

        @Test
        @DisplayName("toMap - CUBA format to'g'ri qaytaradi")
        void toMap_returnsCubaFormat() {
            Map<String, Object> map = service.toStudentCertificateMap(entity, false);

            assertThat(map.get("_entityName")).isEqualTo("hemishe_EStudentCertificate");
            assertThat(map.get("_instanceName")).isEqualTo("SN-12345");
            assertThat(map.get("id")).isEqualTo(testId);
            assertThat(map.get("university")).isEqualTo("301");
            assertThat(map.get("certificateType")).isEqualTo("IELTS");
            assertThat(map.get("active")).isEqualTo(true);
        }

        @Test
        @DisplayName("updateFromMap - oddiy qiymatlar yangilanadi")
        void updateFromMap_updatesSimpleValues() {
            Map<String, Object> data = new HashMap<>();
            data.put("certificateName", "New Name");
            data.put("certificateGrade", "8.0");
            data.put("active", false);

            service.updateStudentCertificateFromMap(entity, data);

            assertThat(entity.getCertificateName()).isEqualTo("New Name");
            assertThat(entity.getCertificateGrade()).isEqualTo("8.0");
            assertThat(entity.getActive()).isFalse();
        }

        @Test
        @DisplayName("updateFromMap - nested object bilan ishlaydi")
        void updateFromMap_handlesNestedObject() {
            UUID studentId = UUID.randomUUID();
            Map<String, Object> data = new HashMap<>();
            data.put("student", Map.of("id", studentId.toString()));
            data.put("university", Map.of("code", "401"));

            service.updateStudentCertificateFromMap(entity, data);

            assertThat(entity.getStudent()).isEqualTo(studentId);
            assertThat(entity.getUniversity()).isEqualTo("401");
        }
    }

    // =====================================================
    // AdministrativeStudent3 Tests
    // =====================================================

    @Nested
    @DisplayName("AdministrativeStudent3")
    class AdministrativeStudent3Tests {

        private AdministrativeStudent3 entity;
        private UUID testId;

        @BeforeEach
        void setUp() {
            testId = UUID.randomUUID();
            entity = new AdministrativeStudent3();
            entity.setId(testId);
            entity.setCompany("Test Company");
            entity.setPosition("Developer");
            entity.setMastersUniversityName("MIT");
        }

        @Test
        @DisplayName("toMap - to'g'ri CUBA format qaytaradi")
        void toMap_returnsCubaFormat() {
            Map<String, Object> map = service.toAdministrativeStudent3Map(entity, false);

            assertThat(map.get("_entityName")).isEqualTo("hemishe_RIAdministrativeStudent3");
            assertThat((String) map.get("_instanceName")).contains("RIAdministrativeStudent3");
            assertThat(map.get("id")).isEqualTo(testId);
            assertThat(map.get("company")).isEqualTo("Test Company");
            assertThat(map.get("position")).isEqualTo("Developer");
            assertThat(map.get("mastersUniversityName")).isEqualTo("MIT");
        }

        @Test
        @DisplayName("updateFromMap - UUID FK larni to'g'ri extract qiladi")
        void updateFromMap_extractsUuidFKs() {
            UUID universityId = UUID.randomUUID();
            UUID studentId = UUID.randomUUID();

            Map<String, Object> data = new HashMap<>();
            data.put("university", Map.of("id", universityId.toString()));
            data.put("student", Map.of("id", studentId.toString()));
            data.put("company", "New Company");

            service.updateAdministrativeStudent3FromMap(entity, data);

            assertThat(entity.getUniversity()).isEqualTo(universityId);
            assertThat(entity.getStudent()).isEqualTo(studentId);
            assertThat(entity.getCompany()).isEqualTo("New Company");
        }
    }

    // =====================================================
    // AdministrativeStudent4 Tests
    // =====================================================

    @Nested
    @DisplayName("AdministrativeStudent4")
    class AdministrativeStudent4Tests {

        private AdministrativeStudent4 entity;
        private UUID testId;

        @BeforeEach
        void setUp() {
            testId = UUID.randomUUID();
            entity = new AdministrativeStudent4();
            entity.setId(testId);
            entity.setOlimpiadaType("Xalqaro");
            entity.setOlimpiadaName("Matematika olimpiadasi");
            entity.setTakenPosition("1");
        }

        @Test
        @DisplayName("toMap - olympiada maydonlari to'g'ri qaytariladi")
        void toMap_returnsOlympiadFields() {
            Map<String, Object> map = service.toAdministrativeStudent4Map(entity, false);

            assertThat(map.get("_entityName")).isEqualTo("hemishe_RIAdministrativeStudent4");
            assertThat(map.get("olimpiadaType")).isEqualTo("Xalqaro");
            assertThat(map.get("olimpiadaName")).isEqualTo("Matematika olimpiadasi");
            assertThat(map.get("takenPosition")).isEqualTo("1");
        }
    }

    // =====================================================
    // AdministrativeStudentSport Tests
    // =====================================================

    @Nested
    @DisplayName("AdministrativeStudentSport")
    class AdministrativeStudentSportTests {

        private AdministrativeStudentSport entity;
        private UUID testId;

        @BeforeEach
        void setUp() {
            testId = UUID.randomUUID();
            entity = new AdministrativeStudentSport();
            entity.setId(testId);
            entity.setSportDate(LocalDate.of(2024, 6, 15));
            entity.setSportTypeRank("Master");
            entity.setSportTypeRankDocument("Doc-123");
        }

        @Test
        @DisplayName("toMap - sport maydonlari to'g'ri qaytariladi")
        void toMap_returnsSportFields() {
            Map<String, Object> map = service.toAdministrativeStudentSportMap(entity, false);

            assertThat(map.get("_entityName")).isEqualTo("hemishe_RIAdministrativeStudentSport");
            assertThat(map.get("sportDate")).isEqualTo(LocalDate.of(2024, 6, 15));
            assertThat(map.get("sportTypeRank")).isEqualTo("Master");
        }

        @Test
        @DisplayName("updateFromMap - sportDate to'g'ri parse qilinadi")
        void updateFromMap_parsesSportDate() {
            Map<String, Object> data = new HashMap<>();
            data.put("sportDate", "2025-01-20");

            service.updateAdministrativeStudentSportFromMap(entity, data);

            assertThat(entity.getSportDate()).isEqualTo(LocalDate.of(2025, 1, 20));
        }
    }

    // =====================================================
    // Citizenship Tests (String PK)
    // =====================================================

    @Nested
    @DisplayName("Citizenship")
    class CitizenshipTests {

        private Citizenship entity;

        @BeforeEach
        void setUp() {
            entity = new Citizenship();
            entity.setCode("11");
            entity.setName("O'zbekiston fuqarosi");
            entity.setActive(true);
        }

        @Test
        @DisplayName("findById - String kod bilan ishlaydi")
        void findById_worksWithStringCode() {
            when(citizenshipRepository.findById("11")).thenReturn(Optional.of(entity));

            Optional<Citizenship> result = service.findCitizenshipById("11");

            assertThat(result).isPresent();
            assertThat(result.get().getCode()).isEqualTo("11");
        }

        @Test
        @DisplayName("toMap - CUBA format to'g'ri qaytaradi")
        void toMap_returnsCubaFormat() {
            Map<String, Object> map = service.toCitizenshipMap(entity, false);

            assertThat(map.get("_entityName")).isEqualTo("hemishe_HCitizenship");
            assertThat((String) map.get("_instanceName")).contains("11");
            assertThat(map.get("id")).isEqualTo("11");
            assertThat(map.get("code")).isEqualTo("11");
            assertThat(map.get("name")).isEqualTo("O'zbekiston fuqarosi");
        }

        @Test
        @DisplayName("toMinimalMap - minimal response qaytaradi")
        void toMinimalMap_returnsMinimalResponse() {
            Map<String, Object> map = service.toCitizenshipMinimalMap(entity);

            assertThat(map).containsKeys("_entityName", "_instanceName", "id");
            assertThat(map).doesNotContainKey("name");
        }
    }

    // =====================================================
    // Expel Tests
    // =====================================================

    @Nested
    @DisplayName("Expel")
    class ExpelTests {

        private Expel entity;
        private UUID testId;

        @BeforeEach
        void setUp() {
            testId = UUID.randomUUID();
            entity = new Expel();
            entity.setId(testId);
            entity.setUniversityCode("301");
            entity.setUniversityName("TDTU");
            entity.setFacultyName("IT fakulteti");
            entity.setExpelReasonCode("01");
            entity.setExpelReasonName("O'z xohishi bilan");
            entity.setExpelCount(5);
        }

        @Test
        @DisplayName("toMap - barcha maydonlar to'g'ri qaytariladi")
        void toMap_returnsAllFields() {
            Map<String, Object> map = service.toExpelMap(entity, false);

            assertThat(map.get("_entityName")).isEqualTo("hemishe_RExpel");
            assertThat(map.get("_instanceName")).isEqualTo("TDTU");
            assertThat(map.get("universityCode")).isEqualTo("301");
            assertThat(map.get("facultyName")).isEqualTo("IT fakulteti");
            assertThat(map.get("expelCount")).isEqualTo(5);
        }

        @Test
        @DisplayName("updateFromMap - expelCount to'g'ri yangilanadi")
        void updateFromMap_updatesExpelCount() {
            Map<String, Object> data = new HashMap<>();
            data.put("expelCount", 10);
            data.put("expelReasonName", "Akademik qarzdorlik");

            service.updateExpelFromMap(entity, data);

            assertThat(entity.getExpelCount()).isEqualTo(10);
            assertThat(entity.getExpelReasonName()).isEqualTo("Akademik qarzdorlik");
        }
    }

    // =====================================================
    // StudentStatusType Tests (String PK)
    // =====================================================

    @Nested
    @DisplayName("StudentStatusType")
    class StudentStatusTypeTests {

        private StudentStatusType entity;

        @BeforeEach
        void setUp() {
            entity = new StudentStatusType();
            entity.setCode("11");
            entity.setName("O'qimoqda");
            entity.setActive(true);
        }

        @Test
        @DisplayName("toMap - CUBA format to'g'ri qaytaradi")
        void toMap_returnsCubaFormat() {
            Map<String, Object> map = service.toStudentStatusTypeMap(entity, false);

            assertThat(map.get("_entityName")).isEqualTo("hemishe_HStudentStatusType");
            assertThat((String) map.get("_instanceName")).contains("11");
            assertThat(map.get("id")).isEqualTo("11");
            assertThat(map.get("code")).isEqualTo("11");
            assertThat(map.get("name")).isEqualTo("O'qimoqda");
        }

        @Test
        @DisplayName("hasSoftDeleted - soft delete tekshiruvi")
        void hasSoftDeleted_checksCorrectTable() {
            when(softDeleteRestoreService.hasSoftDeletedRecord("hemishe_h_student_status_type", "11"))
                .thenReturn(true);

            boolean result = service.hasSoftDeletedStudentStatusType("11");

            assertThat(result).isTrue();
            verify(softDeleteRestoreService).hasSoftDeletedRecord("hemishe_h_student_status_type", "11");
        }
    }

    // =====================================================
    // AdministrativeSportFacilities Tests
    // =====================================================

    @Nested
    @DisplayName("AdministrativeSportFacilities")
    class AdministrativeSportFacilitiesTests {

        private AdministrativeSportFacilities entity;
        private UUID testId;

        @BeforeEach
        void setUp() {
            testId = UUID.randomUUID();
            entity = new AdministrativeSportFacilities();
            entity.setId(testId);
            entity.setUniversity("301");
            entity.setEducationYear("2024");
            entity.setSquare(1500.5);
        }

        @Test
        @DisplayName("toMap - nested university object qaytaradi")
        void toMap_returnsNestedUniversityObject() {
            when(referenceDataService.getUniversityData("301"))
                .thenReturn(Map.of("name", "TDTU"));
            when(referenceDataService.getEducationYearData("2024"))
                .thenReturn(Map.of("name", "2024-2025"));

            Map<String, Object> map = service.toAdministrativeSportFacilitiesMap(entity, false);

            assertThat(map.get("_entityName")).isEqualTo("hemishe_RIAdministrativeSportFacilities");
            assertThat(map.get("square")).isEqualTo(1500.5);

            @SuppressWarnings("unchecked")
            Map<String, Object> universityMap = (Map<String, Object>) map.get("university");
            assertThat(universityMap.get("_entityName")).isEqualTo("hemishe_EUniversity");
            assertThat(universityMap.get("code")).isEqualTo("301");
            assertThat(universityMap.get("name")).isEqualTo("TDTU");
        }

        @Test
        @DisplayName("updateFromMap - square to'g'ri parse qilinadi")
        void updateFromMap_parsesSquare() {
            Map<String, Object> data = new HashMap<>();
            data.put("square", "2000.75");

            service.updateAdministrativeSportFacilitiesFromMap(entity, data);

            assertThat(entity.getSquare()).isEqualTo(2000.75);
        }
    }
}
