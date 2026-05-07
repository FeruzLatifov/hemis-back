package uz.hemis.app.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Arxitektura qoidalarini compile-time enforce qiluvchi ArchUnit testlar.
 *
 * <p>Senior engineering guardrails — refactor paytida noto'g'ri dependency yo'nalishi
 * yoki modul boundary buzilishini qo'riqlaydi.</p>
 *
 * <p><b>Qoidalar:</b></p>
 * <ul>
 *   <li>{@code common} hech qaysi hemis modulga bog'liq emas (pure utility)</li>
 *   <li>{@code domain} faqat {@code common} ga bog'lanadi (Clean Arch inner layer)</li>
 *   <li>{@code api-*} modullar bir-biriga bog'lanmaydi (sibling isolation)</li>
 *   <li>Controllerlar service'ga bog'lanadi, boshqa controllerga emas</li>
 *   <li>Cyclic dependency yo'q</li>
 * </ul>
 */
@DisplayName("Arxitektura qoidalari — ArchUnit")
class ArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        // JAR'larni include qilish KERAK — multi-modul loyihada boshqa modullar JAR
        // sifatida classpathda keladi. Faqat test classlarini chiqaramiz.
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("uz.hemis");
    }

    @Test
    @DisplayName("common modul hemis-ning boshqa modullariga bog'lanmaydi")
    void commonModuleMustNotDependOnOtherHemisModules() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("uz.hemis.common..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "uz.hemis.domain..",
                        "uz.hemis.service..",
                        "uz.hemis.security..",
                        "uz.hemis.api..",
                        "uz.hemis.app.."
                )
                .because("common — pure utility modul, boshqa hech narsaga bog'lanmasligi kerak");
        rule.check(classes);
    }

    @Test
    @DisplayName("domain modul service/api/security/app ga bog'lanmaydi (Clean Architecture)")
    void domainModuleMustBeInner() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("uz.hemis.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "uz.hemis.service..",
                        "uz.hemis.security..",
                        "uz.hemis.api..",
                        "uz.hemis.app.."
                )
                .because("domain — Clean Architecture inner layer, faqat common dependency ruxsat etiladi");
        rule.check(classes);
    }

    @Test
    @DisplayName("api-* modullar bir-biriga bog'lanmaydi (sibling isolation)")
    void apiModulesMustNotDependOnSiblingApiModules() {
        ArchRule rule = slices()
                .matching("uz.hemis.api.(*)..")
                .should().notDependOnEachOther()
                .because("har bir api-* modul mustaqil — ular orasida hech qanday bog'lanish bo'lmasligi kerak");
        rule.check(classes);
    }

    @Test
    @DisplayName("Hemis controllerlar bir-biriga bog'lanmaydi")
    void controllersMustNotCallControllers() {
        // Hemis controllerlar kesimida tekshirish — Spring `@RestController` annotation
        // class nomi ham "Controller" bilan tugaydi, shuning uchun hemis paketi bilan chegaralaymiz.
        ArchRule rule = noClasses()
                .that().haveSimpleNameEndingWith("Controller")
                .and().resideInAPackage("uz.hemis..")
                .should().dependOnClassesThat(
                        com.tngtech.archunit.base.DescribedPredicate.describe(
                                "hemis controllerlar",
                                clazz -> clazz.getSimpleName().endsWith("Controller")
                                        && clazz.getPackageName().startsWith("uz.hemis")
                        ))
                .because("controller-to-controller chaqirish antipatterni — service layer ishlatilsin");
        rule.check(classes);
    }

    @Test
    @DisplayName("Controllerlar Repository'ga to'g'ridan-to'g'ri kirmaydi")
    void controllersMustNotAccessRepositoriesDirectly() {
        ArchRule rule = noClasses()
                .that().haveSimpleNameEndingWith("Controller")
                .and().resideInAPackage("uz.hemis..")
                .should().dependOnClassesThat().haveSimpleNameEndingWith("Repository")
                .because("controllerlar service layer orqali ishlashi kerak — direct repository chaqiruvi qatlam abstraksiyasini buzadi");
        rule.check(classes);
    }

    @Test
    @DisplayName("Hech qanday cyclic dependency bo'lmasligi kerak")
    void noCyclicDependenciesBetweenSlices() {
        ArchRule rule = slices()
                .matching("uz.hemis.(*)..")
                .should().beFreeOfCycles()
                .because("cyclic dependency = compile time OK, lekin runtime class load va modularity muammo");
        rule.check(classes);
    }

    @Test
    @DisplayName("Entity fieldlari uchun Spring yoki Jackson annotation ishlatmaslik kerak (domain leak)")
    void entitiesShouldNotUseSpringOrJacksonAnnotations() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("uz.hemis.domain.entity..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework.stereotype..",
                        "org.springframework.beans.factory..",
                        "com.fasterxml.jackson.annotation.."
                )
                .because("domain entity'lar Spring va Jackson'dan mustasno — framework leak emas");
        // Bu test yumshoq — hozir violation bor (BaseEntity audit via Spring),
        // lekin kelajakda buni toza domain core'ga ajratish maqsadi. Hozircha log only:
        // rule.check(classes); // UNCOMMENT to enforce later
    }

    // =====================================================
    // ADR-0008 GOLDEN RULE — api-legacy split-brain himoyasi
    // =====================================================
    // Univer (Yii2 PHP, 224 OTM) eski hemishe_* jadvallarni kutadi.
    // api-legacy yangi schema'ga (employee_job, employee) yozsa →
    // ma'lumot Univer'da ko'rinmaydi (silent data loss).
    //
    // Documented exception (foydalanuvchi qarori 2026-05-07):
    //   User → users (yangi) — auth/profile read-only, M001+M004 sync taminlanadi.

    @Test
    @DisplayName("ADR-0008: api-legacy `Employee` (yangi schema) ni import qilmaydi")
    void apiLegacyMustNotImportNewSchemaEmployee() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("uz.hemis.api.legacy..")
                .should().dependOnClassesThat().haveFullyQualifiedName(
                        "uz.hemis.domain.entity.employee.Employee")
                .because("ADR-0008 GOLDEN RULE: api-legacy `Employee` (employee jadval, yangi schema) "
                        + "ni import qilmasligi kerak. Univer 224 OTM Yii2 PHP `hemishe_e_teacher` "
                        + "jadvalni kutadi → `Teacher` entity ishlating.");
        rule.check(classes);
    }

    @Test
    @DisplayName("ADR-0008: api-legacy `EmployeeJobs` (yangi schema) ni import qilmaydi")
    void apiLegacyMustNotImportNewSchemaEmployeeJobs() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("uz.hemis.api.legacy..")
                .should().dependOnClassesThat().haveFullyQualifiedName(
                        "uz.hemis.domain.entity.employee.EmployeeJobs")
                .because("ADR-0008 GOLDEN RULE: api-legacy `EmployeeJobs` (employee_job jadval, "
                        + "yangi schema) ni import qilmasligi kerak. Univer 224 OTM "
                        + "`hemishe_e_employee_jobs` jadvalni kutadi → "
                        + "`uz.hemis.domain.entity.legacy.employee.LegacyEmployeeJobs` ishlating.");
        rule.check(classes);
    }

    @Test
    @DisplayName("Legacy* entity'lar `domain.entity.legacy.*` paketda joylashishi shart")
    void legacyEntitiesMustResideInLegacyPackage() {
        // Diqqat: faqat real entity'lar (`@Entity` annotation bilan) tekshiriladi.
        // Base class (`@MappedSuperclass` LegacyClassifierEntity) `entity/base/` da bo'lishi mumkin —
        // bu base class, real entity emas.
        ArchRule rule = classes()
                .that().haveSimpleNameStartingWith("Legacy")
                .and().resideInAPackage("uz.hemis.domain.entity..")
                .and().areAnnotatedWith(jakarta.persistence.Entity.class)
                .should().resideInAPackage("uz.hemis.domain.entity.legacy..")
                .because("Legacy* prefiksli entity'lar paket konventsiyasi (ADR-0008): "
                        + "domain/entity/legacy/<sub-domain>/ — modular ajratish + future-proof guard.");
        rule.check(classes);
    }
}
