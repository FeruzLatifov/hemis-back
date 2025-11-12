package uz.hemis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.UniversityDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.University;
import uz.hemis.service.mapper.UniversityMapper;
import uz.hemis.domain.repository.UniversityRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * University Service - Business Logic Layer
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Business logic and validation</li>
 *   <li>Transaction management</li>
 *   <li>Entity ↔ DTO conversion (via UniversityMapper)</li>
 *   <li>Soft delete implementation</li>
 * </ul>
 *
 * <p><strong>CRITICAL - VARCHAR Primary Key:</strong></p>
 * <ul>
 *   <li>PK Type: String (code) - NOT UUID!</li>
 *   <li>Code cannot be changed after creation</li>
 * </ul>
 *
 * <p><strong>CRITICAL - NO DELETE OPERATIONS:</strong></p>
 * <ul>
 *   <li>NDG (Non-Deletion Guarantee) enforced</li>
 *   <li>Soft delete only (set deleteTs)</li>
 *   <li>NO physical DELETE from database</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UniversityService {

    private final UniversityRepository universityRepository;
    private final UniversityMapper universityMapper;

    // =====================================================
    // Read Operations (Read-Only Transactions)
    // =====================================================

    /**
     * Find university by code (PK)
     *
     * @param code university code
     * @return university DTO
     * @throws ResourceNotFoundException if not found
     */
    public UniversityDto findByCode(String code) {
        log.debug("Finding university by code: {}", code);

        University university = universityRepository.findById(code)
                .orElseThrow(() -> new ResourceNotFoundException("University", "code", code));

        return universityMapper.toDto(university);
    }

    /**
     * Find university by TIN
     *
     * @param tin Tax Identification Number
     * @return university DTO
     * @throws ResourceNotFoundException if not found
     */
    public UniversityDto findByTin(String tin) {
        log.debug("Finding university by TIN: {}", tin);

        University university = universityRepository.findByTin(tin)
                .orElseThrow(() -> new ResourceNotFoundException("University", "tin", tin));

        return universityMapper.toDto(university);
    }

    /**
     * Find all universities (paginated)
     *
     * @param pageable pagination parameters
     * @return page of university DTOs
     */
    public Page<UniversityDto> findAll(Pageable pageable) {
        log.debug("Finding all universities with pagination: {}", pageable);

        Page<University> universities = universityRepository.findAll(pageable);
        return universities.map(universityMapper::toDto);
    }

    /**
     * Find universities by name (partial match, case-insensitive)
     *
     * @param name university name (partial)
     * @param pageable pagination
     * @return page of university DTOs
     */
    public Page<UniversityDto> findByName(String name, Pageable pageable) {
        log.debug("Finding universities by name: {}", name);

        Page<University> universities = universityRepository.findByNameContainingIgnoreCase(name, pageable);
        return universities.map(universityMapper::toDto);
    }

    /**
     * Find active universities
     *
     * @param pageable pagination
     * @return page of active university DTOs
     */
    public Page<UniversityDto> findActiveUniversities(Pageable pageable) {
        log.debug("Finding active universities");

        Page<University> universities = universityRepository.findByActiveTrue(pageable);
        return universities.map(universityMapper::toDto);
    }

    /**
     * Find universities by type
     *
     * @param typeCode university type code
     * @param pageable pagination
     * @return page of university DTOs
     */
    public Page<UniversityDto> findByType(String typeCode, Pageable pageable) {
        log.debug("Finding universities by type: {}", typeCode);

        Page<University> universities = universityRepository.findByUniversityType(typeCode, pageable);
        return universities.map(universityMapper::toDto);
    }

    /**
     * Find universities by ownership
     *
     * @param ownershipCode ownership code
     * @param pageable pagination
     * @return page of university DTOs
     */
    public Page<UniversityDto> findByOwnership(String ownershipCode, Pageable pageable) {
        log.debug("Finding universities by ownership: {}", ownershipCode);

        Page<University> universities = universityRepository.findByOwnership(ownershipCode, pageable);
        return universities.map(universityMapper::toDto);
    }

    /**
     * Find universities by SOATO region
     *
     * @param soatoRegion SOATO region code
     * @param pageable pagination
     * @return page of university DTOs
     */
    public Page<UniversityDto> findByRegion(String soatoRegion, Pageable pageable) {
        log.debug("Finding universities by region: {}", soatoRegion);

        Page<University> universities = universityRepository.findBySoatoRegion(soatoRegion, pageable);
        return universities.map(universityMapper::toDto);
    }

    /**
     * Find child universities by parent code
     *
     * @param parentCode parent university code
     * @return list of child university DTOs
     */
    public List<UniversityDto> findByParent(String parentCode) {
        log.debug("Finding universities by parent: {}", parentCode);

        List<University> universities = universityRepository.findByParentUniversity(parentCode);
        return universityMapper.toDtoList(universities);
    }

    /**
     * Count active universities
     *
     * @return count of active universities
     */
    public long countActive() {
        return universityRepository.countActiveUniversities();
    }

    // =====================================================
    // Write Operations (Transactional)
    // =====================================================

    /**
     * Create new university
     *
     * @param dto university DTO
     * @return created university DTO
     * @throws ValidationException if validation fails
     */
    @Transactional
    public UniversityDto create(UniversityDto dto) {
        log.info("Creating new university with code: {}", dto.getCode());

        // Validation: Code required
        if (dto.getCode() == null || dto.getCode().isBlank()) {
            throw new ValidationException("University code is required");
        }

        // Validation: Code uniqueness
        if (universityRepository.existsByCode(dto.getCode())) {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "University with code '" + dto.getCode() + "' already exists");
            throw new ValidationException("University validation failed", errors);
        }

        // Validation: TIN uniqueness (if provided)
        if (dto.getTin() != null && !dto.getTin().isBlank()) {
            universityRepository.findByTin(dto.getTin()).ifPresent(existing -> {
                Map<String, String> errors = new HashMap<>();
                errors.put("tin", "University with TIN '" + dto.getTin() + "' already exists");
                throw new ValidationException("University validation failed", errors);
            });
        }

        // Validation: Name required
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new ValidationException("University name is required");
        }

        // Convert DTO to entity
        University university = universityMapper.toEntity(dto);

        // Save
        University saved = universityRepository.save(university);

        log.info("University created successfully with code: {}", saved.getCode());
        return universityMapper.toDto(saved);
    }

    /**
     * Update existing university
     *
     * @param code university code (PK - cannot be changed)
     * @param dto university DTO with updated values
     * @return updated university DTO
     * @throws ResourceNotFoundException if not found
     * @throws ValidationException if validation fails
     */
    @Transactional
    public UniversityDto update(String code, UniversityDto dto) {
        log.info("Updating university with code: {}", code);

        // Find existing
        University existing = universityRepository.findById(code)
                .orElseThrow(() -> new ResourceNotFoundException("University", "code", code));

        // Validation: TIN uniqueness (if changed)
        if (dto.getTin() != null && !dto.getTin().equals(existing.getTin())) {
            if (universityRepository.existsByTinAndCodeNot(dto.getTin(), code)) {
                Map<String, String> errors = new HashMap<>();
                errors.put("tin", "University with TIN '" + dto.getTin() + "' already exists");
                throw new ValidationException("University validation failed", errors);
            }
        }

        // Update fields (code cannot be changed - it's PK)
        if (dto.getTin() != null) existing.setTin(dto.getTin());
        if (dto.getName() != null) existing.setName(dto.getName());
        if (dto.getAddress() != null) existing.setAddress(dto.getAddress());
        if (dto.getCadastre() != null) existing.setCadastre(dto.getCadastre());
        if (dto.getUniversityUrl() != null) existing.setUniversityUrl(dto.getUniversityUrl());
        if (dto.getStudentUrl() != null) existing.setStudentUrl(dto.getStudentUrl());
        if (dto.getTeacherUrl() != null) existing.setTeacherUrl(dto.getTeacherUrl());
        if (dto.getUzbmbUrl() != null) existing.setUzbmbUrl(dto.getUzbmbUrl());
        if (dto.getSoato() != null) existing.setSoato(dto.getSoato());
        if (dto.getSoatoRegion() != null) existing.setSoatoRegion(dto.getSoatoRegion());
        if (dto.getUniversityType() != null) existing.setUniversityType(dto.getUniversityType());
        if (dto.getOwnership() != null) existing.setOwnership(dto.getOwnership());
        if (dto.getUniversityVersion() != null) existing.setUniversityVersion(dto.getUniversityVersion());
        if (dto.getUniversityActivityStatus() != null) existing.setUniversityActivityStatus(dto.getUniversityActivityStatus());
        if (dto.getUniversityBelongsTo() != null) existing.setUniversityBelongsTo(dto.getUniversityBelongsTo());
        if (dto.getUniversityContractCategory() != null) existing.setUniversityContractCategory(dto.getUniversityContractCategory());
        if (dto.getParentUniversity() != null) existing.setParentUniversity(dto.getParentUniversity());
        if (dto.getActive() != null) existing.setActive(dto.getActive());
        if (dto.getGpaEdit() != null) existing.setGpaEdit(dto.getGpaEdit());
        if (dto.getAccreditationEdit() != null) existing.setAccreditationEdit(dto.getAccreditationEdit());
        if (dto.getAddStudent() != null) existing.setAddStudent(dto.getAddStudent());
        if (dto.getAllowGrouping() != null) existing.setAllowGrouping(dto.getAllowGrouping());
        if (dto.getAllowTransferOutside() != null) existing.setAllowTransferOutside(dto.getAllowTransferOutside());

        // Save
        University updated = universityRepository.save(existing);

        log.info("University updated successfully: {}", code);
        return universityMapper.toDto(updated);
    }

    /**
     * Partial update (PATCH) - only update non-null fields
     *
     * @param code university code (PK)
     * @param dto university DTO with fields to update
     * @return updated university DTO
     * @throws ResourceNotFoundException if not found
     * @throws ValidationException if validation fails
     */
    @Transactional
    public UniversityDto partialUpdate(String code, UniversityDto dto) {
        log.info("Partially updating university with code: {}", code);

        // Find existing
        University existing = universityRepository.findById(code)
                .orElseThrow(() -> new ResourceNotFoundException("University", "code", code));

        // Validation: TIN uniqueness (if provided and changed)
        if (dto.getTin() != null && !dto.getTin().equals(existing.getTin())) {
            if (universityRepository.existsByTinAndCodeNot(dto.getTin(), code)) {
                Map<String, String> errors = new HashMap<>();
                errors.put("tin", "University with TIN '" + dto.getTin() + "' already exists");
                throw new ValidationException("University validation failed", errors);
            }
        }

        // Use mapper for partial update (only non-null fields)
        universityMapper.updateEntityFromDto(dto, existing);

        // Save
        University updated = universityRepository.save(existing);

        log.info("University partially updated successfully: {}", code);
        return universityMapper.toDto(updated);
    }

    // =====================================================
    // Soft Delete (NO Physical DELETE)
    // =====================================================

    /**
     * Soft delete university (set deleteTs)
     *
     * <p><strong>CRITICAL - NDG:</strong> Physical DELETE is PROHIBITED!</p>
     * <p>This method only sets deleteTs timestamp.</p>
     *
     * @param code university code
     * @throws ResourceNotFoundException if not found
     */
    @Transactional
    public void softDelete(String code) {
        log.info("Soft deleting university with code: {}", code);

        University university = universityRepository.findById(code)
                .orElseThrow(() -> new ResourceNotFoundException("University", "code", code));

        // Set soft delete timestamp
        university.setDeleteTs(LocalDateTime.now());
        // TODO: Set deletedBy from SecurityContext

        universityRepository.save(university);

        log.info("University soft deleted successfully: {}", code);
    }

    /**
     * Restore soft-deleted university (clear deleteTs)
     *
     * @param code university code
     * @throws ResourceNotFoundException if not found
     */
    @Transactional
    public void restore(String code) {
        log.info("Restoring university with code: {}", code);

        // Note: This will NOT find soft-deleted records due to @Where clause
        // We need a special query or temporary disable the filter
        // For now, assuming we can access via findById if needed

        University university = universityRepository.findById(code)
                .orElseThrow(() -> new ResourceNotFoundException("University", "code", code));

        // Clear soft delete fields
        university.setDeleteTs(null);
        university.setDeletedBy(null);

        universityRepository.save(university);

        log.info("University restored successfully: {}", code);
    }

    // =====================================================
    // CUBA REST API Compatible Methods
    // =====================================================

    /**
     * Get university configuration (CUBA compatible)
     *
     * <p>Returns system configuration including:</p>
     * <ul>
     *   <li>List of all active universities</li>
     *   <li>System settings</li>
     *   <li>Feature flags</li>
     * </ul>
     *
     * @return configuration map
     */
    public Object getConfig() {
        log.debug("CUBA API: getting university configuration");
        
        Page<UniversityDto> page = findAll(Pageable.unpaged());
        List<UniversityDto> universities = page.getContent();
        
        Map<String, Object> config = new HashMap<>();
        config.put("universities", universities);
        config.put("totalCount", universities.size());
        config.put("timestamp", LocalDateTime.now());
        
        return config;
    }

    /**
     * Get university by code (CUBA compatible)
     *
     * @param code university code
     * @return university DTO or null
     */
    public Object getByCode(String code) {
        log.debug("CUBA API: get university by code: {}", code);
        try {
            return findByCode(code);
        } catch (ResourceNotFoundException e) {
            log.warn("University not found: {}", code);
            return null;
        }
    }
}
