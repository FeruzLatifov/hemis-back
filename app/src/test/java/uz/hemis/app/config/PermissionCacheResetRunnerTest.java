package uz.hemis.app.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uz.hemis.security.service.UserPermissionCacheService;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("PermissionCacheResetRunner")
class PermissionCacheResetRunnerTest {

    private final UserPermissionCacheService cacheService = mock(UserPermissionCacheService.class);
    private final PermissionCacheResetRunner runner = new PermissionCacheResetRunner(cacheService);

    @Test
    @DisplayName("Startup'da keshlangan RBAC to'plamlarini tozalaydi (seed grantni o'zgartirgan bo'lishi mumkin)")
    void clearsPermissionCachesOnStartup() {
        runner.clearStalePermissionCaches();

        verify(cacheService).clearAllCaches();
    }

    @Test
    @DisplayName("Kesh tozalash uzilsa ham startup'ni yiqitmaydi (Redis down = cache miss -> DB fallback)")
    void doesNotFailStartupWhenCacheClearThrows() {
        // The service swallows its own Redis failures; this pins the contract that the runner adds
        // no new failure mode on top of it — a boot must never die over a cold cache.
        doThrow(new RuntimeException("redis down")).when(cacheService).clearAllCaches();

        org.assertj.core.api.Assertions
                .assertThatCode(runner::clearStalePermissionCaches)
                .doesNotThrowAnyException();
    }
}
