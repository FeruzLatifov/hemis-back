package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.finance.ContractStatistics;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ContractStatistics Repository
 *
 * <p>Repository for hemishe_r_contract_statistics table operations.</p>
 *
 * <p><strong>OLD-HEMIS Compatible</strong> - 100% backward compatibility</p>
 *
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface ContractStatisticsRepository extends JpaRepository<ContractStatistics, UUID> {

    /**
     * Find all non-deleted contract statistics
     */
    List<ContractStatistics> findAllByDeleteTsIsNull();

    /**
     * Find by university code
     *
     * @param university university code
     * @return list of contract statistics for the university
     */
    List<ContractStatistics> findByUniversityAndDeleteTsIsNull(String university);

    /**
     * Find by university and education year
     *
     * @param university university code
     * @param educationYear education year code
     * @return list of contract statistics
     */
    List<ContractStatistics> findByUniversityAndEducationYearAndDeleteTsIsNull(
            String university, String educationYear);

    /**
     * Find first by all key fields (for duplicate check)
     * Uses findFirst to handle duplicate records in database
     *
     * @param university university code
     * @param educationYear education year code
     * @param educationType education type code
     * @param educationForm education form code
     * @param faculty faculty code
     * @param course course code
     * @param semester semester code
     * @param date statistics date
     * @return existing contract statistics if found
     */
    Optional<ContractStatistics> findFirstByUniversityAndEducationYearAndEducationTypeAndEducationFormAndFacultyAndCourseAndSemesterAndDateAndDeleteTsIsNull(
            String university, String educationYear, String educationType, String educationForm,
            String faculty, String course, String semester, LocalDate date);
}
