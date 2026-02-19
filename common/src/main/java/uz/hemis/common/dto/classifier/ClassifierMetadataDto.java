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
@Schema(name = "ClassifierMetadata", description = "Klasifikator metadata")
public class ClassifierMetadataDto implements Serializable {

    @Schema(description = "API kalit (URL da ishlatiladigan)", example = "education-type")
    private String apiKey;

    @Schema(description = "DB jadval nomi", example = "hemishe_h_education_type")
    private String tableName;

    @Schema(description = "Nomi (o'zbek)", example = "Ta'lim turlari")
    private String titleUz;

    @Schema(description = "Nomi (rus)", example = "Типы образования")
    private String titleRu;

    @Schema(description = "Nomi (ingliz)", example = "Education types")
    private String titleEn;

    @Schema(description = "Kategoriya", example = "EDUCATION")
    private String category;

    @Schema(description = "Elementlar soni", example = "5")
    private long itemCount;

    @Schema(description = "Tahrirlanadigan yoki faqat o'qish uchun", example = "true")
    private boolean editable;

    @Schema(description = "Ierarxik (parent_code mavjud)", example = "false")
    private boolean hierarchical;
}
