package uz.hemis.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Rejalashtirilgan job'larni yoqadi — LEKIN migratsiya pod'ida EMAS.
 *
 * <p><strong>Nega alohida klass:</strong> ilgari {@code @EnableScheduling} to'g'ridan-to'g'ri
 * {@code HemisApplication} da turardi, ya'ni {@code migrate} profilida ham yoqilgan edi.
 * Helm {@code pre-upgrade} Job'i ilovani {@code prod,migrate} bilan ko'taradi: Liquibase bean
 * initsializatsiyasida tugaydi, keyin kontekst refresh yakunlanadi (o'shanda scheduler ISHGA
 * TUSHADI), va faqat undan keyin {@code MigrationExitRunner} {@code System.exit} qiladi.
 * O'sha oynada {@code OutboxPoller} ({@code fixedDelay=1s} — birinchi ijro darhol) prod
 * {@code outbox_event} qatorlariga {@code retry_count + 1} yozib ulgurardi. Kafka markazda
 * yo'qligi uchun har urinish muvaffaqiyatsiz — ya'ni har deploy qatorlarni
 * {@code retry_count < 100} chegarasiga bir qadam yaqinlashtirardi.
 *
 * <p><strong>Nega profil bo'yicha, xususiyat bo'yicha emas:</strong> {@code hemis.outbox.enabled}
 * va {@code hemis.webhook.enabled} ni {@code false} qilish faqat SHU IKKI job'ni to'xtatadi.
 * {@code CadastreRetryScheduler}, {@code ClassifierLookupService}, {@code DashboardCacheWarmup}
 * umuman shartsiz. Profil darajasidagi to'siq mavjudlarini ham, kelajakda qo'shiladiganlarini
 * ham avtomatik qamrab oladi — migratsiya pod'i faqat sxemaga tegishi kerak.
 *
 * <p>Odatiy ish rejimida ({@code migrate} profilisiz) xulq o'zgarmaydi: hamma job avvalgidek.
 */
@Configuration
@Profile("!migrate")
@EnableScheduling
public class SchedulingConfig {
}
