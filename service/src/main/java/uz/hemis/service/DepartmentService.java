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
import uz.hemis.common.dto.DepartmentDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.Department;
import uz.hemis.domain.mapper.DepartmentMapper;
import uz.hemis.domain.repository.DepartmentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Department Service - Business Logic Layer
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Business logic and validation for academic departments/chairs</li>
 *   <li>Transaction management</li>
 *   <li>Entity ↔ DTO conversion (via DepartmentMapper)</li>
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
 * <p>Old-HEMIS: CathedraService (department/chair management)</p>
 *
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    // =====================================================
    // Read Operations (Read-Only Transactions)
    // =====================================================

    /**
     * Find department by ID
     *
     * @param id department ID
     * @return department DTO
     * @throws ResourceNotFoundException if not found
     */
    @Cacheable(value = "departments", key = "#id", unless = "#result == null")
    public DepartmentDto findById(UUID id) {
        log.debug("Finding department by ID: {} (cache miss)", id);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        return departmentMapper.toDto(department);
    }

    /**
     * Find department by code
     *
     * @param departmentCode department code
     * @return department DTO
     * @throws ResourceNotFoundException if not found
     */
    @Cacheable(value = "departments", key = "'code:' + #departmentCode", unless = "#result == null")
    public DepartmentDto findByCode(String departmentCode) {
        log.debug("Finding department by code: {} (cache miss)", departmentCode);

        Department department = departmentRepository.findByDepartmentCode(departmentCode)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "departmentCode", departmentCode));

        return departmentMapper.toDto(department);
    }

    /**
     * Find all departments (paginated)
     *
     * @param pageable pagination parameters
     * @return page of department DTOs
     */
    public Page<DepartmentDto> findAll(Pageable pageable) {
        log.debug("Finding all departments with pagination: {}", pageable);

        Page<Department> departments = departmentRepository.findAll(pageable);
        return departments.map(departmentMapper::toDto);
    }

    /**
     * Find departments by university code
     *
     * @param universityCode university code
     * @param pageable pagination parameters
     * @return page of department DTOs
     */
    public Page<DepartmentDto> findByUniversity(String universityCode, Pageable pageable) {
        log.debug("Finding departments by university: {}", universityCode);

        Page<Department> departments = departmentRepository.findByUniversity(universityCode, pageable);
        return departments.map(departmentMapper::toDto);
    }

    /**
     * Find all departments by university (non-paginated)
     *
     * @param universityCode university code
     * @return list of department DTOs
     */
    public List<DepartmentDto> findByUniversity(String universityCode) {
        log.debug("Finding all departments by university: {}", universityCode);

        List<Department> departments = departmentRepository.findByUniversity(universityCode);
        return departments.stream().map(departmentMapper::toDto).toList();
    }

    /**
     * Find active departments by university
     *
     * @param universityCode university code
     * @return list of active department DTOs
     */
    public List<DepartmentDto> findActiveByUniversity(String universityCode) {
        log.debug("Finding active departments by university: {}", universityCode);

        List<Department> departments = departmentRepository.findActiveByUniversity(universityCode);
        return departments.stream().map(departmentMapper::toDto).toList();
    }

    /**
     * Find departments by faculty
     *
     * @param facultyId faculty UUID
     * @param pageable pagination parameters
     * @return page of department DTOs
     */
    public Page<DepartmentDto> findByFaculty(UUID facultyId, Pageable pageable) {
        log.debug("Finding departments by faculty: {}", facultyId);

        Page<Department> departments = departmentRepository.findByFaculty(facultyId, pageable);
        return departments.map(departmentMapper::toDto);
    }

    /**
     * Find active departments by faculty
     *
     * @param facultyId faculty UUID
     * @return list of active department DTOs
     */
    public List<DepartmentDto> findActiveByFaculty(UUID facultyId) {
        log.debug("Finding active departments by faculty: {}", facultyId);

        List<Department> departments = departmentRepository.findActiveByFaculty(facultyId);
        return departments.stream().map(departmentMapper::toDto).toList();
    }

    /**
     * Find department by head (department head teacher)
     *
     * @param headId teacher UUID
     * @return department DTO if found
     */
    public DepartmentDto findByHead(UUID headId) {
        log.debug("Finding department by head: {}", headId);

        Department department = departmentRepository.findByHead(headId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "head", headId));

        return departmentMapper.toDto(department);
    }

    /**
     * Count active departments by university
     *
     * @param universityCode university code
     * @return count
     */
    public long countActiveByUniversity(String universityCode) {
        log.debug("Counting active departments for university: {}", universityCode);

        return departmentRepository.countActiveByUniversity(universityCode);
    }

    /**
     * Count active departments by faculty
     *
     * @param facultyId faculty UUID
     * @return count
     */
    public long countActiveByFaculty(UUID facultyId) {
        log.debug("Counting active departments for faculty: {}", facultyId);

        return departmentRepository.countActiveByFaculty(facultyId);
    }

    /**
     * Check if department exists by code
     *
     * @param departmentCode department code
     * @return true if exists
     */
    public boolean existsByCode(String departmentCode) {
        return departmentRepository.existsByDepartmentCode(departmentCode);
    }

    // =====================================================
    // Write Operations (Read-Write Transactions)
    // =====================================================

    /**
     * Create new department
     *
     * <p><strong>Business Validations:</strong></p>
     * <ul>
     *   <li>Department code must be unique</li>
     *   <li>University must be specified</li>
     *   <li>Department name must be present</li>
     * </ul>
     *
     * @param departmentDto department data
     * @return created department DTO
     * @throws ValidationException if validation fails
     */
    @Transactional
    @CachePut(value = "departments", key = "#result.id")
    public DepartmentDto create(DepartmentDto departmentDto) {
        log.info("Creating new department with code: {}", departmentDto.getDepartmentCode());

        // Validate department code uniqueness
        if (departmentDto.getDepartmentCode() != null &&
                departmentRepository.existsByDepartmentCode(departmentDto.getDepartmentCode())) {
            throw new ValidationException(
                    "Department with this code already exists",
                    "departmentCode",
                    "Department code must be unique"
            );
        }

        // Convert DTO to Entity
        Department department = departmentMapper.toEntity(departmentDto);

        // ID will be generated by @PrePersist
        // Audit fields (createTs, createdBy) set by @PrePersist

        // Save
        Department saved = departmentRepository.save(department);

        log.info("Department created successfully with ID: {}", saved.getId());

        return departmentMapper.toDto(saved);
    }

    /**
     * Update existing department
     *
     * <p><strong>Business Validations:</strong></p>
     * <ul>
     *   <li>Department must exist</li>
     *   <li>Department code uniqueness (if changed)</li>
     * </ul>
     *
     * @param id department ID
     * @param departmentDto department data
     * @return updated department DTO
     * @throws ResourceNotFoundException if department not found
     * @throws ValidationException if validation fails
     */
    @Transactional
    @CachePut(value = "departments", key = "#id")
    @CacheEvict(value = "departments", key = "'code:' + #result.departmentCode")
    public DepartmentDto update(UUID id, DepartmentDto departmentDto) {
        log.info("Updating department with ID: {}", id);

        // Find existing department
        Department existing = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        // Validate department code uniqueness (if changed)
        if (departmentDto.getDepartmentCode() != null &&
                !departmentDto.getDepartmentCode().equals(existing.getDepartmentCode()) &&
                departmentRepository.existsByDepartmentCode(departmentDto.getDepartmentCode())) {
            throw new ValidationException(
                    "Department with this code already exists",
                    "departmentCode",
                    "Department code must be unique"
            );
        }

        // Update entity from DTO (ignores audit fields)
        departmentMapper.updateEntityFromDto(departmentDto, existing);

        // updateTs and updatedBy will be set by @PreUpdate

        // Save
        Department updated = departmentRepository.save(existing);

        log.info("Department updated successfully: {}", id);

        return departmentMapper.toDto(updated);
    }

    // =====================================================
    // Soft Delete (NOT Physical DELETE)
    // =====================================================

    /**
     * Soft delete department
     *
     * <p><strong>CRITICAL - Soft Delete Only:</strong></p>
     * <ul>
     *   <li>Sets deleteTs = NOW()</li>
     *   <li>Sets deletedBy = current user</li>
     *   <li>NO physical DELETE from database</li>
     *   <li>Department still exists but filtered by @Where clause</li>
     * </ul>
     *
     * @param id department ID
     * @throws ResourceNotFoundException if department not found
     */
    @Transactional
    @CacheEvict(value = "departments", allEntries = true)
    public void softDelete(UUID id) {
        log.warn("Soft deleting department ID: {}", id);

        // Find existing department
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        // Check if already deleted
        if (department.isDeleted()) {
            log.warn("Department already deleted: {}", id);
            return;
        }

        // Set soft delete fields
        department.setDeleteTs(LocalDateTime.now());
        // TODO: Set deletedBy from SecurityContext
        // department.setDeletedBy(SecurityContextHolder.getContext().getAuthentication().getName());

        // Save (this triggers @PreUpdate)
        departmentRepository.save(department);

        log.warn("Department soft deleted: {}", id);
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
