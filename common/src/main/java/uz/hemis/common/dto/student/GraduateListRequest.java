package uz.hemis.common.dto.student;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import java.util.List;

@Data
@JsonPropertyOrder({"year", "universityCode", "graduates"})
public class GraduateListRequest {
    private Integer year;
    private String universityCode;
    private List<Graduate> graduates;

    @Data
    @JsonPropertyOrder({
        "pinfl", "fullName", "graduationDate", "diplomaNumber", "specialityCode"
    })
    public static class Graduate {
        private String pinfl;
        private String fullName;
        private String graduationDate;
        private String diplomaNumber;
        private String specialityCode;
    }
}
