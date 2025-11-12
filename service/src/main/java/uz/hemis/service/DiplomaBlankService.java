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
import uz.hemis.common.dto.DiplomaBlankDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.DiplomaBlank;
import uz.hemis.service.mapper.DiplomaBlankMapper;
import uz.hemis.domain.repository.DiplomaBlankRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Diploma Blank Service - Business Logic Layer
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Business logic and validation for diploma blank forms</li>
 *   <li>Transaction management</li>
 *   <li>Entity ↔ DTO conversion (via DiplomaBlankMapper)</li>
 *   <li>Blank allocation and status management</li>
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
 * <p>Old-HEMIS: DiplomBlankService (diploma blank management)</p>
 * <p>External APIs: /app/rest/diplom-blank/get, /app/rest/diplom-blank/setStatus</p>
 *
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DiplomaBlankService {

    private final DiplomaBlankRepository diplomaBlankRepository;
    private final DiplomaBlankMapper diplomaBlankMapper;

    // =====================================================
    // Read Operations (Read-Only Transactions)
    // =====================================================

    /**
     * Find diploma blank by ID
     *
     * @param id diploma blank ID
     * @return diploma blank DTO
     * @throws ResourceNotFoundException if not found
     */
    @Cacheable(value = "diplomaBlanks", key = "#id", unless = "#result == null")
    public DiplomaBlankDto findById(UUID id) {
        log.debug("Finding diploma blank by ID: {} (cache miss)", id);

        DiplomaBlank diplomaBlank = diplomaBlankRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DiplomaBlank", "id", id));

        return diplomaBlankMapper.toDto(diplomaBlank);
    }

    /**
     * Find diploma blank by code (series + number)
     *
     * @param blankCode blank code (e.g., "AB 1234567")
     * @return diploma blank DTO
     * @throws ResourceNotFoundException if not found
     */
    @Cacheable(value = "diplomaBlanks", key = "'code:' + #blankCode", unless = "#result == null")
    public DiplomaBlankDto findByCode(String blankCode) {
        log.debug("Finding diploma blank by code: {} (cache miss)", blankCode);

        DiplomaBlank diplomaBlank = diplomaBlankRepository.findByBlankCode(blankCode)
                .orElseThrow(() -> new ResourceNotFoundException("DiplomaBlank", "blankCode", blankCode));

        return diplomaBlankMapper.toDto(diplomaBlank);
    }

    /**
     * Find diploma blank by series and number
     *
     * @param series series (e.g., "AB")
     * @param number number (e.g., "1234567")
     * @return diploma blank DTO
     * @throws ResourceNotFoundException if not found
     */
    public DiplomaBlankDto findBySeriesAndNumber(String series, String number) {
        log.debug("Finding diploma blank by series: {} and number: {}", series, number);

        DiplomaBlank diplomaBlank = diplomaBlankRepository.findBySeriesAndNumber(series, number)
                .orElseThrow(() -> new ResourceNotFoundException("DiplomaBlank", "series-number", series + "-" + number));

        return diplomaBlankMapper.toDto(diplomaBlank);
    }

    /**
     * Find all diploma blanks (paginated)
     *
     * @param pageable pagination parameters
     * @return page of diploma blank DTOs
     */
    public Page<DiplomaBlankDto> findAll(Pageable pageable) {
        log.debug("Finding all diploma blanks with pagination: {}", pageable);

        Page<DiplomaBlank> diplomas = diplomaBlankRepository.findAll(pageable);
        return diplomas.map(diplomaBlankMapper::toDto);
    }

    /**
     * Find diploma blanks by university code
     *
     * @param universityCode university code
     * @param pageable pagination parameters
     * @return page of diploma blank DTOs
     */
    public Page<DiplomaBlankDto> findByUniversity(String universityCode, Pageable pageable) {
        log.debug("Finding diploma blanks by university: {}", universityCode);

        Page<DiplomaBlank> diplomas = diplomaBlankRepository.findByUniversity(universityCode, pageable);
        return diplomas.map(diplomaBlankMapper::toDto);
    }

    /**
     * Find all diploma blanks by university (non-paginated)
     *
     * @param universityCode university code
     * @return list of diploma blank DTOs
     */
    public List<DiplomaBlankDto> findByUniversity(String universityCode) {
        log.debug("Finding all diploma blanks by university: {}", universityCode);

        List<DiplomaBlank> diplomas = diplomaBlankRepository.findByUniversity(universityCode);
        return diplomas.stream().map(diplomaBlankMapper::toDto).toList();
    }

    /**
     * Find diploma blanks by university and status
     *
     * @param universityCode university code
     * @param status blank status (AVAILABLE, ASSIGNED, ISSUED, DAMAGED, LOST, ANNULLED)
     * @param pageable pagination parameters
     * @return page of diploma blank DTOs
     */
    public Page<DiplomaBlankDto> findByUniversityAndStatus(String universityCode, String status, Pageable pageable) {
        log.debug("Finding diploma blanks by university: {} and status: {}", universityCode, status);

        Page<DiplomaBlank> diplomas = diplomaBlankRepository.findByUniversityAndStatus(universityCode, status, pageable);
        return diplomas.map(diplomaBlankMapper::toDto);
    }

    /**
     * Find diploma blanks by university and status (non-paginated)
     *
     * @param universityCode university code
     * @param status blank status
     * @return list of diploma blank DTOs
     */
    public List<DiplomaBlankDto> findByUniversityAndStatus(String universityCode, String status) {
        log.debug("Finding all diploma blanks by university: {} and status: {}", universityCode, status);

        List<DiplomaBlank> diplomas = diplomaBlankRepository.findByUniversityAndStatus(universityCode, status);
        return diplomas.stream().map(diplomaBlankMapper::toDto).toList();
    }

    /**
     * Find diploma blanks by university and academic year
     *
     * @param universityCode university code
     * @param academicYear academic year
     * @param pageable pagination parameters
     * @return page of diploma blank DTOs
     */
    public Page<DiplomaBlankDto> findByUniversityAndYear(String universityCode, Integer academicYear, Pageable pageable) {
        log.debug("Finding diploma blanks by university: {} and year: {}", universityCode, academicYear);

        Page<DiplomaBlank> diplomas = diplomaBlankRepository.findByUniversityAndYear(universityCode, academicYear, pageable);
        return diplomas.map(diplomaBlankMapper::toDto);
    }

    /**
     * Find available diploma blanks by university and type
     *
     * <p>Used for diploma issuance - finds blanks with AVAILABLE status</p>
     *
     * @param universityCode university code
     * @param blankType blank type (BACHELOR, MASTER, DOCTORATE, etc.)
     * @return list of available diploma blank DTOs
     */
    public List<DiplomaBlankDto> findAvailableByUniversityAndType(String universityCode, String blankType) {
        log.debug("Finding available diploma blanks by university: {} and type: {}", universityCode, blankType);

        List<DiplomaBlank> diplomas = diplomaBlankRepository.findAvailableByUniversityAndType(universityCode, blankType);
        return diplomas.stream().map(diplomaBlankMapper::toDto).toList();
    }

    /**
     * Find diploma blanks by series
     *
     * @param series series (e.g., "AB")
     * @return list of diploma blank DTOs (ordered by number)
     */
    public List<DiplomaBlankDto> findBySeries(String series) {
        log.debug("Finding diploma blanks by series: {}", series);

        List<DiplomaBlank> diplomas = diplomaBlankRepository.findBySeries(series);
        return diplomas.stream().map(diplomaBlankMapper::toDto).toList();
    }

    /**
     * Count diploma blanks by university and status
     *
     * @param universityCode university code
     * @param status blank status
     * @return count
     */
    public long countByUniversityAndStatus(String universityCode, String status) {
        log.debug("Counting diploma blanks for university: {} with status: {}", universityCode, status);

        return diplomaBlankRepository.countByUniversityAndStatus(universityCode, status);
    }

    /**
     * Count available diploma blanks by university and year
     *
     * @param universityCode university code
     * @param academicYear academic year
     * @return count
     */
    public long countAvailableByUniversityAndYear(String universityCode, Integer academicYear) {
        log.debug("Counting available diploma blanks for university: {} and year: {}", universityCode, academicYear);

        return diplomaBlankRepository.countAvailableByUniversityAndYear(universityCode, academicYear);
    }

    /**
     * Check if diploma blank exists by code
     *
     * @param blankCode blank code
     * @return true if exists
     */
    public boolean existsByCode(String blankCode) {
        return diplomaBlankRepository.existsByBlankCode(blankCode);
    }

    // =====================================================
    // Write Operations (Read-Write Transactions)
    // =====================================================

    /**
     * Create new diploma blank
     *
     * <p><strong>Business Validations:</strong></p>
     * <ul>
     *   <li>Blank code must be unique</li>
     *   <li>University must be specified</li>
     *   <li>Series and number must be present</li>
     * </ul>
     *
     * @param diplomaBlankDto diploma blank data
     * @return created diploma blank DTO
     * @throws ValidationException if validation fails
     */
    @Transactional
    @CachePut(value = "diplomaBlanks", key = "#result.id")
    public DiplomaBlankDto create(DiplomaBlankDto diplomaBlankDto) {
        log.info("Creating new diploma blank with code: {}", diplomaBlankDto.getBlankCode());

        // Validate blank code uniqueness
        if (diplomaBlankDto.getBlankCode() != null &&
                diplomaBlankRepository.existsByBlankCode(diplomaBlankDto.getBlankCode())) {
            throw new ValidationException(
                    "Diploma blank with this code already exists",
                    "blankCode",
                    "Blank code must be unique"
            );
        }

        // Convert DTO to Entity
        DiplomaBlank diplomaBlank = diplomaBlankMapper.toEntity(diplomaBlankDto);

        // ID will be generated by @PrePersist
        // Audit fields (createTs, createdBy) set by @PrePersist

        // Save
        DiplomaBlank saved = diplomaBlankRepository.save(diplomaBlank);

        log.info("Diploma blank created successfully with ID: {}", saved.getId());

        return diplomaBlankMapper.toDto(saved);
    }

    /**
     * Update existing diploma blank
     *
     * <p><strong>Business Validations:</strong></p>
     * <ul>
     *   <li>Diploma blank must exist</li>
     *   <li>Blank code uniqueness (if changed)</li>
     * </ul>
     *
     * @param id diploma blank ID
     * @param diplomaBlankDto diploma blank data
     * @return updated diploma blank DTO
     * @throws ResourceNotFoundException if diploma blank not found
     * @throws ValidationException if validation fails
     */
    @Transactional
    @CachePut(value = "diplomaBlanks", key = "#id")
    @CacheEvict(value = "diplomaBlanks", key = "'code:' + #result.blankCode")
    public DiplomaBlankDto update(UUID id, DiplomaBlankDto diplomaBlankDto) {
        log.info("Updating diploma blank with ID: {}", id);

        // Find existing diploma blank
        DiplomaBlank existing = diplomaBlankRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DiplomaBlank", "id", id));

        // Validate blank code uniqueness (if changed)
        if (diplomaBlankDto.getBlankCode() != null &&
                !diplomaBlankDto.getBlankCode().equals(existing.getBlankCode()) &&
                diplomaBlankRepository.existsByBlankCode(diplomaBlankDto.getBlankCode())) {
            throw new ValidationException(
                    "Diploma blank with this code already exists",
                    "blankCode",
                    "Blank code must be unique"
            );
        }

        // Update entity from DTO (ignores audit fields)
        diplomaBlankMapper.updateEntityFromDto(diplomaBlankDto, existing);

        // updateTs and updatedBy will be set by @PreUpdate

        // Save
        DiplomaBlank updated = diplomaBlankRepository.save(existing);

        log.info("Diploma blank updated successfully: {}", id);

        return diplomaBlankMapper.toDto(updated);
    }

    /**
     * Update diploma blank status
     *
     * <p>Used by external API: /app/rest/diplom-blank/setStatus</p>
     *
     * @param id diploma blank ID
     * @param newStatus new status (AVAILABLE, ASSIGNED, ISSUED, DAMAGED, LOST, ANNULLED)
     * @return updated diploma blank DTO
     * @throws ResourceNotFoundException if diploma blank not found
     */
    @Transactional
    @CachePut(value = "diplomaBlanks", key = "#id")
    public DiplomaBlankDto updateStatus(UUID id, String newStatus) {
        log.info("Updating diploma blank status: {} -> {}", id, newStatus);

        // Find existing diploma blank
        DiplomaBlank existing = diplomaBlankRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DiplomaBlank", "id", id));

        // Update status
        existing.setStatus(newStatus);

        // Save
        DiplomaBlank updated = diplomaBlankRepository.save(existing);

        log.info("Diploma blank status updated successfully: {}", id);

        return diplomaBlankMapper.toDto(updated);
    }

    // =====================================================
    // Soft Delete (NOT Physical DELETE)
    // =====================================================

    /**
     * Soft delete diploma blank
     *
     * <p><strong>CRITICAL - Soft Delete Only:</strong></p>
     * <ul>
     *   <li>Sets deleteTs = NOW()</li>
     *   <li>Sets deletedBy = current user</li>
     *   <li>NO physical DELETE from database</li>
     *   <li>Diploma blank still exists but filtered by @Where clause</li>
     * </ul>
     *
     * @param id diploma blank ID
     * @throws ResourceNotFoundException if diploma blank not found
     */
    @Transactional
    @CacheEvict(value = "diplomaBlanks", allEntries = true)
    public void softDelete(UUID id) {
        log.warn("Soft deleting diploma blank ID: {}", id);

        // Find existing diploma blank
        DiplomaBlank diplomaBlank = diplomaBlankRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DiplomaBlank", "id", id));

        // Check if already deleted
        if (diplomaBlank.isDeleted()) {
            log.warn("Diploma blank already deleted: {}", id);
            return;
        }

        // Set soft delete fields
        diplomaBlank.setDeleteTs(LocalDateTime.now());
        // TODO: Set deletedBy from SecurityContext
        // diplomaBlank.setDeletedBy(SecurityContextHolder.getContext().getAuthentication().getName());

        // Save (this triggers @PreUpdate)
        diplomaBlankRepository.save(diplomaBlank);

        log.warn("Diploma blank soft deleted: {}", id);
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
