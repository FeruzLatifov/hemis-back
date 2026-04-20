package uz.hemis.service.legacy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CubaEntityMapHelper Unit Tests")
class CubaEntityMapHelperTest {

    // ------------------------------------------------------------------
    //  extractUuid
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("extractUuid")
    class ExtractUuid {

        @Test
        @DisplayName("returns null for null input")
        void returnsNull_forNull() {
            assertThat(CubaEntityMapHelper.extractUuid(null)).isNull();
        }

        @Test
        @DisplayName("returns same UUID instance")
        void returnsSame_forUuid() {
            UUID id = UUID.randomUUID();
            assertThat(CubaEntityMapHelper.extractUuid(id)).isEqualTo(id);
        }

        @Test
        @DisplayName("parses UUID from string")
        void parsesFromString() {
            UUID id = UUID.randomUUID();
            assertThat(CubaEntityMapHelper.extractUuid(id.toString())).isEqualTo(id);
        }

        @Test
        @DisplayName("extracts UUID from CUBA nested map")
        void extractsFromNestedMap() {
            UUID id = UUID.randomUUID();
            Map<String, Object> nested = Map.of("id", id.toString());
            assertThat(CubaEntityMapHelper.extractUuid(nested)).isEqualTo(id);
        }

        @Test
        @DisplayName("returns null for invalid UUID string")
        void returnsNull_forInvalidString() {
            assertThat(CubaEntityMapHelper.extractUuid("not-a-uuid")).isNull();
        }

        @Test
        @DisplayName("returns null for empty string")
        void returnsNull_forEmptyString() {
            assertThat(CubaEntityMapHelper.extractUuid("")).isNull();
        }

        @Test
        @DisplayName("returns null for map with invalid UUID")
        void returnsNull_forMapWithInvalidUuid() {
            Map<String, Object> nested = Map.of("id", "not-a-uuid");
            assertThat(CubaEntityMapHelper.extractUuid(nested)).isNull();
        }
    }

    // ------------------------------------------------------------------
    //  extractString
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("extractString")
    class ExtractString {

        @Test
        @DisplayName("returns null for null input")
        void returnsNull_forNull() {
            assertThat(CubaEntityMapHelper.extractString(null)).isNull();
        }

        @Test
        @DisplayName("returns string as-is")
        void returnsStringAsIs() {
            assertThat(CubaEntityMapHelper.extractString("401")).isEqualTo("401");
        }

        @Test
        @DisplayName("extracts id from nested map")
        void extractsIdFromNestedMap() {
            Map<String, Object> nested = Map.of("_entityName", "test", "id", "401");
            assertThat(CubaEntityMapHelper.extractString(nested)).isEqualTo("401");
        }

        @Test
        @DisplayName("extracts code from nested map when no id")
        void extractsCodeFromNestedMap() {
            Map<String, Object> nested = Map.of("code", "402");
            assertThat(CubaEntityMapHelper.extractString(nested)).isEqualTo("402");
        }

        @Test
        @DisplayName("uses toString for other types")
        void usesToString_forOtherTypes() {
            assertThat(CubaEntityMapHelper.extractString(42)).isEqualTo("42");
        }
    }

    // ------------------------------------------------------------------
    //  extractStringId
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("extractStringId")
    class ExtractStringId {

        @Test
        @DisplayName("returns null for null")
        void returnsNull_forNull() {
            assertThat(CubaEntityMapHelper.extractStringId(null)).isNull();
        }

        @Test
        @DisplayName("returns null for non-map")
        void returnsNull_forNonMap() {
            assertThat(CubaEntityMapHelper.extractStringId("some-string")).isNull();
        }

        @Test
        @DisplayName("extracts id from map")
        void extractsIdFromMap() {
            Map<String, Object> map = Map.of("id", "123");
            assertThat(CubaEntityMapHelper.extractStringId(map)).isEqualTo("123");
        }

        @Test
        @DisplayName("returns null when map has no id")
        void returnsNull_whenNoId() {
            Map<String, Object> map = Map.of("code", "123");
            assertThat(CubaEntityMapHelper.extractStringId(map)).isNull();
        }
    }

    // ------------------------------------------------------------------
    //  extractCode
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("extractCode")
    class ExtractCode {

        @Test
        @DisplayName("returns null for null")
        void returnsNull_forNull() {
            assertThat(CubaEntityMapHelper.extractCode(null)).isNull();
        }

        @Test
        @DisplayName("returns null for non-map")
        void returnsNull_forNonMap() {
            assertThat(CubaEntityMapHelper.extractCode("plain")).isNull();
        }

        @Test
        @DisplayName("prefers code over id")
        void prefersCode() {
            Map<String, Object> map = Map.of("code", "C1", "id", "ID1");
            assertThat(CubaEntityMapHelper.extractCode(map)).isEqualTo("C1");
        }

        @Test
        @DisplayName("falls back to id when no code")
        void fallsBackToId() {
            Map<String, Object> map = Map.of("id", "ID1");
            assertThat(CubaEntityMapHelper.extractCode(map)).isEqualTo("ID1");
        }
    }

    // ------------------------------------------------------------------
    //  getStringValue
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("getStringValue")
    class GetStringValue {

        @Test
        void returnsNull_forNull() {
            assertThat(CubaEntityMapHelper.getStringValue(null)).isNull();
        }

        @Test
        void returnToString() {
            assertThat(CubaEntityMapHelper.getStringValue(123)).isEqualTo("123");
        }
    }

    // ------------------------------------------------------------------
    //  getIntegerValue
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("getIntegerValue")
    class GetIntegerValue {

        @Test
        void returnsNull_forNull() {
            assertThat(CubaEntityMapHelper.getIntegerValue(null)).isNull();
        }

        @Test
        void returnsIntFromNumber() {
            assertThat(CubaEntityMapHelper.getIntegerValue(42L)).isEqualTo(42);
        }

        @Test
        void parsesFromString() {
            assertThat(CubaEntityMapHelper.getIntegerValue("99")).isEqualTo(99);
        }

        @Test
        void returnsNull_forInvalidString() {
            assertThat(CubaEntityMapHelper.getIntegerValue("abc")).isNull();
        }
    }

    // ------------------------------------------------------------------
    //  getDoubleValue
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("getDoubleValue")
    class GetDoubleValue {

        @Test
        void returnsNull_forNull() {
            assertThat(CubaEntityMapHelper.getDoubleValue(null)).isNull();
        }

        @Test
        void returnsDoubleFromNumber() {
            assertThat(CubaEntityMapHelper.getDoubleValue(3.14f)).isEqualTo(3.14, org.assertj.core.data.Offset.offset(0.01));
        }

        @Test
        void parsesFromString() {
            assertThat(CubaEntityMapHelper.getDoubleValue("50000.5")).isEqualTo(50000.5);
        }
    }

    // ------------------------------------------------------------------
    //  getBooleanValue
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("getBooleanValue")
    class GetBooleanValue {

        @Test
        void returnsNull_forNull() {
            assertThat(CubaEntityMapHelper.getBooleanValue(null)).isNull();
        }

        @Test
        void returnsBooleanAsIs() {
            assertThat(CubaEntityMapHelper.getBooleanValue(true)).isTrue();
        }

        @Test
        void parsesFromString() {
            assertThat(CubaEntityMapHelper.getBooleanValue("true")).isTrue();
            assertThat(CubaEntityMapHelper.getBooleanValue("false")).isFalse();
        }
    }

    // ------------------------------------------------------------------
    //  parseDate
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("parseDate")
    class ParseDate {

        @Test
        void returnsNull_forNull() {
            assertThat(CubaEntityMapHelper.parseDate(null)).isNull();
        }

        @Test
        void parsesIsoDate() {
            LocalDate result = CubaEntityMapHelper.parseDate("2024-01-15");
            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 15));
        }

        @Test
        void returnsLocalDateAsIs() {
            LocalDate date = LocalDate.of(2024, 6, 1);
            assertThat(CubaEntityMapHelper.parseDate(date)).isEqualTo(date);
        }

        @Test
        void returnsNull_forInvalidDate() {
            assertThat(CubaEntityMapHelper.parseDate("not-a-date")).isNull();
        }
    }

    // ------------------------------------------------------------------
    //  parseLocalDateTime
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("parseLocalDateTime")
    class ParseLocalDateTime {

        @Test
        void returnsNull_forNull() {
            assertThat(CubaEntityMapHelper.parseLocalDateTime(null)).isNull();
        }

        @Test
        void returnsNull_forEmptyString() {
            assertThat(CubaEntityMapHelper.parseLocalDateTime("")).isNull();
        }

        @Test
        void parsesIsoDateTime() {
            LocalDateTime result = CubaEntityMapHelper.parseLocalDateTime("2024-01-15T10:30:00");
            assertThat(result).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        }

        @Test
        void returnsLocalDateTimeAsIs() {
            LocalDateTime dt = LocalDateTime.of(2024, 6, 1, 12, 0);
            assertThat(CubaEntityMapHelper.parseLocalDateTime(dt)).isEqualTo(dt);
        }

        @Test
        void returnsNull_forInvalidDateTime() {
            assertThat(CubaEntityMapHelper.parseLocalDateTime("not-a-datetime")).isNull();
        }
    }

    // ------------------------------------------------------------------
    //  putIfNotNull
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("putIfNotNull")
    class PutIfNotNull {

        @Test
        @DisplayName("puts non-null value")
        void putsNonNullValue() {
            Map<String, Object> map = new LinkedHashMap<>();
            CubaEntityMapHelper.putIfNotNull(map, "key", "value", false);
            assertThat(map).containsEntry("key", "value");
        }

        @Test
        @DisplayName("skips null value when returnNulls is false")
        void skipsNull_whenReturnNullsFalse() {
            Map<String, Object> map = new LinkedHashMap<>();
            CubaEntityMapHelper.putIfNotNull(map, "key", null, false);
            assertThat(map).doesNotContainKey("key");
        }

        @Test
        @DisplayName("puts JsonNull sentinel when returnNulls is true (serializes as JSON null)")
        void putsJsonNull_whenReturnNullsTrue() {
            Map<String, Object> map = new LinkedHashMap<>();
            CubaEntityMapHelper.putIfNotNull(map, "key", null, true);
            assertThat(map).containsEntry("key", uz.hemis.common.JsonNull.INSTANCE);
        }

        @Test
        @DisplayName("skips null value when returnNulls is null")
        void skipsNull_whenReturnNullsNull() {
            Map<String, Object> map = new LinkedHashMap<>();
            CubaEntityMapHelper.putIfNotNull(map, "key", null, null);
            assertThat(map).doesNotContainKey("key");
        }
    }
}
