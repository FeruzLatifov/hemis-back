package uz.hemis.domain.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.infrastructure.UniversityCadastre;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link UniversityCadastre}. cad_number = umumiy noyob (bir mulk = bir yozuv).
 */
@Repository
@Transactional(readOnly = true)
public interface UniversityCadastreRepository extends JpaRepository<UniversityCadastre, UUID> {

    /** Ingest upsert + "bizda bormi" tekshiruvi. */
    Optional<UniversityCadastre> findByCadNumber(String cadNumber);

    boolean existsByCadNumber(String cadNumber);

    /** Retry job — API o'lik bo'lganda qolgan PENDING'lar (eng eski urinish avval). */
    List<UniversityCadastre> findByFetchStatusOrderByLastFetchAttemptAsc(String fetchStatus, Pageable pageable);

    /**
     * Berilgan cad_number'lar ichidan bazada allaqachon COMPLETE bo'lganlari — inkremental sync uchun
     * (force=false'da ularni kadastrdan qayta olib o'tirmaslik). Bitta IN-query (per-item lookup emas).
     */
    @Query("select c.cadNumber from UniversityCadastre c where c.cadNumber in :cadNumbers and c.fetchStatus = 'COMPLETE'")
    List<String> findCompleteCadNumbers(@Param("cadNumbers") List<String> cadNumbers);
}
