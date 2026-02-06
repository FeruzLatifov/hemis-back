package uz.hemis.service.legacy.university;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.Faculty;
import uz.hemis.domain.entity.Group;
import uz.hemis.domain.entity.IctEquipment;
import uz.hemis.domain.entity.Laboratories;
import uz.hemis.domain.entity.Specialty;
import uz.hemis.domain.entity.UniversityAttachedSpeciality;
import uz.hemis.domain.entity.University;
import uz.hemis.domain.repository.FacultyRepository;
import uz.hemis.domain.repository.GroupRepository;
import uz.hemis.domain.repository.IctEquipmentRepository;
import uz.hemis.domain.repository.LaboratoriesRepository;
import uz.hemis.domain.repository.SpecialtyRepository;
import uz.hemis.domain.repository.UniversityAttachedSpecialityRepository;
import uz.hemis.domain.repository.UniversityRepository;
import uz.hemis.service.legacy.CubaEntityMapHelper;

import java.time.LocalDateTime;

import java.util.*;

/**
 * University Reference Legacy Service (Track 1)
 *
 * <p>CUBA Platform REST API compatible service for Track 1 entities:</p>
 * <ul>
 *   <li>IctEquipment (hemishe_RIctEquipment)</li>
 *   <li>Laboratories (hemishe_RLaboratories)</li>
 *   <li>UniversityAttachedSpeciality (hemishe_EUniversityAttachedSpeciality)</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UniversityRefLegacyService {

    private final IctEquipmentRepository ictEquipmentRepository;
    private final LaboratoriesRepository laboratoriesRepository;
    private final UniversityAttachedSpecialityRepository universityAttachedSpecialityRepository;
    private final FacultyRepository facultyRepository;
    private final GroupRepository groupRepository;
    private final SpecialtyRepository specialtyRepository;
    private final UniversityRepository universityRepository;

    // Entity names
    private static final String ICT_EQUIPMENT_ENTITY_NAME = "hemishe_RIctEquipment";
    private static final String LABORATORIES_ENTITY_NAME = "hemishe_RLaboratories";
    private static final String UNIVERSITY_ATTACHED_SPECIALITY_ENTITY_NAME = "hemishe_EUniversityAttachedSpeciality";
    private static final String UNIVERSITY_ATTACHED_SPECIALITY_CUBA_CLASS = "com.company.hemishe.entity.EUniversityAttachedSpeciality";
    private static final String FACULTY_ENTITY_NAME = "hemishe_EFaculty";
    private static final String GROUP_ENTITY_NAME = "hemishe_EUniversityGroup";
    private static final String SPECIALTY_ENTITY_NAME = "hemishe_EUniversitySpeciality";
    private static final String UNIVERSITY_ENTITY_NAME = "hemishe_EUniversity";

    // ==================== IctEquipment ====================

    public Optional<IctEquipment> findIctEquipmentById(UUID id) {
        return ictEquipmentRepository.findById(id);
    }

    public List<IctEquipment> findAllIctEquipment() {
        return ictEquipmentRepository.findAll();
    }

    public Page<IctEquipment> findAllIctEquipment(Pageable pageable) {
        return ictEquipmentRepository.findAll(pageable);
    }

    @Transactional
    public IctEquipment saveIctEquipment(IctEquipment entity) {
        return ictEquipmentRepository.save(entity);
    }

    @Transactional
    public void deleteIctEquipment(IctEquipment entity) {
        ictEquipmentRepository.delete(entity);
    }

    public Map<String, Object> toIctEquipmentMap(IctEquipment entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ICT_EQUIPMENT_ENTITY_NAME);

        String instanceName = "ICT-" + entity.getUniversity() + "-" + entity.getEducationYear();
        map.put("_instanceName", instanceName);

        map.put("id", entity.getId());

        CubaEntityMapHelper.putIfNotNull(map, "university_code", entity.getUniversity(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "education_year_code", entity.getEducationYear(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "room_count", entity.getRoomCount(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "valid_projector_count", entity.getValidProjectorCount(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "invalid_projector_count", entity.getInvalidProjectorCount(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "total_count", entity.getTotalCount(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "total_grade", entity.getTotalGrade(), returnNulls);

        // Audit fields
        CubaEntityMapHelper.putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "createdBy", entity.getCreatedBy(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "updatedBy", entity.getUpdatedBy(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "deleteTs", entity.getDeleteTs(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "deletedBy", entity.getDeletedBy(), returnNulls);

        return map;
    }

    public void updateIctEquipmentFromMap(IctEquipment entity, Map<String, Object> map) {
        if (map.containsKey("university_code")) {
            entity.setUniversity(CubaEntityMapHelper.getStringValue(map.get("university_code")));
        }
        if (map.containsKey("education_year_code")) {
            entity.setEducationYear(CubaEntityMapHelper.getStringValue(map.get("education_year_code")));
        }
        if (map.containsKey("room_count")) {
            entity.setRoomCount(CubaEntityMapHelper.getIntegerValue(map.get("room_count")));
        }
        if (map.containsKey("valid_projector_count")) {
            entity.setValidProjectorCount(CubaEntityMapHelper.getIntegerValue(map.get("valid_projector_count")));
        }
        if (map.containsKey("invalid_projector_count")) {
            entity.setInvalidProjectorCount(CubaEntityMapHelper.getIntegerValue(map.get("invalid_projector_count")));
        }
        if (map.containsKey("total_count")) {
            entity.setTotalCount(CubaEntityMapHelper.getIntegerValue(map.get("total_count")));
        }
        if (map.containsKey("total_grade")) {
            entity.setTotalGrade(CubaEntityMapHelper.getIntegerValue(map.get("total_grade")));
        }
    }

    // ==================== Laboratories ====================

    public Optional<Laboratories> findLaboratoriesById(UUID id) {
        return laboratoriesRepository.findById(id);
    }

    public List<Laboratories> findAllLaboratories() {
        return laboratoriesRepository.findAll();
    }

    public Page<Laboratories> findAllLaboratories(Pageable pageable) {
        return laboratoriesRepository.findAll(pageable);
    }

    @Transactional
    public Laboratories saveLaboratories(Laboratories entity) {
        return laboratoriesRepository.save(entity);
    }

    @Transactional
    public void deleteLaboratories(Laboratories entity) {
        laboratoriesRepository.delete(entity);
    }

    public Map<String, Object> toLaboratoriesMap(Laboratories entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", LABORATORIES_ENTITY_NAME);

        String instanceName = entity.getSpecialityName() != null
            ? entity.getSpecialityName()
            : "Laboratories-" + entity.getId();
        map.put("_instanceName", instanceName);

        map.put("id", entity.getId());

        CubaEntityMapHelper.putIfNotNull(map, "university_code", entity.getUniversity(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "education_year_code", entity.getEducationYear(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "speciality_id", entity.getSpecialityId(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "speciality_code", entity.getSpecialityCode(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "speciality_name", entity.getSpecialityName(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "student_count", entity.getStudentCount(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "valid_laboratories_count", entity.getValidLaboratoriesCount(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "valid_workshops_count", entity.getValidWorkshopsCount(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "invalid_laboratories_count", entity.getInvalidLaboratoriesCount(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "invalid_workshops_count", entity.getInvalidWorkshopsCount(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "total_laboratories", entity.getTotalLaboratories(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "total_workshops", entity.getTotalWorkshops(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "total_grade", entity.getTotalGrade(), returnNulls);

        // Audit fields
        CubaEntityMapHelper.putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "createdBy", entity.getCreatedBy(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "updatedBy", entity.getUpdatedBy(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "deleteTs", entity.getDeleteTs(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "deletedBy", entity.getDeletedBy(), returnNulls);

        return map;
    }

    public void updateLaboratoriesFromMap(Laboratories entity, Map<String, Object> map) {
        if (map.containsKey("university_code")) {
            entity.setUniversity(CubaEntityMapHelper.getStringValue(map.get("university_code")));
        }
        if (map.containsKey("education_year_code")) {
            entity.setEducationYear(CubaEntityMapHelper.getStringValue(map.get("education_year_code")));
        }
        if (map.containsKey("speciality_id")) {
            entity.setSpecialityId(CubaEntityMapHelper.getStringValue(map.get("speciality_id")));
        }
        if (map.containsKey("speciality_code")) {
            entity.setSpecialityCode(CubaEntityMapHelper.getStringValue(map.get("speciality_code")));
        }
        if (map.containsKey("speciality_name")) {
            entity.setSpecialityName(CubaEntityMapHelper.getStringValue(map.get("speciality_name")));
        }
        if (map.containsKey("student_count")) {
            entity.setStudentCount(CubaEntityMapHelper.getIntegerValue(map.get("student_count")));
        }
        if (map.containsKey("valid_laboratories_count")) {
            entity.setValidLaboratoriesCount(CubaEntityMapHelper.getIntegerValue(map.get("valid_laboratories_count")));
        }
        if (map.containsKey("valid_workshops_count")) {
            entity.setValidWorkshopsCount(CubaEntityMapHelper.getIntegerValue(map.get("valid_workshops_count")));
        }
        if (map.containsKey("invalid_laboratories_count")) {
            entity.setInvalidLaboratoriesCount(CubaEntityMapHelper.getIntegerValue(map.get("invalid_laboratories_count")));
        }
        if (map.containsKey("invalid_workshops_count")) {
            entity.setInvalidWorkshopsCount(CubaEntityMapHelper.getIntegerValue(map.get("invalid_workshops_count")));
        }
        if (map.containsKey("total_laboratories")) {
            entity.setTotalLaboratories(CubaEntityMapHelper.getIntegerValue(map.get("total_laboratories")));
        }
        if (map.containsKey("total_workshops")) {
            entity.setTotalWorkshops(CubaEntityMapHelper.getIntegerValue(map.get("total_workshops")));
        }
        if (map.containsKey("total_grade")) {
            entity.setTotalGrade(CubaEntityMapHelper.getIntegerValue(map.get("total_grade")));
        }
    }

    // ==================== UniversityAttachedSpeciality ====================

    public Optional<UniversityAttachedSpeciality> findUniversityAttachedSpecialityById(UUID id) {
        return universityAttachedSpecialityRepository.findById(id);
    }

    public List<UniversityAttachedSpeciality> findAllUniversityAttachedSpeciality() {
        return universityAttachedSpecialityRepository.findAll();
    }

    public List<UniversityAttachedSpeciality> findAllUniversityAttachedSpeciality(Sort sort) {
        return universityAttachedSpecialityRepository.findAll(sort);
    }

    public Page<UniversityAttachedSpeciality> findAllUniversityAttachedSpeciality(Pageable pageable) {
        return universityAttachedSpecialityRepository.findAll(pageable);
    }

    @Transactional
    public UniversityAttachedSpeciality saveUniversityAttachedSpeciality(UniversityAttachedSpeciality entity) {
        return universityAttachedSpecialityRepository.save(entity);
    }

    public Map<String, Object> toUniversityAttachedSpecialityMap(UniversityAttachedSpeciality e, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", UNIVERSITY_ATTACHED_SPECIALITY_ENTITY_NAME);
        map.put("_instanceName", universityAttachedSpecialityInstanceName(e));
        map.put("id", e.getId() != null ? e.getId().toString() : null);

        CubaEntityMapHelper.putIfNotNull(map, "active", e.getActive(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "educationType", e.getEducationType(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "version", e.getVersion(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "deletedBy", e.getDeletedBy(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "deleteTs", e.getDeleteTs() != null ? e.getDeleteTs().toString() : null, returnNulls);

        return map;
    }

    public Map<String, Object> toUniversityAttachedSpecialityMinimalMap(UniversityAttachedSpeciality saved) {
        Map<String, Object> minimalResponse = new LinkedHashMap<>();
        minimalResponse.put("_entityName", UNIVERSITY_ATTACHED_SPECIALITY_ENTITY_NAME);
        minimalResponse.put("_instanceName", universityAttachedSpecialityInstanceName(saved));
        minimalResponse.put("id", saved.getId() != null ? saved.getId().toString() : null);
        return minimalResponse;
    }

    private String universityAttachedSpecialityInstanceName(UniversityAttachedSpeciality e) {
        return UNIVERSITY_ATTACHED_SPECIALITY_CUBA_CLASS + "-" + (e.getId() != null ? e.getId().toString() : "") + " [detached]";
    }

    @SuppressWarnings("unchecked")
    public void updateUniversityAttachedSpecialityFromMap(UniversityAttachedSpeciality entity, Map<String, Object> map) {
        if (map.containsKey("university")) {
            entity.setUniversity(extractCode(map.get("university")));
        }
        if (map.containsKey("educationForm")) {
            entity.setEducationForm(extractCode(map.get("educationForm")));
        }
        if (map.containsKey("specialityBachelor")) {
            entity.setSpecialityBachelor(CubaEntityMapHelper.extractUuid(map.get("specialityBachelor")));
        }
        if (map.containsKey("specialityMaster")) {
            entity.setSpecialityMaster(CubaEntityMapHelper.extractUuid(map.get("specialityMaster")));
        }
        if (map.containsKey("specialityOrdinatura")) {
            entity.setSpecialityOrdinatura(CubaEntityMapHelper.extractUuid(map.get("specialityOrdinatura")));
        }
        if (map.containsKey("specialityDoctoral")) {
            entity.setSpecialityDoctoral(CubaEntityMapHelper.extractUuid(map.get("specialityDoctoral")));
        }
        if (map.containsKey("educationType")) {
            entity.setEducationType(extractCode(map.get("educationType")));
        }
        if (map.containsKey("active")) {
            entity.setActive(CubaEntityMapHelper.getBooleanValue(map.get("active")));
        }
    }

    @SuppressWarnings("unchecked")
    private String extractCode(Object value) {
        if (value == null) return null;
        if (value instanceof String) return (String) value;
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            Object code = nested.get("code");
            return code != null ? code.toString() : null;
        }
        return value.toString();
    }

    // ==================== Faculty ====================

    public Optional<Faculty> findFacultyById(UUID id) {
        return facultyRepository.findById(id);
    }

    public List<Faculty> findAllFaculty() {
        return facultyRepository.findAll();
    }

    public Page<Faculty> findAllFaculty(Pageable pageable) {
        return facultyRepository.findAll(pageable);
    }

    @Transactional
    public Faculty saveFaculty(Faculty entity) {
        return facultyRepository.save(entity);
    }

    @Transactional
    public void deleteFaculty(Faculty entity) {
        facultyRepository.delete(entity);
    }

    public Map<String, Object> toFacultyMap(Faculty entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", FACULTY_ENTITY_NAME);

        String instanceName = entity.getCode() != null ?
            entity.getCode() + " - " + entity.getName() : "Faculty-" + entity.getId();
        map.put("_instanceName", instanceName);

        map.put("id", entity.getId());
        CubaEntityMapHelper.putIfNotNull(map, "code", entity.getCode(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "name", entity.getName(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "shortName", entity.getShortName(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "_university", entity.getUniversity(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "_facultyType", entity.getFacultyType(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "active", entity.getActive(), returnNulls);

        CubaEntityMapHelper.putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "createdBy", entity.getCreatedBy(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "updatedBy", entity.getUpdatedBy(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "deleteTs", entity.getDeleteTs(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "deletedBy", entity.getDeletedBy(), returnNulls);

        return map;
    }

    public void updateFacultyFromMap(Faculty entity, Map<String, Object> map) {
        // DEFERRED: Requires Faculty field mapping specification
    }

    // ==================== Group ====================

    public Optional<Group> findGroupById(UUID id) {
        return groupRepository.findById(id);
    }

    public List<Group> findAllGroup() {
        return groupRepository.findAll();
    }

    public Page<Group> findAllGroup(Pageable pageable) {
        return groupRepository.findAll(pageable);
    }

    public Optional<Group> findGroupByUniqueKey(String university, String educationType,
                                                 String educationYear, String groupId, String groupName) {
        return groupRepository.findByUniqueKey(university, educationType, educationYear, groupId, groupName);
    }

    @Transactional
    public Group saveGroup(Group entity) {
        return groupRepository.save(entity);
    }

    @Transactional
    public void deleteGroup(Group entity) {
        groupRepository.delete(entity);
    }

    public Map<String, Object> toGroupMap(Group entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", GROUP_ENTITY_NAME);
        map.put("_instanceName", entity.getGroupName() != null ? entity.getGroupName() : "Group-" + entity.getId());
        map.put("id", entity.getId() != null ? entity.getId().toString() : null);

        CubaEntityMapHelper.putIfNotNull(map, "groupId", entity.getGroupId(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "groupName", entity.getGroupName(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "_university", entity.getUniversity(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "_educationType", entity.getEducationType(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "_educationYear", entity.getEducationYear(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "active", entity.getActive(), returnNulls);

        return map;
    }

    public Map<String, Object> toGroupMinimalMap(Group saved) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("_entityName", GROUP_ENTITY_NAME);
        result.put("_instanceName", saved.getGroupName() != null ? saved.getGroupName() : "Group-" + saved.getId());
        result.put("id", saved.getId().toString());
        return result;
    }

    @SuppressWarnings("unchecked")
    public void updateGroupFromMap(Group entity, Map<String, Object> map) {
        if (map.containsKey("university")) {
            Object obj = map.get("university");
            if (obj instanceof Map) {
                entity.setUniversity(String.valueOf(((Map<String, Object>) obj).get("code")));
            } else {
                entity.setUniversity(String.valueOf(obj));
            }
        }
        if (map.containsKey("_university")) {
            entity.setUniversity(String.valueOf(map.get("_university")));
        }

        if (map.containsKey("educationType")) {
            Object obj = map.get("educationType");
            if (obj instanceof Map) {
                entity.setEducationType(String.valueOf(((Map<String, Object>) obj).get("code")));
            } else {
                entity.setEducationType(String.valueOf(obj));
            }
        }
        if (map.containsKey("_educationType") || map.containsKey("_education_type")) {
            Object val = map.containsKey("_educationType") ? map.get("_educationType") : map.get("_education_type");
            entity.setEducationType(String.valueOf(val));
        }

        if (map.containsKey("educationYear")) {
            Object obj = map.get("educationYear");
            if (obj instanceof Map) {
                entity.setEducationYear(String.valueOf(((Map<String, Object>) obj).get("code")));
            } else {
                entity.setEducationYear(String.valueOf(obj));
            }
        }
        if (map.containsKey("_educationYear") || map.containsKey("_education_year")) {
            Object val = map.containsKey("_educationYear") ? map.get("_educationYear") : map.get("_education_year");
            entity.setEducationYear(String.valueOf(val));
        }

        if (map.containsKey("groupId")) {
            entity.setGroupId(String.valueOf(map.get("groupId")));
        }
        if (map.containsKey("group_id")) {
            entity.setGroupId(String.valueOf(map.get("group_id")));
        }

        if (map.containsKey("groupName")) {
            entity.setGroupName(String.valueOf(map.get("groupName")));
        }
        if (map.containsKey("group_name")) {
            entity.setGroupName(String.valueOf(map.get("group_name")));
        }
        if (map.containsKey("name")) {
            entity.setGroupName(String.valueOf(map.get("name")));
        }

        if (map.containsKey("active")) {
            Object activeObj = map.get("active");
            if (activeObj instanceof Boolean) {
                entity.setActive((Boolean) activeObj);
            } else if (activeObj instanceof String) {
                entity.setActive(Boolean.parseBoolean((String) activeObj));
            }
        }

        if (entity.getActive() == null) {
            entity.setActive(true);
        }
    }

    // ==================== Specialty ====================

    public Optional<Specialty> findSpecialtyById(UUID id) {
        return specialtyRepository.findById(id);
    }

    public List<Specialty> findAllSpecialty() {
        return specialtyRepository.findAll();
    }

    public Page<Specialty> findAllSpecialty(Pageable pageable) {
        return specialtyRepository.findAll(pageable);
    }

    public Optional<Specialty> findSpecialtyByUniqueKey(String university, String educationType,
                                                         String educationYear, String code, String name) {
        return specialtyRepository.findByUniqueKey(university, educationType, educationYear, code, name);
    }

    @Transactional
    public Specialty saveSpecialty(Specialty entity) {
        return specialtyRepository.save(entity);
    }

    @Transactional
    public void deleteSpecialty(Specialty entity) {
        specialtyRepository.delete(entity);
    }

    public Map<String, Object> toSpecialtyMap(Specialty entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", SPECIALTY_ENTITY_NAME);

        String instanceName = entity.getCode() != null ?
            entity.getCode() + " - " + entity.getName() : "Specialty-" + entity.getId();
        map.put("_instanceName", instanceName);

        map.put("id", entity.getId());
        CubaEntityMapHelper.putIfNotNull(map, "specialityCode", entity.getCode(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "specialityName", entity.getName(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "_university", entity.getUniversity(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "_faculty", entity.getFaculty(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "_educationType", entity.getEducationType(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "_educationYear", entity.getEducationYear(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "active", entity.getActive(), returnNulls);

        return map;
    }

    public Map<String, Object> toSpecialtyCreateResponse(Specialty entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", SPECIALTY_ENTITY_NAME);

        String instanceName = (entity.getCode() != null ? entity.getCode() : "") + " " +
                              (entity.getName() != null ? entity.getName() : "");
        map.put("_instanceName", instanceName.trim());
        map.put("id", entity.getId() != null ? entity.getId().toString() : null);

        return map;
    }

    @SuppressWarnings("unchecked")
    public void updateSpecialtyFromMap(Specialty entity, Map<String, Object> map) {
        // specialityCode -> code
        if (map.containsKey("specialityCode")) {
            entity.setCode((String) map.get("specialityCode"));
        } else if (map.containsKey("code")) {
            entity.setCode((String) map.get("code"));
        }

        // specialityName -> name
        if (map.containsKey("specialityName")) {
            entity.setName((String) map.get("specialityName"));
        } else if (map.containsKey("name")) {
            entity.setName((String) map.get("name"));
        }

        // university.code -> university
        if (map.containsKey("university") && map.get("university") instanceof Map) {
            Map<String, Object> univ = (Map<String, Object>) map.get("university");
            if (univ.containsKey("code")) {
                entity.setUniversity((String) univ.get("code"));
            }
        } else if (map.containsKey("_university")) {
            entity.setUniversity((String) map.get("_university"));
        }

        // faculty.code -> faculty (String)
        if (map.containsKey("faculty") && map.get("faculty") instanceof Map) {
            Map<String, Object> fac = (Map<String, Object>) map.get("faculty");
            if (fac.containsKey("code")) {
                entity.setFaculty((String) fac.get("code"));
            }
        } else if (map.containsKey("_faculty")) {
            entity.setFaculty((String) map.get("_faculty"));
        }

        // educationYear.code -> educationYear
        if (map.containsKey("educationYear") && map.get("educationYear") instanceof Map) {
            Map<String, Object> edYear = (Map<String, Object>) map.get("educationYear");
            if (edYear.containsKey("code")) {
                entity.setEducationYear((String) edYear.get("code"));
            }
        } else if (map.containsKey("_education_year")) {
            entity.setEducationYear((String) map.get("_education_year"));
        }

        // educationType.code -> educationType
        if (map.containsKey("educationType") && map.get("educationType") instanceof Map) {
            Map<String, Object> edType = (Map<String, Object>) map.get("educationType");
            if (edType.containsKey("code")) {
                entity.setEducationType((String) edType.get("code"));
            }
        } else if (map.containsKey("_educationType")) {
            entity.setEducationType((String) map.get("_educationType"));
        }

        // active
        if (map.containsKey("active")) {
            Object activeVal = map.get("active");
            if (activeVal instanceof Boolean) {
                entity.setActive((Boolean) activeVal);
            } else if (activeVal instanceof String) {
                entity.setActive(Boolean.parseBoolean((String) activeVal));
            }
        }
    }

    @SuppressWarnings("unchecked")
    public Specialty findExistingSpecialtyOrNew(Map<String, Object> item) {
        String code = (String) item.getOrDefault("specialityCode", item.get("code"));
        String name = (String) item.getOrDefault("specialityName", item.get("name"));
        String university = null;
        String educationType = null;
        String educationYear = null;

        if (item.get("university") instanceof Map) {
            university = (String) ((Map<String, Object>) item.get("university")).get("code");
        }
        if (item.get("educationType") instanceof Map) {
            educationType = (String) ((Map<String, Object>) item.get("educationType")).get("code");
        }
        if (item.get("educationYear") instanceof Map) {
            educationYear = (String) ((Map<String, Object>) item.get("educationYear")).get("code");
        }

        if (code != null && university != null && educationType != null && educationYear != null && name != null) {
            return specialtyRepository.findByUniqueKey(university, educationType, educationYear, code, name)
                    .orElseGet(Specialty::new);
        }
        return new Specialty();
    }

    // ==================== University ====================

    public Optional<University> findUniversityById(String code) {
        return universityRepository.findById(code);
    }

    public List<University> findAllUniversity() {
        return universityRepository.findAll();
    }

    public List<University> findAllUniversity(org.springframework.data.domain.Sort sort) {
        return universityRepository.findAll(sort);
    }

    public Page<University> findAllUniversity(Pageable pageable) {
        return universityRepository.findAll(pageable);
    }

    @Transactional
    public University saveUniversity(University entity) {
        return universityRepository.save(entity);
    }

    @Transactional
    public void deleteUniversity(University entity) {
        universityRepository.delete(entity);
    }

    @Transactional
    public void softDeleteUniversity(University entity) {
        entity.setDeleteTs(LocalDateTime.now());
        universityRepository.save(entity);
    }

    public Map<String, Object> toUniversityMap(University entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", UNIVERSITY_ENTITY_NAME);

        String instanceName = entity.getCode() + "-" + (entity.getName() != null ? entity.getName() : "");
        map.put("_instanceName", instanceName);

        map.put("id", entity.getCode());

        CubaEntityMapHelper.putIfNotNull(map, "studentUrl", entity.getStudentUrl(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "gradingSystem", entity.getGradingSystem(), returnNulls);
        map.put("code", entity.getCode());
        CubaEntityMapHelper.putIfNotNull(map, "uzbmbUrl", entity.getUzbmbUrl(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "tin", entity.getTin(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "universityUrl", entity.getUniversityUrl(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "addStudent", entity.getAddStudent(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "address", entity.getAddress(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "accreditationEdit", entity.getAccreditationEdit(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "active", entity.getActive(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "cadastre", entity.getCadastre(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "oneId", entity.getOneId(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "allowGrouping", entity.getAllowGrouping(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "teacherUrl", entity.getTeacherUrl(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "allowTransferOutside", entity.getAllowTransferOutside(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "name", entity.getName(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "gpaEdit", entity.getGpaEdit(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "addForeignStudent", entity.getAddForeignStudent(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "addTransferStudent", entity.getAddTransferStudent(), returnNulls);

        CubaEntityMapHelper.putIfNotNull(map, "mailAddress", entity.getMailAddress(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "bankInfo", entity.getBankInfo(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "accreditationInfo", entity.getAccreditationInfo(), returnNulls);

        CubaEntityMapHelper.putIfNotNull(map, "deletedBy", entity.getDeletedBy(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "deleteTs", entity.getDeleteTs() != null ? entity.getDeleteTs().toString() : null, returnNulls);

        return map;
    }

    public Map<String, Object> toUniversityMinimalMap(University entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", UNIVERSITY_ENTITY_NAME);
        map.put("_instanceName", entity.getCode() + "-" + (entity.getName() != null ? entity.getName() : ""));
        map.put("id", entity.getCode());
        return map;
    }

    public void updateUniversityFromMap(University entity, Map<String, Object> map) {
        if (map.containsKey("name")) entity.setName(CubaEntityMapHelper.getStringValue(map.get("name")));
        if (map.containsKey("tin")) entity.setTin(CubaEntityMapHelper.getStringValue(map.get("tin")));
        if (map.containsKey("address")) entity.setAddress(CubaEntityMapHelper.getStringValue(map.get("address")));
        if (map.containsKey("cadastre")) entity.setCadastre(CubaEntityMapHelper.getStringValue(map.get("cadastre")));
        if (map.containsKey("universityUrl")) entity.setUniversityUrl(CubaEntityMapHelper.getStringValue(map.get("universityUrl")));
        if (map.containsKey("studentUrl")) entity.setStudentUrl(CubaEntityMapHelper.getStringValue(map.get("studentUrl")));
        if (map.containsKey("teacherUrl")) entity.setTeacherUrl(CubaEntityMapHelper.getStringValue(map.get("teacherUrl")));
        if (map.containsKey("uzbmbUrl")) entity.setUzbmbUrl(CubaEntityMapHelper.getStringValue(map.get("uzbmbUrl")));
        if (map.containsKey("soato")) entity.setSoato(CubaEntityMapHelper.getStringValue(map.get("soato")));
        if (map.containsKey("soatoRegion")) entity.setSoatoRegion(CubaEntityMapHelper.getStringValue(map.get("soatoRegion")));
        if (map.containsKey("universityType")) entity.setUniversityType(CubaEntityMapHelper.getStringValue(map.get("universityType")));
        if (map.containsKey("ownership")) entity.setOwnership(CubaEntityMapHelper.getStringValue(map.get("ownership")));
        if (map.containsKey("universityVersion")) entity.setUniversityVersion(CubaEntityMapHelper.getStringValue(map.get("universityVersion")));
        if (map.containsKey("universityActivityStatus")) entity.setUniversityActivityStatus(CubaEntityMapHelper.getStringValue(map.get("universityActivityStatus")));
        if (map.containsKey("universityBelongsTo")) entity.setUniversityBelongsTo(CubaEntityMapHelper.getStringValue(map.get("universityBelongsTo")));
        if (map.containsKey("universityContractCategory")) entity.setUniversityContractCategory(CubaEntityMapHelper.getStringValue(map.get("universityContractCategory")));
        if (map.containsKey("parentUniversity")) entity.setParentUniversity(CubaEntityMapHelper.getStringValue(map.get("parentUniversity")));
        if (map.containsKey("terrain")) entity.setTerrain(CubaEntityMapHelper.getStringValue(map.get("terrain")));
        if (map.containsKey("active")) entity.setActive(CubaEntityMapHelper.getBooleanValue(map.get("active")));
        if (map.containsKey("gpaEdit")) entity.setGpaEdit(CubaEntityMapHelper.getBooleanValue(map.get("gpaEdit")));
        if (map.containsKey("accreditationEdit")) entity.setAccreditationEdit(CubaEntityMapHelper.getBooleanValue(map.get("accreditationEdit")));
        if (map.containsKey("addStudent")) entity.setAddStudent(CubaEntityMapHelper.getBooleanValue(map.get("addStudent")));
        if (map.containsKey("allowGrouping")) entity.setAllowGrouping(CubaEntityMapHelper.getBooleanValue(map.get("allowGrouping")));
        if (map.containsKey("allowTransferOutside")) entity.setAllowTransferOutside(CubaEntityMapHelper.getBooleanValue(map.get("allowTransferOutside")));
        if (map.containsKey("oneId")) entity.setOneId(CubaEntityMapHelper.getBooleanValue(map.get("oneId")));
        if (map.containsKey("gradingSystem")) entity.setGradingSystem(CubaEntityMapHelper.getBooleanValue(map.get("gradingSystem")));
        if (map.containsKey("addForeignStudent")) entity.setAddForeignStudent(CubaEntityMapHelper.getBooleanValue(map.get("addForeignStudent")));
        if (map.containsKey("addTransferStudent")) entity.setAddTransferStudent(CubaEntityMapHelper.getBooleanValue(map.get("addTransferStudent")));
        if (map.containsKey("mailAddress")) entity.setMailAddress(CubaEntityMapHelper.getStringValue(map.get("mailAddress")));
        if (map.containsKey("bankInfo")) entity.setBankInfo(CubaEntityMapHelper.getStringValue(map.get("bankInfo")));
        if (map.containsKey("accreditationInfo")) entity.setAccreditationInfo(CubaEntityMapHelper.getStringValue(map.get("accreditationInfo")));
    }

    /**
     * Extract code from request body (id, code, or _university)
     */
    public String extractUniversityCode(Map<String, Object> body) {
        if (body.containsKey("id")) {
            return String.valueOf(body.get("id"));
        }
        if (body.containsKey("code")) {
            return String.valueOf(body.get("code"));
        }
        if (body.containsKey("_university")) {
            Object uniObj = body.get("_university");
            if (uniObj instanceof Map) {
                Object uniCode = ((Map<?, ?>) uniObj).get("code");
                if (uniCode != null) return uniCode.toString();
            } else if (uniObj != null) {
                return uniObj.toString();
            }
        }
        return null;
    }
}
