package uz.hemis.service.registry;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory code-to-name resolver for university classifier tables and SOATO hierarchy.
 *
 * <p>All tables are tiny (≤ 250 rows total) and rarely change, so we hold a full snapshot
 * in process memory and refresh every 30 minutes. Lookups are O(1).
 *
 * <p>Use this from DTO factories to enrich `*Code` fields with their human-readable `*Name`
 * counterparts in a single place — no per-row JPA fetches, no frontend dictionary joins.
 */
@Service
@Slf4j
public class ClassifierLookupService {

    @PersistenceContext
    private EntityManager entityManager;

    private volatile Map<String, String> ownerships = Map.of();
    private volatile Map<String, String> types = Map.of();
    private volatile Map<String, String> activityStatuses = Map.of();
    private volatile Map<String, String> belongsTo = Map.of();
    private volatile Map<String, String> contractCategories = Map.of();
    private volatile Map<String, String> versionTypes = Map.of();
    private volatile Map<String, String> soato = Map.of();

    @PostConstruct
    public void init() {
        reload();
    }

    @Scheduled(fixedRate = 30 * 60 * 1000L)
    public void reload() {
        try {
            ownerships = load("SELECT code, name FROM hemishe_h_ownership WHERE delete_ts IS NULL");
            types = load("SELECT code, name FROM hemishe_h_university_type WHERE delete_ts IS NULL");
            activityStatuses = load("SELECT code, name FROM hemishe_h_university_activity_status WHERE delete_ts IS NULL");
            belongsTo = load("SELECT code, name FROM hemishe_h_university_belongs_to WHERE delete_ts IS NULL");
            contractCategories = load("SELECT code, name FROM hemishe_h_university_contract_category WHERE delete_ts IS NULL");
            versionTypes = load("SELECT code, name FROM hemishe_h_hemis_version_type WHERE delete_ts IS NULL");
            soato = load("SELECT code, name_uz FROM hemishe_h_soato WHERE delete_ts IS NULL");
            log.info("Classifier cache reloaded: ownerships={}, types={}, soato={}",
                    ownerships.size(), types.size(), soato.size());
        } catch (Exception e) {
            log.warn("Classifier cache reload failed — keeping previous snapshot", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> load(String sql) {
        Query q = entityManager.createNativeQuery(sql);
        List<Object[]> rows = q.getResultList();
        Map<String, String> m = new ConcurrentHashMap<>(rows.size());
        for (Object[] row : rows) {
            String code = (String) row[0];
            String name = (String) row[1];
            if (code != null && name != null) m.put(code, name);
        }
        return m;
    }

    /** Null-safe lookup: returns null if code is null or unknown. */
    public String resolveOwnership(String code) { return code == null ? null : ownerships.get(code); }
    public String resolveType(String code) { return code == null ? null : types.get(code); }
    public String resolveActivityStatus(String code) { return code == null ? null : activityStatuses.get(code); }
    public String resolveBelongsTo(String code) { return code == null ? null : belongsTo.get(code); }
    public String resolveContractCategory(String code) { return code == null ? null : contractCategories.get(code); }
    public String resolveVersionType(String code) { return code == null ? null : versionTypes.get(code); }

    /**
     * SOATO is hierarchical (4-digit region, 7-digit district, 11-digit neighborhood).
     * Returns the exact match, or — if the code itself is missing — the longest known prefix.
     */
    public String resolveSoato(String code) {
        if (code == null || code.isBlank()) return null;
        String exact = soato.get(code);
        if (exact != null) return exact;
        // Fallback to longest known prefix (handles legacy values one digit off).
        for (int len = code.length() - 1; len >= 4; len--) {
            String prefix = code.substring(0, len);
            String name = soato.get(prefix);
            if (name != null) return name;
        }
        return null;
    }
}
