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
                example = "3f2a9c14-8b7e-4c1a-9d0f-1e2b3c4d5e6f",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String id,

        @Schema(
                description = "Vazirlik \"Shifr\" kodi — OTM tomonida join/upsert KALITI. 2026 to'plamida 8-xonali. "
                        + "Global unikal EMAS (bir kod ikkala ta'lim turida yoki turli yillarda uchrashi mumkin), shuning uchun "
                        + "barqaror birlashtirish uchun code + educationType juftligidan foydalaning. "
                        + "REQUIRED (distribution faqat code!=null qatorlarni beradi).",
                example = "60710100",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String code,

        @Schema(
                description = "uz-UZ (lotin) — asosiy nom va identity anchor. REQUIRED.",
                example = "Kimyo muhandisligi va texnologiyasi",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String nameUz,

        @Schema(
                description = "oz-UZ (o'zbek kirill) nomi. (NULL bo'lishi mumkin)",
                example = "Кимё муҳандислиги ва технологияси"
        )
        String nameOz,

        @Schema(
                description = "Ruscha nom. (NULL bo'lishi mumkin)",
                example = "Химическая инженерия и технология"
        )
        String nameRu,

        @Schema(
                description = "Inglizcha nom. (NULL bo'lishi mumkin)",
                example = "Chemical Engineering and Technology"
        )
        String nameEn,

        @Schema(
                description = "Ta'lim turi kodi (FK hemishe_h_education_type.code): '11'=Bakalavr, '12'=Magistr. REQUIRED.",
                example = "11",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String educationType,

        @Schema(
                description = "educationType uchun aniqlangan (resolved) nom ('Bakalavr'/'Magistr'). "
                        + "educationType har doim mavjud bo'lgani uchun bu ham har doim to'ladi.",
                example = "Bakalavr",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String educationTypeName,

        @Schema(
                description = "Ota qatorning UUID'i — o'ziga havola qiluvchi (self-reference) qo'shnilik bog'lami; daraxt shu orqali qayta quriladi. "
                        + "Eng yuqori darajada (hierarchyLevel=1, ildiz) NULL bo'ladi. (NULL bo'lishi mumkin)",
                example = "a1b2c3d4-0000-1111-2222-333344445555"
        )
        String parentId,

        @Schema(
                description = "Taksonomiya chuqurligi — 1=Bilim sohasi, 2=Ta'lim sohasi, 3=Yo'nalish (mutaxassislik), 4=Ichki yo'nalish.",
                example = "3"
        )
        Integer hierarchyLevel,

        @Schema(
                description = "Mutaxassislik amal qiladigan o'quv yillari (h_speciality_year'dan). Bo'sh massiv bo'lishi mumkin.",
                example = "[2026]"
        )
        List<Integer> years,

        @Schema(
                description = "Faol bayroq.",
                example = "true"
        )
        Boolean active,

        @Schema(
                description = "Vazirlik ICHKI kuratsiya \"tekshirilgan\" belgisi (default false) — OTM sinxroni uchun SHART EMAS, faqat markaz ichki holati.",
                example = "false"
        )
        Boolean isChecked
) {
}
