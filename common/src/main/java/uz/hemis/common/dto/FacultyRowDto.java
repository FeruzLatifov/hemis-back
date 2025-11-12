package uz.hemis.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyRowDto {
    private UUID id;
    private String code;
    private String nameUz;
    private String nameRu;
    private String shortName;
    private String universityId;
    private Boolean active;
}

