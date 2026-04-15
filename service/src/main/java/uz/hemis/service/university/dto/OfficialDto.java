package uz.hemis.service.university.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * University official (rector, prorektor, etc.) — response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfficialDto {
    private UUID employeeId;
    private UUID metaId;
    private String pinfl;
    private String firstName;
    private String lastName;
    private String middleName;
    private String phone;
    private String positionCode;
    private String positionName;
    private String decreeNumber;
    private String decreeDate;
    private String startDate;
    private String endDate;
    private boolean current;
}
