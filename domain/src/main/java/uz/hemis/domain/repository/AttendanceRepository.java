package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.Attendance;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    @Query("SELECT a FROM Attendance a WHERE a.student = :studentId")
    Page<Attendance> findByStudent(@Param("studentId") UUID studentId, Pageable pageable);

    @Query("SELECT a FROM Attendance a WHERE a.course = :courseId")
    Page<Attendance> findByCourse(@Param("courseId") UUID courseId, Pageable pageable);

    @Query("SELECT a FROM Attendance a WHERE a.group = :groupId")
    Page<Attendance> findByGroup(@Param("groupId") UUID groupId, Pageable pageable);

    @Query("SELECT a FROM Attendance a WHERE a.attendanceDate = :date")
    Page<Attendance> findByDate(@Param("date") LocalDate date, Pageable pageable);

    @Query("SELECT a FROM Attendance a WHERE a.student = :studentId AND a.course = :courseId")
    Page<Attendance> findByStudentAndCourse(@Param("studentId") UUID studentId, @Param("courseId") UUID courseId, Pageable pageable);

    @Query("SELECT a FROM Attendance a WHERE a.group = :groupId AND a.attendanceDate = :date")
    List<Attendance> findByGroupAndDate(@Param("groupId") UUID groupId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student = :studentId AND a.isPresent = true")
    long countPresentByStudent(@Param("studentId") UUID studentId);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student = :studentId AND a.course = :courseId")
    long countByStudentAndCourse(@Param("studentId") UUID studentId, @Param("courseId") UUID courseId);
}
