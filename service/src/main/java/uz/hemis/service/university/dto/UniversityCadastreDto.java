package uz.hemis.service.university.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.hemis.domain.entity.UniversityCadastre;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Cadastre DTO — public-facing property record for a university.
 *
 * <p>Excludes audit internals ({@code version}, {@code apiRawResponse}, {@code createdBy/updatedBy})
 * and leaks nothing from JPA.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UniversityCadastreDto {
    private UUID id;
    private String universityCode;
    private String cadNumber;
    private String cadNumberOld;
    private Integer regionId;
    private String region;
    private Integer districtId;
    private String district;
    private String address;
    private String shortAddress;
    private String street;
    private String streetCode;
    private String domNum;
    private String neighborhood;
    private String neighborhoodId;
    private String tip;
    private String tipText;
    private String vid;
    private String vidText;
    private BigDecimal landArea;
    private BigDecimal landAreaI;
    private BigDecimal landAreaB;
    private BigDecimal landAreaF;
    private BigDecimal landAreaZ;
    private BigDecimal landAreaD;
    private BigDecimal landAreaU;
    private BigDecimal objectArea;
    private BigDecimal objectAreaL;
    private BigDecimal objectAreaU;
    private Long cost;
    private String ecoZone;
    private Boolean banIs;
    private String landFundType;
    private String landUseType;
    private String landFundCategory;
    private String subjects;
    private String documents;
    private String documentsL;
    private String bans;
    private String dataSource;
    private LocalDateTime syncedAt;

    public static UniversityCadastreDto from(UniversityCadastre e) {
        if (e == null) return null;
        return UniversityCadastreDto.builder()
                .id(e.getId())
                .universityCode(e.getUniversityCode())
                .cadNumber(e.getCadNumber())
                .cadNumberOld(e.getCadNumberOld())
                .regionId(e.getRegionId())
                .region(e.getRegion())
                .districtId(e.getDistrictId())
                .district(e.getDistrict())
                .address(e.getAddress())
                .shortAddress(e.getShortAddress())
                .street(e.getStreet())
                .streetCode(e.getStreetCode())
                .domNum(e.getDomNum())
                .neighborhood(e.getNeighborhood())
                .neighborhoodId(e.getNeighborhoodId())
                .tip(e.getTip())
                .tipText(e.getTipText())
                .vid(e.getVid())
                .vidText(e.getVidText())
                .landArea(e.getLandArea())
                .landAreaI(e.getLandAreaI())
                .landAreaB(e.getLandAreaB())
                .landAreaF(e.getLandAreaF())
                .landAreaZ(e.getLandAreaZ())
                .landAreaD(e.getLandAreaD())
                .landAreaU(e.getLandAreaU())
                .objectArea(e.getObjectArea())
                .objectAreaL(e.getObjectAreaL())
                .objectAreaU(e.getObjectAreaU())
                .cost(e.getCost())
                .ecoZone(e.getEcoZone())
                .banIs(e.getBanIs())
                .landFundType(e.getLandFundType())
                .landUseType(e.getLandUseType())
                .landFundCategory(e.getLandFundCategory())
                .subjects(e.getSubjects())
                .documents(e.getDocuments())
                .documentsL(e.getDocumentsL())
                .bans(e.getBans())
                .dataSource(e.getDataSource())
                .syncedAt(e.getSyncedAt())
                .build();
    }
}
