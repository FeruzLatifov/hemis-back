package uz.hemis.domain.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public class EmployeeJobUpsertRepositoryImpl implements EmployeeJobUpsertRepository {

    // Defensive code-resolution: Univer per-OTM kod yuboradi (klassifikator markazdan
    // tarqatilgani uchun odatda mos), lekin drift bo'lsa (markaz klassifikatorida hali
    // yo'q kod) xom INSERT FK violation berib butun batch'ni DLQ'ga tushirardi. Shuning
    // o'rniga har FK kod resolving subquery orqali o'tadi → mavjud bo'lsa kod, aks holda
    // NULL (FK nullable). position_type_code Univer'dan kelmaydi — h_position.type_code
    // dan derive qilinadi (explicit kelsa, COALESCE u bilan ustun).
    private static final String UPSERT_SQL = """
            INSERT INTO employee_job (
                id, employee_id, university_code, source_uid,
                department_code, position_code, position_type_code,
                start_date, end_date, contract_number,
                is_current, synced_at,
                version, created_at, created_by, updated_at, updated_by
            ) VALUES (
                gen_random_uuid(), :employeeId, :universityCode, :sourceUid,
                (SELECT d.code FROM hemishe_e_university_department d
                    WHERE d.code = :departmentCode AND d.delete_ts IS NULL),
                (SELECT p.code FROM h_position p WHERE p.code = :positionCode),
                COALESCE(
                    (SELECT pt.code FROM h_position_type pt WHERE pt.code = :positionTypeCode),
                    (SELECT p.type_code FROM h_position p WHERE p.code = :positionCode)
                ),
                :startDate, :endDate, :contractNumber,
                :isCurrent, :now,
                1, :now, :auditUser, NULL, NULL
            )
            ON CONFLICT (university_code, source_uid)
                WHERE source_uid IS NOT NULL AND deleted_at IS NULL
            DO UPDATE SET
                employee_id        = EXCLUDED.employee_id,
                department_code    = EXCLUDED.department_code,
                position_code      = EXCLUDED.position_code,
                position_type_code = EXCLUDED.position_type_code,
                start_date         = EXCLUDED.start_date,
                end_date           = EXCLUDED.end_date,
                contract_number    = EXCLUDED.contract_number,
                is_current         = EXCLUDED.is_current,
                synced_at          = EXCLUDED.synced_at,
                updated_at         = EXCLUDED.synced_at,
                updated_by         = :auditUser,
                version            = employee_job.version + 1
            RETURNING id
            """;

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public UUID upsertFromSync(
            UUID employeeId,
            String universityCode,
            String sourceUid,
            String departmentCode,
            String positionCode,
            String positionTypeCode,
            LocalDate startDate,
            LocalDate endDate,
            String contractNumber,
            Boolean isCurrent,
            LocalDateTime now,
            String auditUser
    ) {
        Object id = em.createNativeQuery(UPSERT_SQL)
                .setParameter("employeeId", employeeId)
                .setParameter("universityCode", universityCode)
                .setParameter("sourceUid", sourceUid)
                .setParameter("departmentCode", departmentCode)
                .setParameter("positionCode", positionCode)
                .setParameter("positionTypeCode", positionTypeCode)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("contractNumber", contractNumber)
                .setParameter("isCurrent", isCurrent)
                .setParameter("now", now)
                .setParameter("auditUser", auditUser)
                .getSingleResult();
        return (UUID) id;
    }
}
