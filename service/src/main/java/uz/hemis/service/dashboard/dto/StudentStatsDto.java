package uz.hemis.service.dashboard.dto;

import lombok.Data;
import java.util.List;

@Data
public class StudentStatsDto {
    private List<CategoryStatDto> byEducationForm;
    private List<CategoryStatDto> byRegion;
    private List<CategoryStatDto> byLanguage;
}
