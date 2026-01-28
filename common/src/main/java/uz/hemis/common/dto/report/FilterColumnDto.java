package uz.hemis.common.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterColumnDto {
    private String columnName;
    private String refTable;
    private String label;
    private String dataType;
    private Boolean required;
}
