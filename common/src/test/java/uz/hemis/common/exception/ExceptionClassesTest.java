package uz.hemis.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Exception Classes")
class ExceptionClassesTest {

    @Nested
    @DisplayName("ResourceNotFoundException")
    class ResourceNotFoundExceptionTest {

        @Test
        @DisplayName("constructor with simple message sets message")
        void constructor_simpleMessage_setsMessage() {
            ResourceNotFoundException ex = new ResourceNotFoundException("Student not found");

            assertThat(ex.getMessage()).isEqualTo("Student not found");
            assertThat(ex.getResourceName()).isNull();
            assertThat(ex.getFieldName()).isNull();
            assertThat(ex.getFieldValue()).isNull();
        }

        @Test
        @DisplayName("constructor with resource details formats message")
        void constructor_withResourceDetails_formatsMessage() {
            ResourceNotFoundException ex = new ResourceNotFoundException("Student", "id", 42L);

            assertThat(ex.getMessage()).contains("Student");
            assertThat(ex.getMessage()).contains("id");
            assertThat(ex.getMessage()).contains("42");
        }

        @Test
        @DisplayName("getResourceName returns value")
        void getResourceName_returnsValue() {
            ResourceNotFoundException ex = new ResourceNotFoundException("University", "code", "TATU");

            assertThat(ex.getResourceName()).isEqualTo("University");
        }

        @Test
        @DisplayName("getFieldName returns value")
        void getFieldName_returnsValue() {
            ResourceNotFoundException ex = new ResourceNotFoundException("Student", "pinfl", "12345678901234");

            assertThat(ex.getFieldName()).isEqualTo("pinfl");
        }

        @Test
        @DisplayName("getFieldValue returns value")
        void getFieldValue_returnsValue() {
            ResourceNotFoundException ex = new ResourceNotFoundException("Teacher", "id", 99L);

            assertThat(ex.getFieldValue()).isEqualTo(99L);
        }

        @Test
        @DisplayName("extends RuntimeException")
        void extendsRuntimeException() {
            ResourceNotFoundException ex = new ResourceNotFoundException("not found");

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("ValidationException")
    class ValidationExceptionTest {

        @Test
        @DisplayName("constructor with simple message creates empty errors")
        void constructor_simpleMessage_emptyErrors() {
            ValidationException ex = new ValidationException("Validation failed");

            assertThat(ex.getMessage()).isEqualTo("Validation failed");
            assertThat(ex.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("constructor with error map stores errors")
        void constructor_withErrorMap_storesErrors() {
            Map<String, String> errors = new HashMap<>();
            errors.put("pinfl", "Duplicate PINFL");
            errors.put("email", "Invalid email format");

            ValidationException ex = new ValidationException("Validation failed", errors);

            assertThat(ex.getErrors()).hasSize(2);
            assertThat(ex.getErrors()).containsEntry("pinfl", "Duplicate PINFL");
            assertThat(ex.getErrors()).containsEntry("email", "Invalid email format");
        }

        @Test
        @DisplayName("constructor with single field error creates map")
        void constructor_singleFieldError_createsMap() {
            ValidationException ex = new ValidationException("Validation failed", "status", "Invalid status transition");

            assertThat(ex.getErrors()).hasSize(1);
            assertThat(ex.getErrors()).containsEntry("status", "Invalid status transition");
        }

        @Test
        @DisplayName("getErrors returns map")
        void getErrors_returnsMap() {
            Map<String, String> errors = new HashMap<>();
            errors.put("name", "Name is required");

            ValidationException ex = new ValidationException("Validation failed", errors);

            Map<String, String> result = ex.getErrors();
            assertThat(result).isNotNull();
            assertThat(result).containsEntry("name", "Name is required");
        }

        @Test
        @DisplayName("hasErrors with errors returns true")
        void hasErrors_withErrors_returnsTrue() {
            ValidationException ex = new ValidationException("Validation failed", "field", "error");

            assertThat(ex.hasErrors()).isTrue();
        }

        @Test
        @DisplayName("hasErrors with no errors returns false")
        void hasErrors_noErrors_returnsFalse() {
            ValidationException ex = new ValidationException("Validation failed");

            assertThat(ex.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("constructor with null map defaults to empty map")
        void constructor_nullMap_defaultsToEmptyMap() {
            ValidationException ex = new ValidationException("Validation failed", (Map<String, String>) null);

            assertThat(ex.getErrors()).isNotNull();
            assertThat(ex.getErrors()).isEmpty();
            assertThat(ex.hasErrors()).isFalse();
        }
    }

    @Nested
    @DisplayName("BusinessRuleException (HTTP 422)")
    class BusinessRuleExceptionTest {

        @Test
        @DisplayName("constructor with ruleCode + message stores both")
        void constructor_withRuleCodeAndMessage() {
            BusinessRuleException ex = new BusinessRuleException(
                "OTM_CLOSED",
                "Yopilgan OTM'ga yangi talaba kiritib bo'lmaydi: 337"
            );

            assertThat(ex.getRuleCode()).isEqualTo("OTM_CLOSED");
            assertThat(ex.getMessage()).contains("Yopilgan OTM");
            assertThat(ex.getMessage()).contains("337");
        }

        @Test
        @DisplayName("constructor with cause preserves cause chain")
        void constructor_withCause_preservesCause() {
            IllegalStateException root = new IllegalStateException("DB lock contention");
            BusinessRuleException ex = new BusinessRuleException(
                "ENROLLMENT_WINDOW_EXPIRED",
                "Registratsiya muddati o'tgan",
                root
            );

            assertThat(ex.getRuleCode()).isEqualTo("ENROLLMENT_WINDOW_EXPIRED");
            assertThat(ex.getCause()).isSameAs(root);
        }

        @Test
        @DisplayName("is a RuntimeException (unchecked — service layer free to throw)")
        void isRuntimeException() {
            BusinessRuleException ex = new BusinessRuleException("X", "y");
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("getRuleCode returns null when not provided via cause-only ctor — not applicable (ruleCode always required)")
        void ruleCode_alwaysRequired() {
            // Constructor signature requires ruleCode → compile-time enforced
            BusinessRuleException ex = new BusinessRuleException("GRADE_FINALIZED", "Cannot update");
            assertThat(ex.getRuleCode()).isNotNull().isNotEmpty();
        }
    }
}
