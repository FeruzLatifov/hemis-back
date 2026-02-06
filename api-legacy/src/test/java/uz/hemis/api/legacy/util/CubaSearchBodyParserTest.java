package uz.hemis.api.legacy.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CubaSearchBodyParserTest {

    // =====================================================
    // extractInt tests
    // =====================================================

    @Test
    void extractInt_queryParamPresent_returnsQueryParam() {
        Map<String, Object> body = Map.of("limit", 100);
        int result = CubaSearchBodyParser.extractInt(body, "limit", 25, 50);
        assertThat(result).isEqualTo(25);
    }

    @Test
    void extractInt_bodyHasNumber_returnsBodyValue() {
        Map<String, Object> body = Map.of("limit", 100);
        int result = CubaSearchBodyParser.extractInt(body, "limit", null, 50);
        assertThat(result).isEqualTo(100);
    }

    @Test
    void extractInt_bodyHasStringNumber_parsesCorrectly() {
        Map<String, Object> body = Map.of("offset", "25");
        int result = CubaSearchBodyParser.extractInt(body, "offset", null, 0);
        assertThat(result).isEqualTo(25);
    }

    @Test
    void extractInt_bodyHasInvalidString_returnsDefault() {
        Map<String, Object> body = Map.of("offset", "abc");
        int result = CubaSearchBodyParser.extractInt(body, "offset", null, 0);
        assertThat(result).isEqualTo(0);
    }

    @Test
    void extractInt_nullBody_returnsDefault() {
        int result = CubaSearchBodyParser.extractInt(null, "limit", null, 50);
        assertThat(result).isEqualTo(50);
    }

    @Test
    void extractInt_keyNotInBody_returnsDefault() {
        Map<String, Object> body = Map.of("other", 10);
        int result = CubaSearchBodyParser.extractInt(body, "limit", null, 50);
        assertThat(result).isEqualTo(50);
    }

    // =====================================================
    // extractOffset / extractLimit tests
    // =====================================================

    @Test
    void extractOffset_existingValue_preserves() {
        Map<String, Object> body = Map.of("offset", 100);
        Integer result = CubaSearchBodyParser.extractOffset(body, 50);
        assertThat(result).isEqualTo(50);
    }

    @Test
    void extractOffset_nullValue_extractsFromBody() {
        Map<String, Object> body = Map.of("offset", 100);
        Integer result = CubaSearchBodyParser.extractOffset(body, null);
        assertThat(result).isEqualTo(100);
    }

    @Test
    void extractOffset_nullBodyNullValue_returnsNull() {
        Integer result = CubaSearchBodyParser.extractOffset(null, null);
        assertThat(result).isNull();
    }

    @Test
    void extractLimit_stringNumber_parsesCorrectly() {
        Map<String, Object> body = Map.of("limit", "75");
        Integer result = CubaSearchBodyParser.extractLimit(body, null);
        assertThat(result).isEqualTo(75);
    }

    @Test
    void extractLimit_invalidString_returnsNull() {
        Map<String, Object> body = Map.of("limit", "not_a_number");
        Integer result = CubaSearchBodyParser.extractLimit(body, null);
        assertThat(result).isNull();
    }

    // =====================================================
    // extractFilter tests
    // =====================================================

    @Test
    void extractFilter_nullBody_returnsNull() {
        String result = CubaSearchBodyParser.extractFilter(null);
        assertThat(result).isNull();
    }

    @Test
    void extractFilter_noFilterKey_returnsNull() {
        Map<String, Object> body = Map.of("limit", 10);
        String result = CubaSearchBodyParser.extractFilter(body);
        assertThat(result).isNull();
    }

    @Test
    void extractFilter_withMapFilter_returnsJson() {
        Map<String, Object> filter = Map.of("conditions", java.util.List.of());
        Map<String, Object> body = Map.of("filter", filter);
        String result = CubaSearchBodyParser.extractFilter(body);
        assertThat(result).contains("conditions");
    }

    @Test
    void extractFilter_withStringFilter_returnsString() {
        Map<String, Object> body = new HashMap<>();
        body.put("filter", "simple-string-filter");
        String result = CubaSearchBodyParser.extractFilter(body);
        assertThat(result).contains("simple-string-filter");
    }
}
