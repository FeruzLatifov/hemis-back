package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;
import uz.hemis.domain.entity.base.AuditableEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * University Legal Entity — legal/tax registration data from API
 *
 * <p><strong>Source:</strong> 172.18.9.171/legalentity/</p>
 * <p><strong>Table:</strong> university_legal (1:1 with university)</p>
 *
 * <p>Extends AuditableEntity — soft delete via deleted_at/deleted_by.</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "university_legal")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UniversityLegal extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // University Reference
    // =====================================================

    /**
     * University code — plain column, NOT a JPA relationship.
     * References hemishe_e_university(code) in old-hemis table.
     */
    @Column(name = "university_code", nullable = false, unique = true)
    private String universityCode;

    /**
     * FK to organization registry (TIN UNIQUE).
     * tin column kept as API snapshot even when organization row is missing.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    // =====================================================
    // Company Info
    // =====================================================

    @Column(name = "short_name", length = 500)
    private String shortName;

    @Column(name = "opf")
    private Integer opf;

    @Column(name = "kfs")
    private Integer kfs;

    @Column(name = "tin", length = 20)
    private String tin;

    @Column(name = "oked", length = 20)
    private String oked;

    @Column(name = "soogu", length = 20)
    private String soogu;

    @Column(name = "soogu_registrator", length = 20)
    private String sooguRegistrator;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Column(name = "registration_number", length = 100)
    private String registrationNumber;

    @Column(name = "reregistration_date")
    private LocalDate reregistrationDate;

    @Column(name = "status")
    @Builder.Default
    private Integer status = 0;

    @Column(name = "status_updated")
    private LocalDate statusUpdated;

    @Column(name = "vat_number")
    private Long vatNumber;

    @Column(name = "tax_mode")
    private Integer taxMode;

    @Column(name = "taxpayer_type")
    private Integer taxpayerType;

    @Column(name = "business_type")
    private Integer businessType;

    @Column(name = "business_fund")
    private Long businessFund;

    @Column(name = "business_structure")
    private Integer businessStructure;

    @Column(name = "avg_employees")
    private Integer avgEmployees;

    // =====================================================
    // Billing Address
    // =====================================================

    @Column(name = "billing_country_code")
    private Integer billingCountryCode;

    @Column(name = "billing_soato", length = 20)
    private String billingSoato;

    @Column(name = "billing_street", columnDefinition = "TEXT")
    private String billingStreet;

    @Column(name = "billing_postcode", length = 20)
    private String billingPostcode;

    @Column(name = "billing_cadastre", length = 50)
    private String billingCadastre;

    // =====================================================
    // Shipping Addresses (JSONB)
    // =====================================================

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "shipping_addresses", columnDefinition = "jsonb")
    private String shippingAddresses;

    // =====================================================
    // Director & Accountant
    // =====================================================

    /** Director (rektor) → employee. PINFL, FIO, phone — employee dan JOIN. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "director_employee_id")
    private Employee directorEmployee;

    /** Accountant (buxgalter) → employee. PINFL, FIO, phone — employee dan JOIN. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accountant_employee_id")
    private Employee accountantEmployee;


    // =====================================================
    // Bank Accounts (JSONB)
    // =====================================================

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bank_accounts", columnDefinition = "jsonb")
    private String bankAccounts;

    // =====================================================
    // API Sync Metadata
    // =====================================================

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "api_raw_response", columnDefinition = "jsonb")
    private String apiRawResponse;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;

    // =====================================================
    // Equals & HashCode (based on ID)
    // =====================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UniversityLegal)) return false;
        UniversityLegal that = (UniversityLegal) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "UniversityLegal{" +
                "id=" + getId() +
                ", universityCode='" + universityCode + '\'' +
                ", tin='" + tin + '\'' +
                ", shortName='" + shortName + '\'' +
                '}';
    }
}
