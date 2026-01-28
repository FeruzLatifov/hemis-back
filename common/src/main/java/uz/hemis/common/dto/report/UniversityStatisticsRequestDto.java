package uz.hemis.common.dto.report;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityStatisticsRequestDto {
    
    private List<SelectedFilterDto> horizontalColumns;
    
    private String search;
    
    @Min(0)
    @Builder.Default
    private Integer page = 0;
    
    @Min(1)
    @Max(100)
    @Builder.Default
    private Integer size = 20;
    
    @Builder.Default
    private String sort = "name";
    
    @Builder.Default
    private String direction = "ASC";
}
