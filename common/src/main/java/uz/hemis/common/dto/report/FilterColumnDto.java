package uz.hemis.common.dto.report;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"columnName", "refTable", "label", "dataType", "required"})
public class FilterColumnDto {
    private String columnName;
    private String refTable;
    private String label;
    private String dataType;
    private Boolean required;
}
