package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.academic.AcademicGroup;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface AcademicGroupRepository extends JpaRepository<AcademicGroup, UUID> {

    List<AcademicGroup> findByUniversityCode(String universityCode);

    Page<AcademicGroup> findByUniversityCode(String universityCode, Pageable pageable);

    List<AcademicGroup> findByEducationYearCode(String educationYearCode);

    List<AcademicGroup> findByEducationTypeCode(String educationTypeCode);

    List<AcademicGroup> findByEducationFormCode(String educationFormCode);
}
