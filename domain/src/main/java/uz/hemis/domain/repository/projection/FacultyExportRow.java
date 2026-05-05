package uz.hemis.domain.repository.projection;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Spring Data interface projection — faculty export row (CSV/Excel).
 *
 * @since 2.5.0
 */
public interface FacultyExportRow {
    UUID getId();
    String getCode();
    String getNameuz();
    String getShortname();
    String getUniversityname();
    Boolean getActive();
    LocalDateTime getCreatedat();
}
