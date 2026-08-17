package uz.hemis.domain.entity.classifier;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * Modern "Ta'lim shakli" (education form) classifier — OUR OWN reference table
 * ({@code h_education_form}), self-contained and NOT the frozen CUBA
 * {@code hemishe_h_education_form} dump. {@code code} = ministry form code
 * (11=Kunduzgi, 12=Kechki, 13=Sirtqi, 16=Masofaviy, ... 23), with a multilingual
 * name (uz/ru/en) that the CUBA table lacked.
 *
 * <p>FK target for {@code university_speciality_attachment.education_form} — the OTM↔speciality
 * attachment picks its form from here (no hard-coded list). Mirrors {@code h_education_year}
 * (V018): a modern {@link ReferenceEntity} classifier we control.</p>
 *
 * @since 2.1.0
 */
@Entity
@Table(name = "h_education_form")
public class HEducationForm extends ReferenceEntity {
}
