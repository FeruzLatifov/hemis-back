package uz.hemis.api.university.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.hemis.service.classifier.HSpecialityService;
import uz.hemis.service.classifier.dto.SpecialityClassifierDistResponse;

/**
 * Speciality classifier DISTRIBUTION to OTMs — bootstrap PULL snapshot.
 *
 * <p>The NEW-university-facing counterpart of the modern PUSH fanout: a Univer coming online
 * (or re-syncing) pulls the full APPROVED {@code h_speciality} set at once, in the same FLAT v1
 * shape the push delivers per row. Global reference data → authenticated but <strong>NOT
 * tenant-scoped</strong> (every OTM receives the identical set; {@code universityCode} is never a
 * row filter here). Additive channel — the frozen legacy classifier pull
 * ({@code /app/rest/v2/services/classifiers/*}, {@code ClassifierLegacyService.OLD_CLASSIFIER_MAP})
 * and the 175/175 contract are untouched.</p>
 *
 * <p>Audience: 224 Univer Yii2 backends ({@code ClientType.UNIVERSITY_BACKEND}) over OAuth
 * {@code client_credentials}; secured by {@code .anyRequest().authenticated()} (no SecurityConfig change).</p>
 *
 * @since 2.1.0
 */
@RestController
@RequestMapping("/api/v1/university/classifiers")
@Tag(name = "Mutaxassisliklar")
@SecurityRequirement(name = "oauth2")
@RequiredArgsConstructor
@Slf4j
public class SpecialityDistributionController {

    private final HSpecialityService specialityService;

    @GetMapping("/speciality")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Umumiy mutaxassislik klassifikatorini olish",
            description = """
                    ## Umumiy mutaxassislik klassifikatori

                    **Envelope:** `{ success, message, title, version, count, data }`. `title` — klassifikator
                    sarlavhasi (educationType'ga qarab: 11 → "Bakalavriat ta'lim yo'nalishlari", 12 → "Magistratura
                    mutaxassisliklari"); `version` — tarqatiladiganlarning `SUM(version)`'i (OTM cache-bust —
                    o'zgarganda oshadi, `!=` bilan solishtiriladi); `count` — `data` uzunligi. Har element ham
                    o'z `version`'ini oladi (per-mutaxassislik delta-sync).

                    **Javob shakli — MUHIM.** `data` — TEKIS (flat) JSON massiv, daraxt (nested tree) EMAS.
                    Har element mustaqil qator bo'lib, o'z darajasi (`hierarchyLevel`) va otasi (`parentId`)
                    havolasini olib yuradi — bu *adjacency list*. Daraxtni OTM tomoni o'zi quradi.

                    **4 daraja (`hierarchyLevel`):** 1 = Bilim sohasi · 2 = Ta'lim sohasi ·
                    3 = Yo'nalish (mutaxassislik) · 4 = Ichki yo'nalish.

                    **Tartib:** `code` bo'yicha o'sish (`ORDER BY code ASC`) — daraja bo'yicha guruhlanmagan,
                    shuning uchun daraxtni faqat `parentId` bo'yicha quring.

                    **Qamrov:** faqat APPROVED + kodli + faol (`active=true`) qatorlar
                    (NEEDS_REVIEW yoki kodsiz qatorlar tushmaydi).

                    **Join/upsert kaliti:** `code` + `educationType` (id vaqt o'tib o'zgarishi mumkin, code
                    barqaror). `educationType` (11=Bakalavr, 12=Magistr) filtri ixtiyoriy — bo'sh qoldirsangiz
                    ikkalasi keladi. Global ma'lumot — har OTM aynan bir xil to'plamni oladi.

                    **Daraxtni parentId'dan qurish (JS misol):**
                    ```js
                    const byId = new Map();
                    for (const n of flat) { n.children = []; byId.set(n.id, n); }
                    const roots = [];
                    for (const n of flat) {
                      const parent = n.parentId ? byId.get(n.parentId) : null;
                      if (parent) parent.children.push(n); else roots.push(n);
                    }
                    // roots → L1 Bilim sohasi → children → L2 → L3 Yo'nalish → L4 Ichki yo'nalish
                    ```
                    """
    )
    public ResponseEntity<SpecialityClassifierDistResponse> speciality(
            @Parameter(description = "Ta'lim turi filtri (11=Bakalavr, 12=Magistr)")
            @RequestParam(required = false) String educationType
    ) {
        SpecialityClassifierDistResponse response = specialityService.getDistribution(educationType);
        log.info("OTM speciality-classifier pull: educationType={}, items={}, version={}",
                educationType, response.count(), response.version());
        return ResponseEntity.ok(response);
    }
}
