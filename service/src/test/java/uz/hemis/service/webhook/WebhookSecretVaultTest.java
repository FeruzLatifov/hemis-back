package uz.hemis.service.webhook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uz.hemis.domain.entity.webhook.WebhookTarget;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("WebhookSecretVault — DB-backed (secret_enc) plain secret cache")
class WebhookSecretVaultTest {

    private WebhookSecretVault vault;
    private WebhookSecretCipher cipher;

    @BeforeEach
    void setUp() {
        cipher = new WebhookSecretCipher("test-encryption-key", "5c0744940b5c369b");
        vault = new WebhookSecretVault(cipher);
    }

    @Test
    void store_thenResolve_returnsSecret() {
        vault.store("337", "whsec_test123");

        WebhookTarget target = makeTarget("337");
        assertThat(vault.resolve(target)).isEqualTo("whsec_test123");
    }

    @Test
    void resolve_missing_throwsSecretMissingException() {
        WebhookTarget target = makeTarget("999");

        assertThatThrownBy(() -> vault.resolve(target))
                .isInstanceOf(WebhookSecretVault.WebhookSecretMissingException.class)
                .hasMessageContaining("999")
                .hasMessageContaining("regenerate-secret");
    }

    @Test
    void store_overwritesExisting() {
        vault.store("337", "whsec_old");
        vault.store("337", "whsec_new");

        WebhookTarget target = makeTarget("337");
        assertThat(vault.resolve(target)).isEqualTo("whsec_new");
    }

    @Test
    void remove_secretGone() {
        vault.store("337", "whsec_x");
        vault.remove("337");

        WebhookTarget target = makeTarget("337");
        assertThatThrownBy(() -> vault.resolve(target))
                .isInstanceOf(WebhookSecretVault.WebhookSecretMissingException.class);
    }

    @Test
    void size_tracksAddRemove() {
        assertThat(vault.size()).isZero();

        vault.store("337", "a");
        vault.store("401", "b");
        vault.store("501", "c");
        assertThat(vault.size()).isEqualTo(3);

        vault.remove("337");
        assertThat(vault.size()).isEqualTo(2);
    }

    @Test
    void multiTenant_isolated() {
        vault.store("337", "secret-337");
        vault.store("401", "secret-401");

        assertThat(vault.resolve(makeTarget("337"))).isEqualTo("secret-337");
        assertThat(vault.resolve(makeTarget("401"))).isEqualTo("secret-401");
    }

    @Test
    @DisplayName("resolve() cache miss → secret_enc'dan rehydrate (restart-safe, K1)")
    void resolve_cacheMiss_rehydratesFromSecretEnc() {
        // Restart simulyatsiyasi: cache bo'sh, lekin DB'da shifrlangan secret_enc bor.
        WebhookTarget target = makeTarget("337");
        target.setSecretEnc(cipher.encrypt("whsec_persisted"));

        assertThat(vault.resolve(target)).isEqualTo("whsec_persisted");
        // Endi cache'da bo'lishi kerak
        assertThat(vault.size()).isEqualTo(1);
    }

    private static WebhookTarget makeTarget(String code) {
        WebhookTarget t = new WebhookTarget();
        t.setUniversityCode(code);
        return t;
    }
}
