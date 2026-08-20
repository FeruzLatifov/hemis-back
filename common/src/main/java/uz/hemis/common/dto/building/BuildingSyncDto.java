package uz.hemis.common.dto.building;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Univer → hemis-back sync payload.
 * BuildingCreateUpdateDto'ga o'xshash, qo'shimcha {@code sourceUid} bilan (univer'ning ichki ID).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
    "sourceUid", "name", "buildingTypeCode", "buildingTypeCodes", "ownershipCode",
    "address", "yearBuilt", "floorCount", "capacity",
    "totalArea", "usableArea",
    "constructionMaterialCode", "roofTypeCode",
    "lastRenovationDate",
    "latitude", "longitude", "mapUrl",
    "cadNumber", "note"
})
@Schema(name = "BuildingSync", description = "Univer tomondan sync qilinayotgan bino")
public class BuildingSyncDto implements Serializable {

    @NotBlank
    @Size(max = 255)
    private String sourceUid;

    @NotBlank
    @Size(max = 500)
    private String name;


    /** Bino turi kodi — markaziy h_building_type (11-45). ASOSIY tur (ro'yxat/filtr). */
    @Size(max = 20)
    private String buildingTypeCode;

    /** Bino tegishli BARCHA turlar (ko'p-tur: ham o'quv, ham ma'muriy...). buildingTypeCode = asosiy. */
    private List<String> buildingTypeCodes;

    /** Egalik shakli: OWN/OPERATIVE/RENT (h_building_ownership). Default OWN. */
    @Size(max = 20)
    private String ownershipCode;

    private String address;

    @Min(1800) @Max(2100)
    private Integer yearBuilt;

    @Min(1) @Max(100)
    private Integer floorCount;

    @PositiveOrZero
    private Integer capacity;

    @DecimalMin("0.0")
    private BigDecimal totalArea;

    @DecimalMin("0.0")
    private BigDecimal usableArea;

    @Size(max = 20)
    private String constructionMaterialCode;

    @Size(max = 20)
    private String roofTypeCode;

    @PastOrPresent
    private LocalDate lastRenovationDate;

    @DecimalMin("-90") @DecimalMax("90")
    private BigDecimal latitude;

    @DecimalMin("-180") @DecimalMax("180")
    private BigDecimal longitude;

    private String mapUrl;

    @Size(max = 50)
    private String cadNumber;

    private String note;
}
