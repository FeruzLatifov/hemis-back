package uz.hemis.app.integration;

import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Real Postgres container orqali integration testlar uchun baza sinf.
 *
 * <p>H2 HEMIS domen entitylarini to'liq qo'llab-quvvatlay olmaydi — masalan,
 * {@code @Type(StringToUuidType.class)} (PostgreSQL UUID) H2 da {@code SqlTypes.OTHER}
 * ga map bo'lmaydi. Shu sababli integration testlar TestContainers-Postgres orqali
 * ishlaydi.</p>
 *
 * <p><b>Docker talab qilinadi:</b> Docker/Podman topilmasa, testlar skip qilinadi
 * (fail emas). Lokal muhitda Docker o'rnatish: {@code apt install docker.io}.</p>
 *
 * <p><b>Container lifecycle:</b> {@code static} va {@code .withReuse(true)} — har bir
 * test class uchun yangi container emas, session davomida bitta container qayta
 * ishlatiladi.</p>
 *
 * <p><b>Liquibase:</b> {@code spring.liquibase.enabled=true} container startup da
 * avtomatik barcha migratsiyalarni yuritadi — schema real prod schema bilan bir xil.</p>
 */
@Testcontainers
@EnabledIf("uz.hemis.app.integration.AbstractIntegrationTest#isDockerAvailable")
public abstract class AbstractIntegrationTest {

    @SuppressWarnings("resource")
    protected static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("hemis_test")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true);

    static {
        if (isDockerAvailable()) {
            POSTGRES.start();
        }
    }

    public static boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable t) {
            return false;
        }
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        // Docker yo'q bo'lsa @EnabledIf testni skip qiladi — bu yerda propertylar
        // ro'yxatdan o'tmasligi kerak (aks holda getJdbcUrl() Docker izlaydi va fail qiladi).
        if (!isDockerAvailable()) {
            return;
        }

        // Master + Replica: test da ikkalasi ham bir xil containerga ko'rsatiladi
        registry.add("spring.datasource.master.jdbc-url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.master.username", POSTGRES::getUsername);
        registry.add("spring.datasource.master.password", POSTGRES::getPassword);
        registry.add("spring.datasource.replica.jdbc-url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.replica.username", POSTGRES::getUsername);
        registry.add("spring.datasource.replica.password", POSTGRES::getPassword);

        // Spring Boot default datasource (Liquibase ishlatadigan)
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);

        // Liquibase — schema migratsiyalarini yuritish
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.liquibase.change-log",
                () -> "classpath:/db/changelog/db.changelog-master.yaml");

        // JPA — schema generation O'chirilgan (Liquibase boshqaradi)
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }
}
