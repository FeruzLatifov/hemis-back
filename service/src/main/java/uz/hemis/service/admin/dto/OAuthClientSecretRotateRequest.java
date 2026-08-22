package uz.hemis.service.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * OTM API-client sirini almashtirish so'rovi.
 *
 * <p>Ikki rejim:</p>
 * <ul>
 *   <li><strong>Bo'sh tana (tavsiya etiladi)</strong> — markaz kriptografik kuchli maxfiy kalit
 *       generatsiya qiladi ({@code csec_} + 288-bit). Admin uni o'ylab topmaydi.</li>
 *   <li><strong>{@code clientSecret} berilgan</strong> — admin o'zi tanlagan qiymat
 *       (OTM bilan oldindan kelishilgan bo'lsa). Minimal uzunlik 12 —
 *       bu mashina hisobi, odam paroli emas.</li>
 * </ul>
 *
 * <p><strong>Diqqat:</strong> {@code OAuthClientCreateRequest.clientSecret} da minimal uzunlik 4 —
 * bu alohida zaiflik, shu yerda takrorlanmadi (yaratishdagi qoidani o'zgartirish mavjud
 * integratsiyalarni buzishi mumkin, shuning uchun alohida qaror talab qiladi).</p>
 */
@Data
@Schema(description = "Maxfiy kalit almashtirish — bo'sh qoldirilsa markaz o'zi generatsiya qiladi")
public class OAuthClientSecretRotateRequest {

    @Size(min = 12, max = 255, message = "clientSecret kamida 12 belgi bo'lishi kerak")
    @Schema(
            description = "Admin tanlagan yangi maxfiy kalit. Bo'sh qoldiring — markaz kuchli maxfiy kalit generatsiya qiladi.",
            example = "null",
            nullable = true
    )
    private String clientSecret;

    /**
     * Bo'shliqlarni validatsiyadan OLDIN tozalaydi.
     *
     * <p>Aks holda {@code @Size(min = 12)} chetlab o'tilardi: "           x" — 11 bo'shliq + 1 belgi —
     * xom holda 12 belgi bo'lib tekshiruvdan o'tar, servis esa keyin {@code trim()} qilib
     * bir belgilik maxfiy kalitni saqlab qo'yardi.</p>
     *
     * <p>Butunlay bo'sh qiymat {@code null} ga aylantiriladi — bu "markaz o'zi generatsiya qilsin"
     * degani ({@code @Size} null'ga qo'llanmaydi).</p>
     */
    public void setClientSecret(String clientSecret) {
        this.clientSecret = (clientSecret == null || clientSecret.isBlank()) ? null : clientSecret.trim();
    }
}
