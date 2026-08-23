package uz.hemis.common.dto.classifier;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
@JsonPropertyOrder({"name", "nameRu", "nameEn", "active"})
@Schema(name = "ClassifierItemUpdate", description = "Klasifikator elementini tahrirlash")
public class ClassifierItemUpdateDto implements Serializable {

    @Size(max = 512, message = "Nom 512 belgidan oshmasligi kerak")
    @Schema(description = "Nomi (o'zbek)")
    private String name;

    @Size(max = 512, message = "Ruscha nom 512 belgidan oshmasligi kerak")
    @Schema(description = "Nomi (rus)")
    private String nameRu;

    @Size(max = 512, message = "Inglizcha nom 512 belgidan oshmasligi kerak")
    @Schema(description = "Nomi (ingliz)")
    private String nameEn;

    @Schema(description = "Faolmi")
    private Boolean active;
}
