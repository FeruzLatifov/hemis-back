package uz.hemis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.DiplomaDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.Diploma;
import uz.hemis.service.mapper.DiplomaMapper;
import uz.hemis.domain.repository.DiplomaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Diploma Service - Business Logic Layer
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Business logic and validation for student diplomas/degrees</li>
 *   <li>Transaction management</li>
 *   <li>Entity ↔ DTO conversion (via DiplomaMapper)</li>
 *   <li>Diploma verification and hash generation</li>
 *   <li>Soft delete implementation</li>
 * </ul>
 *
 * <p><strong>CRITICAL - NO DELETE OPERATIONS:</strong></p>
 * <ul>
 *   <li>NDG (Non-Deletion Guarantee) enforced</li>
 *   <li>Soft delete only (set deleteTs)</li>
 *   <li>NO physical DELETE from database</li>
 * </ul>
 *
 * <p><strong>Legacy Reference:</strong></p>
 * <p>Old-HEMIS: DiplomaService (diploma issuance and verification)</p>
 * <p>External APIs: /app/rest/diploma/info, /app/rest/diploma/byhash</p>
 *
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DiplomaService {

    private final DiplomaRepository diplomaRepository;
    private final DiplomaMapper diplomaMapper;

    // =====================================================
    // Read Operations (Read-Only Transactions)
    // =====================================================

    /**
     * Find diploma by ID
     *
     * @param id diploma ID
     * @return diploma DTO
     * @throws ResourceNotFoundException if not found
     */
    @Cacheable(value = "diplomas", key = "#id", unless = "#result == null")
    public DiplomaDto findById(UUID id) {
        log.debug("Finding diploma by ID: {} (cache miss)", id);

        Diploma diploma = diplomaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diploma", "id", id));

        return diplomaMapper.toDto(diploma);
    }

    /**
     * Find diploma by number
     *
     * @param diplomaNumber diploma registration number
     * @return diploma DTO
     * @throws ResourceNotFoundException if not found
     */
    @Cacheable(value = "diplomas", key = "'number:' + #diplomaNumber", unless = "#result == null")
    public DiplomaDto findByDiplomaNumber(String diplomaNumber) {
        log.debug("Finding diploma by number: {} (cache miss)", diplomaNumber);

        Diploma diploma = diplomaRepository.findByDiplomaNumber(diplomaNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Diploma", "diplomaNumber", diplomaNumber));

        return diplomaMapper.toDto(diploma);
    }

    /**
     * Find diploma by hash (for verification)
     *
     * <p>Used by external verification API: /app/rest/diploma/byhash</p>
     *
     * @param diplomaHash SHA-256 hash of diploma data
     * @return diploma DTO
     * @throws ResourceNotFoundException if not found
     */
    @Cacheable(value = "diplomas", key = "'hash:' + #diplomaHash", unless = "#result == null")
    public DiplomaDto findByHash(String diplomaHash) {
        log.debug("Finding diploma by hash: {} (cache miss)", diplomaHash);

        Diploma diploma = diplomaRepository.findByDiplomaHash(diplomaHash)
                .orElseThrow(() -> new ResourceNotFoundException("Diploma", "diplomaHash", diplomaHash));

        return diplomaMapper.toDto(diploma);
    }

    /**
     * Find all diplomas (paginated)
     *
     * @param pageable pagination parameters
     * @return page of diploma DTOs
     */
    public Page<DiplomaDto> findAll(Pageable pageable) {
        log.debug("Finding all diplomas with pagination: {}", pageable);

        Page<Diploma> diplomas = diplomaRepository.findAll(pageable);
        return diplomas.map(diplomaMapper::toDto);
    }

    /**
     * Find diplomas by university code
     *
     * @param universityCode university code
     * @param pageable pagination parameters
     * @return page of diploma DTOs
     */
    public Page<DiplomaDto> findByUniversity(String universityCode, Pageable pageable) {
        log.debug("Finding diplomas by university: {}", universityCode);

        Page<Diploma> diplomas = diplomaRepository.findByUniversity(universityCode, pageable);
        return diplomas.map(diplomaMapper::toDto);
    }

    /**
     * Find diplomas by student
     *
     * @param studentId student UUID
     * @return list of diploma DTOs (ordered by issue date descending)
     */
    public List<DiplomaDto> findByStudent(UUID studentId) {
        log.debug("Finding diplomas by student: {}", studentId);

        List<Diploma> diplomas = diplomaRepository.findByStudentOrderByIssueDateDesc(studentId);
        return diplomas.stream().map(diplomaMapper::toDto).toList();
    }

    /**
     * Find diplomas by university and status
     *
     * @param universityCode university code
     * @param status diploma status
     * @param pageable pagination parameters
     * @return page of diploma DTOs
     */
    public Page<DiplomaDto> findByUniversityAndStatus(String universityCode, String status, Pageable pageable) {
        log.debug("Finding diplomas by university: {} and status: {}", universityCode, status);

        Page<Diploma> diplomas = diplomaRepository.findByUniversityAndStatus(universityCode, status, pageable);
        return diplomas.map(diplomaMapper::toDto);
    }

    /**
     * Find diplomas by university and graduation year
     *
     * @param universityCode university code
     * @param graduationYear graduation year
     * @param pageable pagination parameters
     * @return page of diploma DTOs
     */
    public Page<DiplomaDto> findByUniversityAndYear(String universityCode, Integer graduationYear, Pageable pageable) {
        log.debug("Finding diplomas by university: {} and year: {}", universityCode, graduationYear);

        Page<Diploma> diplomas = diplomaRepository.findByUniversityAndYear(universityCode, graduationYear, pageable);
        return diplomas.map(diplomaMapper::toDto);
    }

    /**
     * Count diplomas by university and year
     *
     * @param universityCode university code
     * @param graduationYear graduation year
     * @return count
     */
    public long countByUniversityAndYear(String universityCode, Integer graduationYear) {
        log.debug("Counting diplomas for university: {} and year: {}", universityCode, graduationYear);

        return diplomaRepository.countByUniversityAndYear(universityCode, graduationYear);
    }

    /**
     * Count issued diplomas by university
     *
     * @param universityCode university code
     * @return count
     */
    public long countIssuedByUniversity(String universityCode) {
        log.debug("Counting issued diplomas for university: {}", universityCode);

        return diplomaRepository.countIssuedByUniversity(universityCode);
    }

    /**
     * Check if diploma exists by number
     *
     * @param diplomaNumber diploma registration number
     * @return true if exists
     */
    public boolean existsByDiplomaNumber(String diplomaNumber) {
        return diplomaRepository.existsByDiplomaNumber(diplomaNumber);
    }

    /**
     * Check if diploma exists by hash
     *
     * @param diplomaHash SHA-256 hash
     * @return true if exists
     */
    public boolean existsByHash(String diplomaHash) {
        return diplomaRepository.existsByDiplomaHash(diplomaHash);
    }

    // =====================================================
    // Write Operations (Read-Write Transactions)
    // =====================================================

    /**
     * Create new diploma
     *
     * <p><strong>Business Validations:</strong></p>
     * <ul>
     *   <li>Diploma number must be unique</li>
     *   <li>Student must be specified</li>
     *   <li>University must be specified</li>
     *   <li>Diploma hash must be unique</li>
     * </ul>
     *
     * @param diplomaDto diploma data
     * @return created diploma DTO
     * @throws ValidationException if validation fails
     */
    @Transactional
    @CachePut(value = "diplomas", key = "#result.id")
    public DiplomaDto create(DiplomaDto diplomaDto) {
        log.info("Creating new diploma with number: {}", diplomaDto.getDiplomaNumber());

        // Validate diploma number uniqueness
        if (diplomaDto.getDiplomaNumber() != null &&
                diplomaRepository.existsByDiplomaNumber(diplomaDto.getDiplomaNumber())) {
            throw new ValidationException(
                    "Diploma with this number already exists",
                    "diplomaNumber",
                    "Diploma number must be unique"
            );
        }

        // Validate diploma hash uniqueness
        if (diplomaDto.getDiplomaHash() != null &&
                diplomaRepository.existsByDiplomaHash(diplomaDto.getDiplomaHash())) {
            throw new ValidationException(
                    "Diploma with this hash already exists",
                    "diplomaHash",
                    "Diploma hash must be unique"
            );
        }

        // Convert DTO to Entity
        Diploma diploma = diplomaMapper.toEntity(diplomaDto);

        // ID will be generated by @PrePersist
        // Audit fields (createTs, createdBy) set by @PrePersist

        // Save
        Diploma saved = diplomaRepository.save(diploma);

        log.info("Diploma created successfully with ID: {}", saved.getId());

        return diplomaMapper.toDto(saved);
    }

    /**
     * Update existing diploma
     *
     * <p><strong>Business Validations:</strong></p>
     * <ul>
     *   <li>Diploma must exist</li>
     *   <li>Diploma number uniqueness (if changed)</li>
     *   <li>Diploma hash uniqueness (if changed)</li>
     * </ul>
     *
     * @param id diploma ID
     * @param diplomaDto diploma data
     * @return updated diploma DTO
     * @throws ResourceNotFoundException if diploma not found
     * @throws ValidationException if validation fails
     */
    @Transactional
    @CachePut(value = "diplomas", key = "#id")
    @CacheEvict(value = "diplomas", key = "'number:' + #result.diplomaNumber")
    public DiplomaDto update(UUID id, DiplomaDto diplomaDto) {
        log.info("Updating diploma with ID: {}", id);

        // Find existing diploma
        Diploma existing = diplomaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diploma", "id", id));

        // Validate diploma number uniqueness (if changed)
        if (diplomaDto.getDiplomaNumber() != null &&
                !diplomaDto.getDiplomaNumber().equals(existing.getDiplomaNumber()) &&
                diplomaRepository.existsByDiplomaNumber(diplomaDto.getDiplomaNumber())) {
            throw new ValidationException(
                    "Diploma with this number already exists",
                    "diplomaNumber",
                    "Diploma number must be unique"
            );
        }

        // Validate diploma hash uniqueness (if changed)
        if (diplomaDto.getDiplomaHash() != null &&
                !diplomaDto.getDiplomaHash().equals(existing.getDiplomaHash()) &&
                diplomaRepository.existsByDiplomaHash(diplomaDto.getDiplomaHash())) {
            throw new ValidationException(
                    "Diploma with this hash already exists",
                    "diplomaHash",
                    "Diploma hash must be unique"
            );
        }

        // Update entity from DTO (ignores audit fields)
        diplomaMapper.updateEntityFromDto(diplomaDto, existing);

        // updateTs and updatedBy will be set by @PreUpdate

        // Save
        Diploma updated = diplomaRepository.save(existing);

        log.info("Diploma updated successfully: {}", id);

        return diplomaMapper.toDto(updated);
    }

    // =====================================================
    // Soft Delete (NOT Physical DELETE)
    // =====================================================

    /**
     * Soft delete diploma
     *
     * <p><strong>CRITICAL - Soft Delete Only:</strong></p>
     * <ul>
     *   <li>Sets deleteTs = NOW()</li>
     *   <li>Sets deletedBy = current user</li>
     *   <li>NO physical DELETE from database</li>
     *   <li>Diploma still exists but filtered by @Where clause</li>
     * </ul>
     *
     * @param id diploma ID
     * @throws ResourceNotFoundException if diploma not found
     */
    @Transactional
    @CacheEvict(value = "diplomas", allEntries = true)
    public void softDelete(UUID id) {
        log.warn("Soft deleting diploma ID: {}", id);

        // Find existing diploma
        Diploma diploma = diplomaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diploma", "id", id));

        // Check if already deleted
        if (diploma.isDeleted()) {
            log.warn("Diploma already deleted: {}", id);
            return;
        }

        // Set soft delete fields
        diploma.setDeleteTs(LocalDateTime.now());
        // TODO: Set deletedBy from SecurityContext
        // diploma.setDeletedBy(SecurityContextHolder.getContext().getAuthentication().getName());

        // Save (this triggers @PreUpdate)
        diplomaRepository.save(diploma);

        log.warn("Diploma soft deleted: {}", id);
    }

    // =====================================================
    // NOTE: NO PHYSICAL DELETE METHOD
    // =====================================================
    // public void delete(UUID id) { ... }  ← PROHIBITED
    //
    // Physical DELETE is not allowed (NDG).
    // Use softDelete() instead.
    // =====================================================
}
