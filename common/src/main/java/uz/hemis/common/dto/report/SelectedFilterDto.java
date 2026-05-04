package uz.hemis.common.dto.report;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"columnName", "refTable", "allowedCodes"})
public class SelectedFilterDto {
    private String columnName;
    private String refTable;
    private List<String> allowedCodes;
}
