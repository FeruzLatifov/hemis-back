package uz.hemis.common.dto.classifier;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"code", "name", "nameRu", "nameEn", "active"})
@Schema(name = "ClassifierItemCreate", description = "Yangi klasifikator elementi yaratish")
public class ClassifierItemCreateDto implements Serializable {

    @NotBlank(message = "Kod bo'sh bo'lishi mumkin emas")
    @Size(max = 64, message = "Kod 64 belgidan oshmasligi kerak")
    @Schema(description = "Kod", example = "99", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Size(max = 512, message = "Nom 512 belgidan oshmasligi kerak")
    @Schema(description = "Nomi (o'zbek)", example = "Test klasifikator")
    private String name;

    @Size(max = 512, message = "Ruscha nom 512 belgidan oshmasligi kerak")
    @Schema(description = "Nomi (rus)", example = "Тест классификатор")
    private String nameRu;

    @Size(max = 512, message = "Inglizcha nom 512 belgidan oshmasligi kerak")
    @Schema(description = "Nomi (ingliz)", example = "Test classifier")
    private String nameEn;

    @Schema(description = "Faolmi", example = "true")
    @Builder.Default
    private Boolean active = true;
}
