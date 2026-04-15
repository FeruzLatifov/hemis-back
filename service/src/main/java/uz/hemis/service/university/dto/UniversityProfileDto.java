package uz.hemis.service.university.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Public profile of a university — response DTO.
 *
 * <p>Aggregates flat columns + parsed JSONB ({@code social_links}, {@code documents}) into
 * a single API-friendly shape so the frontend never sees raw JSON strings.</p>
 *
 * @see uz.hemis.domain.entity.UniversityProfile
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UniversityProfileDto {
    private String universityCode;
    private String phone;
    private String email;
    private String description;
    private String logoKey;
    private SocialLinksDto socialLinks;
    private List<DocumentMetaDto> documents;

    /** External map link (Google/Yandex Maps) */
    private String mapUrl;
    /** WGS84 coordinates for map rendering */
    private BigDecimal latitude;
    private BigDecimal longitude;
}
