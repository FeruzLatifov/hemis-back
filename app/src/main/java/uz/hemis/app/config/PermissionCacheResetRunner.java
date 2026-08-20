package uz.hemis.app.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uz.hemis.security.service.UserPermissionCacheService;

/**
 * Drops every cached RBAC permission set once, right after startup.
 *
 * <p><strong>Why:</strong> permissions are deliberately NOT carried in the JWT — they are resolved
 * at login and cached in Redis ({@code user:permissions:{userId}}, 1h TTL). Redis is a separate
 * service that outlives the pod, so a deploy whose Liquibase seed adds or revokes a grant (plain
 * SQL — no application event fires) leaves everyone already logged in on the pre-deploy set for up
 * to an hour: the UI offers the new action and the API answers 403. Grants made through the admin
 * panel are unaffected — they publish {@code UserPermissionsChangedEvent} and the listener evicts.
 * It is the SQL seed path that has no way to signal the cache, and that path runs on every deploy.</p>
 *
 * <p>Clearing at startup pins the reset to the one moment a seed can have changed the grants. Cost
 * is one DB read per active user on their next request (a miss reloads and re-caches), and it is
 * idempotent under a rolling update: each replica clears once, and anything a still-running old
 * replica re-fills is read from the already-migrated database. Redis being down is not fatal —
 * {@link UserPermissionCacheService#clearAllCaches()} swallows its own failures, and a cache miss
 * falls back to the database anyway.</p>
 *
 * <p>Runs in the {@code migrate}-profile Helm job too (the pod that applies the changesets), which
 * is simply the earliest correct moment to do it.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionCacheResetRunner {

    private final UserPermissionCacheService permissionCacheService;

    @EventListener(ApplicationReadyEvent.class)
    public void clearStalePermissionCaches() {
        log.info("Startup: clearing cached RBAC permission sets (a migration seed may have changed grants)");
        try {
            permissionCacheService.clearAllCaches();
        } catch (RuntimeException e) {
            // An ApplicationReadyEvent listener that throws aborts the boot. A cold cache is never
            // worth that: a miss falls back to the database, so the worst case here is one stale
            // permission set until its TTL expires.
            log.error("Startup permission-cache reset failed — continuing: {}", e.getMessage(), e);
        }
    }
}
