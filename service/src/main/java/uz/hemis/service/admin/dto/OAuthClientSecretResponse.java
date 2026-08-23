package uz.hemis.service.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Maxfiy kalit almashtirilgandan keyingi javob.
 *
 * <p><strong>{@code plainSecret} faqat BIR MARTA qaytariladi:</strong> bazada faqat BCrypt hash
 * saqlanadi, ochiq matn hech qayerda (log, audit, kesh) yozilmaydi. Yo'qotilsa tiklab bo'lmaydi —
 * yana rotatsiya qilinadi.</p>
 *
 * <p>Admin o'z qiymatini bergan holatda {@code plainSecret} qaytarilmaydi (admin uni allaqachon
 * biladi, javobda takrorlash — keraksiz oshkor qilish).</p>
 *
 * <p>Naqsh: {@code WebhookSecretResponse} (ADR-0012).</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Yangi maxfiy kalit. Ochiq qiymat faqat shu javobda — darhol saqlang.")
public record OAuthClientSecretResponse(

        @Schema(description = "oauth_client.id")
        UUID id,

        @Schema(description = "OTM client_id — o'zgarmaydi")
        String clientId,

        @Schema(description = "Ochiq maxfiy kalit — faqat markaz generatsiya qilganda qaytariladi")
        String plainSecret,

        @Schema(description = "Rotatsiya hisoblagichi (secret_version)")
        Integer secretVersion,

        LocalDateTime rotatedAt,

        String warning
) {
    private static final String WARN_GENERATED =
            "Ochiq maxfiy kalit faqat shu javobda qaytarildi — tiklab bo'lmaydi, OTM IT uni darhol .env'ga yozsin. "
                    + "Eski maxfiy kalit bilan YANGI token olinmaydi, lekin berilgan tokenlar 24 soatgacha ishlaydi.";
    private static final String WARN_SUPPLIED =
            "Maxfiy kalit almashtirildi. Eski maxfiy kalit bilan YANGI token olinmaydi, lekin allaqachon berilgan "
                    + "tokenlar 24 soatgacha amal qilishda davom etadi — hisobni o'chirish ham "
                    + "ularni bekor qilmaydi.";

    /** Markaz generatsiya qilgan maxfiy kalit — ochiq qiymat bilan. */
    public static OAuthClientSecretResponse generated(UUID id, String clientId, String plainSecret,
                                                      Integer version, LocalDateTime rotatedAt) {
        return new OAuthClientSecretResponse(id, clientId, plainSecret, version, rotatedAt, WARN_GENERATED);
    }

    /** Admin o'zi bergan maxfiy kalit — ochiq qiymat qaytarilmaydi. */
    public static OAuthClientSecretResponse supplied(UUID id, String clientId,
                                                     Integer version, LocalDateTime rotatedAt) {
        return new OAuthClientSecretResponse(id, clientId, null, version, rotatedAt, WARN_SUPPLIED);
    }
}
