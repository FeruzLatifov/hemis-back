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
 * Bino yaratish/yangilash uchun DTO — POST/PUT body.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
    "name", "categoryCode", "buildingTypeCode", "buildingTypeCodes", "ownershipCode",
    "address", "yearBuilt", "floorCount", "capacity",
    "totalArea", "usableArea",
    "constructionMaterialCode", "roofTypeCode",
    "lastRenovationDate",
    "latitude", "longitude", "mapUrl",
    "cadNumber", "note"
})
@Schema(name = "BuildingCreateUpdate", description = "Bino yaratish/yangilash uchun ma'lumot")
public class BuildingCreateUpdateDto implements Serializable {

    @NotBlank
    @Size(max = 500)
    private String name;

    // Eski coarse kategoriya (ixtiyoriy rollup). Asosiy klassifikator — buildingTypeCode.
    @Size(max = 20)
    private String categoryCode;

    /** Bino turi kodi — markaziy h_building_type (11-45). ASOSIY tur. */
    @Size(max = 20)
    private String buildingTypeCode;

    /** Bino tegishli BARCHA turlar (ko'p-tur). buildingTypeCode = asosiy. */
    private List<String> buildingTypeCodes;

    /** Egalik shakli: OWN/OPERATIVE/RENT. Default OWN. */
    @Size(max = 20)
    private String ownershipCode;

    private String address;

    @Min(1800)
    @Max(2100)
    private Integer yearBuilt;

    @Min(1)
    @Max(100)
    private Integer floorCount;

    @PositiveOrZero
    private Integer capacity;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal totalArea;

    @DecimalMin(value = "0.0", inclusive = true)
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

    /**
     * Cross-field validation: coordinates juft bo'lishi kerak (bor yoki yo'q).
     * DB CHECK constraint bilan sinxron.
     */
    @AssertTrue(message = "latitude va longitude birga bo'lishi yoki birga bo'lmasligi kerak")
    public boolean isCoordinatesPaired() {
        return (latitude == null) == (longitude == null);
    }

    /**
     * Cross-field: usable_area <= total_area.
     */
    @AssertTrue(message = "usableArea totalArea dan katta bo'la olmaydi")
    public boolean isUsableAreaValid() {
        return usableArea == null || totalArea == null
                || usableArea.compareTo(totalArea) <= 0;
    }
}
