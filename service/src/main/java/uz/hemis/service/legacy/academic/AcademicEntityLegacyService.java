package uz.hemis.service.legacy.academic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.*;
import uz.hemis.domain.repository.*;
import java.time.LocalDate;
import uz.hemis.service.legacy.ReferenceDataLegacyService;

import java.time.LocalDateTime;
import java.util.*;

import static uz.hemis.service.legacy.CubaEntityMapHelper.*;

/**
 * Legacy service for Academic domain entities.
 * Extracts toMap / updateFromMap / CRUD logic from 6 academic controllers.
 *
 * Entities handled:
 * - Curriculum
 * - Exam
 * - Schedule
 * - Course
 * - EducationMaterials
 * - AcademicMethodologicPublications
 *
 * @since 2.1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AcademicEntityLegacyService {

    private final CurriculumRepository curriculumRepository;
    private final ExamRepository examRepository;
    private final ScheduleRepository scheduleRepository;
    private final CourseRepository courseRepository;
    private final EducationMaterialsRepository educationMaterialsRepository;
    private final AcademicMethodologicPublicationsRepository academicMethodologicPublicationsRepository;
    private final ReferenceDataLegacyService referenceDataService;
    private final AcademicGroupRepository academicGroupRepository;
    private final AcademicSubjectsRepository academicSubjectsRepository;
    private final RAcademicAttendanceRepository rAcademicAttendanceRepository;
    private final AcademicEducationalWorkRepository academicEducationalWorkRepository;

    // ====================================================================
    //  Curriculum
    // ====================================================================

    private static final String CURRICULUM_ENTITY = "hemishe_ECurriculum";

    public Optional<Curriculum> findCurriculumById(UUID id) {
        return curriculumRepository.findById(id);
    }

    public List<Curriculum> findAllCurriculum() {
        return curriculumRepository.findAll();
    }

    public Page<Curriculum> findAllCurriculum(PageRequest pageRequest) {
        return curriculumRepository.findAll(pageRequest);
    }

    @Transactional
    public Curriculum saveCurriculum(Curriculum entity) {
        return curriculumRepository.save(entity);
    }

    @Transactional
    public void deleteCurriculum(Curriculum entity) {
        curriculumRepository.delete(entity);
    }

    public Map<String, Object> toCurriculumMap(Curriculum entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", CURRICULUM_ENTITY);
        map.put("_instanceName", entity.getCode() != null ? entity.getCode() + " - " + entity.getName() : "Curriculum-" + entity.getId());
        map.put("id", entity.getId());
        putIfNotNull(map, "code", entity.getCode(), returnNulls);
        putIfNotNull(map, "name", entity.getName(), returnNulls);
        putIfNotNull(map, "_university", entity.getUniversity(), returnNulls);
        putIfNotNull(map, "_specialty", entity.getSpecialty(), returnNulls);
        putIfNotNull(map, "academicYear", entity.getAcademicYear(), returnNulls);
        putIfNotNull(map, "totalCredits", entity.getTotalCredits(), returnNulls);
        putIfNotNull(map, "studyDuration", entity.getStudyDuration(), returnNulls);
        putIfNotNull(map, "_educationType", entity.getEducationType(), returnNulls);
        putIfNotNull(map, "_educationForm", entity.getEducationForm(), returnNulls);
        putIfNotNull(map, "_curriculumType", entity.getCurriculumType(), returnNulls);
        putIfNotNull(map, "active", entity.getActive(), returnNulls);
        putIfNotNull(map, "isApproved", entity.getIsApproved(), returnNulls);
        putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        putIfNotNull(map, "createdBy", entity.getCreatedBy(), returnNulls);
        putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        putIfNotNull(map, "updatedBy", entity.getUpdatedBy(), returnNulls);
        return map;
    }

    // ====================================================================
    //  Exam
    // ====================================================================

    private static final String EXAM_ENTITY = "hemishe_EExam";

    public Optional<Exam> findExamById(UUID id) {
        return examRepository.findById(id);
    }

    public List<Exam> findAllExam() {
        return examRepository.findAll();
    }

    public Page<Exam> findAllExam(PageRequest pageRequest) {
        return examRepository.findAll(pageRequest);
    }

    @Transactional
    public Exam saveExam(Exam entity) {
        return examRepository.save(entity);
    }

    @Transactional
    public void deleteExam(Exam entity) {
        examRepository.delete(entity);
    }

    public Map<String, Object> toExamMap(Exam entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", EXAM_ENTITY);
        map.put("_instanceName", entity.getExamName() != null ? entity.getExamName() : "Exam-" + entity.getId());
        map.put("id", entity.getId());
        putIfNotNull(map, "examName", entity.getExamName(), returnNulls);
        putIfNotNull(map, "_course", entity.getCourse(), returnNulls);
        putIfNotNull(map, "_group", entity.getGroup(), returnNulls);
        putIfNotNull(map, "_teacher", entity.getTeacher(), returnNulls);
        putIfNotNull(map, "_university", entity.getUniversity(), returnNulls);
        putIfNotNull(map, "_auditorium", entity.getAuditorium(), returnNulls);
        putIfNotNull(map, "examDate", entity.getExamDate(), returnNulls);
        putIfNotNull(map, "startTime", entity.getStartTime(), returnNulls);
        putIfNotNull(map, "endTime", entity.getEndTime(), returnNulls);
        putIfNotNull(map, "durationMinutes", entity.getDurationMinutes(), returnNulls);
        putIfNotNull(map, "academicYear", entity.getAcademicYear(), returnNulls);
        putIfNotNull(map, "semester", entity.getSemester(), returnNulls);
        putIfNotNull(map, "_examType", entity.getExamType(), returnNulls);
        putIfNotNull(map, "maxScore", entity.getMaxScore(), returnNulls);
        putIfNotNull(map, "passingScore", entity.getPassingScore(), returnNulls);
        putIfNotNull(map, "active", entity.getActive(), returnNulls);
        putIfNotNull(map, "isPublished", entity.getIsPublished(), returnNulls);
        putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        putIfNotNull(map, "createdBy", entity.getCreatedBy(), returnNulls);
        putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        putIfNotNull(map, "updatedBy", entity.getUpdatedBy(), returnNulls);
        return map;
    }

    // ====================================================================
    //  Schedule
    // ====================================================================

    private static final String SCHEDULE_ENTITY = "hemishe_ESchedule";

    public Optional<Schedule> findScheduleById(UUID id) {
        return scheduleRepository.findById(id);
    }

    public List<Schedule> findAllSchedule() {
        return scheduleRepository.findAll();
    }

    public Page<Schedule> findAllSchedule(PageRequest pageRequest) {
        return scheduleRepository.findAll(pageRequest);
    }

    @Transactional
    public Schedule saveSchedule(Schedule entity) {
        return scheduleRepository.save(entity);
    }

    @Transactional
    public void deleteSchedule(Schedule entity) {
        scheduleRepository.delete(entity);
    }

    public Map<String, Object> toScheduleMap(Schedule entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", SCHEDULE_ENTITY);
        map.put("_instanceName", "Schedule-" + entity.getId());
        map.put("id", entity.getId());
        putIfNotNull(map, "_university", entity.getUniversity(), returnNulls);
        putIfNotNull(map, "_group", entity.getGroup(), returnNulls);
        putIfNotNull(map, "_course", entity.getCourse(), returnNulls);
        putIfNotNull(map, "_teacher", entity.getTeacher(), returnNulls);
        putIfNotNull(map, "_auditorium", entity.getAuditorium(), returnNulls);
        putIfNotNull(map, "scheduleDate", entity.getScheduleDate(), returnNulls);
        putIfNotNull(map, "startTime", entity.getStartTime(), returnNulls);
        putIfNotNull(map, "endTime", entity.getEndTime(), returnNulls);
        putIfNotNull(map, "dayOfWeek", entity.getDayOfWeek(), returnNulls);
        putIfNotNull(map, "pairNumber", entity.getPairNumber(), returnNulls);
        putIfNotNull(map, "academicYear", entity.getAcademicYear(), returnNulls);
        putIfNotNull(map, "semester", entity.getSemester(), returnNulls);
        putIfNotNull(map, "weekNumber", entity.getWeekNumber(), returnNulls);
        putIfNotNull(map, "_lessonType", entity.getLessonType(), returnNulls);
        putIfNotNull(map, "_scheduleType", entity.getScheduleType(), returnNulls);
        putIfNotNull(map, "active", entity.getActive(), returnNulls);
        putIfNotNull(map, "isCancelled", entity.getIsCancelled(), returnNulls);
        putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        putIfNotNull(map, "createdBy", entity.getCreatedBy(), returnNulls);
        putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        putIfNotNull(map, "updatedBy", entity.getUpdatedBy(), returnNulls);
        return map;
    }

    // ====================================================================
    //  Course
    // ====================================================================

    private static final String COURSE_ENTITY = "hemishe_ECourse";

    public Optional<Course> findCourseById(UUID id) {
        return courseRepository.findById(id);
    }

    public List<Course> findAllCourse() {
        return courseRepository.findAll();
    }

    public Page<Course> findAllCourse(PageRequest pageRequest) {
        return courseRepository.findAll(pageRequest);
    }

    @Transactional
    public Course saveCourse(Course entity) {
        return courseRepository.save(entity);
    }

    @Transactional
    public void deleteCourse(Course entity) {
        courseRepository.delete(entity);
    }

    public Map<String, Object> toCourseMap(Course entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", COURSE_ENTITY);
        map.put("_instanceName", entity.getCode() != null ? entity.getCode() + " - " + entity.getName() : "Course-" + entity.getId());
        map.put("id", entity.getId());
        putIfNotNull(map, "code", entity.getCode(), returnNulls);
        putIfNotNull(map, "name", entity.getName(), returnNulls);
        putIfNotNull(map, "shortName", entity.getShortName(), returnNulls);
        putIfNotNull(map, "_university", entity.getUniversity(), returnNulls);
        putIfNotNull(map, "_subject", entity.getSubject(), returnNulls);
        putIfNotNull(map, "creditCount", entity.getCreditCount(), returnNulls);
        putIfNotNull(map, "totalHours", entity.getTotalHours(), returnNulls);
        putIfNotNull(map, "lectureHours", entity.getLectureHours(), returnNulls);
        putIfNotNull(map, "practiceHours", entity.getPracticeHours(), returnNulls);
        putIfNotNull(map, "labHours", entity.getLabHours(), returnNulls);
        putIfNotNull(map, "semester", entity.getSemester(), returnNulls);
        putIfNotNull(map, "_courseType", entity.getCourseType(), returnNulls);
        putIfNotNull(map, "_assessmentType", entity.getAssessmentType(), returnNulls);
        putIfNotNull(map, "active", entity.getActive(), returnNulls);
        putIfNotNull(map, "isElective", entity.getIsElective(), returnNulls);
        putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        putIfNotNull(map, "createdBy", entity.getCreatedBy(), returnNulls);
        putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        putIfNotNull(map, "updatedBy", entity.getUpdatedBy(), returnNulls);
        return map;
    }

    // ====================================================================
    //  EducationMaterials
    // ====================================================================

    private static final String EDUCATION_MATERIALS_ENTITY = "hemishe_REducationMaterials";

    public Optional<EducationMaterials> findEducationMaterialsById(UUID id) {
        return educationMaterialsRepository.findById(id);
    }

    public List<EducationMaterials> findAllEducationMaterials() {
        return educationMaterialsRepository.findAll();
    }

    public Page<EducationMaterials> findAllEducationMaterials(PageRequest pageRequest) {
        return educationMaterialsRepository.findAll(pageRequest);
    }

    @Transactional
    public EducationMaterials saveEducationMaterials(EducationMaterials entity) {
        return educationMaterialsRepository.save(entity);
    }

    @Transactional
    public void deleteEducationMaterials(EducationMaterials entity) {
        educationMaterialsRepository.delete(entity);
    }

    public Map<String, Object> toEducationMaterialsMap(EducationMaterials entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", EDUCATION_MATERIALS_ENTITY);

        map.put("_instanceName", "com.company.hemishe.entity.REducationMaterials-" + entity.getId() + " [detached]");

        map.put("id", entity.getId());

        putIfNotNull(map, "university_code", entity.getUniversity(), returnNulls);
        putIfNotNull(map, "education_year_code", entity.getEducationYear(), returnNulls);
        putIfNotNull(map, "speciality_id", entity.getSpecialityId(), returnNulls);
        putIfNotNull(map, "speciality_code", entity.getSpecialityCode(), returnNulls);
        putIfNotNull(map, "speciality_name", entity.getSpecialityName(), returnNulls);
        putIfNotNull(map, "subject_count", entity.getSubjectCount(), returnNulls);
        putIfNotNull(map, "textbooks_count", entity.getTextbooksCount(), returnNulls);
        putIfNotNull(map, "created_materials_grade", entity.getCreatedMaterialsGrade(), returnNulls);

        putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        putIfNotNull(map, "createdBy", entity.getCreatedBy(), returnNulls);
        putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        putIfNotNull(map, "updatedBy", entity.getUpdatedBy(), returnNulls);
        putIfNotNull(map, "deleteTs", entity.getDeleteTs(), returnNulls);
        putIfNotNull(map, "deletedBy", entity.getDeletedBy(), returnNulls);

        return map;
    }

    public void updateEducationMaterialsFromMap(EducationMaterials entity, Map<String, Object> map) {
        // DEFERRED: Requires EducationMaterials field mapping specification
    }

    // ====================================================================
    //  AcademicMethodologicPublications
    // ====================================================================

    private static final String AMP_ENTITY = "hemishe_RIAcademicMethodologicPublications";

    public Optional<AcademicMethodologicPublications> findAcademicMethodologicPublicationsById(UUID id) {
        return academicMethodologicPublicationsRepository.findById(id);
    }

    public List<AcademicMethodologicPublications> findAllAcademicMethodologicPublications() {
        return academicMethodologicPublicationsRepository.findAll();
    }

    public Page<AcademicMethodologicPublications> findAllAcademicMethodologicPublications(PageRequest pageRequest) {
        return academicMethodologicPublicationsRepository.findAll(pageRequest);
    }

    @Transactional
    public AcademicMethodologicPublications saveAcademicMethodologicPublications(AcademicMethodologicPublications entity) {
        return academicMethodologicPublicationsRepository.save(entity);
    }

    @Transactional
    public void deleteAcademicMethodologicPublications(AcademicMethodologicPublications entity) {
        academicMethodologicPublicationsRepository.delete(entity);
    }

    public Map<String, Object> toAcademicMethodologicPublicationsMap(AcademicMethodologicPublications entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", AMP_ENTITY);
        map.put("_instanceName", "com.company.hemishe.entity.RIAcademicMethodologicPublications-" + entity.getId() + " [detached]");
        map.put("id", entity.getId());

        putIfNotNull(map, "certificateDate", entity.getCertificateDate(), returnNulls);
        putIfNotNull(map, "authorFullname", entity.getAuthorFullname(), returnNulls);

        // OLD-HEMIS: university va educationYear reference fieldlar default view da qaytarilmaydi
        // Ular faqat underscore-prefixed (_university, _educationYear) sifatida saqlanadi

        putIfNotNull(map, "bookName", entity.getBookName(), returnNulls);
        putIfNotNull(map, "specialityCode", entity.getSpecialityCode(), returnNulls);
        putIfNotNull(map, "certificateNumber", entity.getCertificateNumber(), returnNulls);
        putIfNotNull(map, "specialityName", entity.getSpecialityName(), returnNulls);
        putIfNotNull(map, "bookType", entity.getBookType(), returnNulls);
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);

        return map;
    }

    public void updateAcademicMethodologicPublicationsFromMap(AcademicMethodologicPublications entity, Map<String, Object> body) {
        if (body.containsKey("university")) entity.setUniversity(extractString(body.get("university")));
        if (body.containsKey("educationYear")) entity.setEducationYear(extractString(body.get("educationYear")));
        if (body.containsKey("authorFullname")) entity.setAuthorFullname(getStringValue(body.get("authorFullname")));
        if (body.containsKey("specialityCode")) entity.setSpecialityCode(getStringValue(body.get("specialityCode")));
        if (body.containsKey("specialityName")) entity.setSpecialityName(getStringValue(body.get("specialityName")));
        if (body.containsKey("bookType")) entity.setBookType(getStringValue(body.get("bookType")));
        if (body.containsKey("bookName")) entity.setBookName(getStringValue(body.get("bookName")));
        if (body.containsKey("certificateDate")) entity.setCertificateDate(parseDate(body.get("certificateDate")));
        if (body.containsKey("certificateNumber")) entity.setCertificateNumber(getStringValue(body.get("certificateNumber")));
    }

    // ====================================================================
    //  Nested object builders (for AcademicMethodologicPublications)
    // ====================================================================

    private Map<String, Object> buildUniversityObject(String code) {
        Map<String, String> data = referenceDataService.getUniversityData(code);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("_entityName", "hemishe_EUniversity");
        m.put("_instanceName", code + "-" + data.get("name"));
        m.put("id", code);
        m.put("code", code);
        m.put("name", data.get("name"));
        return m;
    }

    private Map<String, Object> buildEducationYearObject(String code) {
        Map<String, String> data = referenceDataService.getEducationYearData(code);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("_entityName", "hemishe_HEducationYear");
        m.put("_instanceName", data.get("name"));
        m.put("id", code);
        m.put("name", data.get("name"));
        return m;
    }

    // ====================================================================
    //  AcademicGroup (Batch 8)
    // ====================================================================

    private static final String ACADEMIC_GROUP_ENTITY = "hemishe_RAcademicGroup";

    public Optional<AcademicGroup> findAcademicGroupById(UUID id) {
        return academicGroupRepository.findById(id);
    }

    public List<AcademicGroup> findAllAcademicGroup() {
        return academicGroupRepository.findAll();
    }

    @Transactional
    public AcademicGroup saveAcademicGroup(AcademicGroup entity) {
        return academicGroupRepository.save(entity);
    }

    @Transactional
    public void deleteAcademicGroup(AcademicGroup entity) {
        entity.setDeleteTs(LocalDateTime.now());
        academicGroupRepository.save(entity);
    }

    public Map<String, Object> toAcademicGroupMap(AcademicGroup entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ACADEMIC_GROUP_ENTITY);
        map.put("_instanceName", "com.company.hemishe.entity.RAcademicGroup-" + entity.getId() + " [detached]");
        map.put("id", entity.getId().toString());
        putIfNotNull(map, "universityCode", entity.getUniversityCode(), returnNulls);
        putIfNotNull(map, "universityName", entity.getUniversityName(), returnNulls);
        putIfNotNull(map, "educationTypeCode", entity.getEducationTypeCode(), returnNulls);
        putIfNotNull(map, "educationTypeName", entity.getEducationTypeName(), returnNulls);
        putIfNotNull(map, "educationFormCode", entity.getEducationFormCode(), returnNulls);
        putIfNotNull(map, "educationFormName", entity.getEducationFormName(), returnNulls);
        putIfNotNull(map, "educationYearCode", entity.getEducationYearCode(), returnNulls);
        putIfNotNull(map, "educationYearName", entity.getEducationYearName(), returnNulls);
        putIfNotNull(map, "groupCount", entity.getGroupCount(), returnNulls);
        putIfNotNull(map, "updateDate", entity.getUpdateDate(), returnNulls);
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        putIfNotNull(map, "deleteTs", entity.getDeleteTs(), returnNulls);
        return map;
    }

    public void updateAcademicGroupFromMap(AcademicGroup entity, Map<String, Object> data) {
        if (data.containsKey("universityCode")) entity.setUniversityCode(extractString(data.get("universityCode")));
        if (data.containsKey("universityName")) entity.setUniversityName(extractString(data.get("universityName")));
        if (data.containsKey("educationTypeCode")) entity.setEducationTypeCode(extractString(data.get("educationTypeCode")));
        if (data.containsKey("educationTypeName")) entity.setEducationTypeName(extractString(data.get("educationTypeName")));
        if (data.containsKey("educationFormCode")) entity.setEducationFormCode(extractString(data.get("educationFormCode")));
        if (data.containsKey("educationFormName")) entity.setEducationFormName(extractString(data.get("educationFormName")));
        if (data.containsKey("educationYearCode")) entity.setEducationYearCode(extractString(data.get("educationYearCode")));
        if (data.containsKey("educationYearName")) entity.setEducationYearName(extractString(data.get("educationYearName")));
        if (data.containsKey("groupCount")) entity.setGroupCount(getIntegerValue(data.get("groupCount")));
        if (data.containsKey("updateDate")) entity.setUpdateDate(parseLocalDate(data.get("updateDate")));
    }

    // ====================================================================
    //  AcademicSubjects (Batch 8)
    // ====================================================================

    private static final String ACADEMIC_SUBJECTS_ENTITY = "hemishe_RAcademicSubjects";

    public Optional<AcademicSubjects> findAcademicSubjectsById(UUID id) {
        return academicSubjectsRepository.findById(id);
    }

    public List<AcademicSubjects> findAllAcademicSubjects() {
        return academicSubjectsRepository.findAll();
    }

    @Transactional
    public AcademicSubjects saveAcademicSubjects(AcademicSubjects entity) {
        return academicSubjectsRepository.save(entity);
    }

    @Transactional
    public void deleteAcademicSubjects(AcademicSubjects entity) {
        entity.setDeleteTs(LocalDateTime.now());
        academicSubjectsRepository.save(entity);
    }

    public Map<String, Object> toAcademicSubjectsMap(AcademicSubjects entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ACADEMIC_SUBJECTS_ENTITY);
        map.put("_instanceName", "com.company.hemishe.entity.RAcademicSubjects-" + entity.getId() + " [detached]");
        map.put("id", entity.getId().toString());
        putIfNotNull(map, "universityCode", entity.getUniversityCode(), returnNulls);
        putIfNotNull(map, "universityName", entity.getUniversityName(), returnNulls);
        putIfNotNull(map, "educationTypeCode", entity.getEducationTypeCode(), returnNulls);
        putIfNotNull(map, "educationTypeName", entity.getEducationTypeName(), returnNulls);
        putIfNotNull(map, "educationYearCode", entity.getEducationYearCode(), returnNulls);
        putIfNotNull(map, "educationYearName", entity.getEducationYearName(), returnNulls);
        putIfNotNull(map, "curriculumCode", entity.getCurriculumCode(), returnNulls);
        putIfNotNull(map, "curriculumName", entity.getCurriculumName(), returnNulls);
        putIfNotNull(map, "blockCode", entity.getBlockCode(), returnNulls);
        putIfNotNull(map, "blockName", entity.getBlockName(), returnNulls);
        putIfNotNull(map, "subjectCount", entity.getSubjectCount(), returnNulls);
        putIfNotNull(map, "updateDate", entity.getUpdateDate(), returnNulls);
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        putIfNotNull(map, "deleteTs", entity.getDeleteTs(), returnNulls);
        return map;
    }

    public void updateAcademicSubjectsFromMap(AcademicSubjects entity, Map<String, Object> data) {
        if (data.containsKey("universityCode")) entity.setUniversityCode(extractString(data.get("universityCode")));
        if (data.containsKey("universityName")) entity.setUniversityName(extractString(data.get("universityName")));
        if (data.containsKey("educationTypeCode")) entity.setEducationTypeCode(extractString(data.get("educationTypeCode")));
        if (data.containsKey("educationTypeName")) entity.setEducationTypeName(extractString(data.get("educationTypeName")));
        if (data.containsKey("educationYearCode")) entity.setEducationYearCode(extractString(data.get("educationYearCode")));
        if (data.containsKey("educationYearName")) entity.setEducationYearName(extractString(data.get("educationYearName")));
        if (data.containsKey("curriculumCode")) entity.setCurriculumCode(extractString(data.get("curriculumCode")));
        if (data.containsKey("curriculumName")) entity.setCurriculumName(extractString(data.get("curriculumName")));
        if (data.containsKey("blockCode")) entity.setBlockCode(extractString(data.get("blockCode")));
        if (data.containsKey("blockName")) entity.setBlockName(extractString(data.get("blockName")));
        if (data.containsKey("subjectCount")) entity.setSubjectCount(getIntegerValue(data.get("subjectCount")));
        if (data.containsKey("updateDate")) entity.setUpdateDate(parseLocalDate(data.get("updateDate")));
    }

    // ====================================================================
    //  RAcademicAttendance (Batch 8)
    // ====================================================================

    private static final String R_ACADEMIC_ATTENDANCE_ENTITY = "hemishe_RAcademicAttendance";

    public Optional<RAcademicAttendance> findRAcademicAttendanceById(UUID id) {
        return rAcademicAttendanceRepository.findById(id);
    }

    public List<RAcademicAttendance> findAllRAcademicAttendance() {
        return rAcademicAttendanceRepository.findAll();
    }

    @Transactional
    public RAcademicAttendance saveRAcademicAttendance(RAcademicAttendance entity) {
        return rAcademicAttendanceRepository.save(entity);
    }

    @Transactional
    public void deleteRAcademicAttendance(RAcademicAttendance entity) {
        entity.setDeleteTs(LocalDateTime.now());
        rAcademicAttendanceRepository.save(entity);
    }

    public Map<String, Object> toRAcademicAttendanceMap(RAcademicAttendance entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", R_ACADEMIC_ATTENDANCE_ENTITY);
        map.put("_instanceName", "com.company.hemishe.entity.RAcademicAttendance-" + entity.getId() + " [detached]");
        map.put("id", entity.getId().toString());
        putIfNotNull(map, "universityCode", entity.getUniversityCode(), returnNulls);
        putIfNotNull(map, "universityName", entity.getUniversityName(), returnNulls);
        putIfNotNull(map, "facultyCode", entity.getFacultyCode(), returnNulls);
        putIfNotNull(map, "facultyName", entity.getFacultyName(), returnNulls);
        putIfNotNull(map, "educationTypeCode", entity.getEducationTypeCode(), returnNulls);
        putIfNotNull(map, "educationTypeName", entity.getEducationTypeName(), returnNulls);
        putIfNotNull(map, "educationYearCode", entity.getEducationYearCode(), returnNulls);
        putIfNotNull(map, "educationYearName", entity.getEducationYearName(), returnNulls);
        putIfNotNull(map, "semesterTypeCode", entity.getSemesterTypeCode(), returnNulls);
        putIfNotNull(map, "semesterTypeName", entity.getSemesterTypeName(), returnNulls);
        putIfNotNull(map, "courseCode", entity.getCourseCode(), returnNulls);
        putIfNotNull(map, "courseName", entity.getCourseName(), returnNulls);
        putIfNotNull(map, "attendancePercent", entity.getAttendancePercent(), returnNulls);
        putIfNotNull(map, "badAttendanceStudentCount", entity.getBadAttendanceStudentCount(), returnNulls);
        putIfNotNull(map, "updateDate", entity.getUpdateDate(), returnNulls);
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        putIfNotNull(map, "deleteTs", entity.getDeleteTs(), returnNulls);
        return map;
    }

    public void updateRAcademicAttendanceFromMap(RAcademicAttendance entity, Map<String, Object> data) {
        if (data.containsKey("universityCode")) entity.setUniversityCode(extractString(data.get("universityCode")));
        if (data.containsKey("universityName")) entity.setUniversityName(extractString(data.get("universityName")));
        if (data.containsKey("facultyCode")) entity.setFacultyCode(extractString(data.get("facultyCode")));
        if (data.containsKey("facultyName")) entity.setFacultyName(extractString(data.get("facultyName")));
        if (data.containsKey("educationTypeCode")) entity.setEducationTypeCode(extractString(data.get("educationTypeCode")));
        if (data.containsKey("educationTypeName")) entity.setEducationTypeName(extractString(data.get("educationTypeName")));
        if (data.containsKey("educationYearCode")) entity.setEducationYearCode(extractString(data.get("educationYearCode")));
        if (data.containsKey("educationYearName")) entity.setEducationYearName(extractString(data.get("educationYearName")));
        if (data.containsKey("semesterTypeCode")) entity.setSemesterTypeCode(extractString(data.get("semesterTypeCode")));
        if (data.containsKey("semesterTypeName")) entity.setSemesterTypeName(extractString(data.get("semesterTypeName")));
        if (data.containsKey("courseCode")) entity.setCourseCode(extractString(data.get("courseCode")));
        if (data.containsKey("courseName")) entity.setCourseName(extractString(data.get("courseName")));
        if (data.containsKey("attendancePercent")) entity.setAttendancePercent(getDoubleValue(data.get("attendancePercent")));
        if (data.containsKey("badAttendanceStudentCount")) entity.setBadAttendanceStudentCount(getIntegerValue(data.get("badAttendanceStudentCount")));
        if (data.containsKey("updateDate")) entity.setUpdateDate(parseLocalDate(data.get("updateDate")));
    }

    // ====================================================================
    //  AcademicEducationalWork
    // ====================================================================

    private static final String EDUCATIONAL_WORK_ENTITY = "hemishe_RIAcademicEducationalWork";

    public Optional<AcademicEducationalWork> findAcademicEducationalWorkById(UUID id) {
        return academicEducationalWorkRepository.findById(id);
    }

    public List<AcademicEducationalWork> findAllAcademicEducationalWork() {
        return academicEducationalWorkRepository.findAll();
    }

    public Page<AcademicEducationalWork> findAllAcademicEducationalWork(PageRequest pageRequest) {
        return academicEducationalWorkRepository.findAll(pageRequest);
    }

    @Transactional
    public AcademicEducationalWork saveAcademicEducationalWork(AcademicEducationalWork entity) {
        return academicEducationalWorkRepository.save(entity);
    }

    @Transactional
    public void deleteAcademicEducationalWork(AcademicEducationalWork entity) {
        academicEducationalWorkRepository.delete(entity);
    }

    public Map<String, Object> toAcademicEducationalWorkMap(AcademicEducationalWork entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", EDUCATIONAL_WORK_ENTITY);
        map.put("_instanceName", "com.company.hemishe.entity.RIAcademicEducationalWork-" + entity.getId() + " [detached]");
        map.put("id", entity.getId());

        putIfNotNull(map, "studentCount", entity.getStudentCount(), returnNulls);

        // OLD-HEMIS: university, educationYear, course reference fieldlar default view da qaytarilmaydi

        putIfNotNull(map, "document", entity.getDocument(), returnNulls);
        putIfNotNull(map, "subjects", entity.getSubjects(), returnNulls);

        putIfNotNull(map, "languageName", entity.getLanguageName(), returnNulls);
        putIfNotNull(map, "specialityCode", entity.getSpecialityCode(), returnNulls);
        putIfNotNull(map, "specialityName", entity.getSpecialityName(), returnNulls);

        putIfNotNull(map, "version", entity.getVersion(), returnNulls);

        return map;
    }

    public void updateAcademicEducationalWorkFromMap(AcademicEducationalWork entity, Map<String, Object> body) {
        if (body.containsKey("university")) entity.setUniversity(extractString(body.get("university")));
        if (body.containsKey("educationYear")) entity.setEducationYear(extractString(body.get("educationYear")));
        if (body.containsKey("specialityCode")) entity.setSpecialityCode(getStringValue(body.get("specialityCode")));
        if (body.containsKey("specialityName")) entity.setSpecialityName(getStringValue(body.get("specialityName")));
        if (body.containsKey("document")) entity.setDocument(getStringValue(body.get("document")));
        if (body.containsKey("subjects")) entity.setSubjects(getStringValue(body.get("subjects")));
        if (body.containsKey("languageName")) entity.setLanguageName(getStringValue(body.get("languageName")));
        if (body.containsKey("course")) entity.setCourse(extractString(body.get("course")));
        if (body.containsKey("studentCount")) entity.setStudentCount(getIntegerValue(body.get("studentCount")));
    }

    private Map<String, Object> buildCourseObject(String code) {
        Map<String, String> data = referenceDataService.getCourseData(code);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("_entityName", "hemishe_HCourse");
        m.put("_instanceName", code + " " + data.get("name"));
        m.put("id", code);
        m.put("code", code);
        m.put("name", data.get("name"));
        return m;
    }
}
