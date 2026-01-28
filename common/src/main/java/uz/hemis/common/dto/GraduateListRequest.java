package uz.hemis.common.dto;

import lombok.Data;
import java.util.List;

@Data
public class GraduateListRequest {
    private Integer year;
    private String universityCode;
    private List<Graduate> graduates;
    
    @Data
    public static class Graduate {
        private String pinfl;
        private String fullName;
        private String graduationDate;
        private String diplomaNumber;
        private String specialityCode;
    }
}
