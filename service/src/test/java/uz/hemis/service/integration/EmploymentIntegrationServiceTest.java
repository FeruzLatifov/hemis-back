package uz.hemis.service.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.hemis.common.dto.student.GraduateListRequest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmploymentIntegrationService - Mehnat vazirligi integration")
class EmploymentIntegrationServiceTest {

    @InjectMocks
    private EmploymentIntegrationService service;

    // =====================================================
    // getWorkbook
    // =====================================================

    @Nested
    @DisplayName("getWorkbook(pinfl)")
    class GetWorkbook {

        @Test
        @DisplayName("should return response with _entityName hemishe_Workbook")
        void returnsCorrectEntityName() {
            Map<String, Object> result = service.getWorkbook("12345678901234");

            assertThat(result).containsEntry("_entityName", "hemishe_Workbook");
        }

        @Test
        @DisplayName("should return result code 4 indicating data not found")
        void returnsResultCode4ForNotFound() {
            Map<String, Object> result = service.getWorkbook("12345678901234");

            assertThat(result).containsEntry("result", "4");
        }

        @Test
        @DisplayName("should return comments in Russian matching old-hemis format")
        void returnsRussianCommentsForNotFound() {
            Map<String, Object> result = service.getWorkbook("12345678901234");

            assertThat(result).containsEntry("comments", "\u0414\u0430\u043d\u043d\u044b\u0435 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u044b");
        }

        @Test
        @DisplayName("should return empty id field matching old-hemis format")
        void returnsEmptyIdField() {
            Map<String, Object> result = service.getWorkbook("12345678901234");

            assertThat(result).containsEntry("id", "");
        }

        @Test
        @DisplayName("should not contain data field when result is 4")
        void doesNotContainDataFieldWhenNotFound() {
            Map<String, Object> result = service.getWorkbook("12345678901234");

            assertThat(result).doesNotContainKey("data");
        }

        @Test
        @DisplayName("should return consistent response structure for any pinfl input")
        void returnsConsistentStructureForAnyPinfl() {
            Map<String, Object> result = service.getWorkbook("00000000000000");

            assertThat(result)
                    .containsKey("_entityName")
                    .containsKey("id")
                    .containsKey("result")
                    .containsKey("comments");
        }
    }

    // =====================================================
    // submitGraduateList
    // =====================================================

    @Nested
    @DisplayName("submitGraduateList(request)")
    class SubmitGraduateList {

        @Test
        @DisplayName("should return success true on valid request")
        void returnsSuccessOnValidRequest() {
            GraduateListRequest request = new GraduateListRequest();
            request.setYear(2024);
            request.setUniversityCode("00123");
            request.setGraduates(List.of());

            Map<String, Object> result = service.submitGraduateList(request);

            assertThat(result).containsEntry("success", true);
        }

        @Test
        @DisplayName("should return a submissionId UUID")
        void returnsSubmissionId() {
            GraduateListRequest request = new GraduateListRequest();
            request.setYear(2024);
            request.setUniversityCode("00123");
            request.setGraduates(List.of());

            Map<String, Object> result = service.submitGraduateList(request);

            assertThat(result).containsKey("submissionId");
            assertThat(result.get("submissionId")).isNotNull();
        }
    }
}
