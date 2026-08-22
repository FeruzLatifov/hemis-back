package uz.hemis.service.registry;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Regressiya: OTM guruh ro'yxatida `status` filtri.
 *
 * <p><strong>Nima bo'lgan edi:</strong> {@code getFacultyGroups} / {@code getDepartmentGroups}
 * {@code Boolean status} parametrini QABUL QILARDI, lekin uni WHERE'ga hech qachon
 * qo'shmasdi — yagona ishlatilishi {@code log.debug} edi. Natijada UI'dagi "Barchasi /
 * Faol / Nofaol" filtri yuqori darajadagi OTM ro'yxatiga UMUMAN ta'sir qilmasdi:
 * foydalanuvchi "Nofaol" tanlaydi, 262 ta OTM ko'rinadi, lekin qatorni ochganda ichi
 * bo'sh chiqadi (ichki ro'yxat {@code getFacultiesByUniversity} da to'g'ri filtrlanadi).</p>
 *
 * <p>Kompilyator ishlatilmagan parametr uchun ogohlantirmaydi, testlar ham yo'q edi.</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Registry guruh ro'yxati — status filtri WHERE'ga qo'shiladimi")
class RegistryGroupStatusFilterTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @InjectMocks
    private FacultyRegistryService facultyService;

    @InjectMocks
    private DepartmentRegistryService departmentService;

    private List<String> capturedSql() {
        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(entityManager, org.mockito.Mockito.atLeastOnce()).createNativeQuery(sql.capture());
        return sql.getAllValues();
    }

    private void stubCount(long total) {
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(total);
        when(query.getResultList()).thenReturn(List.of());
        when(query.setParameter(anyInt(), any())).thenReturn(query);
    }

    @Test
    @DisplayName("fakultet: status berilsa EXISTS sharti qo'shiladi")
    void facultyGroupsApplyStatus() {
        stubCount(0);

        facultyService.getFacultyGroups(null, Boolean.TRUE, PageRequest.of(0, 20));

        assertThat(capturedSql())
                .as("status filtri WHERE'ga tushishi kerak")
                .anySatisfy(s -> assertThat(s)
                        .contains("EXISTS")
                        .contains("d._deparment_type = '11'")
                        .contains("d.status = ?"));
    }

    @Test
    @DisplayName("kafedra: status berilsa EXISTS sharti qo'shiladi")
    void departmentGroupsApplyStatus() {
        stubCount(0);

        departmentService.getDepartmentGroups(null, Boolean.FALSE, PageRequest.of(0, 20));

        assertThat(capturedSql())
                .anySatisfy(s -> assertThat(s)
                        .contains("EXISTS")
                        .contains("d._deparment_type = '12'")
                        .contains("d.status = ?"));
    }

    @Test
    @DisplayName("status null bo'lsa filtr qo'shilmaydi (Barchasi)")
    void nullStatusAddsNoFilter() {
        stubCount(0);

        facultyService.getFacultyGroups(null, null, PageRequest.of(0, 20));

        assertThat(capturedSql())
                .as("'Barchasi' tanlanganda hech qanday status sharti bo'lmasin")
                .allSatisfy(s -> assertThat(s).doesNotContain("d.status = ?"));
    }
}
