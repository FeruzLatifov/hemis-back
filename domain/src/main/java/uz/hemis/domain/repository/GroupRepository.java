package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.Group;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface GroupRepository extends JpaRepository<Group, UUID> {

    // NO DELETE METHODS (NDG)

    Optional<Group> findByName(String name);

    @Query("SELECT g FROM Group g WHERE g.university = :universityCode")
    Page<Group> findByUniversity(@Param("universityCode") String universityCode, Pageable pageable);

    @Query("SELECT g FROM Group g WHERE g.specialty = :specialtyId")
    Page<Group> findBySpecialty(@Param("specialtyId") UUID specialtyId, Pageable pageable);

    @Query("SELECT g FROM Group g WHERE g.faculty = :facultyId")
    Page<Group> findByFaculty(@Param("facultyId") UUID facultyId, Pageable pageable);

    @Query("SELECT g FROM Group g WHERE g.active = true")
    Page<Group> findByActiveTrue(Pageable pageable);

    @Query("SELECT g FROM Group g WHERE g.academicYear = :year")
    Page<Group> findByAcademicYear(@Param("year") String year, Pageable pageable);

    @Query("SELECT g FROM Group g WHERE g.course = :course")
    Page<Group> findByCourse(@Param("course") Integer course, Pageable pageable);

    @Query("SELECT COUNT(g) FROM Group g WHERE g.university = :universityCode")
    long countByUniversity(@Param("universityCode") String universityCode);

    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM Group g WHERE g.name = :name")
    boolean existsByName(@Param("name") String name);
}
