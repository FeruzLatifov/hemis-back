package uz.hemis.domain.entity.enums;

/**
 * University founder type — matches DB CHECK constraint on {@code university_founder.founder_type}.
 *
 * <p>Stored as {@link jakarta.persistence.EnumType#STRING}: {@code 'INDIVIDUAL'} | {@code 'LEGAL'}.</p>
 */
public enum FounderType {
    /** Jismoniy shaxs (individual) — linked via employee_id. */
    INDIVIDUAL,
    /** Yuridik shaxs (legal entity) — linked via organization_id. */
    LEGAL
}
