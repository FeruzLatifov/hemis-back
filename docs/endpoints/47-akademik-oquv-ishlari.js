// 47.Akademik O'quv ishlari endpoints
// O'quv ishlari - O'quv ishlari haqida ma'lumot
// hemishe_RIAcademicEducationalWork

const endpoints_47 = [
    // ============================================
    // 47.Akademik O'quv ishlari - 7 endpoint
    // ============================================
    {
        id: 1,
        category: "47.Akademik O'quv ishlari",
        name: "Yangi yozuv yaratish (POST)",
        method: "POST",
        url: "/app/rest/v2/entities/hemishe_RIAcademicEducationalWork",
        requiresAuth: true,
        dependsOn: 1,
        description: "O'quv ishlari - yangi yozuv yaratish",
        storeResultId: "academicEducationalWorkEntityId",
        inputFields: {
            university: { label: "Universitet kodi", type: "text", required: false, placeholder: "999", defaultNew: "999", defaultOld: "999" },
            educationYear: { label: "O'quv yili kodi", type: "text", required: false, placeholder: "2021", defaultNew: "2021", defaultOld: "2021" },
            specialityCode: { label: "Mutaxassislik kodi", type: "text", required: false, placeholder: "123456", defaultNew: "123456", defaultOld: "123456" },
            specialityName: { label: "Mutaxassislik nomi", type: "text", required: false, placeholder: "Mutaxassislik", defaultNew: "Informatika", defaultOld: "Informatika" },
            document: { label: "Hujjat", type: "text", required: false, placeholder: "12/5 01.01.2020", defaultNew: "12/5 01.01.2024", defaultOld: "12/5 01.01.2024" },
            subjects: { label: "Fanlar", type: "text", required: false, placeholder: "Fan1, Fan2", defaultNew: "Dasturlash, Ma'lumotlar bazasi", defaultOld: "Dasturlash, Ma'lumotlar bazasi" },
            languageName: { label: "Til", type: "text", required: false, placeholder: "Ingliz tili", defaultNew: "Ingliz tili", defaultOld: "Ingliz tili" },
            course: { label: "Kurs kodi", type: "text", required: false, placeholder: "11", defaultNew: "11", defaultOld: "11" },
            studentCount: { label: "Talabalar soni", type: "number", required: false, placeholder: "80", defaultNew: "50", defaultOld: "50" }
        },
        bodyGenerator: (fields) => {
            const body = {};
            if (fields.university) body.university = { id: fields.university };
            if (fields.educationYear) body.educationYear = { id: fields.educationYear };
            if (fields.specialityCode) body.specialityCode = fields.specialityCode;
            if (fields.specialityName) body.specialityName = fields.specialityName;
            if (fields.document) body.document = fields.document;
            if (fields.subjects) body.subjects = fields.subjects;
            if (fields.languageName) body.languageName = fields.languageName;
            if (fields.course) body.course = { id: fields.course };
            if (fields.studentCount) body.studentCount = parseInt(fields.studentCount);
            return body;
        },
        ported: true
    },
    {
        id: 2,
        category: "47.Akademik O'quv ishlari",
        name: "Barcha yozuvlarni olish (GET)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAcademicEducationalWork",
        requiresAuth: true,
        dependsOn: 1,
        description: "O'quv ishlari - barcha yozuvlarni olish",
        storeFirstId: "academicEducationalWorkEntityId",
        inputFields: {
            limit: { label: "Limit", type: "number", required: false, placeholder: "50", default: "50" },
            offset: { label: "Offset", type: "number", required: false, placeholder: "0", default: "0" },
            returnCount: { label: "Jami sonni qaytarish", type: "select", options: [{ value: "true", label: "Ha" }, { value: "false", label: "Yo'q" }], default: "true", required: false },
            view: { label: "View", type: "text", required: false, placeholder: "rIAcademicEducationalWork-view", defaultNew: "rIAcademicEducationalWork-view", defaultOld: "rIAcademicEducationalWork-view" }
        },
        ported: true
    },
    {
        id: 3,
        category: "47.Akademik O'quv ishlari",
        name: "ID bo'yicha olish (GET)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAcademicEducationalWork/{entityId}",
        requiresAuth: true,
        dependsOn: 1,
        description: "O'quv ishlari - ID bo'yicha olish",
        inputFields: {
            entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "academicEducationalWorkEntityId" }
        },
        ported: true
    },
    {
        id: 4,
        category: "47.Akademik O'quv ishlari",
        name: "Yozuvni yangilash (PUT)",
        method: "PUT",
        url: "/app/rest/v2/entities/hemishe_RIAcademicEducationalWork/{entityId}",
        requiresAuth: true,
        dependsOn: 1,
        description: "O'quv ishlari - yozuvni yangilash",
        inputFields: {
            entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "academicEducationalWorkEntityId" },
            subjects: { label: "Fanlar", type: "text", required: false, placeholder: "Fan1, Fan2", defaultNew: "Yangilangan fanlar", defaultOld: "Yangilangan fanlar" },
            studentCount: { label: "Talabalar soni", type: "number", required: false, placeholder: "100", defaultNew: "100", defaultOld: "100" }
        },
        bodyGenerator: (fields) => {
            const body = {};
            if (fields.subjects) body.subjects = fields.subjects;
            if (fields.studentCount) body.studentCount = parseInt(fields.studentCount);
            return body;
        },
        ported: true
    },
    {
        id: 5,
        category: "47.Akademik O'quv ishlari",
        name: "Yozuvni o'chirish (DELETE)",
        method: "DELETE",
        url: "/app/rest/v2/entities/hemishe_RIAcademicEducationalWork/{entityId}",
        requiresAuth: true,
        dependsOn: 1,
        description: "O'quv ishlari - yozuvni o'chirish",
        inputFields: {
            entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "academicEducationalWorkEntityId" }
        },
        ported: true
    },
    {
        id: 6,
        category: "47.Akademik O'quv ishlari",
        name: "Qidirish (GET)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAcademicEducationalWork/search",
        requiresAuth: true,
        dependsOn: 1,
        description: "O'quv ishlari - qidirish (GET)",
        inputFields: {
            filter: { label: "CUBA JSON filter", type: "textarea", rows: 3, placeholder: '{"conditions":[]}', defaultNew: '', defaultOld: '', required: false },
            limit: { label: "Limit", type: "number", placeholder: "50", defaultNew: "10", defaultOld: "10", required: false },
            offset: { label: "Offset", type: "number", placeholder: "0", defaultNew: "0", defaultOld: "0", required: false }
        },
        urlBuilder: function(fields) {
            let params = [];
            if (fields.filter && fields.filter.trim()) {
                params.push("filter=" + encodeURIComponent(fields.filter.trim()));
            } else {
                params.push("filter=" + encodeURIComponent('{"conditions":[]}'));
            }
            if (fields.limit) params.push("limit=" + encodeURIComponent(fields.limit));
            if (fields.offset) params.push("offset=" + encodeURIComponent(fields.offset));
            return this.url + (params.length > 0 ? "?" + params.join("&") : "");
        },
        ported: true
    },
    {
        id: 7,
        category: "47.Akademik O'quv ishlari",
        name: "Qidirish (POST)",
        method: "POST",
        url: "/app/rest/v2/entities/hemishe_RIAcademicEducationalWork/search",
        requiresAuth: true,
        dependsOn: 1,
        description: "O'quv ishlari - qidirish (POST)",
        inputFields: {
            filter: { label: "CUBA JSON filter", type: "textarea", rows: 3, placeholder: '{"conditions":[]}', defaultNew: '', defaultOld: '', required: false },
            limit: { label: "Limit", type: "number", placeholder: "50", defaultNew: "10", defaultOld: "10", required: false },
            offset: { label: "Offset", type: "number", placeholder: "0", defaultNew: "0", defaultOld: "0", required: false }
        },
        bodyGenerator: (fields) => {
            const body = { limit: parseInt(fields.limit) || 50, offset: parseInt(fields.offset) || 0 };
            if (fields.filter && fields.filter.trim()) {
                try { body.filter = JSON.parse(fields.filter); } catch (e) { body.filter = { conditions: [] }; }
            } else {
                body.filter = { conditions: [] };
            }
            return body;
        },
        ported: true
    }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_47;
}
