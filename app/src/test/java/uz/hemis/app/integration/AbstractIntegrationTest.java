package uz.hemis.app.integration;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Real Postgres container orqali integration testlar uchun baza sinf.
 *
 * <p>H2 HEMIS domen entitylarini to'liq qo'llab-quvvatlay olmaydi — masalan,
 * {@code @Type(StringToUuidType.class)} (PostgreSQL UUID) H2 da {@code SqlTypes.OTHER}
 * ga map bo'lmaydi. Shu sababli integration testlar TestContainers-Postgres orqali
 * ishlaydi.</p>
 *
 * <p><b>Docker MAJBURIY.</b> Topilmasa testlar skip EMAS, {@code IllegalStateException}
 * bilan yiqiladi. Sabab: jimgina skip qilinsa suite yashil ko'rinadi-yu, aslida
 * bironta integratsiya yo'li sinalmagan bo'ladi — bu eng qimmat turdagi soxta signal.
 * Lokal muhitda: {@code apt install docker.io} yoki Docker Desktop.</p>
 *
 * <p><b>Container lifecycle:</b> {@code static} va {@code .withReuse(true)} — har bir
 * test class uchun yangi container emas, session davomida bitta container qayta
 * ishlatiladi.</p>
 *
 * <p><b>Liquibase:</b> {@link IntegrationTestDatabaseConfig} orqali — Spring Boot 4 da
 * {@code LiquibaseAutoConfiguration} YO'Q (alohida modulga ajratilgan, loyihada u modul
 * bog'liqlik sifatida yo'q), shuning uchun schema quruvchi bean'ni aniq e'lon qilamiz.
 * Natijada konteynerdagi schema real prod schema bilan bir xil bo'ladi.</p>
 *
 * <p><b>⚠️ Dasturchi bazasini himoya qilish:</b> {@code DashboardDataSourceConfig}
 * ({@code service} moduli) {@code @Profile} guard'siz va URL'ini {@code System.getenv()}
 * {@code DB_REPLICA_*}/{@code DB_MASTER_*} dan quradi. Gradle esa {@code .env} ning
 * BARCHA kalitlarini test JVM'iga uzatadi ({@code build.gradle.kts} {@code tasks.withType<Test>}).
 * {@code test} profilida {@code DataSourceConfig} ({@code @Profile("!test")}) o'chiq
 * bo'lgani uchun {@code dashboardDataSource} YAGONA {@code DataSource} bean bo'lib qoladi va
 * {@code DataSourceAutoConfiguration} ({@code @ConditionalOnMissingBean}) chekinadi.
 * Natijada quyidagi {@code spring.datasource.*} propertylari ISHLATILMAY qoladi va
 * testlar dasturchining REAL lokal bazasiga ulanadi — u yerda pool read-only bo'lgani
 * uchun har {@code INSERT} "cannot execute INSERT in a read-only transaction" beradi.
 * Shuning uchun {@code spring.datasource.dashboard.*} ni ham shu yerda konteynerga
 * qaratamiz — {@code @ConfigurationProperties("spring.datasource.dashboard")} binding
 * bean yaratilgandan keyin qo'llanadi va {@code setJdbcUrl}/{@code setReadOnly(true)} ni
 * bekor qiladi.</p>
 */
@Testcontainers
@Import(IntegrationTestDatabaseConfig.class)
public abstract class AbstractIntegrationTest {

    private static final String DOCKER_REQUIRED =
            """
            Integratsiya testlari uchun Docker MAJBURIY (Testcontainers PostgreSQL 16).
            Docker/Podman topilmadi.

            Nima uchun skip emas, xato: skip qilinsa test suite yashil ko'rinadi,
            lekin bironta integratsiya yo'li (OAuth token, security filter chain,
            Liquibase schema) sinalmagan bo'ladi.

            Yechim:
              sudo apt install docker.io && sudo systemctl start docker
              sudo usermod -aG docker $USER   # keyin qayta login

            Eslatma: bu testlar sizning lokal ish bazangizga (test3_hemis) TEGMAYDI —
            ular Testcontainers ko'targan vaqtinchalik konteynerda ishlaydi.
            """;

    @SuppressWarnings("resource")
    protected static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("hemis_test")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true);

    /**
     * Redis ham IZOLYATSIYA qilinadi — dasturchining lokal Redis'i EMAS.
     *
     * <p>Busiz testlar {@code application.yml} default'i bilan {@code localhost:6379} ga
     * chiqadi (Gradle {@code .env} ning barcha kalitlarini uzatadi) va u yerga YOZADI:
     * {@code PermissionCacheResetRunner} startup'da {@code user:permissions:*} kalitlarini
     * O'CHIRADI, {@code DashboardCacheWarmup} esa bo'sh konteyner statistikasini
     * {@code stats} keshiga 30 daqiqaga yozib qo'yadi. Ya'ni PostgreSQL uchun tuzatilgan
     * nuqsonning aynan o'zi Redis tomonda ochiq qolgan edi.</p>
     */
    @SuppressWarnings("resource")
    protected static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379)
                    .withReuse(true);

    static {
        if (!isDockerAvailable()) {
            throw new IllegalStateException(DOCKER_REQUIRED);
        }
        POSTGRES.start();
        REDIS.start();
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
        // Master + Replica: test da ikkalasi ham bir xil containerga ko'rsatiladi
        registry.add("spring.datasource.master.jdbc-url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.master.username", POSTGRES::getUsername);
        registry.add("spring.datasource.master.password", POSTGRES::getPassword);
        registry.add("spring.datasource.replica.jdbc-url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.replica.username", POSTGRES::getUsername);
        registry.add("spring.datasource.replica.password", POSTGRES::getPassword);

        // Dashboard pool — yuqoridagi sinf izohiga qarang. Busiz testlar dasturchining
        // lokal bazasiga ulanadi. read-only=false: bu poolda JPA ham ishlaydi (test
        // profilida u yagona DataSource), shuning uchun yozuv ochiq bo'lishi shart.
        registry.add("spring.datasource.dashboard.jdbc-url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.dashboard.username", POSTGRES::getUsername);
        registry.add("spring.datasource.dashboard.password", POSTGRES::getPassword);
        registry.add("spring.datasource.dashboard.read-only", () -> "false");

        // Spring Boot default datasource (Liquibase ishlatadigan)
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);

        // Liquibase — schema migratsiyalarini yuritish
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.liquibase.change-log",
                () -> "classpath:/db/changelog/db.changelog-master.yaml");

        // Redis — dasturchining lokal Redis'iga tegmaslik uchun konteyner
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        registry.add("spring.data.redis.password", () -> "");

        // JPA — schema generation O'chirilgan (Liquibase boshqaradi)
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }
}
