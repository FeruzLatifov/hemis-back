package uz.hemis.common.dto.building;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Universitet binosi — read DTO (GET endpoint'larida javob).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
// Field tartibi declaration order bilan deterministik (Lombok getter order'ga bog'liq emas)
@JsonPropertyOrder({
    "id", "universityCode",
    "name", "categoryCode", "categoryName",
    "address", "yearBuilt", "capacity", "floorCount",
    "totalArea", "usableArea",
    "constructionMaterialCode", "constructionMaterialName", "roofTypeCode", "roofTypeName",
    "lastRenovationDate",
    "latitude", "longitude", "mapUrl",
    "cadNumber", "cadastre", "note",
    "source", "syncedAt",
    "version", "createdAt", "updatedAt"
})
@Schema(name = "Building", description = "Universitet binosi")
public class BuildingDto implements Serializable {

    private UUID id;
    private String universityCode;

    private String name;
    private String categoryCode;
    private String categoryName;

    private String address;
    private Integer yearBuilt;
    private Integer capacity;
    private Integer floorCount;

    private BigDecimal totalArea;
    private BigDecimal usableArea;

    private String constructionMaterialCode;
    private String constructionMaterialName;
    private String roofTypeCode;
    private String roofTypeName;

    private LocalDate lastRenovationDate;

    private BigDecimal latitude;
    private BigDecimal longitude;
    private String mapUrl;

    private String cadNumber;
    /** Kadastr API javobining xom JSON snapshot. */
    private JsonNode cadastre;
    private String note;

    private String source;
    private LocalDateTime syncedAt;

    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
