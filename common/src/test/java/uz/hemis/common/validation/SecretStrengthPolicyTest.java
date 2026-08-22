package uz.hemis.common.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uz.hemis.common.validation.SecretStrengthPolicy.Violation;

/**
 * Mashina kredensiali mustahkamlik siyosati — server-tomon majburlash.
 *
 * <p>Asosiy da'vo: baho ENTROPIYAGA tayanadi, tarkib qoidalariga emas. Ya'ni uzun kichik-harfli
 * tasodifiy satr yaroqli, {@code Parol123!} esa — yo'q. Bu NIST SP 800-63B yo'nalishi.</p>
 *
 * <p>⚠️ Bu yerdagi qoidalar {@code hemis-front/src/lib/secretStrength.ts} bilan mos bo'lishi shart;
 * u tomonda ham aynan shu holatlar sinaladi.</p>
 */
@DisplayName("SecretStrengthPolicy — mashina maxfiy kaliti uchun mustahkamlik siyosati")
class SecretStrengthPolicyTest {

    private static final String CLIENT_ID = "otm301";

    @Nested
    @DisplayName("Oson taxmin qilinadigan qiymatlar RAD ETILADI")
    class Rejected {

        @ParameterizedTest(name = "[{index}] \"{0}\" rad etiladi")
        @ValueSource(strings = {
                "admin123",            // klassik — qisqa ham, lug'at so'zi ham
                "admin1234567",        // 12 belgi, lekin lug'at so'zi + ketma-ketlik
                "Password2026",        // "murakkab" ko'rinadi, lekin lug'at so'zi
                "parol12345678",
                "hemis_secret_1",      // ikkita ro'yxatdagi so'z
                "qwertyuiopas",
                "aaaaaaaaaaaaaa",      // takrorlanish
                "abcdefghijkl",        // ketma-ketlik
                "111111111111",
                "test",                // juda qisqa
                "",
        })
        void rejectsWeakSecrets(String secret) {
            assertThat(SecretStrengthPolicy.validate(secret, CLIENT_ID))
                    .as("«%s» maqbul deb topildi", secret)
                    .isNotEmpty();
        }

        @Test
        @DisplayName("admin123 — aynan foydalanuvchi ko'rsatgan holat")
        void rejectsAdmin123WithClearReasons() {
            List<Violation> v = SecretStrengthPolicy.validate("admin123", CLIENT_ID);

            assertThat(v).contains(Violation.TOO_SHORT, Violation.COMMON_WORD);
            assertThat(SecretStrengthPolicy.describe(v))
                    .contains("12 belgidan")
                    .contains("taxmin qilinadigan");
        }

        @Test
        @DisplayName("client_id ni o'z ichiga olsa rad etiladi")
        void rejectsSecretContainingClientId() {
            assertThat(SecretStrengthPolicy.validate("xK9-OTM301-mQ7wZpL", CLIENT_ID))
                    .contains(Violation.CONTAINS_CLIENT_ID);
        }

        @Test
        @DisplayName("null xavfsiz — TOO_SHORT, istisno emas")
        void handlesNull() {
            assertThat(SecretStrengthPolicy.validate(null, CLIENT_ID))
                    .containsExactly(Violation.TOO_SHORT);
        }
    }

    @Nested
    @DisplayName("Yaroqli qiymatlar QABUL QILINADI")
    class Accepted {

        @ParameterizedTest(name = "[{index}] \"{0}\" qabul qilinadi")
        @ValueSource(strings = {
                "7Kq!zR4$mW9pXv2#",
                "Xm4!vQ8wRt2$Lp",
                "qwrtpsdfghjklzxcvbnm",   // uzun, faqat kichik harf — uzunlik entropiyani beradi
                "csec_9fA2bY7xQ3mZ1kR",
        })
        void acceptsStrongSecrets(String secret) {
            assertThat(SecretStrengthPolicy.validate(secret, CLIENT_ID))
                    .as("«%s» rad etildi", secret)
                    .isEmpty();
        }

        @Test
        @DisplayName("markaz generatsiya qiladigan maxfiy kalit o'z siyosatidan O'TADI")
        void generatedSecretShapePasses() {
            // OAuthClientSecretService: "csec_" + 48 base64url belgi.
            String generated = "csec_9fA2bY7xQ3mZ1kRt6WvJ8sNpLc4XdHgUeTbY2mQ7wZa";
            assertThat(SecretStrengthPolicy.validate(generated, CLIENT_ID)).isEmpty();
        }

        @Test
        @DisplayName("client_id null bo'lsa shu tekshiruv o'tkazib yuboriladi")
        void nullClientIdIsSafe() {
            assertThat(SecretStrengthPolicy.validate("7Kq!zR4$mW9pXv2#", null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Entropiya hisobi")
    class Entropy {

        @Test
        @DisplayName("uzunlik tarkib qoidalaridan ustun turadi")
        void lengthBeatsComposition() {
            int longLowercase = SecretStrengthPolicy.entropyBits("qwrtpsdfghjklzxcvbnm");
            int shortComplex = SecretStrengthPolicy.entropyBits("Ab1!Cd2@");
            assertThat(longLowercase).isGreaterThan(shortComplex);
        }

        @Test
        @DisplayName("takrorlanish entropiyani tushiradi")
        void repetitionLowersEntropy() {
            assertThat(SecretStrengthPolicy.entropyBits("aaaaaaaaaaaaaaaa"))
                    .isLessThan(SecretStrengthPolicy.entropyBits("qwrtpsdfghjklzxc"));
        }

        @Test
        @DisplayName("bo'sh/null uchun 0")
        void zeroForEmpty() {
            assertThat(SecretStrengthPolicy.entropyBits("")).isZero();
            assertThat(SecretStrengthPolicy.entropyBits(null)).isZero();
        }
    }
}
