package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * University Dictionaries DTO - Reference data for university filters
 *
 * Purpose: Provide dropdown options for university filter selects
 * Frontend: Used in filter panel for region, ownership, type
 * Cache: 1 hour TTL (dictionaries don't change often)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "UniversityDictionaries",
    description = "Reference data for university filter dropdowns (cached for performance)"
)
public class UniversityDictionariesDto {

    @Schema(
        description = "Ownership type options (Davlat, Xususiy, etc.)"
    )
    private List<DictionaryItem> ownerships;

    @Schema(
        description = "University type options (Universitet, Institut, Akademiya, etc.)"
    )
    private List<DictionaryItem> types;

    @Schema(
        description = "SOATO region options (Toshkent shahri, Samarqand viloyati, etc.)"
    )
    private List<DictionaryItem> regions;

    @Schema(
        description = "Activity status options (Ishlamoqda, Yopilgan)"
    )
    private List<DictionaryItem> activityStatuses;

    @Schema(
        description = "Belongs to options (Vazirlikka tegishli, etc.)"
    )
    private List<DictionaryItem> belongsToOptions;

    @Schema(
        description = "Contract category options (Kontrakt, Shartnoma)"
    )
    private List<DictionaryItem> contractCategories;

    @Schema(
        description = "HEMIS version type options (To'liq, To'liq emas)"
    )
    private List<DictionaryItem> versionTypes;

    @Schema(
        description = "SOATO district options (tuman/shahar — 7 xonali kodlar)"
    )
    private List<DictionaryItem> districts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(
        name = "UniversityDictionaryItem",
        description = "Dictionary item for university filter dropdown"
    )
    public static class DictionaryItem {

        @Schema(
            description = "Unique code/value",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String code;

        @Schema(
            description = "Display name (localized)",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String name;
    }
}
