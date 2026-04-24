package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.enums.CredentialType;
import uz.hemis.domain.entity.employee.EmployeeAcademicCredential;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link EmployeeAcademicCredential} — SAC API sync target.
 *
 * <p>Primary use-case: idempotent UPSERT from SAC API response using
 * {@code (employee_id, diploma_number)} as the natural key.</p>
 *
 * @since 2.1.0
 */
@Repository
@Transactional(readOnly = true)
public interface EmployeeAcademicCredentialRepository
        extends JpaRepository<EmployeeAcademicCredential, UUID> {

    /** Natural upsert key — matches the {@code uq_eac_diploma} UNIQUE constraint. */
    Optional<EmployeeAcademicCredential> findByEmployeeIdAndDiplomaNumber(
            UUID employeeId, String diplomaNumber);

    /** All credentials (degrees + titles) for an employee, eagerly loading classifiers. */
    @EntityGraph(attributePaths = {"degree", "rank"})
    @Query("SELECT c FROM EmployeeAcademicCredential c WHERE c.employee.id = :employeeId " +
           "ORDER BY c.confirmedDate DESC NULLS LAST")
    List<EmployeeAcademicCredential> findByEmployeeId(@Param("employeeId") UUID employeeId);

    /** Only DEGREE or only TITLE rows for an employee. */
    @EntityGraph(attributePaths = {"degree", "rank"})
    @Query("SELECT c FROM EmployeeAcademicCredential c " +
           "WHERE c.employee.id = :employeeId AND c.credentialType = :type " +
           "ORDER BY c.confirmedDate DESC NULLS LAST")
    List<EmployeeAcademicCredential> findByEmployeeIdAndType(
            @Param("employeeId") UUID employeeId,
            @Param("type") CredentialType type);

    /**
     * Find the highest-ranking DEGREE for denormalised {@code employee.academic_degree_code}.
     * Uses the classifier's natural ordering if defined; caller may refine.
     */
    @Query("SELECT c FROM EmployeeAcademicCredential c " +
           "WHERE c.employee.id = :employeeId AND c.credentialType = 'DEGREE' " +
           "ORDER BY c.confirmedDate DESC NULLS LAST")
    List<EmployeeAcademicCredential> findDegreesNewestFirst(@Param("employeeId") UUID employeeId);

    /** Same as above, for TITLE rows. */
    @Query("SELECT c FROM EmployeeAcademicCredential c " +
           "WHERE c.employee.id = :employeeId AND c.credentialType = 'TITLE' " +
           "ORDER BY c.confirmedDate DESC NULLS LAST")
    List<EmployeeAcademicCredential> findTitlesNewestFirst(@Param("employeeId") UUID employeeId);

    long countByEmployeeIdAndCredentialType(UUID employeeId, CredentialType type);
}
