package uz.hemis.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SqlTableValidator Tests")
class SqlTableValidatorTest {

    // =========================================================
    // Accepted names — legacy + modern classifier patterns
    // =========================================================

    @ParameterizedTest(name = "ACCEPT: {0}")
    @ValueSource(strings = {
        "hemishe_h_position",
        "hemishe_h_gender",
        "hemishe_h_country",
        "hemishe_h_district",
        "hemishe_e_student",
        "hemishe_e_teacher",
        "hemishe_r_curriculum",
        "h_position",
        "h_position_type",
        "h_construction_material",
        "h_roof_type"
    })
    @DisplayName("Legacy classifier / entity / reference + modern h_* — accepted")
    void validateLegacyClassifier_acceptsValid(String name) {
        assertThat(SqlTableValidator.validateLegacyClassifier(name)).isEqualTo(name);
        assertThat(SqlTableValidator.isSafeLegacyClassifier(name)).isTrue();
    }

    // =========================================================
    // Rejected — injection attempts + malformed
    // =========================================================

    @ParameterizedTest(name = "REJECT: {0}")
    @ValueSource(strings = {
        // SQL injection vectors
        "hemishe_h_position; DROP TABLE users",
        "hemishe_h_position--",
        "hemishe_h_position WHERE 1=1",
        "hemishe_h_position UNION SELECT password FROM users",
        "hemishe_h_position'",
        "hemishe_h_position\"",
        "hemishe_h_position`",
        // Schema escape
        "public.hemishe_h_position",
        "hemishe_h_position.users",
        // Case-mixed (postgres lowercase by convention)
        "Hemishe_H_Position",
        "HEMISHE_H_POSITION",
        // Wrong namespace
        "users",
        "sec_user",
        "configuration",
        "outbox_event",
        // Wrong prefix
        "hemishe_x_position",
        "hemishe__position",
        "h__position",
        "ee_student",
        // Empty / structural
        "",
        " ",
        "hemishe_h_",
        "_position",
        "h_",
        // Special chars
        "hemishe_h_pos ition",
        "hemishe_h_pos\nition",
        "hemishe_h_pos\tition",
        "hemishe_h_pos$ition",
        "hemishe_h_pos@ition"
    })
    @DisplayName("Injection vectors + malformed — rejected")
    void validateLegacyClassifier_rejectsMalicious(String name) {
        assertThatThrownBy(() -> SqlTableValidator.validateLegacyClassifier(name))
            .isInstanceOf(IllegalArgumentException.class);
        assertThat(SqlTableValidator.isSafeLegacyClassifier(name)).isFalse();
    }

    @Test
    @DisplayName("Null input — IllegalArgumentException")
    void validateLegacyClassifier_nullRejected() {
        assertThatThrownBy(() -> SqlTableValidator.validateLegacyClassifier(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must not be null or blank");
        assertThat(SqlTableValidator.isSafeLegacyClassifier(null)).isFalse();
    }

    @Test
    @DisplayName("Inline usage — returns original name for SQL string concatenation")
    void validateLegacyClassifier_inlineUsageReturnsName() {
        String tableName = "hemishe_h_education_type";
        String sql = "SELECT code FROM " + SqlTableValidator.validateLegacyClassifier(tableName)
            + " WHERE delete_ts IS NULL";
        assertThat(sql).contains("FROM hemishe_h_education_type WHERE");
    }
}
