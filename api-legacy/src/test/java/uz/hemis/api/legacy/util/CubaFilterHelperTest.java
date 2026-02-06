package uz.hemis.api.legacy.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CubaFilterHelper Tests")
class CubaFilterHelperTest {

    private CubaFilterHelper filterHelper;

    @BeforeEach
    void setUp() {
        filterHelper = new CubaFilterHelper(new ObjectMapper());
    }

    // Simple test entity
    record TestEntity(String name, String code, int age) {
        public String getName() { return name; }
        public String getCode() { return code; }
        public int getAge() { return age; }
    }

    private Object getProperty(CubaFilterHelper.PropertyRequest<TestEntity> req) {
        return filterHelper.getPropertyByReflection(req.entity(), req.property());
    }

    // =============================================
    // Filter Operators
    // =============================================

    @Nested
    @DisplayName("Filter operators")
    class FilterOperators {

        private final List<TestEntity> entities = List.of(
                new TestEntity("Islom", "S001", 22),
                new TestEntity("Madina", "S002", 20),
                new TestEntity("Sardor", "S003", 25),
                new TestEntity("Islomjon", "S004", 19)
        );

        @Test
        @DisplayName("= operator - aniq moslik")
        void equalOperator() {
            String filter = """
                {"conditions":[{"property":"name","operator":"=","value":"Islom"}]}
                """;
            List<TestEntity> result = filterHelper.applyFilter(entities, filter,
                    req -> getProperty(req));
            assertThat(result).hasSize(1);
            assertThat(result.get(0).code()).isEqualTo("S001");
        }

        @Test
        @DisplayName("<> operator - teng emas")
        void notEqualOperator() {
            String filter = """
                {"conditions":[{"property":"name","operator":"<>","value":"Islom"}]}
                """;
            List<TestEntity> result = filterHelper.applyFilter(entities, filter,
                    req -> getProperty(req));
            assertThat(result).hasSize(3);
            assertThat(result).noneMatch(e -> e.name().equals("Islom"));
        }

        @Test
        @DisplayName("contains operator - qism qidirish")
        void containsOperator() {
            String filter = """
                {"conditions":[{"property":"name","operator":"contains","value":"islom"}]}
                """;
            List<TestEntity> result = filterHelper.applyFilter(entities, filter,
                    req -> getProperty(req));
            assertThat(result).hasSize(2);
            assertThat(result).extracting("code").containsExactly("S001", "S004");
        }

        @Test
        @DisplayName("startsWith operator")
        void startsWithOperator() {
            String filter = """
                {"conditions":[{"property":"code","operator":"startsWith","value":"S00"}]}
                """;
            List<TestEntity> result = filterHelper.applyFilter(entities, filter,
                    req -> getProperty(req));
            assertThat(result).hasSize(4);
        }

        @Test
        @DisplayName("> operator - raqamli solishtirish")
        void greaterThanOperator() {
            String filter = """
                {"conditions":[{"property":"age","operator":">","value":"21"}]}
                """;
            List<TestEntity> result = filterHelper.applyFilter(entities, filter,
                    req -> getProperty(req));
            assertThat(result).hasSize(2);
            assertThat(result).extracting("name").containsExactly("Islom", "Sardor");
        }

        @Test
        @DisplayName("isNull operator")
        void isNullOperator() {
            List<TestEntity> withNull = new ArrayList<>(entities);
            withNull.add(new TestEntity(null, "S005", 18));

            String filter = """
                {"conditions":[{"property":"name","operator":"isNull"}]}
                """;
            List<TestEntity> result = filterHelper.applyFilter(withNull, filter,
                    req -> getProperty(req));
            assertThat(result).hasSize(1);
            assertThat(result.get(0).code()).isEqualTo("S005");
        }

        @Test
        @DisplayName("notEmpty operator")
        void notEmptyOperator() {
            List<TestEntity> withNull = new ArrayList<>(entities);
            withNull.add(new TestEntity(null, "S005", 18));

            String filter = """
                {"conditions":[{"property":"name","operator":"notEmpty"}]}
                """;
            List<TestEntity> result = filterHelper.applyFilter(withNull, filter,
                    req -> getProperty(req));
            assertThat(result).hasSize(4);
        }

        @Test
        @DisplayName("Bir nechta condition - AND logikasi")
        void multipleConditions() {
            String filter = """
                {"conditions":[
                    {"property":"name","operator":"contains","value":"islom"},
                    {"property":"age","operator":">","value":"20"}
                ]}
                """;
            List<TestEntity> result = filterHelper.applyFilter(entities, filter,
                    req -> getProperty(req));
            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("Islom");
        }
    }

    // =============================================
    // Pagination
    // =============================================

    @Nested
    @DisplayName("Pagination")
    class Pagination {

        private final List<TestEntity> entities = List.of(
                new TestEntity("A", "1", 1),
                new TestEntity("B", "2", 2),
                new TestEntity("C", "3", 3),
                new TestEntity("D", "4", 4),
                new TestEntity("E", "5", 5)
        );

        @Test
        @DisplayName("applyPagination - birinchi sahifa")
        void firstPage() {
            List<TestEntity> result = filterHelper.applyPagination(entities, 0, 2);
            assertThat(result).hasSize(2);
            assertThat(result).extracting("code").containsExactly("1", "2");
        }

        @Test
        @DisplayName("applyPagination - ikkinchi sahifa")
        void secondPage() {
            List<TestEntity> result = filterHelper.applyPagination(entities, 2, 2);
            assertThat(result).hasSize(2);
            assertThat(result).extracting("code").containsExactly("3", "4");
        }

        @Test
        @DisplayName("applyPagination - oxirgi sahifa (qoldiq)")
        void lastPage() {
            List<TestEntity> result = filterHelper.applyPagination(entities, 4, 2);
            assertThat(result).hasSize(1);
            assertThat(result.get(0).code()).isEqualTo("5");
        }

        @Test
        @DisplayName("applyPagination - offset > size")
        void offsetBeyondSize() {
            List<TestEntity> result = filterHelper.applyPagination(entities, 100, 10);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("applyFilterAndPagination - filter + pagination")
        void filterAndPagination() {
            String filter = """
                {"conditions":[{"property":"age","operator":">","value":"1"}]}
                """;
            List<TestEntity> result = filterHelper.applyFilterAndPagination(
                    entities, filter, 0, 2,
                    req -> filterHelper.getPropertyByReflection(req.entity(), req.property()));
            assertThat(result).hasSize(2);
            assertThat(result).extracting("code").containsExactly("2", "3");
        }
    }

    // =============================================
    // Extract helpers
    // =============================================

    @Nested
    @DisplayName("Extract helpers")
    class ExtractHelpers {

        @Test
        @DisplayName("extractInt - query param ustunligi")
        void extractIntQueryParam() {
            Map<String, Object> body = Map.of("limit", 100);
            int result = filterHelper.extractInt(body, "limit", 50, 10);
            assertThat(result).isEqualTo(50);
        }

        @Test
        @DisplayName("extractInt - body dan olish")
        void extractIntFromBody() {
            Map<String, Object> body = Map.of("limit", 100);
            int result = filterHelper.extractInt(body, "limit", null, 10);
            assertThat(result).isEqualTo(100);
        }

        @Test
        @DisplayName("extractInt - string body")
        void extractIntStringBody() {
            Map<String, Object> body = Map.of("offset", "25");
            int result = filterHelper.extractInt(body, "offset", null, 0);
            assertThat(result).isEqualTo(25);
        }

        @Test
        @DisplayName("extractInt - default qiymat")
        void extractIntDefault() {
            int result = filterHelper.extractInt(null, "limit", null, 50);
            assertThat(result).isEqualTo(50);
        }

        @Test
        @DisplayName("extractFilterFromBody - filter object")
        void extractFilterFromBodyObject() {
            Map<String, Object> condition = new LinkedHashMap<>();
            condition.put("property", "name");
            condition.put("operator", "=");
            condition.put("value", "test");
            Map<String, Object> filter = Map.of("conditions", List.of(condition));
            Map<String, Object> body = Map.of("filter", filter);

            String result = filterHelper.extractFilterFromBody(body);
            assertThat(result).isNotNull();
            assertThat(result).contains("conditions");
        }

        @Test
        @DisplayName("extractFilterFromBody - filter string")
        void extractFilterFromBodyString() {
            Map<String, Object> body = Map.of("filter", "{\"conditions\":[]}");
            String result = filterHelper.extractFilterFromBody(body);
            assertThat(result).isEqualTo("{\"conditions\":[]}");
        }

        @Test
        @DisplayName("extractFilterFromBody - null body")
        void extractFilterFromBodyNull() {
            String result = filterHelper.extractFilterFromBody(null);
            assertThat(result).isNull();
        }
    }

    // =============================================
    // Edge cases
    // =============================================

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("Bo'sh filter JSON")
        void emptyFilter() {
            List<TestEntity> entities = List.of(new TestEntity("A", "1", 1));
            List<TestEntity> result = filterHelper.applyFilter(entities, "", req -> null);
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Null filter")
        void nullFilter() {
            List<TestEntity> entities = List.of(new TestEntity("A", "1", 1));
            List<TestEntity> result = filterHelper.applyFilter(entities, null, req -> null);
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Noto'g'ri JSON filter")
        void invalidJsonFilter() {
            List<TestEntity> entities = List.of(new TestEntity("A", "1", 1));
            List<TestEntity> result = filterHelper.applyFilter(entities, "not-json", req -> null);
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("conditions yo'q")
        void noConditions() {
            List<TestEntity> entities = List.of(new TestEntity("A", "1", 1));
            List<TestEntity> result = filterHelper.applyFilter(entities, "{\"other\":\"value\"}", req -> null);
            assertThat(result).hasSize(1);
        }
    }

    // =============================================
    // Reflection property getter
    // =============================================

    @Test
    @DisplayName("getPropertyByReflection - oddiy getter")
    void reflectionGetter() {
        TestEntity entity = new TestEntity("Islom", "S001", 22);
        Object value = filterHelper.getPropertyByReflection(entity, "name");
        assertThat(value).isEqualTo("Islom");
    }

    @Test
    @DisplayName("getPropertyByReflection - underscore prefix")
    void reflectionGetterWithUnderscore() {
        TestEntity entity = new TestEntity("Islom", "S001", 22);
        Object value = filterHelper.getPropertyByReflection(entity, "_code");
        assertThat(value).isEqualTo("S001");
    }

    @Test
    @DisplayName("getPropertyByReflection - mavjud bo'lmagan property")
    void reflectionGetterNonExistent() {
        TestEntity entity = new TestEntity("Islom", "S001", 22);
        Object value = filterHelper.getPropertyByReflection(entity, "nonExistent");
        assertThat(value).isNull();
    }
}
