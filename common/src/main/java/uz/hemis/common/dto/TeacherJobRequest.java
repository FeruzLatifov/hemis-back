package uz.hemis.common.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TeacherJobRequest {
    private String teacherId;
    private String position;
    private String employeeId;
    private String universityCode;
    private String departmentCode;
    private String positionType;
    private LocalDate startDate;
    private LocalDate endDate;
}
