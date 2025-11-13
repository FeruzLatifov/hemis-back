package uz.hemis.service.dashboard.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DashboardResponse {
    private LocalDateTime timestamp;
    private OverviewStatsDto overview;
    private StudentStatsDto students;
    private List<EducationTypeDto> educationTypes;
    private List<TopUniversityDto> topUniversities;
    private List<RecentActivityDto> recentActivities;
}
