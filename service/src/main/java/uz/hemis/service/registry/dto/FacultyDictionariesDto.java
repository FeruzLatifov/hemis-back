package uz.hemis.service.registry.dto;

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
public class FacultyDictionariesDto {

    private List<DictionaryItem> statuses;

    private List<DictionaryItem> departmentTypes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DictionaryItem {
        private String code;

        private String label;

        private String description;
    }
}
