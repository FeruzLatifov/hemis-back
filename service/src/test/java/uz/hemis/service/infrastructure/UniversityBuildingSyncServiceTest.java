package uz.hemis.service.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.hemis.common.dto.building.BuildingSyncDto;
import uz.hemis.common.dto.building.BuildingSyncResult;
import uz.hemis.domain.entity.infrastructure.UniversityBuilding;
import uz.hemis.domain.repository.UniversityBuildingRepository;
import uz.hemis.service.infrastructure.mapper.BuildingMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * {@link UniversityBuildingSyncService} unit testlar — idempotency ustida.
 *
 * <p>Muhim kategoriyalar:
 * <ul>
 *   <li>Yangi sourceUid → INSERT</li>
 *   <li>Bir xil hash bilan mavjud → SKIP (no DB write)</li>
 *   <li>Farqli hash bilan mavjud → UPDATE (dirty check)</li>
 *   <li>Bir item fail bo'lsa qolgani davom etadi</li>
 * </ul></p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UniversityBuildingSyncService Unit Tests")
class UniversityBuildingSyncServiceTest {

    @Mock
    private UniversityBuildingRepository repo;

    @Mock
    private BuildingCadastreAutoFiller autoFiller;

    @Mock
    private BuildingMapper mapper;

    @Mock
    private BuildingMetrics metrics;

    @InjectMocks
    private UniversityBuildingSyncService service;

    private BuildingSyncDto syncDto;

    @BeforeEach
    void setUp() {
        syncDto = BuildingSyncDto.builder()
                .sourceUid("univer-bld-123")
                .name("Asosiy korpus")
                .categoryCode("ACADEMIC")
                .yearBuilt(2010)
                .build();
    }

    @Test
    @DisplayName("Yangi sourceUid — INSERT qilinadi")
    void sync_whenNew_insertsBuilding() {
        when(repo.findByUniversityCodeAndSourceUid("401", "univer-bld-123"))
                .thenReturn(Optional.empty());
        when(mapper.toSyncEntity(syncDto)).thenReturn(new UniversityBuilding());

        BuildingSyncResult result = service.syncFromUniver("401", List.of(syncDto));

        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailureCount()).isZero();
        verify(repo).save(any(UniversityBuilding.class));
        verify(autoFiller).autoFill(any(UniversityBuilding.class));
    }

    @Test
    @DisplayName("O'zgarmagan sourceUid — SKIP (DB yozuv yo'q)")
    void sync_whenUnchangedHash_skipsUpdate() {
        UniversityBuilding existing = new UniversityBuilding();
        // hash birinchi sync'da hisoblanadi → keyingi sync ham shu hash
        existing.setContentHash(expectedHashFor(syncDto));
        when(repo.findByUniversityCodeAndSourceUid("401", "univer-bld-123"))
                .thenReturn(Optional.of(existing));

        BuildingSyncResult result = service.syncFromUniver("401", List.of(syncDto));

        assertThat(result.getSuccessCount()).isEqualTo(1);
        verify(repo, never()).save(any());
        verify(mapper, never()).applySyncUpdate(any(), any());
    }

    @Test
    @DisplayName("Farqli hash — UPDATE (applySyncUpdate chaqiriladi)")
    void sync_whenHashDiffers_updatesBuilding() {
        UniversityBuilding existing = new UniversityBuilding();
        existing.setContentHash("OLD_DIFFERENT_HASH");
        when(repo.findByUniversityCodeAndSourceUid("401", "univer-bld-123"))
                .thenReturn(Optional.of(existing));

        BuildingSyncResult result = service.syncFromUniver("401", List.of(syncDto));

        assertThat(result.getSuccessCount()).isEqualTo(1);
        verify(mapper).applySyncUpdate(syncDto, existing);
        // Hibernate dirty-check saves — repo.save() CHAQIRILMAYDI
        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("Bir item fail bo'lsa — qolgani davom etadi")
    void sync_withMixedResults_recordsPerItem() {
        BuildingSyncDto good = syncDto;
        BuildingSyncDto bad = BuildingSyncDto.builder()
                .sourceUid("bad-1")
                .name("Xatoli")
                .categoryCode("ACADEMIC")
                .build();

        when(repo.findByUniversityCodeAndSourceUid(anyString(), eq("univer-bld-123")))
                .thenReturn(Optional.empty());
        when(repo.findByUniversityCodeAndSourceUid(anyString(), eq("bad-1")))
                .thenThrow(new RuntimeException("DB error"));
        when(mapper.toSyncEntity(good)).thenReturn(new UniversityBuilding());

        BuildingSyncResult result = service.syncFromUniver("401", List.of(good, bad));

        assertThat(result.getTotalProcessed()).isEqualTo(2);
        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailureCount()).isEqualTo(1);
        assertThat(result.getFailures()).hasSize(1);
        assertThat(result.getFailures().get(0).getSourceUid()).isEqualTo("bad-1");
    }

    /**
     * Test utility — sync service ichidagi hash logic'iga mos ishlab chiqilgan.
     * Har bir test uchun qaytariladigan string content'ni mos hash bilan beradi.
     * Real hash test'dan xabari yo'q — skip test o'zini qayta xato qilishi mumkin.
     */
    private String expectedHashFor(BuildingSyncDto d) {
        // Service computeHash'ni private qilgani sababli — bu yerda content string'ni qayta
        // qurish va hash qilish kerak. Sodda yechim: InvocationOnMock bilan emas, private method'ni
        // reflection bilan chaqirish. Hozircha test uchun oddiy placeholder string ishlatamiz.
        // Real hash equality test'ni integration testda tekshiramiz.
        //
        // NOTE: Bu test SKIP scenariosi uchun — existing.contentHash == incomingHash
        // shuning uchun bu yerda hash service ishlatgan algoritm bilan MOS kelishi kerak.
        // Sodda yondashuv: service'ni test double bilan override qilish.
        return computeSameHashAsService(d);
    }

    /**
     * Service.computeHash() bilan aniq bir xil algoritm (package-private test).
     * NOTE: production kod o'zgarsa, bu ham yangilanishi kerak.
     */
    private String computeSameHashAsService(BuildingSyncDto d) {
        String content = String.join("|",
                safe(d.getName()),
                safe(d.getCategoryCode()),
                safe(d.getAddress()),
                safe(d.getYearBuilt()),
                safe(d.getFloorCount()),
                safe(d.getCapacity()),
                safe(d.getTotalArea()),
                safe(d.getUsableArea()),
                safe(d.getConstructionMaterialCode()),
                safe(d.getRoofTypeCode()),
                safe(d.getLastRenovationDate()),
                safe(d.getLatitude()),
                safe(d.getLongitude()),
                safe(d.getMapUrl()),
                safe(d.getCadNumber()),
                safe(d.getNote())
        );
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private String safe(Object o) {
        return o == null ? "" : o.toString();
    }
}
