package uz.hemis.service.mapper;

import org.mapstruct.*;
import uz.hemis.domain.entity.Menu;
import uz.hemis.service.menu.dto.MenuAdminRequest;
import uz.hemis.service.menu.dto.MenuAdminResponse;

import java.util.List;

/**
 * Menu Mapper - MapStruct Entity ↔ DTO conversion
 *
 * <p><strong>MapStruct Configuration:</strong></p>
 * <ul>
 *   <li>componentModel = "spring" - generates Spring @Component</li>
 *   <li>unmappedTargetPolicy = WARN - warn about unmapped fields</li>
 *   <li>Auto-generated implementation at compile time</li>
 * </ul>
 *
 * <p><strong>Field Mapping:</strong></p>
 * <ul>
 *   <li>Entity → Response: includes all fields + audit trail</li>
 *   <li>Request → Entity: maps user input only (no audit fields)</li>
 *   <li>Hierarchical children handled separately in service layer</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.WARN,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface MenuMapper {

    // =====================================================
    // Entity → DTO (Response)
    // =====================================================

    /**
     * Convert Menu entity to MenuAdminResponse
     *
     * <p>Maps all fields including audit trail and hierarchical structure</p>
     *
     * @param menu entity
     * @return response DTO
     */
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentCode", source = "parent.code")
    @Mapping(target = "children", ignore = true) // handled separately in service
    MenuAdminResponse toResponse(Menu menu);

    /**
     * Convert list of Menu entities to list of MenuAdminResponse
     *
     * @param menus list of entities
     * @return list of response DTOs
     */
    List<MenuAdminResponse> toResponseList(List<Menu> menus);

    // =====================================================
    // DTO (Request) → Entity
    // =====================================================

    /**
     * Convert MenuAdminRequest to Menu entity
     *
     * <p>Maps user input only. Audit fields and parent entity handled in service layer</p>
     *
     * @param request DTO
     * @return entity
     */
    @Mapping(target = "id", ignore = true) // auto-generated UUID
    @Mapping(target = "parent", ignore = true) // set in service layer
    @Mapping(target = "children", ignore = true) // managed separately
    @Mapping(target = "createdAt", ignore = true) // set by JPA
    @Mapping(target = "updatedAt", ignore = true) // set by JPA
    @Mapping(target = "createdBy", ignore = true) // set by audit listener
    @Mapping(target = "updatedBy", ignore = true) // set by audit listener
    @Mapping(target = "deletedAt", ignore = true) // soft delete management
    Menu toEntity(MenuAdminRequest request);

    // =====================================================
    // Update existing entity from DTO
    // =====================================================

    /**
     * Update existing Menu entity from MenuAdminRequest
     *
     * <p>Updates only fields present in request, preserves null values</p>
     * <p>Audit fields and parent relationship handled in service layer</p>
     *
     * @param request DTO with updated values
     * @param menu existing entity to update
     */
    @Mapping(target = "id", ignore = true) // never update ID
    @Mapping(target = "parent", ignore = true) // set in service layer
    @Mapping(target = "children", ignore = true) // managed separately
    @Mapping(target = "createdAt", ignore = true) // never update creation time
    @Mapping(target = "updatedAt", ignore = true) // set by JPA
    @Mapping(target = "createdBy", ignore = true) // never update creator
    @Mapping(target = "updatedBy", ignore = true) // set by audit listener
    @Mapping(target = "deletedAt", ignore = true) // soft delete management
    void updateEntityFromRequest(MenuAdminRequest request, @MappingTarget Menu menu);

    // =====================================================
    // Helper Methods (Default implementations)
    // =====================================================

    /**
     * After mapping entity to response, set computed fields
     *
     * <p>This runs automatically after toResponse() is called</p>
     *
     * @param menu source entity
     * @param response target DTO
     */
    @AfterMapping
    default void afterMappingToResponse(Menu menu, @MappingTarget MenuAdminResponse response) {
        // Computed fields are handled by @Schema methods in MenuAdminResponse
        // No additional logic needed here
    }
}
