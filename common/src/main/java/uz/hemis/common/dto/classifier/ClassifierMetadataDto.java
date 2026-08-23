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
    "apiKey", "tableName",
    "titleUz", "titleRu", "titleEn",
    "category", "itemCount", "editable", "hierarchical"
})
@Schema(name = "ClassifierMetadata", description = "Klasifikator metadata")
public class ClassifierMetadataDto implements Serializable {

    @Schema(description = "API kalit (URL da ishlatiladigan)")
    private String apiKey;

    @Schema(description = "DB jadval nomi")
    private String tableName;

    @Schema(description = "Nomi (o'zbek)")
    private String titleUz;

    @Schema(description = "Nomi (rus)")
    private String titleRu;

    @Schema(description = "Nomi (ingliz)")
    private String titleEn;

    @Schema(description = "Kategoriya")
    private String category;

    @Schema(description = "Elementlar soni")
    private long itemCount;

    @Schema(description = "Tahrirlanadigan yoki faqat o'qish uchun")
    private boolean editable;

    @Schema(description = "Ierarxik (parent_code mavjud)")
    private boolean hierarchical;
}
