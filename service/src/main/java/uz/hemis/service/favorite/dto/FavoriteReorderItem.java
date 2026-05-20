package uz.hemis.service.favorite.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Reorder favorite request item — DTO contract.
 *
 * <p>Avval {@code UserFavoriteService.FavoriteOrderItem} ichki record edi —
 * service internals + API contract aralashuvi (mass-assignment risk).
 * Endi alohida DTO + Bean Validation.</p>
 *
 * @param code  menu code (foreign key to {@code menu.code})
 * @param order yangi order_number (0-based)
 */
public record FavoriteReorderItem(
        @NotBlank @Size(max = 100) String code,
        @Min(0) int order
) {}
