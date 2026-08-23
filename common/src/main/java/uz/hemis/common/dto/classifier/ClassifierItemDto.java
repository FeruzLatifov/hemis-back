package uz.hemis.common.dto.classifier;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "code", "name", "nameRu", "nameEn",
    "active", "version", "parentCode",
    "createTs", "updateTs"
})
@Schema(name = "ClassifierItem", description = "Klasifikator elementi")
public class ClassifierItemDto implements Serializable {

    @Schema(description = "Kod")
    private String code;

    @Schema(description = "Nomi (o'zbek)")
    private String name;

    @Schema(description = "Nomi (rus)")
    private String nameRu;

    @Schema(description = "Nomi (ingliz)")
    private String nameEn;

    @Schema(description = "Faolmi")
    private Boolean active;

    @Schema(description = "Versiya")
    private Integer version;

    @Schema(description = "Ota element kodi (ierarxik klasifikatorlar uchun)")
    private String parentCode;

    @Schema(description = "Yaratilgan vaqt")
    private LocalDateTime createTs;

    @Schema(description = "Yangilangan vaqt")
    private LocalDateTime updateTs;
}
