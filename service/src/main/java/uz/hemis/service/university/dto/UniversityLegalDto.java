package uz.hemis.service.university.dto;

import lombok.Builder;
import lombok.Data;
import uz.hemis.domain.entity.UniversityLegal;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class UniversityLegalDto {
    private String universityCode;
    private String shortName;
    private Integer opf;
    private Integer kfs;
    private String tin;
    private String oked;
    private String soogu;
    private String registrationNumber;
    private LocalDate registrationDate;
    private LocalDate reregistrationDate;
    private Integer status;
    private LocalDate statusUpdated;
    private Integer avgEmployees;

    // Billing address
    private String billingStreet;
    private String billingSoato;
    /** Resolved name for `billingSoato` (district/region from hemishe_h_soato). */
    private String billingSoatoName;
    private String billingPostcode;
    private String billingCadastre;
    private Integer billingCountryCode;

    // Bank accounts
    private String bankAccounts;

    // Director — employee dan
    private PersonRefDto director;

    // Accountant — employee dan
    private PersonRefDto accountant;

    // Sync
    private LocalDateTime syncedAt;

    public static UniversityLegalDto from(UniversityLegal entity) {
        if (entity == null) return null;
        return UniversityLegalDto.builder()
                .universityCode(entity.getUniversityCode())
                .shortName(entity.getShortName())
                .opf(entity.getOpf())
                .kfs(entity.getKfs())
                .tin(entity.getTin())
                .oked(entity.getOked())
                .soogu(entity.getSoogu())
                .registrationNumber(entity.getRegistrationNumber())
                .registrationDate(entity.getRegistrationDate())
                .reregistrationDate(entity.getReregistrationDate())
                .status(entity.getStatus())
                .statusUpdated(entity.getStatusUpdated())
                .avgEmployees(entity.getAvgEmployees())
                .billingStreet(entity.getBillingStreet())
                .billingSoato(entity.getBillingSoato())
                .billingPostcode(entity.getBillingPostcode())
                .billingCadastre(entity.getBillingCadastre())
                .billingCountryCode(entity.getBillingCountryCode())
                .bankAccounts(entity.getBankAccounts())
                .director(PersonRefDto.from(entity.getDirectorEmployee()))
                .accountant(PersonRefDto.from(entity.getAccountantEmployee()))
                .syncedAt(entity.getSyncedAt())
                .build();
    }
}
