package uz.hemis.service.menu.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Menu Response DTO
 * Complete API response with menu, permissions, and metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuResponse {

    private List<MenuItem> menu;
    private List<String> permissions;
    private String locale;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private MetaData _meta;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetaData {
        private Boolean cached;
        private Long cacheExpiresAt;
        private String generatedAt;
    }
}
