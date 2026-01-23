package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.RAcademicAttendance;

import java.util.List;
import java.util.UUID;

/**
 * Akademik Davomat Hisobotlari Repository
 *
 * <p>Table: hemishe_r_academic_attendance</p>
 *
 * <p><strong>Query Methods:</strong></p>
 * <ul>
 *   <li>findByUniversityCode - Universitet bo'yicha qidirish</li>
 *   <li>findByFacultyCode - Fakultet bo'yicha qidirish</li>
 *   <li>findByEducationYearCode - O'quv yili bo'yicha qidirish</li>
 *   <li>findByEducationTypeCode - Ta'lim turi bo'yicha qidirish</li>
 *   <li>findBySemesterTypeCode - Semester bo'yicha qidirish</li>
 *   <li>findByCourseCode - Kurs bo'yicha qidirish</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface RAcademicAttendanceRepository extends JpaRepository<RAcademicAttendance, UUID> {

    /**
     * Universitet kodi bo'yicha qidirish
     */
    List<RAcademicAttendance> findByUniversityCode(String universityCode);

    /**
     * Universitet kodi bo'yicha sahifalangan qidirish
     */
    Page<RAcademicAttendance> findByUniversityCode(String universityCode, Pageable pageable);

    /**
     * Fakultet kodi bo'yicha qidirish
     */
    List<RAcademicAttendance> findByFacultyCode(String facultyCode);

    /**
     * O'quv yili kodi bo'yicha qidirish
     */
    List<RAcademicAttendance> findByEducationYearCode(String educationYearCode);

    /**
     * Ta'lim turi kodi bo'yicha qidirish
     */
    List<RAcademicAttendance> findByEducationTypeCode(String educationTypeCode);

    /**
     * Semester turi kodi bo'yicha qidirish
     */
    List<RAcademicAttendance> findBySemesterTypeCode(String semesterTypeCode);

    /**
     * Kurs kodi bo'yicha qidirish
     */
    List<RAcademicAttendance> findByCourseCode(String courseCode);

    /**
     * Universitet va ta'lim yili bo'yicha qidirish
     */
    List<RAcademicAttendance> findByUniversityCodeAndEducationYearCode(
            String universityCode, String educationYearCode);

    /**
     * Universitet, fakultet va kurs bo'yicha qidirish
     */
    List<RAcademicAttendance> findByUniversityCodeAndFacultyCodeAndCourseCode(
            String universityCode, String facultyCode, String courseCode);
}
