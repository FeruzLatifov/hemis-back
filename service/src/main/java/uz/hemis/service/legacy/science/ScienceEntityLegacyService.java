package uz.hemis.service.legacy.science;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.*;
import uz.hemis.domain.repository.*;
import uz.hemis.service.legacy.CubaEntityMapHelper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static uz.hemis.service.legacy.CubaEntityMapHelper.*;

/**
 * Legacy service for Science domain entities.
 * Extracts toMap / updateFromMap / CRUD logic from 8 science controllers.
 *
 * Entities handled:
 * - PublicationCriteria
 * - PublicationAuthorMeta
 * - PublicationMethodical
 * - MethodicalPublicationType
 * - ProjectMeta
 * - ResearchActivity
 * - ProjectExecutor
 * - Project
 *
 * @since 2.1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ScienceEntityLegacyService {

    private final PublicationCriteriaRepository publicationCriteriaRepository;
    private final PublicationAuthorMetaRepository publicationAuthorMetaRepository;
    private final PublicationMethodicalRepository publicationMethodicalRepository;
    private final MethodicalPublicationTypeRepository methodicalPublicationTypeRepository;
    private final ProjectMetaRepository projectMetaRepository;
    private final ResearchActivityRepository researchActivityRepository;
    private final ProjectExecutorRepository projectExecutorRepository;
    private final ProjectRepository projectRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    // ====================================================================
    //  PublicationCriteria
    // ====================================================================

    private static final String PC_ENTITY = "hemishe_EPublicationCriteria";

    public Optional<PublicationCriteria> findPublicationCriteriaById(UUID id) {
        return publicationCriteriaRepository.findById(id);
    }

    public List<PublicationCriteria> findAllPublicationCriteria() {
        return publicationCriteriaRepository.findAll();
    }

    public Page<PublicationCriteria> findAllPublicationCriteria(PageRequest pageRequest) {
        return publicationCriteriaRepository.findAll(pageRequest);
    }

    @Transactional
    public PublicationCriteria savePublicationCriteria(PublicationCriteria entity) {
        return publicationCriteriaRepository.save(entity);
    }

    @Transactional
    public void deletePublicationCriteria(PublicationCriteria entity) {
        publicationCriteriaRepository.delete(entity);
    }

    public Map<String, Object> toPublicationCriteriaMap(PublicationCriteria entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", PC_ENTITY);

        String instanceName = entity.getPublicationTypeTable() != null
            ? entity.getPublicationTypeTable()
            : "PublicationCriteria-" + entity.getId();
        map.put("_instanceName", instanceName);

        map.put("id", entity.getId());
        map.put("version", entity.getVersion());

        putIfNotNull(map, "active", entity.getActive(), returnNulls);
        putIfNotNull(map, "publicationTypeTable", entity.getPublicationTypeTable(), returnNulls);
        putIfNotNull(map, "markValue", entity.getMarkValue(), returnNulls);

        return map;
    }

    public void updatePublicationCriteriaFromMap(PublicationCriteria entity, Map<String, Object> map) {
        if (map.containsKey("u_id") || map.containsKey("uId")) {
            entity.setUId(getIntegerValue(map.getOrDefault("uId", map.get("u_id"))));
        }
        if (map.containsKey("_university")) {
            entity.setUniversity(extractString(map.get("_university")));
        }
        if (map.containsKey("_education_year")) {
            entity.setEducationYear(extractString(map.get("_education_year")));
        }
        if (map.containsKey("_publication_type_table")) {
            entity.setPublicationTypeTable(extractString(map.get("_publication_type_table")));
        }
        if (map.containsKey("_publication_methodical_type")) {
            entity.setPublicationMethodicalType(extractString(map.get("_publication_methodical_type")));
        }
        if (map.containsKey("_publication_scientific_type")) {
            entity.setPublicationScientificType(extractString(map.get("_publication_scientific_type")));
        }
        if (map.containsKey("_publication_property_type")) {
            entity.setPublicationPropertyType(extractString(map.get("_publication_property_type")));
        }
        if (map.containsKey("_in_publication_database") || map.containsKey("inPublicationDatabase")) {
            entity.setInPublicationDatabase(getIntegerValue(map.getOrDefault("inPublicationDatabase", map.get("_in_publication_database"))));
        }
        if (map.containsKey("mark_value") || map.containsKey("markValue")) {
            entity.setMarkValue(getIntegerValue(map.getOrDefault("markValue", map.get("mark_value"))));
        }
        if (map.containsKey("position")) {
            entity.setPosition(getIntegerValue(map.get("position")));
        }
        if (map.containsKey("active")) {
            entity.setActive(getBooleanValue(map.get("active")));
        }
        if (map.containsKey("_translations")) {
            entity.setTranslations(extractString(map.get("_translations")));
        }
    }

    // ====================================================================
    //  PublicationAuthorMeta
    // ====================================================================

    private static final String PAM_ENTITY = "hemishe_EPublicationAuthorMeta";

    public Optional<PublicationAuthorMeta> findPublicationAuthorMetaById(UUID id) {
        return publicationAuthorMetaRepository.findById(id);
    }

    public Page<PublicationAuthorMeta> findAllPublicationAuthorMeta(PageRequest pageRequest) {
        return publicationAuthorMetaRepository.findAll(pageRequest);
    }

    @Transactional
    public PublicationAuthorMeta savePublicationAuthorMeta(PublicationAuthorMeta entity) {
        return publicationAuthorMetaRepository.save(entity);
    }

    @Transactional
    public void softDeletePublicationAuthorMeta(PublicationAuthorMeta entity) {
        entity.setDeleteTs(LocalDateTime.now());
        publicationAuthorMetaRepository.save(entity);
    }

    public Map<String, Object> toPublicationAuthorMetaMap(PublicationAuthorMeta entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", PAM_ENTITY);
        map.put("_instanceName", "com.company.hemishe.entity.EPublicationAuthorMeta-" + entity.getId() + " [detached]");
        map.put("id", entity.getId() != null ? entity.getId().toString() : null);

        putIfNotNull(map, "isCheckedByAuthor", entity.getIsCheckedByAuthor(), returnNulls);
        putIfNotNull(map, "active", entity.getActive(), returnNulls);
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "publicationTypeTable", entity.getPublicationTypeTable(), returnNulls);
        putIfNotNull(map, "isMainAuthor", entity.getIsMainAuthor(), returnNulls);

        return map;
    }

    public Map<String, Object> toPublicationAuthorMetaMinimalMap(PublicationAuthorMeta entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", PAM_ENTITY);
        map.put("_instanceName", "com.company.hemishe.entity.EPublicationAuthorMeta-" + entity.getId() + " [detached]");
        map.put("id", entity.getId() != null ? entity.getId().toString() : null);
        return map;
    }

    @SuppressWarnings("unchecked")
    public void updatePublicationAuthorMetaFromMap(PublicationAuthorMeta entity, Map<String, Object> map) {
        if (map.containsKey("isCheckedByAuthor")) {
            entity.setIsCheckedByAuthor(getBooleanValue(map.get("isCheckedByAuthor")));
        }
        if (map.containsKey("active")) {
            entity.setActive(getBooleanValue(map.get("active")));
        }
        if (map.containsKey("isMainAuthor")) {
            Object val = map.get("isMainAuthor");
            if (val instanceof Number) {
                entity.setIsMainAuthor(((Number) val).intValue());
            } else if (val instanceof Boolean) {
                entity.setIsMainAuthor(((Boolean) val) ? 1 : 0);
            } else if (val instanceof String) {
                try {
                    entity.setIsMainAuthor(Integer.parseInt((String) val));
                } catch (NumberFormatException e) {
                    entity.setIsMainAuthor(Boolean.parseBoolean((String) val) ? 1 : 0);
                }
            }
        }
        if (map.containsKey("publicationTypeTable")) {
            entity.setPublicationTypeTable((String) map.get("publicationTypeTable"));
        }
        if (map.containsKey("position")) {
            entity.setPosition(getIntegerValue(map.get("position")));
        }
        if (map.containsKey("university")) {
            Object universityVal = map.get("university");
            if (universityVal instanceof Map) {
                Map<String, Object> universityMap = (Map<String, Object>) universityVal;
                entity.setUniversity((String) universityMap.getOrDefault("code", universityMap.get("id")));
            }
        }
        if (map.containsKey("employee")) {
            entity.setEmployee(extractUuid(map.get("employee")));
        }
        if (map.containsKey("publicationScientific")) {
            entity.setPublicationScientific(extractUuid(map.get("publicationScientific")));
        }
        if (map.containsKey("publicationProperty")) {
            entity.setPublicationProperty(extractUuid(map.get("publicationProperty")));
        }
        if (map.containsKey("publicationMethodical")) {
            entity.setPublicationMethodical(extractUuid(map.get("publicationMethodical")));
        }
    }

    // ====================================================================
    //  PublicationMethodical
    // ====================================================================

    private static final String PM_ENTITY = "hemishe_EPublicationMethodical";

    public Optional<PublicationMethodical> findPublicationMethodicalById(UUID id) {
        return publicationMethodicalRepository.findById(id);
    }

    public List<PublicationMethodical> findAllPublicationMethodical() {
        return publicationMethodicalRepository.findAll();
    }

    public Page<PublicationMethodical> findAllPublicationMethodical(PageRequest pageRequest) {
        return publicationMethodicalRepository.findAll(pageRequest);
    }

    @Transactional
    public PublicationMethodical savePublicationMethodical(PublicationMethodical entity) {
        return publicationMethodicalRepository.save(entity);
    }

    @Transactional
    public void softDeletePublicationMethodical(PublicationMethodical entity) {
        entity.setDeleteTs(LocalDateTime.now());
        publicationMethodicalRepository.save(entity);
    }

    public Map<String, Object> toPublicationMethodicalMap(PublicationMethodical entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", PM_ENTITY);

        String instanceName = entity.getName() != null ? entity.getName() : "PublicationMethodical-" + entity.getId();
        map.put("_instanceName", instanceName);

        map.put("id", entity.getId());
        map.put("version", entity.getVersion());

        putIfNotNull(map, "authorCounts", entity.getAuthorCounts(), returnNulls);
        putIfNotNull(map, "parameter", entity.getParameter(), returnNulls);
        putIfNotNull(map, "active", entity.getActive(), returnNulls);
        putIfNotNull(map, "isChecked", entity.getIsChecked(), returnNulls);
        putIfNotNull(map, "issueYear", entity.getIssueYear(), returnNulls);
        putIfNotNull(map, "filename", entity.getFilename(), returnNulls);
        putIfNotNull(map, "name", entity.getName(), returnNulls);
        putIfNotNull(map, "publisher", entity.getPublisher(), returnNulls);
        putIfNotNull(map, "authors", entity.getAuthors(), returnNulls);
        putIfNotNull(map, "uId", entity.getUId(), returnNulls);
        putIfNotNull(map, "_university", entity.getUniversity(), returnNulls);
        putIfNotNull(map, "sourceName", entity.getSourceName(), returnNulls);
        putIfNotNull(map, "_methodical_publication_type", entity.getMethodicalPublicationType(), returnNulls);
        putIfNotNull(map, "_publication_database", entity.getPublicationDatabase(), returnNulls);
        putIfNotNull(map, "_employee", entity.getEmployee(), returnNulls);
        putIfNotNull(map, "position", entity.getPosition(), returnNulls);
        putIfNotNull(map, "_translations", entity.getTranslations(), returnNulls);
        putIfNotNull(map, "isCheckedDate", entity.getIsCheckedDate(), returnNulls);
        putIfNotNull(map, "_education_year", entity.getEducationYear(), returnNulls);

        putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        putIfNotNull(map, "createdBy", entity.getCreatedBy(), returnNulls);
        putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        putIfNotNull(map, "updatedBy", entity.getUpdatedBy(), returnNulls);
        putIfNotNull(map, "deleteTs", entity.getDeleteTs(), returnNulls);
        putIfNotNull(map, "deletedBy", entity.getDeletedBy(), returnNulls);

        return map;
    }

    public void updatePublicationMethodicalFromMap(PublicationMethodical entity, Map<String, Object> map) {
        if (map.containsKey("u_id") || map.containsKey("uId")) {
            entity.setUId(getIntegerValue(map.getOrDefault("uId", map.get("u_id"))));
        }
        if (map.containsKey("_university") || map.containsKey("university")) {
            entity.setUniversity(extractString(map.getOrDefault("university", map.get("_university"))));
        }
        if (map.containsKey("name")) {
            entity.setName(extractString(map.get("name")));
        }
        if (map.containsKey("authors")) {
            entity.setAuthors(extractString(map.get("authors")));
        }
        if (map.containsKey("author_counts") || map.containsKey("authorCounts")) {
            entity.setAuthorCounts(getIntegerValue(map.getOrDefault("authorCounts", map.get("author_counts"))));
        }
        if (map.containsKey("publisher")) {
            entity.setPublisher(extractString(map.get("publisher")));
        }
        if (map.containsKey("issue_year") || map.containsKey("issueYear")) {
            entity.setIssueYear(getIntegerValue(map.getOrDefault("issueYear", map.get("issue_year"))));
        }
        if (map.containsKey("source_name") || map.containsKey("sourceName")) {
            entity.setSourceName(extractString(map.getOrDefault("sourceName", map.get("source_name"))));
        }
        if (map.containsKey("parameter")) {
            entity.setParameter(extractString(map.get("parameter")));
        }
        if (map.containsKey("_methodical_publication_type") || map.containsKey("methodicalPublicationType")) {
            entity.setMethodicalPublicationType(extractString(map.getOrDefault("methodicalPublicationType", map.get("_methodical_publication_type"))));
        }
        if (map.containsKey("_publication_database") || map.containsKey("publicationDatabase")) {
            entity.setPublicationDatabase(extractString(map.getOrDefault("publicationDatabase", map.get("_publication_database"))));
        }
        if (map.containsKey("_employee") || map.containsKey("employee")) {
            entity.setEmployee(extractUuid(map.getOrDefault("employee", map.get("_employee"))));
        }
        if (map.containsKey("filename")) {
            entity.setFilename(extractString(map.get("filename")));
        }
        if (map.containsKey("position")) {
            entity.setPosition(getIntegerValue(map.get("position")));
        }
        if (map.containsKey("active")) {
            entity.setActive(getBooleanValue(map.get("active")));
        }
        if (map.containsKey("_translations")) {
            entity.setTranslations(extractString(map.get("_translations")));
        }
        if (map.containsKey("is_checked") || map.containsKey("isChecked")) {
            entity.setIsChecked(getBooleanValue(map.getOrDefault("isChecked", map.get("is_checked"))));
        }
        if (map.containsKey("is_checked_date") || map.containsKey("isCheckedDate")) {
            entity.setIsCheckedDate(parseLocalDateTime(map.getOrDefault("isCheckedDate", map.get("is_checked_date"))));
        }
        if (map.containsKey("_education_year") || map.containsKey("educationYear")) {
            entity.setEducationYear(extractString(map.getOrDefault("educationYear", map.get("_education_year"))));
        }
    }

    // ====================================================================
    //  MethodicalPublicationType
    // ====================================================================

    private static final String MPT_ENTITY = "hemishe_HMethodicalPublicationType";

    public Optional<MethodicalPublicationType> findMethodicalPublicationTypeByCode(String code) {
        return methodicalPublicationTypeRepository.findByCodeAndDeleteTsIsNull(code);
    }

    public Optional<MethodicalPublicationType> findMethodicalPublicationTypeByCodeIncludingDeleted(String code) {
        return methodicalPublicationTypeRepository.findByCodeIncludingDeleted(code);
    }

    public List<MethodicalPublicationType> findAllMethodicalPublicationTypes() {
        return methodicalPublicationTypeRepository.findAllByDeleteTsIsNull();
    }

    @Transactional
    public MethodicalPublicationType saveMethodicalPublicationType(MethodicalPublicationType entity) {
        return methodicalPublicationTypeRepository.save(entity);
    }

    @Transactional
    public void softDeleteMethodicalPublicationType(MethodicalPublicationType entity) {
        entity.setDeleteTs(LocalDateTime.now());
        methodicalPublicationTypeRepository.save(entity);
    }

    public Map<String, Object> toMethodicalPublicationTypeListMap(MethodicalPublicationType entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", MPT_ENTITY);
        map.put("_instanceName", entity.getCode() + " " + (entity.getName() != null ? entity.getName() : ""));
        map.put("id", entity.getCode());
        if (entity.getNameRu() != null) {
            map.put("nameRu", entity.getNameRu());
        }
        map.put("code", entity.getCode());
        map.put("name", entity.getName());
        map.put("active", entity.getActive() != null ? entity.getActive() : true);
        if (entity.getNameEn() != null) {
            map.put("nameEn", entity.getNameEn());
        }
        map.put("version", entity.getVersion() != null ? entity.getVersion() : 1);
        if (entity.getDeletedBy() != null) {
            map.put("deletedBy", entity.getDeletedBy());
        }
        return map;
    }

    public Map<String, Object> toMethodicalPublicationTypeCreateMap(MethodicalPublicationType entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", MPT_ENTITY);
        map.put("_instanceName", entity.getCode() + " " + (entity.getName() != null ? entity.getName() : ""));
        map.put("id", entity.getCode());
        return map;
    }

    public void updateMethodicalPublicationTypeFromMap(MethodicalPublicationType entity, Map<String, Object> map) {
        if (map.containsKey("name")) {
            entity.setName(extractString(map.get("name")));
        }
        if (map.containsKey("nameRu") || map.containsKey("name_ru")) {
            entity.setNameRu(extractString(map.getOrDefault("nameRu", map.get("name_ru"))));
        }
        if (map.containsKey("nameEn") || map.containsKey("name_en")) {
            entity.setNameEn(extractString(map.getOrDefault("nameEn", map.get("name_en"))));
        }
        if (map.containsKey("active")) {
            entity.setActive(getBooleanValue(map.get("active")));
        }
    }

    // ====================================================================
    //  ProjectMeta
    // ====================================================================

    private static final String PMT_ENTITY = "hemishe_EProjectMeta";
    private static final String PMT_CUBA_CLASS = "com.company.hemishe.entity.EProjectMeta";

    public Optional<ProjectMeta> findProjectMetaById(UUID id) {
        return projectMetaRepository.findById(id);
    }

    public List<ProjectMeta> findAllProjectMeta() {
        return projectMetaRepository.findAll();
    }

    public Page<ProjectMeta> findAllProjectMeta(PageRequest pageRequest) {
        return projectMetaRepository.findAll(pageRequest);
    }

    @Transactional
    public ProjectMeta saveProjectMeta(ProjectMeta entity) {
        return projectMetaRepository.save(entity);
    }

    @Transactional
    public void deleteProjectMeta(ProjectMeta entity) {
        projectMetaRepository.delete(entity);
    }

    public Map<String, Object> toProjectMetaMap(ProjectMeta entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", PMT_ENTITY);
        map.put("_instanceName", PMT_CUBA_CLASS + "-" + entity.getId() + " [detached]");
        map.put("id", entity.getId() != null ? entity.getId().toString() : null);

        putIfNotNull(map, "quantityMembers", entity.getQuantityMembers(), returnNulls);
        map.put("active", entity.getActive());
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "fiscalYear", entity.getFiscalYear(), returnNulls);
        putIfNotNull(map, "budget", entity.getBudget(), returnNulls);

        return map;
    }

    public Map<String, Object> toProjectMetaMinimalMap(ProjectMeta entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", PMT_ENTITY);
        map.put("_instanceName", PMT_CUBA_CLASS + "-" + entity.getId() + " [detached]");
        map.put("id", entity.getId().toString());
        return map;
    }

    public void updateProjectMetaFromMap(ProjectMeta entity, Map<String, Object> map) {
        if (map.containsKey("fiscalYear")) {
            Object val = map.get("fiscalYear");
            if (val != null) entity.setFiscalYear(Integer.parseInt(val.toString()));
        }
        if (map.containsKey("budget")) {
            Object val = map.get("budget");
            if (val != null) entity.setBudget(Double.parseDouble(val.toString()));
        }
        if (map.containsKey("quantityMembers")) {
            Object val = map.get("quantityMembers");
            if (val != null) entity.setQuantityMembers(Integer.parseInt(val.toString()));
        }
        if (map.containsKey("position")) {
            Object val = map.get("position");
            if (val != null) entity.setPosition(Integer.parseInt(val.toString()));
        }
        if (map.containsKey("active")) {
            Object val = map.get("active");
            if (val instanceof Boolean) {
                entity.setActive((Boolean) val);
            } else if (val != null) {
                entity.setActive(Boolean.valueOf(val.toString()));
            }
        }
        if (map.containsKey("project")) {
            entity.setProject(extractUuid(map.get("project")));
        }
        if (map.containsKey("translations")) {
            Object val = map.get("translations");
            entity.setTranslations(val != null ? val.toString() : null);
        }
    }

    // ====================================================================
    //  ResearchActivity
    // ====================================================================

    private static final String RA_ENTITY = "hemishe_EResearchActivity";
    private static final String RA_CUBA_CLASS = "com.company.hemishe.entity.EResearchActivity";

    public Optional<ResearchActivity> findResearchActivityById(UUID id) {
        return researchActivityRepository.findById(id);
    }

    public List<ResearchActivity> findAllResearchActivity() {
        return researchActivityRepository.findAll();
    }

    public Page<ResearchActivity> findAllResearchActivity(PageRequest pageRequest) {
        return researchActivityRepository.findAll(pageRequest);
    }

    @Transactional
    public ResearchActivity saveResearchActivity(ResearchActivity entity) {
        return researchActivityRepository.save(entity);
    }

    @Transactional
    public void deleteResearchActivity(ResearchActivity entity) {
        researchActivityRepository.delete(entity);
    }

    public Map<String, Object> toResearchActivityMap(ResearchActivity entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", RA_ENTITY);
        map.put("_instanceName", RA_CUBA_CLASS + "-" + entity.getId() + " [detached]");
        map.put("id", entity.getId() != null ? entity.getId().toString() : null);

        putIfNotNull(map, "scientificWorkCount", entity.getScientificWorkCount(), returnNulls);
        putIfNotNull(map, "link", entity.getLink(), returnNulls);
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "referenceCount", entity.getReferenceCount(), returnNulls);
        putIfNotNull(map, "hIndex", entity.getHIndex(), returnNulls);

        return map;
    }

    public Map<String, Object> toResearchActivityMinimalMap(ResearchActivity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", RA_ENTITY);
        map.put("_instanceName", RA_CUBA_CLASS + "-" + entity.getId() + " [detached]");
        map.put("id", entity.getId().toString());
        return map;
    }

    public void updateResearchActivityFromMap(ResearchActivity entity, Map<String, Object> map) {
        if (map.containsKey("hIndex")) {
            entity.setHIndex(getStringValue(map.get("hIndex")));
        }
        if (map.containsKey("scientificWorkCount")) {
            entity.setScientificWorkCount(getStringValue(map.get("scientificWorkCount")));
        }
        if (map.containsKey("referenceCount")) {
            entity.setReferenceCount(getStringValue(map.get("referenceCount")));
        }
        if (map.containsKey("link")) {
            entity.setLink(getStringValue(map.get("link")));
        }
        if (map.containsKey("university")) {
            entity.setUniversity(extractCode(map.get("university")));
        }
        if (map.containsKey("educationYear")) {
            entity.setEducationYear(extractCode(map.get("educationYear")));
        }
        if (map.containsKey("scholarDatabase")) {
            entity.setScholarDatabase(extractCode(map.get("scholarDatabase")));
        }
    }

    // ====================================================================
    //  ProjectExecutor
    // ====================================================================

    private static final String PE_ENTITY = "hemishe_EProjectExecutor";

    public Optional<ProjectExecutor> findProjectExecutorById(UUID id) {
        return projectExecutorRepository.findById(id);
    }

    public List<ProjectExecutor> findAllProjectExecutor() {
        return projectExecutorRepository.findAll();
    }

    public Page<ProjectExecutor> findAllProjectExecutor(PageRequest pageRequest) {
        return projectExecutorRepository.findAll(pageRequest);
    }

    public long countProjectExecutor() {
        return projectExecutorRepository.count();
    }

    @Transactional
    public ProjectExecutor saveProjectExecutor(ProjectExecutor entity) {
        return projectExecutorRepository.save(entity);
    }

    @Transactional
    public void deleteProjectExecutor(ProjectExecutor entity) {
        projectExecutorRepository.delete(entity);
    }

    public Map<String, Object> toProjectExecutorMap(ProjectExecutor entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", PE_ENTITY);
        map.put("_instanceName", "com.company.hemishe.entity.EProjectExecutor-" + entity.getId() + " [detached]");
        map.put("id", entity.getId() != null ? entity.getId().toString() : null);

        if (entity.getEndDate() != null) {
            map.put("endDate", entity.getEndDate().format(DATE_FORMAT));
        } else {
            map.put("endDate", null);
        }

        map.put("active", entity.getActive());
        map.put("idNumber", entity.getIdNumber());
        map.put("version", entity.getVersion());

        map.put("deletedBy", entity.getDeletedBy());
        map.put("deleteTs", entity.getDeleteTs() != null ? entity.getDeleteTs().toString() : null);
        map.put("translations", entity.getTranslations());
        map.put("position", entity.getPosition());

        map.put("outsider", entity.getOutsider());

        if (entity.getStartDate() != null) {
            map.put("startDate", entity.getStartDate().format(DATE_FORMAT));
        } else {
            map.put("startDate", null);
        }

        return map;
    }

    public Map<String, Object> toProjectExecutorMinimalMap(ProjectExecutor entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", PE_ENTITY);
        map.put("_instanceName", "com.company.hemishe.entity.EProjectExecutor-" + entity.getId() + " [detached]");
        map.put("id", entity.getId().toString());
        return map;
    }

    public void updateProjectExecutorFromMap(ProjectExecutor entity, Map<String, Object> map) {
        if (map.containsKey("project")) {
            entity.setProject(extractUuid(map.get("project")));
        }
        if (map.containsKey("projectExecutorType")) {
            entity.setProjectExecutorType(extractStringId(map.get("projectExecutorType")));
        }
        if (map.containsKey("idNumber")) {
            entity.setIdNumber(getIntegerValue(map.get("idNumber")));
        }
        if (map.containsKey("outsider")) {
            entity.setOutsider(getStringValue(map.get("outsider")));
        }
        if (map.containsKey("startDate")) {
            entity.setStartDate(parseDate(map.get("startDate")));
        }
        if (map.containsKey("endDate")) {
            entity.setEndDate(parseDate(map.get("endDate")));
        }
        if (map.containsKey("position")) {
            entity.setPosition(getIntegerValue(map.get("position")));
        }
        if (map.containsKey("active")) {
            entity.setActive(getBooleanValue(map.get("active")));
        }
        if (map.containsKey("translations")) {
            entity.setTranslations(getStringValue(map.get("translations")));
        }
    }

    // ====================================================================
    //  Project
    // ====================================================================

    private static final String P_ENTITY = "hemishe_EProject";

    public Optional<Project> findProjectById(UUID id) {
        return projectRepository.findById(id);
    }

    public List<Project> findAllProject() {
        return projectRepository.findAll();
    }

    public Page<Project> findAllProject(PageRequest pageRequest) {
        return projectRepository.findAll(pageRequest);
    }

    @Transactional
    public Project saveProject(Project entity) {
        return projectRepository.save(entity);
    }

    @Transactional
    public void softDeleteProject(Project entity) {
        entity.setDeleteTs(LocalDateTime.now());
        projectRepository.save(entity);
    }

    public Map<String, Object> toProjectMap(Project entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", P_ENTITY);

        String instanceName = entity.getName() != null ? entity.getName() : "Project-" + entity.getId();
        map.put("_instanceName", instanceName);
        map.put("id", entity.getId() != null ? entity.getId().toString() : null);

        if (entity.getContractDate() != null) {
            map.put("contractDate", entity.getContractDate().format(DATE_FORMAT));
        } else {
            map.put("contractDate", null);
        }

        if (entity.getEndDate() != null) {
            map.put("endDate", entity.getEndDate().format(DATE_FORMAT));
        } else {
            map.put("endDate", null);
        }

        map.put("projectNumber", entity.getProjectNumber());
        map.put("contractNumber", entity.getContractNumber());

        map.put("active", entity.getActive());
        map.put("version", entity.getVersion());
        map.put("deletedBy", entity.getDeletedBy());
        map.put("deleteTs", entity.getDeleteTs() != null ? entity.getDeleteTs().toString() : null);

        map.put("uId", entity.getUId());
        map.put("translations", entity.getTranslations());
        map.put("name", entity.getName());
        map.put("position", entity.getPosition());

        if (entity.getStartDate() != null) {
            map.put("startDate", entity.getStartDate().format(DATE_FORMAT));
        } else {
            map.put("startDate", null);
        }

        return map;
    }

    public Map<String, Object> toProjectMinimalMap(Project entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", P_ENTITY);
        String instanceName = entity.getName() != null ? entity.getName() : "Project-" + entity.getId();
        map.put("_instanceName", instanceName);
        map.put("id", entity.getId().toString());
        return map;
    }

    public void updateProjectFromMap(Project entity, Map<String, Object> map) {
        if (map.containsKey("name")) {
            entity.setName(getStringValue(map.get("name")));
        }
        if (map.containsKey("projectNumber")) {
            entity.setProjectNumber(getStringValue(map.get("projectNumber")));
        }
        if (map.containsKey("university")) {
            entity.setUniversity(extractStringId(map.get("university")));
        }
        if (map.containsKey("department")) {
            entity.setDepartment(extractStringId(map.get("department")));
        }
        if (map.containsKey("projectType")) {
            entity.setProjectType(extractStringId(map.get("projectType")));
        }
        if (map.containsKey("locality")) {
            entity.setLocality(extractStringId(map.get("locality")));
        }
        if (map.containsKey("projectCurrency")) {
            entity.setProjectCurrency(extractStringId(map.get("projectCurrency")));
        }
        if (map.containsKey("contractNumber")) {
            entity.setContractNumber(getStringValue(map.get("contractNumber")));
        }
        if (map.containsKey("contractDate")) {
            entity.setContractDate(parseDate(map.get("contractDate")));
        }
        if (map.containsKey("startDate")) {
            entity.setStartDate(parseDate(map.get("startDate")));
        }
        if (map.containsKey("endDate")) {
            entity.setEndDate(parseDate(map.get("endDate")));
        }
        if (map.containsKey("position")) {
            entity.setPosition(getIntegerValue(map.get("position")));
        }
        if (map.containsKey("active")) {
            entity.setActive(getBooleanValue(map.get("active")));
        }
        if (map.containsKey("translations")) {
            entity.setTranslations(getStringValue(map.get("translations")));
        }
    }
}
