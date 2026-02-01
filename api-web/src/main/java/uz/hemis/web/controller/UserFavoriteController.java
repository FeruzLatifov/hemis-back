package uz.hemis.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.UserFavorite;
import uz.hemis.service.favorite.UserFavoriteService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * User Favorites Controller - Quick Access Menu Items
 *
 * <p><strong>Endpoints:</strong></p>
 * <ul>
 *   <li>GET    /api/v1/web/favorites        - Foydalanuvchi favoritlarini olish</li>
 *   <li>POST   /api/v1/web/favorites        - Favorite qo'shish</li>
 *   <li>DELETE  /api/v1/web/favorites/{code} - Favorite o'chirish</li>
 *   <li>PATCH  /api/v1/web/favorites/reorder - Favoritlar tartibini o'zgartirish</li>
 * </ul>
 *
 * <p><strong>Security:</strong></p>
 * <ul>
 *   <li>JWT Bearer token required for all endpoints</li>
 *   <li>User ID extracted from JWT subject (UUID)</li>
 *   <li>Users can only access their own favorites</li>
 * </ul>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/web/favorites")
@Tag(
    name = "Favorites",
    description = """
        Foydalanuvchi tez havolalari (favorites) API

        **Features:**
        - Foydalanuvchi sevimli menyu elementlarini saqlash
        - Tartibni o'zgartirish (drag-and-drop)
        - Maksimum 10 ta favorite
        - JWT orqali foydalanuvchi identifikatsiyasi

        **Use Case:**
        - Sidebar'da tez havolalar paneli
        - Foydalanuvchiga qulay navigatsiya
        """
)
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class UserFavoriteController {

    private final UserFavoriteService favoriteService;

    // =====================================================
    // GET - List Favorites
    // =====================================================

    @GetMapping
    @Operation(
        summary = "Foydalanuvchi favoritlarini olish",
        description = """
            Joriy foydalanuvchining barcha favorite menyu elementlarini qaytaradi.

            **Tartib:** order_number bo'yicha (kichikdan kattaga)

            **Example Response:**
            ```json
            [
              {
                "id": "...",
                "userId": "...",
                "menuCode": "dashboard",
                "orderNumber": 0,
                "createdAt": "2025-01-15T10:30:00"
              },
              {
                "id": "...",
                "userId": "...",
                "menuCode": "registry-e-reestr",
                "orderNumber": 1,
                "createdAt": "2025-01-15T10:35:00"
              }
            ]
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Favoritlar muvaffaqiyatli qaytarildi",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserFavorite[].class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Token topilmadi yoki yaroqsiz"
        )
    })
    public ResponseEntity<List<UserFavorite>> getFavorites(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        log.debug("GET /api/v1/web/favorites - userId: {}", userId);

        List<UserFavorite> favorites = favoriteService.getUserFavorites(userId);
        return ResponseEntity.ok(favorites);
    }

    // =====================================================
    // POST - Add Favorite
    // =====================================================

    @PostMapping
    @Operation(
        summary = "Favorite qo'shish",
        description = """
            Menyu elementini foydalanuvchi favoritlariga qo'shadi.

            **Cheklovlar:**
            - Maksimum 10 ta favorite
            - Bir xil menuCode qayta qo'shib bo'lmaydi

            **Example Request:**
            ```json
            {
              "menuCode": "dashboard"
            }
            ```

            **Example Response:**
            ```json
            {
              "id": "...",
              "userId": "...",
              "menuCode": "dashboard",
              "orderNumber": 0,
              "createdAt": "2025-01-15T10:30:00"
            }
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Favorite muvaffaqiyatli qo'shildi",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserFavorite.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Menyu allaqachon favoritlarda yoki limit to'lgan",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"error\": \"Menu item already in favorites\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Token topilmadi yoki yaroqsiz"
        )
    })
    public ResponseEntity<?> addFavorite(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Map<String, String> body
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        String menuCode = body.get("menuCode");
        log.debug("POST /api/v1/web/favorites - userId: {}, menuCode: {}", userId, menuCode);

        if (menuCode == null || menuCode.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "menuCode is required"));
        }

        try {
            UserFavorite favorite = favoriteService.addFavorite(userId, menuCode);
            return ResponseEntity.ok(favorite);
        } catch (IllegalArgumentException e) {
            log.warn("Add favorite failed - duplicate: userId: {}, menuCode: {}", userId, menuCode);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("Add favorite failed - limit reached: userId: {}", userId);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // =====================================================
    // DELETE - Remove Favorite
    // =====================================================

    @DeleteMapping("/{code}")
    @Operation(
        summary = "Favorite o'chirish",
        description = """
            Menyu elementini foydalanuvchi favoritlaridan o'chiradi.

            **Idempotent:** Agar favorite mavjud bo'lmasa, xato qaytarmaydi.

            **Example:**
            ```
            DELETE /api/v1/web/favorites/dashboard
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Favorite muvaffaqiyatli o'chirildi"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Token topilmadi yoki yaroqsiz"
        )
    })
    public ResponseEntity<Void> removeFavorite(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String code
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        log.debug("DELETE /api/v1/web/favorites/{} - userId: {}", code, userId);

        favoriteService.removeFavorite(userId, code);
        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // PATCH - Reorder Favorites
    // =====================================================

    @PatchMapping("/reorder")
    @Operation(
        summary = "Favoritlar tartibini o'zgartirish",
        description = """
            Foydalanuvchi favoritlarining tartibini yangilaydi.

            **Use Case:** Drag-and-drop orqali tartibni o'zgartirish

            **Example Request:**
            ```json
            [
              {"code": "registry-e-reestr", "order": 0},
              {"code": "dashboard", "order": 1},
              {"code": "rating-university", "order": 2}
            ]
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Tartib muvaffaqiyatli yangilandi"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Token topilmadi yoki yaroqsiz"
        )
    })
    public ResponseEntity<Void> reorderFavorites(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody List<UserFavoriteService.FavoriteOrderItem> items
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        log.debug("PATCH /api/v1/web/favorites/reorder - userId: {}, items: {}", userId, items.size());

        favoriteService.reorderFavorites(userId, items);
        return ResponseEntity.noContent().build();
    }
}
