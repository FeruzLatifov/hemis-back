package uz.hemis.domain.repository.projection;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Spring Data interface projection — faculty detail with university name.
 *
 * @since 2.5.0
 */
public interface FacultyDetailRow {
    UUID getId();
    String getCode();
    String getName();
    String getShortname();
    String getUniversitycode();
    String getUniversityname();
    String getFacultytype();
    Boolean getActive();
    LocalDateTime getCreatedat();
    String getCreatedby();
    LocalDateTime getUpdatedat();
    String getUpdatedby();
}
