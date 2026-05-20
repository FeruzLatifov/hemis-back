package uz.hemis.service.webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.webhook.WebhookTarget;
import uz.hemis.domain.repository.webhook.WebhookTargetRepository;

/**
 * Dev-only webhook target seed + vault populate.
 *
 * <p>Test environment'da: `dev` profile + {@code hemis.webhook.dev-seed.enabled=true}
 * bo'lsa, startup'da {@code webhook_target} row yaratadi va plain secret'ni
 * {@link WebhookSecretVault} ga joylashtiradi.</p>
 *
 * <p><strong>URL convention (2026-05-18):</strong> callback URL endi
 * {@code application.yml} convention'dan derive: {@code protocol + university.student_url + suffix}.
 * Lokal dev uchun {@code WEBHOOK_CALLBACK_PROTOCOL=http} +
 * {@code hemishe_e_university.student_url='localhost:9999'} qo'yib testlash mumkin
 * (yoki Univer real domain ishlatilsa, OTM 337 ning student.adu.uz yo'naltirishi kerak).</p>
 *
 * <p>Production'da hech qachon ishlamasligi shart — profile + property double guard.</p>
 */
@Component
@Profile("dev")
@ConditionalOnProperty(name = "hemis.webhook.dev-seed.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class DevWebhookBootstrap implements CommandLineRunner {

    private final WebhookTargetRepository targetRepository;
    private final WebhookSecretVault secretVault;
    private final PasswordEncoder passwordEncoder;

    private static final String UNIVERSITY_CODE = "337";
    private static final String PLAIN_SECRET = "whsec_dev_test_secret_DO_NOT_USE_IN_PROD";

    @Override
    @Transactional
    public void run(String... args) {
        WebhookTarget target = targetRepository.findByUniversityCode(UNIVERSITY_CODE)
                .orElseGet(() -> {
                    WebhookTarget fresh = new WebhookTarget();
                    fresh.setUniversityCode(UNIVERSITY_CODE);
                    fresh.setSecretHash(passwordEncoder.encode(PLAIN_SECRET));
                    fresh.setDescription("Dev test target — auto-seeded by DevWebhookBootstrap");
                    fresh.setTimeoutMs(15000);
                    fresh.setMaxRetries(3);
                    return targetRepository.save(fresh);
                });

        secretVault.store(target.getUniversityCode(), PLAIN_SECRET);

        log.warn("DEV BOOTSTRAP: webhook_target id={} for university={} | secret stored in vault",
                target.getId(), target.getUniversityCode());
    }
}
