package uz.hemis.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.hemis.app.service.base.AbstractInternalCubaService;
import uz.hemis.domain.entity.Department;
import uz.hemis.domain.entity.Faculty;
import uz.hemis.domain.entity.Group;
import uz.hemis.domain.entity.Specialty;
import uz.hemis.domain.repository.DepartmentRepository;
import uz.hemis.domain.repository.FacultyRepository;
import uz.hemis.domain.repository.GroupRepository;
import uz.hemis.domain.repository.SpecialtyRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Reference Data CUBA Service - OLD-HEMIS Compatibility
 *
 * <p><strong>CRITICAL - OLD-HEMIS Compatibility:</strong></p>
 * <ul>
 *   <li>Implements 4 CUBA reference data services from rest-services.xml</li>
 *   <li>Faculty, Cathedra (Department), Speciality, Group services</li>
 *   <li>Used by universities for lookups and dropdowns</li>
 * </ul>
 *
 * <p><strong>OPTIMIZATION:</strong></p>
 * <ul>
 *   <li>Extends AbstractInternalCubaService</li>
 *   <li>Uses REAL database for ALL services (100% production ready)</li>
 *   <li>All 4 services in ONE class - no code duplication</li>
 * </ul>
 *
 * <p><strong>OLD-HEMIS URL Pattern:</strong></p>
 * <pre>
 * GET /app/rest/v2/services/hemishe_FacultyService/get?university={code}
 * GET /app/rest/v2/services/hemishe_CathedraService/get?university={code}
 * GET /app/rest/v2/services/hemishe_SpecialityService/get?university={code}&type={type}
 * GET /app/rest/v2/services/hemishe_GroupService/get?university={code}&type={type}&year={year}
 * </pre>
 *
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReferenceDataCubaService extends AbstractInternalCubaService {

    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final SpecialtyRepository specialtyRepository;
    private final GroupRepository groupRepository;

    // =====================================================
    // FACULTY SERVICE
    // =====================================================

    /**
     * Get faculties by university
     *
     * <p><strong>Method:</strong> FacultyService.get</p>
     * <p><strong>URL:</strong> /app/rest/v2/services/hemishe_FacultyService/get?university={code}</p>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "data": [
     *     {"id": "uuid", "code": "01", "name": "Математика факультети"},
     *     {"id": "uuid", "code": "02", "name": "Физика факультети"},
     *     ...
     *   ],
     *   "count": 10
     * }
     * </pre>
     *
     * @param university University code
     * @return List of faculties
     */
    public Map<String, Object> getFaculties(String university) {
        log.info("Getting faculties - University: {}", university);

        // Validate required parameter
        Map<String, Object> validationError = validateRequired("university", university);
        if (validationError != null) {
            return validationError;
        }

        // Query from REAL database
        List<Faculty> faculties = facultyRepository.findByUniversity(university);

        List<Map<String, Object>> data = faculties.stream()
                .filter(Faculty::isActive)
                .map(this::mapFacultyToResponse)
                .collect(Collectors.toList());

        log.info("Found {} faculties for university: {}", data.size(), university);
        return successListResponse(data);
    }

    /**
     * Map Faculty entity to response format
     */
    private Map<String, Object> mapFacultyToResponse(Faculty faculty) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", faculty.getId());
        map.put("code", faculty.getCode());
        map.put("name", faculty.getName());
        map.put("short_name", faculty.getShortName());
        map.put("university", faculty.getUniversity());
        map.put("faculty_type", faculty.getFacultyType());
        map.put("active", faculty.getActive());
        return map;
    }

    // =====================================================
    // CATHEDRA SERVICE
    // =====================================================

    /**
     * Get cathedras (departments) by university
     *
     * <p><strong>Method:</strong> CathedraService.get</p>
     * <p><strong>URL:</strong> /app/rest/v2/services/hemishe_CathedraService/get?university={code}</p>
     *
     * @param university University code
     * @return List of cathedras (departments)
     */
    public Map<String, Object> getCathedras(String university) {
        log.info("Getting cathedras (departments) - University: {}", university);

        Map<String, Object> validationError = validateRequired("university", university);
        if (validationError != null) {
            return validationError;
        }

        // Query from REAL database (Department = Cathedra)
        List<Department> departments = departmentRepository.findByUniversity(university);

        List<Map<String, Object>> data = departments.stream()
                .filter(Department::isActive)
                .map(this::mapDepartmentToResponse)
                .collect(Collectors.toList());

        log.info("Found {} cathedras for university: {}", data.size(), university);
        return successListResponse(data);
    }

    /**
     * Map Department entity to response format
     */
    private Map<String, Object> mapDepartmentToResponse(Department dept) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", dept.getId());
        map.put("code", dept.getCode());
        map.put("name", dept.getName());
        map.put("short_name", dept.getShortName());
        map.put("university", dept.getUniversity());
        map.put("faculty", dept.getFaculty());
        map.put("active", dept.getActive());
        return map;
    }

    // =====================================================
    // SPECIALITY SERVICE
    // =====================================================

    /**
     * Get specialities by university and type
     *
     * <p><strong>Method:</strong> SpecialityService.get</p>
     * <p><strong>URL:</strong> /app/rest/v2/services/hemishe_SpecialityService/get?university={code}&type={type}</p>
     *
     * <p><strong>Parameters:</strong></p>
     * <ul>
     *   <li>university - University code (required)</li>
     *   <li>type - Education type: 11=Bakalavr, 12=Magistratura, 13=Doktorantura (required)</li>
     * </ul>
     *
     * @param university University code
     * @param type Education type
     * @return List of specialities
     */
    public Map<String, Object> getSpecialties(String university, String type) {
        log.info("Getting specialties - University: {}, Type: {}", university, type);

        Map<String, Object> validationError = validateRequired("university", university);
        if (validationError != null) {
            return validationError;
        }

        validationError = validateRequired("type", type);
        if (validationError != null) {
            return validationError;
        }

        // Query from REAL database
        List<Specialty> specialties = specialtyRepository
                .findByUniversityAndEducationType(university, type, org.springframework.data.domain.Pageable.unpaged())
                .getContent();

        // Filter active specialties and map to response
        List<Map<String, Object>> data = specialties.stream()
                .filter(Specialty::isActive)
                .map(this::mapSpecialtyToResponse)
                .collect(Collectors.toList());

        log.info("Found {} specialties for university: {}, type: {}", data.size(), university, type);
        return successListResponse(data);
    }

    /**
     * Map Specialty entity to response format
     */
    private Map<String, Object> mapSpecialtyToResponse(Specialty specialty) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", specialty.getId());
        map.put("code", specialty.getCode());
        map.put("name", specialty.getName());
        map.put("short_name", specialty.getShortName());
        map.put("university", specialty.getUniversity());
        map.put("faculty", specialty.getFaculty());
        map.put("specialty_type", specialty.getSpecialtyType());
        map.put("education_type", specialty.getEducationType());
        map.put("education_form", specialty.getEducationForm());
        map.put("active", specialty.getActive());
        return map;
    }

    // =====================================================
    // GROUP SERVICE
    // =====================================================

    /**
     * Get groups by university, type, and year
     *
     * <p><strong>Method:</strong> GroupService.get</p>
     * <p><strong>URL:</strong> /app/rest/v2/services/hemishe_GroupService/get?university={code}&type={type}&year={year}</p>
     *
     * <p><strong>Parameters:</strong></p>
     * <ul>
     *   <li>university - University code (required)</li>
     *   <li>type - Education type: 11=Bakalavr, 12=Magistratura (optional)</li>
     *   <li>year - Course (year of study): 1, 2, 3, 4 (optional)</li>
     * </ul>
     *
     * @param university University code
     * @param type Education type (optional)
     * @param year Course/year (optional)
     * @return List of groups
     */
    public Map<String, Object> getGroups(String university, String type, String year) {
        log.info("Getting groups - University: {}, Type: {}, Year: {}", university, type, year);

        Map<String, Object> validationError = validateRequired("university", university);
        if (validationError != null) {
            return validationError;
        }

        // Query from REAL database
        // Start with all groups for university
        List<Group> allGroups = groupRepository.findByUniversity(university, org.springframework.data.domain.Pageable.unpaged()).getContent();

        // Filter by course/year if provided
        if (year != null && !year.isEmpty()) {
            try {
                Integer courseNumber = Integer.parseInt(year);
                allGroups = allGroups.stream()
                        .filter(g -> courseNumber.equals(g.getCourse()))
                        .collect(Collectors.toList());
            } catch (NumberFormatException e) {
                log.warn("Invalid year format: {}", year);
            }
        }

        // Filter by education type if needed (TODO: add education type field to Group)
        // For now, we return all groups matching university and course

        List<Map<String, Object>> data = allGroups.stream()
                .filter(Group::isActive)
                .map(this::mapGroupToResponse)
                .collect(Collectors.toList());

        log.info("Found {} groups for university: {}", data.size(), university);
        return successListResponse(data);
    }

    /**
     * Map Group entity to response format
     */
    private Map<String, Object> mapGroupToResponse(Group group) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", group.getId());
        map.put("code", group.getName()); // Group name as code
        map.put("name", group.getName());
        map.put("university", group.getUniversity());
        map.put("faculty", group.getFaculty());
        map.put("specialty", group.getSpecialty());
        map.put("academic_year", group.getAcademicYear());
        map.put("course", group.getCourse());
        map.put("capacity", group.getCapacity());
        map.put("active", group.getActive());
        return map;
    }
}
