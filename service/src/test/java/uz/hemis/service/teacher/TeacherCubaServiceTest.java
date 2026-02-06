package uz.hemis.service.teacher;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.hemis.domain.entity.Teacher;
import uz.hemis.domain.repository.TeacherRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TeacherCubaService - CUBA-compatible teacher lookup and job management")
class TeacherCubaServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @InjectMocks
    private TeacherCubaService service;

    // =====================================================
    // id(String data)
    // =====================================================

    @Nested
    @DisplayName("id(data)")
    class IdMethod {

        @Test
        @DisplayName("should return teacher ID when found by PINFL")
        void returnsTeacherIdWhenFound() {
            Teacher teacher = createTeacher();
            when(teacherRepository.findByPinfl("12345678901234")).thenReturn(Optional.of(teacher));

            Map<String, Object> result = service.id("12345678901234");

            assertThat(result).containsEntry("success", true);
            assertThat(result).containsEntry("id", teacher.getId());
            assertThat(result).containsEntry("pinfl", "12345678901234");
        }

        @Test
        @DisplayName("should return error when data parameter is null")
        void returnsErrorWhenDataIsNull() {
            Map<String, Object> result = service.id(null);

            assertThat(result).containsEntry("success", false);
        }

        @Test
        @DisplayName("should return error when data parameter is empty")
        void returnsErrorWhenDataIsEmpty() {
            Map<String, Object> result = service.id("");

            assertThat(result).containsEntry("success", false);
        }

        @Test
        @DisplayName("should return not_found error when teacher does not exist")
        void returnsNotFoundWhenTeacherMissing() {
            when(teacherRepository.findByPinfl("99999999999999")).thenReturn(Optional.empty());

            Map<String, Object> result = service.id("99999999999999");

            assertThat(result).containsEntry("success", false);
        }
    }

    // =====================================================
    // getById(String id)
    // =====================================================

    @Nested
    @DisplayName("getById(id)")
    class GetByIdMethod {

        @Test
        @DisplayName("should return teacher data when valid UUID found")
        void returnsTeacherDataWhenFound() {
            Teacher teacher = createTeacher();
            when(teacherRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));

            Map<String, Object> result = service.getById(teacher.getId().toString());

            assertThat(result).containsEntry("success", true);
            assertThat(result).containsKey("data");
        }

        @Test
        @DisplayName("should return error when id is null")
        void returnsErrorWhenIdIsNull() {
            Map<String, Object> result = service.getById(null);

            assertThat(result).containsEntry("success", false);
        }

        @Test
        @DisplayName("should return error when id is invalid UUID format")
        void returnsErrorWhenIdIsInvalidUuid() {
            Map<String, Object> result = service.getById("not-a-uuid");

            assertThat(result).containsEntry("success", false);
        }

        @Test
        @DisplayName("should return not_found when teacher with UUID does not exist")
        void returnsNotFoundWhenTeacherMissing() {
            UUID randomId = UUID.randomUUID();
            when(teacherRepository.findById(randomId)).thenReturn(Optional.empty());

            Map<String, Object> result = service.getById(randomId.toString());

            assertThat(result).containsEntry("success", false);
        }
    }

    // =====================================================
    // get(String pinfl)
    // =====================================================

    @Nested
    @DisplayName("get(pinfl)")
    class GetByPinflMethod {

        @Test
        @DisplayName("should return teacher data when PINFL found")
        void returnsTeacherDataWhenPinflFound() {
            Teacher teacher = createTeacher();
            when(teacherRepository.findByPinfl("12345678901234")).thenReturn(Optional.of(teacher));

            Map<String, Object> result = service.get("12345678901234");

            assertThat(result).containsEntry("success", true);
            assertThat(result).containsKey("data");
        }

        @Test
        @DisplayName("should return error when PINFL is null")
        void returnsErrorWhenPinflIsNull() {
            Map<String, Object> result = service.get(null);

            assertThat(result).containsEntry("success", false);
        }

        @Test
        @DisplayName("should return not_found when PINFL does not match any teacher")
        void returnsNotFoundWhenPinflNotFound() {
            when(teacherRepository.findByPinfl("00000000000000")).thenReturn(Optional.empty());

            Map<String, Object> result = service.get("00000000000000");

            assertThat(result).containsEntry("success", false);
        }
    }

    // =====================================================
    // addJob(Map<String, Object> jobData)
    // =====================================================

    @Nested
    @DisplayName("addJob(jobData)")
    class AddJobMethod {

        @Test
        @DisplayName("should return success when teacher exists and job data is valid")
        void returnsSuccessWhenTeacherExists() {
            Teacher teacher = createTeacher();
            when(teacherRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));

            Map<String, Object> jobData = new HashMap<>();
            jobData.put("teacher_id", teacher.getId().toString());
            jobData.put("university", "00123");

            Map<String, Object> result = service.addJob(jobData);

            assertThat(result).containsEntry("success", true);
            assertThat(result).containsEntry("teacher_id", teacher.getId().toString());
        }

        @Test
        @DisplayName("should return error when jobData is null")
        void returnsErrorWhenJobDataIsNull() {
            Map<String, Object> result = service.addJob(null);

            assertThat(result).containsEntry("success", false);
        }

        @Test
        @DisplayName("should return error when jobData is empty map")
        void returnsErrorWhenJobDataIsEmpty() {
            Map<String, Object> result = service.addJob(new HashMap<>());

            assertThat(result).containsEntry("success", false);
        }

        @Test
        @DisplayName("should return error when teacher_id is missing from jobData")
        void returnsErrorWhenTeacherIdMissing() {
            Map<String, Object> jobData = new HashMap<>();
            jobData.put("university", "00123");

            Map<String, Object> result = service.addJob(jobData);

            assertThat(result).containsEntry("success", false);
        }

        @Test
        @DisplayName("should return error when teacher_id has invalid UUID format")
        void returnsErrorWhenTeacherIdInvalidUuid() {
            Map<String, Object> jobData = new HashMap<>();
            jobData.put("teacher_id", "bad-uuid");

            Map<String, Object> result = service.addJob(jobData);

            assertThat(result).containsEntry("success", false);
        }

        @Test
        @DisplayName("should return not_found when teacher does not exist")
        void returnsNotFoundWhenTeacherMissing() {
            UUID randomId = UUID.randomUUID();
            when(teacherRepository.findById(randomId)).thenReturn(Optional.empty());

            Map<String, Object> jobData = new HashMap<>();
            jobData.put("teacher_id", randomId.toString());

            Map<String, Object> result = service.addJob(jobData);

            assertThat(result).containsEntry("success", false);
        }
    }

    // =====================================================
    // Helper
    // =====================================================

    private Teacher createTeacher() {
        Teacher teacher = new Teacher();
        teacher.setId(UUID.randomUUID());
        teacher.setPinfl("12345678901234");
        teacher.setFirstname("Ali");
        teacher.setLastname("Valiyev");
        teacher.setFathername("Karimovich");
        teacher.setGender("11");
        teacher.setCitizenship("860");
        teacher.setUniversity("00123");
        return teacher;
    }
}
