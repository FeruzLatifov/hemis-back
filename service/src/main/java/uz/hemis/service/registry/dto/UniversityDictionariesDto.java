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
        description = "Ownership type options (Davlat, Xususiy, etc.)",
        example = "[{\"code\":\"11\",\"name\":\"Davlat\"},{\"code\":\"12\",\"name\":\"Xususiy\"}]"
    )
    private List<DictionaryItem> ownerships;

    @Schema(
        description = "University type options (Universitet, Institut, Akademiya, etc.)",
        example = "[{\"code\":\"11\",\"name\":\"Universitet\"},{\"code\":\"12\",\"name\":\"Institut\"}]"
    )
    private List<DictionaryItem> types;

    @Schema(
        description = "SOATO region options (Toshkent shahri, Samarqand viloyati, etc.)",
        example = "[{\"code\":\"26\",\"name\":\"Toshkent shahar\"}]"
    )
    private List<DictionaryItem> regions;

    @Schema(
        description = "Activity status options (Ishlamoqda, Yopilgan)",
        example = "[{\"code\":\"10\",\"name\":\"Ishlamoqda\"},{\"code\":\"11\",\"name\":\"Yopilgan\"}]"
    )
    private List<DictionaryItem> activityStatuses;

    @Schema(
        description = "Belongs to options (Vazirlikka tegishli, etc.)",
        example = "[{\"code\":\"10\",\"name\":\"Vazirlikka tegishli\"}]"
    )
    private List<DictionaryItem> belongsToOptions;

    @Schema(
        description = "Contract category options (Kontrakt, Shartnoma)",
        example = "[{\"code\":\"10\",\"name\":\"Kontrakt\"},{\"code\":\"11\",\"name\":\"Shartnoma\"}]"
    )
    private List<DictionaryItem> contractCategories;

    @Schema(
        description = "HEMIS version type options (To'liq, To'liq emas)",
        example = "[{\"code\":\"11\",\"name\":\"To'liq\"},{\"code\":\"12\",\"name\":\"To'liq emas\"}]"
    )
    private List<DictionaryItem> versionTypes;

    @Schema(
        description = "SOATO district options (tuman/shahar — 7 xonali kodlar)",
        example = "[{\"code\":\"1726269\",\"name\":\"Shayxontohur tumani\"}]"
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
            example = "26",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String code;

        @Schema(
            description = "Display name (localized)",
            example = "Toshkent shahar",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String name;
    }
}
