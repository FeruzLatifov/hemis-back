package uz.hemis.service.university.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Document metadata — one entry in {@code university_profile.documents} JSONB array.
 *
 * <p>Actual file bytes are stored in MinIO under {@link #fileKey}. This DTO holds metadata only.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentMetaDto {
    /** LICENSE, ACCREDITATION, CHARTER, OTHER */
    private String type;
    private String name;
    private String fileKey;
    private String mimeType;
    private Long size;
    private LocalDate validFrom;
    private LocalDate validTo;
    private String uploadedAt;
}
