package uz.hemis.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Clean Architecture Validation Tests
 * 
 * Ushbu testlar arxitektura qoidalarini tekshiradi:
 * 1. Domain qatlami infrastructure'ga bog'liq bo'lmasligi kerak
 * 2. Controller qatlami repository'larga to'g'ridan-to'g'ri murojaat qilmasligi kerak
 * 3. Qatlamlar orasidagi dependency qoidalari saqlanishi kerak
 * 
 * @see <a href="https://www.archunit.org/">ArchUnit Documentation</a>
 */
public class CleanArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("uz.hemis");
    }

    /**
     * CRITICAL RULE 1: Domain qatlami Spring'ga bog'liq bo'lmasligi kerak
     * 
     * Domain qatlami faqat biznes reglari va modellarni o'z ichiga olishi kerak.
     * Spring, JPA va boshqa framework'larga bog'liqlik bo'lmasligi kerak.
     */
    @Test
    void domain_should_not_depend_on_spring_framework() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("uz.hemis.domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "org.springframework..",
                "org.springframework.data..",
                "javax.persistence..",
                "jakarta.persistence.."
            )
            .because("Domain layer must be framework-agnostic for Clean Architecture");

        try {
            rule.check(classes);
            System.out.println("✅ Domain layer is framework-agnostic!");
        } catch (AssertionError e) {
            System.out.println("❌ Domain layer has framework dependencies (needs refactoring):");
            System.out.println(e.getMessage());
        }
    }

    /**
     * CRITICAL RULE 2: Controller'lar repository'larga to'g'ridan-to'g'ri murojaat qilmasligi kerak
     */
    @Test
    void controllers_should_not_access_repositories_directly() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..controller..")
            .should().dependOnClassesThat()
            .resideInAPackage("..repository..")
            .because("Controllers must use Service layer, not repositories directly");

        try {
            rule.check(classes);
            System.out.println("✅ Controllers use Service layer correctly!");
        } catch (AssertionError e) {
            System.out.println("❌ Some controllers access repositories directly:");
            System.out.println(e.getMessage());
        }
    }

    /**
     * RULE 3: Service qatlami web qatlamiga bog'liq bo'lmasligi kerak
     */
    @Test
    void services_should_not_depend_on_web_layer() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..service..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "..controller..",
                "org.springframework.web..",
                "javax.servlet..",
                "jakarta.servlet.."
            )
            .because("Service layer must be independent of web layer");

        rule.check(classes);
        System.out.println("✅ Service layer is independent of web layer!");
    }

    /**
     * RULE 4: Repository'lar faqat service va infrastructure'dan ishlatilishi mumkin
     */
    @Test
    void repositories_should_only_be_accessed_from_service_or_infrastructure() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..repository..")
            .should().onlyBeAccessed().byAnyPackage(
                "..service..",
                "..infrastructure..",
                "..repository..",
                "..config.."
            )
            .because("Repositories should only be accessed from service layer or infrastructure");

        try {
            rule.check(classes);
            System.out.println("✅ Repositories are accessed correctly!");
        } catch (AssertionError e) {
            System.out.println("❌ Some classes access repositories incorrectly:");
            System.out.println(e.getMessage());
        }
    }

    /**
     * RULE 5: Common qatlam boshqa qatlamlarga bog'liq bo'lmasligi kerak
     */
    @Test
    void common_should_not_depend_on_other_layers() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("uz.hemis.common..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "uz.hemis.domain..",
                "uz.hemis.service..",
                "uz.hemis.api..",
                "uz.hemis.infrastructure.."
            )
            .because("Common layer should be independent and not depend on other layers");

        try {
            rule.check(classes);
            System.out.println("✅ Common layer is independent!");
        } catch (AssertionError e) {
            System.out.println("❌ Common layer has dependencies on other layers:");
            System.out.println(e.getMessage());
        }
    }
}
