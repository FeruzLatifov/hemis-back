package uz.hemis.service.university.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uz.hemis.domain.entity.university.UniversityFounder;
import uz.hemis.domain.entity.enums.FounderType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class UniversityFounderDto {
    private FounderType founderType;
    private String name;            // employee FIO or organization name
    private String tin;             // employee TIN or organization TIN
    private String pinfl;           // employee PINFL (null for legal)
    private BigDecimal sharePercent;
    private Long shareSum;
    @JsonProperty("isCurrent")
    private boolean isCurrent;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    // Nested refs (for detail view)
    private PersonRefDto person;           // individual → employee
    private OrganizationRefDto organization; // legal → organization

    public static UniversityFounderDto from(UniversityFounder entity) {
        if (entity == null) return null;

        var person = PersonRefDto.from(entity.getEmployee());
        var org = OrganizationRefDto.from(entity.getOrganization());

        String name;
        String tin;
        String pinfl;

        if (FounderType.INDIVIDUAL == entity.getFounderType()) {
            name = person != null ? person.getFullName() : null;
            tin = person != null ? person.getTin() : null;
            pinfl = person != null ? person.getPinfl() : null;
        } else {
            name = org != null ? org.getName() : null;
            tin = org != null ? org.getTin() : null;
            pinfl = null;
        }

        return UniversityFounderDto.builder()
                .founderType(entity.getFounderType())
                .name(name)
                .tin(tin)
                .pinfl(pinfl)
                .sharePercent(entity.getSharePercent())
                .shareSum(entity.getShareSum())
                .isCurrent(entity.getIsCurrent())
                .effectiveFrom(entity.getEffectiveFrom())
                .effectiveTo(entity.getEffectiveTo())
                .person(person)
                .organization(org)
                .build();
    }
}
