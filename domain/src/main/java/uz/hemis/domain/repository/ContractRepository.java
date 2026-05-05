package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.finance.Contract;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
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

    /**
     * Check whether student has any UNPAID contract — boolean predicate
     * (avoids loading full contract list just to test {@code anyMatch(!isFullyPaid)}).
     *
     * <p>Mirror of {@link Contract#isFullyPaid} predicate at SQL level:
     * paidSum NULL/contractSum NULL/paidSum &lt; contractSum → unpaid.</p>
     *
     * <p>Used by {@code StudentCubaService.checkScholarship} — single SQL boolean
     * instead of fetching N contracts and iterating in JVM.</p>
     */
    @Query("SELECT (COUNT(c) > 0) FROM Contract c "
            + "WHERE c.student = :studentId "
            + "  AND (c.paidSum IS NULL OR c.contractSum IS NULL OR c.paidSum < c.contractSum)")
    boolean existsUnpaidByStudent(@Param("studentId") UUID studentId);

    /**
     * Atomic payment increment — race-condition free.
     *
     * <p>OWASP A04 fix: avval {@code findById + setPaidSum + save} pattern lost-update
     * qilardi (concurrent kassa + portal: ikkala transaction same {@code paidSum}+amount
     * o'qiydi → oxirgi save'i wins, biri yo'qoladi).</p>
     *
     * <p>Endi: bitta atomic UPDATE — {@code paid_sum = paid_sum + :amount}. Single
     * round-trip, DB-level row lock guaranteed. {@code @Modifying}+{@code @Query}
     * standart Spring Data pattern.</p>
     *
     * <p><strong>WHERE clause:</strong> {@code paid_sum + :amount &lt;= contract_sum}
     * — overflow yopiladi (over-payment refused, return value 0). Caller checks rows
     * affected: 0 → throw ConflictException (insufficient remaining).</p>
     *
     * @param contractId contract UUID
     * @param amount payment amount (must be &gt; 0; caller's responsibility)
     * @return rows updated (1 = success, 0 = constraint violation or contract missing)
     */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query(value = "UPDATE hemishe_e_contract "
            + "SET paid_sum = COALESCE(paid_sum, 0) + :amount, "
            + "    remaining_sum = contract_sum - (COALESCE(paid_sum, 0) + :amount), "
            + "    update_ts = CURRENT_TIMESTAMP, "
            + "    version = version + 1 "
            + "WHERE id = :contractId "
            + "  AND delete_ts IS NULL "
            + "  AND COALESCE(paid_sum, 0) + :amount <= contract_sum",
            nativeQuery = true)
    int addPayment(@Param("contractId") UUID contractId,
                   @Param("amount") java.math.BigDecimal amount);

    // NO DELETE METHODS (NDG - Non-Deletion Guarantee)
}
