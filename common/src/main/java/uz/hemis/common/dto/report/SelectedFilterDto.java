package uz.hemis.common.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectedFilterDto {
    private String columnName;
    private String refTable;
    private List<String> allowedCodes;
}
