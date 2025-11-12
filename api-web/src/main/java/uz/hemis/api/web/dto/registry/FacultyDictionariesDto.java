package uz.hemis.api.web.dto.registry;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Faculty Dictionaries DTO - Reference data for filters
 * 
 * Purpose: Provide dropdown options for faculty filters
 * Frontend: Used in filter panel selects
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Faculty Dictionaries - Reference data for filters")
public class FacultyDictionariesDto {

    @Schema(description = "Status options")
    private List<DictionaryItem> statuses;

    @Schema(description = "Department types")
    private List<DictionaryItem> departmentTypes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DictionaryItem {
        @Schema(description = "Option code/value", example = "11")
        private String code;

        @Schema(description = "Option label (localized)", example = "Fakultet")
        private String label;

        @Schema(description = "Additional info", example = "Active faculty")
        private String description;
    }
}
