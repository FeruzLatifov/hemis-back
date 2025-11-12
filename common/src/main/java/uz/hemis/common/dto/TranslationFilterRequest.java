package uz.hemis.common.dto;

import lombok.Data;

@Data
public class TranslationFilterRequest {
    private String language;
    private String category;
    private String searchQuery;
}
