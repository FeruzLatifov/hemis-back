package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Employee Jobs Registry Dictionaries DTO - reference data for filters (cached).
 */
@Schema(name = "EmployeeJobsDictionaries", description = "Reference data for employee-jobs filter dropdowns (cached)")
public record EmployeeJobsDictionariesDto(
    List<DictionaryItem> universities,
    List<DictionaryItem> employeeTypes
) {

    @Schema(name = "EmployeeJobsDictionaryItem", description = "Generic dictionary item {code,name}")
    public record DictionaryItem(String code, String name) {}
}
