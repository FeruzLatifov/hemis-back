package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.UniversityDepartment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Repository for UniversityDepartment (Bo'lim/Kafedra)
 *
 * <p>Primary key: code (String, not UUID)</p>
 * <p>Master/Replica: All read operations routed to Replica</p>
 *
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface UniversityDepartmentRepository extends JpaRepository<UniversityDepartment, String> {

    Optional<UniversityDepartment> findByCode(String code);

    /**
     * Find by code INCLUDING soft-deleted entities.
     * Ignores @Where clause to find deleted entities for UPSERT logic.
     *
     * @param code Entity code
     * @return Entity (including deleted ones)
     */
    @Query(value = "SELECT * FROM hemishe_e_university_department WHERE code = :code", nativeQuery = true)
    Optional<UniversityDepartment> findByCodeIncludingDeleted(@Param("code") String code);

    List<UniversityDepartment> findByUniversityCode(String universityCode);

    Page<UniversityDepartment> findByUniversityCode(String universityCode, Pageable pageable);

    List<UniversityDepartment> findByStatusTrue();

    List<UniversityDepartment> findByUniversityCodeAndStatusTrue(String universityCode);

    List<UniversityDepartment> findByNameUzContainingIgnoreCase(String name);

    long countByUniversityCode(String universityCode);
}
