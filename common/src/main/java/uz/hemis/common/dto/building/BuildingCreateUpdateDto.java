package uz.hemis.common.dto.building;

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
 * Bino yaratish/yangilash uchun DTO — POST/PUT body.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "BuildingCreateUpdate", description = "Bino yaratish/yangilash uchun ma'lumot")
public class BuildingCreateUpdateDto implements Serializable {

    @NotBlank
    @Size(max = 500)
    private String name;

    @NotBlank
    @Size(max = 20)
    private String categoryCode;

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
