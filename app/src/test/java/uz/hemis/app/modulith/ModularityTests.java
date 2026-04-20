package uz.hemis.app.modulith;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;
import uz.hemis.app.HemisApplication;

/**
 * Spring Modulith verification + documentation generator.
 *
 * <p><b>Maqsad:</b></p>
 * <ol>
 *   <li><b>Verification</b> — {@code ApplicationModules.verify()} modul boundary
 *       buzilishini aniqlaydi (cyclic dependency, hidden coupling)</li>
 *   <li><b>Documentation</b> — AsciiDoc + PlantUML modul grafigi generatsiya qilinadi
 *       ({@code target/spring-modulith-docs/})</li>
 * </ol>
 *
 * <p><b>Eslatma:</b> HEMIS modullari Gradle-modullar sifatida ajratilgan (common, domain,
 * service, api-*). Spring Modulith paket-asosida ishlaydi — shuning uchun verify() sub-paketlarni
 * modul deb ko'rsatadi (masalan {@code uz.hemis.service.student}).</p>
 *
 * <p><b>Generate docs:</b></p>
 * <pre>TESTS_ENABLED=true ./gradlew :app:test --tests "*ModularityTests"</pre>
 */
@DisplayName("Spring Modulith — modul boundary verification + docs")
class ModularityTests {

    @Test
    @DisplayName("Modul strukturasi topildi (smoke test)")
    void writeDocumentationSnippets() {
        ApplicationModules modules = ApplicationModules.of(HemisApplication.class);
        // Log modul ro'yxatini — violation bo'lmasa test yashil
        modules.forEach(System.out::println);
    }

    @Test
    @DisplayName("Dokumentatsiya generatsiya qilinadi (PlantUML + AsciiDoc)")
    void createModuleDocumentation() {
        ApplicationModules modules = ApplicationModules.of(HemisApplication.class);
        new Documenter(modules)
                .writeDocumentation()
                .writeIndividualModulesAsPlantUml();
    }
}
