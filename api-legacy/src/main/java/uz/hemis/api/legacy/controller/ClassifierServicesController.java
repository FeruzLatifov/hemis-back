package uz.hemis.api.legacy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.University;
import uz.hemis.domain.repository.ClassifierRepository;
import uz.hemis.domain.repository.UniversityRepository;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Classifier Services Controller - CUBA Service Pattern
 *
 * <p><strong>Legacy Compatibility:</strong></p>
 * <ul>
 *   <li>Base URL: /app/rest/v2/services/classifiers</li>
 *   <li>Matches OLD-HEMIS CUBA service pattern</li>
 *   <li>200+ universities depend on these endpoints</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Tag(name = "13.Klassifikatorlar", description = "Tizim klassifikatorlari - OTM, jins, fuqarolik va boshqalar")
@RestController
@RequestMapping("/app/rest/v2/services/classifiers")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class ClassifierServicesController {

    private final ClassifierRepository classifierRepository;
    private final UniversityRepository universityRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private static final DateTimeFormatter CUBA_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Barcha klassifikatorlar (items bilan)
     *
     * <p><strong>Legacy Endpoint:</strong> GET /app/rest/v2/services/classifiers/allItems</p>
     *
     * <p>OLD-HEMIS formatida barcha klassifikatorlarni items bilan birga qaytaradi:</p>
     * <pre>
     * {
     *   "success": true,
     *   "classifiers": [
     *     {
     *       "h_citizenship_type": {
     *         "title": "Fuqarolik holati",
     *         "version": 3,
     *         "count": 3,
     *         "items": [
     *           {"_entityName": "hemishe_HCitizenship", "id": "11", "code": "11", "name": "...", "version": 1},
     *           ...
     *         ]
     *       }
     *     },
     *     ...
     *   ]
     * }
     * </pre>
     *
     * @return barcha klassifikatorlar items bilan
     */
    @GetMapping("/allItems")
    @Operation(
        summary = "Barcha klassifikatorlar (items bilan)",
        description = "Tizimda mavjud barcha klassifikatorlarni title, version, count va items bilan qaytaradi"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli",
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"success\":true,\"classifiers\":[{\"h_gender\":{\"title\":\"Jinslar\",\"version\":4,\"count\":4,\"items\":[...]}}]}")))
    })
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    public ResponseEntity<Map<String, Object>> allItems() {
        log.info("GET /services/classifiers/allItems - barcha klassifikatorlar (items bilan)");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);

        List<Map<String, Object>> classifiers = new ArrayList<>();

        // Bazada mavjud hemishe_h_* jadvallarini topish
        List<String> classifierTables = getClassifierTables();

        for (String tableName : classifierTables) {
            Map<String, Object> classifierWithItems = getClassifierWithItems(tableName);
            if (classifierWithItems != null) {
                classifiers.add(classifierWithItems);
            }
        }

        response.put("classifiers", classifiers);
        log.info("Jami {} ta klassifikator (items bilan) topildi", classifiers.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Bitta klassifikator jadvalining to'liq ma'lumotlarini olish (items bilan)
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getClassifierWithItems(String tableName) {
        try {
            // Jadval nomidan classifier nomini olish
            String classifierName;
            String entityName;
            if (tableName.startsWith("hemishe_h_")) {
                String baseName = tableName.substring(10); // hemishe_h_ olib tashlash
                classifierName = "h_" + baseName;
                entityName = "H" + toPascalCase(baseName);
            } else if (tableName.equals("hemishe_e_university")) {
                classifierName = "h_university";
                entityName = "EUniversity";
            } else {
                return null;
            }

            // Mavjud ustunlarni tekshirish
            boolean hasCode = checkColumnExists(tableName, "code");
            boolean hasId = checkColumnExists(tableName, "id");
            boolean hasName = checkColumnExists(tableName, "name");
            boolean hasNameUz = checkColumnExists(tableName, "name_uz");
            boolean hasVersion = checkColumnExists(tableName, "version");
            boolean hasDeleteTs = checkColumnExists(tableName, "delete_ts");

            // Primary key ustunini aniqlash
            String pkColumn = hasCode ? "code" : (hasId ? "id" : null);
            if (pkColumn == null) {
                log.debug("Jadvalda code yoki id ustuni topilmadi: {}", tableName);
                return null;
            }

            // Count va max version olish
            String countSql;
            if (hasVersion && hasDeleteTs) {
                countSql = "SELECT COUNT(*), COALESCE(MAX(version), 0) FROM " + tableName + " WHERE delete_ts IS NULL";
            } else if (hasVersion) {
                countSql = "SELECT COUNT(*), COALESCE(MAX(version), 0) FROM " + tableName;
            } else if (hasDeleteTs) {
                countSql = "SELECT COUNT(*), 0 FROM " + tableName + " WHERE delete_ts IS NULL";
            } else {
                countSql = "SELECT COUNT(*), 0 FROM " + tableName;
            }

            Object[] countResult = (Object[]) entityManager.createNativeQuery(countSql).getSingleResult();
            long count = ((Number) countResult[0]).longValue();
            int maxVersion = ((Number) countResult[1]).intValue();

            // Items olish - dinamik ustunlar
            StringBuilder selectColumns = new StringBuilder(pkColumn);
            if (hasName) selectColumns.append(", name");
            else if (hasNameUz) selectColumns.append(", name_uz as name");
            else selectColumns.append(", " + pkColumn + " as name");

            if (hasVersion) selectColumns.append(", version");
            else selectColumns.append(", 0 as version");

            String whereClause = hasDeleteTs ? " WHERE delete_ts IS NULL" : "";
            String itemsSql = "SELECT " + selectColumns + " FROM " + tableName + whereClause + " ORDER BY " + pkColumn;

            List<Object[]> itemsResult = entityManager.createNativeQuery(itemsSql).getResultList();

            // Items list yaratish
            final String finalEntityName = entityName;
            List<Map<String, Object>> items = itemsResult.stream()
                    .map(row -> {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("_entityName", "hemishe_" + finalEntityName);
                        item.put("id", String.valueOf(row[0]));
                        item.put("code", String.valueOf(row[0]));
                        item.put("name", row[1] != null ? String.valueOf(row[1]) : "");
                        item.put("version", row[2] != null ? ((Number) row[2]).intValue() : 0);
                        return item;
                    })
                    .collect(Collectors.toList());

            // Title ni aniqlash
            String title = getClassifierTitle(classifierName);

            // Metadata yaratish
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("title", title);
            metadata.put("version", maxVersion);
            metadata.put("count", count);
            metadata.put("items", items);

            // Wrapper yaratish
            Map<String, Object> wrapper = new LinkedHashMap<>();
            wrapper.put(classifierName, metadata);

            return wrapper;
        } catch (Exception e) {
            log.debug("Klassifikator {} ma'lumotini olishda xatolik: {}", tableName, e.getMessage());
            return null;
        }
    }

    /**
     * Barcha klassifikatorlar ro'yxati
     *
     * <p><strong>Legacy Endpoint:</strong> GET /app/rest/v2/services/classifiers/info</p>
     *
     * <p>OLD-HEMIS formatida barcha mavjud klassifikatorlar ro'yxatini qaytaradi:</p>
     * <pre>
     * {
     *   "success": true,
     *   "classifiers": [
     *     {"h_gender": {"title": "Jinslar", "version": 4, "count": 4}},
     *     {"h_citizenship_type": {"title": "Fuqarolik holati", "version": 3, "count": 3}},
     *     ...
     *   ]
     * }
     * </pre>
     *
     * @return barcha klassifikatorlar metadatasi
     */
    @GetMapping("/info")
    @Operation(
        summary = "Barcha klassifikatorlar ro'yxati",
        description = "Tizimda mavjud barcha klassifikatorlar ro'yxatini title, version va count bilan qaytaradi"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli",
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"success\":true,\"classifiers\":[{\"h_gender\":{\"title\":\"Jinslar\",\"version\":4,\"count\":4}}]}")))
    })
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> info() {
        log.info("GET /services/classifiers/info - barcha klassifikatorlar");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);

        List<Map<String, Object>> classifiers = new ArrayList<>();

        // Bazada mavjud hemishe_h_* va hemishe_e_university jadvallarini topish
        List<String> classifierTables = getClassifierTables();

        for (String tableName : classifierTables) {
            Map<String, Object> classifierInfo = getClassifierInfo(tableName);
            if (classifierInfo != null) {
                classifiers.add(classifierInfo);
            }
        }

        response.put("classifiers", classifiers);
        log.info("Jami {} ta klassifikator topildi", classifiers.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Bazada mavjud klassifikator jadvallarini olish
     */
    @SuppressWarnings("unchecked")
    private List<String> getClassifierTables() {
        String sql = """
            SELECT table_name FROM information_schema.tables
            WHERE table_schema = 'public'
            AND (table_name LIKE 'hemishe_h_%' OR table_name = 'hemishe_e_university')
            ORDER BY table_name
            """;
        return entityManager.createNativeQuery(sql).getResultList();
    }

    /**
     * Bitta klassifikator jadvalining metadatasini olish
     */
    private Map<String, Object> getClassifierInfo(String tableName) {
        try {
            // Jadval nomidan classifier nomini olish
            // hemishe_h_gender -> h_gender
            // hemishe_e_university -> h_university
            String classifierName;
            if (tableName.startsWith("hemishe_h_")) {
                classifierName = "h_" + tableName.substring(10);
            } else if (tableName.equals("hemishe_e_university")) {
                classifierName = "h_university";
            } else {
                return null;
            }

            // Avval version ustuni borligini tekshiramiz
            boolean hasVersionColumn = checkColumnExists(tableName, "version");
            boolean hasDeleteTs = checkColumnExists(tableName, "delete_ts");

            // Count va max version olish (dinamik SQL)
            String countSql;
            if (hasVersionColumn && hasDeleteTs) {
                countSql = "SELECT COUNT(*), COALESCE(MAX(version), 0) FROM " + tableName + " WHERE delete_ts IS NULL";
            } else if (hasVersionColumn) {
                countSql = "SELECT COUNT(*), COALESCE(MAX(version), 0) FROM " + tableName;
            } else if (hasDeleteTs) {
                countSql = "SELECT COUNT(*), 0 FROM " + tableName + " WHERE delete_ts IS NULL";
            } else {
                countSql = "SELECT COUNT(*), 0 FROM " + tableName;
            }

            Object[] result = (Object[]) entityManager.createNativeQuery(countSql).getSingleResult();

            long count = ((Number) result[0]).longValue();
            int version = ((Number) result[1]).intValue();

            // Title ni aniqlash (klassifikator nomidan)
            String title = getClassifierTitle(classifierName);

            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("title", title);
            metadata.put("version", version);
            metadata.put("count", count);

            Map<String, Object> wrapper = new LinkedHashMap<>();
            wrapper.put(classifierName, metadata);

            return wrapper;
        } catch (Exception e) {
            log.debug("Klassifikator {} ma'lumotini olishda xatolik: {}", tableName, e.getMessage());
            return null;
        }
    }

    /**
     * Jadvalda ustun mavjudligini tekshirish
     */
    private boolean checkColumnExists(String tableName, String columnName) {
        try {
            String sql = """
                SELECT EXISTS (
                    SELECT 1 FROM information_schema.columns
                    WHERE table_schema = 'public'
                    AND table_name = :tableName
                    AND column_name = :columnName
                )
                """;
            return (Boolean) entityManager.createNativeQuery(sql)
                    .setParameter("tableName", tableName)
                    .setParameter("columnName", columnName)
                    .getSingleResult();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Klassifikator nomi asosida title ni aniqlash (OLD-HEMIS formatiga mos)
     */
    private String getClassifierTitle(String classifierName) {
        return switch (classifierName) {
            // Asosiy klassifikatorlar
            case "h_university" -> "OTMlar ro'yxati";
            case "h_gender" -> "Jinslar nonmlari";
            case "h_citizenship", "h_citizenship_type" -> "Fuqarolik holati";
            case "h_country" -> "Davlatlar ro'yxati";
            case "h_soato" -> "Viloyat va tumanlar";
            case "h_nationality" -> "";
            case "h_bachelor_speciality" -> "BSc ta'lim yo'nalishlari";
            case "h_master_speciality" -> "MSc mutaxassisliklari";
            case "h_doctoral_speciality" -> "Doktorantura mutaxassisliklari";
            case "h_education_type" -> "Ta'lim turlari";
            case "h_education_form" -> "Ta'lim shakllari";
            case "h_payment_form" -> "To'lov turlari";
            case "h_language" -> "Ta'lim tillari";

            // OTM va xodimlar
            case "h_university_form" -> "OTM tashkiliy shakllari";
            case "h_ownership" -> "OTM mulkchilik shakllari";
            case "h_structure_type" -> "OTM bo'linmalari turlari";
            case "h_employee_type" -> "Xodimlar toifalari";
            case "h_teacher_status" -> "O'qituvchi xolatlari";
            case "h_employment_staff" -> "Mehnat stavkalari";
            case "h_employment_form" -> "Mehnat shakllari";
            case "h_teacher_position_type" -> "Lavozim turlari";
            case "h_qualification" -> "Malaka oshirish joylari";
            case "h_teacher_success" -> "O'qituvchi yutuqlari";
            case "h_academic_degree" -> "Ilmiy darajalar";
            case "h_academic_rank" -> "Ilmiy unvonlar";

            // Talabalar
            case "h_student_status", "h_student_status_type" -> "Talaba holatlari";
            case "h_student_success" -> "Talaba yutuqlari";
            case "h_expel_reason" -> "Chetlatish sabablari";
            case "h_accommodation", "h_accomodation" -> "Yashash joyi turlari";
            case "h_social_category" -> "Ijtimoiy toifalar";
            case "h_doctoral_student_type" -> "Doktorant toifalari";
            case "h_academic_reason" -> "Talabalarga akademik ta'til berish sabablari";

            // O'quv jarayoni
            case "h_course" -> "O'quv kurslari";
            case "h_semestr_type", "h_semester" -> "Semestr turlari";
            case "h_marking_system", "h_grade_system_type" -> "Baholash tizimlari";
            case "h_grade_type" -> "Baho turlari";
            case "h_exam_type" -> "Nazorat turlari";
            case "h_final_exam_type" -> "Qaydnoma turlari";
            case "h_exam_finish" -> "Fanning yakuniy nazorat shakli";
            case "h_education_week_type" -> "O'quv grafik haftalari";
            case "h_subject_block" -> "Fanlar bloklari";
            case "h_subject_type" -> "Fanlar toifalari";
            case "h_training_type" -> "Mashg'ulot turlari";

            // Ilmiy faoliyat
            case "h_science_branch" -> "Fan tarmog'i va ixtisoslik nomi";
            case "h_project_type" -> "Ilmiy loyihalar turlari";
            case "h_locality" -> "Ilmiy loyiha maqomi";
            case "h_locality_type" -> "Mahalliylik turi";
            case "h_project_currency" -> "Valyuta turlari";
            case "h_project_executor_type" -> "Ijrochilar turlari";
            case "h_scientific_publication_type" -> "Ilmiy nashr turlari";
            case "h_methodical_publication_type" -> "Uslubiy nashr turlari";
            case "h_patient_type" -> "Intellektural mulklar";
            case "h_publication_database" -> "Ilmiy nashr bazalari";

            // Boshqalar
            case "h_stipend_rate" -> "Stipendiya turlari";
            case "h_auditorium_type" -> "Auditoriya turlari";
            case "h_device_type" -> "AKT qurilmalari";
            case "h_attendance_setting" -> "Davomat chegaralari";

            default -> "Klassifikator: " + classifierName;
        };
    }

    /**
     * Bitta klassifikatorni olish
     *
     * <p><strong>Legacy Endpoint:</strong> GET /app/rest/v2/services/classifiers/single?classifier=h_university</p>
     *
     * <p>OLD-HEMIS formatida klassifikator ma'lumotlarini qaytaradi:</p>
     * <pre>
     * {
     *   "success": true,
     *   "classifier": {
     *     "h_university": {
     *       "title": "Oliy ta'lim muassasalari ro'yxati",
     *       "version": 2358,
     *       "count": 238,
     *       "items": [...]
     *     }
     *   }
     * }
     * </pre>
     *
     * @param classifier klassifikator nomi (masalan: h_university, h_gender, h_citizenship)
     * @return klassifikator ma'lumotlari CUBA formatida
     */
    @GetMapping("/single")
    @Operation(
        summary = "Bitta klassifikatorni olish",
        description = "Klassifikator nomi bo'yicha barcha elementlarni OLD-HEMIS formatida qaytaradi"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli",
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"success\":true,\"classifier\":{\"h_university\":{\"title\":\"...\",\"version\":1,\"count\":238,\"items\":[...]}}}"))),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri klassifikator nomi")
    })
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> single(
            @Parameter(description = "Klassifikator nomi", required = true, example = "h_university")
            @RequestParam String classifier
    ) {
        log.info("GET /services/classifiers/single - classifier: {}", classifier);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);

        Map<String, Object> classifierData = new LinkedHashMap<>();

        // h_university maxsus holat - boshqa jadval strukturasi
        Map<String, Object> classifierResult;
        if ("h_university".equals(classifier)) {
            classifierResult = buildUniversityClassifier();
        } else {
            // Dinamik: classifier nomi asosida jadval va entity nomlarini generatsiya qilish
            // h_gender -> hemishe_h_gender, HGender
            // h_citizenship_type -> hemishe_h_citizenship, HCitizenship (type olib tashlanadi)
            classifierResult = buildDynamicClassifier(classifier);
        }

        if (classifierResult == null) {
            log.warn("Klassifikator topilmadi yoki jadval mavjud emas: {}", classifier);
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Klassifikator topilmadi: " + classifier);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        classifierData.put(classifier, classifierResult);

        response.put("classifier", classifierData);
        return ResponseEntity.ok(response);
    }

    /**
     * h_university klassifikatorini yaratish
     */
    private Map<String, Object> buildUniversityClassifier() {
        List<University> universities = universityRepository.findAll();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("title", "Oliy ta'lim muassasalari ro'yxati");
        result.put("version", calculateMaxVersion(universities));
        result.put("count", universities.size());

        List<Map<String, Object>> items = universities.stream()
                .map(this::mapUniversityToItem)
                .collect(Collectors.toList());

        result.put("items", items);
        return result;
    }

    /**
     * University entityni OLD-HEMIS formatiga o'girish
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> mapUniversityToItem(University u) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("_entityName", "hemishe_EUniversity");
        item.put("id", u.getCode());

        if (u.getStudentUrl() != null) item.put("studentUrl", u.getStudentUrl());
        item.put("code", u.getCode());
        if (u.getAddress() != null) item.put("address", u.getAddress());
        if (u.getUpdatedBy() != null) item.put("updatedBy", u.getUpdatedBy());

        // SOATO ma'lumotlarini olish
        if (u.getSoato() != null) {
            Map<String, Object> soatoObj = loadSoatoObject(u.getSoato());
            if (soatoObj != null) {
                item.put("soato", soatoObj);
            }
        }

        item.put("active", u.getActive() != null ? u.getActive() : false);
        item.put("version", u.getVersion() != null ? u.getVersion() : 1);
        if (u.getTeacherUrl() != null) item.put("teacherUrl", u.getTeacherUrl());
        if (u.getCreatedBy() != null) item.put("createdBy", u.getCreatedBy());
        if (u.getTin() != null) item.put("tin", u.getTin());
        item.put("name", u.getName());
        item.put("gpaEdit", u.getGpaEdit() != null ? u.getGpaEdit() : false);

        if (u.getCreateTs() != null) {
            item.put("createTs", u.getCreateTs().format(CUBA_DATE_FORMAT));
        }
        if (u.getUpdateTs() != null) {
            item.put("updateTs", u.getUpdateTs().format(CUBA_DATE_FORMAT));
        }

        return item;
    }

    /**
     * SOATO ob'ektini yuklash
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> loadSoatoObject(String soatoCode) {
        try {
            List<Object[]> results = entityManager.createNativeQuery(
                    "SELECT code, name_uz, name_ru, active, version, created_by, create_ts, updated_by, update_ts " +
                    "FROM hemishe_h_soato WHERE code = :code AND delete_ts IS NULL")
                    .setParameter("code", soatoCode)
                    .getResultList();

            if (results.isEmpty()) {
                return null;
            }

            Object[] row = results.get(0);
            Map<String, Object> soato = new LinkedHashMap<>();
            soato.put("_entityName", "hemishe_HSoato");
            soato.put("id", row[0]);
            soato.put("code", row[0]);
            if (row[3] != null) soato.put("active", row[3]);
            if (row[4] != null) soato.put("version", row[4]);
            if (row[1] != null) soato.put("name_uz", row[1]);
            if (row[2] != null) soato.put("name_ru", row[2]);
            if (row[5] != null) soato.put("createdBy", row[5]);
            if (row[6] != null) {
                java.sql.Timestamp ts = (java.sql.Timestamp) row[6];
                soato.put("createTs", ts.toLocalDateTime().format(CUBA_DATE_FORMAT));
            }
            if (row[7] != null) soato.put("updatedBy", row[7]);
            if (row[8] != null) {
                java.sql.Timestamp ts = (java.sql.Timestamp) row[8];
                soato.put("updateTs", ts.toLocalDateTime().format(CUBA_DATE_FORMAT));
            }

            return soato;
        } catch (Exception e) {
            log.warn("SOATO yuklashda xatolik (code={}): {}", soatoCode, e.getMessage());
            return null;
        }
    }

    /**
     * Maksimal versiyani hisoblash
     */
    private int calculateMaxVersion(List<University> universities) {
        return universities.stream()
                .filter(u -> u.getVersion() != null)
                .mapToInt(University::getVersion)
                .max()
                .orElse(1);
    }

    /**
     * Dinamik klassifikator yaratish - classifier nomi asosida jadval va entity nomlarini aniqlaydi
     *
     * <p>Konversiya qoidalari:</p>
     * <ul>
     *   <li>h_gender -> jadval: hemishe_h_gender, entity: HGender</li>
     *   <li>h_citizenship_type -> jadval: hemishe_h_citizenship, entity: HCitizenship</li>
     *   <li>h_bachelor_speciality -> jadval: hemishe_h_bachelor_speciality, entity: HBachelorSpeciality</li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> buildDynamicClassifier(String classifier) {
        // classifier nomidan jadval va entity nomlarini generatsiya qilish
        String baseName = classifier;
        if (baseName.startsWith("h_")) {
            baseName = baseName.substring(2);
        }

        // _type suffixini olib tashlash (h_citizenship_type -> citizenship)
        String tableBaseName = baseName;
        if (tableBaseName.endsWith("_type")) {
            tableBaseName = tableBaseName.substring(0, tableBaseName.length() - 5);
        }

        String tableName = "hemishe_h_" + tableBaseName;
        String entityName = "H" + toPascalCase(tableBaseName);

        log.debug("Dinamik klassifikator: {} -> jadval: {}, entity: {}", classifier, tableName, entityName);

        try {
            // Avval jadval mavjudligini tekshiramiz
            String checkSql = "SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = :tableName)";
            Boolean exists = (Boolean) entityManager.createNativeQuery(checkSql)
                    .setParameter("tableName", tableName)
                    .getSingleResult();

            if (!exists) {
                // _type suffixsiz qayta urinib ko'ramiz
                if (!baseName.equals(tableBaseName)) {
                    tableName = "hemishe_h_" + baseName;
                    entityName = "H" + toPascalCase(baseName);
                    exists = (Boolean) entityManager.createNativeQuery(checkSql)
                            .setParameter("tableName", tableName)
                            .getSingleResult();
                }

                if (!exists) {
                    log.warn("Jadval topilmadi: {} (classifier: {})", tableName, classifier);
                    return null;
                }
            }

            // Lambda uchun final o'zgaruvchilar
            final String finalTableName = tableName;
            final String finalEntityName = entityName;

            // Mavjud ustunlarni tekshirish
            boolean hasName = checkColumnExists(tableName, "name");
            boolean hasNameUz = checkColumnExists(tableName, "name_uz");
            boolean hasVersion = checkColumnExists(tableName, "version");
            boolean hasDeleteTs = checkColumnExists(tableName, "delete_ts");
            boolean hasCreateTs = checkColumnExists(tableName, "create_ts");
            boolean hasUpdateTs = checkColumnExists(tableName, "update_ts");
            boolean hasCreatedBy = checkColumnExists(tableName, "created_by");
            boolean hasUpdatedBy = checkColumnExists(tableName, "updated_by");

            // Dinamik SQL yaratish
            StringBuilder selectColumns = new StringBuilder("code");
            if (hasName) selectColumns.append(", name");
            else if (hasNameUz) selectColumns.append(", name_uz as name");
            else selectColumns.append(", code as name");

            if (hasVersion) selectColumns.append(", version");
            else selectColumns.append(", 0 as version");

            if (hasCreateTs) selectColumns.append(", create_ts");
            else selectColumns.append(", null as create_ts");

            if (hasUpdateTs) selectColumns.append(", update_ts");
            else selectColumns.append(", null as update_ts");

            if (hasCreatedBy) selectColumns.append(", created_by");
            else selectColumns.append(", null as created_by");

            if (hasUpdatedBy) selectColumns.append(", updated_by");
            else selectColumns.append(", null as updated_by");

            String whereClause = hasDeleteTs ? " WHERE delete_ts IS NULL" : "";
            String sql = "SELECT " + selectColumns + " FROM " + finalTableName + whereClause + " ORDER BY code";

            List<Object[]> results = entityManager.createNativeQuery(sql).getResultList();

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("title", getClassifierTitle(classifier));

            // Maksimal versiyani topamiz
            int maxVersion = results.stream()
                    .filter(row -> row[2] != null)
                    .mapToInt(row -> ((Number) row[2]).intValue())
                    .max()
                    .orElse(0);

            result.put("version", maxVersion);
            result.put("count", results.size());

            List<Map<String, Object>> items = results.stream()
                    .map(row -> {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("_entityName", "hemishe_" + finalEntityName);
                        item.put("id", String.valueOf(row[0]));
                        item.put("code", String.valueOf(row[0]));
                        item.put("name", row[1] != null ? String.valueOf(row[1]) : "");
                        item.put("version", row[2] != null ? ((Number) row[2]).intValue() : 0);

                        if (row[3] != null) {
                            java.sql.Timestamp ts = (java.sql.Timestamp) row[3];
                            item.put("createTs", ts.toLocalDateTime().format(CUBA_DATE_FORMAT));
                        }
                        if (row[4] != null) {
                            java.sql.Timestamp ts = (java.sql.Timestamp) row[4];
                            item.put("updateTs", ts.toLocalDateTime().format(CUBA_DATE_FORMAT));
                        }
                        if (row[5] != null) item.put("createdBy", row[5]);
                        if (row[6] != null) item.put("updatedBy", row[6]);

                        return item;
                    })
                    .collect(Collectors.toList());

            result.put("items", items);
            return result;
        } catch (Exception e) {
            log.error("Dinamik klassifikator yuklashda xatolik (classifier={}): {}", classifier, e.getMessage());
            return null;
        }
    }

    /**
     * snake_case ni PascalCase ga o'girish
     * Masalan: bachelor_speciality -> BachelorSpeciality
     */
    private String toPascalCase(String snakeCase) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : snakeCase.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else {
                result.append(capitalizeNext ? Character.toUpperCase(c) : c);
                capitalizeNext = false;
            }
        }
        return result.toString();
    }

    /**
     * Get hokimiyat (local government) classifiers
     *
     * <p><strong>Legacy Endpoint:</strong> GET /app/rest/v2/services/classifiers/hokimiyat</p>
     *
     * <p>Returns hierarchical structure of regions and districts</p>
     *
     * @return hokimiyat classifiers (regions and districts)
     */
    @GetMapping("/hokimiyat")
    @Operation(summary = "Get hokimiyat classifiers", description = "Returns local government structure (regions and districts)")
    public ResponseEntity<Map<String, Object>> hokimiyat() {
        log.info("GET /services/classifiers/hokimiyat");

        // Get regions (ERegion)
        List<Map<String, Object>> regions = classifierRepository.findAllByType("ERegion")
                .stream()
                .map(classifier -> {
                    Map<String, Object> region = new LinkedHashMap<>();
                    region.put("id", classifier.getId());
                    region.put("code", classifier.getCode());
                    region.put("name", classifier.getNameUz());
                    region.put("nameUz", classifier.getNameUz());
                    region.put("nameRu", classifier.getNameRu());
                    region.put("nameEn", classifier.getNameEn());

                    // Get districts for this region
                    // Note: Assuming district codes start with region code
                    List<Map<String, Object>> districts = classifierRepository.findAllByType("EDistrict")
                            .stream()
                            .filter(d -> d.getCode() != null && d.getCode().startsWith(classifier.getCode()))
                            .map(district -> {
                                Map<String, Object> d = new LinkedHashMap<>();
                                d.put("id", district.getId());
                                d.put("code", district.getCode());
                                d.put("name", district.getNameUz());
                                d.put("nameUz", district.getNameUz());
                                d.put("nameRu", district.getNameRu());
                                d.put("nameEn", district.getNameEn());
                                return d;
                            })
                            .collect(Collectors.toList());

                    region.put("districts", districts);
                    return region;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("regions", regions);
        result.put("totalRegions", regions.size());

        log.info("Returned {} regions with districts", regions.size());
        return ResponseEntity.ok(result);
    }
}
