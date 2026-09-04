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
        @DisplayName("old_value/new_value JSONB — obyekt bo'lib qaytadi, matn emas")
        void jsonColumnsAreParsed() {
            // The driver hands JSONB over as a wrapper whose toString() is the JSON text. Left alone,
            // the client received {"type":"jsonb","value":"..."} and every field read came back empty —
            // which is how the history dialog showed "—" for a code that really did change.
            when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(List.of(
                    new java.util.LinkedHashMap<>(Map.of(
                            "id", "1",
                            "old_value", "{\"code\": \"shifrsiz\"}",
                            "new_value", "{\"code\": \"shifrsiz1\"}"))));

            PageResponse<Map<String, Object>> result = service.getActivities(new HashMap<>(), 0, 20);

            Map<String, Object> row = result.getContent().getFirst();
            assertThat(row.get("oldValue")).isInstanceOf(Map.class);
            assertThat(((Map<?, ?>) row.get("oldValue")).get("code")).isEqualTo("shifrsiz");
            assertThat(((Map<?, ?>) row.get("newValue")).get("code")).isEqualTo("shifrsiz1");
        }

        @Test
        @DisplayName("scopeKey — egasi (OTM) bo'yicha tarix: tenglik, LIKE emas")
        void scopeKeyFilter_isIndexedEquality() {
            // The question a hard-deleted link row cannot answer: "everything that happened to OTM
            // 301's attachments". Equality on the indexed (entity_type, scope_key, created_at) triple
            // — a LIKE over a label would grow more expensive with every audit row ever written.
            Map<String, String> filters = new HashMap<>();
            filters.put("entityType", "UniversitySpecialityAttachment");
            filters.put("scopeKey", "301");

            service.getActivities(filters, 0, 20);

            ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Object[]> args = ArgumentCaptor.forClass(Object[].class);
            verify(jdbcTemplate).queryForList(sql.capture(), args.capture());
            assertThat(sql.getValue()).contains("AND scope_key = ?");
            assertThat(sql.getValue()).doesNotContain("scope_key LIKE");
            assertThat(args.getValue()).startsWith("UniversitySpecialityAttachment", "301");
        }

        @Test
        @DisplayName("entityId — bitta yozuv tarixi (mutaxassislik id'si) bo'yicha filtr")
        void entityIdFilter_narrowsToOneRow() {
            // The list is what an admin actually opens; without this filter the only way to see one
            // speciality's history was to hand-craft the /entities/... URL.
            Map<String, String> filters = new HashMap<>();
            filters.put("entityId", "af5d516a-baf6-4962-9fa3-499fcfdab67f");

            service.getActivities(filters, 0, 20);

            ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Object[]> args = ArgumentCaptor.forClass(Object[].class);
            verify(jdbcTemplate).queryForList(sql.capture(), args.capture());
            assertThat(sql.getValue()).contains("AND entity_id = ?");
            assertThat(args.getValue()).contains("af5d516a-baf6-4962-9fa3-499fcfdab67f");
        }

        @Test
        @DisplayName("entityType ro'yxati — IN (?, ?) bo'lib bog'lanadi, qiymatlar parametr sifatida")
        void entityTypeList_becomesInClause() {
            // A UI group spans several entity classes: "Classifiers" is HSpeciality + ClassifierItem.
            Map<String, String> filters = new HashMap<>();
            filters.put("entityType", "HSpeciality, ClassifierItem");

            service.getActivities(filters, 0, 20);

            ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Object[]> args = ArgumentCaptor.forClass(Object[].class);
            verify(jdbcTemplate).queryForList(sql.capture(), args.capture());
            assertThat(sql.getValue()).contains("AND entity_type IN (?, ?)");
            // The trailing limit/offset that queryPage appends are not part of the filter contract.
            assertThat(args.getValue()).startsWith("HSpeciality", "ClassifierItem");
        }

        @Test
        @DisplayName("bitta entityType — IN emas, oddiy tenglik")
        void singleEntityType_staysEquality() {
            Map<String, String> filters = new HashMap<>();
            filters.put("entityType", "HSpeciality");

            service.getActivities(filters, 0, 20);

            ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
            verify(jdbcTemplate).queryForList(sql.capture(), any(Object[].class));
            assertThat(sql.getValue()).contains("AND entity_type = ?");
            assertThat(sql.getValue()).doesNotContain("entity_type IN");
        }

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
