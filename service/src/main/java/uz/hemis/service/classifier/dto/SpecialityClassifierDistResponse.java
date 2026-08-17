package uz.hemis.service.classifier.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * OTM-facing envelope for the speciality classifier bootstrap PULL
 * ({@code GET /api/v1/university/classifiers/speciality}).
 *
 * <p>Mirrors the canonical old-hemis classifier contract ({@code title}, {@code version},
 * {@code count}, items) that the 224 Univer backends already consume, so a Univer can adopt
 * this endpoint with the same top-level shape:</p>
 * <ul>
 *   <li>{@code title} — classifier title, per {@code educationType} (matches
 *       {@code ClassifiersServiceBean}: 11 → "Bakalavriat ta'lim yo'nalishlari",
 *       12 → "Magistratura mutaxassisliklari").</li>
 *   <li>{@code version} — {@code SUM(h_speciality.version)} over the distributed set. Changes on
 *       any curation edit; Univer compares it ({@code !=}) to detect a stale cache.</li>
 *   <li>{@code count} — {@code data.size()}.</li>
 *   <li>{@code data} — the FLAT v1 {@link SpecialityDistItemDto} items (code ASC adjacency list).</li>
 * </ul>
 *
 * @since 2.1.0
 */
@Schema(description = "OTM bootstrap PULL — mutaxassislik klassifikatori (title/version/count + FLAT elementlar)")
public record SpecialityClassifierDistResponse(
        @Schema(description = "So'rov muvaffaqiyati.", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean success,

        @Schema(description = "Xabar (odatda \"OK\").", example = "OK")
        String message,

        @Schema(
                description = "Klassifikator sarlavhasi (old-hemis bilan mos): educationType=11 → "
                        + "\"Bakalavriat ta'lim yo'nalishlari\", 12 → \"Magistratura mutaxassisliklari\", "
                        + "filtrsiz → \"Mutaxassisliklar klassifikatori\".",
                example = "Bakalavriat ta'lim yo'nalishlari",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String title,

        @Schema(
                description = "Umumiy versiya = tarqatiladigan mutaxassisliklarning SUM(version)'i. Har qanday kuratsiya "
                        + "tahriridan keyin oshadi — OTM buni oldingi qiymat bilan solishtirib (!=) klassifikator "
                        + "yangilanganini aniqlaydi.",
                example = "1240",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Long version,

        @Schema(description = "Elementlar soni (data uzunligi).", example = "842", requiredMode = Schema.RequiredMode.REQUIRED)
        int count,

        @Schema(description = "FLAT v1 elementlar (adjacency list, code ASC).", requiredMode = Schema.RequiredMode.REQUIRED)
        List<SpecialityDistItemDto> data
) {
}
