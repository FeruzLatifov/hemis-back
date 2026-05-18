package uz.hemis.service.employee;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.employee.EmployeeSyncDto;
import uz.hemis.common.vo.Pinfl;
import uz.hemis.domain.repository.EmployeeJobsRepository;
import uz.hemis.domain.repository.EmployeeRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Single-item upsert (Kafka consumer chaqiradi).
 *
 * <p>Tx scope = 1 row → bitta xodim xato chiqarsa, qolgan row'lar ta'sirlanmaydi.</p>
 *
 * <p>Native PostgreSQL {@code INSERT ON CONFLICT} ishlatadi → Hibernate
 * {@code @Version} optimistic lock chetlanadi. 224 OTM bir xil PINFL'ni
 * concurrent push qilsa ham — DB row-level lock atomik.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeSyncProcessor {

    private final EmployeeRepository employeeRepo;
    private final EmployeeJobsRepository jobsRepo;

    @Transactional
    public ProcessResult process(String universityCode, EmployeeSyncDto dto, String auditUser) {
        if (!Pinfl.isValid(dto.getPinfl())) {
            throw new IllegalArgumentException("Invalid PINFL: " + mask(dto.getPinfl()));
        }

        LocalDateTime now = LocalDateTime.now();

        UUID empId = employeeRepo.upsertFromSync(
                dto.getPinfl(),
                dto.getFirstName(),
                dto.getLastName(),
                dto.getMiddleName(),
                dto.getBirthDate(),
                nullIfBlank(dto.getGenderCode()),
                nullIfBlank(dto.getCitizenshipCode()),
                nullIfBlank(dto.getNationalityCode()),
                nullIfBlank(dto.getPassport()),
                nullIfBlank(dto.getEmail()),
                dto.getAddress(),
                nullIfBlank(dto.getAcademicDegreeCode()),
                nullIfBlank(dto.getAcademicRankCode()),
                now,
                auditUser
        );

        UUID jobId = null;
        if (dto.getSourceUid() != null && !dto.getSourceUid().isBlank()) {
            jobId = jobsRepo.upsertFromSync(
                    empId,
                    universityCode,
                    dto.getSourceUid(),
                    nullIfBlank(dto.getDepartmentCode()),
                    nullIfBlank(dto.getPositionCode()),
                    nullIfBlank(dto.getPositionTypeCode()),
                    dto.getStartDate(),
                    dto.getEndDate(),
                    nullIfBlank(dto.getContractNumber()),
                    Optional.ofNullable(dto.getIsCurrent()).orElse(Boolean.TRUE),
                    now,
                    auditUser
            );
        }

        log.debug("Sync upserted: pinfl={} sourceUid={} empId={} jobId={}",
                mask(dto.getPinfl()), dto.getSourceUid(), empId, jobId);

        return new ProcessResult(empId, jobId);
    }

    public record ProcessResult(UUID employeeId, UUID jobId) {
        public boolean hasJob() {
            return jobId != null;
        }
    }

    private static String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private static String mask(String pinfl) {
        if (pinfl == null || pinfl.length() < 6) return "***";
        return pinfl.substring(0, 4) + "****" + pinfl.substring(pinfl.length() - 2);
    }
}
