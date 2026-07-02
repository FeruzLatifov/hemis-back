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
}
