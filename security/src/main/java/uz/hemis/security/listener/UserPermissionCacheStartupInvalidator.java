package uz.hemis.security.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uz.hemis.security.service.UserPermissionCacheService;

/**
 * Startup'da security-layer {@code user:permissions:*} Redis cache'ini BIR MARTA tozalaydi.
 *
 * <p><strong>Nega:</strong> {@link UserPermissionCacheService} JWT authorization uchun har foydalanuvchi
 * ruxsatlarini {@code user:permissions:{userId}} xom Redis kaliti (1 soat TTL) sifatida keshlaydi — bu
 * kesh {@code JwtGrantedAuthoritiesConverter} orqali har so'rovда {@code @PreAuthorize} tekshiruvини gate
 * qiladi. Kesh <em>shared</em> Redis'да yashaydi va app restart'idan omon qoladi. Ruxsatlar Liquibase
 * seed (S### permission) bilan o'zgartirilsa — seed plain SQL bo'lib, ishlab turgan app'ga xabar bera
 * olmaydi. Natijada deploy/seed'дан keyin app 1 soat (TTL) davomida ESKI ruxsatlarни beradi: yangi grant
 * ko'rinmaydi → foydalanuvchi to'g'ri grant'ga ega bo'lsa ham <strong>403</strong>.</p>
 *
 * <p>{@code service.cache.ReferenceCacheStartupInvalidator} Spring-managed {@code userPermissions} cache'ini
 * (menu/i18n bilan) startup'да tozalaydi, lekin bu SECURITY-layer xom kesh ALOHIDA mexanizm — uni
 * {@code :service} moduldan chaqirib bo'lmaydi ({@code :service} {@code :security}'ga bog'lanmaydi). Shu
 * invalidator o'sha bo'shliqni yopadi: authorization uchun aynan ishlatiladigan kesh har deploy'да
 * yangilanadi, ya'ni seed bilan qo'shilgan permission darhol jonli bo'ladi (qo'l aralashuvsiz).</p>
 *
 * <p>{@link ApplicationReadyEvent}'да (Liquibase barcha changeset'larni qo'llagandan keyin) ishlaydi;
 * kesh keyingi so'rovда DB'дан lazily qayta yuklanadi (bir necha ms). Ko'p-pod deployда har pod o'z
 * startup'ida chaqiradi — idempotent (shared kalitlarни qayta o'chirish zararsiz).</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserPermissionCacheStartupInvalidator {

    private final UserPermissionCacheService userPermissionCacheService;

    @EventListener(ApplicationReadyEvent.class)
    public void invalidatePermissionCacheOnStartup() {
        try {
            userPermissionCacheService.clearAllCaches();
            log.info("Startup: security user-permission cache (user:permissions:*) invalidated — "
                    + "seed/deploy permission changes are served fresh on the first request.");
        } catch (Exception e) {
            // Startup'ni cache-tozalash nosozligi bilan bloklamaymiz (Redis vaqtincha yo'q bo'lsa);
            // eng yomon holat — 1 soat TTL tugaguncha eski kesh (admin "clear cache" tugmasi ham tuzatadi).
            log.warn("Startup security permission-cache invalidation failed (non-fatal): {}", e.toString());
        }
    }
}
