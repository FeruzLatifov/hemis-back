package uz.hemis.service.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Person data resolved from the api_mspd/GUVD passport-data gateway (172.18.9.171)
 * for the "Shaxs" (person) user-create autofill flow.
 *
 * <p>Populated by {@code GovPersonLookupService} from {@code POST /person/pinpp-and-document/}
 * (PINFL + passport document) and {@code /person/person-address/}. All fields are raw strings
 * as returned by the gateway (dates ISO {@code yyyy-MM-dd}). NO DB side-effect — this is a
 * read-only lookup that fills the create form; persistence happens on save.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Person data resolved from the GUVD/api_mspd passport-data gateway")
public class GovPersonDto {

    @Schema(description = "PINFL / JSHSHIR (14 digits)", example = "31507976020031")
    private String pinfl;

    @Schema(description = "First name (GUVD: name_latin)", example = "AKMAL")
    private String firstName;

    @Schema(description = "Last name / surname (GUVD: sur_name_latin)", example = "ABDULLAYEV")
    private String lastName;

    @Schema(description = "Middle name / patronymic (GUVD: patronym_name_latin)", example = "AHMADOVICH")
    private String middleName;

    @Schema(description = "Composed full name (last first middle)")
    private String fullName;

    @Schema(description = "Birth date ISO yyyy-MM-dd (GUVD: birth_date)", example = "1990-01-15")
    private String birthDate;

    @Schema(description = "Birth place (GUVD: birth_place)", example = "TOSHKENT SHAHAR")
    private String birthPlace;

    @Schema(description = "Gender (GUVD: sex)", example = "M")
    private String gender;

    @Schema(description = "Nationality (GUVD: nationality)", example = "O'ZBEKISTON")
    private String nationality;

    @Schema(description = "Passport series+number (GUVD: document)", example = "AB1234567")
    private String passport;

    @Schema(description = "Passport issuing place (GUVD: doc_give_place)")
    private String passportGivePlace;

    @Schema(description = "Passport issued date ISO yyyy-MM-dd (GUVD: issued_date)")
    private String passportIssuedDate;

    @Schema(description = "Passport expiry date ISO yyyy-MM-dd (GUVD: expiry_date)")
    private String passportExpiryDate;

    @Schema(description = "Registered address (GUVD person-address: region, district, address)")
    private String address;

    @Schema(description = "Person photo base64 (GUVD: photo)")
    private String photo;
}
