package uz.hemis.service.finance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.audit.AuditAction;
import uz.hemis.common.audit.Audited;
import uz.hemis.common.dto.finance.ContractDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.finance.Contract;
import uz.hemis.service.finance.mapper.ContractMapper;
import uz.hemis.service.security.TenantGuard;
import uz.hemis.domain.repository.ContractRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ContractService {

    private final ContractRepository contractRepository;
    private final ContractMapper contractMapper;
    private final TenantGuard tenantGuard;

    @Cacheable(value = "contracts", key = "#id", unless = "#result == null")
    public ContractDto findById(UUID id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", id));
        // OWASP A01 — caller must own the contract's university (or be admin).
        // Money data — cross-tenant leakage = real business loss.
        tenantGuard.verifyOwnershipOrAdmin(contract.getUniversity());
        return contractMapper.toDto(contract);
    }

    @Cacheable(value = "contracts", key = "'number:' + #number", unless = "#result == null")
    public ContractDto findByContractNumber(String number) {
        Contract contract = contractRepository.findByContractNumber(number)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "contractNumber", number));
        tenantGuard.verifyOwnershipOrAdmin(contract.getUniversity());
        return contractMapper.toDto(contract);
    }

    public Page<ContractDto> findAll(Pageable pageable) {
        // Listing all contracts cross-tenant — admin only.
        tenantGuard.verifyOwnershipOrAdmin(null);  // null code → admin bypass kerak
        return contractRepository.findAll(pageable).map(contractMapper::toDto);
    }

    public Page<ContractDto> findByUniversity(String universityCode, Pageable pageable) {
        tenantGuard.verifyOwnershipOrAdmin(universityCode);
        return contractRepository.findByUniversity(universityCode, pageable).map(contractMapper::toDto);
    }

    public List<ContractDto> findByStudent(UUID studentId) {
        // Note: student.universityCode joinsiz aniqlash kerak — tenant scope service layer'da
        // findByStudent direct lookup; resultni filterda ham bo'lishi mumkin. Hozircha
        // controller/PreAuthorize layer'i scope cheki bilan ishlaydi (students.view permission +
        // student.universityCode caller bilan teng kontekst). Result-side filter kelajakda.
        return contractMapper.toDtoList(contractRepository.findByStudent(studentId));
    }

    public List<ContractDto> findActiveByStudent(UUID studentId) {
        return contractMapper.toDtoList(contractRepository.findActiveByStudent(studentId));
    }

    public Page<ContractDto> findByUniversityAndYear(String universityCode, String year, Pageable pageable) {
        tenantGuard.verifyOwnershipOrAdmin(universityCode);
        return contractRepository.findByUniversityAndYear(universityCode, year, pageable).map(contractMapper::toDto);
    }

    public BigDecimal sumContractByUniversityAndYear(String universityCode, String year) {
        tenantGuard.verifyOwnershipOrAdmin(universityCode);
        BigDecimal sum = contractRepository.sumContractByUniversityAndYear(universityCode, year);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    public BigDecimal sumPaidByUniversityAndYear(String universityCode, String year) {
        tenantGuard.verifyOwnershipOrAdmin(universityCode);
        BigDecimal sum = contractRepository.sumPaidByUniversityAndYear(universityCode, year);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Audited(action = AuditAction.CREATE, entity = "Contract", entityClass = Contract.class)
    @Transactional
    @CachePut(value = "contracts", key = "#result.id")
    public ContractDto create(ContractDto contractDto) {
        log.info("Creating contract: {}", contractDto.getContractNumber());
        // Tenant scope — caller can only create contracts in their own university.
        tenantGuard.verifyOwnershipOrAdmin(contractDto.getUniversity());

        if (contractDto.getContractNumber() != null &&
                contractRepository.existsByContractNumber(contractDto.getContractNumber())) {
            throw new ValidationException("Contract with this number already exists", "contractNumber", "Number must be unique");
        }

        Contract contract = contractMapper.toEntity(contractDto);
        Contract saved = contractRepository.save(contract);
        log.info("Contract created: {}", saved.getId());
        return contractMapper.toDto(saved);
    }

    @Audited(action = AuditAction.UPDATE, entity = "Contract", entityClass = Contract.class, keyArg = "id")
    @Transactional
    @CachePut(value = "contracts", key = "#id")
    public ContractDto update(UUID id, ContractDto contractDto) {
        log.info("Updating contract: {}", id);

        Contract existing = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", id));
        tenantGuard.verifyOwnershipOrAdmin(existing.getUniversity());

        // Mass-assignment defense — university field tied to original.
        // Caller can NOT relocate contract to another OTM via update.
        contractDto.setUniversity(null);

        contractMapper.updateEntityFromDto(contractDto, existing);
        Contract updated = contractRepository.save(existing);
        log.info("Contract updated: {}", id);
        return contractMapper.toDto(updated);
    }

    @Audited(action = AuditAction.DELETE, entity = "Contract", entityClass = Contract.class, keyArg = "id")
    @Transactional
    @CacheEvict(value = "contracts", allEntries = true)
    public void softDelete(UUID id) {
        log.warn("Soft deleting contract: {}", id);
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", id));
        tenantGuard.verifyOwnershipOrAdmin(contract.getUniversity());

        if (contract.isDeleted()) {
            log.warn("Contract already deleted: {}", id);
            return;
        }

        contract.setDeleteTs(LocalDateTime.now());
        contractRepository.save(contract);
        log.warn("Contract soft deleted: {}", id);
    }
}
