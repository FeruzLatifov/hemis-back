package uz.hemis.common.dto.classifier;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "ClassifierItemUpdate", description = "Klasifikator elementini tahrirlash")
public class ClassifierItemUpdateDto implements Serializable {

    @Size(max = 512, message = "Nom 512 belgidan oshmasligi kerak")
    @Schema(description = "Nomi (o'zbek)", example = "Yangilangan nom")
    private String name;

    @Size(max = 512, message = "Ruscha nom 512 belgidan oshmasligi kerak")
    @Schema(description = "Nomi (rus)", example = "Обновленное название")
    private String nameRu;

    @Size(max = 512, message = "Inglizcha nom 512 belgidan oshmasligi kerak")
    @Schema(description = "Nomi (ingliz)", example = "Updated name")
    private String nameEn;

    @Schema(description = "Faolmi", example = "true")
    private Boolean active;
}
