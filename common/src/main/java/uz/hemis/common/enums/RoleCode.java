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
     * Administrator - the ministry's day-to-day administrator (tier 2 of 3).
     * <p>Code renamed from MINISTRY_ADMIN in seed S038 (the ministry runs one admin tier).</p>
     * <p>Type: SYSTEM</p>
     * <p>Scope: everything except the platform/security-critical actions reserved for
     * {@link #SUPER_ADMIN} — role/permission editing, integration secrets, platform settings,
     * raw PINFL and registry deletions. See seed S038.</p>
     */
    ADMIN("ADMIN", "Administrator", RoleType.SYSTEM),

    /**
     * Viewer - Read-only access
     * <p>Type: SYSTEM</p>
     * <p>Scope: System-wide read-only</p>
     */
    VIEWER("VIEWER", "Viewer", RoleType.SYSTEM),

    /**
     * Inspector - Inspection / audit (old-hemis: Inspeksiya)
     * <p>Type: SYSTEM</p>
     * <p>Scope: Ministry-wide inspection / audit</p>
     */
    INSPECTOR("INSPECTOR", "Inspector", RoleType.SYSTEM),

    /**
     * External API - External system integration (student / employee / hokimiyat APIs)
     * <p>Type: SYSTEM</p>
     * <p>Scope: Machine-to-machine external integration</p>
     */
    EXTERNAL_API("EXTERNAL_API", "External API", RoleType.SYSTEM),

    // =====================================================
    // University Roles (UNIVERSITY)
    // =====================================================

    /**
     * University Administrator - University-level administrator
     * <p>Type: UNIVERSITY</p>
     * <p>Scope: Single university</p>
     */
    OTM_API("OTM_API", "University Administrator", RoleType.UNIVERSITY),

    // =====================================================
    // Custom Roles (CUSTOM)
    // =====================================================

    /**
     * Report Viewer - Read-only access for reports
     * <p>Type: CUSTOM</p>
     * <p>Scope: Reporting module</p>
     */
    REPORT_VIEWER("REPORT_VIEWER", "Report Viewer", RoleType.CUSTOM),

    /**
     * Classifier Manager - central staff who view + assign classifiers (h_*) to OTMs (fanout)
     * <p>Type: CUSTOM</p>
     * <p>Scope: Classifier management (view + edit) — unchanged by S038, which gave technical
     * staff their own role ({@link #TECH_STAFF}) rather than re-purposing this one.</p>
     */
    CLASSIFIER_MANAGER("CLASSIFIER_MANAGER", "Classifier Manager", RoleType.CUSTOM),

    /**
     * Technical staff — the "sub-admin" tier (tier 3 of 3), shown to staff as "Texnik xodim".
     * <p>Type: CUSTOM</p>
     * <p>Scope: classifier operator — views, adds and corrects classifier rows and attaches them
     * to OTMs, but cannot approve (approval is what distributes a row to 230 OTMs), delete or
     * restore. The maker in a maker/checker split with {@link #ADMIN} as the checker. Duties widen
     * one permission at a time as work is assigned. Created by seed S038.</p>
     */
    TECH_STAFF("TECH_STAFF", "Texnik xodim", RoleType.CUSTOM);

    // =====================================================
    // Fields
    // =====================================================

    /**
     * Role code (machine-readable identifier)
     * <p>Example: "SUPER_ADMIN", "OTM_API"</p>
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
