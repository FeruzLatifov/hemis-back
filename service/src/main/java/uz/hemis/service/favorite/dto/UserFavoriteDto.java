package uz.hemis.service.favorite.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.hemis.domain.entity.security.UserFavorite;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response payload for user favorite queries.
 *
 * <p>Decouples the API contract from JPA internals — entities never leak to controllers.</p>
 */
@Schema(description = "User favorite item")
public record UserFavoriteDto(
        UUID id,
        UUID userId,
        String menuCode,
        Integer orderNumber,
        LocalDateTime createdAt
) {
    public static UserFavoriteDto from(UserFavorite e) {
        if (e == null) return null;
        return new UserFavoriteDto(
                e.getId(),
                e.getUserId(),
                e.getMenuCode(),
                e.getOrderNumber(),
                e.getCreatedAt()
        );
    }
}
