package uz.hemis.domain.repository.projection;

import java.util.UUID;

/**
 * Spring Data interface projection — faculty list row (lazy children of university group).
 *
 * @since 2.5.0
 */
public interface FacultyRow {
    UUID getId();
    String getCode();
    String getNameuz();
    String getNameru();
    String getShortname();
    String getUniversityid();
    Boolean getActive();
}
