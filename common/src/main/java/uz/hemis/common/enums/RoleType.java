package uz.hemis.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Role Type Enum - Categorization of Roles
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Categorize roles by scope and mutability</li>
 *   <li>Enforce business rules (e.g., SYSTEM roles cannot be deleted)</li>
 *   <li>Support multi-tenancy (UNIVERSITY roles are scoped to a single OTM)</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum RoleType {

    /**
     * System-level roles
     * <p><strong>Characteristics:</strong></p>
     * <ul>
     *   <li>Built-in roles defined at system level</li>
     *   <li>Cannot be deleted or modified</li>
     *   <li>Common across all institutions</li>
     *   <li>Examples: SUPER_ADMIN, MINISTRY_ADMIN, VIEWER</li>
     * </ul>
     */
    SYSTEM("System", "Built-in system roles"),

    /**
     * University-level roles
     * <p><strong>Characteristics:</strong></p>
     * <ul>
     *   <li>Scoped to a single university</li>
     *   <li>Managed by university administrators</li>
     *   <li>Can be customized per institution</li>
     *   <li>Examples: UNIVERSITY_ADMIN, UNIVERSITY_VIEWER</li>
     * </ul>
     */
    UNIVERSITY("University", "University-scoped roles"),

    /**
     * Custom roles
     * <p><strong>Characteristics:</strong></p>
     * <ul>
     *   <li>User-defined roles</li>
     *   <li>Created for specific organizational needs</li>
     *   <li>Can be deleted or modified</li>
     *   <li>Examples: REPORT_VIEWER, DATA_ANALYST</li>
     * </ul>
     */
    CUSTOM("Custom", "User-defined roles");

    /**
     * Human-readable name
     */
    private final String displayName;

    /**
     * Description
     */
    private final String description;

    /**
     * Find RoleType by name
     *
     * @param name RoleType name (e.g., "SYSTEM")
     * @return RoleType enum or null if not found
     */
    public static RoleType fromName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return name();
    }
}
