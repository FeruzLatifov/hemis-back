package uz.hemis.service.shared.mapper;

import uz.hemis.common.dto.university.UniversityDto;
import uz.hemis.domain.entity.University;

import java.util.List;

/**
 * University Entity → DTO conversion contract (Port)
 *
 * <p>Shared interface allowing multiple domains to convert University entities
 * to DTOs without depending on each other's internal mappers.</p>
 *
 * <p>Implementation is provided by {@code university.mapper.UniversityMapper}
 * (MapStruct-generated Spring bean).</p>
 *
 * @since 2.0.0
 */
public interface UniversityDtoConverter {

    UniversityDto toDto(University university);

    List<UniversityDto> toDtoList(List<University> universities);
}
