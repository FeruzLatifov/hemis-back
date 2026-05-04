package uz.hemis.common.dto.university;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
    "id", "code", "nameUz", "nameRu", "shortName",
    "universityId", "active"
})
public class FacultyRowDto {
    private UUID id;
    private String code;
    private String nameUz;
    private String nameRu;
    private String shortName;
    private String universityId;
    private Boolean active;
}

