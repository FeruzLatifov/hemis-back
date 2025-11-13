package uz.hemis.service.dashboard.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RecentActivityDto {
    private String type;
    private String action;
    private String name;
    private LocalDateTime time;
}
