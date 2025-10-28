package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.Enrollment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    // NO DELETE METHODS (NDG)

    Optional<Enrollment> findByEnrollmentNumber(String enrollmentNumber);

    @Query("SELECT e FROM Enrollment e WHERE e.student = :studentId")
    Page<Enrollment> findByStudent(@Param("studentId") UUID studentId, Pageable pageable);

    @Query("SELECT e FROM Enrollment e WHERE e.university = :universityCode")
    Page<Enrollment> findByUniversity(@Param("universityCode") String universityCode, Pageable pageable);

    @Query("SELECT e FROM Enrollment e WHERE e.specialty = :specialtyId")
    Page<Enrollment> findBySpecialty(@Param("specialtyId") UUID specialtyId, Pageable pageable);

    @Query("SELECT e FROM Enrollment e WHERE e.faculty = :facultyId")
    Page<Enrollment> findByFaculty(@Param("facultyId") UUID facultyId, Pageable pageable);

    @Query("SELECT e FROM Enrollment e WHERE e.active = true")
    Page<Enrollment> findByActiveTrue(Pageable pageable);

    @Query("SELECT e FROM Enrollment e WHERE e.academicYear = :year")
    Page<Enrollment> findByAcademicYear(@Param("year") String year, Pageable pageable);

    @Query("SELECT e FROM Enrollment e WHERE e.enrollmentStatus = :status")
    Page<Enrollment> findByEnrollmentStatus(@Param("status") String status, Pageable pageable);

    @Query("SELECT e FROM Enrollment e WHERE e.university = :universityCode AND e.academicYear = :year")
    Page<Enrollment> findByUniversityAndAcademicYear(@Param("universityCode") String universityCode, @Param("year") String year, Pageable pageable);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.university = :universityCode")
    long countByUniversity(@Param("universityCode") String universityCode);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.specialty = :specialtyId")
    long countBySpecialty(@Param("specialtyId") UUID specialtyId);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Enrollment e WHERE e.enrollmentNumber = :number")
    boolean existsByEnrollmentNumber(@Param("number") String number);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Enrollment e WHERE e.enrollmentNumber = :number AND e.id != :id")
    boolean existsByEnrollmentNumberAndIdNot(@Param("number") String number, @Param("id") UUID id);
}
