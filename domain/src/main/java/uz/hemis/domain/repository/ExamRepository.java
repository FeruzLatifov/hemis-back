package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.Exam;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface ExamRepository extends JpaRepository<Exam, UUID> {

    @Query("SELECT e FROM Exam e WHERE e.course = :courseId")
    Page<Exam> findByCourse(@Param("courseId") UUID courseId, Pageable pageable);

    @Query("SELECT e FROM Exam e WHERE e.group = :groupId")
    Page<Exam> findByGroup(@Param("groupId") UUID groupId, Pageable pageable);

    @Query("SELECT e FROM Exam e WHERE e.teacher = :teacherId")
    Page<Exam> findByTeacher(@Param("teacherId") UUID teacherId, Pageable pageable);

    @Query("SELECT e FROM Exam e WHERE e.university = :universityCode")
    Page<Exam> findByUniversity(@Param("universityCode") String universityCode, Pageable pageable);

    @Query("SELECT e FROM Exam e WHERE e.examDate = :date")
    Page<Exam> findByExamDate(@Param("date") LocalDate date, Pageable pageable);

    @Query("SELECT e FROM Exam e WHERE e.examType = :type")
    Page<Exam> findByExamType(@Param("type") String type, Pageable pageable);

    @Query("SELECT e FROM Exam e WHERE e.academicYear = :year")
    Page<Exam> findByAcademicYear(@Param("year") String year, Pageable pageable);

    @Query("SELECT e FROM Exam e WHERE e.active = true AND e.isPublished = true")
    Page<Exam> findPublishedExams(Pageable pageable);

    @Query("SELECT e FROM Exam e WHERE e.group = :groupId AND e.examDate = :date")
    List<Exam> findByGroupAndDate(@Param("groupId") UUID groupId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(e) FROM Exam e WHERE e.course = :courseId")
    long countByCourse(@Param("courseId") UUID courseId);
}
