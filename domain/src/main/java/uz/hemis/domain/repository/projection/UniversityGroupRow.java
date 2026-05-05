package uz.hemis.domain.repository.projection;

/**
 * Spring Data interface projection — university tree root level row.
 *
 * <p>PostgreSQL unquoted column aliases lowercase'ga normallashtirilgan,
 * shuning uchun getter nomlari (e.g. {@code getUniversityid}) lowercase
 * column'ga aniq mos kelishi kerak.</p>
 *
 * <p>Avval {@code Map<String, Object>} qaytarilardi → endi type-safe getter chain.</p>
 *
 * @since 2.5.0
 */
public interface UniversityGroupRow {
    String getUniversityid();
    String getUniversityname();
    Long getFacultycount();
    Long getActivecount();
    Long getInactivecount();
}
