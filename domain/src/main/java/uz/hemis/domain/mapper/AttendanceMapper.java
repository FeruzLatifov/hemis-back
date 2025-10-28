package uz.hemis.domain.mapper;

import org.mapstruct.*;
import uz.hemis.common.dto.AttendanceDto;
import uz.hemis.domain.entity.Attendance;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AttendanceMapper {
    AttendanceDto toDto(Attendance entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTs", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updateTs", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleteTs", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Attendance toEntity(AttendanceDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTs", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updateTs", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleteTs", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntityFromDto(AttendanceDto dto, @MappingTarget Attendance entity);
}
