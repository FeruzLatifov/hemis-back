package uz.hemis.service.university.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.hemis.domain.entity.UniversityLifecycle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lifecycle event DTO — response shape for timeline/history views.
 *
 * <p>Decouples the API contract from JPA internals (no entity leaking to clients).</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UniversityLifecycleDto {
    private UUID id;
    private String universityCode;
    private String eventType;
    private LocalDate eventDate;
    private String successorCode;
    private String decreeNumber;
    private LocalDate decreeDate;
    private Integer studentsCount;
    private Integer employeesCount;
    private String oldName;
    private String newName;
    private String note;
    private LocalDateTime createdAt;
    private String createdBy;

    public static UniversityLifecycleDto from(UniversityLifecycle e) {
        if (e == null) return null;
        return UniversityLifecycleDto.builder()
                .id(e.getId())
                .universityCode(e.getUniversityCode())
                .eventType(e.getEventType())
                .eventDate(e.getEventDate())
                .successorCode(e.getSuccessorCode())
                .decreeNumber(e.getDecreeNumber())
                .decreeDate(e.getDecreeDate())
                .studentsCount(e.getStudentsCount())
                .employeesCount(e.getEmployeesCount())
                .oldName(e.getOldName())
                .newName(e.getNewName())
                .note(e.getNote())
                .createdAt(e.getCreatedAt())
                .createdBy(e.getCreatedBy())
                .build();
    }
}
