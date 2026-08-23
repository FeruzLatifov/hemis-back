package uz.hemis.common.dto.classifier;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
    "key", "titleUz", "titleRu", "titleEn", "classifierCount"
})
@Schema(name = "ClassifierCategory", description = "Klasifikator kategoriyasi")
public class ClassifierCategoryDto implements Serializable {

    @Schema(description = "Kategoriya kaliti")
    private String key;

    @Schema(description = "Nomi (o'zbek)")
    private String titleUz;

    @Schema(description = "Nomi (rus)")
    private String titleRu;

    @Schema(description = "Nomi (ingliz)")
    private String titleEn;

    @Schema(description = "Kategoriya ichidagi klasifikatorlar soni")
    private int classifierCount;
}
