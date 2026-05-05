package uz.hemis.service.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.building.BuildingSyncDto;
import uz.hemis.common.dto.building.BuildingSyncResult;
import uz.hemis.domain.entity.infrastructure.UniversityBuilding;
import uz.hemis.domain.repository.UniversityBuildingRepository;
import uz.hemis.service.infrastructure.mapper.BuildingMapper;
import uz.hemis.service.security.TenantGuard;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Univer (224 OTM) bulk sync — idempotent upsert by (university_code, source_uid).
 * Content hash orqali o'zgarmagan yozuvlarni skip qiladi (performance).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UniversityBuildingSyncService {

    private final UniversityBuildingRepository repo;
    private final BuildingCadastreAutoFiller autoFiller;
    private final BuildingMapper mapper;
    private final BuildingMetrics metrics;
    private final TenantGuard tenantGuard;

    @Transactional
    @io.micrometer.core.annotation.Timed(value = "buildings.sync.duration",
            description = "Univer bulk sync duration")
    public BuildingSyncResult syncFromUniver(String universityCode, List<BuildingSyncDto> items) {
        // Defense-in-depth: even if controller @PreAuthorize is bypassed (test, refactor),
        // verify caller's JWT university_code claim matches the path variable.
        // STRICT — no admin bypass: building sync is owned by each OTM, ministry should not push.
        tenantGuard.verifyOwnership(universityCode);
        BuildingSyncResult result = BuildingSyncResult.builder().build();
        for (BuildingSyncDto item : items) {
            try {
                upsertOne(universityCode, item);
                result.recordSuccess();
                metrics.recordSyncOutcome(universityCode, "success");
            } catch (Exception e) {
                log.warn("Sync failed: university={}, sourceUid={}, error={}",
                        universityCode, item.getSourceUid(), e.getMessage());
                result.recordFailure(item.getSourceUid(), e.getMessage());
                metrics.recordSyncOutcome(universityCode, "failure");
            }
        }
        log.info("Univer sync done: university={}, total={}, success={}, failed={}",
                universityCode, result.getTotalProcessed(),
                result.getSuccessCount(), result.getFailureCount());
        return result;
    }

    private void upsertOne(String universityCode, BuildingSyncDto dto) {
        String incomingHash = computeHash(dto);
        Optional<UniversityBuilding> existing =
                repo.findByUniversityCodeAndSourceUid(universityCode, dto.getSourceUid());

        if (existing.isPresent()) {
            UniversityBuilding b = existing.get();
            if (Objects.equals(incomingHash, b.getContentHash())) {
                return; // no change — skip
            }
            mapper.applySyncUpdate(dto, b);
            b.setContentHash(incomingHash);
            b.setSyncedAt(LocalDateTime.now());
            // Hibernate dirty-check saves on transaction commit
        } else {
            UniversityBuilding b = mapper.toSyncEntity(dto);
            b.setUniversityCode(universityCode);
            b.setSourceUid(dto.getSourceUid());
            b.setContentHash(incomingHash);
            b.setSyncedAt(LocalDateTime.now());
            autoFiller.autoFill(b);
            repo.save(b);
        }
    }

    /**
     * Sync-relevant field'lar SHA-256 — content change detection.
     * Audit field'lar (created_at, ...) kiritilmaydi (ular har safar o'zgaradi).
     */
    private String computeHash(BuildingSyncDto d) {
        String content = String.join("|",
                str(d.getName()),
                str(d.getCategoryCode()),
                str(d.getAddress()),
                str(d.getYearBuilt()),
                str(d.getFloorCount()),
                str(d.getCapacity()),
                str(d.getTotalArea()),
                str(d.getUsableArea()),
                str(d.getConstructionMaterialCode()),
                str(d.getRoofTypeCode()),
                str(d.getLastRenovationDate()),
                str(d.getLatitude()),
                str(d.getLongitude()),
                str(d.getMapUrl()),
                str(d.getCadNumber()),
                str(d.getNote())
        );
        return sha256Hex(content);
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private static String str(Object o) {
        return o == null ? "" : o.toString();
    }
}
