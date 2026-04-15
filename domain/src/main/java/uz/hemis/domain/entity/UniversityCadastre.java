package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * University Cadastre — real estate objects from cadastre API
 *
 * <p><strong>Source:</strong> 172.18.9.171/kadastr/by-inn + /kadastr/by-cadnum</p>
 * <p><strong>Table:</strong> university_cadastre (1:N with university)</p>
 *
 * <p>Does NOT extend ModernBaseEntity — no soft delete.
 * Cadastre records persist permanently.</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "university_cadastre")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UniversityCadastre implements Serializable {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Primary Key
    // =====================================================

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    // =====================================================
    // University Reference
    // =====================================================

    /**
     * University code — plain column, NOT a JPA relationship.
     */
    @Column(name = "university_code", nullable = false)
    private String universityCode;

    // =====================================================
    // Cadastre Identity
    // =====================================================

    /**
     * Unique cadastre number, e.g. "10:10:02:03:03:5010"
     */
    @Column(name = "cad_number", nullable = false, unique = true, length = 50)
    private String cadNumber;

    @Column(name = "cad_number_old", length = 50)
    private String cadNumberOld;

    // =====================================================
    // Location
    // =====================================================

    @Column(name = "region_id")
    private Integer regionId;

    @Column(name = "region")
    private String region;

    @Column(name = "district_id")
    private Integer districtId;

    @Column(name = "district")
    private String district;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "short_address", length = 500)
    private String shortAddress;

    @Column(name = "street", length = 500)
    private String street;

    @Column(name = "street_code", length = 50)
    private String streetCode;

    @Column(name = "dom_num", length = 50)
    private String domNum;

    @Column(name = "neighborhood")
    private String neighborhood;

    @Column(name = "neighborhood_id", length = 50)
    private String neighborhoodId;

    // =====================================================
    // Object Classification
    // =====================================================

    @Column(name = "tip", length = 10)
    private String tip;

    @Column(name = "tip_text")
    private String tipText;

    @Column(name = "vid", length = 10)
    private String vid;

    @Column(name = "vid_text")
    private String vidText;

    // =====================================================
    // Land Area (sq meters)
    // =====================================================

    @Column(name = "land_area", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal landArea = BigDecimal.ZERO;

    @Column(name = "land_area_i", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal landAreaI = BigDecimal.ZERO;

    @Column(name = "land_area_b", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal landAreaB = BigDecimal.ZERO;

    @Column(name = "land_area_f", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal landAreaF = BigDecimal.ZERO;

    @Column(name = "land_area_z", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal landAreaZ = BigDecimal.ZERO;

    @Column(name = "land_area_d", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal landAreaD = BigDecimal.ZERO;

    @Column(name = "land_area_u", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal landAreaU = BigDecimal.ZERO;

    // =====================================================
    // Object Area (sq meters)
    // =====================================================

    @Column(name = "object_area", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal objectArea = BigDecimal.ZERO;

    @Column(name = "object_area_l", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal objectAreaL = BigDecimal.ZERO;

    @Column(name = "object_area_u", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal objectAreaU = BigDecimal.ZERO;

    // =====================================================
    // Value
    // =====================================================

    @Column(name = "cost")
    private Long cost;

    // =====================================================
    // Legal Status
    // =====================================================

    @Column(name = "eco_zone", length = 10)
    private String ecoZone;

    @Column(name = "ban_is")
    @Builder.Default
    private Boolean banIs = false;

    // =====================================================
    // Land Classification
    // =====================================================

    @Column(name = "land_fund_type", length = 50)
    private String landFundType;

    @Column(name = "land_use_type", length = 50)
    private String landUseType;

    @Column(name = "land_fund_category", length = 50)
    private String landFundCategory;

    // =====================================================
    // Nested Data (JSONB)
    // =====================================================

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "subjects", columnDefinition = "jsonb")
    private String subjects;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "documents", columnDefinition = "jsonb")
    private String documents;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "documents_l", columnDefinition = "jsonb")
    private String documentsL;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bans", columnDefinition = "jsonb")
    private String bans;

    // =====================================================
    // API Sync Metadata
    // =====================================================

    @Column(name = "data_source", length = 20)
    private String dataSource;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "api_raw_response", columnDefinition = "jsonb")
    private String apiRawResponse;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;

    // =====================================================
    // Audit Fields
    // =====================================================

    @Version
    @Column(name = "version")
    @Builder.Default
    private Integer version = 1;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    // =====================================================
    // JPA Lifecycle Hooks
    // =====================================================

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (version == null) {
            version = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // =====================================================
    // Equals & HashCode (based on ID)
    // =====================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UniversityCadastre)) return false;
        UniversityCadastre that = (UniversityCadastre) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "UniversityCadastre{" +
                "id=" + id +
                ", universityCode='" + universityCode + '\'' +
                ", cadNumber='" + cadNumber + '\'' +
                ", region='" + region + '\'' +
                '}';
    }
}
