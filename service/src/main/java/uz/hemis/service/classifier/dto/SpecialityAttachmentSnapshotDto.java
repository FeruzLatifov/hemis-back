package uz.hemis.service.classifier.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * One OTM-facing speciality-attachment snapshot item — "a speciality this OTM is
 * allowed to run, in this education form".
 *
 * <p>Consumed by the 224 Univer backends over the bootstrap PULL channel
 * ({@code GET /api/v1/university/speciality-attachments}). The wire join key is the
 * natural {@code specialityCode} (not the internal attachment UUID), mirroring
 * {@code SpecialityDistItemDto}; the OTM already knows its own {@code universityCode}
 * (JWT claim), so it is not repeated per row.</p>
 *
 * @since 2.1.0
 */
@Schema(description = "OTM biriktirilgan mutaxassislik snapshoti — shu OTM olib borishga ruxsat etilgan mutaxassislik, shu ta'lim shaklida")
public record SpecialityAttachmentSnapshotDto(
        @Schema(
                description = "h_speciality UUID — mutaxassislikning ichki identifikatori (klassifikatordagi id bilan bir xil).",
                example = "3f2a9c14-8b7e-4c1a-9d0f-1e2b3c4d5e6f",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String specialityId,

        @Schema(
                description = "Birlashtirish (join) KALITI — SpecialityDistItemDto.code bilan bir xil qiymat; mutaxassislikning tabiiy kodi.",
                example = "60710100",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String specialityCode,

        @Schema(
                description = "Ko'rsatish uchun mutaxassislik nomi (name_uz).",
                example = "Kimyo muhandisligi va texnologiyasi",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String specialityName,

        @Schema(
                description = "Ta'lim turi kodi — '11'=Bakalavr, '12'=Magistr.",
                example = "11",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String educationType,

        @Schema(
                description = "Ta'lim turi nomi — 'Bakalavr' yoki 'Magistr'.",
                example = "Bakalavr",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String educationTypeName,

        @Schema(
                description = "Ta'lim shakli kodi — '11'=Kunduzgi, '12'=Kechki, '16'=Masofaviy.",
                example = "11",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String educationForm,

        @Schema(
                description = "O'quv yili — biriktirish qaysi qabul yili uchun amal qiladi (2026 = 2026-2027 o'quv yili).",
                example = "2026",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer eduYear,

        @Schema(
                description = "Biriktirish holati — ACTIVE (faol) / SUSPENDED (vaqtincha to'xtatilgan) / REVOKED (bekor qilingan).",
                example = "ACTIVE",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String status
) {
}
