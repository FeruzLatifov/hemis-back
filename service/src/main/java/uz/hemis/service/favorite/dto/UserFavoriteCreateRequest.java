package uz.hemis.service.favorite.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Input payload for {@code POST /api/v1/web/favorites}.
 *
 * @param menuCode menu item code to pin (references {@code menu.code})
 */
@Schema(description = "Create favorite request")
public record UserFavoriteCreateRequest(
        @NotBlank(message = "menuCode is required")
        @Size(max = 100, message = "menuCode must be at most 100 characters")
        @Schema(description = "Menu item code", example = "dashboard")
        String menuCode
) {}
