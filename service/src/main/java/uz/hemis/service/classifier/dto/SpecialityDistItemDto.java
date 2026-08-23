package uz.hemis.service.classifier.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * FLAT v1 distribution item for the unified speciality classifier ({@code h_speciality}).
 *
 * <p>The wire shape shared by BOTH distribution channels so they stay byte-consistent:</p>
 * <ul>
 *   <li>the {@code api-university} bootstrap PULL ({@code GET /api/v1/university/classifiers/speciality}), and</li>
 *   <li>the modern PUSH delta ({@code aggregate_type="classifier"} → webhook fanout {@code data.item}).</li>
 * </ul>
 *
 * <p>Only APPROVED, code-bearing rows are ever emitted (the 53 {@code NEEDS_REVIEW}, incl. the
 * 15 code-less, are excluded). {@code educationType} + {@code educationTypeName} carry the
 * bachelor/master ('11'/'12') discriminator so the OTM side keeps them distinguishable.</p>
 *
 * <p>Field naming is {@code speciality}-prefixed and self-describing to match the OTM-facing
 * envelope ({@link SpecialityClassifierDistResponse}). Each item also carries its own
 * {@code version} (the row's optimistic-lock counter) so the OTM can do per-speciality delta
 * sync — mirroring the old-hemis per-item {@code version}.</p>
 *
 * @since 2.1.0
 */
@Schema(
        description = "Yagona mutaxassislik klassifikatori (h_speciality) uchun FLAT v1 tarqatish elementi — "
                + "javob kod bo'yicha o'sish tartibida (code ASC) TEKIS massiv bo'lib keladi va parentId "
                + "orqali qo'shnilik ro'yxati (adjacency list) hosil qiladi; daraxtni OTM tomoni o'zi quradi."
)
public record SpecialityDistItemDto(
        @Schema(
                description = "h_speciality birlamchi kaliti (UUID matni). Boshqa qatorning parentId'si aynan shu id'ga ishora qiladi.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String id,

        @Schema(
                description = "Vazirlik \"Shifr\" kodi — OTM tomonida join/upsert KALITI. 2026 to'plamida 8-xonali. "
                        + "Global unikal EMAS (bir kod ikkala ta'lim turida yoki turli yillarda uchrashi mumkin), shuning uchun "
                        + "barqaror birlashtirish uchun specialityCode + educationType juftligidan foydalaning. "
                        + "REQUIRED (distribution faqat code!=null qatorlarni beradi).",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String specialityCode,

        @Schema(
                description = "uz-UZ (lotin) — asosiy nom va identity anchor. REQUIRED.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String specialityNameUz,

        @Schema(
                description = "oz-UZ (o'zbek kirill) nomi. (NULL bo'lishi mumkin)"
        )
        String specialityNameOz,

        @Schema(
                description = "Ruscha nom. (NULL bo'lishi mumkin)"
        )
        String specialityNameRu,

        @Schema(
                description = "Inglizcha nom. (NULL bo'lishi mumkin)"
        )
        String specialityNameEn,

        @Schema(
                description = "Ta'lim turi kodi (FK hemishe_h_education_type.code): '11'=Bakalavr, '12'=Magistr. REQUIRED.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String educationType,

        @Schema(
                description = "educationType uchun aniqlangan (resolved) nom ('Bakalavr'/'Magistr'). "
                        + "educationType har doim mavjud bo'lgani uchun bu ham har doim to'ladi.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String educationTypeName,

        @Schema(
                description = "Ota qatorning UUID'i — o'ziga havola qiluvchi (self-reference) qo'shnilik bog'lami; daraxt shu orqali qayta quriladi. "
                        + "Eng yuqori darajada (hierarchyLevel=1, ildiz) NULL bo'ladi. (NULL bo'lishi mumkin)"
        )
        String parentId,

        @Schema(
                description = "Taksonomiya chuqurligi — 1=Bilim sohasi, 2=Ta'lim sohasi, 3=Yo'nalish (mutaxassislik), 4=Ichki yo'nalish."
        )
        Integer hierarchyLevel,

        @Schema(
                description = "Mutaxassislik amal qiladigan o'quv yillari (h_speciality_year'dan). Bo'sh massiv bo'lishi mumkin."
        )
        List<Integer> years,

        @Schema(
                description = "Faol bayroq."
        )
        Boolean active,

        @Schema(
                description = "Vazirlik ICHKI kuratsiya \"tekshirilgan\" belgisi (default false) — OTM sinxroni uchun SHART EMAS, faqat markaz ichki holati."
        )
        Boolean isChecked,

        @Schema(
                description = "Shu mutaxassislikning versiyasi (optimistik-qulf hisoblagichi). Qator tahrirlanganda oshadi — "
                        + "OTM per-mutaxassislik delta-sync uchun ishlatadi. Umumiy 'version' (envelope) = shu qiymatlar SUM'i.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Integer version
) {
}
