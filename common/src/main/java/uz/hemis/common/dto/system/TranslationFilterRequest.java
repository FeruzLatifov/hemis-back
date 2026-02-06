package uz.hemis.common.dto.system;

import lombok.Data;

@Data
public class TranslationFilterRequest {
    private String language;
    private String category;
    private String searchQuery;
}
