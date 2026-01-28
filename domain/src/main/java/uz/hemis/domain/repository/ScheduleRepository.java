package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.Schedule;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {

    // NO DELETE METHODS (NDG)

    @Query("SELECT s FROM Schedule s WHERE s.group = :groupId")
    Page<Schedule> findByGroup(@Param("groupId") UUID groupId, Pageable pageable);

    @Query("SELECT s FROM Schedule s WHERE s.teacher = :teacherId")
    Page<Schedule> findByTeacher(@Param("teacherId") UUID teacherId, Pageable pageable);

    @Query("SELECT s FROM Schedule s WHERE s.university = :universityCode")
    Page<Schedule> findByUniversity(@Param("universityCode") String universityCode, Pageable pageable);

    @Query("SELECT s FROM Schedule s WHERE s.scheduleDate = :date")
    Page<Schedule> findByScheduleDate(@Param("date") LocalDate date, Pageable pageable);

    @Query("SELECT s FROM Schedule s WHERE s.group = :groupId AND s.scheduleDate = :date")
    List<Schedule> findByGroupAndDate(@Param("groupId") UUID groupId, @Param("date") LocalDate date);

    @Query("SELECT s FROM Schedule s WHERE s.teacher = :teacherId AND s.scheduleDate = :date")
    List<Schedule> findByTeacherAndDate(@Param("teacherId") UUID teacherId, @Param("date") LocalDate date);

    @Query("SELECT s FROM Schedule s WHERE s.auditorium = :auditoriumId AND s.scheduleDate = :date")
    List<Schedule> findByAuditoriumAndDate(@Param("auditoriumId") UUID auditoriumId, @Param("date") LocalDate date);

    @Query("SELECT s FROM Schedule s WHERE s.academicYear = :year")
    Page<Schedule> findByAcademicYear(@Param("year") String year, Pageable pageable);

    @Query("SELECT s FROM Schedule s WHERE s.active = true AND s.isCancelled = false")
    Page<Schedule> findActiveSchedules(Pageable pageable);

    @Query("SELECT COUNT(s) FROM Schedule s WHERE s.group = :groupId")
    long countByGroup(@Param("groupId") UUID groupId);
}
