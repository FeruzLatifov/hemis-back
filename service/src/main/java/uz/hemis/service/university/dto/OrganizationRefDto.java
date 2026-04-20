package uz.hemis.service.university.dto;

import lombok.Builder;
import lombok.Data;
import uz.hemis.domain.entity.university.Organization;

@Data
@Builder
public class OrganizationRefDto {
    private String tin;
    private String name;

    public static OrganizationRefDto from(Organization org) {
        if (org == null) return null;
        return OrganizationRefDto.builder()
                .tin(org.getTin())
                .name(org.getName())
                .build();
    }
}
