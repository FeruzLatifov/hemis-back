package uz.hemis.domain.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Native PostgreSQL upsert for {@code employee_job} — atomic INSERT ON CONFLICT.
 *
 * <p>Conflict key — partial UNIQUE
 * {@code uq_ejob_univer_source(university_code, source_uid) WHERE source_uid IS NOT NULL AND deleted_at IS NULL}
 * (V014). Faqat Univer push qilgan row'lar (source_uid NOT NULL) uchun ishlaydi —
 * markazda admin UI orqali yaratilganlar (source_uid IS NULL) conflict yaratmaydi.</p>
 *
 * <p>UPDATE branch faqat Univer DTO'dan keladigan field'larni yangilaydi
 * (employment_form_code, employee_rate_code, contract_date, decree_*
 * — admin UI bilan tahrirlangan bo'lishi mumkin → tegmaymiz).</p>
 *
 * @since ADR-0010
 */
public interface EmployeeJobUpsertRepository {

    /**
     * Atomic upsert. {@code sourceUid} NOT NULL bo'lishi shart (bu metod sync uchun).
     */
    UUID upsertFromSync(
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
    );
}
