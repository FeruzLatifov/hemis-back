package uz.hemis.service.classifier;

import lombok.Getter;
import uz.hemis.common.dto.classifier.ClassifierCategoryDto;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Classifier Metadata Registry — Barcha klasifikator jadvallarining whitelist + metadata registri.
 *
 * <p>116 ta klasifikator jadvali ({@code hemishe_h_*}) uchun metadata saqlaydi:
 * jadval nomi, API kalit, kategoriya, sarlavha va boshqa xususiyatlar.</p>
 *
 * <p><strong>Xavfsizlik:</strong> Faqat shu registrda ro'yxatga olingan jadvallar bilan ishlashga ruxsat beriladi.
 * Bu SQL injection xavfini to'liq oldini oladi.</p>
 */
public final class ClassifierMetadataRegistry {

    private ClassifierMetadataRegistry() {
    }

    // ==================== Kategoriyalar ====================

    public enum Category {
        GENERAL("Umumiy", "Общие", "General"),
        STRUCTURE("Tashkiliy tuzilma", "Организационная структура", "Organizational structure"),
        EMPLOYEE("Xodimlar", "Сотрудники", "Employees"),
        STUDENT("Talabalar", "Студенты", "Students"),
        EDUCATION("Ta'lim", "Образование", "Education"),
        STUDY("O'quv jarayoni", "Учебный процесс", "Study process"),
        SCIENCE("Ilmiy", "Научная деятельность", "Science"),
        FINANCIAL("Moliyaviy", "Финансовые", "Financial"),
        DIPLOMA("Diplom", "Дипломы", "Diploma"),
        SPECIALITY("Mutaxassisliklar", "Специальности", "Specialities");

        @Getter
        private final String titleUz;
        @Getter
        private final String titleRu;
        @Getter
        private final String titleEn;

        Category(String titleUz, String titleRu, String titleEn) {
            this.titleUz = titleUz;
            this.titleRu = titleRu;
            this.titleEn = titleEn;
        }
    }

    // ==================== Classifier Metadata ====================

    @Getter
    public static class ClassifierMeta {
        private final String tableName;
        private final String apiKey;
        private final Category category;
        private final String titleUz;
        private final String titleRu;
        private final String titleEn;
        private final boolean editable;
        private final boolean hierarchical;

        ClassifierMeta(String tableName, String apiKey, Category category,
                       String titleUz, String titleRu, String titleEn,
                       boolean editable, boolean hierarchical) {
            this.tableName = tableName;
            this.apiKey = apiKey;
            this.category = category;
            this.titleUz = titleUz;
            this.titleRu = titleRu;
            this.titleEn = titleEn;
            this.editable = editable;
            this.hierarchical = hierarchical;
        }
    }

    // ==================== Registr ====================

    /** apiKey -> ClassifierMeta */
    private static final Map<String, ClassifierMeta> REGISTRY = new LinkedHashMap<>();

    /** tableName -> apiKey (teskari mapping) */
    private static final Map<String, String> TABLE_TO_API_KEY = new HashMap<>();

    static {
        // ─── GENERAL (Umumiy) ───
        reg("hemishe_h_gender", Category.GENERAL, "Jins turlari", "Типы пола", "Gender types", true, false);
        reg("hemishe_h_nationality", Category.GENERAL, "Millatlar", "Национальности", "Nationalities", true, false);
        reg("hemishe_h_citizenship", Category.GENERAL, "Fuqarolik turlari", "Типы гражданства", "Citizenship types", true, false);
        reg("hemishe_h_country", Category.GENERAL, "Davlatlar", "Страны", "Countries", true, false);
        reg("soato", Category.GENERAL, "Viloyat va tumanlar (SOATO)", "Области и районы (СОАТО)", "Regions and districts (SOATO)", false, true);
        reg("hemishe_h_locality_type", Category.GENERAL, "Joylashuv turlari", "Типы местности", "Locality types", true, false);
        reg("hemishe_h_terrain", Category.GENERAL, "Hudud turi", "Тип территории", "Terrain types", true, false);
        reg("hemishe_h_education_language", Category.GENERAL, "Ta'lim tillari", "Языки обучения", "Education languages", true, false);
        reg("hemishe_h_certificate_type", Category.GENERAL, "Sertifikat turlari", "Типы сертификатов", "Certificate types", true, false);
        // RENAMED: DB=certificate_name, apiKey saqlandi
        reg("hemishe_h_certificate_names", "certificate-names", Category.GENERAL, "Sertifikat nomlari", "Названия сертификатов", "Certificate names", true, false);
        reg("hemishe_h_certificate_subjects", "certificate-subjects", Category.GENERAL, "Sertifikat fanlari", "Предметы сертификатов", "Certificate subjects", true, false);
        reg("hemishe_h_certificate_grades", "certificate-grades", Category.GENERAL, "Sertifikat baholari", "Оценки сертификатов", "Certificate grades", true, false);
        reg("hemishe_h_resource_type", Category.GENERAL, "Resurs turlari", "Типы ресурсов", "Resource types", true, false);
        // RENAMED: DB=hemis_version, apiKey saqlandi
        reg("hemishe_h_hemis_version_type", "hemis-version-type", Category.GENERAL, "HEMIS versiya turlari", "Типы версий HEMIS", "HEMIS version types", false, false);
        reg("hemishe_h_outside_activities", "outside-activities", Category.GENERAL, "Auditoriyadan tashqari mashg'ulotlar", "Внеаудиторные занятия", "Outside activities", true, false);
        reg("hemishe_h_sport_type", Category.GENERAL, "Sport turlari", "Виды спорта", "Sport types", true, false);
        reg("hemishe_h_device_type", Category.GENERAL, "Qurilma turlari", "Типы устройств", "Device types", true, false);
        reg("hemishe_h_auditorium_type", Category.GENERAL, "Auditoriya turlari", "Типы аудиторий", "Auditorium types", true, false);
        reg("hemishe_h_external_service_type", Category.GENERAL, "Tashqi xizmat turlari", "Типы внешних услуг", "External service types", true, false);
        reg("hemishe_h_poverty_level", Category.GENERAL, "Kam ta'minlanganlik darajasi", "Уровень бедности", "Poverty levels", true, false);
        reg("hemishe_h_patient_type", Category.GENERAL, "Bemor turlari", "Типы пациентов", "Patient types", true, false);
        reg("hemishe_h_verification_type", Category.GENERAL, "Tekshiruv turlari", "Типы верификации", "Verification types", true, false);

        // ─── STRUCTURE (Tashkiliy tuzilma) ───
        reg("hemishe_h_university_type", Category.STRUCTURE, "OTM turlari", "Типы вузов", "University types", true, false);
        reg("hemishe_h_university_department_type", Category.STRUCTURE, "Bo'linma turlari", "Типы подразделений", "Department types", true, false);
        reg("hemishe_h_ownership", Category.STRUCTURE, "Mulkchilik shakllari", "Формы собственности", "Ownership forms", true, false);

        // ─── EMPLOYEE (Xodimlar) ───
        // NOTE: teacher_position_type olib tashlandi — position (V013) + S008 seed
        //       aynan shu ma'lumotni saqlaydi (position_type='12' — Professor-o'qituvchi xodimlar).
        reg("hemishe_h_university_employee_type", Category.EMPLOYEE, "Xodim turlari", "Типы сотрудников", "Employee types", true, false);
        reg("hemishe_h_university_employee_status_type", Category.EMPLOYEE, "Xodim holatlari", "Статусы сотрудников", "Employee status types", true, false);
        // RENAMED: DB=employment_form, apiKey=university-employee-form (legacy compat)
        reg("hemishe_h_university_employee_form", "university-employee-form", Category.EMPLOYEE, "Xodim shtat turlari", "Штатные формы сотрудников", "Employee staff forms", true, false);
        // UNCHANGED: hemishe_h_employment_form — boshqa jadval, migratsiya qilinmagan
        reg("hemishe_h_employment_form", Category.EMPLOYEE, "Bandlik shakllari", "Формы занятости", "Employment forms", true, false);
        reg("hemishe_h_qualification", Category.EMPLOYEE, "Malaka toifalari", "Категории квалификации", "Qualification categories", true, false);
        reg("hemishe_h_academic_degree", Category.EMPLOYEE, "Ilmiy darajalar", "Ученые степени", "Academic degrees", true, false);
        reg("hemishe_h_academic_rank", Category.EMPLOYEE, "Ilmiy unvonlar", "Ученые звания", "Academic ranks", true, false);
        reg("hemishe_h_teacher_achievement_type", Category.EMPLOYEE, "O'qituvchi yutuqlari turlari", "Типы достижений преподавателей", "Teacher achievement types", true, false);
        reg("hemishe_h_teacher_conduction_form", Category.EMPLOYEE, "Dars o'tkazish shakllari", "Формы проведения занятий", "Teaching conduction forms", true, false);

        // ─── STUDENT (Talabalar) ───
        reg("hemishe_h_student_status_type", Category.STUDENT, "Talaba holatlari", "Статусы студентов", "Student status types", true, false);
        reg("hemishe_h_student_type", Category.STUDENT, "Talaba toifalari", "Категории студентов", "Student types", true, false);
        reg("hemishe_h_student_social_type", Category.STUDENT, "Ijtimoiy toifalar", "Социальные категории", "Social categories", true, false);
        reg("hemishe_h_student_achievement_type", Category.STUDENT, "Talaba yutuqlari turlari", "Типы достижений студентов", "Student achievement types", true, false);
        reg("hemishe_h_accomodation", Category.STUDENT, "Yashash joyi turlari", "Типы проживания", "Accommodation types", true, false);
        reg("hemishe_h_student_living_status", Category.STUDENT, "Yashash joyi holati", "Статус проживания", "Living status", true, false);
        reg("hemishe_h_student_room_mate_type", Category.STUDENT, "Birgalikda yashash toifasi", "Типы соседей по комнате", "Roommate types", true, false);
        reg("hemishe_h_expel", Category.STUDENT, "Chetlatish sabablari", "Причины отчисления", "Expulsion reasons", true, false);
        reg("hemishe_h_academic_reason", Category.STUDENT, "Akademik ta'til sabablari", "Причины академического отпуска", "Academic leave reasons", true, false);
        reg("hemishe_h_admission_type", Category.STUDENT, "Qabul turlari", "Типы приема", "Admission types", true, false);
        reg("hemishe_h_transfer_type", Category.STUDENT, "O'tkazish turlari", "Типы перевода", "Transfer types", true, false);
        reg("hemishe_h_academic_mobile_type", Category.STUDENT, "Akademik mobillik turlari", "Типы академической мобильности", "Academic mobility types", true, false);

        // ─── EDUCATION (Ta'lim) ───
        reg("hemishe_h_education_type", Category.EDUCATION, "Ta'lim turlari", "Типы образования", "Education types", true, false);
        reg("hemishe_h_education_form", Category.EDUCATION, "Ta'lim shakllari", "Формы образования", "Education forms", true, false);
        reg("hemishe_h_education_year", Category.EDUCATION, "O'quv yillari", "Учебные года", "Education years", true, false);
        reg("course", Category.EDUCATION, "O'quv kurslari", "Учебные курсы", "Courses", true, false);
        reg("hemishe_h_semester", Category.EDUCATION, "Semestrlar", "Семестры", "Semesters", true, false);
        reg("hemishe_h_semester_list", Category.EDUCATION, "Semestr turlari", "Типы семестров", "Semester types", true, false);
        reg("hemishe_h_education_week_type", Category.EDUCATION, "Ta'lim hafta turlari", "Типы учебных недель", "Education week types", true, false);
        reg("hemishe_h_subject_block", Category.EDUCATION, "Fan bloklari", "Блоки дисциплин", "Subject blocks", true, false);
        reg("hemishe_h_subject_type", Category.EDUCATION, "Fan turlari", "Типы дисциплин", "Subject types", true, false);
        reg("hemishe_h_study_schedule_type", Category.EDUCATION, "Mashg'ulot turlari", "Типы занятий", "Study schedule types", true, false);
        reg("hemishe_h_attandance_setting", Category.EDUCATION, "Davomat sozlamalari", "Настройки посещаемости", "Attendance settings", true, false);
        reg("hemishe_h_graduate_fields_type", Category.EDUCATION, "Bitiruvchi soha turlari", "Типы направлений выпускников", "Graduate field types", true, false);
        reg("hemishe_h_graduate_inactive_type", Category.EDUCATION, "Bitiruvchi nofaol turlari", "Типы неактивности выпускников", "Graduate inactive types", true, false);

        // ─── STUDY (O'quv jarayoni) ───
        reg("hemishe_h_exam_type", Category.STUDY, "Imtihon turlari", "Типы экзаменов", "Exam types", true, false);
        reg("hemishe_h_exam_finish", Category.STUDY, "Imtihon yakunlash turlari", "Типы завершения экзаменов", "Exam finish types", true, false);
        reg("hemishe_h_final_exam_type", Category.STUDY, "Yakuniy imtihon turlari", "Типы итоговых экзаменов", "Final exam types", true, false);
        reg("hemishe_h_score_type", Category.STUDY, "Baholash tizimlari", "Системы оценивания", "Score types", true, false);
        reg("hemishe_h_grade_system_type", Category.STUDY, "Baho turlari", "Типы оценок", "Grade system types", true, false);
        reg("hemishe_h_internship_type", Category.STUDY, "Amaliyot turlari", "Типы практики", "Internship types", true, false);
        reg("hemishe_h_internship_form", Category.STUDY, "Amaliyot shakllari", "Формы практики", "Internship forms", true, false);

        // ─── SCIENCE (Ilmiy) ───
        reg("hemishe_h_doctoral_student_type", Category.SCIENCE, "Doktorant turlari", "Типы докторантов", "Doctoral student types", true, false);
        reg("hemishe_h_doctoral_student_status", Category.SCIENCE, "Doktorant holatlari", "Статусы докторантов", "Doctoral student statuses", true, false);
        reg("hemishe_h_science_branch", Category.SCIENCE, "Fan tarmoqlari", "Отрасли науки", "Science branches", true, false);
        reg("hemishe_h_publication_type", Category.SCIENCE, "Ilmiy nashr turlari", "Типы научных публикаций", "Publication types", true, false);
        reg("hemishe_h_methodical_publication_type", Category.SCIENCE, "Metodik nashr turlari", "Типы методических публикаций", "Methodical publication types", true, false);
        reg("hemishe_h_publication_database", Category.SCIENCE, "Nashr bazalari", "Базы публикаций", "Publication databases", true, false);
        reg("hemishe_h_scholar_database", Category.SCIENCE, "Ilmiy platformalar", "Научные платформы", "Scholar databases", true, false);
        reg("hemishe_h_project_type", Category.SCIENCE, "Loyiha turlari", "Типы проектов", "Project types", true, false);
        reg("hemishe_h_project_locality", Category.SCIENCE, "Loyiha joylashuvlari", "Локации проектов", "Project localities", true, false);
        reg("hemishe_h_currency", Category.SCIENCE, "Valyutalar", "Валюты", "Currencies", true, false);
        reg("hemishe_h_project_executor_type", Category.SCIENCE, "Loyiha ijrochi turlari", "Типы исполнителей проектов", "Project executor types", true, false);

        // ─── FINANCIAL (Moliyaviy) ───
        reg("hemishe_h_payment_form", Category.FINANCIAL, "To'lov turlari", "Типы оплаты", "Payment forms", true, false);
        reg("hemishe_h_grant_type", Category.FINANCIAL, "Grant turlari", "Типы грантов", "Grant types", true, false);
        reg("hemishe_h_stipend_rate", Category.FINANCIAL, "Stipendiya turlari", "Типы стипендий", "Stipend rates", true, false);
        reg("hemishe_h_stipend_rate_category", Category.FINANCIAL, "Stipendiya toifalari", "Категории стипендий", "Stipend rate categories", true, false);
        reg("hemishe_h_contract_type", Category.FINANCIAL, "Shartnoma turlari", "Типы договоров", "Contract types", true, false);
        // RENAMED: DB=contract_class, apiKey=contract-types (legacy compat)
        reg("hemishe_h_contract_types", "contract-types", Category.FINANCIAL, "Shartnoma sinflari", "Классы договоров", "Contract classes", true, false);
        reg("hemishe_h_contract_summa_type", Category.FINANCIAL, "Shartnoma summa turlari", "Типы сумм договоров", "Contract amount types", true, false);
        reg("hemishe_h_decree_type", Category.FINANCIAL, "Buyruq turlari", "Типы приказов", "Decree types", true, false);
        reg("hemishe_h_decree_type_param", Category.FINANCIAL, "Buyruq turi parametrlari", "Параметры типа приказа", "Decree type parameters", true, false);
        reg("hemishe_h_scholarship_decree_type", Category.FINANCIAL, "Stipendiya qaror turlari", "Типы решений по стипендиям", "Scholarship decree types", true, false);

        // ─── DIPLOMA (Diplom) ───
        reg("hemishe_h_diplom_blank_status", Category.DIPLOMA, "Diplom blank holati", "Статус бланка диплома", "Diploma blank status", true, false);
        reg("hemishe_h_diplom_blank_category", Category.DIPLOMA, "Diplom blank toifasi", "Категория бланка диплома", "Diploma blank category", true, false);

        // ─── SPECIALITY (Mutaxassisliklar) — CUBA legacy, tegilmadi ───
        reg("hemishe_h_bachelor_speciality", Category.SPECIALITY, "Bakalavriat yo'nalishlari", "Направления бакалавриата", "Bachelor specialities", false, false);
        reg("hemishe_h_master_speciality", Category.SPECIALITY, "Magistratura mutaxassisliklari", "Специальности магистратуры", "Master specialities", false, false);
        reg("hemishe_h_speciality_ordinatura", Category.SPECIALITY, "Ordinatura mutaxassisliklari", "Специальности ординатуры", "Ordinatura specialities", false, false);
        reg("hemishe_h_speciality_bachelor", Category.SPECIALITY, "Bakalavriat mutaxassisliklari (eski)", "Специальности бакалавриата (старые)", "Bachelor specialities (legacy)", false, false);
        reg("hemishe_h_speciality_master", Category.SPECIALITY, "Magistratura mutaxassisliklari (eski)", "Специальности магистратуры (старые)", "Master specialities (legacy)", false, false);
    }

    /**
     * Register a classifier (apiKey auto-derived from table name).
     * Simple migrated: reg("gender", ...) → apiKey="gender"
     * Legacy CUBA: reg("hemishe_h_speciality_master", ...) → apiKey="speciality-master"
     */
    private static void reg(String tableName, Category category,
                            String titleUz, String titleRu, String titleEn,
                            boolean editable, boolean hierarchical) {
        reg(tableName, tableNameToApiKey(tableName), category, titleUz, titleRu, titleEn, editable, hierarchical);
    }

    /**
     * Register a classifier with explicit apiKey (used when DB table is renamed but API must stay same).
     * Example: reg("hemishe_h_contract_types", "contract-types", ...) — yangi jadval "contract_class" lekin API "contract-types" saqlanadi.
     */
    private static void reg(String tableName, String apiKey, Category category,
                            String titleUz, String titleRu, String titleEn,
                            boolean editable, boolean hierarchical) {
        ClassifierMeta meta = new ClassifierMeta(tableName, apiKey, category,
                titleUz, titleRu, titleEn, editable, hierarchical);
        REGISTRY.put(apiKey, meta);
        TABLE_TO_API_KEY.put(tableName, apiKey);
    }

    /**
     * Derive apiKey from table name.
     *   hemishe_h_education_type → education-type (legacy)
     *   education_type → education-type (yangi)
     */
    public static String tableNameToApiKey(String tableName) {
        if (tableName == null) {
            throw new IllegalArgumentException("Invalid classifier table name: null");
        }
        String stripped = tableName.startsWith("hemishe_h_")
                ? tableName.substring("hemishe_h_".length())
                : tableName;
        return stripped.replace('_', '-');
    }

    /**
     * education-type -> hemishe_h_education_type (legacy)
     * Tavsiya: {@link #getByApiKey(String)} dan foydalanish — u aniq DB jadvalni qaytaradi.
     */
    public static String apiKeyToTableName(String apiKey) {
        if (apiKey == null) {
            return null;
        }
        return "hemishe_h_" + apiKey.replace('-', '_');
    }

    // ==================== Public API ====================

    /**
     * Get classifier metadata by API key.
     * Returns null if not in whitelist.
     */
    public static ClassifierMeta getByApiKey(String apiKey) {
        return REGISTRY.get(apiKey);
    }

    /**
     * Check if an API key is registered in the whitelist.
     */
    public static boolean isRegistered(String apiKey) {
        return REGISTRY.containsKey(apiKey);
    }

    /**
     * Get all registered classifiers.
     */
    public static Collection<ClassifierMeta> getAll() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    /**
     * Get classifiers by category.
     */
    public static List<ClassifierMeta> getByCategory(Category category) {
        return REGISTRY.values().stream()
                .filter(m -> m.getCategory() == category)
                .collect(Collectors.toList());
    }

    /**
     * Get all categories with their classifier counts.
     */
    public static List<ClassifierCategoryDto> getAllCategories() {
        List<ClassifierCategoryDto> categories = new ArrayList<>();
        for (Category cat : Category.values()) {
            long count = REGISTRY.values().stream()
                    .filter(m -> m.getCategory() == cat)
                    .count();
            categories.add(ClassifierCategoryDto.builder()
                    .key(cat.name().toLowerCase())
                    .titleUz(cat.getTitleUz())
                    .titleRu(cat.getTitleRu())
                    .titleEn(cat.getTitleEn())
                    .classifierCount((int) count)
                    .build());
        }
        return categories;
    }

    /**
     * Resolve category from string key (case-insensitive).
     * Returns null if not found.
     */
    public static Category resolveCategory(String categoryKey) {
        if (categoryKey == null) return null;
        try {
            return Category.valueOf(categoryKey.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
