package uz.hemis.service.mapper;

import org.mapstruct.*;
import uz.hemis.common.dto.TranslationDto;
import uz.hemis.domain.entity.SystemMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * SystemMessage Mapper - Entity → DTO conversion only
 * (DTO → Entity not needed for translation admin)
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface SystemMessageMapper {

    @Mapping(target = "translations", expression = "java(mapTranslations(entity))")
    TranslationDto toDto(SystemMessage entity);

    /**
     * Map translations collection to Map<language, translation>
     */
    default Map<String, String> mapTranslations(SystemMessage entity) {
        Map<String, String> translationsMap = new HashMap<>();
        if (entity.getTranslations() != null) {
            entity.getTranslations().forEach(trans -> {
                translationsMap.put(trans.getLanguage(), trans.getTranslation());
            });
        }
        return translationsMap;
    }
}
