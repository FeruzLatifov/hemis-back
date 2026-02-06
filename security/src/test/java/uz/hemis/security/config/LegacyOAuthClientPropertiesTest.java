package uz.hemis.security.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link LegacyOAuthClientProperties}.
 *
 * <p><strong>What is tested:</strong></p>
 * <ul>
 *   <li>Default field values (clientId and clientSecret are empty, not hardcoded)</li>
 *   <li>Getter/setter round-trip for clientId</li>
 *   <li>Getter/setter round-trip for clientSecret</li>
 *   <li>Default scope value ("rest-api")</li>
 *   <li>Construction with all fields set</li>
 * </ul>
 *
 * <p>No Spring context is loaded -- this is a plain POJO test.</p>
 *
 * @since 2.0.0
 */
@DisplayName("LegacyOAuthClientProperties Tests")
class LegacyOAuthClientPropertiesTest {

    // =====================================================
    // Default Value Tests
    // =====================================================

    @Test
    @DisplayName("Default clientId should be empty string, not a hardcoded value")
    void defaultValues_areNotHardcoded() {
        // Given
        LegacyOAuthClientProperties props = new LegacyOAuthClientProperties();

        // Then: clientId must be empty (not "client" or any other hardcoded value)
        assertThat(props.getClientId())
                .as("clientId default must be empty string to force explicit configuration")
                .isEqualTo("");
    }

    @Test
    @DisplayName("Default clientSecret should be empty string")
    void defaultClientSecret_isEmptyString() {
        // Given
        LegacyOAuthClientProperties props = new LegacyOAuthClientProperties();

        // Then
        assertThat(props.getClientSecret())
                .as("clientSecret default must be empty string to force explicit configuration")
                .isEqualTo("");
    }

    @Test
    @DisplayName("Default scope should be 'rest-api'")
    void defaultScope_isRestApi() {
        // Given
        LegacyOAuthClientProperties props = new LegacyOAuthClientProperties();

        // Then
        assertThat(props.getScope())
                .as("scope default must be 'rest-api' for OLD-HEMIS compatibility")
                .isEqualTo("rest-api");
    }

    // =====================================================
    // Setter / Getter Round-Trip Tests
    // =====================================================

    @Test
    @DisplayName("setClientId / getClientId round-trip")
    void setClientId_getsClientId() {
        // Given
        LegacyOAuthClientProperties props = new LegacyOAuthClientProperties();

        // When
        props.setClientId("my-hemis-client");

        // Then
        assertThat(props.getClientId()).isEqualTo("my-hemis-client");
    }

    @Test
    @DisplayName("setClientSecret / getClientSecret round-trip")
    void setClientSecret_getsClientSecret() {
        // Given
        LegacyOAuthClientProperties props = new LegacyOAuthClientProperties();

        // When
        props.setClientSecret("super-secret-value-2024");

        // Then
        assertThat(props.getClientSecret()).isEqualTo("super-secret-value-2024");
    }

    @Test
    @DisplayName("setScope / getScope round-trip")
    void setScope_getsScope() {
        // Given
        LegacyOAuthClientProperties props = new LegacyOAuthClientProperties();

        // When
        props.setScope("custom-scope");

        // Then
        assertThat(props.getScope()).isEqualTo("custom-scope");
    }

    // =====================================================
    // Full Construction Test
    // =====================================================

    @Test
    @DisplayName("Can construct with all fields set and read them back")
    void allFieldsSet_readBackCorrectly() {
        // Given
        LegacyOAuthClientProperties props = new LegacyOAuthClientProperties();
        props.setClientId("hemis-legacy-client");
        props.setClientSecret("s3cret!@#$%^&*()");
        props.setScope("rest-api-v2");

        // Then
        assertThat(props.getClientId()).isEqualTo("hemis-legacy-client");
        assertThat(props.getClientSecret()).isEqualTo("s3cret!@#$%^&*()");
        assertThat(props.getScope()).isEqualTo("rest-api-v2");
    }

    @Test
    @DisplayName("Overwriting a field replaces old value")
    void overwriteField_replacesOldValue() {
        // Given
        LegacyOAuthClientProperties props = new LegacyOAuthClientProperties();
        props.setClientId("first-id");

        // When
        props.setClientId("second-id");

        // Then
        assertThat(props.getClientId()).isEqualTo("second-id");
    }
}
