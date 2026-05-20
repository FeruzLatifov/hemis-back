package uz.hemis.service.registry;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * {@link ClassifierLookupService} unit testlar — in-memory code resolver + SOATO prefix fallback.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClassifierLookupService")
class ClassifierLookupServiceTest {

    @Mock private EntityManager entityManager;
    @Mock private Query query;

    @InjectMocks
    private ClassifierLookupService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        lenient().when(entityManager.createNativeQuery(anyString())).thenReturn(query);
    }

    @Test
    @DisplayName("reload() success — SQL fragment'iga ko'ra mapping yangilanadi")
    @SuppressWarnings("unchecked")
    void reload_populatesAllMaps() {
        // SQL fragment-based — har jadval uchun alohida mock query.
        when(entityManager.createNativeQuery(anyString())).thenAnswer(inv -> {
            String sql = inv.getArgument(0, String.class);
            jakarta.persistence.Query q = org.mockito.Mockito.mock(jakarta.persistence.Query.class);
            java.util.List<Object[]> rows;
            if (sql.contains("hemishe_h_ownership")) {
                rows = java.util.List.<Object[]>of(new Object[]{"01", "Davlat"}, new Object[]{"02", "Nodavlat"});
            } else if (sql.contains("hemishe_h_university_type")) {
                rows = java.util.List.<Object[]>of(new Object[]{"T1", "OTM"});
            } else if (sql.contains("activity_status")) {
                rows = java.util.List.<Object[]>of(new Object[]{"AS1", "Aktiv"});
            } else if (sql.contains("belongs_to")) {
                rows = java.util.List.<Object[]>of(new Object[]{"B1", "Vazirlik"});
            } else if (sql.contains("contract_category")) {
                rows = java.util.List.<Object[]>of(new Object[]{"CC1", "Kontrakt"});
            } else if (sql.contains("version_type")) {
                rows = java.util.List.<Object[]>of(new Object[]{"V1", "Yangi"});
            } else if (sql.contains("soato")) {
                rows = java.util.List.<Object[]>of(
                        new Object[]{"1701", "Toshkent shahar"},
                        new Object[]{"1726", "Chilonzor"});
            } else {
                rows = java.util.List.of();
            }
            when(q.getResultList()).thenReturn(rows);
            return q;
        });

        service.reload();

        assertThat(service.resolveOwnership("01")).isEqualTo("Davlat");
        assertThat(service.resolveOwnership("99")).isNull();
        assertThat(service.resolveType("T1")).isEqualTo("OTM");
        assertThat(service.resolveActivityStatus("AS1")).isEqualTo("Aktiv");
        assertThat(service.resolveBelongsTo("B1")).isEqualTo("Vazirlik");
        assertThat(service.resolveContractCategory("CC1")).isEqualTo("Kontrakt");
        assertThat(service.resolveVersionType("V1")).isEqualTo("Yangi");
        assertThat(service.resolveSoato("1701")).isEqualTo("Toshkent shahar");
    }

    @Test
    @DisplayName("reload() — DB xato bo'lsa, eski snapshot saqlanadi (warn log, silent)")
    void reload_resilient_keepsOldOnError() {
        when(query.getResultList()).thenThrow(new RuntimeException("DB down"));

        // Exception silent caught — bo'sh snapshot saqlanadi
        service.reload();

        assertThat(service.resolveOwnership("01")).isNull();
    }

    @Test
    @DisplayName("Null-safe lookup — barcha resolve* metodlari")
    void nullCode_returnsNull() {
        assertThat(service.resolveOwnership(null)).isNull();
        assertThat(service.resolveType(null)).isNull();
        assertThat(service.resolveActivityStatus(null)).isNull();
        assertThat(service.resolveBelongsTo(null)).isNull();
        assertThat(service.resolveContractCategory(null)).isNull();
        assertThat(service.resolveVersionType(null)).isNull();
        assertThat(service.resolveSoato(null)).isNull();
    }

    @Test
    @DisplayName("SOATO exact match — to'g'ridan-to'g'ri qaytariladi")
    void soato_exactMatch() throws Exception {
        injectSoato(java.util.Map.of(
                "1701", "Toshkent shahar",
                "17012", "Mirzo Ulug'bek tumani"));

        assertThat(service.resolveSoato("1701")).isEqualTo("Toshkent shahar");
        assertThat(service.resolveSoato("17012")).isEqualTo("Mirzo Ulug'bek tumani");
    }

    @Test
    @DisplayName("SOATO prefix fallback — district topilmasa, region qaytariladi")
    void soato_prefixFallback() throws Exception {
        injectSoato(java.util.Map.of("1701", "Toshkent shahar"));

        // 17012 (4-digit prefix 1701) — exact yo'q, fallback to 1701
        assertThat(service.resolveSoato("170199999")).isEqualTo("Toshkent shahar");
    }

    @Test
    @DisplayName("SOATO — 4-digitdan kichik prefix qidirilmaydi (region minimum)")
    void soato_belowMinPrefix_null() throws Exception {
        injectSoato(java.util.Map.of("17", "Toshkent viloyati"));

        // 4-digitdan kichik prefix — null
        assertThat(service.resolveSoato("17XX")).isNull();
    }

    @Test
    @DisplayName("SOATO empty/blank — null")
    void soato_blank_null() {
        assertThat(service.resolveSoato("")).isNull();
        assertThat(service.resolveSoato("   ")).isNull();
    }

    // =====================================================
    // helpers
    // =====================================================

    private void injectSoato(java.util.Map<String, String> data) throws Exception {
        Field f = ClassifierLookupService.class.getDeclaredField("soato");
        f.setAccessible(true);
        f.set(service, data);
    }
}
