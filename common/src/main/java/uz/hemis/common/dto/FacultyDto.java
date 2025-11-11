package uz.hemis.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * Faculty DTO - API Response/Request for Faculty
 *
 * <p><strong>CRITICAL - Legacy JSON Field Names:</strong></p>
 * <p>All field names with underscore prefixes MUST be preserved for backward compatibility
 * with 200+ universities using the old CUBA Platform API.</p>
 *
 * <p><strong>JSON Field Mapping (FROZEN - NO CHANGES ALLOWED):</strong></p>
 * <ul>
 *   <li>_university → _university (NOT university)</li>
 *   <li>_faculty_type → _faculty_type (NOT facultyType)</li>
 * </ul>
 *
 * <p><strong>Audit Fields:</strong> NOT included in DTO (internal only)</p>
 *
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacultyDto implements Serializable {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Primary Key
    // =====================================================

    /**
     * Faculty ID (UUID)
     * JSON: "id"
     */
    @JsonProperty("id")
    private UUID id;

    // =====================================================
    // Business Fields
    // =====================================================

    /**
     * Faculty code
     * JSON: "code"
     */
    @JsonProperty("code")
    private String code;

    /**
     * Faculty name
     * JSON: "name"
     */
    @JsonProperty("name")
    private String name;

    /**
     * Short name
     * JSON: "short_name"
     */
    @JsonProperty("short_name")
    private String shortName;

    // =====================================================
    // University Reference (LEGACY FIELD NAME WITH _)
    // =====================================================

    /**
     * University code
     * JSON: "_university" (NOT "university")
     *
     * CRITICAL: Underscore prefix MUST be preserved!
     * References: hemishe_e_university.code (VARCHAR PK)
     */
    @JsonProperty("_university")
    private String university;

    // =====================================================
    // Classifiers (LEGACY FIELD NAMES WITH _)
    // =====================================================

    /**
     * Faculty type code
     * JSON: "_faculty_type" (NOT "facultyType")
     *
     * CRITICAL: Underscore prefix MUST be preserved!
     */
    @JsonProperty("_faculty_type")
    private String facultyType;

    // =====================================================
    // Boolean Flags
    // =====================================================

    /**
     * Active flag
     * JSON: "active"
     */
    @JsonProperty("active")
    private Boolean active;
}
