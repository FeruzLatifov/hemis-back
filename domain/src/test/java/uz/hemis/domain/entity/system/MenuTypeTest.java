package uz.hemis.domain.entity.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MenuType} enum + JPA converter + JSON serialization.
 */
@DisplayName("MenuType enum + JPA converter + Jackson")
class MenuTypeTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("Enum + getValue")
    class EnumBasics {

        @Test
        @DisplayName("MAIN.getValue() — 'main' lowercase")
        void mainValue() {
            assertThat(MenuType.MAIN.getValue()).isEqualTo("main");
        }

        @Test
        @DisplayName("SYSTEM.getValue() — 'system' lowercase")
        void systemValue() {
            assertThat(MenuType.SYSTEM.getValue()).isEqualTo("system");
        }

        @Test
        @DisplayName("values() — 2 ta enum (kelajak qo'shilishi mumkin)")
        void valuesArray() {
            assertThat(MenuType.values()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("fromValue() — string → enum")
    class FromValue {

        @Test
        @DisplayName("'main' → MAIN")
        void mainString() {
            assertThat(MenuType.fromValue("main")).isEqualTo(MenuType.MAIN);
        }

        @Test
        @DisplayName("'system' → SYSTEM")
        void systemString() {
            assertThat(MenuType.fromValue("system")).isEqualTo(MenuType.SYSTEM);
        }

        @Test
        @DisplayName("case-insensitive ('MAIN', 'Main', 'main')")
        void caseInsensitive() {
            assertThat(MenuType.fromValue("MAIN")).isEqualTo(MenuType.MAIN);
            assertThat(MenuType.fromValue("Main")).isEqualTo(MenuType.MAIN);
            assertThat(MenuType.fromValue("SYSTEM")).isEqualTo(MenuType.SYSTEM);
        }

        @Test
        @DisplayName("unknown qiymat → MAIN default")
        void unknownDefaultsToMain() {
            assertThat(MenuType.fromValue("unknown")).isEqualTo(MenuType.MAIN);
            assertThat(MenuType.fromValue("")).isEqualTo(MenuType.MAIN);
        }
    }

    @Nested
    @DisplayName("JPA Converter")
    class JpaConverter {

        private final MenuType.Converter converter = new MenuType.Converter();

        @Test
        @DisplayName("enum → DB string (lowercase)")
        void convertToDb() {
            assertThat(converter.convertToDatabaseColumn(MenuType.MAIN)).isEqualTo("main");
            assertThat(converter.convertToDatabaseColumn(MenuType.SYSTEM)).isEqualTo("system");
        }

        @Test
        @DisplayName("null → 'main' default (DB CHECK constraint complies)")
        void nullDefaultsToMain() {
            assertThat(converter.convertToDatabaseColumn(null)).isEqualTo("main");
        }

        @Test
        @DisplayName("DB string → enum")
        void convertFromDb() {
            assertThat(converter.convertToEntityAttribute("main")).isEqualTo(MenuType.MAIN);
            assertThat(converter.convertToEntityAttribute("system")).isEqualTo(MenuType.SYSTEM);
        }
    }

    @Nested
    @DisplayName("Jackson JSON serialization (wire format)")
    class JsonSerialization {

        @Test
        @DisplayName("MenuType.MAIN → JSON string 'main' (via @JsonValue)")
        void serializeToLowercase() throws Exception {
            String json = objectMapper.writeValueAsString(MenuType.MAIN);
            assertThat(json).isEqualTo("\"main\"");
        }

        @Test
        @DisplayName("MenuType.SYSTEM → JSON 'system'")
        void serializeSystem() throws Exception {
            String json = objectMapper.writeValueAsString(MenuType.SYSTEM);
            assertThat(json).isEqualTo("\"system\"");
        }

        @Test
        @DisplayName("JSON 'main' → MenuType.MAIN (via @JsonCreator)")
        void deserializeFromLowercase() throws Exception {
            MenuType result = objectMapper.readValue("\"main\"", MenuType.class);
            assertThat(result).isEqualTo(MenuType.MAIN);
        }

        @Test
        @DisplayName("JSON 'SYSTEM' → MenuType.SYSTEM (case-insensitive)")
        void deserializeCaseInsensitive() throws Exception {
            MenuType result = objectMapper.readValue("\"SYSTEM\"", MenuType.class);
            assertThat(result).isEqualTo(MenuType.SYSTEM);
        }

        @Test
        @DisplayName("JSON unknown value → MAIN default")
        void deserializeUnknown() throws Exception {
            MenuType result = objectMapper.readValue("\"foo\"", MenuType.class);
            assertThat(result).isEqualTo(MenuType.MAIN);
        }
    }
}
