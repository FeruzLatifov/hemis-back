package uz.hemis.app.integration;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Integratsiya testlari uchun Liquibase — schema'ni Testcontainers konteynerida quradi.
 *
 * <h2>Nega bu sinf kerak</h2>
 *
 * <p><b>Spring Boot 4 da {@code LiquibaseAutoConfiguration} YO'Q.</b> Boot 3 da u
 * {@code spring-boot-autoconfigure} ichida edi va {@code DataSource} bean'i bo'lsa
 * o'zi ishga tushardi. Boot 4 uni alohida {@code org.springframework.boot:spring-boot-liquibase}
 * moduliga ajratgan, loyihada esa faqat {@code org.liquibase:liquibase-core} bor
 * ({@code app/build.gradle.kts}). O'lchov: Boot 4.0.6 ning barcha jar'larida
 * {@code LiquibaseAutoConfiguration} klassi topilmadi.</p>
 *
 * <p>Ya'ni bu loyihada Liquibase FAQAT qo'lda yozilgan bean orqali ishlaydi —
 * {@code DataSourceConfig#liquibase} ({@code app} moduli). U esa {@code @Profile("!test")},
 * shuning uchun {@code test} profilida schema quruvchi hech kim qolmaydi va testlar
 * {@code relation "public.users" does not exist} bilan yiqiladi.</p>
 *
 * <p>Bu holat ilgari YASHIRIN edi: {@code DashboardDataSourceConfig} testlarni
 * dasturchining REAL lokal bazasiga ulab qo'yardi, u yerda schema allaqachon mavjud edi.
 * Testcontainers'ga to'g'ri ulanish tiklangach ({@link AbstractIntegrationTest}), yetishmayotgan
 * migratsiya ko'rinib qoldi.</p>
 *
 * <h2>Nima uchun {@code @TestConfiguration}</h2>
 *
 * <p>{@code @TestConfiguration} component-scan'ga tushmaydi — faqat aniq {@code @Import}
 * bilan ulanadi ({@link AbstractIntegrationTest} shuni qiladi). Demak bu bean prod
 * kontekstiga hech qachon sizib o'tmaydi.</p>
 *
 * <p><b>Tartib haqida:</b> Liquibase ham, {@code EntityManagerFactory} ham kontekst
 * refresh'i davomida yaratiladi va testlar refresh TUGAGANDAN keyin ishlaydi — shuning
 * uchun test kodi uchun schema doim tayyor bo'ladi. {@code spring.jpa.hibernate.ddl-auto}
 * {@code none} ({@link AbstractIntegrationTest}), ya'ni Hibernate startup'da schema
 * validatsiya qilmaydi va tartibga bog'liq emas.</p>
 */
@TestConfiguration(proxyBeanMethods = false)
public class IntegrationTestDatabaseConfig {

    /** Legacy stub skripti — changelog'dan OLDIN bajariladi. */
    private static final String LEGACY_STUB = "db/testfixture/legacy-cuba-stub.sql";

    /**
     * Yagona {@code DataSource} bean'ini oladi. {@code test} profilida bu
     * {@code dashboardDataSource} ({@code DashboardDataSourceConfig}) bo'ladi va
     * {@link AbstractIntegrationTest} uni konteynerga qaratib, read-only'ni yechib qo'ygan.
     */
    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        applyLegacyStub(dataSource);

        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:/db/changelog/db.changelog-master.yaml");
        liquibase.setDefaultSchema("public");
        liquibase.setShouldRun(true);
        return liquibase;
    }

    /**
     * Eski CUBA jadvallarining minimal stub'ini yaratadi.
     *
     * <p>{@code db.changelog-master.yaml} o'zini o'zi ta'minlamaydi: u 11 ta legacy
     * jadvalga FK bilan tayanadi, lekin ularni yaratmaydi (real muhitlarda ular
     * old-hemis dump'idan keladi). Stub busiz migratsiya {@code V004_create_employee}
     * da to'xtaydi. Tafsilot — {@code db/testfixture/legacy-cuba-stub.sql} sarlavhasida.</p>
     *
     * <p>Liquibase'dan OLDIN va {@code CREATE TABLE IF NOT EXISTS} bilan bajariladi,
     * shuning uchun konteyner qayta ishlatilganda ham xavfsiz (idempotent).</p>
     */
    private void applyLegacyStub(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, new ClassPathResource(LEGACY_STUB));
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Legacy CUBA stub skriptini bajarib bo'lmadi: " + LEGACY_STUB, e);
        }
    }

    /**
     * Liquibase JPA'DAN OLDIN ishlashini majburlaydi.
     *
     * <p>Busiz {@code entityManagerFactory} va unga bog'liq beanlar (masalan
     * {@code LanguageProperties}, i18n kesh isitgichlari) kontekst refresh'i davomida
     * schema hali qurilmasdan turib so'rov yuboradi va
     * {@code relation "public.language" does not exist} /
     * {@code relation "public.system_message" does not exist} chiqadi. Xatolar
     * yutiladi (YAML fallback'ga tushadi), lekin ilova noto'g'ri holatda ko'tariladi
     * va keyingi so'rovlar 500 beradi.</p>
     *
     * <p>Spring Boot ham aynan shu usulni ishlatadi ({@code LiquibaseAutoConfiguration}
     * ichidagi {@code DataSourceInitializationDependencyConfigurer}) — Boot 4 da u modul
     * bilan birga yo'qolgani uchun bu yerda qo'lda takrorlaymiz.</p>
     *
     * <p>{@code static} bo'lishi SHART: {@code BeanFactoryPostProcessor} bean'lari
     * konfiguratsiya sinfi to'liq yaratilishidan oldin kerak bo'ladi.</p>
     */
    @Bean
    public static BeanFactoryPostProcessor liquibaseBeforeJpa() {
        return beanFactory -> {
            for (String name : beanFactory.getBeanDefinitionNames()) {
                if (!"entityManagerFactory".equals(name)) {
                    continue;
                }
                BeanDefinition definition = beanFactory.getBeanDefinition(name);
                definition.setDependsOn(
                        StringUtils.addStringToArray(definition.getDependsOn(), "liquibase"));
            }
        };
    }
}
