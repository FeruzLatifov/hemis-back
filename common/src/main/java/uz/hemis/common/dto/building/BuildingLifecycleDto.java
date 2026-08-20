package uz.hemis.common.dto.building;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
 * Bino lifecycle hodisasi — immutable read view.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id", "eventType", "eventDate",
    "cost", "decreeNumber", "decreeDate",
    "note", "createdAt", "createdBy"
})
@Schema(name = "BuildingLifecycle", description = "Bino tarixi voqeasi")
public class BuildingLifecycleDto implements Serializable {

    private UUID id;
    private String eventType;
    private LocalDate eventDate;

    private BigDecimal cost;
    private String decreeNumber;
    private LocalDate decreeDate;

    private String note;

    private LocalDateTime createdAt;
    private String createdBy;
}
