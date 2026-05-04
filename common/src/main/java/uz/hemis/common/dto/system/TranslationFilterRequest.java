package uz.hemis.common.dto.system;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({"language", "category", "searchQuery"})
public class TranslationFilterRequest {
    private String language;
    private String category;
    private String searchQuery;
}
