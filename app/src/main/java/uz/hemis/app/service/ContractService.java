package uz.hemis.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.ContractDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.Contract;
import uz.hemis.domain.mapper.ContractMapper;
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

    @Cacheable(value = "contracts", key = "#id", unless = "#result == null")
    public ContractDto findById(UUID id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", id));
        return contractMapper.toDto(contract);
    }

    @Cacheable(value = "contracts", key = "'number:' + #number", unless = "#result == null")
    public ContractDto findByContractNumber(String number) {
        Contract contract = contractRepository.findByContractNumber(number)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "contractNumber", number));
        return contractMapper.toDto(contract);
    }

    public Page<ContractDto> findAll(Pageable pageable) {
        return contractRepository.findAll(pageable).map(contractMapper::toDto);
    }

    public Page<ContractDto> findByUniversity(String universityCode, Pageable pageable) {
        return contractRepository.findByUniversity(universityCode, pageable).map(contractMapper::toDto);
    }

    public List<ContractDto> findByStudent(UUID studentId) {
        return contractMapper.toDtoList(contractRepository.findByStudent(studentId));
    }

    public List<ContractDto> findActiveByStudent(UUID studentId) {
        return contractMapper.toDtoList(contractRepository.findActiveByStudent(studentId));
    }

    public Page<ContractDto> findByUniversityAndYear(String universityCode, String year, Pageable pageable) {
        return contractRepository.findByUniversityAndYear(universityCode, year, pageable).map(contractMapper::toDto);
    }

    public BigDecimal sumContractByUniversityAndYear(String universityCode, String year) {
        BigDecimal sum = contractRepository.sumContractByUniversityAndYear(universityCode, year);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    public BigDecimal sumPaidByUniversityAndYear(String universityCode, String year) {
        BigDecimal sum = contractRepository.sumPaidByUniversityAndYear(universityCode, year);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Transactional
    @CachePut(value = "contracts", key = "#result.id")
    public ContractDto create(ContractDto contractDto) {
        log.info("Creating contract: {}", contractDto.getContractNumber());

        if (contractDto.getContractNumber() != null &&
                contractRepository.existsByContractNumber(contractDto.getContractNumber())) {
            throw new ValidationException("Contract with this number already exists", "contractNumber", "Number must be unique");
        }

        Contract contract = contractMapper.toEntity(contractDto);
        Contract saved = contractRepository.save(contract);
        log.info("Contract created: {}", saved.getId());
        return contractMapper.toDto(saved);
    }

    @Transactional
    @CachePut(value = "contracts", key = "#id")
    public ContractDto update(UUID id, ContractDto contractDto) {
        log.info("Updating contract: {}", id);

        Contract existing = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", id));

        contractMapper.updateEntityFromDto(contractDto, existing);
        Contract updated = contractRepository.save(existing);
        log.info("Contract updated: {}", id);
        return contractMapper.toDto(updated);
    }

    @Transactional
    @CacheEvict(value = "contracts", allEntries = true)
    public void softDelete(UUID id) {
        log.warn("Soft deleting contract: {}", id);
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", id));

        if (contract.isDeleted()) {
            log.warn("Contract already deleted: {}", id);
            return;
        }

        contract.setDeleteTs(LocalDateTime.now());
        contractRepository.save(contract);
        log.warn("Contract soft deleted: {}", id);
    }
}
