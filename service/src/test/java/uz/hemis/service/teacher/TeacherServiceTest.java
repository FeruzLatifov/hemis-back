package uz.hemis.service.teacher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Example;
import uz.hemis.domain.entity.employee.Teacher;
import uz.hemis.domain.entity.university.University;
import uz.hemis.domain.entity.security.User;
import uz.hemis.domain.repository.TeacherRepository;
import uz.hemis.domain.repository.UserRepository;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TeacherService}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>generateTeacherId - validation, existing teacher, new teacher creation</li>
 *   <li>getTeacherByPinfl - found by PINFL, found by serial, not found</li>
 *   <li>getTeacherByCode - found, not found, null/empty input</li>
 *   <li>getUserUniversityCode - found, not found, invalid UUID</li>
 * </ul>
 *
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TeacherService Unit Tests")
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private UserRepository userRepository;

    /** P2.T1 — generateUniqueCode endi pg_advisory_xact_lock orqali JdbcTemplate ishlatadi. */
    @Mock
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Mock
    private uz.hemis.domain.repository.EmployeeJobsRepository employeeJobsRepository;

    @InjectMocks
    private TeacherService teacherService;

    private UUID teacherId;
    private Teacher sampleTeacher;

    @BeforeEach
    void setUp() {
        teacherId = UUID.randomUUID();

        sampleTeacher = new Teacher();
        sampleTeacher.setId(teacherId);
        sampleTeacher.setCode("40124101001");
        sampleTeacher.setPinfl("12345678901234");
        sampleTeacher.setSerialNumber("AA1234567");
        sampleTeacher.setFirstname("Akbar");
        sampleTeacher.setLastname("Rahimov");
        sampleTeacher.setFathername("Xasanovich");
        sampleTeacher.setGender("1");
        sampleTeacher.setCitizenship("11");
        sampleTeacher.setUniversity("401");
        sampleTeacher.setEmployeeYear("2024");
    }

    // =====================================================
    // generateTeacherId tests
    // =====================================================

    @Nested
    @DisplayName("generateTeacherId")
    class GenerateTeacherId {

        @Test
        @DisplayName("returns existing teacher when found by PINFL (citizen)")
        void returnsExisting_whenFoundByPinfl() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("citizenship", "11");
            data.put("pinfl", "12345678901234");
            data.put("serial", "AA1234567");
            data.put("gender", "1");
            data.put("year", "2024");

            when(teacherRepository.findByPinfl("12345678901234"))
                    .thenReturn(Optional.of(sampleTeacher));

            Map<String, Object> result = teacherService.generateTeacherId(data, "401");

            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result.get("is_new")).isEqualTo(false);
            assertThat(result.get("unique_id")).isEqualTo("40124101001");

            verify(teacherRepository).findByPinfl("12345678901234");
            verify(teacherRepository, never()).save(any());
        }

        @Test
        @DisplayName("returns existing teacher when found by serial and citizenship (foreigner)")
        void returnsExisting_whenFoundBySerialAndCitizenship() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("citizenship", "22");
            data.put("pinfl", "");
            data.put("serial", "AA1234567");
            data.put("gender", "1");
            data.put("year", "2024");

            when(teacherRepository.findBySerialNumberAndCitizenship("AA1234567", "22"))
                    .thenReturn(Optional.of(sampleTeacher));

            Map<String, Object> result = teacherService.generateTeacherId(data, "401");

            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result.get("is_new")).isEqualTo(false);

            verify(teacherRepository).findBySerialNumberAndCitizenship("AA1234567", "22");
            verify(teacherRepository, never()).save(any());
        }

        @Test
        @DisplayName("creates new teacher when not found")
        void createsNew_whenNotFound() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("citizenship", "11");
            data.put("pinfl", "99999999999999");
            data.put("serial", "BB9999999");
            data.put("gender", "2");
            data.put("year", "2024");

            when(teacherRepository.findByPinfl("99999999999999"))
                    .thenReturn(Optional.empty());
            when(teacherRepository.countByCodePrefix("401242")).thenReturn(0L);
            when(teacherRepository.existsByCode(anyString())).thenReturn(false);
            when(teacherRepository.save(any(Teacher.class))).thenAnswer(invocation -> {
                Teacher saved = invocation.getArgument(0);
                return saved;
            });

            Map<String, Object> result = teacherService.generateTeacherId(data, "401");

            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result.get("is_new")).isEqualTo(true);
            assertThat(result.get("unique_id")).isNotNull();

            verify(teacherRepository).save(any(Teacher.class));
        }

        @Test
        @DisplayName("returns validation error when citizenship is missing")
        void returnsError_whenCitizenshipMissing() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("pinfl", "12345678901234");
            data.put("serial", "AA1234567");
            data.put("gender", "1");
            data.put("year", "2024");

            Map<String, Object> result = teacherService.generateTeacherId(data, "401");

            assertThat(result.get("success")).isEqualTo(false);
            assertThat(result.get("message")).isEqualTo("Citizenship value incorrect");

            verify(teacherRepository, never()).save(any());
        }

        @Test
        @DisplayName("returns validation error when PINFL missing for citizen")
        void returnsError_whenPinflMissingForCitizen() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("citizenship", "11");
            data.put("serial", "AA1234567");
            data.put("gender", "1");
            data.put("year", "2024");

            Map<String, Object> result = teacherService.generateTeacherId(data, "401");

            assertThat(result.get("success")).isEqualTo(false);
            assertThat(result.get("message")).isEqualTo("PINFL value incorrect");
        }

        @Test
        @DisplayName("returns validation error when gender is missing")
        void returnsError_whenGenderMissing() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("citizenship", "11");
            data.put("pinfl", "12345678901234");
            data.put("serial", "AA1234567");
            data.put("year", "2024");

            Map<String, Object> result = teacherService.generateTeacherId(data, "401");

            assertThat(result.get("success")).isEqualTo(false);
            assertThat(result.get("message")).isEqualTo("Gender value incorrect");
        }

        @Test
        @DisplayName("returns validation error when year is missing")
        void returnsError_whenYearMissing() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("citizenship", "11");
            data.put("pinfl", "12345678901234");
            data.put("serial", "AA1234567");
            data.put("gender", "1");

            Map<String, Object> result = teacherService.generateTeacherId(data, "401");

            assertThat(result.get("success")).isEqualTo(false);
            assertThat(result.get("message")).isEqualTo("Year value incorrect");
        }
    }

    // =====================================================
    // getTeacherByPinfl tests
    // =====================================================

    @Nested
    @DisplayName("getTeacherByPinfl")
    class GetTeacherByPinfl {

        @Test
        @DisplayName("returns success with teacher data when found by PINFL (with university scope)")
        void returnsSuccess_whenFoundByPinfl() {
            when(teacherRepository.findByPinflAndUniversity("12345678901234", "401"))
                    .thenReturn(Optional.of(sampleTeacher));

            Map<String, Object> result = teacherService.getTeacherByPinfl("12345678901234", "401");

            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result.get("code")).isEqualTo("ok");

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            assertThat(data.get("id")).isEqualTo("40124101001");
            assertThat(data.get("pinfl")).isEqualTo("12345678901234");

            verify(teacherRepository).findByPinflAndUniversity("12345678901234", "401");
        }

        @Test
        @DisplayName("falls back to serial number search when not found by PINFL")
        void fallsBackToSerial_whenNotFoundByPinfl() {
            when(teacherRepository.findByPinflAndUniversity("SERIAL_NUM", "401"))
                    .thenReturn(Optional.empty());
            when(teacherRepository.findBySerialNumberAndUniversity("SERIAL_NUM", "401"))
                    .thenReturn(Optional.of(sampleTeacher));

            Map<String, Object> result = teacherService.getTeacherByPinfl("SERIAL_NUM", "401");

            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result.get("code")).isEqualTo("ok");

            verify(teacherRepository).findByPinflAndUniversity("SERIAL_NUM", "401");
            verify(teacherRepository).findBySerialNumberAndUniversity("SERIAL_NUM", "401");
        }

        @Test
        @DisplayName("returns not_found when teacher not found anywhere")
        void returnsNotFound_whenNotFoundAnywhere() {
            when(teacherRepository.findByPinflAndUniversity("99999999999999", "401"))
                    .thenReturn(Optional.empty());
            when(teacherRepository.findBySerialNumberAndUniversity("99999999999999", "401"))
                    .thenReturn(Optional.empty());

            Map<String, Object> result = teacherService.getTeacherByPinfl("99999999999999", "401");

            assertThat(result.get("success")).isEqualTo(false);
            assertThat(result.get("code")).isEqualTo("not_found");
        }

        @Test
        @DisplayName("returns bad_request when PINFL is null or empty")
        void returnsBadRequest_whenPinflEmpty() {
            Map<String, Object> resultNull = teacherService.getTeacherByPinfl(null, "401");
            assertThat(resultNull.get("success")).isEqualTo(false);
            assertThat(resultNull.get("code")).isEqualTo("bad_request");

            Map<String, Object> resultEmpty = teacherService.getTeacherByPinfl("", "401");
            assertThat(resultEmpty.get("success")).isEqualTo(false);
            assertThat(resultEmpty.get("code")).isEqualTo("bad_request");
        }

        @Test
        @DisplayName("returns forbidden when university scope is missing")
        void returnsForbidden_whenScopeMissing() {
            // OWASP A01 — universityCode majburiy parametr.
            Map<String, Object> result = teacherService.getTeacherByPinfl("12345678901234", null);
            assertThat(result.get("success")).isEqualTo(false);
            assertThat(result.get("code")).isEqualTo("forbidden");
        }
    }

    // =====================================================
    // getTeacherByCode tests
    // =====================================================

    @Nested
    @DisplayName("getTeacherByCode")
    class GetTeacherByCode {

        @Test
        @DisplayName("returns success with teacher data when found by code")
        void returnsSuccess_whenFound() {
            // P1.T2 — direct findByCode, no Example.of probe.
            when(teacherRepository.findByCode("40124101001"))
                    .thenReturn(Optional.of(sampleTeacher));

            Map<String, Object> result = teacherService.getTeacherByCode("40124101001");

            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result.get("code")).isEqualTo("ok");

            verify(teacherRepository).findByCode("40124101001");
        }

        @Test
        @DisplayName("returns not_found when code not found")
        void returnsNotFound_whenCodeNotFound() {
            when(teacherRepository.findByCode("NONEXIST"))
                    .thenReturn(Optional.empty());

            Map<String, Object> result = teacherService.getTeacherByCode("NONEXIST");

            assertThat(result.get("success")).isEqualTo(false);
            assertThat(result.get("code")).isEqualTo("not_found");
        }

        @Test
        @DisplayName("returns bad_request when code is null or empty")
        void returnsBadRequest_whenCodeEmpty() {
            Map<String, Object> resultNull = teacherService.getTeacherByCode(null);
            assertThat(resultNull.get("success")).isEqualTo(false);
            assertThat(resultNull.get("code")).isEqualTo("bad_request");

            Map<String, Object> resultEmpty = teacherService.getTeacherByCode("");
            assertThat(resultEmpty.get("success")).isEqualTo(false);
            assertThat(resultEmpty.get("code")).isEqualTo("bad_request");
        }
    }

    // =====================================================
    // getUserUniversityCode tests
    // =====================================================

    @Nested
    @DisplayName("getUserUniversityCode")
    class GetUserUniversityCode {

        @Test
        @DisplayName("returns university code when user found with university")
        void returnsCode_whenUserFound() {
            UUID userId = UUID.randomUUID();
            University university = new University();
            university.setCode("401");

            User user = new User();
            user.setUniversity(university);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            String result = teacherService.getUserUniversityCode(userId.toString());

            assertThat(result).isEqualTo("401");
            verify(userRepository).findById(userId);
        }

        @Test
        @DisplayName("returns null when user not found")
        void returnsNull_whenUserNotFound() {
            UUID userId = UUID.randomUUID();
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            String result = teacherService.getUserUniversityCode(userId.toString());

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("returns null when user has no university")
        void returnsNull_whenNoUniversity() {
            UUID userId = UUID.randomUUID();
            User user = new User();
            user.setUniversity(null);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            String result = teacherService.getUserUniversityCode(userId.toString());

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("returns null when userId is invalid UUID")
        void returnsNull_whenInvalidUuid() {
            String result = teacherService.getUserUniversityCode("not-a-uuid");

            assertThat(result).isNull();
            verify(userRepository, never()).findById(any());
        }
    }
}
