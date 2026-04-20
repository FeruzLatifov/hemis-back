package uz.hemis.service.university.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.hemis.domain.entity.university.UniversityLifecycle;
import uz.hemis.domain.entity.enums.LifecycleEventType;

import java.time.LocalDate;

/**
 * Input DTO for POST /university/{code}/lifecycle.
 *
 * <p>Validated at API boundary — entity is never deserialized directly from HTTP.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UniversityLifecycleRequest {

    @NotNull(message = "Event type is required")
    private LifecycleEventType eventType;

    @NotNull(message = "Event date is required")
    private LocalDate eventDate;

    private String successorCode;

    @Size(max = 100)
    private String decreeNumber;

    private LocalDate decreeDate;

    private Integer studentsCount;
    private Integer employeesCount;

    @Size(max = 1024)
    private String oldName;

    @Size(max = 1024)
    private String newName;

    private String note;

    public UniversityLifecycle toEntity(String universityCode) {
        UniversityLifecycle e = new UniversityLifecycle();
        e.setUniversityCode(universityCode);
        e.setEventType(eventType);
        e.setEventDate(eventDate);
        e.setSuccessorCode(successorCode);
        e.setDecreeNumber(decreeNumber);
        e.setDecreeDate(decreeDate);
        e.setStudentsCount(studentsCount);
        e.setEmployeesCount(employeesCount);
        e.setOldName(oldName);
        e.setNewName(newName);
        e.setNote(note);
        return e;
    }
}
