package uz.hemis.common.dto.building;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRawValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Kadastr obyekti — read DTO (OTM'ga serve). {@code subjects}/{@code documents} JSONB matn
 * {@code @JsonRawValue} bilan to'g'ridan-to'g'ri JSON sifatida chiqadi (escape qilinmaydi).
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "cadNumber", "cadNumberOld", "name",
    "region", "district", "address", "shortAddress", "street", "domNum", "neighborhood",
    "tip", "tipText", "vid", "vidText", "objectRooms",
    "landArea", "landAreaB", "objectArea", "objectAreaL", "objectAreaU",
    "cost", "banIs", "subjects", "documents",
    "fetchStatus", "syncedAt"
})
@Schema(name = "Cadastre", description = "Kadastr obyekti (egalar/maydon/qiymat)")
public class CadastreDto implements Serializable {

    private String cadNumber;
    private String cadNumberOld;
    private String name;

    private String region;
    private String district;
    private String address;
    private String shortAddress;
    private String street;
    private String domNum;
    private String neighborhood;

    private String tip;
    private String tipText;
    private String vid;
    private String vidText;
    private Integer objectRooms;

    private BigDecimal landArea;
    private BigDecimal landAreaB;
    private BigDecimal objectArea;
    private BigDecimal objectAreaL;
    private BigDecimal objectAreaU;

    private Long cost;
    private Boolean banIs;

    /** Egalar — JSONB matn to'g'ridan JSON sifatida. */
    @JsonRawValue
    private String subjects;
    @JsonRawValue
    private String documents;

    private String fetchStatus;
    private LocalDateTime syncedAt;
}
