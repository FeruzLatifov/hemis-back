package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.Grade;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface GradeRepository extends JpaRepository<Grade, UUID> {

    // NO DELETE METHODS (NDG)

    @Query("SELECT g FROM Grade g WHERE g.student = :studentId")
    Page<Grade> findByStudent(@Param("studentId") UUID studentId, Pageable pageable);

    @Query("SELECT g FROM Grade g WHERE g.course = :courseId")
    Page<Grade> findByCourse(@Param("courseId") UUID courseId, Pageable pageable);

    @Query("SELECT g FROM Grade g WHERE g.university = :universityCode")
    Page<Grade> findByUniversity(@Param("universityCode") String universityCode, Pageable pageable);

    @Query("SELECT g FROM Grade g WHERE g.teacher = :teacherId")
    Page<Grade> findByTeacher(@Param("teacherId") UUID teacherId, Pageable pageable);

    @Query("SELECT g FROM Grade g WHERE g.student = :studentId AND g.course = :courseId")
    Page<Grade> findByStudentAndCourse(@Param("studentId") UUID studentId, @Param("courseId") UUID courseId, Pageable pageable);

    @Query("SELECT g FROM Grade g WHERE g.academicYear = :year")
    Page<Grade> findByAcademicYear(@Param("year") String year, Pageable pageable);

    @Query("SELECT g FROM Grade g WHERE g.semester = :semester")
    Page<Grade> findBySemester(@Param("semester") Integer semester, Pageable pageable);

    @Query("SELECT g FROM Grade g WHERE g.isPassed = true")
    Page<Grade> findPassedGrades(Pageable pageable);

    @Query("SELECT g FROM Grade g WHERE g.isFinalized = true")
    Page<Grade> findFinalizedGrades(Pageable pageable);

    @Query("SELECT g FROM Grade g WHERE g.student = :studentId AND g.academicYear = :year")
    Page<Grade> findByStudentAndAcademicYear(@Param("studentId") UUID studentId, @Param("year") String year, Pageable pageable);

    @Query("SELECT AVG(g.gradePoints) FROM Grade g WHERE g.student = :studentId AND g.isPassed = true")
    Double calculateGPA(@Param("studentId") UUID studentId);

    @Query("SELECT COUNT(g) FROM Grade g WHERE g.student = :studentId")
    long countByStudent(@Param("studentId") UUID studentId);

    @Query("SELECT COUNT(g) FROM Grade g WHERE g.course = :courseId")
    long countByCourse(@Param("courseId") UUID courseId);
}
