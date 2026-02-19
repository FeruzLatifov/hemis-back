package uz.hemis.common.dto.classifier;

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
@Schema(name = "ClassifierCategory", description = "Klasifikator kategoriyasi")
public class ClassifierCategoryDto implements Serializable {

    @Schema(description = "Kategoriya kaliti", example = "GENERAL")
    private String key;

    @Schema(description = "Nomi (o'zbek)", example = "Umumiy")
    private String titleUz;

    @Schema(description = "Nomi (rus)", example = "Общие")
    private String titleRu;

    @Schema(description = "Nomi (ingliz)", example = "General")
    private String titleEn;

    @Schema(description = "Kategoriya ichidagi klasifikatorlar soni", example = "15")
    private int classifierCount;
}
