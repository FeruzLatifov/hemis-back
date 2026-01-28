package uz.hemis.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Role Code Enum - System Role Identifiers
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Type-safe role codes (no magic strings)</li>
 *   <li>Central definition for all roles</li>
 *   <li>IDE autocomplete support</li>
 *   <li>Refactoring-friendly</li>
 * </ul>
 *
 * <p><strong>Role Types:</strong></p>
 * <ul>
 *   <li>SYSTEM - Built-in roles (cannot be deleted)</li>
 *   <li>UNIVERSITY - University-scoped roles</li>
 *   <li>CUSTOM - User-defined roles</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum RoleCode {

    // =====================================================
    // System Roles (SYSTEM)
    // =====================================================

    /**
     * Super Administrator - Full system access (Ministry level)
     * <p>Type: SYSTEM</p>
     * <p>Scope: All universities</p>
     */
    SUPER_ADMIN("SUPER_ADMIN", "Super Administrator", RoleType.SYSTEM),

    /**
     * Ministry Administrator - Ministry-level administrator
     * <p>Type: SYSTEM</p>
     * <p>Scope: Ministry operations</p>
     */
    MINISTRY_ADMIN("MINISTRY_ADMIN", "Ministry Administrator", RoleType.SYSTEM),

    /**
     * Viewer - Read-only access
     * <p>Type: SYSTEM</p>
     * <p>Scope: System-wide read-only</p>
     */
    VIEWER("VIEWER", "Viewer", RoleType.SYSTEM),

    // =====================================================
    // University Roles (UNIVERSITY)
    // =====================================================

    /**
     * University Administrator - University-level administrator
     * <p>Type: UNIVERSITY</p>
     * <p>Scope: Single university</p>
     */
    UNIVERSITY_ADMIN("UNIVERSITY_ADMIN", "University Administrator", RoleType.UNIVERSITY),

    // =====================================================
    // Custom Roles (CUSTOM)
    // =====================================================

    /**
     * Report Viewer - Read-only access for reports
     * <p>Type: CUSTOM</p>
     * <p>Scope: Reporting module</p>
     */
    REPORT_VIEWER("REPORT_VIEWER", "Report Viewer", RoleType.CUSTOM);

    // =====================================================
    // Fields
    // =====================================================

    /**
     * Role code (machine-readable identifier)
     * <p>Example: "SUPER_ADMIN", "UNIVERSITY_ADMIN"</p>
     */
    private final String code;

    /**
     * Human-readable name
     * <p>Example: "Super Administrator", "University Administrator"</p>
     */
    private final String displayName;

    /**
     * Role type (SYSTEM, UNIVERSITY, CUSTOM)
     */
    private final RoleType type;

    // =====================================================
    // Utility Methods
    // =====================================================

    /**
     * Find RoleCode by code string
     *
     * @param code Role code (e.g., "SUPER_ADMIN")
     * @return RoleCode enum or null if not found
     */
    public static RoleCode fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        
        for (RoleCode roleCode : values()) {
            if (roleCode.code.equals(code)) {
                return roleCode;
            }
        }
        
        return null;
    }

    /**
     * Check if this is a system role (cannot be deleted)
     *
     * @return true if type is SYSTEM
     */
    public boolean isSystemRole() {
        return type == RoleType.SYSTEM;
    }

    /**
     * Check if this is a university role
     *
     * @return true if type is UNIVERSITY
     */
    public boolean isUniversityRole() {
        return type == RoleType.UNIVERSITY;
    }

    /**
     * Check if this is a custom role
     *
     * @return true if type is CUSTOM
     */
    public boolean isCustomRole() {
        return type == RoleType.CUSTOM;
    }

    @Override
    public String toString() {
        return code;
    }
}
