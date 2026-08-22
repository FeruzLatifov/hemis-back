package uz.hemis.service.classifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uz.hemis.service.classifier.ClassifierMetadataRegistry.Category;
import uz.hemis.service.classifier.ClassifierMetadataRegistry.ClassifierMeta;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ClassifierMetadataRegistry} unit testlar — whitelist + apiKey/tableName mapping.
 *
 * <p>Fokus: Positions (h_position) va Qualifications (hemishe_h_qualification) editable klassifikator
 * sifatida generic klassifikator tizimiga ulanganini tasdiqlaydi.</p>
 */
@DisplayName("ClassifierMetadataRegistry")
class ClassifierMetadataRegistryTest {

    @Test
    @DisplayName("position — explicit apiKey 'position', editable, jadval h_position")
    void position_isEditableClassifier_withExplicitApiKey() {
        ClassifierMeta meta = ClassifierMetadataRegistry.getByApiKey("position");

        assertThat(meta).as("'position' whitelist'da ro'yxatga olingan bo'lishi kerak").isNotNull();
        assertThat(meta.getTableName()).isEqualTo("h_position");
        assertThat(meta.getApiKey()).isEqualTo("position");
        assertThat(meta.isEditable()).as("Positions to'liq CRUD (LIST/CREATE/EDIT/DELETE)").isTrue();
        assertThat(meta.isHierarchical()).isFalse();
        assertThat(meta.getCategory()).isEqualTo(Category.EMPLOYEE);
    }

    @Test
    @DisplayName("position — auto-derive 'h-position' EMAS, explicit 'position' ostida ro'yxatda")
    void position_isNotRegisteredUnderAutoDerivedKey() {
        assertThat(ClassifierMetadataRegistry.isRegistered("position")).isTrue();
        assertThat(ClassifierMetadataRegistry.isRegistered("h-position")).isFalse();
    }

    @Test
    @DisplayName("qualification — hemishe_h_qualification, editable (fanout classifier_type='qualification')")
    void qualification_isEditableClassifier() {
        ClassifierMeta meta = ClassifierMetadataRegistry.getByApiKey("qualification");

        assertThat(meta).isNotNull();
        assertThat(meta.getTableName()).isEqualTo("hemishe_h_qualification");
        assertThat(meta.getApiKey()).isEqualTo("qualification");
        assertThat(meta.isEditable()).isTrue();
        assertThat(meta.getCategory()).isEqualTo(Category.EMPLOYEE);
    }

    @Test
    @DisplayName("har bir jadval nomi 'hemishe_h_' yoki 'h_' prefiksi bilan boshlanadi")
    void everyTableNameHasClassifierPrefix() {
        // NEGA: 2026-08-22 da `reg("soato", ...)` va `reg("course", ...)` prefikssiz yozilgani
        // aniqlandi. Bunday jadval bazada YO'Q (real nomlar hemishe_h_soato / hemishe_h_course),
        // ClassifierWebService esa tableExists() false bo'lganda Page.empty() qaytaradi —
        // ya'ni admin web'da "Viloyat va tumanlar (SOATO)" (225 qator) va "O'quv kurslari"
        // (6 qator) sahifalari XATOSIZ, lekin BO'SH ko'rinardi. HTTP 200, Sentry'da iz yo'q.
        var noaniq = ClassifierMetadataRegistry.getAll().stream()
                .map(ClassifierMeta::getTableName)
                .filter(n -> !n.startsWith("hemishe_h_") && !n.startsWith("h_"))
                .toList();

        assertThat(noaniq)
                .as("klassifikator jadvali `hemishe_h_*` (legacy) yoki `h_*` (yangi) bo'lishi shart; "
                        + "prefikssiz nom bazada mavjud emas -> sahifa jimgina bo'sh chiqadi")
                .isEmpty();
    }

    @Test
    @DisplayName("soato va course — haqiqiy jadval nomiga bog'langan, apiKey o'zgarmagan")
    void soatoAndCourse_pointAtRealTables() {
        ClassifierMeta soato = ClassifierMetadataRegistry.getByApiKey("soato");
        assertThat(soato).isNotNull();
        assertThat(soato.getTableName()).isEqualTo("hemishe_h_soato");
        assertThat(soato.getApiKey()).as("API URL o'zgarmasligi kerak").isEqualTo("soato");

        ClassifierMeta course = ClassifierMetadataRegistry.getByApiKey("course");
        assertThat(course).isNotNull();
        assertThat(course.getTableName()).isEqualTo("hemishe_h_course");
        assertThat(course.getApiKey()).as("API URL o'zgarmasligi kerak").isEqualTo("course");
    }
}
