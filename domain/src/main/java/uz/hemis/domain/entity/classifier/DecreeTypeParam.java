package uz.hemis.domain.entity.classifier;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

/**
 * CUBA legacy classifier — buyruq turi parametrlari ({@code hemishe_h_decree_type_param}).
 *
 * <p><strong>Old-hemis manba:</strong> {@code com.company.hemishe.entity.HDecreeTypeParam}
 * (CUBA migrations 260427-1, 260427-2, 260505-2).</p>
 *
 * <p><strong>Maqsad:</strong> Buyruq turlarining (decree_type) parametrlari — har bir buyruq
 * turi qaysi parametrlarni o'z ichiga olishini ifodalaydi (masalan: "Talabani o'qishdan
 * chetlashtirish", "Akademik ta'til berish", ...). 28 ta seed qator (CODE 11..38) CUBA
 * tomonidan 224 OTM bazasiga qo'yilgan.</p>
 *
 * <p><strong>FK:</strong> {@code _decree_type} → {@code hemishe_h_decree_type(code)}.</p>
 *
 * @since 2.5.0
 */
@Entity
@Table(name = "hemishe_h_decree_type_param")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class DecreeTypeParam extends LegacyClassifierEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "_decree_type", referencedColumnName = "code")
    private DecreeType decreeType;
}
