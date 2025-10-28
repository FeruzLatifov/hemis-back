package uz.hemis.admin.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * UniversityUser mapping entity (CUBA custom entity)
 *
 * Maps to: hemishe_university_user table
 * Purpose: Associates system users with specific universities (multi-tenant)
 * Pattern: One user belongs to one university
 */
@Entity
@Table(name = "hemishe_university_user", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UniversityUser {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    /**
     * Associated system user
     * CUBA field: user_id
     * Relationship: One-to-One (each user has one university assignment)
     */
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    /**
     * Assigned university
     * CUBA field: _university (VARCHAR code reference, CUBA pattern)
     * Note: CUBA uses VARCHAR codes instead of foreign keys
     *
     * However, for new Spring Boot API we use proper foreign key
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "_university", referencedColumnName = "code")
    private University university;

    /**
     * Get university ID for easier access
     */
    public String getUniversityId() {
        return university != null ? university.getId() : null;
    }

    /**
     * Get university code
     */
    public String getUniversityCode() {
        return university != null ? university.getCode() : null;
    }

    /**
     * Get university name in specified locale
     */
    public String getUniversityName(String locale) {
        if (university == null) return null;

        return switch (locale) {
            case "ru" -> university.getNameRu();
            case "en" -> university.getNameEn();
            default -> university.getNameUz();
        };
    }
}
