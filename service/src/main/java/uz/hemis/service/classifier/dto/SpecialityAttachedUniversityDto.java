package uz.hemis.service.classifier.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * One OTM that a speciality is (or was) attached to — a delete BLOCKER row.
 *
 * <p>Shown in the classifier delete dialog next to the blocking sub-directions, so the admin
 * sees where to go instead of only "3 attachment(s)". Revoked (soft-deleted) attachments are
 * included on purpose: {@code fk_univ_spec_attach_spec} is {@code ON DELETE RESTRICT} and
 * blocks on them too — hence the two counters, {@code total} (all physical rows) and
 * {@code live} (still {@code deleted_at IS NULL}).</p>
 *
 * @since 2.1.0
 */
@Schema(description = "Mutaxassislik biriktirilgan OTM — o'chirishni bloklovchi qator (bekor qilinganlari ham)")
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
                description = "Shu OTMdagi BARCHA biriktirma qatorlari — bekor qilingan (soft-deleted) larni ham qo'shib.",
                example = "3",
                requiredMode = Schema.RequiredMode.REQUIRED)
        long total,

        @Schema(
                description = "Shu OTMdagi faol (bekor qilinmagan) biriktirma qatorlari soni.",
                example = "2",
                requiredMode = Schema.RequiredMode.REQUIRED)
        long live
) {
}
