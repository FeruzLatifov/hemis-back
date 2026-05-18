package uz.hemis.domain.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Native PostgreSQL upsert for {@code employee} — atomic INSERT ON CONFLICT.
 *
 * <p>Conflict key — partial UNIQUE index {@code uq_employee_pinfl(pinfl) WHERE deleted_at IS NULL}.
 * Soft-deleted xodim PINFL'ini qayta ishlatish (rehire) — yangi row yaratiladi.</p>
 *
 * <p>JPA {@code save()} (SELECT + UPDATE + Hibernate {@code @Version}) o'rniga
 * native upsert ishlatiladi: 224 OTM bir xil PINFL'ni concurrent push qilsa,
 * row-level lock PostgreSQL ichida — Hibernate {@code OptimisticLockException} chiqmaydi.</p>
 *
 * @since ADR-0010
 */
public interface EmployeeUpsertRepository {

    /**
     * Atomic upsert: INSERT yangi row, yoki mavjud row'ni UPDATE ({@code pinfl} bo'yicha).
     *
     * @return employee.id (insert yoki update — RETURNING id)
     */
    UUID upsertFromSync(
            String pinfl,
            String firstName,
            String lastName,
            String middleName,
            LocalDate birthDate,
            String genderCode,
            String citizenshipCode,
            String nationalityCode,
            String passport,
            String email,
            String address,
            String academicDegreeCode,
            String academicRankCode,
            LocalDateTime now,
            String auditUser
    );
}
