package uz.hemis.service.registry;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Regressiya: fakultet/kafedra tafsilotida vaqt ustunlari.
 *
 * <p><strong>Nima bo'lgan edi:</strong> {@code getFacultyDetail} / {@code getDepartmentDetail}
 * native query natijasidagi {@code create_ts} / {@code update_ts} ni to'g'ridan-to'g'ri
 * {@code (java.sql.Timestamp)} deb kastlardi. Hibernate 6 esa {@code LocalDateTime} qaytaradi —
 * natijada har ikkala endpoint 500 berardi va UI'da drawer "Yuklanmoqda..." holatida qotib
 * qolardi (Sentry MINISTRY-HEMIS-BACK-12 / -13).</p>
 *
 * <p>Kast — runtime amali, shuning uchun kompilyator ham, mavjud testlar ham tutmagan.
 * Bu test aynan o'sha tipni ({@code LocalDateTime}) uzatib, regressiyani qaytishdan saqlaydi.</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Registry detail — native query vaqt ustunlari (Hibernate 6 LocalDateTime)")
class RegistryDetailTimestampTest {

    private static final LocalDateTime CREATED = LocalDateTime.of(2024, 3, 1, 9, 15);
    private static final LocalDateTime UPDATED = LocalDateTime.of(2026, 8, 22, 11, 6, 35);

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @InjectMocks
    private FacultyRegistryService facultyService;

    @InjectMocks
    private DepartmentRegistryService departmentService;

    /** Detail so'rovi kutgan 15 ustunli qator — vaqtlar LocalDateTime sifatida. */
    private Object[] row() {
        return new Object[] {
                "387-100", "Fizika fakulteti", "Физический факультет",
                "387", "Toshkent davlat universiteti",
                Boolean.TRUE, "11", "Fakultet", null, "387-100",
                CREATED, "admin", UPDATED, "operator", 3
        };
    }

    private void stubRow() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of((Object) row()));
    }

    @Test
    @DisplayName("fakultet: LocalDateTime kelganda ClassCastException bo'lmaydi")
    void facultyDetailAcceptsLocalDateTime() {
        stubRow();

        var dto = facultyService.getFacultyDetail("387-100");

        assertThat(dto).isPresent();
        assertThat(dto.get().getCreatedAt()).isEqualTo(CREATED);
        assertThat(dto.get().getUpdatedAt()).isEqualTo(UPDATED);
    }

    @Test
    @DisplayName("kafedra: LocalDateTime kelganda ClassCastException bo'lmaydi")
    void departmentDetailAcceptsLocalDateTime() {
        stubRow();

        var dto = departmentService.getDepartmentDetail("387-100");

        assertThat(dto).isPresent();
        assertThat(dto.get().getCreatedAt()).isEqualTo(CREATED);
        assertThat(dto.get().getUpdatedAt()).isEqualTo(UPDATED);
    }

    @Test
    @DisplayName("java.sql.Timestamp ham qabul qilinadi (drayver/versiya farqiga chidamli)")
    void stillAcceptsSqlTimestamp() {
        Object[] r = row();
        r[10] = java.sql.Timestamp.valueOf(CREATED);
        r[12] = java.sql.Timestamp.valueOf(UPDATED);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of((Object) r));

        var dto = facultyService.getFacultyDetail("387-100");

        assertThat(dto).isPresent();
        assertThat(dto.get().getCreatedAt()).isEqualTo(CREATED);
        assertThat(dto.get().getUpdatedAt()).isEqualTo(UPDATED);
    }

    @Test
    @DisplayName("vaqt NULL bo'lsa DTO'da ham null")
    void nullTimestampsStayNull() {
        Object[] r = row();
        r[10] = null;
        r[12] = null;
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of((Object) r));

        var dto = facultyService.getFacultyDetail("387-100");

        assertThat(dto).isPresent();
        assertThat(dto.get().getCreatedAt()).isNull();
        assertThat(dto.get().getUpdatedAt()).isNull();
    }
}
