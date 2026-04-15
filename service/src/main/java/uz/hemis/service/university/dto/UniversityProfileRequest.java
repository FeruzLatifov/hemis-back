package uz.hemis.service.university.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Upsert payload for university profile (PUT).
 *
 * <p>All fields optional — nulls clear the corresponding column. Documents list replaces the
 * existing JSONB array on every call (idempotent).</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UniversityProfileRequest {

    @Size(max = 50)
    private String phone;

    @Email
    @Size(max = 255)
    private String email;

    private String description;

    @Size(max = 500)
    private String logoKey;

    @Valid
    private SocialLinksDto socialLinks;

    @Valid
    private List<DocumentMetaDto> documents;

    private String mapUrl;

    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    private BigDecimal longitude;
}
