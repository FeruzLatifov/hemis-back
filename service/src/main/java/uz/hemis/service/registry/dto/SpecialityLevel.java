package uz.hemis.service.registry.dto;

/**
 * Speciality level — decides which of the four {@code _speciality_*} UUID columns
 * of {@code hemishe_e_university_attached_speciality} is populated.
 *
 * <p>Exactly one column is written per row; the other three are NULLed.</p>
 *
 * @since 2.0.0
 */
public enum SpecialityLevel {
    BACHELOR,
    MASTER,
    ORDINATURA,
    DOCTORAL
}
