package uz.hemis.common.dto.building;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Univer → hemis-back sync payload.
 * BuildingCreateUpdateDto'ga o'xshash, qo'shimcha {@code sourceUid} bilan (univer'ning ichki ID).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
    "sourceUid", "name", "categoryCode",
    "address", "yearBuilt", "floorCount", "capacity",
    "totalArea", "usableArea",
    "constructionMaterialCode", "roofTypeCode",
    "lastRenovationDate",
    "latitude", "longitude", "mapUrl",
    "cadNumber", "cadastre", "note"
})
@Schema(name = "BuildingSync", description = "Univer tomondan sync qilinayotgan bino")
public class BuildingSyncDto implements Serializable {

    @NotBlank
    @Size(max = 255)
    private String sourceUid;

    @NotBlank
    @Size(max = 500)
    private String name;

    @NotBlank
    @Size(max = 20)
    private String categoryCode;

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

    /** Kadastr API javobining xom JSON snapshot (univer tomondan yuboriladi). */
    @Schema(description = "Kadastr ma'lumotining JSON nusxasi")
    private JsonNode cadastre;

    private String note;
}
