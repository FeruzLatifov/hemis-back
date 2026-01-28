package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.REmployment;

import java.util.List;
import java.util.UUID;

/**
 * Bandlik Statistikasi Repository
 *
 * <p>Table: hemishe_r_employment</p>
 *
 * <p><strong>Query Methods:</strong></p>
 * <ul>
 *   <li>findByUniversityCode - Universitet bo'yicha qidirish</li>
 *   <li>findByDepartmentCode - Bo'lim bo'yicha qidirish</li>
 *   <li>findByEducationYearCode - O'quv yili bo'yicha qidirish</li>
 *   <li>findByEducationTypeCode - Ta'lim turi bo'yicha qidirish</li>
 *   <li>findByEducationFormCode - Ta'lim shakli bo'yicha qidirish</li>
 *   <li>findByGenderCode - Jins bo'yicha qidirish</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface REmploymentRepository extends JpaRepository<REmployment, UUID> {

    /**
     * Universitet kodi bo'yicha qidirish
     */
    List<REmployment> findByUniversityCode(String universityCode);

    /**
     * Universitet kodi bo'yicha sahifalangan qidirish
     */
    Page<REmployment> findByUniversityCode(String universityCode, Pageable pageable);

    /**
     * Bo'lim kodi bo'yicha qidirish
     */
    List<REmployment> findByDepartmentCode(String departmentCode);

    /**
     * Ta'lim yili kodi bo'yicha qidirish
     */
    List<REmployment> findByEducationYearCode(String educationYearCode);

    /**
     * Ta'lim turi kodi bo'yicha qidirish
     */
    List<REmployment> findByEducationTypeCode(String educationTypeCode);

    /**
     * Ta'lim shakli kodi bo'yicha qidirish
     */
    List<REmployment> findByEducationFormCode(String educationFormCode);

    /**
     * Jins kodi bo'yicha qidirish
     */
    List<REmployment> findByGenderCode(String genderCode);

    /**
     * Universitet va ta'lim yili bo'yicha qidirish
     */
    List<REmployment> findByUniversityCodeAndEducationYearCode(
            String universityCode, String educationYearCode);

    /**
     * Universitet, bo'lim va ta'lim yili bo'yicha qidirish
     */
    List<REmployment> findByUniversityCodeAndDepartmentCodeAndEducationYearCode(
            String universityCode, String departmentCode, String educationYearCode);

    /**
     * UPSERT uchun: Unique key kombinatsiyasi bo'yicha mavjud yozuvni topish
     * Unique index: idx_hemishe_r_employment_unq
     */
    java.util.Optional<REmployment> findByDepartmentCodeAndEducationYearCodeAndEducationTypeCodeAndEducationFormCodeAndPaymentFormCodeAndGenderCodeAndWorkplaceCompatibilityCodeAndGraduateFieldsTypeCodeAndGraduateInactiveTypeCode(
            String departmentCode,
            String educationYearCode,
            String educationTypeCode,
            String educationFormCode,
            String paymentFormCode,
            String genderCode,
            String workplaceCompatibilityCode,
            String graduateFieldsTypeCode,
            String graduateInactiveTypeCode);
}
