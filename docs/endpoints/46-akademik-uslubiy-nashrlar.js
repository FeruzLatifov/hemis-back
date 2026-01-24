// 46.Akademik Uslubiy nashrlar endpoints
// Uslubiy nashrlar - Uslubiy nashrlar haqida ma'lumot
// hemishe_RIAcademicMethodologicPublications

const endpoints_46 = [
    // ============================================
    // 46.Akademik Uslubiy nashrlar - 7 endpoint
    // ============================================
    {
        id: 1,
        category: "46.Akademik Uslubiy nashrlar",
        name: "Yangi yozuv yaratish (POST)",
        method: "POST",
        url: "/app/rest/v2/entities/hemishe_RIAcademicMethodologicPublications",
        requiresAuth: true,
        dependsOn: 1,
        description: "Uslubiy nashrlar - yangi yozuv yaratish",
        storeResultId: "academicMethodologicPublicationsEntityId",
        inputFields: {
            university: { label: "Universitet kodi", type: "text", required: false, placeholder: "999", defaultNew: "999", defaultOld: "999" },
            educationYear: { label: "O'quv yili kodi", type: "text", required: false, placeholder: "2021", defaultNew: "2021", defaultOld: "2021" },
            authorFullname: { label: "Muallif ismi", type: "text", required: true, placeholder: "Ism Familiya", defaultNew: "Test Muallif", defaultOld: "Test Muallif" },
            specialityCode: { label: "Mutaxassislik kodi", type: "text", required: false, placeholder: "123456", defaultNew: "101010", defaultOld: "101010" },
            specialityName: { label: "Mutaxassislik nomi", type: "text", required: false, placeholder: "Informatika", defaultNew: "Informatika", defaultOld: "Informatika" },
            bookType: { label: "Kitob turi", type: "text", required: false, placeholder: "textbook/manual", defaultNew: "textbook", defaultOld: "textbook" },
            bookName: { label: "Kitob nomi", type: "text", required: false, placeholder: "Kitob nomi", defaultNew: "Test darslik", defaultOld: "Test darslik" },
            certificateDate: { label: "Sertifikat sanasi", type: "text", required: false, placeholder: "2024-01-15", defaultNew: "2024-01-15", defaultOld: "2024-01-15" },
            certificateNumber: { label: "Sertifikat raqami", type: "text", required: false, placeholder: "123/1", defaultNew: "123/1", defaultOld: "123/1" }
        },
        bodyGenerator: (fields) => {
            const body = {};
            if (fields.university) body.university = { id: fields.university };
            if (fields.educationYear) body.educationYear = { id: fields.educationYear };
            if (fields.authorFullname) body.authorFullname = fields.authorFullname;
            if (fields.specialityCode) body.specialityCode = fields.specialityCode;
            if (fields.specialityName) body.specialityName = fields.specialityName;
            if (fields.bookType) body.bookType = fields.bookType;
            if (fields.bookName) body.bookName = fields.bookName;
            if (fields.certificateDate) body.certificateDate = fields.certificateDate;
            if (fields.certificateNumber) body.certificateNumber = fields.certificateNumber;
            return body;
        },
        ported: true
    },
    {
        id: 2,
        category: "46.Akademik Uslubiy nashrlar",
        name: "Barcha yozuvlarni olish (GET)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAcademicMethodologicPublications",
        requiresAuth: true,
        dependsOn: 1,
        description: "Uslubiy nashrlar - barcha yozuvlarni olish",
        storeFirstId: "academicMethodologicPublicationsEntityId",
        inputFields: {
            limit: { label: "Limit", type: "number", required: false, placeholder: "50", default: "50" },
            offset: { label: "Offset", type: "number", required: false, placeholder: "0", default: "0" },
            returnCount: { label: "Jami sonni qaytarish", type: "select", options: [{ value: "true", label: "Ha" }, { value: "false", label: "Yo'q" }], default: "true", required: false },
            view: { label: "View", type: "text", required: false, placeholder: "rIAcademicMethodologicPublications-view", defaultNew: "rIAcademicMethodologicPublications-view", defaultOld: "rIAcademicMethodologicPublications-view" }
        },
        ported: true
    },
    {
        id: 3,
        category: "46.Akademik Uslubiy nashrlar",
        name: "ID bo'yicha olish (GET)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAcademicMethodologicPublications/{entityId}",
        requiresAuth: true,
        dependsOn: 1,
        description: "Uslubiy nashrlar - ID bo'yicha olish",
        inputFields: {
            entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "academicMethodologicPublicationsEntityId" }
        },
        ported: true
    },
    {
        id: 4,
        category: "46.Akademik Uslubiy nashrlar",
        name: "Yozuvni yangilash (PUT)",
        method: "PUT",
        url: "/app/rest/v2/entities/hemishe_RIAcademicMethodologicPublications/{entityId}",
        requiresAuth: true,
        dependsOn: 1,
        description: "Uslubiy nashrlar - yozuvni yangilash",
        inputFields: {
            entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "academicMethodologicPublicationsEntityId" },
            authorFullname: { label: "Muallif ismi", type: "text", required: false, placeholder: "Ism Familiya", defaultNew: "Yangilangan Muallif", defaultOld: "Yangilangan Muallif" },
            bookName: { label: "Kitob nomi", type: "text", required: false, placeholder: "Kitob nomi", defaultNew: "Yangilangan darslik", defaultOld: "Yangilangan darslik" }
        },
        bodyGenerator: (fields) => {
            const body = {};
            if (fields.authorFullname) body.authorFullname = fields.authorFullname;
            if (fields.bookName) body.bookName = fields.bookName;
            return body;
        },
        ported: true
    },
    {
        id: 5,
        category: "46.Akademik Uslubiy nashrlar",
        name: "Yozuvni o'chirish (DELETE)",
        method: "DELETE",
        url: "/app/rest/v2/entities/hemishe_RIAcademicMethodologicPublications/{entityId}",
        requiresAuth: true,
        dependsOn: 1,
        description: "Uslubiy nashrlar - yozuvni o'chirish",
        inputFields: {
            entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "academicMethodologicPublicationsEntityId" }
        },
        ported: true
    },
    {
        id: 6,
        category: "46.Akademik Uslubiy nashrlar",
        name: "Qidirish (GET)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAcademicMethodologicPublications/search",
        requiresAuth: true,
        dependsOn: 1,
        description: "Uslubiy nashrlar - qidirish (GET)",
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
        category: "46.Akademik Uslubiy nashrlar",
        name: "Qidirish (POST)",
        method: "POST",
        url: "/app/rest/v2/entities/hemishe_RIAcademicMethodologicPublications/search",
        requiresAuth: true,
        dependsOn: 1,
        description: "Uslubiy nashrlar - qidirish (POST)",
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
    module.exports = endpoints_46;
}
