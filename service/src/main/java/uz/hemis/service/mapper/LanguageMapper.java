package uz.hemis.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import uz.hemis.common.dto.LanguageDto;
import uz.hemis.domain.entity.Language;

import java.util.List;

/**
 * Language Mapper - Entity → DTO conversion
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface LanguageMapper {

    @Mapping(target = "canDisable", expression = "java(!entity.isSystemDefault())")
    LanguageDto toDto(Language entity);

    List<LanguageDto> toDtoList(List<Language> entities);
}
