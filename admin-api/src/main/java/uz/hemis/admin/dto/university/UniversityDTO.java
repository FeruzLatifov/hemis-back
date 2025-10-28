package uz.hemis.admin.dto.university;

import lombok.*;
import uz.hemis.admin.dto.common.MultilingualString;

/**
 * University DTO for API responses
 *
 * Contains basic university information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UniversityDTO {

    /**
     * University ID
     */
    private String id;

    /**
     * Unique university code
     */
    private String code;

    /**
     * University name (multilingual)
     */
    private MultilingualString name;

    /**
     * Short name (multilingual)
     */
    private MultilingualString shortName;

    /**
     * TIN (Tax Identification Number)
     */
    private String tin;

    /**
     * Active status
     */
    private Boolean active;
}
