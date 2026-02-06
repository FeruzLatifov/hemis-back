package uz.hemis.common.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ResponseWrapper")
class ResponseWrapperTest {

    // =====================================================
    // Success Factory Methods
    // =====================================================

    @Nested
    @DisplayName("success factory methods")
    class SuccessFactoryMethods {

        @Test
        @DisplayName("success(data) sets success=true, data is set, error is null")
        void success_withData_setsFieldsCorrectly() {
            List<Integer> payload = List.of(10, 20, 30);

            ResponseWrapper<List<Integer>> result = ResponseWrapper.success(payload);

            assertThat(result.getSuccess()).isTrue();
            assertThat(result.getData()).containsExactly(10, 20, 30);
            assertThat(result.getMessage()).isNull();
            assertThat(result.getError()).isNull();
        }

        @Test
        @DisplayName("success(data, message) sets success, data, and message")
        void success_withDataAndMessage_setsAllFields() {
            List<Integer> payload = List.of(1, 2, 3);
            String message = "Items fetched";

            ResponseWrapper<List<Integer>> result = ResponseWrapper.success(payload, message);

            assertThat(result.getSuccess()).isTrue();
            assertThat(result.getData()).isEqualTo(payload);
            assertThat(result.getMessage()).isEqualTo("Items fetched");
            assertThat(result.getError()).isNull();
        }

        @Test
        @DisplayName("success(message) sets message and null data")
        void success_messageOnly_setsMessageAndNullData() {
            ResponseWrapper<Object> result = ResponseWrapper.success("Operation completed");

            assertThat(result.getSuccess()).isTrue();
            assertThat(result.getMessage()).isEqualTo("Operation completed");
            assertThat(result.getData()).isNull();
            assertThat(result.getError()).isNull();
        }
    }

    // =====================================================
    // Error Factory Methods
    // =====================================================

    @Nested
    @DisplayName("error factory methods")
    class ErrorFactoryMethods {

        @Test
        @DisplayName("error(message) sets success=false, message, data is null")
        void error_withMessage_setsFieldsCorrectly() {
            ResponseWrapper<Object> result = ResponseWrapper.error("Something went wrong");

            assertThat(result.getSuccess()).isFalse();
            assertThat(result.getMessage()).isEqualTo("Something went wrong");
            assertThat(result.getData()).isNull();
            assertThat(result.getError()).isNull();
        }

        @Test
        @DisplayName("error(ErrorResponse) sets error object, success=false")
        void error_withErrorResponse_setsErrorObject() {
            ErrorResponse errorResponse = ErrorResponse.of(404, "Not Found", "Resource not found");

            ResponseWrapper<Object> result = ResponseWrapper.error(errorResponse);

            assertThat(result.getSuccess()).isFalse();
            assertThat(result.getError()).isNotNull();
            assertThat(result.getError().getStatus()).isEqualTo(404);
            assertThat(result.getError().getError()).isEqualTo("Not Found");
            assertThat(result.getError().getMessage()).isEqualTo("Resource not found");
            assertThat(result.getData()).isNull();
            assertThat(result.getMessage()).isNull();
        }

        @Test
        @DisplayName("error(message, ErrorResponse) sets both message and error")
        void error_withMessageAndErrorResponse_setsBoth() {
            ErrorResponse errorResponse = ErrorResponse.of(
                    500, "Internal Server Error", "Unexpected failure"
            );

            ResponseWrapper<Object> result = ResponseWrapper.error("Server error occurred", errorResponse);

            assertThat(result.getSuccess()).isFalse();
            assertThat(result.getMessage()).isEqualTo("Server error occurred");
            assertThat(result.getError()).isNotNull();
            assertThat(result.getError().getStatus()).isEqualTo(500);
            assertThat(result.getError().getError()).isEqualTo("Internal Server Error");
            assertThat(result.getError().getMessage()).isEqualTo("Unexpected failure");
            assertThat(result.getData()).isNull();
        }
    }
}
