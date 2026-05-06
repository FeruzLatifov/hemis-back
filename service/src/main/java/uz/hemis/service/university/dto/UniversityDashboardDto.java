package uz.hemis.service.university.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.hemis.common.dto.building.BuildingDto;

import java.util.List;

/**
 * University Dashboard DTO — aggregates all university info for the admin panel.
 *
 * <p>All nested shapes are DTOs — no JPA entity leakage across API boundary.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityDashboardDto {
    private UniversityLegalDto legal;
    private List<UniversityFounderDto> founders;
    private List<UniversityLifecycleDto> lifecycle;
    private List<BuildingDto> buildings;
    private RectorDto rector;
}
