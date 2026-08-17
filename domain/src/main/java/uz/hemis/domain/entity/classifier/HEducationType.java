package uz.hemis.domain.entity.classifier;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * Modern "Ta'lim turi" (education type) classifier — OUR OWN reference table
 * ({@code h_education_type}), self-contained and NOT the frozen CUBA
 * {@code hemishe_h_education_type} dump. {@code code} = ministry type code
 * (11=Bakalavr, 12=Magistr, 13=Ordinatura, 14=Doktorantura PhD, 15=Doktorantura DSc),
 * with a multilingual name (uz/ru/en) that the CUBA table lacked.
 *
 * <p>FK target for {@code h_speciality.education_type}. Mirrors {@code h_education_form} /
 * {@code h_education_year}: a modern {@link ReferenceEntity} classifier we control. The speciality
 * classifier currently carries only Bakalavr/Magistr (a CHECK on h_speciality), but the type table
 * holds all five so future levels only need data + a CHECK relax + an FE tab.</p>
 *
 * @since 2.1.0
 */
@Entity
@Table(name = "h_education_type")
public class HEducationType extends ReferenceEntity {
}
