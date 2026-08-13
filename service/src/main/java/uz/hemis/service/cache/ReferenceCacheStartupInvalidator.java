package uz.hemis.service.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Invalidates the versioned/shared reference caches (menu, i18n, permissions) ONCE on application
 * startup.
 *
 * <p><strong>Why:</strong> these caches live in a <em>shared</em> Redis that survives an app restart
 * and is reused across DB rebuilds. Their source data is changed by Liquibase seeds (menu, i18n,
 * permission), which run as plain SQL and cannot notify the running app. So after a deploy, a seed
 * change, or a fresh-DB rebuild the app would keep serving a previous build's menu/i18n/permissions
 * until the 30-minute TTL expires. Admin UI edits already self-invalidate (MenuController /
 * TranslationAdminController → {@link CacheEvictionService}); this closes the seed/rebuild gap so no
 * one has to remember to press a "clear cache" button after a deploy.</p>
 *
 * <p>Runs on {@link ApplicationReadyEvent}, after Liquibase has applied all pending changesets. It
 * only touches the small reference caches (each rebuilds lazily on the first request, a few ms) and
 * publishes cross-pod invalidation via the same {@link CacheEvictionService} the admin actions use.
 * The dashboard/directions warmup targets different caches, so there is no ordering conflict.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReferenceCacheStartupInvalidator {

    private final CacheEvictionService cacheEvictionService;

    @EventListener(ApplicationReadyEvent.class)
    public void invalidateReferenceCachesOnStartup() {
        try {
            cacheEvictionService.evictAllMenus();
            cacheEvictionService.evictAllI18n();
            cacheEvictionService.evictAllPermissions();
            log.info("Startup: reference caches (menu, i18n, permissions) invalidated — a seed/rebuild "
                    + "change is now served fresh on the first request.");
        } catch (Exception e) {
            // Never block startup on a cache-invalidation hiccup (e.g. Redis briefly unavailable);
            // the worst case is a stale reference cache until its TTL, which the admin button also fixes.
            log.warn("Startup reference-cache invalidation failed (non-fatal): {}", e.toString());
        }
    }
}
