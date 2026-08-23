package uz.hemis.service.classifier.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * One OTM a speciality is attached to — a delete BLOCKER row.
 *
 * <p>Shown in the classifier delete dialog next to the blocking sub-directions, so the admin sees
 * where to go instead of only "3 attachment(s)". Attachments have no soft delete, so every OTM
 * listed here holds a row the admin can find in the registry and detach.</p>
 *
 * @since 2.1.0
 */
@Schema(description = "Mutaxassislik biriktirilgan OTM — o'chirishni bloklovchi biriktirma")
public record SpecialityAttachedUniversityDto(
        @Schema(
                description = "OTM kodi (hemishe_e_university.code).",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String universityCode,

        @Schema(
                description = "OTM nomi; registrda topilmasa (orfan kod) kodning O'ZI qaytadi — u ham bloklaydi.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String universityName,

        @Schema(
                description = "Shu OTMdagi biriktirma qatorlari soni (ta'lim shakli/o'quv yili bo'yicha).",
                requiredMode = Schema.RequiredMode.REQUIRED)
        long count
) {
}
