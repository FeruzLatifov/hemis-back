package uz.hemis.service.infrastructure.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import uz.hemis.common.dto.building.BuildingCreateUpdateDto;
import uz.hemis.common.dto.building.BuildingDto;
import uz.hemis.common.dto.building.BuildingLifecycleDto;
import uz.hemis.common.dto.building.BuildingSyncDto;
import uz.hemis.domain.entity.infrastructure.BuildingLifecycle;
import uz.hemis.domain.entity.infrastructure.UniversityBuilding;

/**
 * Entity ↔ DTO mapping (MapStruct avtomatik generatsiya qiladi).
 *
 * <p>Eslatma: AuditableEntity superclass field'lari (createdAt/By, updatedAt/By,
 * deletedAt/By, version) DTO'da yo'q, shuning uchun @Mapping ignore kerak emas —
 * MapStruct avtomatik skip qiladi. FK entity field'lari esa DTO'dagi *Code'ga mos,
 * ularni ignore qilish kerak (insertable=false, updatable=false).</p>
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        // AuditableEntity audit columns (id, version, createdAt/By, updatedAt/By,
        // deletedAt/By) DTO'da yo'q — MapStruct warning'larini bostiramiz, chunki
        // ular @PrePersist/@PreUpdate orqali boshqariladi.
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BuildingMapper {

    // Entity → Read DTO
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "buildingType.name", target = "buildingTypeName")
    @Mapping(source = "ownership.name", target = "ownershipName")
    @Mapping(source = "constructionMaterial.name", target = "constructionMaterialName")
    @Mapping(source = "roofType.name", target = "roofTypeName")
    BuildingDto toDto(UniversityBuilding entity);

    // Create DTO → new Entity
    @Mapping(target = "universityCode", ignore = true)
    @Mapping(target = "university", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "buildingType", ignore = true)
    @Mapping(target = "ownership", ignore = true)
    @Mapping(target = "constructionMaterial", ignore = true)
    @Mapping(target = "roofType", ignore = true)
    @Mapping(target = "source", constant = "manual")
    @Mapping(target = "sourceUid", ignore = true)
    @Mapping(target = "syncedAt", ignore = true)
    @Mapping(target = "contentHash", ignore = true)
    UniversityBuilding toEntity(BuildingCreateUpdateDto dto);

    // Update DTO → existing Entity (partial — null skip)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "universityCode", ignore = true)
    @Mapping(target = "university", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "buildingType", ignore = true)
    @Mapping(target = "ownership", ignore = true)
    @Mapping(target = "constructionMaterial", ignore = true)
    @Mapping(target = "roofType", ignore = true)
    @Mapping(target = "source", ignore = true)
    @Mapping(target = "sourceUid", ignore = true)
    @Mapping(target = "syncedAt", ignore = true)
    @Mapping(target = "contentHash", ignore = true)
    void updateEntity(BuildingCreateUpdateDto dto, @MappingTarget UniversityBuilding entity);

    // Sync DTO → new Entity (univer push)
    @Mapping(target = "universityCode", ignore = true)
    @Mapping(target = "university", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "buildingType", ignore = true)
    @Mapping(target = "ownership", ignore = true)
    @Mapping(target = "constructionMaterial", ignore = true)
    @Mapping(target = "roofType", ignore = true)
    @Mapping(target = "source", constant = "univer_sync")
    @Mapping(target = "syncedAt", ignore = true)
    @Mapping(target = "contentHash", ignore = true)
    UniversityBuilding toSyncEntity(BuildingSyncDto dto);

    // Sync DTO → existing Entity (change detection'dan keyin qo'llaniladi)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "universityCode", ignore = true)
    @Mapping(target = "university", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "buildingType", ignore = true)
    @Mapping(target = "ownership", ignore = true)
    @Mapping(target = "constructionMaterial", ignore = true)
    @Mapping(target = "roofType", ignore = true)
    @Mapping(target = "source", ignore = true)
    @Mapping(target = "sourceUid", ignore = true)
    @Mapping(target = "syncedAt", ignore = true)
    @Mapping(target = "contentHash", ignore = true)
    void applySyncUpdate(BuildingSyncDto dto, @MappingTarget UniversityBuilding entity);

    // Lifecycle read
    BuildingLifecycleDto toLifecycleDto(BuildingLifecycle entity);
}
