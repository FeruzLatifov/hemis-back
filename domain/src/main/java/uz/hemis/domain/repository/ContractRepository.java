package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.hemis.domain.entity.Contract;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {

    Optional<Contract> findByContractNumber(String contractNumber);

    List<Contract> findByStudent(UUID studentId);

    Page<Contract> findByUniversity(String universityCode, Pageable pageable);

    @Query("SELECT c FROM Contract c WHERE c.university = :universityCode AND c.educationYear = :year")
    Page<Contract> findByUniversityAndYear(@Param("universityCode") String universityCode, @Param("year") String year, Pageable pageable);

    @Query("SELECT c FROM Contract c WHERE c.university = :universityCode AND c.status = :status")
    Page<Contract> findByUniversityAndStatus(@Param("universityCode") String universityCode, @Param("status") String status, Pageable pageable);

    @Query("SELECT c FROM Contract c WHERE c.student = :studentId AND c.status = 'ACTIVE' AND c.isActive = true")
    List<Contract> findActiveByStudent(@Param("studentId") UUID studentId);

    @Query("SELECT COUNT(c) FROM Contract c WHERE c.university = :universityCode AND c.educationYear = :year AND c.status = 'ACTIVE'")
    long countActiveByUniversityAndYear(@Param("universityCode") String universityCode, @Param("year") String year);

    @Query("SELECT SUM(c.contractSum) FROM Contract c WHERE c.university = :universityCode AND c.educationYear = :year")
    BigDecimal sumContractByUniversityAndYear(@Param("universityCode") String universityCode, @Param("year") String year);

    @Query("SELECT SUM(c.paidSum) FROM Contract c WHERE c.university = :universityCode AND c.educationYear = :year")
    BigDecimal sumPaidByUniversityAndYear(@Param("universityCode") String universityCode, @Param("year") String year);

    boolean existsByContractNumber(String contractNumber);

    // NO DELETE METHODS (NDG - Non-Deletion Guarantee)
}
