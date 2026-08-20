package uz.hemis.domain.entity.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uz.hemis.domain.entity.base.AuditableEntityNoSoftDelete;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Kadastr huquqiy snapshot — {@code 172.18.9.171/kadastr/by-cadnum} jonli javobidan.
 *
 * <p>{@code cad_number} bo'yicha BIR MARTA saqlanadi (umumiy fakt — bir jismoniy mulk = bir yozuv).
 * OTM bog'lanish {@link UniversityBuilding#getCadNumber()} orqali (yumshoq, qattiq FK yo'q).
 * {@link #raw} = to'liq xom javob (hech narsa yo'qolmaydi).</p>
 *
 * <p>Snapshot: {@link AuditableEntityNoSoftDelete} (soft-delete yo'q — kadastrda o'chsa
 * {@link #syncedAt} eski qoladi, biz o'chirmaymiz).</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "university_cadastre")
@Getter
@Setter
public class UniversityCadastre extends AuditableEntityNoSoftDelete {

    @Column(name = "cad_number", nullable = false, length = 50)
    private String cadNumber;

    @Column(name = "cad_number_old", length = 50)
    private String cadNumberOld;

    @Column(length = 500)
    private String name;

    @Column(name = "data_source", length = 30)
    private String dataSource;

    @Column(name = "response_id")
    private Long responseId;

    // Joylashuv
    @Column(name = "region_id")   private Integer regionId;
    private String region;
    @Column(name = "district_id") private Integer districtId;
    private String district;
    @Column(columnDefinition = "TEXT") private String address;
    @Column(name = "short_address", length = 500) private String shortAddress;
    @Column(length = 500) private String street;
    @Column(name = "street_code", length = 50) private String streetCode;
    @Column(name = "dom_num", length = 50) private String domNum;
    @Column(name = "kvartira_num", length = 50) private String kvartiraNum;
    private String neighborhood;
    @Column(name = "neighborhood_id", length = 50) private String neighborhoodId;

    // Obyekt turi
    @Column(length = 10) private String tip;
    @Column(name = "tip_text", length = 500) private String tipText;
    @Column(length = 10) private String vid;
    @Column(name = "vid_text", length = 500) private String vidText;
    @Column(name = "object_rooms") private Integer objectRooms;

    // Yer maydoni (7)
    @Column(name = "land_area",   precision = 14, scale = 2) private BigDecimal landArea;
    @Column(name = "land_area_i", precision = 14, scale = 2) private BigDecimal landAreaI;
    @Column(name = "land_area_b", precision = 14, scale = 2) private BigDecimal landAreaB;
    @Column(name = "land_area_f", precision = 14, scale = 2) private BigDecimal landAreaF;
    @Column(name = "land_area_z", precision = 14, scale = 2) private BigDecimal landAreaZ;
    @Column(name = "land_area_d", precision = 14, scale = 2) private BigDecimal landAreaD;
    @Column(name = "land_area_u", precision = 14, scale = 2) private BigDecimal landAreaU;

    // Obyekt maydoni
    @Column(name = "object_area",   precision = 14, scale = 2) private BigDecimal objectArea;
    @Column(name = "object_area_l", precision = 14, scale = 2) private BigDecimal objectAreaL;
    @Column(name = "object_area_u", precision = 14, scale = 2) private BigDecimal objectAreaU;

    // Qiymat
    private Long cost;

    // Huquqiy holat
    @Column(name = "ban_is") private Boolean banIs;
    @Column(name = "eco_zone", length = 50) private String ecoZone;
    @Column(name = "land_fund_type", length = 100) private String landFundType;
    @Column(name = "land_use_type", length = 100) private String landUseType;
    @Column(name = "land_fund_category", length = 100) private String landFundCategory;

    // Nested JSONB (egalar/hujjatlar/cheklovlar)
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb") private String subjects;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb") private String documents;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "documents_l", columnDefinition = "jsonb") private String documentsL;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb") private String bans;

    // To'liq xom javob
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb") private String raw;

    // Fetch chidamlilik
    @Column(name = "fetch_status", nullable = false, length = 20) private String fetchStatus = "COMPLETE";
    @Column(name = "fetch_error", length = 500) private String fetchError;
    @Column(name = "last_fetch_attempt") private LocalDateTime lastFetchAttempt;
    @Column(name = "synced_at") private LocalDateTime syncedAt;
}
