package uz.hemis.service.dashboard.dto;

import lombok.Data;

@Data
public class TopUniversityDto {
    private Integer rank;
    private String code;
    private String name;
    private Long studentCount;
    private Long maleCount;
    private Long femaleCount;
    private Long grantCount;
    private Long contractCount;
}
