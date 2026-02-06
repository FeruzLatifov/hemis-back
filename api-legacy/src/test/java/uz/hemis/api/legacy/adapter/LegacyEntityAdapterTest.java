package uz.hemis.api.legacy.adapter;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LegacyEntityAdapter Tests")
class LegacyEntityAdapterTest {

    private LegacyEntityAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new LegacyEntityAdapter();
    }

    // =============================================
    // Test DTO classes
    // =============================================

    public static class SimpleDto {
        @JsonProperty("id")
        private UUID id;
        @JsonProperty("name")
        private String name;
        @JsonProperty("code")
        private String code;
        @JsonProperty("active")
        private Boolean active;

        public SimpleDto() {}

        public SimpleDto(UUID id, String name, String code, Boolean active) {
            this.id = id;
            this.name = name;
            this.code = code;
            this.active = active;
        }

        public UUID getId() { return id; }
        public String getName() { return name; }
        public String getCode() { return code; }
        public Boolean getActive() { return active; }
    }

    public static class DateDto {
        @JsonProperty("id")
        private UUID id;
        @JsonProperty("birthDate")
        private LocalDate birthDate;
        @JsonProperty("createTs")
        private LocalDateTime createTs;
        @JsonProperty("version")
        private Integer version;

        public DateDto() {}
        public UUID getId() { return id; }
        public LocalDate getBirthDate() { return birthDate; }
        public LocalDateTime getCreateTs() { return createTs; }
        public Integer getVersion() { return version; }
    }

    public static class RefDto {
        @JsonProperty("id")
        private UUID id;
        @JsonProperty("_university")
        private String university;
        @JsonProperty("_faculty")
        private String faculty;
        @JsonProperty("name")
        private String name;

        public RefDto() {}
        public UUID getId() { return id; }
        public String getUniversity() { return university; }
        public String getFaculty() { return faculty; }
        public String getName() { return name; }
    }

    // =============================================
    // toMap tests
    // =============================================

    @Nested
    @DisplayName("toMap - DTO ni CUBA Map ga aylantirish")
    class ToMapTests {

        @Test
        @DisplayName("Asosiy fieldlar to'g'ri aylantiriladi")
        void basicFields() {
            UUID id = UUID.randomUUID();
            SimpleDto dto = new SimpleDto(id, "Test University", "520", true);

            Map<String, Object> result = adapter.toMap(dto, "hemishe_EUniversity", false);

            assertThat(result).containsEntry("_entityName", "hemishe_EUniversity");
            assertThat(result).containsKey("_instanceName");
            assertThat(result).containsEntry("id", id.toString());
            assertThat(result).containsEntry("name", "Test University");
            assertThat(result).containsEntry("code", "520");
            assertThat(result).containsEntry("active", true);
        }

        @Test
        @DisplayName("null qiymatlar returnNulls=false da chiqarilmaydi")
        void nullFieldsExcluded() {
            SimpleDto dto = new SimpleDto(UUID.randomUUID(), null, "520", null);

            Map<String, Object> result = adapter.toMap(dto, "hemishe_EUniversity", false);

            assertThat(result).doesNotContainKey("name");
            assertThat(result).doesNotContainKey("active");
            assertThat(result).containsEntry("code", "520");
        }

        @Test
        @DisplayName("null qiymatlar returnNulls=true da JsonNull sifatida qaytariladi")
        void nullFieldsIncludedAsJsonNull() {
            SimpleDto dto = new SimpleDto(UUID.randomUUID(), null, "520", null);

            Map<String, Object> result = adapter.toMap(dto, "hemishe_EUniversity", true);

            assertThat(result).containsKey("name");
            assertThat(result.get("name")).isInstanceOf(JsonNull.class);
            assertThat(result).containsKey("active");
        }

        @Test
        @DisplayName("null DTO uchun null qaytaradi")
        void nullDto() {
            Map<String, Object> result = adapter.toMap(null, "hemishe_EUniversity", false);
            assertThat(result).isNull();
        }
    }

    // =============================================
    // Date format tests
    // =============================================

    @Nested
    @DisplayName("Sana formatlari")
    class DateFormatTests {

        @Test
        @DisplayName("LocalDate ISO formatida qaytariladi")
        void localDateFormat() {
            DateDto dto = new DateDto();
            setField(dto, "id", UUID.randomUUID());
            setField(dto, "birthDate", LocalDate.of(1999, 3, 15));

            Map<String, Object> result = adapter.toMap(dto, "hemishe_EStudent", false);

            assertThat(result.get("birthDate")).isEqualTo("1999-03-15");
        }

        @Test
        @DisplayName("LocalDateTime CUBA formatida qaytariladi (space bilan)")
        void localDateTimeFormat() {
            DateDto dto = new DateDto();
            setField(dto, "id", UUID.randomUUID());
            setField(dto, "createTs", LocalDateTime.of(2024, 1, 15, 10, 30, 45, 123000000));

            // createTs audit field - faqat non-local viewda ko'rinadi
            Map<String, Object> result = adapter.toMap(dto, "hemishe_EStudent", false, "eStudent-view");

            assertThat(result.get("createTs")).isEqualTo("2024-01-15 10:30:45.123");
        }
    }

    // =============================================
    // View tests
    // =============================================

    @Nested
    @DisplayName("CUBA View filtrlari")
    class ViewTests {

        @Test
        @DisplayName("_local view: underscore-prefixed reference fieldlar chiqarilmaydi")
        void localViewExcludesReferences() {
            RefDto dto = new RefDto();
            setField(dto, "id", UUID.randomUUID());
            setField(dto, "university", "520");
            setField(dto, "faculty", "101");
            setField(dto, "name", "Test");

            Map<String, Object> result = adapter.toMap(dto, "hemishe_EStudent", false, "_local");

            assertThat(result).containsKey("name");
            assertThat(result).doesNotContainKey("_university");
            assertThat(result).doesNotContainKey("_faculty");
        }

        @Test
        @DisplayName("_local view: audit fieldlar chiqarilmaydi")
        void localViewExcludesAuditFields() {
            DateDto dto = new DateDto();
            setField(dto, "id", UUID.randomUUID());
            setField(dto, "createTs", LocalDateTime.now());
            setField(dto, "version", 1);

            Map<String, Object> result = adapter.toMap(dto, "hemishe_EStudent", false, "_local");

            assertThat(result).doesNotContainKey("createTs");
            assertThat(result).doesNotContainKey("version");
        }

        @Test
        @DisplayName("Full view: barcha fieldlar qaytariladi")
        void fullViewIncludesAll() {
            RefDto dto = new RefDto();
            setField(dto, "id", UUID.randomUUID());
            setField(dto, "university", "520");
            setField(dto, "name", "Test");

            Map<String, Object> result = adapter.toMap(dto, "hemishe_EStudent", false, "eStudent-view");

            assertThat(result).containsKey("name");
            assertThat(result).containsKey("_university");
        }
    }

    // =============================================
    // fromMap tests
    // =============================================

    @Nested
    @DisplayName("fromMap - CUBA Map dan DTO ga aylantirish")
    class FromMapTests {

        @Test
        @DisplayName("Asosiy fieldlar to'g'ri aylantiriladi")
        void basicFromMap() {
            UUID id = UUID.randomUUID();
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("_entityName", "hemishe_EUniversity");
            map.put("id", id.toString());
            map.put("name", "Test University");
            map.put("code", "520");
            map.put("active", true);

            SimpleDto result = adapter.fromMap(map, SimpleDto.class);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getName()).isEqualTo("Test University");
            assertThat(result.getCode()).isEqualTo("520");
            assertThat(result.getActive()).isTrue();
        }

        @Test
        @DisplayName("CUBA underscore format (_university) qo'llab-quvvatlanadi")
        void cubaUnderscoreFormat() {
            UUID id = UUID.randomUUID();
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", id.toString());
            map.put("_university", "520");
            map.put("_faculty", "101");
            map.put("name", "Test");

            RefDto result = adapter.fromMap(map, RefDto.class);

            assertThat(result).isNotNull();
            assertThat(result.getUniversity()).isEqualTo("520");
            assertThat(result.getFaculty()).isEqualTo("101");
        }

        @Test
        @DisplayName("Nested object dan code olish")
        void nestedObjectExtraction() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", UUID.randomUUID().toString());
            map.put("_university", Map.of("_entityName", "hemishe_EUniversity", "code", "520"));
            map.put("name", "Test");

            RefDto result = adapter.fromMap(map, RefDto.class);

            assertThat(result).isNotNull();
            assertThat(result.getUniversity()).isEqualTo("520");
        }

        @Test
        @DisplayName("null yoki bo'sh map uchun null qaytaradi")
        void nullMap() {
            assertThat(adapter.fromMap(null, SimpleDto.class)).isNull();
            assertThat(adapter.fromMap(Map.of(), SimpleDto.class)).isNull();
        }

        @Test
        @DisplayName("LocalDate string dan parse qiladi")
        void parsesLocalDate() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", UUID.randomUUID().toString());
            map.put("birthDate", "1999-03-15");

            DateDto result = adapter.fromMap(map, DateDto.class);

            assertThat(result).isNotNull();
            assertThat(result.getBirthDate()).isEqualTo(LocalDate.of(1999, 3, 15));
        }

        @Test
        @DisplayName("LocalDateTime CUBA formatdan parse qiladi")
        void parsesLocalDateTimeCubaFormat() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", UUID.randomUUID().toString());
            map.put("createTs", "2024-01-15 10:30:45.123");

            DateDto result = adapter.fromMap(map, DateDto.class);

            assertThat(result).isNotNull();
            assertThat(result.getCreateTs()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 45, 123000000));
        }

        @Test
        @DisplayName("LocalDateTime ISO formatdan parse qiladi")
        void parsesLocalDateTimeIsoFormat() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", UUID.randomUUID().toString());
            map.put("createTs", "2024-01-15T10:30:45");

            DateDto result = adapter.fromMap(map, DateDto.class);

            assertThat(result).isNotNull();
            assertThat(result.getCreateTs().getYear()).isEqualTo(2024);
        }
    }

    // =============================================
    // toMapList tests
    // =============================================

    @Nested
    @DisplayName("toMapList")
    class ToMapListTests {

        @Test
        @DisplayName("Ro'yxatni CUBA Map lar ro'yxatiga aylantirish")
        void convertsList() {
            List<SimpleDto> dtos = List.of(
                    new SimpleDto(UUID.randomUUID(), "A", "1", true),
                    new SimpleDto(UUID.randomUUID(), "B", "2", false)
            );

            List<Map<String, Object>> result = adapter.toMapList(dtos, "hemishe_EUniversity", false);

            assertThat(result).hasSize(2);
            assertThat(result.get(0)).containsEntry("_entityName", "hemishe_EUniversity");
            assertThat(result.get(1)).containsEntry("_entityName", "hemishe_EUniversity");
        }

        @Test
        @DisplayName("null ro'yxat uchun bo'sh list qaytaradi")
        void nullList() {
            List<Map<String, Object>> result = adapter.toMapList(null, "hemishe_EUniversity", false);
            assertThat(result).isEmpty();
        }
    }

    // =============================================
    // Utility
    // =============================================

    private void setField(Object obj, String fieldName, Object value) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException("Cannot set field " + fieldName, e);
        }
    }
}
