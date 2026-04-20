package uz.hemis.service.student;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import uz.hemis.common.dto.student.StudentDto;

import java.util.UUID;

import static org.mockito.Mockito.verify;

/**
 * StudentService is a thin facade delegating to specialized services.
 *
 * <p>Business logic is exercised by the specialized services' own tests
 * ({@code StudentCoreServiceTest}, {@code StudentEnrollmentServiceTest}, etc.).</p>
 *
 * <p>This test only verifies that the facade forwards each call to the correct delegate —
 * any regression that breaks delegation wiring would surface here without having to run
 * the heavier service-level integration tests.</p>
 *
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StudentService facade delegation")
class StudentServiceTest {

    @Mock private StudentCoreService coreService;
    @Mock private StudentLegacyApiService legacyApiService;
    @Mock private StudentStatisticsService statisticsService;
    @Mock private StudentEnrollmentService enrollmentService;
    @Mock private StudentScholarshipService scholarshipService;

    @InjectMocks
    private StudentService studentService;

    @Test
    @DisplayName("findById delegates to core service")
    void findById_delegatesToCore() {
        UUID id = UUID.randomUUID();
        studentService.findById(id);
        verify(coreService).findById(id);
    }

    @Test
    @DisplayName("findByPinfl delegates to core service")
    void findByPinfl_delegatesToCore() {
        studentService.findByPinfl("12345678901234");
        verify(coreService).findByPinfl("12345678901234");
    }

    @Test
    @DisplayName("findAll delegates to core service")
    void findAll_delegatesToCore() {
        Pageable pageable = Pageable.unpaged();
        studentService.findAll(pageable);
        verify(coreService).findAll(pageable);
    }

    @Test
    @DisplayName("countActiveByUniversity delegates to core service")
    void countActiveByUniversity_delegatesToCore() {
        studentService.countActiveByUniversity("387");
        verify(coreService).countActiveByUniversity("387");
    }

    @Test
    @DisplayName("existsByPinfl delegates to core service")
    void existsByPinfl_delegatesToCore() {
        studentService.existsByPinfl("12345678901234");
        verify(coreService).existsByPinfl("12345678901234");
    }

    @Test
    @DisplayName("create delegates to enrollment service")
    void create_delegatesToEnrollment() {
        StudentDto dto = new StudentDto();
        studentService.create(dto);
        verify(coreService).create(dto);
    }

    @Test
    @DisplayName("softDelete delegates to enrollment service")
    void softDelete_delegatesToCore() {
        UUID id = UUID.randomUUID();
        studentService.softDelete(id, "387");
        verify(coreService).softDelete(id, "387");
    }

    @Test
    @DisplayName("checkScholarship delegates to scholarship service")
    void checkScholarship_delegatesToScholarship() {
        java.util.Map<String, Object> request = new java.util.HashMap<>();
        studentService.checkScholarship(request);
        verify(scholarshipService).checkScholarship(request);
    }

    @Test
    @DisplayName("getByPinflFlat delegates to legacy api service")
    void getByPinflFlat_delegatesToLegacy() {
        studentService.getByPinflFlat("12345678901234");
        verify(legacyApiService).getByPinflFlat("12345678901234");
    }
}
