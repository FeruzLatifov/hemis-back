package uz.hemis.service.legacy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SoftDeleteRestoreLegacyService - soft-delete check and restore via native SQL")
class SoftDeleteRestoreLegacyServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private SoftDeleteRestoreLegacyService service;

    // =====================================================
    // hasSoftDeletedRecord
    // =====================================================

    @Nested
    @DisplayName("hasSoftDeletedRecord")
    class HasSoftDeletedRecord {

        @Test
        @DisplayName("should return true when a soft-deleted record exists")
        void returnsTrueWhenSoftDeletedRecordExists() {
            when(jdbcTemplate.queryForList(
                    contains("SELECT COUNT(*) AS cnt FROM hemishe_e_teacher"),
                    eq("TEST_CODE")
            )).thenReturn(List.of(Map.of("cnt", 1L)));

            boolean result = service.hasSoftDeletedRecord("hemishe_e_teacher", "TEST_CODE");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when no soft-deleted record exists")
        void returnsFalseWhenNoSoftDeletedRecordExists() {
            when(jdbcTemplate.queryForList(
                    contains("SELECT COUNT(*) AS cnt FROM hemishe_e_student"),
                    eq("SOME_CODE")
            )).thenReturn(List.of(Map.of("cnt", 0L)));

            boolean result = service.hasSoftDeletedRecord("hemishe_e_student", "SOME_CODE");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return false when query returns empty result")
        void returnsFalseWhenQueryReturnsEmptyList() {
            when(jdbcTemplate.queryForList(
                    contains("SELECT COUNT(*) AS cnt FROM hemishe_e_teacher"),
                    eq("MISSING")
            )).thenReturn(Collections.emptyList());

            boolean result = service.hasSoftDeletedRecord("hemishe_e_teacher", "MISSING");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return false and not throw when query throws exception")
        void returnsFalseOnException() {
            when(jdbcTemplate.queryForList(
                    anyString(),
                    anyString()
            )).thenThrow(new RuntimeException("DB connection failed"));

            boolean result = service.hasSoftDeletedRecord("hemishe_e_teacher", "ERR_CODE");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should throw IllegalArgumentException for invalid table name")
        void throwsForInvalidTableName() {
            assertThatThrownBy(() -> service.hasSoftDeletedRecord("DROP TABLE; --", "code"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid table name");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException for null table name")
        void throwsForNullTableName() {
            assertThatThrownBy(() -> service.hasSoftDeletedRecord(null, "code"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid table name");
        }
    }

    // =====================================================
    // restoreSoftDeletedRecord
    // =====================================================

    @Nested
    @DisplayName("restoreSoftDeletedRecord")
    class RestoreSoftDeletedRecord {

        @Test
        @DisplayName("should execute UPDATE statement to clear delete_ts and deleted_by")
        void executesUpdateStatementToRestore() {
            service.restoreSoftDeletedRecord("hemishe_e_teacher", "RESTORE_CODE");

            verify(jdbcTemplate).update(
                    contains("UPDATE hemishe_e_teacher SET delete_ts = NULL, deleted_by = NULL"),
                    eq("RESTORE_CODE")
            );
        }

        @Test
        @DisplayName("should throw IllegalArgumentException for invalid table name")
        void throwsForInvalidTableName() {
            assertThatThrownBy(() -> service.restoreSoftDeletedRecord("bad table!", "code"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid table name");

            verify(jdbcTemplate, never()).update(anyString(), anyString());
        }
    }
}
