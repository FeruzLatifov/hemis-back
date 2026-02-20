package uz.hemis.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Migration rejimida (migrate profil) Spring Boot ishga tushganda,
 * Liquibase migration tugagach avtomatik exit qiladi.
 * Bu Helm pre-upgrade Job uchun kerak — Job tugashi shart.
 *
 * Liquibase bean init paytida migrationni bajaradi.
 * CommandLineRunner.run() chaqilganda migration allaqachon tugagan bo'ladi.
 */
@Profile("migrate")
@Component
public class MigrationExitRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MigrationExitRunner.class);

    private final ConfigurableApplicationContext context;

    public MigrationExitRunner(ConfigurableApplicationContext context) {
        this.context = context;
    }

    @Override
    public void run(String... args) {
        log.info("Liquibase migration muvaffaqiyatli tugadi. Application yopilmoqda...");
        System.exit(SpringApplication.exit(context));
    }
}
