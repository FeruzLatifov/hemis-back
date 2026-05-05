package uz.hemis.domain.entity.university;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uz.hemis.domain.entity.base.AuditableEntityNoSoftDelete;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * University Cadastre — real estate objects from cadastre API.
 *
 * <p><strong>Source:</strong> 172.18.9.171/kadastr/by-inn + /kadastr/by-cadnum</p>
 * <p><strong>Table:</strong> university_cadastre (1:N with university)</p>
 *
 * <p>Extends {@link AuditableEntityNoSoftDelete} — no soft delete by design.
 * Records are API snapshots updated in-place via upsert on cad_number.</p>
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
@ToString(exclude = {"apiRawResponse", "subjects", "documents", "documentsL", "bans"})
public class UniversityCadastre extends AuditableEntityNoSoftDelete {

    private static final long serialVersionUID = 1L;

    @Column(name = "university_code", nullable = false)
    private String universityCode;

    /** Unique cadastre number, e.g. "10:10:02:03:03:5010" */
    @Column(name = "cad_number", nullable = false, unique = true, length = 50)
    private String cadNumber;

    @Column(name = "cad_number_old", length = 50)
    private String cadNumberOld;

    // =====================================================
    // Location (kadastr API numbering — not SOATO)
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

    @Column(name = "type_code", length = 10)
    private String typeCode;

    @Column(name = "type_name")
    private String typeName;

    @Column(name = "kind_code", length = 10)
    private String kindCode;

    @Column(name = "kind_name")
    private String kindName;

    // =====================================================
    // Land Area (sq meters) — see table COMMENTS for meaning of _i/_b/_f/_z/_d/_u
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
    // Object Area (sq meters) — see table COMMENTS for _l/_u
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
    // Value / Status
    // =====================================================

    /** Cadastre value in UZS */
    @Column(name = "cost")
    private Long cost;

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

    @Override
    public String toString() {
        return "UniversityCadastre{" +
                "id=" + getId() +
                ", universityCode='" + universityCode + '\'' +
                ", cadNumber='" + cadNumber + '\'' +
                ", region='" + region + '\'' +
                '}';
    }
}
