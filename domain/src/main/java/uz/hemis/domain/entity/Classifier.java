package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Classifier Entity
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Reference/lookup data (classifiers)</li>
 *   <li>Dropdowns, selection lists</li>
 *   <li>Standardized codes and names</li>
 * </ul>
 *
 * <p><strong>Examples:</strong></p>
 * <ul>
 *   <li>Student status codes (11=Active, 21=Graduated, 31=Expelled)</li>
 *   <li>Payment forms (10=Grant, 11=Contract)</li>
 *   <li>Education types (Bakalavr, Magistr, Doktorantura)</li>
 *   <li>Gender codes (1=Male, 2=Female)</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "h_classifier")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Classifier extends BaseEntity {

    /**
     * Classifier type/category
     * Examples: STUDENT_STATUS, PAYMENT_FORM, EDUCATION_TYPE
     */
    @Column(name = "classifier_type", nullable = false, length = 50)
    private String classifierType;

    /**
     * Classifier code
     * Examples: "11", "10", "BACHELOR"
     */
    @Column(name = "code", nullable = false, length = 20)
    private String code;

    /**
     * Name in Uzbek
     */
    @Column(name = "name_uz", nullable = false, length = 255)
    private String nameUz;

    /**
     * Name in Russian
     */
    @Column(name = "name_ru", length = 255)
    private String nameRu;

    /**
     * Name in English
     */
    @Column(name = "name_en", length = 255)
    private String nameEn;

    /**
     * Description
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Sort order for display
     */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /**
     * Active flag
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
