package uz.hemis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.StudentMetaDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.StudentMeta;
import uz.hemis.domain.repository.StudentMetaRepository;
import uz.hemis.service.mapper.StudentMetaMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * StudentMeta Service - Business Logic Layer
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Business logic and validation</li>
 *   <li>Transaction management</li>
 *   <li>Entity ↔ DTO conversion (via StudentMetaMapper)</li>
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
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StudentMetaService {

    private final StudentMetaRepository studentMetaRepository;
    private final StudentMetaMapper studentMetaMapper;

    // =====================================================
    // Read Operations (Read-Only Transactions)
    // =====================================================

    /**
     * Find student meta by ID
     *
     * @param id student meta ID
     * @return student meta DTO
     * @throws ResourceNotFoundException if not found
     */
    @Cacheable(value = "studentMetas", key = "#id", unless = "#result == null")
    public StudentMetaDto findById(UUID id) {
        log.debug("Finding student meta by ID: {} (cache miss)", id);

        StudentMeta studentMeta = studentMetaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StudentMeta", "id", id));

        return studentMetaMapper.toDto(studentMeta);
    }

    /**
     * Find student meta by uId and university (unique business key)
     *
     * @param uId university-specific ID
     * @param university university code
     * @return student meta DTO
     * @throws ResourceNotFoundException if not found
     */
    @Cacheable(value = "studentMetas", key = "'uid:' + #uId + ':' + #university", unless = "#result == null")
    public StudentMetaDto findByUIdAndUniversity(Integer uId, String university) {
        log.debug("Finding student meta by uId: {} and university: {} (cache miss)", uId, university);

        StudentMeta studentMeta = studentMetaRepository.findByUIdAndUniversity(uId, university)
                .orElseThrow(() -> new ResourceNotFoundException("StudentMeta", "uId/university", uId + "/" + university));

        return studentMetaMapper.toDto(studentMeta);
    }

    /**
     * Find all student metas (paginated)
     *
     * @param pageable pagination parameters
     * @return page of student meta DTOs
     */
    public Page<StudentMetaDto> findAll(Pageable pageable) {
        log.debug("Finding all student metas with pagination: {}", pageable);

        Page<StudentMeta> studentMetas = studentMetaRepository.findAll(pageable);
        return studentMetas.map(studentMetaMapper::toDto);
    }

    /**
     * Find student metas by university code
     *
     * @param universityCode university code
     * @param pageable pagination parameters
     * @return page of student meta DTOs
     */
    public Page<StudentMetaDto> findByUniversity(String universityCode, Pageable pageable) {
        log.debug("Finding student metas by university: {}", universityCode);

        Page<StudentMeta> studentMetas = studentMetaRepository.findByUniversity(universityCode, pageable);
        return studentMetas.map(studentMetaMapper::toDto);
    }

    /**
     * Find all student metas for a student
     *
     * @param studentId student UUID
     * @return list of student meta DTOs
     */
    public List<StudentMetaDto> findByStudent(UUID studentId) {
        log.debug("Finding student metas by student: {}", studentId);

        List<StudentMeta> studentMetas = studentMetaRepository.findByStudent(studentId);
        return studentMetaMapper.toDtoList(studentMetas);
    }

    /**
     * Find active student metas for a student
     *
     * @param studentId student UUID
     * @return list of active student meta DTOs
     */
    public List<StudentMetaDto> findActiveByStudent(UUID studentId) {
        log.debug("Finding active student metas by student: {}", studentId);

        List<StudentMeta> studentMetas = studentMetaRepository.findActiveByStudent(studentId);
        return studentMetaMapper.toDtoList(studentMetas);
    }

    /**
     * Find active student metas by university
     *
     * @param universityCode university code
     * @return list of student meta DTOs
     */
    public List<StudentMetaDto> findActiveByUniversity(String universityCode) {
        log.debug("Finding active student metas by university: {}", universityCode);

        List<StudentMeta> studentMetas = studentMetaRepository.findActiveByUniversity(universityCode);
        return studentMetaMapper.toDtoList(studentMetas);
    }

    /**
     * Count student metas by university
     *
     * @param universityCode university code
     * @return count
     */
    public long countByUniversity(String universityCode) {
        log.debug("Counting student metas for university: {}", universityCode);

        return studentMetaRepository.countByUniversity(universityCode);
    }

    /**
     * Check if student meta exists by uId and university
     *
     * @param uId university-specific ID
     * @param university university code
     * @return true if exists
     */
    public boolean existsByUIdAndUniversity(Integer uId, String university) {
        return studentMetaRepository.existsByUIdAndUniversity(uId, university);
    }

    // =====================================================
    // Write Operations (Read-Write Transactions)
    // =====================================================

    /**
     * Create new student meta
     *
     * <p><strong>Business Validations:</strong></p>
     * <ul>
     *   <li>(uId, university) must be unique</li>
     *   <li>If uId is null, auto-generate next available</li>
     * </ul>
     *
     * @param dto student meta DTO
     * @return created student meta DTO
     * @throws ValidationException if business validation fails
     */
    @Transactional
    @CacheEvict(value = "studentMetas", allEntries = true)
    public StudentMetaDto create(StudentMetaDto dto) {
        log.info("Creating new student meta for university: {}", dto.getUniversity());

        // Auto-generate uId if not provided
        if (dto.getUId() == null && dto.getUniversity() != null) {
            Integer maxUId = studentMetaRepository.findMaxUIdByUniversity(dto.getUniversity());
            dto.setUId(maxUId != null ? maxUId + 1 : 1);
            log.debug("Auto-generated uId: {} for university: {}", dto.getUId(), dto.getUniversity());
        }

        // Check for duplicate (uId, university)
        if (dto.getUId() != null && dto.getUniversity() != null) {
            if (studentMetaRepository.existsByUIdAndUniversity(dto.getUId(), dto.getUniversity())) {
                throw new ValidationException("StudentMeta with uId " + dto.getUId() +
                        " already exists for university " + dto.getUniversity());
            }
        }

        StudentMeta entity = studentMetaMapper.toEntity(dto);
        StudentMeta saved = studentMetaRepository.save(entity);

        log.info("Created student meta with ID: {} and uId: {}", saved.getId(), saved.getUId());
        return studentMetaMapper.toDto(saved);
    }

    /**
     * Update existing student meta
     *
     * @param id student meta ID
     * @param dto updated student meta DTO
     * @return updated student meta DTO
     * @throws ResourceNotFoundException if not found
     * @throws ValidationException if business validation fails
     */
    @Transactional
    @CacheEvict(value = "studentMetas", allEntries = true)
    public StudentMetaDto update(UUID id, StudentMetaDto dto) {
        log.info("Updating student meta: {}", id);

        StudentMeta existing = studentMetaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StudentMeta", "id", id));

        // Check if uId/university is being changed and would cause conflict
        if (dto.getUId() != null && dto.getUniversity() != null) {
            if (!dto.getUId().equals(existing.getUId()) || !dto.getUniversity().equals(existing.getUniversity())) {
                if (studentMetaRepository.existsByUIdAndUniversity(dto.getUId(), dto.getUniversity())) {
                    throw new ValidationException("StudentMeta with uId " + dto.getUId() +
                            " already exists for university " + dto.getUniversity());
                }
            }
        }

        studentMetaMapper.updateEntityFromDto(dto, existing);
        StudentMeta saved = studentMetaRepository.save(existing);

        log.info("Updated student meta: {}", id);
        return studentMetaMapper.toDto(saved);
    }

    /**
     * Partial update student meta (PATCH)
     *
     * @param id student meta ID
     * @param dto partial student meta DTO
     * @return updated student meta DTO
     * @throws ResourceNotFoundException if not found
     */
    @Transactional
    @CacheEvict(value = "studentMetas", allEntries = true)
    public StudentMetaDto partialUpdate(UUID id, StudentMetaDto dto) {
        log.info("Partial updating student meta: {}", id);

        StudentMeta existing = studentMetaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StudentMeta", "id", id));

        studentMetaMapper.partialUpdate(dto, existing);
        StudentMeta saved = studentMetaRepository.save(existing);

        log.info("Partial updated student meta: {}", id);
        return studentMetaMapper.toDto(saved);
    }

    /**
     * Soft delete student meta
     *
     * <p><strong>CRITICAL - NDG (Non-Deletion Guarantee):</strong></p>
     * <ul>
     *   <li>Only sets deleteTs to current timestamp</li>
     *   <li>NO physical DELETE from database</li>
     *   <li>Record remains in DB but filtered by @Where clause</li>
     * </ul>
     *
     * @param id student meta ID
     * @throws ResourceNotFoundException if not found
     */
    @Transactional
    @CacheEvict(value = "studentMetas", allEntries = true)
    public void softDelete(UUID id) {
        log.info("Soft deleting student meta: {}", id);

        StudentMeta existing = studentMetaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StudentMeta", "id", id));

        existing.setDeleteTs(LocalDateTime.now());
        // deletedBy should be set by audit callback or SecurityContext
        studentMetaRepository.save(existing);

        log.info("Soft deleted student meta: {}", id);
    }

    // =====================================================
    // Entity Access (for legacy/internal use)
    // =====================================================

    /**
     * Find student meta entity by ID (internal use)
     *
     * @param id student meta ID
     * @return Optional of StudentMeta entity
     */
    public Optional<StudentMeta> findEntityById(UUID id) {
        return studentMetaRepository.findById(id);
    }

    /**
     * Save student meta entity directly (for legacy compatibility)
     *
     * @param entity student meta entity
     * @return saved entity
     */
    @Transactional
    @CacheEvict(value = "studentMetas", allEntries = true)
    public StudentMeta saveEntity(StudentMeta entity) {
        return studentMetaRepository.save(entity);
    }
}
