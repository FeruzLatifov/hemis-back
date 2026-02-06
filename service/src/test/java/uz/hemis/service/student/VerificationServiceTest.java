package uz.hemis.service.student;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import uz.hemis.domain.entity.Verification;
import uz.hemis.domain.entity.VerificationType;
import uz.hemis.domain.repository.VerificationRepository;
import uz.hemis.domain.repository.VerificationTypeRepository;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link VerificationService}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>verifyByPinfl - found records, empty results, error handling</li>
 *   <li>Response format validation (OLD-HEMIS compatible)</li>
 *   <li>Nested reference loading (VerificationType, University, etc.)</li>
 * </ul>
 *
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VerificationService Unit Tests")
class VerificationServiceTest {

    @Mock
    private VerificationRepository verificationRepository;

    @Mock
    private VerificationTypeRepository verificationTypeRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private VerificationService verificationService;

    private Verification sampleVerification;

    @BeforeEach
    void setUp() {
        sampleVerification = new Verification();
        sampleVerification.setId(UUID.randomUUID());
        sampleVerification.setPinfl("12345678901234");
        sampleVerification.setPoints("85.5");
        sampleVerification.setVersion(1);
        sampleVerification.setUniversity("UNI_001");
        sampleVerification.setEducationYear("2024");
        sampleVerification.setEducationType("11");
        sampleVerification.setPaymentForm("12");
        sampleVerification.setCategory("CAT_01");
    }

    // =====================================================
    // verifyByPinfl tests
    // =====================================================

    @Nested
    @DisplayName("verifyByPinfl")
    class VerifyByPinfl {

        @Test
        @DisplayName("returns success with records when verifications found")
        void returnsSuccessWithRecordsWhenFound() {
            // Given
            String pinfl = "12345678901234";

            // Mock reference loading
            Map<String, Object> refRow = new LinkedHashMap<>();
            refRow.put("code", "12");
            refRow.put("name", "Test");
            refRow.put("name_ru", "Тест");
            refRow.put("name_en", "Test");
            refRow.put("active", true);
            refRow.put("version", 1);

            when(verificationRepository.findByPinfl(pinfl)).thenReturn(List.of(sampleVerification));
            when(jdbcTemplate.queryForMap(anyString(), eq("12"))).thenReturn(refRow);
            when(jdbcTemplate.queryForMap(anyString(), eq("11"))).thenReturn(refRow);

            Map<String, Object> uniRow = new LinkedHashMap<>(refRow);
            uniRow.put("student_url", "https://student.test.uz");
            uniRow.put("teacher_url", "https://teacher.test.uz");
            uniRow.put("tin", "123456789");
            uniRow.put("address", "Tashkent");
            uniRow.put("add_student", true);
            uniRow.put("allow_grouping", true);
            uniRow.put("allow_transfer_outside", false);
            uniRow.put("accreditation_edit", false);
            uniRow.put("gpa_edit", false);
            when(jdbcTemplate.queryForMap(anyString(), eq("UNI_001"))).thenReturn(uniRow);
            when(jdbcTemplate.queryForMap(anyString(), eq("2024"))).thenReturn(refRow);

            VerificationType vt = new VerificationType();
            vt.setCode("CAT_01");
            vt.setName("Test Category");
            vt.setNameRu("Тест Категория");
            vt.setNameEn("Test Category");
            vt.setVersion(1);
            when(verificationTypeRepository.findById("CAT_01")).thenReturn(Optional.of(vt));

            // When
            Map<String, Object> result = verificationService.verifyByPinfl(pinfl);

            // Then
            assertThat(result).containsEntry("success", true);
            assertThat(result).containsEntry("count", 1);
            assertThat(result).containsKey("records");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> records = (List<Map<String, Object>>) result.get("records");
            assertThat(records).hasSize(1);

            Map<String, Object> record = records.get(0);
            assertThat(record).containsEntry("_entityName", "hemishe_EVerification");
            assertThat(record).containsEntry("pinfl", "12345678901234");
            assertThat(record).containsEntry("points", "85.5");
        }

        @Test
        @DisplayName("returns success with empty records when no verifications found")
        void returnsEmptyRecordsWhenNotFound() {
            // Given
            String pinfl = "99999999999999";
            when(verificationRepository.findByPinfl(pinfl)).thenReturn(Collections.emptyList());

            // When
            Map<String, Object> result = verificationService.verifyByPinfl(pinfl);

            // Then
            assertThat(result).containsEntry("success", true);
            assertThat(result).containsEntry("count", 0);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> records = (List<Map<String, Object>>) result.get("records");
            assertThat(records).isEmpty();

            verify(verificationRepository).findByPinfl(pinfl);
        }

        @Test
        @DisplayName("returns error response when exception occurs")
        void returnsErrorResponseOnException() {
            // Given
            String pinfl = "12345678901234";
            when(verificationRepository.findByPinfl(pinfl))
                    .thenThrow(new RuntimeException("Database connection lost"));

            // When
            Map<String, Object> result = verificationService.verifyByPinfl(pinfl);

            // Then
            assertThat(result).containsEntry("success", false);
            assertThat(result).containsKey("error");
            assertThat((String) result.get("error")).contains("Database connection lost");
        }

        @Test
        @DisplayName("handles null reference codes gracefully")
        void handlesNullReferenceCodes() {
            // Given
            Verification verificationWithNulls = new Verification();
            verificationWithNulls.setId(UUID.randomUUID());
            verificationWithNulls.setPinfl("11111111111111");
            verificationWithNulls.setPoints("70.0");
            verificationWithNulls.setVersion(1);
            verificationWithNulls.setUniversity(null);
            verificationWithNulls.setEducationYear(null);
            verificationWithNulls.setEducationType(null);
            verificationWithNulls.setPaymentForm(null);
            verificationWithNulls.setCategory(null);

            when(verificationRepository.findByPinfl("11111111111111"))
                    .thenReturn(List.of(verificationWithNulls));

            // When
            Map<String, Object> result = verificationService.verifyByPinfl("11111111111111");

            // Then
            assertThat(result).containsEntry("success", true);
            assertThat(result).containsEntry("count", 1);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> records = (List<Map<String, Object>>) result.get("records");
            Map<String, Object> record = records.get(0);
            assertThat(record.get("paymentForm")).isNull();
            assertThat(record.get("university")).isNull();
            assertThat(record.get("educationType")).isNull();
            assertThat(record.get("educationYear")).isNull();
            assertThat(record.get("category")).isNull();

            // Verify no JDBC calls were made for null reference codes
            verifyNoInteractions(jdbcTemplate);
        }

        @Test
        @DisplayName("returns multiple records for same PINFL")
        void returnsMultipleRecordsForSamePinfl() {
            // Given
            String pinfl = "12345678901234";

            Verification v1 = new Verification();
            v1.setId(UUID.randomUUID());
            v1.setPinfl(pinfl);
            v1.setPoints("85.5");
            v1.setVersion(1);

            Verification v2 = new Verification();
            v2.setId(UUID.randomUUID());
            v2.setPinfl(pinfl);
            v2.setPoints("90.0");
            v2.setVersion(1);

            when(verificationRepository.findByPinfl(pinfl)).thenReturn(List.of(v1, v2));

            // When
            Map<String, Object> result = verificationService.verifyByPinfl(pinfl);

            // Then
            assertThat(result).containsEntry("success", true);
            assertThat(result).containsEntry("count", 2);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> records = (List<Map<String, Object>>) result.get("records");
            assertThat(records).hasSize(2);
        }

        @Test
        @DisplayName("loads verification type via JPA repository")
        void loadsVerificationTypeViaRepository() {
            // Given
            Verification v = new Verification();
            v.setId(UUID.randomUUID());
            v.setPinfl("55555555555555");
            v.setPoints("60.0");
            v.setVersion(1);
            v.setCategory("VT_01");

            VerificationType vt = new VerificationType();
            vt.setCode("VT_01");
            vt.setName("Kirish imtihon");
            vt.setNameRu("Вступительный экзамен");
            vt.setNameEn("Entrance exam");
            vt.setVersion(1);

            when(verificationRepository.findByPinfl("55555555555555")).thenReturn(List.of(v));
            when(verificationTypeRepository.findById("VT_01")).thenReturn(Optional.of(vt));

            // When
            Map<String, Object> result = verificationService.verifyByPinfl("55555555555555");

            // Then
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> records = (List<Map<String, Object>>) result.get("records");
            @SuppressWarnings("unchecked")
            Map<String, Object> categoryMap = (Map<String, Object>) records.get(0).get("category");

            assertThat(categoryMap).isNotNull();
            assertThat(categoryMap).containsEntry("_entityName", "hemishe_HVerificationType");
            assertThat(categoryMap).containsEntry("code", "VT_01");
            assertThat(categoryMap).containsEntry("name", "Kirish imtihon");
            assertThat(categoryMap).containsEntry("nameRu", "Вступительный экзамен");
            assertThat(categoryMap).containsEntry("nameEn", "Entrance exam");
        }

        @Test
        @DisplayName("handles blank reference codes as null (not queried)")
        void handlesBlankReferenceCodes() {
            // Given
            Verification v = new Verification();
            v.setId(UUID.randomUUID());
            v.setPinfl("22222222222222");
            v.setPoints("45.0");
            v.setVersion(1);
            v.setUniversity("   ");
            v.setCategory("");

            when(verificationRepository.findByPinfl("22222222222222")).thenReturn(List.of(v));

            // When
            Map<String, Object> result = verificationService.verifyByPinfl("22222222222222");

            // Then
            assertThat(result).containsEntry("success", true);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> records = (List<Map<String, Object>>) result.get("records");
            Map<String, Object> record = records.get(0);

            assertThat(record.get("university")).isNull();
            assertThat(record.get("category")).isNull();

            verify(verificationTypeRepository, never()).findById(anyString());
        }
    }
}
