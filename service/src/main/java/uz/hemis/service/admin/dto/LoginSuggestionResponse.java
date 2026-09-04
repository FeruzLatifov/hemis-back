package uz.hemis.service.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Login taklifi javobi.
 *
 * <p>Wire shakli: {@code {"success":true,"data":{"login":"ism_familiya2"}}}. Login band
 * bo'lmagani tekshirilgan holda qaytariladi, lekin u BAND QILINMAYDI — taklif bilan create
 * orasida boshqa operator o'sha loginni olib qo'yishi mumkin, shu sababli yakuniy tekshiruv
 * yaratish paytida qayta bajariladi.</p>
 */
@Schema(description = "Generated login suggestion")
public record LoginSuggestionResponse(

        // Namuna (example) ATAYLAB yo'q: spec haqiqiy odamga o'xshash login ko'rsatmasligi kerak.
        @Schema(description = "Suggested login: ism_familiya, with a numeric suffix on collision")
        String login
) {
}
