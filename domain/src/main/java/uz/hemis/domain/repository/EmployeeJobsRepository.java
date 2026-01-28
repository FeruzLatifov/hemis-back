package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.EmployeeJobs;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface EmployeeJobsRepository extends JpaRepository<EmployeeJobs, UUID> {

    List<EmployeeJobs> findByUniversity(String university);

    List<EmployeeJobs> findByUniversityAndEmployee(String university, UUID employee);

    Page<EmployeeJobs> findByUniversityAndEmployee(String university, UUID employee, Pageable pageable);

    long countByUniversityAndEmployee(String university, UUID employee);

    /**
     * Xodimning asosiy shtat birligidagi ishini topish
     *
     * @param employeeId xodim ID si
     * @param mainFormCode asosiy shtat kodi (11)
     * @param workingStatusCode ishlamoqda holati (11)
     * @return EmployeeJobs agar topilsa
     */
    @Query("""
        SELECT e FROM EmployeeJobs e
        WHERE e.employee = :employeeId
        AND e.employeeForm = :mainFormCode
        AND e.employeeStatus = :workingStatusCode
        AND e.deleteTs IS NULL
        """)
    Optional<EmployeeJobs> findMainJobByEmployee(
            @Param("employeeId") UUID employeeId,
            @Param("mainFormCode") String mainFormCode,
            @Param("workingStatusCode") String workingStatusCode
    );

    /**
     * PINFL bo'yicha xodimning asosiy shtat birligidagi ishini topish
     *
     * @param pinfl xodim PINFL raqami
     * @param mainFormCode asosiy shtat kodi (11)
     * @param workingStatusCode ishlamoqda holati (11)
     * @return EmployeeJobs agar topilsa
     */
    @Query(value = """
        SELECT ej.* FROM hemishe_e_employee_jobs ej
        JOIN hemishe_e_employee e ON e.id = ej._employee
        WHERE e.pinfl = :pinfl
        AND ej._employee_form = :mainFormCode
        AND ej._employee_status = :workingStatusCode
        AND ej.delete_ts IS NULL
        AND e.delete_ts IS NULL
        LIMIT 1
        """, nativeQuery = true)
    Optional<EmployeeJobs> findMainJobByPinfl(
            @Param("pinfl") String pinfl,
            @Param("mainFormCode") String mainFormCode,
            @Param("workingStatusCode") String workingStatusCode
    );

    // =====================================================
    // University Filtered Queries (Security)
    // =====================================================

    /**
     * ID va universitet kodi bo'yicha lavozimni topish
     *
     * <p><strong>Security:</strong> Har bir OTM faqat o'z xodimlarini ko'rishi uchun</p>
     *
     * @param id lavozim UUID
     * @param universityCode OTM kodi
     * @return EmployeeJobs agar topilsa va OTM kodi mos kelsa
     */
    @Query("SELECT e FROM EmployeeJobs e WHERE e.id = :id AND e.university = :universityCode")
    Optional<EmployeeJobs> findByIdAndUniversity(
            @Param("id") UUID id,
            @Param("universityCode") String universityCode
    );

    /**
     * Universitet bo'yicha barcha lavozimlarni sahifalab olish
     *
     * @param universityCode OTM kodi
     * @param pageable sahifalash parametrlari
     * @return Page of EmployeeJobs
     */
    Page<EmployeeJobs> findAllByUniversity(String universityCode, Pageable pageable);

    // =====================================================
    // Search by Employee Code (Xodim kodi)
    // =====================================================

    /**
     * Xodim kodi bo'yicha ish joylarini topish
     *
     * <p><strong>Use case:</strong> POST /search endpoint uchun</p>
     * <p>Employee.code (masalan: "3011911003") bo'yicha qidirish</p>
     *
     * @param employeeCode xodim kodi (hemishe_e_employee.code)
     * @param universityCode OTM kodi (security filter)
     * @return List of EmployeeJobs
     */
    @Query(value = """
        SELECT ej.* FROM hemishe_e_employee_jobs ej
        JOIN hemishe_e_teacher e ON e.id = ej._employee
        WHERE e.code = :employeeCode
        AND ej._university = :universityCode
        AND ej.delete_ts IS NULL
        AND e.delete_ts IS NULL
        """, nativeQuery = true)
    List<EmployeeJobs> findByEmployeeCodeAndUniversity(
            @Param("employeeCode") String employeeCode,
            @Param("universityCode") String universityCode
    );

    /**
     * Xodim kodi bo'yicha ish joylarini topish (sahifalash bilan)
     *
     * @param employeeCode xodim kodi
     * @param universityCode OTM kodi
     * @param pageable sahifalash
     * @return Page of EmployeeJobs
     */
    @Query(value = """
        SELECT ej.* FROM hemishe_e_employee_jobs ej
        JOIN hemishe_e_teacher e ON e.id = ej._employee
        WHERE e.code = :employeeCode
        AND ej._university = :universityCode
        AND ej.delete_ts IS NULL
        AND e.delete_ts IS NULL
        """,
        countQuery = """
        SELECT COUNT(*) FROM hemishe_e_employee_jobs ej
        JOIN hemishe_e_teacher e ON e.id = ej._employee
        WHERE e.code = :employeeCode
        AND ej._university = :universityCode
        AND ej.delete_ts IS NULL
        AND e.delete_ts IS NULL
        """,
        nativeQuery = true)
    Page<EmployeeJobs> findByEmployeeCodeAndUniversity(
            @Param("employeeCode") String employeeCode,
            @Param("universityCode") String universityCode,
            Pageable pageable
    );

    /**
     * Xodim UUID bo'yicha ish joylarini topish (universitet filtri bilan)
     *
     * @param employeeId xodim UUID
     * @param universityCode OTM kodi
     * @return List of EmployeeJobs
     */
    @Query("SELECT e FROM EmployeeJobs e WHERE e.employee = :employeeId AND e.university = :universityCode")
    List<EmployeeJobs> findByEmployeeIdAndUniversity(
            @Param("employeeId") UUID employeeId,
            @Param("universityCode") String universityCode
    );

    // =====================================================
    // Search WITHOUT University Filter (OLD-HEMIS Compatible)
    // =====================================================

    /**
     * Xodim kodi bo'yicha BARCHA ish joylarini topish (universitet filtrisiz)
     *
     * <p><strong>OLD-HEMIS Compatible:</strong> Universitet filtri yo'q</p>
     *
     * @param employeeCode xodim kodi
     * @return List of EmployeeJobs (barcha universitetlardan)
     */
    @Query(value = """
        SELECT ej.* FROM hemishe_e_employee_jobs ej
        JOIN hemishe_e_teacher e ON e.id = ej._employee
        WHERE e.code = :employeeCode
        AND ej.delete_ts IS NULL
        AND e.delete_ts IS NULL
        """, nativeQuery = true)
    List<EmployeeJobs> findByEmployeeCode(@Param("employeeCode") String employeeCode);

    /**
     * Xodim UUID bo'yicha BARCHA ish joylarini topish (universitet filtrisiz)
     *
     * @param employeeId xodim UUID
     * @return List of EmployeeJobs (barcha universitetlardan)
     */
    @Query("SELECT e FROM EmployeeJobs e WHERE e.employee = :employeeId")
    List<EmployeeJobs> findByEmployeeId(@Param("employeeId") UUID employeeId);

    // =====================================================
    // UPSERT Support (OLD-HEMIS Compatible)
    // =====================================================

    /**
     * Xodim va ish shakli bo'yicha mavjud AKTIV yozuvni topish (UPSERT uchun)
     *
     * <p><strong>OLD-HEMIS Compatible:</strong> POST endpoint'da UPSERT mantiqini qo'llash uchun.
     * Agar (_employee, _employee_form) kombinatsiyasi mavjud bo'lsa, yangi yozuv yaratish o'rniga
     * mavjud yozuvni yangilash kerak.</p>
     *
     * <p><strong>Unique Constraint:</strong> employee_jobs_main_form_unique_constraint</p>
     * <p><strong>MUHIM:</strong> Faqat o'chirilmagan yozuvlarni qaytaradi (delete_ts IS NULL)</p>
     *
     * @param employeeId xodim UUID
     * @param employeeForm ish shakli kodi (11=asosiy, 12=qo'shimcha)
     * @return Optional EmployeeJobs agar topilsa (birinchi aktiv yozuv)
     */
    @Query("SELECT e FROM EmployeeJobs e WHERE e.employee = :employeeId AND e.employeeForm = :employeeForm AND e.deleteTs IS NULL ORDER BY e.createTs DESC")
    List<EmployeeJobs> findByEmployeeAndEmployeeFormActive(
            @Param("employeeId") UUID employeeId,
            @Param("employeeForm") String employeeForm
    );

    /**
     * Xodim va ish shakli bo'yicha BARCHA yozuvlarni topish (soft-deleted ham)
     *
     * @param employeeId xodim UUID
     * @param employeeForm ish shakli kodi
     * @return Barcha yozuvlar (soft-deleted ham)
     */
    @Query("SELECT e FROM EmployeeJobs e WHERE e.employee = :employeeId AND e.employeeForm = :employeeForm")
    List<EmployeeJobs> findAllByEmployeeAndEmployeeForm(
            @Param("employeeId") UUID employeeId,
            @Param("employeeForm") String employeeForm
    );

    /**
     * Xodim, ish shakli va status bo'yicha aktiv yozuvlarni topish
     *
     * <p><strong>Constraint uchun:</strong> UNIQUE (_employee, _employee_form)
     * WHERE _employee_form = '11' AND _employee_status = '11'</p>
     *
     * <p>Yangi yozuv yaratishdan oldin bu constraint'ni buzmasligi uchun
     * eski yozuvlarning statusini o'zgartirish kerak.</p>
     *
     * @param employeeId xodim UUID
     * @param employeeForm ish shakli kodi (11=asosiy)
     * @param employeeStatus holat kodi (11=ishlamoqda)
     * @return Constraint shartlariga mos yozuvlar
     */
    @Query("SELECT e FROM EmployeeJobs e WHERE e.employee = :employeeId AND e.employeeForm = :employeeForm AND e.employeeStatus = :employeeStatus AND e.deleteTs IS NULL")
    List<EmployeeJobs> findByEmployeeAndFormAndStatus(
            @Param("employeeId") UUID employeeId,
            @Param("employeeForm") String employeeForm,
            @Param("employeeStatus") String employeeStatus
    );
}
