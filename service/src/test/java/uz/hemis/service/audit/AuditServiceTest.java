package uz.hemis.service.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import uz.hemis.common.dto.PageResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AuditService} unit testlar — JdbcTemplate filter SQL building.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuditService — replica read")
class AuditServiceTest {

    @Mock private JdbcTemplate jdbcTemplate;

    private AuditService service;

    @BeforeEach
    void setUp() {
        service = new AuditService(jdbcTemplate);
        // count(*) default 0; queryForList default empty.
        lenient().when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(Object[].class)))
                .thenReturn(0L);
        lenient().when(jdbcTemplate.queryForList(anyString(), any(Object[].class)))
                .thenReturn(List.of());
    }

    @Nested
    @DisplayName("getActivities()")
    class Activities {

        @Test
        @DisplayName("filters yo'q — barchasi tanlanadi (WHERE 1=1)")
        void noFilters_baseWhere() {
            PageResponse<Map<String, Object>> result = service.getActivities(new HashMap<>(), 0, 20);

            assertThat(result).isNotNull();
            // SQL'da WHERE 1=1 dan boshqa filter yo'q
            ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
            verify(jdbcTemplate).queryForList(sql.capture(), any(Object[].class));
            assertThat(sql.getValue()).contains("WHERE 1=1");
            assertThat(sql.getValue()).contains("activity_log");
        }

        @Test
        @DisplayName("action filter — SQL WHERE'ga qo'shiladi")
        void actionFilter_addedToWhere() {
            Map<String, String> filters = new HashMap<>();
            filters.put("action", "CREATE");

            service.getActivities(filters, 0, 20);

            ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
            verify(jdbcTemplate).queryForList(sql.capture(), any(Object[].class));
            assertThat(sql.getValue()).contains("action = ?");
        }

        @Test
        @DisplayName("search filter — LIKE entity_name OR description")
        void searchFilter_likeOnTwoColumns() {
            Map<String, String> filters = new HashMap<>();
            filters.put("search", "student-1");

            service.getActivities(filters, 0, 20);

            ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
            verify(jdbcTemplate).queryForList(sql.capture(), any(Object[].class));
            assertThat(sql.getValue()).containsIgnoringCase("LIKE");
        }
    }

    @Nested
    @DisplayName("getEntityHistory()")
    class EntityHistory {

        @Test
        @DisplayName("entity_type + entity_id filter — required params")
        void entityHistory_filtersApplied() {
            service.getEntityHistory("Student", "id-123", 0, 20);

            ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
            verify(jdbcTemplate).queryForList(sql.capture(), any(Object[].class));
            assertThat(sql.getValue()).contains("entity_type = ?")
                    .contains("entity_id = ?");
        }
    }

    @Nested
    @DisplayName("getErrors()")
    class Errors {

        @Test
        @DisplayName("error_log table'dan o'qiydi")
        void errors_correctTable() {
            service.getErrors(new HashMap<>(), 0, 20);

            ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
            verify(jdbcTemplate).queryForList(sql.capture(), any(Object[].class));
            assertThat(sql.getValue()).contains("error_log");
        }

        @Test
        @DisplayName("errorType filter — LIKE")
        void errorTypeFilter() {
            Map<String, String> filters = new HashMap<>();
            filters.put("errorType", "NullPointer");

            service.getErrors(filters, 0, 20);

            ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
            verify(jdbcTemplate).queryForList(sql.capture(), any(Object[].class));
            assertThat(sql.getValue()).containsIgnoringCase("LIKE");
        }
    }

    @Nested
    @DisplayName("getLogins()")
    class Logins {

        @Test
        @DisplayName("login_log table'dan o'qiydi")
        void logins_correctTable() {
            service.getLogins(new HashMap<>(), 0, 20);

            ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
            verify(jdbcTemplate).queryForList(sql.capture(), any(Object[].class));
            assertThat(sql.getValue()).contains("login_log");
        }

        @Test
        @DisplayName("eventType filter (LOGIN_SUCCESS, LOGOUT, etc.)")
        void eventTypeFilter() {
            Map<String, String> filters = new HashMap<>();
            filters.put("eventType", "LOGIN_SUCCESS");

            service.getLogins(filters, 0, 20);

            ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
            verify(jdbcTemplate).queryForList(sql.capture(), any(Object[].class));
            assertThat(sql.getValue()).contains("event_type = ?");
        }
    }

    @Nested
    @DisplayName("Detail lookups")
    class Details {

        @Test
        @DisplayName("getActivityDetail — id bo'yicha lookup")
        void activityDetail() {
            when(jdbcTemplate.queryForList(contains("activity_log"), any(Object[].class)))
                    .thenReturn(List.of(Map.of("id", "abc-123", "action", "CREATE")));

            Map<String, Object> result = service.getActivityDetail("abc-123");

            assertThat(result).containsEntry("action", "CREATE");
        }

        @Test
        @DisplayName("getActivityDetail — not found returns null (service contract)")
        void activityDetail_notFound() {
            // setUp default: queryForList returns empty list. queryById returns null if empty.
            Map<String, Object> result = service.getActivityDetail("nonexistent");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("getErrorDetail — column names → camelCase (errorType, NOT error_type)")
        void errorDetail() {
            // Service queryById applies toCamelCaseKeys after fetch.
            when(jdbcTemplate.queryForList(contains("error_log"), any(Object[].class)))
                    .thenReturn(List.of(new java.util.LinkedHashMap<>(
                            Map.of("id", "err-1", "error_type", "NullPointer"))));

            Map<String, Object> result = service.getErrorDetail("err-1");

            // Service transforms error_type → errorType
            assertThat(result).containsEntry("errorType", "NullPointer");
        }
    }
}
