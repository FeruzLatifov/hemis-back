package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.AcademicSubjects;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface AcademicSubjectsRepository extends JpaRepository<AcademicSubjects, UUID> {

    List<AcademicSubjects> findByUniversityCode(String universityCode);

    Page<AcademicSubjects> findByUniversityCode(String universityCode, Pageable pageable);

    List<AcademicSubjects> findByEducationYearCode(String educationYearCode);

    List<AcademicSubjects> findByEducationTypeCode(String educationTypeCode);

    List<AcademicSubjects> findByCurriculumCode(String curriculumCode);

    List<AcademicSubjects> findByBlockCode(String blockCode);
}
