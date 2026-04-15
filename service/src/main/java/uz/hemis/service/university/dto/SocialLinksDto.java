package uz.hemis.service.university.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Social media links for university public profile.
 *
 * <p>Stored as JSONB in {@code university_profile.social_links}. Null fields omitted from JSON.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SocialLinksDto {
    private String website;
    private String telegram;
    private String instagram;
    private String youtube;
    private String facebook;
    private String twitter;
    private String linkedin;
}
