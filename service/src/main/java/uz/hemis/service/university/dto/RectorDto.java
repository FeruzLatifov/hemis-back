package uz.hemis.service.university.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Rector DTO — from hemishe_e_teacher + hemishe_e_employee_jobs.
 * Source: university sync (position_code='20').
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RectorDto {
    private String firstname;
    private String lastname;
    private String fathername;
    private String pinfl;
    private String phone;
    private String positionCode;
    private String positionName;
}
