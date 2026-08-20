package uz.hemis.service.classifier.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * One OTM a speciality is attached to — a delete BLOCKER row.
 *
 * <p>Shown in the classifier delete dialog next to the blocking sub-directions, so the admin sees
 * where to go instead of only "3 attachment(s)". LIVE attachments only: a revoked (soft-deleted)
 * one is invisible in the registry and cannot be detached again, so listing it as a blocker would
 * point the admin at something they cannot act on. Those rows are purged at delete time
 * instead.</p>
 *
 * @since 2.1.0
 */
@Schema(description = "Mutaxassislik biriktirilgan OTM — o'chirishni bloklovchi faol biriktirma")
public record SpecialityAttachedUniversityDto(
        @Schema(
                description = "OTM kodi (hemishe_e_university.code).",
                example = "301",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String universityCode,

        @Schema(
                description = "OTM nomi; registrda topilmasa (orfan kod) kodning O'ZI qaytadi — u ham bloklaydi.",
                example = "Andijon davlat universiteti",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String universityName,

        @Schema(
                description = "Shu OTMdagi faol biriktirma qatorlari soni (ta'lim shakli/o'quv yili bo'yicha).",
                example = "3",
                requiredMode = Schema.RequiredMode.REQUIRED)
        long count
) {
}
