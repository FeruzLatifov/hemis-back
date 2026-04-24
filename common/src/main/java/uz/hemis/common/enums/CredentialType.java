package uz.hemis.common.enums;

/**
 * Discriminator for {@code employee_academic_credential} (STI — Single Table Inheritance).
 *
 * <p>One employee may hold many credentials of either type:</p>
 * <ul>
 *   <li>{@link #DEGREE} — ilmiy daraja (PhD, DSc, Fan nomzodi) — diploma-based academic degree</li>
 *   <li>{@link #TITLE}  — ilmiy unvon (Doцent, Professor)     — academic rank/title</li>
 * </ul>
 *
 * <p>Pattern: Martin Fowler PEAA — Single Table Inheritance. Classifier FK differs per type:
 * DEGREE → {@code hemishe_h_academic_degree}, TITLE → {@code hemishe_h_academic_rank}.</p>
 *
 * @since 2.1.0
 */
public enum CredentialType {

    /** Ilmiy daraja — PhD, DSc, Fan nomzodi (diploma-based). */
    DEGREE,

    /** Ilmiy unvon — Doцent, Professor (rank-based). */
    TITLE
}
