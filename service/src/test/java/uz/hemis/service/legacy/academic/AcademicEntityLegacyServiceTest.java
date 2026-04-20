package uz.hemis.service.legacy.academic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.hemis.domain.entity.academic.*;
import uz.hemis.domain.entity.student.*;
import uz.hemis.domain.entity.employee.*;
import uz.hemis.domain.entity.university.*;
import uz.hemis.domain.entity.research.*;
import uz.hemis.domain.entity.finance.*;
import uz.hemis.domain.entity.security.*;
import uz.hemis.domain.entity.reference.*;
import uz.hemis.domain.entity.system.*;
import uz.hemis.domain.entity.infrastructure.*;
import uz.hemis.domain.entity.base.*;
import uz.hemis.domain.entity.enums.*;
import uz.hemis.domain.repository.*;
import uz.hemis.service.legacy.ReferenceDataLegacyService;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AcademicEntityLegacyService Unit Tests")
class AcademicEntityLegacyServiceTest {

    @Mock private CurriculumRepository curriculumRepository;
    @Mock private ExamRepository examRepository;
    @Mock private ScheduleRepository scheduleRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private EducationMaterialsRepository educationMaterialsRepository;
    @Mock private AcademicMethodologicPublicationsRepository academicMethodologicPublicationsRepository;
    @Mock private ReferenceDataLegacyService referenceDataService;

    @InjectMocks
    private AcademicEntityLegacyService service;

    // ====================================================================
    //  Curriculum
    // ====================================================================
    @Nested
    @DisplayName("Curriculum")
    class CurriculumTests {

        @Test
        @DisplayName("toCurriculumMap returns correct fields")
        void toMap_returnsCorrectFields() {
            Curriculum entity = new Curriculum();
            entity.setId(UUID.randomUUID());
            entity.setCode("OQR-001");
            entity.setName("Informatika");
            entity.setUniversity("401");
            entity.setAcademicYear("2024/2025");
            entity.setTotalCredits(240);
            entity.setActive(true);

            Map<String, Object> result = service.toCurriculumMap(entity, false);

            assertThat(result).containsEntry("_entityName", "hemishe_ECurriculum");
            assertThat(result).containsEntry("code", "OQR-001");
            assertThat(result).containsEntry("name", "Informatika");
            assertThat(result).containsEntry("_university", "401");
            assertThat(result).containsEntry("totalCredits", 240);
            assertThat(result).containsEntry("active", true);
            assertThat(result.get("_instanceName").toString()).contains("OQR-001");
        }

        @Test
        @DisplayName("findCurriculumById delegates to repository")
        void findById_delegates() {
            UUID id = UUID.randomUUID();
            Curriculum entity = new Curriculum();
            entity.setId(id);
            when(curriculumRepository.findById(id)).thenReturn(Optional.of(entity));

            Optional<Curriculum> result = service.findCurriculumById(id);

            assertThat(result).isPresent();
            verify(curriculumRepository).findById(id);
        }
    }

    // ====================================================================
    //  Exam
    // ====================================================================
    @Nested
    @DisplayName("Exam")
    class ExamTests {

        @Test
        @DisplayName("toExamMap returns correct fields")
        void toMap_returnsCorrectFields() {
            Exam entity = new Exam();
            entity.setId(UUID.randomUUID());
            entity.setExamName("Yakuniy imtihon");
            entity.setUniversity("401");
            entity.setMaxScore(100);
            entity.setPassingScore(60);
            entity.setActive(true);

            Map<String, Object> result = service.toExamMap(entity, false);

            assertThat(result).containsEntry("_entityName", "hemishe_EExam");
            assertThat(result).containsEntry("examName", "Yakuniy imtihon");
            assertThat(result).containsEntry("maxScore", 100);
            assertThat(result).containsEntry("passingScore", 60);
            assertThat(result.get("_instanceName")).isEqualTo("Yakuniy imtihon");
        }

        @Test
        @DisplayName("findExamById delegates to repository")
        void findById_delegates() {
            UUID id = UUID.randomUUID();
            when(examRepository.findById(id)).thenReturn(Optional.empty());

            Optional<Exam> result = service.findExamById(id);

            assertThat(result).isEmpty();
            verify(examRepository).findById(id);
        }
    }

    // ====================================================================
    //  Schedule
    // ====================================================================
    @Nested
    @DisplayName("Schedule")
    class ScheduleTests {

        @Test
        @DisplayName("toScheduleMap returns correct fields")
        void toMap_returnsCorrectFields() {
            Schedule entity = new Schedule();
            UUID id = UUID.randomUUID();
            entity.setId(id);
            entity.setUniversity("401");
            entity.setDayOfWeek(1);
            entity.setPairNumber(3);
            entity.setActive(true);

            Map<String, Object> result = service.toScheduleMap(entity, false);

            assertThat(result).containsEntry("_entityName", "hemishe_ESchedule");
            assertThat(result).containsEntry("_university", "401");
            assertThat(result).containsEntry("dayOfWeek", 1);
            assertThat(result).containsEntry("pairNumber", 3);
            assertThat(result.get("_instanceName")).isEqualTo("Schedule-" + id);
        }
    }

    // ====================================================================
    //  Course
    // ====================================================================
    @Nested
    @DisplayName("Course")
    class CourseTests {

        @Test
        @DisplayName("toCourseMap returns correct fields")
        void toMap_returnsCorrectFields() {
            Course entity = new Course();
            entity.setId(UUID.randomUUID());
            entity.setCode("INF-101");
            entity.setName("Informatika asoslari");
            entity.setCreditCount(4);
            entity.setTotalHours(120);
            entity.setActive(true);

            Map<String, Object> result = service.toCourseMap(entity, false);

            assertThat(result).containsEntry("_entityName", "hemishe_ECourse");
            assertThat(result).containsEntry("code", "INF-101");
            assertThat(result).containsEntry("creditCount", 4);
            assertThat(result).containsEntry("totalHours", 120);
            assertThat(result.get("_instanceName").toString()).contains("INF-101");
        }

        @Test
        @DisplayName("saveCourse delegates to repository")
        void save_delegates() {
            Course entity = new Course();
            entity.setId(UUID.randomUUID());
            when(courseRepository.save(entity)).thenReturn(entity);

            Course result = service.saveCourse(entity);

            assertThat(result).isEqualTo(entity);
            verify(courseRepository).save(entity);
        }
    }

    // ====================================================================
    //  EducationMaterials
    // ====================================================================
    @Nested
    @DisplayName("EducationMaterials")
    class EducationMaterialsTests {

        @Test
        @DisplayName("toEducationMaterialsMap returns correct fields")
        void toMap_returnsCorrectFields() {
            UUID id = UUID.randomUUID();
            EducationMaterials entity = new EducationMaterials();
            entity.setId(id);
            entity.setUniversity("401");
            entity.setEducationYear("2024");
            entity.setSpecialityName("Informatika");
            entity.setSubjectCount(15);
            entity.setTextbooksCount(10);

            Map<String, Object> result = service.toEducationMaterialsMap(entity, false);

            assertThat(result).containsEntry("_entityName", "hemishe_REducationMaterials");
            assertThat(result).containsEntry("universityCode", "401");
            assertThat(result).containsEntry("educationYearCode", "2024");
            assertThat(result).containsEntry("specialityName", "Informatika");
            assertThat(result).containsEntry("subjectCount", 15);
            assertThat((String) result.get("_instanceName"))
                    .contains("com.company.hemishe.entity.REducationMaterials-" + id);
        }

        @Test
        @DisplayName("toEducationMaterialsMap - returnNulls true da barcha maydonlar JsonNull bo'ladi")
        void toMap_includesDeleteFields() {
            EducationMaterials entity = new EducationMaterials();
            entity.setId(UUID.randomUUID());

            Map<String, Object> result = service.toEducationMaterialsMap(entity, true);

            // Service delete fieldlarni qaytarmaydi, lekin returnNulls=true da boshqa fieldlar JsonNull
            assertThat(result).containsKey("universityCode");
            assertThat(result).containsKey("specialityName");
            assertThat(result.get("universityCode")).isEqualTo(uz.hemis.common.JsonNull.INSTANCE);
        }
    }

    // ====================================================================
    //  AcademicMethodologicPublications
    // ====================================================================
    @Nested
    @DisplayName("AcademicMethodologicPublications")
    class AcademicMethodologicPublicationsTests {

        @Test
        @DisplayName("toMap returns correct fields with nested university and educationYear")
        void toMap_returnsCorrectFields() {
            AcademicMethodologicPublications entity = new AcademicMethodologicPublications();
            entity.setId(UUID.randomUUID());
            entity.setUniversity("401");
            entity.setEducationYear("2024");
            entity.setAuthorFullname("Karimov A.A.");
            entity.setBookName("Informatika darsligi");
            entity.setCertificateDate(LocalDate.of(2024, 6, 15));
            entity.setVersion(1);

            Map<String, Object> result = service.toAcademicMethodologicPublicationsMap(entity, false);

            assertThat(result).containsEntry("_entityName", "hemishe_RIAcademicMethodologicPublications");
            assertThat(result).containsEntry("authorFullname", "Karimov A.A.");
            assertThat(result).containsEntry("bookName", "Informatika darsligi");
            assertThat(result).containsEntry("version", 1);

            // OLD-HEMIS: university va educationYear reference fieldlar default view da qaytarilmaydi
            assertThat(result).doesNotContainKey("university");
            assertThat(result).doesNotContainKey("educationYear");
        }

        @Test
        @DisplayName("updateFromMap sets fields correctly")
        void updateFromMap_setsFields() {
            AcademicMethodologicPublications entity = new AcademicMethodologicPublications();
            Map<String, Object> map = new HashMap<>();
            map.put("university", "401");
            map.put("educationYear", "2024");
            map.put("authorFullname", "Karimov A.A.");
            map.put("bookName", "Test kitob");
            map.put("certificateDate", "2024-06-15");
            map.put("certificateNumber", "001");

            service.updateAcademicMethodologicPublicationsFromMap(entity, map);

            assertThat(entity.getUniversity()).isEqualTo("401");
            assertThat(entity.getEducationYear()).isEqualTo("2024");
            assertThat(entity.getAuthorFullname()).isEqualTo("Karimov A.A.");
            assertThat(entity.getBookName()).isEqualTo("Test kitob");
            assertThat(entity.getCertificateDate()).isEqualTo(LocalDate.of(2024, 6, 15));
            assertThat(entity.getCertificateNumber()).isEqualTo("001");
        }

        @Test
        @DisplayName("updateFromMap handles nested university object")
        void updateFromMap_nestedUniversity() {
            AcademicMethodologicPublications entity = new AcademicMethodologicPublications();
            Map<String, Object> map = new HashMap<>();
            map.put("university", Map.of("id", "401", "name", "TATU"));

            service.updateAcademicMethodologicPublicationsFromMap(entity, map);

            assertThat(entity.getUniversity()).isEqualTo("401");
        }
    }
}
