package uz.hemis.service.university.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.hemis.common.validation.ValidPhoneNumber;
import uz.hemis.common.validation.ValidPinfl;

/**
 * Request to appoint a university official (rector, prorektor, etc.)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfficialRequest {

    @NotBlank(message = "PINFL is required")
    @ValidPinfl
    private String pinfl;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String middleName;

    @ValidPhoneNumber
    private String phone;

    @NotBlank(message = "Position code is required")
    private String positionCode;  // 20=Rektor, 46=Birinchi prorektor, etc.

    private String decreeNumber;
    private String decreeDate;  // yyyy-MM-dd
}
