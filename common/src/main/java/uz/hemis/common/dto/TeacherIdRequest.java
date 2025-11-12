package uz.hemis.common.dto;

import lombok.Data;

@Data
public class TeacherIdRequest {
    private String pinfl;
    private String passportSeries;
    private String passportNumber;
}
